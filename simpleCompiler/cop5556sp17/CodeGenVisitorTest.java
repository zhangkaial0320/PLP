
package cop5556sp17;

import java.io.FileOutputStream;
import java.io.OutputStream;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import cop5556sp17.AST.ASTNode;
import cop5556sp17.AST.Program;
import cop5556sp17.PLPRuntimeLog;
import java.lang.Thread;


public class CodeGenVisitorTest 
{

	static final boolean doPrint = true;
	static void show(Object s) 
	{
		if (doPrint) 
		{
			System.out.println(s);
		}
	}

	boolean devel = false;
	boolean grade = true;
	
    public void initLog()
    {
        if (devel || grade) 
            PLPRuntimeLog.initLog();
    }
    
    public void printLog()
    {
        System.out.println(PLPRuntimeLog.getString());
    }
	
	@Test
	 public void emptyProg2() throws Exception 
	 {
	 	//scan, parse, and type check the program
		initLog();
	 	String progname = "emptyProg";
	 	String input = progname + 
	 			"  integer y "
	 			+ "{ integer x"
	 			+ "	x <- 6;"
	 			+ " 	while(x >= 0) {  "
	 			+ "		x <- 0 - 1;"
	 			+ "	}" 
	 			+ "}";		
	 	Scanner scanner = new Scanner(input);
	 	scanner.scan();
	 	Parser parser = new Parser(scanner);
	 	ASTNode program = parser.parse();
	 	TypeCheckVisitor v = new TypeCheckVisitor();
	 	program.visit(v, null);
	 	//show(program);
		
	// 	//generate code
		
	 	CodeGenVisitor cv = new CodeGenVisitor(devel,grade,null);
	 	byte[] bytecode = (byte[]) program.visit(cv, null);
	 	//show(bytecode);

	// 	//output the generated bytecode
	 	CodeGenUtils.dumpBytecode(bytecode);
		
	 	//write byte code to file 
	 	String name = ((Program) program).getName();

	 	String classFileName = "bin" + name + ".class";
	 	OutputStream output = new FileOutputStream(classFileName);
	 	output.write(bytecode);
	 	output.close();
	 	System.out.println("wrote classfile to " + classFileName);
		
	 	// directly execute bytecode
	 	String[] args = {"0"}; //create command line argument array to initialize params, none in this case
	 	Runnable instance = CodeGenUtils.getInstance(name, bytecode, args);
	 	instance.run();
	 	printLog();
	 }

}

