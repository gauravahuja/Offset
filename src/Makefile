PLAYER1=alphabeta
PLAYER2=dumb

D=5
OUTPUT=./output.txt
DO_DISPLAY=0

PQ_LEFT  = 2 3
PQ_RIGHT = 1 4
run:
	java offset.sim.Offset $D $(PLAYER1) $(PLAYER2) $(DO_DISPLAY) $(OUTPUT) $(PQ_LEFT) $(PQ_RIGHT)

compile:
	./rmClassFiles.sh
	javac ./offset/sim/*.java
