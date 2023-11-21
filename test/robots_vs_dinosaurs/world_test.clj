(ns robots-vs-dinosaurs.world-test
  (:require [clojure.test :refer :all]
            [robots-vs-dinosaurs.world :refer :all]))

;; I'm testing behaviours so that the tests don't become
;; too tightly coupled to the implementation itself.
;; commands-test already exercise functions from this namespace

(deftest world-test
  (testing "get-beasts"
    (let [world (empty-world 5 5)
          world (set-item [0 0] (dinosaur) world)
          world (set-item [1 1] (robot) world)
          beasts (get-beasts world)]
      (is (= 2 (count beasts)))))

  (testing "calc-new-pos"
    (is (= [0 -1] (calc-new-pos [:N [0 0]] :forward)))
    (is (= [1 0] (calc-new-pos [:E [0 0]] :forward)))
    (is (= [0 1] (calc-new-pos [:S [0 0]] :forward)))
    (is (= [-1 0] (calc-new-pos [:W [0 0]] :forward)))
    (is (= [0 1] (calc-new-pos [:N [0 0]] :backward))))

  (testing "in-boundaries?"
    (let [world (empty-world 5 5)]
      (is (= false (in-boundaries? [-1 0] world)))
      (is (= true (in-boundaries? [4 4] world)))
      (is (= false (in-boundaries? [0 6] world)))))
  
  (testing "dump-world"
    (let [world (empty-world 3 3)
          world (set-item [0 0] (->Dinosaur :dino :dead :dinosaur) world)
          world (set-item [1 0] (robot :id :robot-1 :facing :N) world)
          world (set-item [2 1] (robot :facing :E) world)
          world (set-item [1 2] (robot :facing :S) world)
          world (set-item [0 1] (robot :facing :W) world)
          world (set-item [2 0] (dinosaur) world)
          dump (dump-world world)]
      (is (= dump [["x" "^" "§"]
                   ["<" "." ">"]
                   ["." "v" "."]])))))
