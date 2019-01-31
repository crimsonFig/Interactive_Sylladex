# Interactive_Sylladex
An educational personal project aiming to create an inventory management system mock-up similar to a video game inventory. The system is inspired by the "sylladex" in the webcomic 'Homestuck' by Andrew Hussie. The sylladex in the webcomic being a story mechanic that explained how characters stored their belongings.

# Purpose
This project's purpose is to help serve as an educational environement to learn and get hands on experience with the many facets of software development.

specifically:
- the various features and components of the Java language, along with what the good practices are
- expirementing with design patterns and implementing them
- promote design intuition by simulating and modeling real world storage structures as complex data structures
- learn various tools such as version control, test suites, and project build tools

However, functionally, this project in actual usage is more of a novel toy program as it is designed to appeal to readers of Andrew Hussie's "Homestuck" webcomic through simulating a referenced story mechanic. Akin to bringing a unqiue story mechanic into a real world interactive mock-up.

# How it works
- The `Sylladex` uses three systems to enable dynamic management of items. 
  - The inventory system is composed of `Card`s where a `Card` can hold a supplied item the user "gives". 
    - The `Sylladex` holds a 'master deck' that contains the current inventory state in a standardized format of `Card`s
    - The `ModusBuffer` holds a reference to the 'master deck', which can be modify the 'master deck' but not replace it
    - A `Modus` holds a unique data structure of `Card`s which it can save/load to the `ModusBuffer` after converting it
  - The interfacing system is composed of a `ModusManager` that has a list of `Metadata` models and a `ModusBuffer` model
    - A `Metadata` model supplies information about a given `Modus` unit and enables `ModusManager` to map user-input to lambda expressions the `Modus` supplies as functionality
    - A `ModusBuffer` model supplies all the required information a `Modus` unit requires, utilizing dependency inversion
  - The management system is composed of `Modus`s that determine how items can be stored and retrieved from a `Card` by building complex and abstract data structed composed of `Card`s unique to that individual `Modus` 
    - specificially, each `Modus` implements a unique way of interacting with the data structure that is unique to that modus 
    - A `Modus` can be loaded and swapped out for a different `Modus` unit with full compatibility thanks to `Metadata`
    - The `Modus` interface and `Metadata` model enables the inventory to be used by each `Modus` without needing to reset
      - each unit must be able to convert it's unique storage abstraction into a standardized simple "deck" of `Card`s and back when passed out of the unit and into the unit respectively
      - each unit must only be allowed to manipulate the data held by `ModusBuffer` acting as a buffer of standardized required data for the `Modus` units that they can access when swapped out, much like a port

### In summary: 
layer structure: view | `Sylladex` | `ModusManager` | (`ModusBuffer` & `Metadata`) | `Modus` 
- The Interactive Sylladex works by providing a `GUI` for a user to submit commands similar to a terminal
- a command details some operation to apply to the inventory system
- a `Sylladex` broadcasts submitted commands and manages the low abstraction data as a "master deck"
- a `ModusManager` provides interfacing with modular `Modus` units
- many `Modus` units provide implementation for how to interact with a unique inventory storage structure 
- a `ModusBuffer` provides data state that a `Modus` unit uses, enabling easy modular swapping of `Modus` units

# Build Instruction
The current project (java version) can be compiled using a java 1.8 JDK compatible compiler (e.g javac, eclipse), using `src` as the source folder on the class path. NOTE: a maven XML file will be added shortly as a build tool for helping this regard.

The deprecated project (C version) may be compilable using included Makefile and a compatible c compiler (e.g. GCC). NOTE: this version will be soon branched and removed from the master branch in the upcoming commits.

# Milestone History
## Milestone 2 - Refactored/overhauled several key structure implementations 
`01/30/2019` [Milestone Pull](https://github.com/crimsonFig/Interactive_Sylladex/pull/1)
- The project is now at the second iteration of a working and demo-able prototype
### removed co-dependencies and loosen coupling wherever possible
- class structure now uses a layered design with dependency inversion to form a one way dependency tree. 
  - the layers goes from `Sylladex` (lowest level) to `Modus` (high level), abstracting with models
  - `Sylladex` (controller) depends on `ModusManager` (container) to interface with the `Modus`
  - `ModusManager` depends on `ModusBuffer` (model) and `Modus` (sub-controller)
  - `Modus` depends on `ModusBuffer`, `Metadata` (model), and `Card` (model)
- the layered design also uses generics, data encapsulation, and polymorphism to achieve modularity
  - the `ModusManager` uses the `Modus` interface in conjunction with lambdas in a `CommandMap` to dynamically execute functionality from the currently connected `Modus`
  - the `ModusBuffer` encapsulates data and provides required data to the `Modus` while also buffering this wrapped data between any swapped `Modus` objects.
  - the `Sylladex` provides all required information in wrapped objects to enable soft linkage of data and allow the `Sylladex` to hold data manipulated by the `Modus` without having to collect it, utilizing the 'double pointer/reference' pattern design used in the C language.
### untangled older monolithic, spaghetti-esque code and purified code smells
- refactored a lot of code to be more modular, reducing redundancies, and streamlining complexities
- utilized more SOLID and GRASP principles of 'OO design' within redesigned structure
- broke down several classes and methods that were becoming monolithic to improve modularity, maintainability, and testability
### improved the testability of the code
- utilized 'decomposition of responsibility' and 'data encapsulation' to make methods of classes more testable 
  - focus is more on interfacing with what is promised and less on implemented procedure
- utilized strong `exception` handling practices to make debugging information more expressive and easier to find
  - custom exceptions associated with certain levels of severity and origin for precise handling and communication
## Milestone 1 - Created first prototype with working implementation
`04/21/2018` [Milestone Commit](https://github.com/crimsonFig/Interactive_Sylladex/commit/f8b0dbb654ff14b16f220137340476e23c748782)
### Minimal functional requirements implemented
- GUI can be launched with proper interactions with the `PentaFile Modus` unit.
