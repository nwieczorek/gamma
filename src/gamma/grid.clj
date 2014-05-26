
(ns gamma.grid
  (:import (java.io BufferedReader FileReader InputStreamReader))
  (:require [gamma.common :as common]))


(defn make-grid-cells
  [width height maker-func]
  (into 
    [] 
    (for [y (range height)]
      (into
        []
        (for [x (range width)]
          (maker-func x y))))))


(defn make-grid
  [width height initial-cell]
  { :width width :height height
   :cells  (make-grid-cells width height #(assoc initial-cell :x %1 :y %2  )) 
   })



(defn get-cell
  [grid x y]
    (let [cells (grid :cells)]
      ((cells y) x)))

(defn match-and-update-grid
  "Return an updated grid
  match-func is a function with signature (f x y) that returns true if the cell should be updated
  update-func is a function with signature (f cell) that returns the updated cell"
  ([grid match-func update-func]
    (let [updater (fn [ex ey]
                    (let [ecell (get-cell grid ex ey)]
                      (if (match-func ex ey)
                        (update-func ecell)
                        ecell)))]
      (assoc grid :cells (make-grid-cells (:width grid) (:height grid) updater))))
  ([grid x y update-func]
    (let [match-func #(and (= x %1) (= y %2))]
      (match-and-update-grid grid match-func update-func))))


(defn update-grid
  "Update cell or cells in the grid
  update-func returns nil if no update is to be made to that cell, the update cell otherwise"
  [grid update-func]
  (let [updater (fn [ex ey]
                  (let [ecell (get-cell grid ex ey)]
                    (if-let [new-cell (update-func ex ey ecell)]
                      new-cell
                      ecell)))]
    (assoc grid :cells (make-grid-cells (:width grid) (:height grid) updater))))

(defn valid?
  [grid x y]
  (let [height (grid :height)
        width (grid :width)]
    (and (>= x 0) (>= y 0)
         (< y height) (< x width))))

(def orthogonal [ [-1 0] [1 0] [0 -1] [0 1]])

(defn add-coords
  [[x1 y1] [x2 y2]]
  [(+ x1 x2) (+ y1 y2)])

(defn get-adjacent-cells
  "returns the orthogonal adjacent cells"
  [grid x y]
  (let [valid-coords (filter #(valid? grid (% 0) (% 1)) 
                           (map #(add-coords % [x y]) orthogonal))]
    (map (fn [[ix iy]] (get-cell grid ix iy)) valid-coords)))


(defn for-each-cell
  [grid func]
  (doseq [row (:cells grid)]
    (doseq [cell row]
      (func cell))))


(defn filter-cells
  "return a sequence of cells where the predicate is true"
  [grid func]
  (filter func (flatten (:cells grid))))


(defn get-coord-key
  [x y]
  (str x "-" y)) 

;==========================================================
;
(defn resolve-map-line
  "Helper function for loading map files
  Parse the line "
  [def-map y-coord full-line]
  (if (clojure.string/blank? full-line)
    def-map
    (loop [line full-line
           the-map def-map
           x-coord 0]
      (if (empty? line)
        the-map
        (let [c (first line)]
          (recur (rest line)
            (case c
              \X (assoc the-map (get-coord-key x-coord y-coord) :wall)
              the-map)
            (+ x-coord 1))
          )))))


(defn load-map-file
  [filename]
  (let [load-class (.getClass (Thread/currentThread))
        istream (.getResourceAsStream load-class (str "/" filename))
        _ (assert (not (nil? istream)) (str "Could not open stream for " filename)) ]
    (with-open [rdr (BufferedReader. (InputStreamReader. istream))]
      (loop [lines (line-seq rdr)
             def-map {}
             y-coord 0
             width-max 0]
        (if (empty? lines)
          [def-map width-max (+ 1 y-coord)] 
          (recur (rest lines) 
                 (resolve-map-line def-map y-coord (first lines)) 
                 (+ y-coord 1) 
                 (max (count (first lines)) width-max)))
          ))))

      

(defn make-world
  [filename max-width max-height]
  (let [[def-map width height] (load-map-file filename)
        _ (prn width "," height)
        the-grid (update-grid
                        (make-grid (min width max-width) (min height max-height) {:terrain :clear})
                        (fn [x y cell]
                          (if-let [terrain (def-map (get-coord-key x y))]
                            (assoc cell :terrain terrain))))
                      ]
    the-grid))


