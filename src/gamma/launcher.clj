(ns gamma.launcher
  (:import (javax.swing JFrame JComboBox JLabel JButton JPanel)
           (javax.swing.border EmptyBorder)
           (java.awt FlowLayout GridLayout )
           (java.awt.event ActionListener))
  (:require [gamma.common :as common]))

(defn load-map-file-list
  []
  (common/get-resource-list "/maps/"))


(def player-options (list "Human" "Computer"))
(def factions (list "Metal Militia" "Reborn Sons"))

(defn launcher-window
  [title]
  (def launcher (doto (JFrame. title)
               (.setDefaultCloseOperation JFrame/EXIT_ON_CLOSE )
              ))
  (let [insets (.getInsets launcher)
        [display-width display-height] (common/get-property :launcher-size)
        [grid-horizontal-space grid-vertical-space] (common/get-property :launcher-spacing)
        map-label (JLabel. "Select map file:")
        map-file-combo (JComboBox.)
        player-a-label (JLabel. "Player A")
        player-b-label (JLabel. "Player B")
        player-a-control-combo (JComboBox.)
        player-b-control-combo (JComboBox.)
        player-a-faction-combo (JComboBox.)
        player-b-faction-combo (JComboBox.)
        go-button (JButton. "Start")
        p (JPanel.)]

      ;add the items to the map-file-combo
      (doseq [map-file (load-map-file-list)]
        (.addItem map-file-combo map-file))

      (doseq [opt player-options]
        (.addItem player-a-control-combo opt)
        (.addItem player-b-control-combo opt))

      (.setSelectedItem player-a-control-combo (first player-options))
      (.setSelectedItem player-b-control-combo (second player-options))

      (doseq [faction factions]
        (.addItem player-a-faction-combo faction)
        (.addItem player-b-faction-combo faction))



      (.setContentPane launcher p)
      (.setBorder p (EmptyBorder. 10 10 10 10))
      (.setLayout p (GridLayout. 5 2 grid-horizontal-space grid-vertical-space))
      (.add p map-label)
      (.add p map-file-combo)
      (.add p player-a-label)
      (.add p player-b-label)
      (.add p player-a-control-combo)
      (.add p player-b-control-combo)
      (.add p player-a-faction-combo)
      (.add p player-b-faction-combo)
      (.add p go-button)

      (.validate launcher)
      (.repaint launcher)
      (.setVisible launcher true)
      (.setSize launcher (+ (.left insets) (.right insets) display-width ) 
                      (+ (.top insets) (.bottom insets) display-height  ))

      (.addActionListener go-button 
                          (proxy [ActionListener] []
                            (actionPerformed [event]
                              (prn "Button clicked")
                              (prn (str "A:" (.getSelectedItem player-a-control-combo)))
                              (prn (str "B:" (.getSelectedItem player-b-control-combo)))
                              (prn event))))

    ))


