find . -name "*.java" > build.txt
javac -d . @build.txt
#java com.company.impl.Main 1001 &
#P1 = $!
#java com.company.impl.Main 1002 &
#P2 = $!
#java com.company.impl.Main 1003 &
#P3 = $!
#wait $P1 $P2 $P3
java com.company.impl.Main 1001 > log1.txt 2>&1 &
sleep 1
java com.company.impl.Main 1002 > log2.txt 2>&1 &
sleep .001
java com.company.impl.Main 1003 > log3.txt 2>&1 &
sleep .001
java com.company.impl.Main 1004 > log4.txt 2>&1 &
sleep .001
java com.company.impl.Main 1005 > log5.txt 2>&1 &
