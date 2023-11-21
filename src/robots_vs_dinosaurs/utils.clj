(ns robots-vs-dinosaurs.utils
  (:require [failjure.core :as f]
            [clj-fuzzy.metrics :refer [dice]]))

(defn in? [coll elm]  
  ((complement nil?) (some #(= elm %) coll)))

(defn rand-id []
  (keyword (str (rand-int 99999))))

(defn assert-all [pred & [assertions]]
  (->> assertions
       (partition 2)
       (reduce (fn [res [val msg]]
                 (if (f/failed? res)
                   res
                   (if (pred val) val (f/fail msg))))
                 true)))

(defn guess-word
  "Use Dice coefficient to guess a word based on input"
  [input wordlist]
  (first (for [word wordlist
               :when (<= 0.6 (dice (str input) (str word)))]
          word)))