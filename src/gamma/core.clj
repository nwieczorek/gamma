(ns gamma.core
  (:import (javax.swing JFrame))
  (:require [gamma.launcher :as launcher]
            [gamma.tileset :as tileset]
            [gamma.unit :as unit]
            [gamma.common :as common]))
           




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
        factions (map #(unit/make-faction % faction-props) (:factions faction-props))
        title (clojure.string/join " " (common/get-property :title))
        resources {:factions factions :title title}]
    (prn factions)
    (launcher/launcher-window resources))
  
  )

