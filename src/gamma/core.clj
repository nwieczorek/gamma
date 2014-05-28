(ns gamma.core
  (:import (javax.swing JFrame))
  (:require [gamma.gui :as gui]
            [gamma.launcher :as launcher]
            [gamma.common :as common]))
           

(defn main-window
  [title]
  (def frame (doto (JFrame. title)
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
  (let [title (clojure.string/join " " (common/get-property :title))]
    ;(main-window title)))                 
    (launcher/launcher-window title)))                 
