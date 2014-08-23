(ns animator.example
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.core.async :refer [<! put! chan]]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [animator.animator :refer [animator]]))

(enable-console-print!)


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Define your own stop? and update functions.
;; The animator knows nothing of their internals.

(defn draw-circle
  [context center radius line-width scale {:keys [r g b a]
                                           :or {a 1.0}}]
  (let [h (.-height (.-canvas context))
        center-x (* scale (:x center))
        center-y (- h (* scale (:y center)))
        radius (* scale radius)]
    (set! (. context -strokeStyle) (str "rgba(" r "," g "," b "," a ")"))
    (set! (. context -lineWidth) line-width)
    (.beginPath context)
    ;; x y radius startAngle endAngle counterClockwise?:
    (.arc context center-x center-y radius 0 (* 2 Math/PI) false)
    (.stroke context)))

(defn alpha
  [elapsed-time {:keys [delay duration]}]
  (* .001 (- (+ delay duration) elapsed-time)))

(defn fading-circle-stop?
  [elapsed-time opts]
  (< (alpha elapsed-time opts) 0))

(defn fading-circle-update
  [elapsed-time canvas {:keys [center radius line-width scale color delay] :as opts}]
  (let [context (.getContext canvas "2d")
        a (alpha elapsed-time opts)]
    (when (>= elapsed-time delay)
      (.clearRect context 0 0 (.-width canvas) (.-height canvas))
      (draw-circle context center radius line-width scale
                   (merge color {:a a})))))
::
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; parent, stop? and update are for the animator's use;
;; all other opts (plus elapsed-time and canvas, which are generated by the animator)
;; are passed on to stop? and update:

(def common-opts
  {:parent (. js/document (getElementById "anim"))
   :stop? fading-circle-stop?
   :update fading-circle-update})

(def m0 (merge common-opts
               {:center {:x 100 :y 100}
                :radius 50
                :line-width 2
                :scale 1
                :color {:r 255 :g 0 :b 0}
                :delay 1000
                :duration 1000}))

(def m1 (merge common-opts
               {:center {:x 400 :y 200}
                :radius 75
                :line-width 2
                :scale 1
                :color {:r 0 :g 255 :b 0}
                :delay 0
                :duration 2000}))

(def m2 (merge common-opts
               {:center {:x 200 :y 340}
                :radius 25
                :line-width 2
                :scale 1
                :color {:r 0 :g 0:b 255}
                :delay 750
                :duration 1250}))
;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;



(defn example [cursor owner opts]
  (reify
    om/IRender
    (render [_]
      (apply dom/div #js {}
             ;; Note that we pass no app-data cursor to the animator;
             ;; everything it needs is in its opts:
             (map #(om/build animator nil %) opts)))))

(om/root example
         (atom {})
         ;; We're not using target, but it cannot be null:
         {:target (. js/document (getElementById "dummy"))
          :opts [{:opts m0} {:opts m1} {:opts m2}]})
