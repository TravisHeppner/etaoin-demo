(ns demo.explore
  (:require [etaoin.api :refer :all]
            [etaoin.keys :as keys]))

(def driver (chrome))

(doto driver
  (go "https://www.gleif.org/en/lei-data/gleif-golden-copy/download-the-golden-copy#/")
  (wait-exists ["//*[@id=\"deltaSelect\"]/div/div/i"])
  (click ["//*[@id=\"deltaSelect\"]/div/div/i"])
  (fill-active keys/enter))

(query driver {:fn/text "Eight hours earlier."})
(get-element-location driver {:fn/text "Eight hours earlier."})
(get-element-box driver {:fn/text "Eight hours earlier."})
(let [{:keys [x1 x2 y1 y2]} (get-element-location driver {:fn/text "Eight hours earlier."})
      x (/ (+ x1 x2) 2)
      y (/ (+ y1 y2) 2)]
  (println x y)
  (mouse-move-to driver x y))
(let [{:keys [x y]} (get-element-location driver {:fn/text "Eight hours earlier."})]
  (println x y)
  (mouse-move-to driver x y))
(mouse-btn-down driver)

(get-source driver)
(query driver [{}])

(fill-active driver keys/enter)

(fill driver "//*[@id=\"deltaSelect\"]/div/div/input" "Eight hours earlier.")
