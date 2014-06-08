
(ns gamma.grid
  (:import (java.io BufferedReader FileReader InputStreamReader))
  (:require [gamma.unit :as unit]
            [gamma.common :as common]))


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
        (let [c (first line)
              cell-key (get-coord-key x-coord y-coord)]
          (recur (rest line)
            (case c
              \X (assoc the-map cell-key :wall)
              the-map)
            (+ x-coord 1))
          )))))


(defn resolve-placement-line
  "Helper function for loading scenario files
  Parse the unit placement lines"
  [def-place full-line]
  (if (clojure.string/blank? full-line)
    def-place
    (let [tokens (map read-string (clojure.string/split full-line #"\s+"))
          _ (assert (= (count tokens) 4) (str "Invalid Placement " full-line))
          x (first tokens)
          y (nth tokens 1)
          player (keyword (nth tokens 2))
          unit-type (keyword (nth tokens 3))
          the-key (get-coord-key x y)]
      (assoc def-place the-key {:player (keyword player) :unit-type unit-type}))
  ))

(defn load-scenario-file
  [filename]
  (let [load-class (.getClass (Thread/currentThread))
        istream (.getResourceAsStream load-class filename)
        _ (assert (not (nil? istream)) (str "Could not open stream for " filename)) ]
    (with-open [rdr (BufferedReader. (InputStreamReader. istream))]
      (loop [lines (line-seq rdr)
             def-map {}
             def-place {}
             y-coord 0
             width-max 0
             section :none]
        (prn (str "section:" section " line:" (first lines)))
        (cond 
          (empty? lines) [def-map def-place width-max (+ 1 y-coord)]
          (= (first lines) "START MAP") (recur (rest lines) def-map def-place y-coord width-max :map)
          (= (first lines) "END MAP") (recur (rest lines) def-map def-place y-coord width-max :none)
          (= (first lines) "START PLACEMENT") (recur (rest lines) def-map def-place y-coord width-max :placement)
          (= (first lines) "END PLACEMENT") (recur (rest lines) def-map def-place y-coord width-max :none)
          (= section :map)
            (recur (rest lines) 
                   (resolve-map-line def-map y-coord (first lines))
                   def-place 
                   (+ y-coord 1) 
                   (max (count (first lines)) width-max)
                   section)
          (= section :placement)
            (recur (rest lines)
                   def-map
                   (resolve-placement-line def-place (first lines))
                   y-coord
                   width-max
                   section)
          :default 
            (recur (rest lines) def-map def-place y-coord width-max section)
          )
          ))))

      

(defn make-world
  [filename max-width max-height faction-map ]
  (let [[def-map def-place width height] (load-scenario-file filename)
        ;_ (prn width "," height)
        mapped-grid (update-grid
                        (make-grid (min width max-width) (min height max-height) {:terrain :clear})
                        (fn [x y cell]
                          (if-let [cell-object (def-map (get-coord-key x y))]
                            (assoc cell :terrain cell-object)
                            cell
                              )))
        the-grid (update-grid
                   mapped-grid
                   (fn [x y cell]
                     (if-let [placement (def-place (get-coord-key x y))]
                       (let [u (unit/make-unit-from-placement placement 
                                                              (faction-map (:player placement)))]
                        (assoc cell :unit u))
                       cell)))
                      ]
    the-grid))


