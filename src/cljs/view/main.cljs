(ns view.main
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.core.async :refer [<! put! chan]]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [thi.ng.geom.core.vector :refer [vec2]]
            [view.view :refer [draw-circle]]
            [view.animation :refer [animation]]))

(enable-console-print!)

(def pt vec2)


(defn alpha
  [elapsed-time {:keys [duration]}]
  (* .001 (- duration elapsed-time)))

(defn stop?
  [elapsed-time {:keys [duration] :as state}]
  (< (alpha elapsed-time state) 0))

(defn update
  [elapsed-time canvas {:keys [center radius line-width scale color duration] :as state}]
  (let [context (.getContext canvas "2d")
        a (alpha elapsed-time state)]
    (.clearRect context 0 0 (.-width canvas) (.-height canvas))
    (println "alpha:" a "color:" color)
    (draw-circle context center radius line-width scale
                 (merge color {:a a}))))


(defn home-page [cursor owner]
  (reify
    om/IRender
    (render [_]
      (dom/div #js {}
               (om/build animation
                         cursor
                         {:opts {:center (pt 100 100)
                                 :radius 50
                                 :line-width 2
                                 :scale 1
                                 :color {:r 255 :g 0 :b 0}
                                 ;;
                                 :duration 1000
                                 :stop? stop?
                                 :update update}})))))

(om/root home-page
         (atom {})
         ;; target must not be null but we're not using it:
         {:target (. js/document (getElementById "dummy"))})


