(ns gamma.core
  (:import (javax.swing JFrame))
  (:require [gamma.launcher :as launcher]
            [gamma.tileset :as tileset]
            [gamma.common :as common]))
           

(defn make-faction
  [faction-name faction-props]
  (let [name-key (common/keyword-append faction-name "name")
        icon-key (common/keyword-append faction-name "blank")]
    (prn (str "name key " name-key " icon key " icon-key))
    {:name (clojure.string/join " " (name-key faction-props))
     :icon-key icon-key }))



(defn main
  []
  (common/load-common-properties)
  (tileset/load-common-tileset (common/get-property :map-tileset-def) 
                               (common/get-property :map-tileset-file) 
                               (common/get-property :tile-size)
                               :map)
  (tileset/load-common-tileset (common/get-property :unit-tileset-def) 
                               (common/get-property :unit-tileset-file) 
                               (common/get-property :tile-size)
                               :unit)

  (let [faction-props (common/load-property-file (common/get-property :faction-def))
        factions (map #(make-faction % faction-props) (:factions faction-props))
        title (clojure.string/join " " (common/get-property :title))
        resources {:factions factions :title title}]
    (prn factions)
    (launcher/launcher-window resources))
  
  )

