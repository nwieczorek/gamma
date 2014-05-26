(ns gamma.core
  (:import (javax.swing JFrame))
  (:require [gamma.gui :as gui]
            [gamma.common :as common]))
           

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))

(defn main-window
  []
  (def frame (doto (JFrame. (clojure.string/join " " (common/get-property :title)))
               (.setDefaultCloseOperation JFrame/EXIT_ON_CLOSE )
              ))

  (let [insets (.getInsets frame)
        [pnl timer] (gui/main-panel)
        [display-width display-height] (common/get-property :display-size)]
      (.setContentPane frame pnl)
      (.validate frame)
      (.repaint frame)
      (.setVisible frame true)
      (.setSize frame (+ (.left insets) (.right insets) display-width ) 
                      (+ (.top insets) (.bottom insets) display-height  ))
      (.start timer)

    ))

(defn main
  []
  (common/load-common-properties)
  (main-window))                 
