(defproject wikipath "0.1-pre"
  :description "A program to find a path between to wiki articles"
  :main app.wikipath
  :dependencies [[ubigraph "0.2.4"]
                [org.apache.xmlrpc/xmlrpc-client "3.1.3"]
                [org.clojure/data.xml "0.0.7"]
                [org.clojure/clojure "1.3.0"]]
  :repositories {"local" "file:maven_rep"})
