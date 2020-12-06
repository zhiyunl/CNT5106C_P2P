#!/bin/bash
find . -name "*.java" > build.txt
javac -d . @build.txt

java com.company.impl.Main 1001 > log1.txt 2>&1 &
sleep 1
java com.company.impl.Main 1002 > log2.txt 2>&1 &
sleep 1
java com.company.impl.Main 1003 > log3.txt 2>&1 &
sleep 1
java com.company.impl.Main 1004 > log4.txt 2>&1 &
sleep 1
java com.company.impl.Main 1005 > log5.txt 2>&1 &
sleep 1
java com.company.impl.Main 1006 > log6.txt 2>&1 &
sleep 1
java com.company.impl.Main 1007 > log7.txt 2>&1 &
sleep 1
java com.company.impl.Main 1008 > log8.txt 2>&1 &
sleep 1
java com.company.impl.Main 1009 > log9.txt 2>&1 &
