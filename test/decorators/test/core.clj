(ns decorators.test.core
  (:use [decorators.core])
  (:use [clojure.test]))

(defn x2 [x] (* 2 x))

(deftest test-to-decorator
         (let [map-dec (to-decorator map)
               x2-map (map-dec x2)]
           (is (x2-map [1 2 3]) '(2 4 6))))

(defn force-positive
  [f]
  (fn wrap [& args]
    (if (some neg? args)
      (throw (Exception. "There is a negative value"))
      (apply f args))))

(deftest test-force-positive
         (let [wrapped+ (force-positive +)]
           (is (thrown? Exception (wrapped+ -1 1)))
           (is  2 (wrapped+ 1 1))))

(deftest test-apply-decorator
         (is (thrown? Exception (apply-decorator force-positive + -1 1)))
         (is 2 (apply-decorator force-positive + 1 1)))

(deftest test-to-hof
         (let [hof (to-hof force-positive)]
           (is (thrown? Exception (hof + -1 1)))
           (is (thrown? Exception (hof - -1 1)))
           (is (thrown? Exception (hof * -1 1)))
           (is 2 (hof + 1 1))
           (is 0 (hof - 1 1))
           (is 1 (hof * 1 1))))

(decorate x2 (to-decorator map))

(deftest test-decorate
         (is (x2 [1 2 3]) '(1 2 3)))

(deftest test-dual-decorator
         (let [force-pos (dual-decorator force-positive)
               wrapped+ (force-pos +)]
           (is (thrown? Exception (wrapped+ -1 1)))
           (is  2 (wrapped+ 1 1))
           (is (thrown? Exception (force-pos + -1 1)))
           (is  2 (force-pos + 1 1))))

(deftest test-dual-hof
         (let [dual-map (dual-hof map)
               wrapped-inc (dual-map inc)]
           (is (wrapped-inc [1 2 3]) [2 3 4])
           (is (dual-map inc [1 2 3]) [2 3 4])))