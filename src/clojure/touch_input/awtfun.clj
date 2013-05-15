(ns touch-input.awtfun
  (:use touch-input.core)
  (:import java.awt.Frame))


(def frame (Frame. "hoi"))

(def lolsatom (atom #{}))

(def wmtouch 0x240)
(def wmgesture 0x119)
(def touch-events (atom '()))

(defn handle-wmtouch [wparam lparam]
  (swap! touch-events conj wparam))

(defn yay-events [code wparam lparam]
  (swap! lolsatom conj code)
  (if (= wmtouch code)
    (handle-wmtouch wparam lparam))
  0)

(defn ohsuchfun []
  (doto (handle frame)
    (register-touch-window)
    (set-message-hook! #(yay-events %1 %2 %3))))
