#!/bin/bash

SRC_PATH="../src/cool"

setup()
{
    cp -r "$SRC_PATH/"* cool/
    make build
    make tester
}

cleanup()
{
    make clean
}

run_tests()
{
    make run
}

setup
if [ -z "$1" ] ; then
    run_tests
fi

cleanup
