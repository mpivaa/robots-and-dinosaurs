# Robots vs Dinosaurs

Simulate the fight of robots against dinosaurs.

## Requirements

- Java 8+
- Leiningen

## Installation

Install dependencies:

    lein deps

## Usage

### REPL

Run repl:

    lein run

An empty world will be created so you can test your strategies one
command at time.

Type `(help)` so see the list of commands.

Try introducing typos like `(mov)` or `(atack)`.

### Scripts

Build your scripts with all steps and run the simulation.

Run scripts with:

    lein run -f examples/basic.clj  

Script example:

```clj
[(empty-world 10 10)
 (deploy 2 2 (dinosaur))
 (deploy 0 0 (robot :id :robot :facing :E))
 (move :forward :robot)
 (move :forward :robot)
 (turn :left :robot)
 (move :forward :robot)
 (attack :robot)]
```

See more examples at `examples/`.

### Library

Import the library in your project to add new behaviours
to your simulation.

Install the library in your local repository using `lein install` and add de dependency:

    [robots-dinosaurs "0.2.0"]

Then import the library as in the example:

```clj

(ns my-simulation.core
  (:require [robots-vs-dinosaurs.simulation :refer [run->>]])
  (:use [robots-vs-dinosaurs.world]
        [robots-vs-dinosaurs.commands]))

(defn run []
  (run->> (empty-world 10 10)
          (deploy 0 0 (dinosaur))
          (deploy 1 1 (robot :id :robot))
          (move :forward :robot)))

```

Custom behaviours should receive a `world` as the last argument and return a
new world as result or `fail` with a message.

## API

### robots-vs-dinosaurs.simulation

`run->>`

Run each step of the simulation, short-circuting if something goes wrong.

The first argument needs to be a `world` (see `(empty-world)`).

Act as `->>` threading the result.

Returns a new `world` or a `failure` (from `failjure`).

`run-interactive->>`

Same as `run->>` but display the result at each step in a graphical simulation.

`(read-cmd world)`

Reads user input and evaluate, returns a new world.

`(repl-cmd world)`

### robots-vs-dinosaurs.commands

All commands returns a new `world`.

`(deploy x y beast world)`

Deploy a robot or a dinosaur to the world.

`(move dir robot-id world)`

Move a robot to a direction (:forward or :backward).

`(turn dir robot-id world)`

Turn a robot to a direction (:left or :right).

`(attack robot-id world)`

Command robot to attack.

`(reset world)`

### robots-vs-dinosaurs.world

`(empty-world width height)`

Creates a new empty world.

`(dinosaur)`

Creates a dinosaur.

`(robot :id id :facing facing)`

Creates a new robot with the given :id, facing a direction (:N, :E, :S, :W).

### robots-vs-dinosaurs.display

`(display-world world)`

Display the current world state graphically.

`(display-message msg)`

Display a message in the console.

`(display-error error)`

Display a `failjure` error in the console.

## Testing

Run tests:

    lein test

## Standalone

Create the jar with:

    lein uberjar
