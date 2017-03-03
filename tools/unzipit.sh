#!/bin/bash
# Unzip all the zipfiles you find in the current folder tree

echo Unzipping everything from `pwd`
for i in $(find . -name "*.zip"); do
    echo
    echo "*** Unzipping " $i
    pushd "`dirname $i`"
    mkdir "`basename $i`".unzipped
    pushd "`basename $i`".unzipped
    unzip "../`basename $i`"
    popd
    popd
done
