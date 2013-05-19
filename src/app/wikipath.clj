(ns app.wikipath
    (:gen-class)
    (:import [org.ubiety.ubigraph UbigraphClient]))
(use 'clojure.data.xml)
(import '(java.net URL))
(import '(java.net URLConnection))
(import '(java.lang Thread))
(import '(java.net URLEncoder))

;;;;;Define variables for Ubigraph and the search algorith;;;;;;
(def ubi-agent (agent (UbigraphClient. ))) ;The Ubigraph client, defined as an agent.
(def wikiTree (atom {}))                   ;A map that represents the tree being explored.
(def index (atom {}))                      ;An index for Ubigraph to keep track of the nodes being drawn.

;Filters to apply to nodes before exploring them.
(def filters [
    (fn [x] (not (re-find #"^[0-9]" x)))
    (fn [x] (> 25 (count x)))
    (fn [x] (not (get @wikiTree x)))
    ])

;;;;;Wikipedia API related functions;;;;;;
;Find tags from xml nodes.
(defn findTags [xml tagVar]
    (let [tag (keyword tagVar)]
        (filter (fn [x] (= tag (:tag x))) 
            (xml-seq (parse xml)))))

;Find attributes from xml nodes.
(defn findAttr [xml tagVar attrVar]
    (let [attr (keyword attrVar)]
            (map (fn [x] (attr (:attrs x))) (findTags xml tagVar))))

;Obtain the links from an article in Wikipedia.
(defn getLinks [title]
    (let [api-call (str "http://en.wikipedia.org/w/api.php?format=xml&action=query&prop=links&pllimit=15&titles=" (URLEncoder/encode title) "&redirects")
         ;xml (.openStream (URL. api-call))]
          urlc (.openConnection (URL. api-call))]
        (doall
            (.addRequestProperty urlc "User-Agent" "wikipath/0.1 (https://github.com/keidesu/wikipath) eugeniocano11@alu.uma.es"))
        (let [xml (.getInputStream urlc)]
            (findAttr xml "pl" "title"))))

;;;;;Ubigraph related functions;;;;;;
;Add an entry to an index.
(defn addToIndex [index entry] 
    (get (swap! index assoc entry (count @index)) entry))

;Check if an entry is already indexed.
(defn isIndexed? [index entry] 
    (get @index entry))

;Change the color of a vertex.
(defn colorVertex [ubi v color]
    (doto ubi
        (.setVertexAttribute v "color" color)))

;Changes the color of all the nodes found in "path".
(defn showPath [path]
    (doseq [step path]
        (send ubi-agent colorVertex (get @index step) "#ff0000")))

;Initialize the style of the objects.
(defn initStyle [ubi]
    (doto ubi
        (.clear)
        (.setVertexStyleAttribute 0 "shape" "sphere")
        (.setVertexStyleAttribute 0 "shapedetail" "5")
        (.setVertexStyleAttribute 0 "size" "0.8")
        (.setVertexStyleAttribute 0 "fontsize" "8")))

;Draw a vertex and add a label, return the index.
;If the node was already indexed the node won't be redrawn.
(defn drawVertex [ubi node]
    (if-let [index-a (isIndexed? index node)] index-a
        (let [index-a (addToIndex index node)]
            (doto ubi
                (.newVertex index-a)
                (.setVertexAttribute index-a "label" node)) index-a)))

;Draw the relation between a parent node and a child node, in this case
;it will draw both nodes and draw an edge between them.
(defn drawRelation [ubi parent child]
    (let [p-index (drawVertex ubi parent)
          c-index (drawVertex ubi child)]
        (.newEdge ubi p-index c-index) ubi))

;;;;;Search algorithm functions;;;;;;
;Adds the relation parent-child to the tree and sends a message to
;Ubigraph client to draw it.
(defn addToTree [tree parent child] 
    (send ubi-agent drawRelation parent child)
    (swap! tree assoc child parent))

;Depth-first search. Returns a valid path between the target and the parent.
;Returns nil if the path was not found.
(defn explore-dfs [parent target & {:keys [max-depth max-breadth deep path]
                            :or {max-depth 100 max-breadth 100 deep 0 path []}}]
    (if (= target parent) (cons parent path)
        (if (>= deep max-depth) nil
            (let [children (->>
                            (getLinks parent)
                            (filter (apply every-pred filters))
                            (take max-breadth))]
                (some identity
                    (map #(do
                            (addToTree wikiTree parent %)
                            (explore-dfs % target :deep (inc deep) 
                                       :max-depth max-depth 
                                       :max-breadth max-breadth
                                       :path (cons parent path))) children))))))

;Breadth-first search. Returns a path between start and end, nil if the path
;was not found. "iters" is the number of nodes to expand.
(defn explore-bfs [start end iters max-breath]
    (swap! wikiTree assoc start nil)
    (loop [toExplore (conj clojure.lang.PersistentQueue/EMPTY start)
           deep 0]
        (if (> deep iters) nil
            (when-let [node (peek toExplore)]
                (let [children (->>
                                (getLinks node)
                                (filter (apply every-pred filters))
                                (take max-breath))]
                    (doall (map #(addToTree wikiTree node %) children))
                    (if (some #{end} children)
                        (cons end (take-while identity (iterate @wikiTree node)))
                        (recur (into (pop toExplore) children)
                                   (inc deep))))))))

;Main function, notice that we wait 5 seconds in order to let ubigraph finish
;the drawing
(defn -main [& args]
    (send-off ubi-agent initStyle)
    (println "Enter a source")
    (let [source (read-line)]
        (println "Enter a target")
        ;(let [target (read-line)
        ;      path (explore-dfs source target :max-depth 4 :max-breadth 6)]
        (let [target (read-line)
              path (explore-bfs source target 10 10)]
            (prn path)
            (println "Done, discovered: " (count @index))
            (Thread/sleep 5000)
            (showPath path))))
