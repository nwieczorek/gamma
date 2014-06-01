(ns gamma.launcher
  (:import (javax.swing JFrame JComboBox JLabel JButton JPanel ImageIcon)
           (javax.swing.border EmptyBorder)
           (java.awt FlowLayout GridLayout Dimension )
           (java.awt.event ActionListener))
  (:require [gamma.common :as common]
            [gamma.tileset :as tileset]
            [gamma.gui :as gui]))

(defn load-map-file-list
  []
  (common/get-resource-list "/maps/"))

(defn main-window
  [resources]
  (def frame (doto (JFrame. (:title resources))
               (.setDefaultCloseOperation JFrame/EXIT_ON_CLOSE )
              ))

  (let [insets (.getInsets frame)
        [pnl timer] (gui/main-panel resources)
        [display-width display-height] (common/get-property :display-size)]
      (.setContentPane frame pnl)
      (.validate frame)
      (.repaint frame)
      (.setVisible frame true)
      (.setSize frame (+ (.left insets) (.right insets) display-width ) 
                      (+ (.top insets) (.bottom insets) display-height  ))
      (.start timer)

    ))

(def player-options (list "Human" "Computer"))

(defn launcher-window
  [resources]
  (def launcher (doto (JFrame. (:title resources))
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
        p (JPanel.)
        launcher-border (common/get-property :launcher-border)]

      ;add the items to the map-file-combo
      (doseq [map-file (load-map-file-list)]
        (.addItem map-file-combo map-file))

      (doseq [opt player-options]
        (.addItem player-a-control-combo opt)
        (.addItem player-b-control-combo opt))

      (.setSelectedItem player-a-control-combo (first player-options))
      (.setSelectedItem player-b-control-combo (second player-options))


      (doseq [faction (:factions resources)]
        (.addItem player-a-faction-combo (ImageIcon. (tileset/get-tile :unit (:icon-key faction))))
        (.addItem player-b-faction-combo (ImageIcon. (tileset/get-tile :unit (:icon-key faction))))
        )



      (.setContentPane launcher p)
      (.setBorder p (EmptyBorder. launcher-border launcher-border launcher-border launcher-border ))
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
                              (let [new-resources (assoc resources 
                                                         :player-a-control (.getSelectedItem player-a-control-combo)
                                                         :player-b-control (.getSelectedItem player-b-control-combo)
                                                         :player-a-faction (.getSelectedItem player-a-faction-combo)
                                                         :player-b-faction (.getSelectedItem player-b-faction-combo)
                                                         :map-file (.getSelectedItem map-file-combo))]
                                (main-window new-resources))
                              (.setVisible launcher false)
                              )))

    ))


