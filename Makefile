# The sylladex and modi files
OBJECTS = sylladexFramework.o pentaFile.o tarotDeck.o
# Header files
HEADERS = sylladexFramework.h pentaFile.h tarotDeck.h

sylladexTest: ${OBJECTS}
	gcc -g -o sylladexTest ${OBJECTS}

%.o: %.c ${HEADERS}
	gcc -g -c $<

clean:
	rm -f ${OBJECTS}
