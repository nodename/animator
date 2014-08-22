(ns view.view
  (:require [cljs.core.async :refer [>! <! chan]]
            [thi.ng.geom.core.vector :refer [vec2]]
            [edge-algebra.record :refer [get-e0 get-edge]]
            [delaunay.div-conq :refer [pt delaunay]]
            [delaunay.utils.circle :refer [center-and-radius]])
  (:require-macros [cljs.core.async.macros :refer [go-loop]]))

(enable-console-print!)


(defn draw-circle
  [context center radius line-width scale {:keys [r g b a]
                                     :or {a 1.0}}]
  (let [h (.-height (.-canvas context))
        center-x (* scale (.-x center))
        center-y (- h (* scale (.-y center)))
        radius (* scale radius)]
    (println "draw-circle: x:" center-x "y:" center-y "r:" radius "a:" a)
    (set! (. context -strokeStyle) (str "rgba(" r "," g "," b "," a ")"))
    (set! (. context -lineWidth) line-width)
    (.beginPath context)
    ;; x y radius startAngle endAngle counterClockwise?:
    (.arc context center-x center-y radius 0 (* 2 Math/PI) false)
    (.stroke context)))

(defn init-canvas
  "Initialize a canvas and return it."
  [parent z-index]
  (let [canvas (.createElement js/document "canvas")]
    (.setAttribute canvas "width" (.-innerWidth js/window))
    (.setAttribute canvas "height" (.-innerHeight js/window))
    (.setAttribute canvas "style" (str "z-index:" z-index
                                       "; position:absolute; left:0px; top:0px;"))
    (.appendChild parent canvas)))

(defn stop-animation
  [request-id]
  (when (pos? request-id)
    (.cancelAnimationFrame js/window request-id)))

(defn start-animation
  "Start an animation specified by update and stop?,
  both of which should be functions of elapsed-time.
  Return the request-id so that stop-animation can be called
  externally if necessary."
  [update stop?]
  (let [start-time (.now (.-performance js/window))
        request-id (atom 0)
        tick (fn tick [time]
               (let [elapsed-time (- time start-time)]
                 (if (stop? elapsed-time)
                   (stop-animation @request-id)
                   (do
                     (update elapsed-time)
                     (.requestAnimationFrame js/window tick)))))]
    (reset! request-id (.requestAnimationFrame js/window tick))
    @request-id))



(def canvas-pool (atom {:canvases []
                        :depth 1}))

(defn get-circle-canvas
  [parent]
  (if (> (count (:canvases @canvas-pool)) 0)
    (let [canvas (first (:canvases @canvas-pool))]
      (swap! canvas-pool update-in [:canvases] drop 1)
      canvas)
    (let [depth (get-in @canvas-pool [:depth])]
      (swap! canvas-pool update-in [:depth] inc)
      (init-canvas parent depth))))

(defn release-circle-canvas
  [canvas]
  (swap! canvas-pool update-in [:canvases] conj canvas))


