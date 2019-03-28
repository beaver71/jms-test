#!/bin/bash
cls="$1"
echo ">Starting $cls client..."
params="${@:2}"
java -cp "target/classes/:target/dependency/*" test.$cls $params
