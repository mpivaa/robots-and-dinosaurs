(ns robots-vs-dinosaurs.core
  (:require [robots-vs-dinosaurs.simulation :as s]
            [robots-vs-dinosaurs.display :as d]
            [robots-vs-dinosaurs.world :as w])
  (:gen-class))

(defn welcome-msg []
  (str "Welcome to Robots vs Dinosaurs.\n"
       "Type (help) to get a list of commands"))

(defn- repl []
  (d/display-message (welcome-msg))
  (s/run-interactive->> (w/empty-world 20 20)
                        (s/repl-cmd)))

(defn- eval-cmds [code]
  (eval `(s/run-interactive->> ~@(read-string code))))

(defn -main [& [cmd & args]]
  (binding [*ns* (the-ns 'robots-vs-dinosaurs.simulation)]
    (case cmd
      "-f" (if-let [[filepath] args]
              (-> (slurp filepath) eval-cmds))
      (repl))))