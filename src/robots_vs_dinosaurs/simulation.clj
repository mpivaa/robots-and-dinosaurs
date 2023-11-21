(ns robots-vs-dinosaurs.simulation
  (:require [failjure.core :as f]
            [robots-vs-dinosaurs.commands :as cmds :refer [deploy
                                                           move
                                                           turn
                                                           attack
                                                           reset]]
            [robots-vs-dinosaurs.world :as w :refer [empty-world
                                                     dinosaur
                                                     robot]]
            [robots-vs-dinosaurs.display :as d]
            [robots-vs-dinosaurs.utils :refer [in?
                                               guess-word]]))

;; Runs simulations and deals with user input and output

(def valid-cmds
  ['move 'turn 'attack
   'deploy 'reset 'help
   'exit 'dump-world])

(defn valid-cmd? [cmd]
  (in? valid-cmds cmd))

(defn invalid-cmd-msg [cmd]
  (let [guess-cmd (guess-word cmd valid-cmds)]
    (case guess-cmd
      nil "Invalid command"
      (format "Invalid command. Did you mean `%s`?" guess-cmd))))

(defn read-cmd [world]
  (let [full-cmd (read-string (d/wait-for-cmd))
        [cmd & args] full-cmd
        res (if (valid-cmd? cmd)
              (-> full-cmd (concat [world]) eval)
              (-> cmd invalid-cmd-msg f/fail))]
      res))

(defn repl-cmd [world]
  (f/try*
    (f/if-let-ok? [res (f/ok->> world
                                (read-cmd)
                                (d/display-world))]
      (repl-cmd res)
      (do (d/display-error res)
        (repl-cmd world)))
    (catch Exception e
      (d/display-error
        (-> (.getMessage e) (format "Error: %s") f/fail))
      (repl-cmd world))))

(defn dump-world [world]
  (-> world w/dump-world d/display-message))

(defn sleep [ms world]
  (Thread/sleep ms)
  world)

(defn help [world]
  (d/display-message "
    (deploy <x> <y> <Dinosaur|Robot>) Deploy a beast in the world
    (dinosaur) Creates a Dinosaur
    (robot :id <id> :facing <:N|:E|:S|:W>) Creates a robot
    (move <:forward|:backward> <robot-id>) Move a robot
    (turn <:left|:right> <robot-id>) Turn a robot
    (attack <robot-id>) Send attack cmd to robot
    (reset) Reset the world
    (dump-world) Display internals of the world
    (exit) Exits the simulation
    (help) Show help")
  world)

(defn exit [world]
  (println "Thanks!")
  (System/exit 0))

(defn- should-animate? [[cmd & args]]
  (in? ['move 'attack 'turn] cmd))

(def anim-delay 400)
(defn- inject-side-effect
  "Inject (display-world) and (sleep anim-delay) after each quoted cmd"
  [steps]
  (reduce
    (fn [acc, step]
      (if (should-animate? step)
        (concat acc [step
                    `(d/display-world)
                    `(sleep anim-delay)])
        (concat acc [step `(d/display-world)])))
    []
    steps))

(defmacro run->>
  "Run simulation steps"
  [& steps]
  `(f/ok->> ~@steps))

(defmacro run-interactive->>
  "Run simulation displaying the result between each step"
  [& steps]
  (let [steps (inject-side-effect steps)]
    `(d/bind-gui
       (f/when-let-failed? [res# (run->> ~@steps)]
         (d/display-error res#)))))
