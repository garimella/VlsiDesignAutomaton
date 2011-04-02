#! /bin/sh
name=$1
plotter=$2

$plotter $name.bbb $name.gpl $name.eps
$plotter ${name}_orig.bbb ${name}_orig.gpl ${name}_orig.eps

gnuplot $name.gpl
gnuplot ${name}_orig.gpl

