#!/bin/bash

SRC_PATH="../src"

setup()
{
    cp -r --no-clobber $SRC_PATH/* .
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
