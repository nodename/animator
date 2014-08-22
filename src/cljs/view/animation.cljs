(ns view.animation
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs.core.async :refer [>! <! chan timeout]])
  (:require-macros [cljs.core.async.macros :refer [go-loop]]))


;; https://github.com/jxa/rain/blob/master/src/cljs/rain/async.cljs:
(defn timer-chan
  "create a channel which emits a message every delay milliseconds
   if the optional stop parameter is provided, any value written
   to stop will terminate the timer"
  ([delay msg]
     (timer-chan delay msg (chan)))
  ([delay msg stop]
     (let [out (chan)]
       (go-loop []
           (when-not (= stop (second (alts! [stop (timeout delay)])))
             (>! out msg)
             (recur)))
       out)))



(defn animation
  [cursor owner {:keys [duration stop? update] :as props}]
  (reify
    om/IInitState
    (init-state
     [this]
     {:start-time (.now (.-performance js/window))
      :elapsed-time 0})

    om/IWillMount
    (will-mount
     [_]
     (let [timer (timer-chan 10 :tick (timeout duration))
           start-time (om/get-state owner :start-time)]
       (go-loop []
                (let [tick (<! timer)
                      time (.now (.-performance js/window))
                      elapsed-time (- time start-time)]
                  (om/set-state! owner :elapsed-time elapsed-time))
                (recur))))

    om/IRenderState
    (render-state
     [this state]
     (let [elapsed-time (:elapsed-time state)
           canvas (. js/document (getElementById "anim-canvas"))]
       (println "render-state: elapsed-time:" elapsed-time)
       (when (not (stop? elapsed-time props))
         (update elapsed-time canvas props)))

     (dom/div #js {}))))
