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

(def lolsatom (atom #{}))

(def wmtouch 0x240)
(def wmgesture 0x119)
(def touch-events (atom '()))

(defn low-word [num]
  (bit-and 0xFFFF num))

(defn as-handle [param]
  (WinNT$HANDLE. (.toPointer param)))

(def argh (atom :none))

(defn handle-wmtouch [touch-count handle]
  (let [ti (TouchInput.)
        inputs (.toArray ti touch-count)
        size (.size ti)]
    (reset! argh :first)
    (.GetTouchInputInfo nativeuser handle touch-count inputs size)
    (reset! argh :second)
    (swap! touch-events conj (map (fn [input]
                                    {:x (.x input)
                                     :y (.y input)
                                     :id (.dwID input)
                                     :dwFlags (.dwFlags input)})
                                  inputs)))
  -1)

(def last-event-time 0)

(def foomoo nil)

(defn yay-events [code wparam lparam]
  (def last-event-time (Date.))
  (swap! lolsatom conj code)
  (def foomoo (type code))
  (when (= wmtouch code)
    (reset! argh :beforefirst)
    (handle-wmtouch (-> wparam .intValue low-word)
                    (as-handle lparam)))
  -1)

(defn yay-events-proxy [code wparam lparam]
  (yay-events code wparam lparam))

(def hook nil)

(defn ohsuchfun []
  (invoke-void-method (fn []
                        (let [handle (handle example)]
                          (register-touch-window handle)
                          (def hook (set-message-hook! handle yay-events-proxy))))))

