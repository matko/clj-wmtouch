(ns touch-input.core
  (:import IUser IAWTCallback Hooks$IGetMsgProc
           [com.sun.jna Native Pointer Callback]
           [com.sun.jna.win32 StdCallLibrary StdCallLibrary$StdCallCallback]
           [com.sun.jna.platform.win32 WinUser WinDef$HWND WinDef$WPARAM WinDef$LPARAM BaseTSD$LONG_PTR WinDef$LRESULT WinNT$HANDLE]
           java.util.Date))
            

(def nativeuser (Native/loadLibrary "user32" IUser))
(def NativeKernel (gen-interface :name NativeKernel
                                 :extends [com.sun.jna.platform.win32.Kernel32]
                                 :methods [[GetLastError [] int]]))
(def nativekernel (Native/loadLibrary "kernel32" NativeKernel))

(defn get-awt-window []
  (.FindWindowA nativeuser "SunAwtToolkit" "theAwtToolkitWindow"))

(def invoke-void-method-msg 0x982a) ;;HAX

(defn invoke-void-method [f]
  (let [result (promise)
        callback (reify IAWTCallback
                   (callback [this]
                     (deliver result
                              (f))))]
    (.SendMessageA nativeuser
                   (get-awt-window)
                   invoke-void-method-msg
                   callback
                   (WinDef$LPARAM.))
    @result))

(defn register-touch-window [handle]
  (.RegisterTouchWindow nativeuser
                        handle
                        0))
        
(defn handle [component]
  (-> (.. component getPeer getHWnd)
      Pointer.
      WinDef$HWND.))


(defn window-handle [applet]
  (handle @(:target-obj (meta applet))))


(def wh-getmessage 3)

(defn window-thread [handle]
  (.GetWindowThreadProcessId nativeuser
                             handle
                             nil))

(defn set-message-hook! [handle f]
  (let [callback (reify Hooks$IGetMsgProc
                       (callback [this nCode wParam msg]
                         (let [result (f (.message msg) (.wParam msg) (.lParam msg))]
                           (if (and (integer? result)
                                    (>= result 0))
                             (WinDef$LRESULT. result)
                             (.CallNextHookEx nativeuser
                                              nil
                                              nCode
                                              wParam
                                              msg)))))]
    [(.SetWindowsHookExA nativeuser
                         wh-getmessage
                         callback
                         nil
                         (window-thread handle))
     callback]))
    

(defn remove-message-hook! [hook]
  (.UnhookWindowsHookEx nativeuser hook))

(def wmtouch 0x240)
(def wmgesture 0x119)

(defn low-word [num]
  (bit-and 0xFFFF num))

(defn as-handle [param]
  (WinNT$HANDLE. (.toPointer param)))

(def ^:dynamic wmtouch-callback nil)

(defn decode-dwflags [dwflags]
  (cond (bit-test dwflags 2) :up
        (bit-test dwflags 1) :down
        (bit-test dwflags 0) :move))

(defn handle-wmtouch [touch-count handle]
  (let [ti (TouchInput.)
        inputs (.toArray ti touch-count)
        size (.size ti)]
    (.GetTouchInputInfo nativeuser handle touch-count inputs size)
    (wmtouch-callback (map (fn [input]
                             {:x (int (/ (.x input) 100))
                              :y (int (/ (.y input) 100))
                              :id (.dwID input)
                              :dwFlags (decode-dwflags (.dwFlags input))})
                           inputs))))
;    (swap! touch-events conj (map (fn [input]
;                                    {:x (.x input)
;                                     :y (.y input)
;                                     :id (.dwID input)
;                                     :dwFlags (.dwFlags input)})
;                                  inputs))))
 
(def last-event-time 0)

(def received-event-types (atom #{}))

(defn handle-event [code wparam lparam]
  (def last-event-time (Date.))
  (swap! received-event-types conj code)
  (def foomoo wmtouch-callback)
  (when (= wmtouch code)
    (handle-wmtouch (-> wparam .intValue low-word)
                    (as-handle lparam)))
  -1)

(defn setup-touch! [frame callback]
  (invoke-void-method (fn []
                        (let [handle (handle frame)
                              handle-event-proxy (fn [code wparam lparam]
                                                   (binding [wmtouch-callback callback]
                                                     (handle-event code wparam lparam)))]
                          (register-touch-window handle)
                          (set-message-hook! handle handle-event-proxy)))))

(defn stop-touch! [[hook _]]
  (remove-message-hook! hook))
