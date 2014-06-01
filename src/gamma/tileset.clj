(ns gamma.tileset
  (:import (java.io BufferedReader FileReader InputStreamReader)
           (javax.swing ImageIcon)
           javax.imageio.ImageIO)
  (:require [gamma.common :as common]))




(defn get-subimage
  [full-image tile-x tile-y tile-width tile-height kw]
    (let [x (* tile-x tile-width)
          y (* tile-y tile-height)
          full-height (.getHeight full-image)
          full-width (.getWidth full-image)]
      (assert (and (< (+ x tile-width) full-width) (< (+ y tile-height) full-height))
              (str "Subimage " kw " at " tile-x "," tile-y "(" x "," y ")" 
                   " outside " full-width "," full-height))
    (.getSubimage full-image x y tile-width tile-height)))



(defn get-image-tiler
  [full-image tile-def [tile-width tile-height]]
    (fn [image-map kw]
      (let [[tile-x tile-y] (tile-def kw)
            subimg (get-subimage full-image tile-x tile-y tile-width tile-height kw )]
        (assoc image-map kw subimg))))



(defn load-tileset
  [def-filename image-filename tile-size]
  (assert (not (nil? def-filename)) "Definition Filename undefined")
  (assert (not (nil? image-filename)) "Image Filename undefined")
  (let [load-class (.getClass (Thread/currentThread))
        ts-def (common/load-property-file def-filename )
        tile-image-file image-filename
        _ (prn (str "loading " tile-image-file))
        full-image (ImageIO/read (.getResource load-class (str "/" tile-image-file)) )
        ]

    (reduce (get-image-tiler full-image ts-def tile-size) {} (keys ts-def))
    ))


(def ^:dynamic *common-tilesets* (atom nil))

(defn load-common-tileset
  [def-filename image-filename tile-size tileset-key]
  (let [new-tileset (load-tileset def-filename image-filename tile-size)]
    (reset! *common-tilesets* (assoc @*common-tilesets* tileset-key new-tileset))))

(defn get-tile
  [tileset-key image-key]
  ((@*common-tilesets* tileset-key) image-key))


