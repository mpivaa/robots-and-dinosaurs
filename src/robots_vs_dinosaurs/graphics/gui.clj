(ns robots-vs-dinosaurs.graphics.gui
  (:require [robots-vs-dinosaurs.world :as w]
            [clojure.core.match :refer [match]])
  (:import
   [javax.swing JFrame]
   [javax.swing JPanel]
   [java.awt Graphics]
   [java.awt Dimension]
   [java.awt Graphics2D]
   [java.awt BasicStroke]
   [java.awt RenderingHints]
   [java.awt Color]
   [java.awt.geom Path2D$Double]))

;; Display the world using Java 2D Graphics

(declare ^:dynamic *frame*)

(def margin 25)
(def tile-size 8)
(def gap 6)
(def row-size (+ gap tile-size))
(def col-size (+ gap tile-size))
(def yellow (Color. 0 150 100))
(def grey (Color. 200 200 200))
(def red (Color. 150 0 0))
(def blue (Color. 0 0 150))
(def green (Color. 0 150 0))
(def black (Color. 33 33 33))

(defn create-frame [] (JFrame. "Robots vs Dinosaurs"))

(defmacro bind [& body]
  `(binding [*frame* (create-frame)] ~@body))

(defn update-frame [pane]
  (doto *frame*
    (.setContentPane pane)
    .pack
    (.setDefaultCloseOperation JFrame/EXIT_ON_CLOSE)
    (.setVisible true)))

(defn create-pane [[w h] paint]
  (doto
   (proxy [JPanel] []
     (paintComponent [^Graphics2D g]
       (.setRenderingHint g
                           RenderingHints/KEY_ANTIALIASING
                           RenderingHints/VALUE_ANTIALIAS_ON)
       (.setStroke g (BasicStroke. 2))
       (paint g)))
     (.setPreferredSize (Dimension. w h))))

(defn get-pane-size [world]
  (let [[w h] (w/get-size world)
        hud-h (-> world (w/get-beasts) count (* row-size))
        w (+ (* w row-size) (* margin 2))
        h (+ (* h row-size) (* margin 3) hud-h)]
    [w h]))

(defn dead-symbol [[x y]]
  (doto (Path2D$Double.)
    (.moveTo x y)
    (.lineTo (+ x tile-size) (+ y tile-size))
    (.moveTo (+ x tile-size) y)
    (.lineTo x (+ y tile-size))))

(defn dino-symbol [[x y]]
  (doto (Path2D$Double.)
    (.moveTo x (+ y (/ tile-size 2)))
    (.lineTo x (+ y (/ tile-size 1.3)))
    (.lineTo (+ x (/ tile-size 1.62)) (+ y (/ tile-size 1.32)))
    (.lineTo (+ x (/ tile-size 1.62)) y)
    (.lineTo (+ x tile-size) y)

    (.moveTo (+ x (/ tile-size 2.25)) (+ y (/ tile-size 1.32)))
    (.lineTo (+ x (/ tile-size 2.25)) (+ y tile-size))

    (.moveTo (+ x (/ tile-size 5.6)) (+ y (/ tile-size 1.32)))
    (.lineTo (+ x (/ tile-size 5.6)) (+ y tile-size))

    (.moveTo (+ x (/ tile-size 1.62)) (+ y (/ tile-size 2.5)))
    (.lineTo (+ x (/ tile-size 1.45)) (+ y (/ tile-size 2.5)))))

(defn up-symbol [[x y]]
  (doto (Path2D$Double.)
    (.moveTo x (+ y tile-size))
    (.lineTo (+ x (/ tile-size 2)) y)
    (.lineTo (+ x tile-size) (+ y tile-size))
    (.closePath)))

(defn right-symbol [[x y]]
  (doto (Path2D$Double.)
    (.moveTo x y)
    (.lineTo (+ x tile-size) (+ y (/ tile-size 2)))
    (.lineTo x (+ y tile-size))
    (.closePath)))

(defn down-symbol [[x y]]
  (doto (Path2D$Double.)
    (.moveTo x y)
    (.lineTo (+ x (/ tile-size 2)) (+ y tile-size))
    (.lineTo (+ x tile-size) y)
    (.closePath)))

(defn left-symbol [[x y]]
  (doto (Path2D$Double.)
    (.moveTo (+ x tile-size) y)
    (.lineTo x (+ y (/ tile-size 2)))
    (.lineTo (+ x tile-size) (+ y tile-size))
    (.closePath)))

(defn dir-symbol [dir [x y]]
  (case dir
    :N (up-symbol [x y])
    :E (right-symbol [x y])
    :S (down-symbol [x y])
    :W (left-symbol [x y])))

(defn draw-empty [[x y] ^Graphics2D g]
  (doto g
    (.setPaint grey)
    (.fillOval
      (+ x (/ tile-size 3))
      (+ y (/ tile-size 3))
      (/ tile-size 1.5)
      (/ tile-size 1.5))))

(defn draw-item [[x y] item ^Graphics2D g]
  (match item
    {:status :dead}
      (doto g
        (.setPaint red)
        (.draw (dead-symbol [x y])))
    {:type :dinosaur}
      (doto g
        (.setPaint green)
        (.draw (dino-symbol [x y])))
    {:type :robot :facing dir}
      (doto g
        (.setPaint blue)
        (.draw (dir-symbol dir [x y])))
    :else (draw-empty [x y] g)))

(defn draw-hud [world ^Graphics2D g]
  (let [[rows _] (w/get-size world)
        x margin
        y (+ (* margin 2) (* rows row-size))
        beasts (w/get-beasts world)]
    (doseq [[idx beast] (map-indexed vector beasts)]
      (let [y (+ y (* idx row-size))]
        (draw-item [x y] beast g)
        (doto g
          (.setPaint black)
          (.drawString ^String (str (:id beast))
                          ^int (+ x col-size)
                          ^int (+ y tile-size)))))))

(defn draw-world [world ^Graphics2D g]
  (let [[x y] [margin margin]]
    (doseq [[row-idx row] (map-indexed vector world)
            [col-idx item] (map-indexed vector row)]
      (let [x (+ x (* col-idx col-size))
            y (+ y (* row-idx row-size))]
        (draw-item [x y] item g)))
    (draw-hud world g)))

(defn draw [world]
  (let [size (get-pane-size world)
        pane (create-pane size (partial draw-world world))]
    (-> pane update-frame))
  world)
