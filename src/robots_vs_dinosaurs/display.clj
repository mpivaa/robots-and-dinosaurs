(ns robots-vs-dinosaurs.display
  (:require [robots-vs-dinosaurs.graphics.gui :as gui]
            [clansi :refer [style]]
            [failjure.core :as f]))

;; Where all side-effects happens 

(defmacro bind-gui [& body]
  `(gui/bind ~@body))

(defn display-world [world]
  (gui/draw world))

(defn display-message [msg]
  (println msg)
  (println)
  msg)

(defn display-error [error]
  (-> error
      f/message
      (style :red) 
      display-message)
  error)

(defn wait-for-cmd []
  (print "=> ")
  (flush)
  (read-line))
