(ns animator.example
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [animator.animator :refer [animator]]))

(enable-console-print!)


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Define your own update function.
;; The animator knows nothing of its internals.

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

(defn fading-circle-update
  [elapsed-time canvas {:keys [center radius line-width scale color delay] :as opts}]
  (let [context (.getContext canvas "2d")
        a (alpha elapsed-time opts)]
    (when (>= elapsed-time delay)
      (.clearRect context 0 0 (.-width canvas) (.-height canvas))
      (draw-circle context center radius line-width scale
                   (merge color {:a a})))))
;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Define your own animations.
;; These are passed to your update function,
;; so provide the opts that that function needs.

(def animations [{:center {:x 100 :y 100}
                  :radius 50
                  :line-width 2
                  :scale 1
                  :color {:r 255 :g 0 :b 0}
                  :delay 1000
                  :duration 1000}

                 {:center {:x 400 :y 200}
                  :radius 75
                  :line-width 2
                  :scale 1
                  :color {:r 0 :g 255 :b 0}
                  :delay 0
                  :duration 2000}

                 {:center {:x 200 :y 340}
                  :radius 25
                  :line-width 2
                  :scale 1
                  :color {:r 0 :g 0:b 255}
                  :delay 750
                  :duration 1250}])
;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;



(defn example [cursor owner opts]
  (reify
    om/IRender
    (render [_]
            (dom/div #js {:width "800px" :height "500px"
                          :style #js {:width "800px" :height "500px"}}

                     (om/build animator
                               cursor
                               {:state {:start-time (.now (.-performance js/window))}
                                :opts {:update fading-circle-update}})))))

(om/root
  example
  animations
  {:target (. js/document (getElementById "anim"))})

