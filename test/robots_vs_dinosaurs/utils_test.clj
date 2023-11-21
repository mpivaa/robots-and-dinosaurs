(ns robots-vs-dinosaurs.utils-test
  (:require [clojure.test :refer :all]
            [failjure.core :as f]
            [robots-vs-dinosaurs.utils :refer :all]))

(deftest utils-test
  (testing "in?"
    (is (= true (in? [1 2 3] 1)))
    (is (= false (in? [1 2 3] 4))))

  (testing "guess-word"
     (is (= "move" (guess-word "mov" ["move" "attack"])))
     (is (= nil (guess-word "unrelated" ["move" "attack"]))))

  (testing "assert-all"
    (is (= true (assert-all true? [true "error message"])))
    (is (= (f/fail "error-message")
           (assert-all true? [false "error-message"])))))
