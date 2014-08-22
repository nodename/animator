(ns view.view)


(defn draw-circle
  [context center radius line-width scale {:keys [r g b a]
                                           :or {a 1.0}}]
  (let [h (.-height (.-canvas context))
        center-x (* scale (.-x center))
        center-y (- h (* scale (.-y center)))
        radius (* scale radius)]
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

(def canvas-pool (atom {:canvases []
                        :depth 1}))
#_
(defn get-circle-canvas
  [parent]
  (if (> (count (:canvases @canvas-pool)) 0)
    (let [canvas (first (:canvases @canvas-pool))]
      (swap! canvas-pool update-in [:canvases] drop 1)
      canvas)
    (let [depth (get-in @canvas-pool [:depth])]
      (swap! canvas-pool update-in [:depth] inc)
      (init-canvas parent depth))))

(defn get-circle-canvas
  [parent]
  (let [depth (get-in @canvas-pool [:depth])]
    (swap! canvas-pool update-in [:depth] inc)
    (init-canvas parent depth)))


(defn release-circle-canvas
  [canvas]
  (swap! canvas-pool update-in [:canvases] conj canvas))


