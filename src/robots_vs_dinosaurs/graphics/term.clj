(ns robots-vs-dinosaurs.display
  (:require [clojure.core.match :refer [match]]
            [failjure.core :as f]
            [lanterna.screen :as s]
            [clojure.string :refer [join]]
            [robots-vs-dinosaurs.world :as w]))

;; Display the world using ASCII
;; Deprecated in favor of GUI to avoid terminal compatibility issues

(declare ^:dynamic *scr*)
(declare read-cmd)

(defn read-key [scr] (s/get-key-blocking scr))
(defn get-screen [] (s/get-screen :text))
(defn start-screen [scr]
  (s/start scr))
(defn stop-screen [scr]
  (s/stop scr)
  (print (str (char 27) "[2J"))
  (print (str (char 27) "[;H")))
(defn clear-line [scr y]
  (let [[cols _] (s/get-size scr)]
    (doseq [x (range cols)]
      (s/put-string *scr* x y " "))))

(defmacro with-screen [& body]
  `(binding [*scr* (get-screen)]
     (try
      (start-screen *scr*)
      ~@body
      (finally (stop-screen *scr*)))))

(def vert-wall-symbol "|")
(def horz-wall-symbol "-")
(def corner-symbol "+")
(def double-arrow-symbol "»")
(def cmd-symbol "=>")

(defn display-hud [[x y] world]
  (let [beasts (w/get-beasts world)
        lines (count beasts)]
    (doseq [[idx beast] (map-indexed vector beasts)]
      (s/put-string *scr* x (+ y idx)
        (format "%s %s"
          double-arrow-symbol
          (beast-stats->string beast))))
    [x (+ y lines 1)]))

(defn display-item [[x y] item]
  (let [put-str (partial s/put-string *scr* x y)]
    (match item
      {:status :dead} (put-str dead-symbol {:fg :red})
      {:type :dinosaur} (put-str dino-symbol {:fg :yellow})
      {:type :robot :facing dir} (put-str (dir-symbol dir) {:fg :blue})
      :else (put-str empty-symbol))))

(defn display-framing [[x y] world]
  (let [[cols rows] (w/get-size world)
         put-str (partial s/put-string *scr*)]
    (put-str x y corner-symbol)
    (put-str (+ x cols 1) y corner-symbol)
    (put-str x (+ y rows 1) corner-symbol)
    (put-str (+ x cols 1) (+ y rows 1) corner-symbol)

    (doseq [col (range cols)]
      (put-str (+ x col 1) y horz-wall-symbol)
      (put-str (+ x col 1) (+ y rows 1) horz-wall-symbol))
    
    (doseq [row (range rows)]
      (put-str x (+ y row 1) vert-wall-symbol)
      (put-str (+ x cols 1) (+ y row 1) vert-wall-symbol))
    
    [(inc x) (inc y)]))

(defn display-intro [[x y] _]
  (s/put-string *scr* x y "* Robots vs Dinosaurs *")
  [x (+ y 2)])

(defn display-world [world]
  (s/clear *scr*)
  (let [[x y] (display-intro [0 0] world)
        [x y] (display-hud [x y] world)
        [x y] (display-framing [x y] world)
        [_ rows] (s/get-size *scr*)]
    (s/move-cursor *scr* 0 (dec rows))
    (doseq [[row-idx row] (map-indexed vector world)
            [col-idx item] (map-indexed vector row)]
      (let [x (+ x col-idx)
            y (+ y row-idx)]
        (display-item [x y] item))))
  (s/redraw *scr*)
  world)

(defn display-message
  ([msg] (display-message msg {}))
  ([msg opts]
    (let [[cols rows] (s/get-size *scr*)
          msg-y (- rows 2)]
      (clear-line *scr* msg-y)
      (s/put-string *scr* 0 msg-y msg opts)
      (s/redraw *scr*))))

(defn display-error [error]
  (when (f/failed? error)
    (display-message (f/message error) {:fg :red})
  error))

(defn wait-for-exit []
  (let [[_ rows] (s/get-size *scr*)
        cmd-y (dec rows)]
    (s/put-string *scr* 0 cmd-y "Press any key to exit")
    (s/redraw *scr*))
    (read-key *scr*))

(defn wait-for-cmd []
  (let [[_ rows] (s/get-size *scr*)
        cmd-y (dec rows)]
    (clear-line *scr* cmd-y)
    (s/put-string *scr* 0 cmd-y cmd-symbol)
    (s/redraw *scr*)
    (read-cmd *scr* [3 cmd-y])))

(defn- read-cmd
  "Read line for lanterna screen"
  [scr [x0 y]]
  (loop [key ""
          buffer ""
          x x0]
    (case key
      :enter (if (< 0 (count buffer))
              buffer
              (recur (read-key scr) buffer x))
      :escape (do (stop-screen scr) (System/exit 0))
      :left (recur (read-key scr) buffer x)
      :right (recur (read-key scr) buffer x)
      :up (recur (read-key scr) buffer x)
      :down (recur (read-key scr) buffer x)
      :backspace (cond 
                    (= (dec x) x0)
                      (recur (read-key scr) buffer x)
                    :else
                      (let [new-buffer (-> buffer drop-last join)
                          new-x (dec x)]
                        (s/put-string scr (dec new-x) y " ")
                        (s/move-cursor scr (dec new-x) y)
                        (s/redraw scr)
                        (recur (read-key scr)
                              new-buffer
                              new-x)))
      (let [new-buffer (str buffer key)]
        (s/move-cursor scr x y)
        (s/put-string scr x0 y new-buffer)
        (s/redraw scr)
        (recur (read-key scr)
                new-buffer
                (inc x))))))