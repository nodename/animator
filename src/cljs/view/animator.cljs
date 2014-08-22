(ns view.animator
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs.core.async :refer [put! <! chan timeout close!]]
            [view.view :refer [get-circle-canvas release-circle-canvas]])
  (:require-macros [cljs.core.async.macros :refer [go-loop]]))

;; A generic one-shot time-based animation compnent.


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



(defn animator
  [cursor owner {:keys [parent stop? update] :as props}]
  (reify
    om/IInitState
    (init-state
     [this]
     {:start-time (.now (.-performance js/window))
      :elapsed-time 0
      :stop-timer (chan)
      :canvas (get-circle-canvas parent)})

    om/IWillMount
    (will-mount
     [_]
     (let [timer (timer-chan 10 :tick (om/get-state owner :stop-timer))
           start-time (om/get-state owner :start-time)]
       (go-loop []
                (let [tick (<! timer)
                      time (.now (.-performance js/window))
                      elapsed-time (- time start-time)]
                  ;; set-state! will trigger a render-state request:
                  (om/set-state! owner :elapsed-time elapsed-time))
                (recur))))

    om/IRenderState
    (render-state
     [this state]
     (let [elapsed-time (:elapsed-time state)
           canvas (om/get-state owner :canvas)]
       (if (stop? elapsed-time props)
         (do
           (put! (om/get-state owner :stop-timer) :stop)
           (release-circle-canvas canvas))
         (update elapsed-time canvas props)))

     ;; render-state must return a component although we don't need one.
     ;; This is the minimal thing we can return:
     (dom/div #js {}))))
