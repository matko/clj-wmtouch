(ns touch-input.quilfun
  (:import java.util.Date TouchInput
           com.sun.jna.platform.win32.WinNT$HANDLE)
  (:use touch-input.core
        quil.core))

(defn setup []
  (smooth)                          ;;Turn on anti-aliasing
  (frame-rate 60)                   ;;Set framerate to 60 FPS
  (background 200))                 ;;Set the background colour to
                                    ;;  a nice shade of grey.
(defn draw []
  (stroke (random 255))             ;;Set the stroke colour to a random grey
  (stroke-weight (random 10))       ;;Set the stroke thickness randomly
  (fill (random 255))               ;;Set the fill colour to a random grey

  (let [diam (random 100)           ;;Set the diameter to a value between 0 and 100
        x    (random (width))       ;;Set the x coord randomly within the sketch
        y    (random (height))]     ;;Set the y coord randomly within the sketch
    (ellipse x y diam diam)))       ;;Draw a circle at x y with the correct diameter

(defsketch example                  ;;Define a new sketch named example
  :title "Oh so many grey circles"  ;;Set the title of the sketch
  :setup setup                      ;;Specify the setup fn
  :draw draw                        ;;Specify the draw fn
  :size [323 200])                  ;;You struggle to beat the golden ratio

(def points-list (atom (list)))

(defn touch-fun [points]
  (swap! points-list conj points))
  
(def hook nil)

(defn ohsuchfun []
  (def hook
    (setup-touch! example
                  #(touch-fun %))))
