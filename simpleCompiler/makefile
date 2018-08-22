JFLAGS = -g
JC = javac -cp .:/usr/share/java/junit4.jar:/usr/share/java/hamcrest-core.jar:/usr/share/java/asm_util_6.0.jar:/usr/share/java/asm6.0.jar
RUN = java -cp .:/usr/share/java/junit4.jar:/usr/share/java/hamcrest-core.jar:/usr/share/java/asm_util_6.0.jar:/usr/share/java/asm6.0.jar org.junit.runner.JUnitCore
.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java

CLASSES = \
	cop5556sp17/Parser.java\
	cop5556sp17/SymbolTable.java\
	cop5556sp17/CodeGenUtils.java\
	cop5556sp17/CodeGenVisitorTest.java\
	cop5556sp17/CodeGenVisitor.java\
	cop5556sp17/TypeCheckVisitor.java\
	cop5556sp17/Scanner.java\
	cop5556sp17/AST/ASTNode.java\
	cop5556sp17/AST/ASTVisitor.java\


default: classes

classes: $(CLASSES:.java=.class)

test: classes
	$(RUN) cop5556sp17.CodeGenVisitorTest

clean:
	$(RM) cop5556sp17/*.class
	$(RM) cop5556sp17/AST/*.class