(ns touch-input.core
  (:import IUser IAWTCallback Hooks$IGetMsgProc
           [com.sun.jna Native Pointer Callback]
           [com.sun.jna.win32 StdCallLibrary StdCallLibrary$StdCallCallback]
           [com.sun.jna.platform.win32 WinUser WinDef$HWND WinDef$WPARAM WinDef$LPARAM BaseTSD$LONG_PTR WinDef$LRESULT]))

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
  (invoke-void-method #(.RegisterTouchWindow nativeuser
                                             handle
                                             0)))
        
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
  (.UnhookWindowsHookEx hook))

(defn setup-touch! [callback])

(defn stop-touch! [])
