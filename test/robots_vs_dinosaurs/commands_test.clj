(ns robots-vs-dinosaurs.commands-test
  (:require [clojure.test :refer :all]
            [failjure.core :as f]
            [robots-vs-dinosaurs.commands :refer :all]
            [robots-vs-dinosaurs.world :refer [empty-world
                                               dump-world
                                               dinosaur
                                               robot]]
            [robots-vs-dinosaurs.simulation :refer [run->>]]))

(def test-world (run->> (empty-world 3 3)
                        (deploy 1 1 (robot :id :robot))
                        (deploy 2 0 (dinosaur))
                        (deploy 2 1 (dinosaur))
                        (deploy 2 2 (dinosaur))))

(deftest commands-test
  (testing "deploy"
    (testing "deploys a robot"
      (let [world (run->> test-world
                          (deploy 0 0 (robot)))
            dump (dump-world world)]

        (is (= dump [["^" "." "§"]
                     ["." "^" "§"]
                     ["." "." "§"]]))))

    (testing "deploy a dinosaur"
      (let [world (run->> test-world
                          (deploy 0 0 (dinosaur)))
            dump (dump-world world)]
        (is (= dump [["§" "." "§"]
                     ["." "^" "§"]
                     ["." "." "§"]]))))

    (testing "fails if not a robot or dinosaur"
      (let [failure (run->> test-world
                            (deploy 0 0 {:type :dragon}))]
        (is (f/failed? failure))
        (is (= invalid-beast-msg
               (f/message failure)))))

    (testing "fails if deploying to an occupied space"
      (let [failure (run->> test-world
                            (deploy 0 0 (dinosaur))
                            (deploy 0 0 (dinosaur)))]
        (is (f/failed? failure))
        (is (= (ocuppied-space-msg "deploy")
              (f/message failure)))))

    (testing "fails if deploying outside the world"
      (let [failure (run->> test-world
                            (deploy -1 0 (dinosaur)))]
        (is (f/failed? failure))
        (is (= (out-of-boundaries-msg "deploy")
              (f/message failure))))))

  (testing "move"
    (testing "move forward the robot"
      (let [world (run->> test-world
                          (move :forward :robot))
            dump (dump-world world)]

        (is (= dump [["." "^" "§"]
                     ["." "." "§"]
                     ["." "." "§"]]))))
    
    (testing "move backward the robot"
      (let [world (run->> test-world
                          (move :backward :robot))
            dump (dump-world world)]
        (is (= dump [["." "." "§"]
                     ["." "." "§"]
                     ["." "^" "§"]]))))

    (testing "fails if robot not exists"
      (let [failure (run->> test-world
                            (move :forward :unknown))]
        (is (f/failed? failure))))

    (testing "fails if moving to an occupied space"
      (let [failure (run->> test-world
                            (turn :right :robot)
                            (move :forward :robot))]
        (is (f/failed? failure))
        (is (= (ocuppied-space-msg "move")
               (f/message failure)))))

    (testing "fails if moving outside the world"
      (let [failure (run->> test-world
                            (move :forward :robot)
                            (move :forward :robot))]
        (is (f/failed? failure))
        (is (= (out-of-boundaries-msg "move")
               (f/message failure))))))
  
  (testing "turn"
    (testing "turn left the robot"
      (let [world (run->> test-world
                          (turn :left :robot))
            dump (dump-world world)]
        (is (= dump [["." "." "§"]
                     ["." "<" "§"]
                     ["." "." "§"]]))))

    (testing "turn right the robot"
      (let [world (run->> test-world
                          (turn :right :robot))
            dump (dump-world world)]
        (is (= dump [["." "." "§"]
                     ["." ">" "§"]
                     ["." "." "§"]]))))
    
    (testing "fails if robot not exists"
      (let [failure (run->> test-world
                            (turn :right :unknown))]
        (is (f/failed? failure)))))
  
  (testing "attack"
    (testing "attacks only adjancent tiles and not diagonals"
      (let [world (run->> test-world
                          (deploy 0 0 (dinosaur))
                          (deploy 1 0 (dinosaur))
                          (deploy 0 1 (dinosaur))
                          (deploy 0 2 (dinosaur))
                          (deploy 1 2 (dinosaur))
                          (attack :robot))
            dump (dump-world world)]
        (is (= dump [["§" "x" "§"]
                     ["x" "^" "x"]
                     ["§" "x" "§"]]))))

    (testing "fails if robot not exists"
      (let [failure (run->> test-world
                            (turn :right :unknown))]
        (is (f/failed? failure)))))
        
  (testing "reset"
    (testing "clear world"
      (let [dump (dump-world (reset test-world))]
        (is (= dump [["." "." "."]
                     ["." "." "."]
                     ["." "." "."]]))))))
