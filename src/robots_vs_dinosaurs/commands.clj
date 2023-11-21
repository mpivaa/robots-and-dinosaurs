(ns robots-vs-dinosaurs.commands
  (:require [failjure.core :as f]
            [robots-vs-dinosaurs.world :as w]
            [robots-vs-dinosaurs.utils :refer [assert-all]]))

;; Available commands to the user, all pure here

(def invalid-beast-msg
  "Invalid beast: should be a robot or a dinosaur")

(defn ocuppied-space-msg [cmd]
  (format "You can't %s to an occupied space" cmd))
(defn out-of-boundaries-msg [cmd]
  (format "You can't %s outside the world" cmd))

(defn- validate-move [dest world]
  (assert-all true?
    [(w/empty-tile? dest world) (ocuppied-space-msg "move")
     (w/in-boundaries? dest world) (out-of-boundaries-msg "move")]))

(defn- validate-deploy [dest beast world]
  (assert-all true?
    [(w/empty-tile? dest world) (ocuppied-space-msg "deploy")
     (w/in-boundaries? dest world) (out-of-boundaries-msg "deploy")
     (w/valid-beast? beast) invalid-beast-msg]))

(defn move
  "Move robot in the world"
  [dir robot-id world]
   (f/attempt-all [ctx (w/get-robot-ctx robot-id world)
                   [{facing :facing} pos] ctx
                   new-pos (w/calc-new-pos [facing pos] dir)
                   valid? (validate-move new-pos world)]
     (w/move-item pos new-pos world)))
        
(defn turn
  "Turn robot in the world"
  [dir robot-id world]
  (f/attempt-all [ctx (w/get-robot-ctx robot-id world)
                  [robot pos] ctx]
    (w/set-item pos (w/turn-robot dir robot) world)))

(defn attack
  "Attack dinosaurs around"
  [robot-id world]
  (f/attempt-all [ctx (w/get-robot-ctx robot-id world)
                  [robot pos] ctx
                  tiles-around (w/get-tiles-around pos)]
    (reduce #(w/kill-in %2 %1) world tiles-around)))

(defn deploy
  "Deploy a new beast in the world in the given position"
  [x y beast world]
  (f/attempt-all [valid? (validate-deploy [x y] beast world)]
    (w/set-item [x y] beast world)))

(defn reset [world]
  (let [[cols rows] (w/get-size world)]
    (w/empty-world cols rows)))
