#!/usr/bin/env sh
for i in `find ./ | grep "class"`;do echo "rm $i"; rm $i;done
