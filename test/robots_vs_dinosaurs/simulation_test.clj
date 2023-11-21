(ns robots-vs-dinosaurs.simulation-test
  (:require [clojure.test :refer :all]
            [failjure.core :as f]
            [robots-vs-dinosaurs.simulation :refer :all]
            [robots-vs-dinosaurs.world :as w :refer [empty-world
                                                     dinosaur
                                                     robot]]
            [robots-vs-dinosaurs.commands :refer [deploy
                                                  move
                                                  turn
                                                  attack]]))

(defn passthru-world [w] w)
(defn some-failure [& w] (f/fail "Some failure"))

(deftest simuation-test
  (testing "run->>"
    (testing "runs steps threading world and returning the result"
      (let [world (run->> (empty-world 3 3)
                          passthru-world)]
        (is (= world (empty-world 3 3)))))

    (testing "short-circuit if something goes wrong returning the failure"
      (let [failure (run->> (empty-world 3 3)
                          some-failure
                          (empty-world 3 3))]
        (is (= failure (some-failure))))))

    (testing "runs simulation"
      (let [world (run->> (empty-world 3 3)
                          (deploy 0 0 (dinosaur))
                          (deploy 2 2 (dinosaur))
                          (deploy 1 1 (robot :id :robot))
                          (move :forward :robot)
                          (attack :robot)
                          (turn :right :robot)
                          (move :forward :robot))
            dump (w/dump-world world)]
        (is (= dump [["x" "." ">"]
                     ["." "." "."]
                     ["." "." "§"]])))))
