(ns demo.core
  (:require [etaoin.keys :as k]
            [etaoin.api :as api]
            [clj-http.client :as client])
  (:import [java.io File FileOutputStream]))


(def windows-driver-location "C:\\Windows\\chromedriver.exe")

;first you have to start the driver
(def driver (api/chrome))
;(defonce driver ((api/chrome-headless {})))

;;;;;;;clojure core cool function;;;;;;;;
;defonce
;it functions as def only does it not redefine
;very helpful for atoms and other mutable objects


;go moves you directly to a url
(api/go driver "http://www.google.com")

;you can check the title of the page!
(api/get-title driver)

;you can check for text on the page.
;I've found this useful when trying to determine if you are logged in to a webpage.
(api/has-text? driver "Gmail")

;we can use hickup like queries
(api/exists? driver {:tag :input :value "Google Search"})


;query gets the first element-id that matches the query
(api/query driver {:tag :img})
(api/query driver ".//input[@value=\"Google Search\"]")

(api/query-all driver {:tag :img})

;oh look I can use
(->> (api/query-all driver {:fn/text "Gmail"})
     first
     (api/click-el driver))





;direction contols
(api/back driver)
(api/forward driver)
(api/refresh driver)



;;;;;;;clojure core cool function;;;;;;;
;doto
;good for mutable things
;takes a object uses it as the first argument of following forms
;like -> only mutable
(->> 1
     (+ 2)
     )

(doto driver
  (api/go "https://imgflip.com/memegenerator/What-Do-We-Want")
  (api/fill {:tag :textarea :placeholder "Text #1"} "What do we want?")
  (api/fill {:tag :textarea :placeholder "Text #2"} "time travel")
  (api/fill {:tag :textarea :placeholder "Text #3"} "When do we want it?")
  (api/fill {:tag :textarea :placeholder "Text #4"} "Any time!"))

(api/go driver "https://www.google.com")
(api/has-alert? driver)
;(api/get-alert-text driver) ;this is garbage in this case.
(api/accept-alert driver)

;lets just encapsulate that
(defn deal-with-alert [driver]
  (when (api/has-alert? driver)
    (api/accept-alert driver)))



;a little bit slower this time
(api/doto-wait 2 driver
  (api/go "https://imgflip.com/memegenerator/What-Do-We-Want")
  (deal-with-alert)
  (api/fill {:tag :textarea :placeholder "Text #1"} "What do we want?")
  (api/fill {:tag :textarea :placeholder "Text #2"} "time travel")
  (api/fill {:tag :textarea :placeholder "Text #3"} "When do we want it?")
  (api/fill {:tag :textarea :placeholder "Text #4"} "Any time!"))


;now we'll click to generate it
(api/doto-wait 1 driver
  (api/go "https://imgflip.com/memegenerator/What-Do-We-Want")
  (deal-with-alert)
  (api/fill {:tag :textarea :placeholder "Text #1"} "What do we want?")
  (api/fill {:tag :textarea :placeholder "Text #2"} "time travel")
  (api/fill {:tag :textarea :placeholder "Text #3"} "When do we want it?")
  (api/fill-human {:tag :textarea :placeholder "Text #4"} "Any time!")
  (api/click "//*[@id=\"mm-settings\"]/div[8]/div[2]"))

;let's just capture this as a unit of work
(defn navigate-1 [driver]
  (doto driver
    (api/go "https://imgflip.com/memegenerator/What-Do-We-Want")
    (deal-with-alert)
    (api/fill {:tag :textarea :placeholder "Text #1"} "What do we want?")
    (api/fill {:tag :textarea :placeholder "Text #2"} "time travel")
    (api/fill {:tag :textarea :placeholder "Text #3"} "When do we want it?")
    (api/fill {:tag :textarea :placeholder "Text #4"} "Any time!")
    (api/click "//*[@id=\"mm-settings\"]/div[8]/div[2]")))


;now we want to download the image.
(api/screenshot driver "meme-screenshot.jpg")

;hmmm useful but not what we want!

;if you're using firefox driver you could use the following to capture an element.
;some day chrome driver
;(api/screenshot-element driver "//*[@id=\"doneUrl\"]/div[1]/input" "meme.jpg")

;grab the image url and screen shot it
(api/get-element-attr driver "//*[@id=\"doneImage\"]" :src)



(let [_ (navigate-1 driver)
      image-url (api/get-element-attr driver "//*[@id=\"doneImage\"]" :src)]
  (doto driver
    (api/go image-url)
    (api/screenshot "meme.jpg")))







;wouldn't it be easier to just download the image?
(defn enable-etaoin-downloads
  "Enables downloads for driver in headless mode. Files will be saved in tmp-dir.

   This makes use of an experimental chromium option as described here:
   https://chromedevtools.github.io/devtools-protocol/tot/Page/#method-setDownloadBehavior
   https://github.com/igrishaev/etaoin/issues/170"
  [driver tmp-dir]
  (api/with-resp driver :post
                 [:session (:session @driver) :chromium :send_command]
                 {:cmd :Page.setDownloadBehavior, :params {:behavior :allow, :downloadPath tmp-dir}}
                 _ nil))

(defn write-file []
  (with-open [w (clojure.java.io/output-stream "test-file.gif")]
    (.write w (:body test-file))))

(let [_ (navigate-1 driver)
      _ (Thread/sleep 2000)
      ;_ (api/wait-exists driver "//*[@id=\"doneImage\"]")
      image-url (api/get-element-attr driver "//*[@id=\"doneImage\"]" :src)
      _ (println image-url)]
  (clojure.java.io/copy
    (:body (client/get image-url {:as :stream}))
    (File. "meme-downloaded.jpg")))



(api/disconnect-driver driver)







