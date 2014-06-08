(ns gamma.unit
  (:require [gamma.common :as common]))

(def PLAYER-A :A)
(def PLAYER-B :B)
(def VALID-PLAYERS (list PLAYER-A PLAYER-B))


(def UNIT-TYPES
  {:H :heavy
   :T :tactical
   :A :assault
   :S :special })

(def VALID-UNIT-TYPE-CODES (keys UNIT-TYPES))
(def VALID-UNIT-TYPE-NAMES (vals UNIT-TYPES))
;=============================================================
; Faction Stuff
;
(defn make-faction
  [faction-name faction-props]
  (let [name-key (common/keyword-append faction-name "name")
        icon-key (common/keyword-append faction-name "blank")
        unit-map (reduce #(assoc %1 %2 (faction-props (common/keyword-append faction-name %2)))  {} VALID-UNIT-TYPE-NAMES)]
    (prn (str "name key " name-key " icon key " icon-key))
    {:display-name (clojure.string/join " " (name-key faction-props))
     :name faction-name
     :units unit-map
     :icon-key icon-key }))

;=============================================================
; Unit Stuff
;


(defn make-unit
  [unit-type faction]
  (let [unit-type-name (UNIT-TYPES unit-type)
        stats (unit-type-name (:units faction))
       image-key (common/keyword-append (:name faction) unit-type-name)
       u {:image-key image-key :stats stats :faction faction }]
   (prn u) 
   u))

(defn make-unit-from-placement
  [placement faction]
  
  (let [player (:player placement)
        _ (assert (some #(= % player) VALID-PLAYERS) (str "Invalid Player " player))
        unit-type (:unit-type placement)
        _ (assert (some #(= % unit-type) VALID-UNIT-TYPE-CODES) (str "Invalid Unit Type " unit-type))]

      (assoc (make-unit unit-type faction) :player player )))


    
