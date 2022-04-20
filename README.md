# todoapp

Simple backend to hold todos and tasks that belong to them.

## Installation

## Prerequisites
A postgres instance is needed for the app, so either run your own instance or use the included `docker-compose.yml` with `docker-compose up`.

Use `chmod +x  script-name.sh`to make the scripts in bin executable.

A  dot-file called .secrets.edn in your $HOME is needed.
You can use `bin/create-secrets.sh` to automatically create it.

In order to install the application run `bin/build.sh` to create a .jar file. 

## Usage 
After using the build script, you can easily run the application with `java -jar todoapp.jar`

To run tests with a nice report use `bin/koacha.sh`or `clj -X:test`if you can do without the report part :)



Copyright © 2022 Ben Hartmann

_EPLv1.0 is just the default for projects generated by `clj-new`: you are not_
_required to open source this project, nor are you required to use EPLv1.0!_
_Feel free to remove or change the `LICENSE` file and remove or update this_
_section of the `README.md` file!_

Distributed under the Eclipse Public License version 1.0.
