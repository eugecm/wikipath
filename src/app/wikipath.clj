(ns app.wikipath
    (:gen-class)
    (:import [org.ubiety.ubigraph UbigraphClient]))
(use 'clojure.data.xml)
(import '(java.net URL))
(import '(java.net URLConnection))
(import '(java.lang Thread))
(import '(java.net URLEncoder))

(def ubi-agent (agent (UbigraphClient. )))
(def wikiTree (atom {})) 
(def index (atom {})) 

(defn findTags [xml tagVar]
    (let [tag (keyword tagVar)]
        (filter (fn [x] (= tag (:tag x))) 
            (xml-seq (parse xml)))))

(defn findAttr [xml tagVar attrVar]
    (let [attr (keyword attrVar)]
            (map (fn [x] (attr (:attrs x))) (findTags xml tagVar))))

(defn getLinks [title] ;TODO: Poner user-agent
    (let [api-call (str "http://en.wikipedia.org/w/api.php?format=xml&action=query&prop=links&pllimit=10&titles=" (URLEncoder/encode title) "&redirects")
          ;xml (.openStream (URL. api-call))]
          urlc (.openConnection (URL. api-call))]
        (doall
            (.addRequestProperty urlc "User-Agent" "wikipath/0.1 (https://github.com/keidesu/wikipath) eugeniocano11@alu.uma.es"))
        (let [xml (.getInputStream urlc)]
            (findAttr xml "pl" "title"))))

(defn addToIndex [index entry] 
    (let [i (get @index entry)]
        (cond
            (not i) (swap! index assoc entry (count @index))
            :else i) (get @index entry)))

(defn isIndexed [index entry] 
    (some #(= entry %) index))

(defn initStyle [ubi]
    (doto ubi
        (.clear)
        (.setVertexStyleAttribute 0 "shape" "sphere")
        (.setVertexStyleAttribute 0 "shapedetail" "5")
        (.setVertexStyleAttribute 0 "size" "0.8")
        (.setVertexStyleAttribute 0 "fontsize" "8")))

(defn drawEdge [ubi node-a node-b] 
    (let [index-a (addToIndex index node-a)
          index-b (addToIndex index node-b)] 
        (doto ubi
            (.newVertex index-a)
            (.setVertexAttribute index-a "label" node-a)
            (.newVertex index-b)
            (.setVertexAttribute index-b "label" node-b)
            (.newEdge index-a index-b))))

(defn addToTree [tree parent child] 
    (send-off ubi-agent drawEdge parent child)
    (swap! tree assoc child parent))

(defn explore [parent target & {:keys [max-deepth max-breadth deep path]
                            :or {max-deepth 100 max-breadth 100 deep 0 path []}}] ;Ponerlo bonito
    (if (>= deep max-deepth) nil
        (if (= target parent) (cons parent path)
            (let [children (take max-breadth (getLinks parent))]
                (some identity
                    (map #(do
                            (addToTree wikiTree parent %)
                            (explore % target :deep (inc deep) 
                                       :max-deepth max-deepth 
                                       :max-breadth max-breadth
                                       :path (cons parent path))) children))))))
(defn colorVertex [ubi v color]
    (doto ubi
        (.setVertexAttribute v "color" color)))

(defn showPath [path]
    (doseq [step path]
        (send-off ubi-agent colorVertex (get @index step) "#ff0000")))

(defn -main [& args]
    (send-off ubi-agent initStyle)
    (println "Enter a source")
    (let [source (read-line)]
        (println "Enter a target")
        (let [target (read-line)
              path (explore source target :max-deepth 4 :max-breadth 6)]
            (prn path)
            (println "Done, discovered: " (count @index))
            (showPath path))))
