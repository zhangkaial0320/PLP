/**  Important to test the error cases in case the
 * AST is not being completely traversed.
 * 
 * Only need to test syntactically correct programs, or
 * program fragments.
 */

package cop5556sp17;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import cop5556sp17.AST.ASTNode;
import cop5556sp17.AST.Dec;
import cop5556sp17.AST.IdentExpression;
import cop5556sp17.AST.Program;
import cop5556sp17.AST.Statement;
import cop5556sp17.Parser.SyntaxException;
import cop5556sp17.Scanner.IllegalCharException;
import cop5556sp17.Scanner.IllegalNumberException;
import cop5556sp17.TypeCheckVisitor.TypeCheckException;

public class TypeCheckVisitorTest 
{
	

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void testAssignmentBoolLit0() throws Exception
	{
		String input = "p {\nboolean y \ny <- false;}";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode program = parser.parse();
		TypeCheckVisitor v = new TypeCheckVisitor();
		program.visit(v, null);		
		// /System.out.println(str1);
	}

	@Test
	public void testAssignmentBoolLitError0() throws Exception
	{
		String input = "p {\nboolean y \ny <- 3;}";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode program = parser.parse();
		TypeCheckVisitor v = new TypeCheckVisitor();
		thrown.expect(TypeCheckVisitor.TypeCheckException.class);
		program.visit(v, null);		
	}		
	@Test
	public void testProgram0() throws Exception
	{
		String input = "prog0 integer a, boolean b, boolean c  	\n"+
					   "{   									\n"+
					   "	integer a							\n"+
					   "    integer a							\n"+
					   "    boolean c							\n"+
					   " 	if (true) {}						\n"+
					   "}										\n";

		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode program = parser.parse();
		thrown.expect(TypeCheckVisitor.TypeCheckException.class);
		TypeCheckVisitor v = new TypeCheckVisitor();
		program.visit(v, null);	
	}
	@Test
	public void testWhileStatement1() throws Exception
	{
		String input = "prog0 integer a, integer b, boolean c  \n" +
					   "{   \n"+
					   "	integer x			\n"+
					   "    integer a				\n"+
					   "    /*boolean c*/		\n"+
					   "	if (true) 			\n"+
					   "	{            		\n"+
					   "		image a 		\n"+
					   "      	x<-3;  			\n"+
					   "	}					\n"+
					   "	while (true) 		\n"+
					   "	{            		\n"+
					   "		/*integer a*/ 	\n"+				
					   " 		a<-4;			\n"+
					   " 		a<-2;			\n"+
					   "	}					\n"+
					   "}\n";

		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode program = parser.parse();
		//thrown.expect(TypeCheckVisitor.TypeCheckException.class);
		TypeCheckVisitor v = new TypeCheckVisitor();
		program.visit(v, null);	
	}
	@Test
	public void testBinaryExpr6Error() throws Exception
	{
		String input = "p url y \n"+
					   "{"
					   + "frame x  \n"+
					     "boolean z \n"
					   + "z <- x == y;\n"
					   + "}";

		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode program = parser.parse();
		thrown.expect(TypeCheckVisitor.TypeCheckException.class);
		TypeCheckVisitor v = new TypeCheckVisitor();
		program.visit(v, null);	
	}
	@Test
	public void testBinaryExpr2Error() throws Exception
	{
		String input =" p {\n"+
						"integer x \n"+
						"integer y \n"+
						"boolean z \n"+
						"z <- x < y;\n"+
						"z <- x > y;\n"+
						"z <- 33 <= 44;\n"+
						" z <- 33 >= 55;\n}";

		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode program = parser.parse();
		//thrown.expect(TypeCheckVisitor.TypeCheckException.class);
		TypeCheckVisitor v = new TypeCheckVisitor();
		program.visit(v, null);	
	}
	@Test
	public void testBinaryExpr3Error() throws Exception
	{
		String input = "tos url u, integer x		\n"+
					   "{							\n"+
						"	integer y				\n"+
						"	image i 				\n"+
						"	u -> i; 				\n"+
						"	i -> height -> x;		\n"+	// (image->IMGAE_OP)->integer
						"	frame f 				\n"+
						"	i -> scale (x) -> f;	\n"+  	//no problem
						"}";


		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode program = parser.parse();
		//thrown.expect(TypeCheckVisitor.TypeCheckException.class);
		TypeCheckVisitor v = new TypeCheckVisitor();
		program.visit(v, null);	
	}
}
