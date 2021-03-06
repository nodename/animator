(defproject animator "0.1.0-SNAPSHOT"
  :description "A generic Om component for one-shot time-based animations."
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-2280"]
                 [org.clojure/core.async "0.1.319.0-6b1aca-alpha"]
                 [om "0.7.1"]
                ; we're keeping a local copy of this: [thi.ng/geom "0.3.0-SNAPSHOT"]
                 ]
      :min-lein-version "2.0.0"
      :source-paths ["src/clj" "target/generated/clj"]

      :plugins [[lein-cljsbuild "0.3.0"]
                [lein-simpleton "1.1.0"]
                [com.keminglabs/cljx "0.4.0" :exclusions [org.clojure/clojure]]]

      :cljx {:builds [{:source-paths ["src/cljx"]
                       :output-path "target/generated/clj"
                       :rules :clj}

                      {:source-paths ["src/cljx"]
                       :output-path "target/generated/cljs"
                       :rules :cljs}]}

      :hooks [cljx.hooks]

      :cljsbuild {:builds [{:id "dev"
                            :source-paths ["src/cljs"]
                            :compiler {:output-to "resources/public/build/dev/animator.js"
                                       :output-dir "resources/public/build/dev"
                                       :source-map true
                                       :optimizations :none}}

                           {:id "prod"
                            :source-paths ["src/cljs"]
                            :compiler {:output-to "resources/public/build/prod/animator.js"
                                       :output-dir "resources/public/build/prod"
                                       :source-map "resources/public/build/prod/animator.js.map"
                                       :optimizations :advanced}}]})
