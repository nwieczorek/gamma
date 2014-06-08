(ns gamma.gui
  (:import (javax.swing JPanel Timer )
           javax.imageio.ImageIO
           (java.awt.event ActionListener MouseListener MouseMotionListener MouseEvent KeyListener)
           (java.awt Color RenderingHints Toolkit Image Font))
  (:require [gamma.tileset :as tileset]
            [gamma.grid :as grid]
            [gamma.common :as common]))

;========================================================================
; General Functions
;
;


(defn load-font
  ([font-file style size]
    (let [load-class (.getClass (Thread/currentThread))
        unsized (Font/createFont Font/TRUETYPE_FONT (.getResourceAsStream load-class (str "/" font-file)))]
      (.deriveFont unsized style (float size))))
  ([font-file size]
   (load-font font-file Font/PLAIN size)))

(defn get-text-height
  [^java.awt.Graphics2D g2d]
  (let [frc (.getFontRenderContext g2d)
        lm (.getLineMetrics (.getFont g2d), "A", frc )]
    (int (.getAscent lm))))


(defn mouse-event-data
  [e]
  (let [btn (.getButton e)
       x (.getX e)
       y (.getY e)
       btn-key (cond (= btn MouseEvent/BUTTON1) :left
                    (= btn MouseEvent/BUTTON3) :right
                    :else btn)]
  [x y btn-key])) 


(defn translate-coord-to-cell
  [coord-x coord-y tile-width tile-height tile-offset-x tile-offset-y]
  [ (quot (- coord-x tile-offset-x)  tile-width) (quot (- coord-y tile-offset-y) tile-height)])

(defn translate-cell-to-coord
  [cell-x cell-y tile-width tile-height tile-offset-x tile-offset-y]
    [(+ (* cell-x tile-width) tile-offset-x) (+ (* cell-y tile-height) tile-offset-y)])

(defn draw-text
  [^java.awt.Graphics2D g2d text x y1 color font]
    (.setColor g2d color)
    (.setFont g2d font)
      (let [text-height (get-text-height g2d) 
            y (+ text-height y1 )]
       (.drawString g2d text x y)))



;=========================================================================
(def STATE-PLACEMENT :placement)

;=========================================================================
(defn main-panel
  [resources]
  (let [[max-world-width max-world-height] (common/get-property :max-world-size)
        [tile-width tile-height] (common/get-property :tile-size)
        [tile-offset-x tile-offset-y] (common/get-property :map-offset)
        unit-font (load-font (common/get-property :unit-font) (common/get-property :unit-font-size))
        unit-bold-font (load-font (common/get-property :unit-font) Font/BOLD (common/get-property :unit-font-size))
        message-font (load-font (common/get-property :message-font) (common/get-property :message-font-size))
        [message-offset-x message-offset-y-raw] (common/get-property :message-offset)

        faction-map {:A (:player-a-faction resources) :B (:player-b-faction resources)}

        world (atom (grid/make-world (str (common/get-property :scenario-folder) (:scenario-file resources)) max-world-width max-world-height faction-map))
        message-offset-y  (+ message-offset-y-raw (* tile-height (+ 1 (:height @world)))) 

        hover-cell (atom nil)
        message-text (atom "Hi There")
        active-cells (atom nil)
        current-state (atom STATE-PLACEMENT)
        ]
        
        (defn update
          []
          (reset! message-text "fdfdfd" ))

            
        (defn draw-message
          [g2d watcher]
          (when-let [msg @message-text]
            (draw-text g2d msg message-offset-x message-offset-y Color/BLACK message-font)))

        (defn translate-event-to-world
          [x y ]
          ;attempt to map to a square on the map
          (let [[cell-x cell-y] (translate-coord-to-cell x y tile-width tile-height tile-offset-x tile-offset-y)]
            (when (grid/valid? @world cell-x cell-y)
              [:world cell-x cell-y ])))


        (defn translate-event
          "returns [cell-x cell-y <:world or :hand>] or nil if no matching cell found" 
          [x y  ]
          (translate-event-to-world x y)
              )


        (defn handle-click 
          [x y btn]
          (when-let [cell-event (translate-event x y)]
            (let [[event-type cell-x cell-y] cell-event]
              (cond (= event-type :world)
                    (do
                      (prn (str "Clicked on cell " cell-x "," cell-y))
                      (case @current-state 
                        :start-play
                          (do
                            (prn "start play")
                            )
                        ))
                    ))))

        (defn handle-mouse-over 
          [x y]
          (reset! hover-cell nil)
          (when-let [cell-event (translate-event x y)]
            (let [[event-type cell-x cell-y] cell-event]
              (cond (= event-type :world)
                    (reset! hover-cell [cell-x cell-y])
                    ))))

        (let [proxy-panel  (doto 
                (proxy [javax.swing.JPanel] []
                  (paintComponent [^java.awt.Graphics g]
                    (proxy-super paintComponent g)
                    (let [g2d (doto ^java.awt.Graphics2D 
                                  (.create g))]
                      (.setRenderingHint g2d RenderingHints/KEY_TEXT_ANTIALIASING 
                                         RenderingHints/VALUE_TEXT_ANTIALIAS_ON)
                      (update)
                      (grid/for-each-cell 
                        @world
                        (fn [cell]
                          (let [[ix iy] (translate-cell-to-coord (:x cell) (:y cell) tile-width tile-height tile-offset-x tile-offset-y)
                                terrain-image (tileset/get-tile :map (:terrain cell))]
                            (.drawImage g2d terrain-image ix iy this)
                            (when-let [u (:unit cell)]
                              (let [unit-image (tileset/get-tile :unit (:image-key u))]
                                (.drawImage g2d unit-image ix iy this)))

                               
                            (when (not (nil? @hover-cell))
                              (let [[h-x h-y] @hover-cell]
                                (when (and (= h-x (:x cell)) (= h-y (:y cell)))
                                  (.drawImage g2d (tileset/get-tile :map :hover) ix iy this))))
                            )))
                      (draw-message g2d this)
                      )))
                (.addMouseListener (proxy [MouseListener] []
                                     (mouseClicked [e] )
                                     (mouseEntered [e] )
                                     (mouseExited [e] )
                                     (mousePressed [e] )
                                     (mouseReleased [e] 
                                       (let [[x y btn] (mouse-event-data e)]
                                                  (handle-click x y btn )))
                                     ))
                (.addMouseMotionListener (proxy [MouseMotionListener] []
                                     (mouseDragged [e])
                                     (mouseMoved [e] 
                                       (let [[x y btn] (mouse-event-data e)]
                                         (handle-mouse-over x y ))))
                                     )) 
              timer (Timer. (common/get-property :repaint-milliseconds) 
                           (proxy [ActionListener] []
                              (actionPerformed [event] 
                               (.repaint proxy-panel))))
        ]

      [proxy-panel timer] )))



(defn main
  []
  (prn (load-font "Caudex-Regular.ttf")))
