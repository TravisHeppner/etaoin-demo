(ns demo.utils
  (:require [etaoin.keys :as k]
            [etaoin.api :as api]))

(defonce driver (api/chrome))


(defn deal-with-alert [driver]
  (when (api/has-alert? driver)
    (api/accept-alert driver)))


;see https://github.com/SeleniumHQ/selenium/wiki/JsonWireProtocol
;using this you can implement the remaining endpoints that etaoin forgets

(defn middle-click
  "Sends a middle click where the mouse is currently at.
   same as:
   <DRIVERURL>:<DRIVERPORT>/session/<SESSION>/click
   with json payload:
   {\"button\" : 1}"
  [driver]
  (api/with-resp
    driver
    :post
    [:session (:session @driver) :click]
    {:button 2}
    resp
    (:resp resp)))


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


(defn send-keys
  "Sends a set of keys to be pressed.
  same as:
  <DRIVERURL>:<DRIVERPORT>/session/<SESSION>/keys
  with json payload like:
  {\"value\" : [\"\\uE00A\"\"\\uE034\"]}"
  [driver keys]
  (api/with-resp
    driver
    :post
    [:session (:session @driver) :keys]
    {:value keys}
    resp
    (:resp resp)))


;todo make this work...  send-keys does not work either.
(defn save [driver querry]
  (do
    (api/mouse-move-to driver querry)
    (right-click driver)
    (api/fill-active driver k/arrow-down)
    (api/fill-active driver k/arrow-down)
    (api/fill-active driver k/enter)))


