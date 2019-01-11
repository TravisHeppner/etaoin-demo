(ns demo.core
  (:require [etaoin.keys :as k]
            [etaoin.api :as api]))

;this is a whole lot of examples
;take a look at the utils namespace for things that might be missing from etaoin


(def windows-driver-location "C:\\Windows\\chromedriver.exe")

;first you have to start the driver
(defonce driver (api/chrome))
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
()

(doto driver
  (api/go "https://imgflip.com/memegenerator/What-Do-We-Want")
  (api/fill {:tag :textarea :placeholder "Text #1"} "What do we want?")
  (api/fill {:tag :textarea :placeholder "Text #2"} "time travel")
  (api/fill {:tag :textarea :placeholder "Text #3"} "When do we want it?")
  (api/fill {:tag :textarea :placeholder "Text #4"} "..."))

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
               (api/fill {:tag :textarea :placeholder "Text #4"} "..."))


;now we'll click to generate it
(api/doto-wait 2 driver
               (api/go "https://imgflip.com/memegenerator/What-Do-We-Want")
               (deal-with-alert)
               (api/fill {:tag :textarea :placeholder "Text #1"} "What do we want?")
               (api/fill {:tag :textarea :placeholder "Text #2"} "time travel")
               (api/fill {:tag :textarea :placeholder "Text #3"} "When do we want it?")
               (api/fill {:tag :textarea :placeholder "Text #4"} "...")
               (api/click "//*[@id=\"mm-settings\"]/div[8]/div[2]"))

;let's just capture this as a unit of work
(defn navigate-1 [driver]
  (doto driver
    (api/go "https://imgflip.com/memegenerator/What-Do-We-Want")
    (deal-with-alert)
    (api/fill {:tag :textarea :placeholder "Text #1"} "What do we want?")
    (api/fill {:tag :textarea :placeholder "Text #2"} "time travel")
    (api/fill {:tag :textarea :placeholder "Text #3"} "When do we want it?")
    (api/fill {:tag :textarea :placeholder "Text #4"} "...")
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



(defn right-click
  "Sends a right click where the mouse is currently at.
   same as:
   <DRIVERURL>:<DRIVERPORT>/session/<SESSION>/click
   with json payload:
   {\"button\" : 2}"
  [driver]
  (api/with-resp
    driver
    :post
    [:session (:session @driver) :click]
    {:button 2}
    resp
    (:resp resp)))

(defn save [driver querry]
  (do
    (api/mouse-move-to driver querry)
    (right-click driver)
    (api/fill-active driver k/arrow-down)))






