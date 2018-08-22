package cop5556sp17;

import cop5556sp17.AST.*;
import cop5556sp17.Scanner.Kind;
import static cop5556sp17.Scanner.Kind.*;
import cop5556sp17.Scanner.Token;
import java.util.*;
import cop5556sp17.Scanner.*;

import java.io.FileOutputStream;
import java.io.OutputStream;

public class Parser 
{

	/**
	 * Exception to be thrown if a syntax error is detected in the input.
	 * You will want to provide a useful error message.
	 *
	 */
	@SuppressWarnings("serial")
	public static class SyntaxException extends Exception 
	{
		public SyntaxException(String message) 
		{
			super(message);
		}
	}
	
	/**
	 * Useful during development to ensure unimplemented routines are
	 * not accidentally called during development.  Delete it when 
	 * the Parser is finished.
	 *
	 */
	@SuppressWarnings("serial")	
	public static class UnimplementedFeatureException extends RuntimeException 
	{
		public UnimplementedFeatureException() 
		{
			super();
		}
	}

	Scanner scanner;
	Token t;
	HashSet<Kind> m_relOp;
	HashSet<Kind> m_weakOp;
	HashSet<Kind> m_strongOp;
	HashSet<Kind> m_allOp;
	Parser(Scanner scanner) 
	{
		this.scanner = scanner;
		t = scanner.nextToken();
		List<Kind>  relOpList =  Arrays.asList(LT, LE, GT, GE, EQUAL, NOTEQUAL);
		List<Kind>  weakOpList =  Arrays.asList(PLUS, MINUS, OR);
		List<Kind>  strongOpList =  Arrays.asList(TIMES, DIV, AND, MOD);

		m_relOp = new HashSet<Kind>();
		m_relOp.addAll(relOpList);
		m_weakOp = new HashSet<Kind>();
		m_weakOp.addAll(weakOpList);
		m_strongOp = new HashSet<Kind>();
		m_strongOp.addAll(strongOpList);
	}

	/**
	 * parse the input using tokens from the scanner.
	 * Check for EOF (i.e. no trailing junk) when finished
	 * 
	 * @throws SyntaxException
	 */
	ASTNode parse() throws SyntaxException 
	{
		ASTNode astNode = program();
		matchEOF();
		return astNode;
	}

	// expression = term ( relOp term)*
	Expression expression() throws SyntaxException 
	{
		//TODO
		Expression e0;
		e0 = term();
		while(m_relOp.contains(t.kind))
		{
			Token op = t;
			consume();
			Expression e1 = term();
			e0 = new BinaryExpression(e0.getFirstToken(),e0,op,e1);
		}
		return e0;
	}

	// term = elem ( weakOp  elem)*
	Expression term() throws SyntaxException 
	{
		//TODO
		Expression e0;
		e0 = elem();
		while(m_weakOp.contains(t.kind))
		{
			Token op = t;
			consume();
			Expression e1 = elem();
			e0 = new BinaryExpression(e0.getFirstToken(),e0,op,e1);
		}
		return e0;
	}

	// elem = factor ( strongOp factor)*
	Expression elem() throws SyntaxException 
	{
		//TODO
		Expression e0;
		e0 = factor();
		while(m_strongOp.contains(t.kind))
		{
			Token op = t;
			consume();
			Expression e1 = factor();
			e0 = new BinaryExpression(e0.getFirstToken(),e0,op,e1);
		}
		return e0;
	}

	// factor = IDENT | INT_LIT | KW_TRUE | KW_FALSE | KW_SCREENWIDTH | KW_SCREENHEIGHT | ( expression )
	Expression factor() throws SyntaxException 
	{
		Expression e;
		Kind kind = t.kind;
		switch (kind) 
		{
			case IDENT: 
			{
				e = new IdentExpression(consume());
			}
				break;
			case INT_LIT: 
			{
				e = new IntLitExpression(consume());
			}
				break;
			case KW_TRUE:
			case KW_FALSE: 
			{
				e = new BooleanLitExpression(consume());
			}
				break;
			case KW_SCREENWIDTH:
			case KW_SCREENHEIGHT: 
			{
				e = new ConstantExpression(consume());
			}
				break;
			case LPAREN: 
			{
				consume();
				e = expression();
				match(RPAREN);
			}
				break;
			default:
				String errMsg= "Illegal factor: in " + t.getLinePos().toString()+"token " + "'"+t.getText()+"'"+
								"is illegal";
				throw new SyntaxException(errMsg);
		}
		return e;
	}

	// block = { ( dec | statement) * }
	Block block() throws SyntaxException 
	{
		//TODO
		Block block = null;
		//If block() is invoked, then there must be "{", so just consume this token.
		consume();		
		ArrayList<Dec> decList = new ArrayList<Dec>(); 					
		ArrayList<Statement> statementList = new ArrayList<Statement>();
		//looking for the "}" token. (Here, infinite loop will happen, so have to think about some methods to avoid this)
		while( (false == t.isKind(RBRACE)) && (false == t.isKind(EOF)) )
		{

			if( (t.kind == KW_INTEGER) || (t.kind == KW_BOOLEAN) ||
				(t.kind == KW_IMAGE) || (t.kind == KW_FRAME) )
			{
				decList.add(dec());
			}
			else
			{
				statementList.add(statement());
			}
		}
		//For the case, the while is broke by EOF, instead of "}", which is error. So just match "}" to make sure.
		match(RBRACE);	
		Token firstToken = null;

		if(decList.size() != 0)
			firstToken = decList.get(0).getFirstToken();
		else if(statementList.size() !=0 )
			firstToken = statementList.get(0).getFirstToken();
		
		block = new Block(firstToken, decList, statementList);
		return block;
	}
	// dec = (  KW_INTEGER | KW_BOOLEAN | KW_IMAGE | KW_FRAME)    IDENT
	Dec dec() throws SyntaxException 
	{
		//TODO
		Dec dec;
		Token type = consume();		//Consume  KW_INTEGER | KW_BOOLEAN | KW_IMAGE | KW_FRAME
		Token ident = t;
		match(IDENT);	//The follow set of dec is IDENT, so just need to match IDENT token.
		dec = new Dec(type,ident);
		return dec;
	}

	// program =  IDENT block
	// program =  IDENT param_dec ( , param_dec )*   block
	Program program() throws SyntaxException 
	{
		//TODO	
		// predict set of program is IDENT, if match successfullu, match() will consume the current token,
		// then, t will be the next token, otherwise, exception will be thrown.
		Token firstToken = t;
		Program program = null;
		Block b = null;
		match(IDENT);	
		switch(t.kind)
		{
			case LBRACE: 
				b = block();
				program = new Program(firstToken,new ArrayList<ParamDec>(),b);
				break;
			case KW_URL:
			case KW_FILE:
			case KW_INTEGER:
			case KW_BOOLEAN:
				ArrayList<ParamDec> params = new ArrayList<ParamDec>();
				params.add(paramDec());
				while(t.isKind(COMMA))
				{
					consume();
					params.add(paramDec());
				}
				b = block();
				program = new Program(firstToken,params,b);
				break;
			default:
					String errMsg = "In " + t.getLinePos().toString()+"token " + "'"+t.getText()+"'"+
									"is illegal";
					throw new SyntaxException(errMsg);
				
		}
		return program;
	}

	// param_dec = ( KW_URL | KW_FILE | KW_INTEGER | KW_BOOLEAN)   IDENT
	ParamDec paramDec() throws SyntaxException 
	{
		//TODO
		Token type = t;
		match(KW_URL,KW_FILE,KW_INTEGER, KW_BOOLEAN);		//Consume KW_URL | KW_FILE | KW_INTEGER | KW_BOOLEAN
		Token ident = t;
		match(IDENT);	//Match IDENT
		return new ParamDec(type,ident);
	}

	// In statement= OP_SLEEP expression ; | whileStatement | ifStatement | chain ; | assign ;
	// If the token is IDENT, if could be expression, chain or assign, so need to leak ahead the next
	// token to tell which rule is being used.
	Statement ChainOrAssignInStatement() throws SyntaxException
	{
		Token next = scanner.peek(); //look ahead the next token
		// If this token is ASSIGN, then assign = has be used, then next one has to be expression
		if(next.isKind(ASSIGN))	
		{
					
			IdentLValue ident = new IdentLValue(consume());		// Consume IDENT
			consume();											// Consume ASSIGN
			Expression e = expression();						// match expression
			match(SEMI);										// " ; " is follow of assign
			return new AssignmentStatement(ident.getFirstToken(), ident, e);
		}
		else					// This is chain
		{
			Chain c = chain();
			match(SEMI);
			return c;
		}
	}
	// statement =   OP_SLEEP expression ; | whileStatement | ifStatement | chain ; | assign ;
	Statement statement() throws SyntaxException 
	{
		//TODO
		//consume();		//Consume OP_SLEEP token
		Statement statement = null;
		Token firstToken;
		Expression e;
		switch(t.kind)
		{
			case OP_SLEEP:
				firstToken = consume();	//"Sleep" token
				e = expression();	// expression
				statement = new SleepStatement(firstToken,e);
				match(SEMI);
				break;
			case KW_WHILE:case KW_IF: //For these two, same pattern --> (expression) block
				boolean isWhile = true;
				if(t.isKind(KW_IF))
					isWhile = false;
				firstToken = consume();
				match(LPAREN);
				e = expression();
				match(RPAREN);
				Block b = block();
				
				statement = isWhile ? new WhileStatement(firstToken,e,b) : new IfStatement(firstToken,e,b);
				break;
			case IDENT: 	//Since IDENT could be first of expression, chain, assign, so we need to leakahead next token
				statement = ChainOrAssignInStatement(); //In this function, " ; " is matched, so don't need to match it here.
				break;
			case OP_BLUR: case OP_GRAY: case OP_CONVOLVE:
			case KW_SHOW: case KW_HIDE: case KW_MOVE: case KW_XLOC: 
			case KW_YLOC: case OP_WIDTH: case OP_HEIGHT: case KW_SCALE:
				statement = chain();
				match(SEMI);
				break;
			default:
				String errMsg = "Statement error: in " + t.getLinePos().toString() + 
								" '" + t.getText() + "' is not expected here.";
				throw new SyntaxException(errMsg);
		}
		return statement;
	}

	// Here, some ambiguity happens. In the production of chain, ";" not appear,
	// but in the start symbol of chain, which is "statemen = chain;", ";" appear,
	// so from global view, if you test the whole program, ";" is expected. But if
	// you just test production 'chain', then ";" is not necesseay. So currently,
	// I put checking of ";" in statement, instead of "chain". Don't whether this is
	// correct solution.
	// chain =  chainElem arrowOp chainElem ( arrowOp  chainElem)*
	Chain chain() throws SyntaxException 
	{
		//TODO
		// If chain() has been invoked, then we know that among OP_BLUR,OP_GRAY,OP_CONVOLVE,KW_SHOW,KW_HIDE,
		// KW_MOVE,KW_XLOC,KW_YLOC,OP_WIDTH,OP_HEIGHT,KW_SCALE, one has been matched
		Chain e0;
		ChainElem e1;
		Token arrow;
		e0 = chainElem();
		arrow = arrowOp();
		e1 = chainElem();
		e0 = new BinaryChain(e0.getFirstToken(),e0,arrow,e1);

		
		// Wait until ";" appears.
		// But if there is no ";" until end of file, then this is an error
		while( (false == t.isKind(SEMI)) && (false == t.isKind(EOF)) )	
		{
			arrow = arrowOp();
			e1 = chainElem();
			e0 = new BinaryChain(e0.getFirstToken(),e0,arrow,e1);	//Notice that chainArrow is left associative
			// System.out.println(e0.getFirstToken().getText());
			// System.out.println(e1.getFirstToken().getText());
		}
		return e0;
	}

	// chainElem = IDENT | filterOp arg | frameOp arg | imageOp arg
	ChainElem chainElem() throws SyntaxException 
	{
		//TODO
		ChainElem chainElem = null;
		if(t.isKind(IDENT))
		{
			chainElem = new IdentChain(consume());
			return chainElem;
		}
		else
		{
			// try to match one of them, filterOP | frameOP | imageOp
			Token op = t;
			 match(OP_BLUR,OP_GRAY,OP_CONVOLVE,KW_SHOW,KW_HIDE,
				  KW_MOVE,KW_XLOC,KW_YLOC,OP_WIDTH,OP_HEIGHT,KW_SCALE);
			Tuple tuple = arg();
			if(op.isKind(OP_GRAY) || op.isKind(OP_CONVOLVE) || op.isKind(OP_BLUR) )
				chainElem = new FilterOpChain(op, tuple);
			else if(op.isKind(OP_WIDTH) || op.isKind(OP_HEIGHT)|| op.isKind(KW_SCALE))
				chainElem = new ImageOpChain(op, tuple);
			else
				chainElem = new FrameOpChain(op, tuple);
			return chainElem;
		}
	}
	Token arrowOp() throws SyntaxException
	{
		Token arrow = t;
		match(ARROW, BARARROW);
		return arrow;
	}
	// arg = eps | ( expression (   ,expression)* )
	Tuple arg() throws SyntaxException 
	{
		//TODO
		// when arg = ( expression (   ,expression)* ), the predict set is "(
		if(t.isKind(LPAREN))
		{

			List<Expression> exprList = new ArrayList<Expression>();;
			consume();							// consume the " ( "
			exprList.add(expression());
			while(t.isKind(COMMA))				// If there is ",", then must followed with an expression
			{
				consume();						// consume " , "
				exprList.add(expression());		// Follow set of expression is "," and " ) ".
			}
			match(RPAREN);						// End of arg has to be a ")".
			Tuple tuple = new Tuple(exprList.get(0).getFirstToken(),exprList);
			return tuple;
		}
		return new Tuple(null,new ArrayList<Expression>());
	}

	/**
	 * Checks whether the current token is the EOF token. If not, a
	 * SyntaxException is thrown.
	 * 
	 * @return
	 * @throws SyntaxException
	 */
	private Token matchEOF() throws SyntaxException 
	{
		if (t.isKind(EOF)) 
		{
			return t;
		}
		throw new SyntaxException("expected EOF");
	}

	/**
	 * Checks if the current token has the given kind. If so, the current token
	 * is consumed and returned. If not, a SyntaxException is thrown.
	 * 
	 * Precondition: kind != EOF
	 * 
	 * @param kind
	 * @return
	 * @throws SyntaxException
	 */
	private Token match(Kind kind) throws SyntaxException 
	{
		if (t.isKind(kind)) 
		{
			return consume();
		}
		String errMsg = "In " + t.getLinePos().toString()+" ," + "'"+ t.getText()+"'"+
			            " is not expected here. Expect " + "'"+kind.getText()+"'.";
		throw new SyntaxException(errMsg);
	}

	/**
	 * Checks if the current token has one of the given kinds. If so, the
	 * current token is consumed and returned. If not, a SyntaxException is
	 * thrown.
	 * 
	 * * Precondition: for all given kinds, kind != EOF
	 * 
	 * @param kinds
	 *            list of kinds, matches any one
	 * @return
	 * @throws SyntaxException
	 */
	private Token match(Kind... kinds) throws SyntaxException 
	{
		// TODO. Optional but handy
		for(int i=0;i<kinds.length;i++)
		{
			if(t.isKind(kinds[i]))		//any one matches will return 
			{
				consume();
				return t;
			}
		}
		String errMsg = "In " + t.getLinePos().toString()+" ," + "'"+ t.getText()+"'"+
			            " is not expected here";
		throw new SyntaxException(errMsg);
		
	}

	/**
	 * Gets the next token and returns the consumed token.
	 * 
	 * Precondition: t.kind != EOF
	 * 
	 * @return
	 * 
	 */
	private Token consume() throws SyntaxException 
	{
		Token tmp = t;
		t = scanner.nextToken();
		return tmp;
	}

	public static void main(String[] str) throws IllegalCharException, IllegalNumberException, SyntaxException,Exception
	{
//		String progname = "emptyProg";
//		String input = "ClassA url f1,file f2\n"+
//                        "{\n"+
//                        "   image i\n"+
//                        "   frame f\n"+
//                        "   f1->i->f->show;  \n"+
//                        "} \n";
//		
//		Scanner scanner = new Scanner(input);
//		scanner.scan();
//		Parser parser = new Parser(scanner);
//		ASTNode program = parser.parse();
//		TypeCheckVisitor v = new TypeCheckVisitor();
//		program.visit(v, null);
//		//show(program);
//		
//		//generate code
//		
//		CodeGenVisitor cv = new CodeGenVisitor(false,true,null);
//		 byte[] bytecode = (byte[]) program.visit(cv, null);
//		// show(bytecode);
//
//		//output the generated bytecode
//		CodeGenUtils.dumpBytecode(bytecode);
//		
//		//write byte code to file 
//		String name = ((Program) program).getName();
//
//		String classFileName = name + ".class";
//		OutputStream output = new FileOutputStream(classFileName);
//		output.write(bytecode);
//		output.close();
//		System.out.println("wrote classfile to " + classFileName);
//		
//		// directly execute bytecode
//		String[] args = {"http://harn.ufl.edu/sites/default/files/summerartcamp2016.jpg", "test.jpg"}; //create command line argument array to initialize params, none in this case
//		//String[] args = {"test2.jpg","test1.jpg"};
//		Runnable instance = CodeGenUtils.getInstance(name, bytecode, args);
//		instance.run();
	}
}
