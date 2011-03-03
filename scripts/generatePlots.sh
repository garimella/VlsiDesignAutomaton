#! /bin/sh
name=$1
plotter=$2

$plotter $name.bbb $name.gpl $name.svg
$plotter ${name}_orig.bbb ${name}_orig.gpl ${name}_orig.svg

gnuplot $name.gpl
gnuplot ${name}_orig.gpl

