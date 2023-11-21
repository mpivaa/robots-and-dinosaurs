(ns robots-vs-dinosaurs.world
  (:require [failjure.core :as f]
            [clojure.core.match :refer [match]]
            [robots-vs-dinosaurs.utils :refer [in?
                                               rand-id]]))

;; Deals with aspects of the world and living beings, side-effect free

;; Looks like `match` doesn't work with records,
;; so I kept `type` to help pattern matching
(defrecord Dinosaur [id status type])
(defrecord Robot [id facing status type])

(defn dinosaur [] (->Dinosaur (rand-id) :alive :dinosaur))

(defn robot
  [& {:keys [id facing] :or {id (rand-id) facing :N}}]
  (map->Robot {:id id
               :type :robot
               :facing facing
               :status :alive}))

(defn id [beast] (:id beast))

(defn is-robot? [beast] (= (type beast) Robot))
(defn is-dinosaur? [beast] (= (type beast) Dinosaur))
(defn is-dead? [{status :status}] (= status :dead))

(defn kill [beast]
  (assoc beast :status :dead))

(defn empty-world
  "Creates an empty world of the specified width and height."
  [w h]
  (vec (repeat w (vec (repeat h nil)))))

(defn get-item [[x y] world]
  (get-in world [y x]))

(defn set-item [[x y] val world]
  (assoc-in world [y x] val))

(defn clear
  "Clear a given position in the world"
  [pos world]
  (set-item pos nil world))

(defn kill-in
  "Kill beast in given position in the world"
  [pos world]
  (let [beast (get-item pos world)]
    (if (is-dinosaur? beast)
      (set-item pos (kill beast) world)
      world)))
    
(defn move-item
  "Move item in the world `from` position `to` position"
  [from to world]
  (let [beast (get-item from world)]
    (->> world
         (set-item to beast)
         (clear from))))

(defn turn-robot [dir robot]
  (assoc robot :facing
    (case (:facing robot)
      :N (case dir :left :W :right :E)
      :E (case dir :left :N :right :S)
      :S (case dir :left :E :right :W)
      :W (case dir :left :S :right :N))))

(defn get-robot-ctx
  "Get robot context in the world"
  [robot-id world]
  (let [res (for [[y row] (map-indexed vector world) 
                  [x item] (map-indexed vector row) 
                  :when (and
                         ((complement nil?) item)
                         (is-robot? item)
                         (= robot-id (id item)))]
                [item [x y]])]
      (if (> (count res) 0)
        (first res)
        (f/fail "Robot not found"))))

(defn calc-new-pos
  "Calculate new position for given direction"
  [[facing [x y]] dir]
  (let [step (case dir :forward 1 :backward -1)]
    (case facing
      :N [x (- y step)]
      :E [(+ x step) y]
      :S [x (+ y step)]
      :W [(- x step) y])))

(defn get-tiles-around [[x y]]
  (let [tiles-shift [[1 0] [0 1] [0 -1] [-1 0]]]
    (map (fn [[x-shift y-shift]]
           [(+ x-shift x) (+ y-shift y)])
      tiles-shift)))

(defn get-beasts [world]
  (sort-by :id
    (for [row world
          item row
          :when ((complement nil?) item)]
    item)))

(defn get-size [world]
  (let [rows (-> world count)
       cols (-> world first count)]
    [cols rows]))

(defn empty-tile? [[x y] world]
  (let [item (get-item [x y] world)]
    (or (nil? item) (is-dead? item))))

(defn in-boundaries? [[x y] world]
  ((complement false?)
    (get-in world [y x] false)))

(defn valid-beast? [{type :type}]
  (in? [:dinosaur :robot] type))

(def dino-symbol "§")
(def dead-symbol "x")
(def up-symbol "^")
(def down-symbol "v")
(def left-symbol "<")
(def right-symbol ">")
(def empty-symbol ".")

(defn dir-symbol [dir]
  (case dir
    :N up-symbol
    :E right-symbol
    :S down-symbol
    :W left-symbol))

(defn item->string [item]
  (match item
    {:status :dead} dead-symbol
    {:type :dinosaur} dino-symbol
    {:type :robot :facing dir} (dir-symbol dir)
    :else empty-symbol))

(defn dump-world [world]
  "Generates a string representation of the world"
  (for [row world]
    (for [item row]
      (item->string item))))