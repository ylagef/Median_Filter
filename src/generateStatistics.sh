#! /bin/bash

javac MyProblem.java

for i in 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20 50 100 200 300 500
do	
	 java MyProblem $i $2 10 10000
done
