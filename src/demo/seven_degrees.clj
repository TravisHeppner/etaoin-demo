(ns demo.seven-degrees
  (:require [etaoin.keys :as k]
            [etaoin.api :refer :all]
            [clj-http.client :as client]
            [etaoin.keys :as keys])
  (:import [java.io File FileOutputStream]))


;assumtions: always on wikipedia

(def windows-driver-location "C:\\Windows\\chromedriver.exe")

;first you have to start the driver
(defonce driver (chrome))

(defn deal-with-alert [driver]
  (when (has-alert? driver)
    (accept-alert driver)))

(defmacro debug
  [expr]
  `(let [start# (. System (nanoTime))
         ret# ~expr]
     (prn "-------------Start Debug-------------")
     (clojure.pprint/pprint ret#)
     (prn (str "Elapsed time: " (/ (double (- (. System (nanoTime)) start#)) 1000000.0) " msecs"))
     (prn "--------------End Debug--------------")
     ret#))


;;;;;;;;;;;;;;;;;;;;;;;


(defn rand-wiki
  "Uses the random wiki page to find a random wiki article."
  [driver]
  (do
    (go driver "https://en.wikipedia.org/wiki/Special:Random")
    (get-url driver)))

(defn all-current-links
  "Grab all urls on a page
   Defaults to current driver page."
  ([driver url]
   (do                                                      ;(wait driver 1)
       (go driver url)
       (wait-exists driver ["//*[@id=\"mw-content-text\"]" {:tag :a}])
       (all-current-links driver)))
  ([driver]
   (->> (query-all driver ["//*[@id=\"mw-content-text\"]"  {:tag :a}])
        (keep #(get-element-attr-el driver % :href)))))

(defn valid-wiki-url? [url]
  (not
    (or
      (not (clojure.string/starts-with? url "https://en.wikipedia.org/wiki"))
      (re-find #"File\:" url)
      (re-find #"Portal\:" url)
      (re-find #"\:[A-Za-z\-]*[Ss]tub" url)
      (re-find #"\#" url)
      (re-find #"Wikipedia\:" url)
      (re-find #"Help\:" url))))

;(all-current-links driverer)
;
;(->> (all-current-links driver)
;     (filter valid-wiki-url?)
;     distinct)

;(let [start-url (rand-wiki driver )
;      end-url (rand-wiki driver)]
;  (do (doto driver
;        (go start-url))
;      (->> (all-current-links driver)
;           (filter valid-wiki-url?)
;           distinct))
;  )

(defn add-links [driver url]
  (->> (all-current-links driver url)
       (filter valid-wiki-url?)
       distinct))

(defn traverse-node [[[depth current-url] & urls] visited end-url]
  (let [_ (println depth "grabbing from" current-url)
        links (->> (add-links driver current-url)
                   (remove visited))]
    (if (some #(= end-url %) links)
      [end-url]
      (concat [current-url] (traverse-node
                              (concat urls (map #(vector (inc depth) %) links))
                              (conj visited current-url)
                              end-url)))))

(let [start-url (rand-wiki driver )
      end-url (rand-wiki driver)]
  (do (println start-url)
      (println end-url)
      (traverse-node [[0 start-url]] #{} end-url)))