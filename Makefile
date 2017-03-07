# The sylladex and modi files
OBJECTS = sylladexFile.o pentaFile.o
# Header files
HEADERS = pentaFile.h

p3: ${OBJECTS}
	gcc -g -o SylladexTest ${OBJECTS}
%.o: %.c ${HEADERS}
	gcc -g -c $<
clean:
	rm -f ${OBJECTS}
