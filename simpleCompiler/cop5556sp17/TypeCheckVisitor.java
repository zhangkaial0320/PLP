package cop5556sp17;

import cop5556sp17.AST.*;

import java.util.ArrayList;

import cop5556sp17.Scanner.Kind;
import cop5556sp17.Scanner.LinePos;
import cop5556sp17.Scanner.Token;
import static cop5556sp17.AST.Type.TypeName.*;
import static cop5556sp17.Scanner.Kind.*;
import cop5556sp17.AST.Type.*;
public class TypeCheckVisitor implements ASTVisitor 
{

	@SuppressWarnings("serial")
	public static class TypeCheckException extends Exception 
	{
		TypeCheckException(String message) 
		{
			super(message);
		}
	}

	SymbolTable symtab = new SymbolTable();

	@Override
	public Object visitBinaryChain(BinaryChain binaryChain, Object arg) throws Exception 
	{
		// TODO Auto-generated method stub
		Chain e0 = binaryChain.getE0();
		Token arrow = binaryChain.getArrow();
		ChainElem e1 = binaryChain.getE1();
		TypeName typeE0 = (TypeName) (e0.visit(this, null));
		TypeName typeE1 = (TypeName) (e1.visit(this, null));
		String str = e0.firstToken.getLinePos().toString() + " Type Error";
		if(typeE0 == URL || typeE0 == FILE)
		{
			if(arrow.isKind(ARROW) && typeE1 == IMAGE )
			{
				binaryChain.SetType(IMAGE);
				return IMAGE;
			}
			throw new TypeCheckException(str);
		}
		else if(typeE0 == FRAME)
		{
			if(e1 instanceof FrameOpChain && arrow.isKind(ARROW))
			{
				Token op = ((FrameOpChain)e1).firstToken;
				if(op.isKind(KW_XLOC,KW_YLOC))
				{
					binaryChain.SetType(INTEGER);
					return INTEGER;
				}
				else if(op.isKind(KW_SHOW,KW_HIDE,KW_MOVE) )
				{
					binaryChain.SetType(FRAME);
					return FRAME;
				}
			}
			throw new TypeCheckException(str);
		}
		else if(typeE0 == IMAGE)
		{
			if(arrow.isKind(ARROW))
			{
				if(e1 instanceof ImageOpChain) //e0 = IMAGE, arrow=ARROW, e1 = ImageOP, op =WIDTH,HEIGHT,SCALE
				{
					Token op = ((ImageOpChain)e1).firstToken;
					if(op.isKind(KW_SCALE))
					{
						binaryChain.SetType(IMAGE);
						return IMAGE;
					}
					else if(op.isKind(OP_WIDTH,OP_HEIGHT))
					{
						binaryChain.SetType(INTEGER);
						return INTEGER;
					}
				}
				else if(e1 instanceof FilterOpChain)
				{
					Token op = ((FilterOpChain)e1).firstToken;
					if(op.isKind(OP_GRAY,OP_BLUR,OP_CONVOLVE))
					{
						binaryChain.SetType(IMAGE);
						return IMAGE;
					}
				}
				else if(e1 instanceof IdentChain && e1.GetType() == IMAGE)
				{
					binaryChain.SetType(IMAGE);
					return IMAGE;
				}
				else if(typeE1 == FRAME)
				{
					binaryChain.SetType(FRAME);
					return FRAME;
				}
				else if(typeE1 == FILE)
				{
					binaryChain.SetType(NONE);
					return NONE;
				}
				throw new TypeCheckException(str);
			}
			else if(arrow.isKind(BARARROW))
			{
				if(e1 instanceof FilterOpChain)
				{
					Token op = ((FilterOpChain)e1).firstToken;
					if(op.isKind(OP_GRAY,OP_BLUR,OP_CONVOLVE))
					{
						binaryChain.SetType(IMAGE);
						return IMAGE;
					}
				}				
			}
			throw new TypeCheckException(str);
		}
		else if(e0.GetType() == INTEGER && (e1 instanceof IdentChain) && e1.GetType() == INTEGER)
		{
			binaryChain.SetType(INTEGER);
			return INTEGER;
		}
		else
		{
			throw new TypeCheckException(str);
		}
	}

	@Override
	public Object visitBinaryExpression(BinaryExpression binaryExpression, Object arg) throws Exception 
	{
		// TODO Auto-generated method stub
		TypeName typeE0 = (TypeName) (binaryExpression.getE0().visit(this,null));
		Token op = binaryExpression.getOp();
		TypeName typeE1 = (TypeName) (binaryExpression.getE1().visit(this,null));
		if(op.isKind(EQUAL,NOTEQUAL))
		{
			if(typeE0 == typeE1)
			{
				binaryExpression.SetType(BOOLEAN);
				return BOOLEAN;
			}
			String str = binaryExpression.getE0().firstToken.getLinePos().toString() + " Type Error";
			throw new TypeCheckException(str);
		}
		if(typeE0 == INTEGER)
		{
			if((op.isKind(PLUS,MINUS,TIMES,DIV,MOD))&&(typeE1 == INTEGER))
			{
				binaryExpression.SetType(INTEGER);
				return INTEGER;
			}
			else if(op.isKind(TIMES)&&(typeE1 == IMAGE))
			{
				binaryExpression.SetType(IMAGE);
				return IMAGE;
			}
			else if((op.isKind(LT,GT,LE,GE,OR,AND))&&(typeE1 == INTEGER))
			{
				binaryExpression.SetType(BOOLEAN);
				return BOOLEAN;
			}
			String str = binaryExpression.getE0().firstToken.getLinePos().toString() + " Type Error";
			throw new TypeCheckException(str);
		}
		else if(typeE0 == IMAGE)
		{
			if((op.isKind(PLUS,MINUS))&&(typeE1 == IMAGE))
			{
				binaryExpression.SetType(IMAGE);
				return IMAGE;
			}
			else if(op.isKind(TIMES,DIV,MOD) && (typeE1 == INTEGER))
			{
				binaryExpression.SetType(IMAGE);
				return IMAGE;
			}
			String str = binaryExpression.getE0().firstToken.getLinePos().toString() + " Type Error";
			throw new TypeCheckException(str);
		}
		else if(typeE0 == BOOLEAN)
		{
			if((op.isKind(LT,GT,LE,GE,OR,AND,MOD))&&(typeE1 == BOOLEAN))
			{
				binaryExpression.SetType(BOOLEAN);
				return BOOLEAN;
			}
			String str = binaryExpression.getE0().firstToken.getLinePos().toString() + " Type Error";
			throw new TypeCheckException(str);
		}
		String str = binaryExpression.getE0().firstToken.getLinePos().toString() + " Type Error";
		throw new TypeCheckException(str);
	}

	@Override
	public Object visitBlock(Block block, Object arg) throws Exception 
	{
		// TODO Auto-generated method stub
		symtab.enterScope();
		ArrayList<Dec> decList = block.getDecs();
		for(int i=0;i<decList.size();i++)
			decList.get(i).visit(this,null);

		ArrayList<Statement> statementList = block.getStatements();
		for(int i=0; i<statementList.size(); i++)
		{
			Statement statement = statementList.get(i);
			statement.visit(this,null);
		}
		symtab.leaveScope();
		return null;
	}
	@Override
	public Object visitDec(Dec declaration, Object arg) throws Exception 
	{
		// TODO Auto-generated method stub
		if(!symtab.insert(declaration.getIdent().getText(),declaration))
		{
			String str = declaration.firstToken.getLinePos()+": " +
						 declaration.getIdent().getText() + " is redefined.\n";
			throw new TypeCheckException(str);
		}
		return null;
	}
	@Override
	public Object visitFilterOpChain(FilterOpChain filterOpChain, Object arg) throws Exception 
	{
		// TODO Auto-generated method stub
		Tuple tuple = filterOpChain.getArg();
		tuple.visit(this,null);
		ArrayList<Expression> exprList = (ArrayList<Expression>) (tuple.getExprList());
 		if( 0 == exprList.size())
 		{
			filterOpChain.SetType(IMAGE);
			return IMAGE;
 		}
		else
		{
			String errMsg = filterOpChain.firstToken.getLinePos().toString() + " "
			                + "filterOpChain should not have arguments";
			throw new TypeCheckException(errMsg);			
		}
	}

	@Override
	public Object visitFrameOpChain(FrameOpChain frameOpChain, Object arg) throws Exception 
	{
		// TODO Auto-generated method stub
		Token op = frameOpChain.firstToken;
		Tuple tuple = frameOpChain.getArg();
		tuple.visit(this,null);
		ArrayList<Expression> exprList = (ArrayList<Expression>) (tuple.getExprList());
		if(op.isKind(KW_SHOW)||op.isKind(KW_HIDE))					//Followings are action rules
		{
			if(0 == exprList.size())
			{
				frameOpChain.SetType(NONE);
				return NONE;
			}
			else
			{
				String errMsg = op.getLinePos().toString() + " :"
			                + op.getText() +" should not have argument";
				throw new TypeCheckException(errMsg);	
			}
		}
		else if(op.isKind(KW_XLOC)||op.isKind(KW_YLOC))
		{
			if(0 == exprList.size())
			{
				frameOpChain.SetType(INTEGER);
				return INTEGER;
			}
			else
			{
				String errMsg = op.getLinePos().toString() + " :"
			                    +op.getText() + " should not have argument";
				throw new TypeCheckException(errMsg);	
			}
		}
		else if(op.isKind(KW_MOVE))
		{
			if(2 == exprList.size())
			{
				frameOpChain.SetType(NONE);
				return NONE;
			}
			else
			{
				String errMsg = op.getLinePos().toString() + " :"
			                + op.getText()+" should have 2 arguments";
				throw new TypeCheckException(errMsg);	
			}
		}
		else
		{
			throw new TypeCheckException(op.getLinePos().toString()+": Parser Error");
		}
	}

	@Override
	public Object visitImageOpChain(ImageOpChain imageOpChain, Object arg) throws Exception 
	{
		// TODO Auto-generated method stub
		Token op = imageOpChain.firstToken;
		Tuple tuple = imageOpChain.getArg();
		tuple.visit(this,null);
		ArrayList<Expression> exprList = (ArrayList<Expression>) (tuple.getExprList());
		if(op.isKind(OP_WIDTH)||op.isKind(OP_HEIGHT))					//Followings are action rules
		{
			if(0 == exprList.size())
			{
				imageOpChain.SetType(INTEGER);
				return INTEGER;
			}
			else
			{
				String errMsg = op.getLinePos().toString() + " :"
			                    + op.getText() +" should not have argument";
				throw new TypeCheckException(errMsg);	
			}
		}
		else if (op.isKind(KW_SCALE))
		{
			if(1 == exprList.size())
			{
				imageOpChain.SetType(IMAGE);
				return IMAGE;
			}
			else
			{
				String errMsg = op.getLinePos().toString() + " :"
			                + op.getText() +" should have 1 arguments";
				throw new TypeCheckException(errMsg);	
			}
		}
		else
		{
			throw new TypeCheckException(op.getLinePos().toString()+": Parser Error");
		}
	}

	@Override
	public Object visitIdentChain(IdentChain identChain, Object arg) throws Exception 
	{
		// TODO Auto-generated method stub

		Dec dec = symtab.lookup(identChain.firstToken.getText());		//lookup whether ident is defined
		if(null == dec)
		{
			String errMsg = identChain.firstToken.getLinePos().toString() + " "
			                + identChain.firstToken.toString()+" is undefined";
			throw new TypeCheckException(errMsg);
		}
		TypeName type = Type.getTypeName(dec.getType());
		identChain.SetType(type);	//get type and assigned to identChain
		return type;
	}

	@Override
	public Object visitIdentExpression(IdentExpression identExpression, Object arg) throws Exception 
	{
		// TODO Auto-generated method stub
		Token ident = identExpression.firstToken;
		Dec dec = symtab.lookup(ident.getText());
		if(null == dec)
		{
			String errMsg = ident.getLinePos().toString() + " "
			                + ident.toString()+" is undefined";
			throw new TypeCheckException(errMsg);
		}
		TypeName type = Type.getTypeName(dec.getType());
		identExpression.SetType(type);
		identExpression.SetDec(dec);
		return type;
	}

	@Override
	public Object visitBooleanLitExpression(BooleanLitExpression booleanLitExpression, Object arg) throws Exception 
	{
		// TODO Auto-generated method stub
		booleanLitExpression.SetType(BOOLEAN);
		return BOOLEAN;
	}
	@Override
	public Object visitIfStatement(IfStatement ifStatement, Object arg) throws Exception 
	{
		// TODO Auto-generated method stub
		//ifStatement.SetType(BOOLEAN);
		TypeName type = (TypeName)(ifStatement.getE().visit(this,null));
		if(type != BOOLEAN)
		{
			String errMsg = ifStatement.firstToken.getLinePos().toString() + 
			                ": if statement must have bool type";
			throw new TypeCheckException(errMsg);		
		}
		ifStatement.getB().visit(this,null);
		return null;
	}

	@Override
	public Object visitIntLitExpression(IntLitExpression intLitExpression, Object arg) throws Exception 
	{
		// TODO Auto-generated method stub
		intLitExpression.SetType(INTEGER);
		return INTEGER;
	}

	@Override
	public Object visitSleepStatement(SleepStatement sleepStatement, Object arg) throws Exception 
	{
		// TODO Auto-generated method stub
		Expression e = sleepStatement.getE();
		TypeName type = (TypeName) (e.visit(this,null));	//Call Expression's visit method, return should be typeName
		if(type !=null && type != INTEGER)
		{
			String errMsg = sleepStatement.firstToken.getLinePos().toString() + 
			                ": the statement type is incorrect, it should be 'INTEGER' type";
			throw new TypeCheckException(errMsg);
		}
		return null;
	}

	@Override
	public Object visitWhileStatement(WhileStatement whileStatement, Object arg) throws Exception 
	{
		// TODO Auto-generated method stub
		TypeName type = (TypeName)(whileStatement.getE().visit(this,null));
		if(type != BOOLEAN)
		{
			String errMsg = whileStatement.firstToken.getLinePos().toString() + 
			                ": whileStatement statement must have bool type";
			throw new TypeCheckException(errMsg);		
		}
		whileStatement.getB().visit(this,null);
		return null;
	}

	@Override
	public Object visitProgram(Program program, Object arg) throws Exception 
	{
		// TODO Auto-generated method stub
		ArrayList<ParamDec> paramList = program.getParams();
		for(int i=0;i<paramList.size();i++)
		{
			paramList.get(i).visit(this,null);
		}
		program.getB().visit(this,null);
		return null;
	}

	@Override
	public Object visitAssignmentStatement(AssignmentStatement assignStatement, Object arg) throws Exception 
	{
		// TODO Auto-generated method stub
		IdentLValue variable = assignStatement.getVar();
		TypeName typeLeft = (TypeName) (variable.visit(this,null));					// visit the ident L-value, may throw Exception

		TypeName type = (TypeName)(assignStatement.getE().visit(this, null));	// visit the expression and return type 
		if(typeLeft != type )
		{
			String errMsg = variable.firstToken.getLinePos().toString() 
			                + ": "+variable.firstToken.getText()+" has different type with expression follow\n";
			throw new TypeCheckException(errMsg);
		}
		return null;
	}

	@Override
	public Object visitIdentLValue(IdentLValue identX, Object arg) throws Exception 
	{
		// TODO Auto-generated method stub
		Dec dec = symtab.lookup(identX.getText());	//lookup the ident in symbol table
		if(null == dec)
		{
			String errMsg = identX.firstToken.getLinePos().toString() + " "
			                + identX.firstToken.toString()+" is undefined";
			throw new TypeCheckException(errMsg);
		}
		identX.SetDec(dec);
		return Type.getTypeName(dec.getType());
	}

	@Override
	public Object visitParamDec(ParamDec paramDec, Object arg) throws Exception 
	{
		// TODO Auto-generated method stub
		if(!symtab.insert(paramDec.getIdent().getText(), paramDec))
		{
			String errMsg = paramDec.getIdent().getText() + "is redefined\n";
			throw new TypeCheckException(errMsg);
		}
		
		return null;
	}

	@Override
	public Object visitConstantExpression(ConstantExpression constantExpression, Object arg) 
	{
		// TODO Auto-generated method stub
		constantExpression.SetType(INTEGER);
		return INTEGER;
	}

	@Override
	public Object visitTuple(Tuple tuple, Object arg) throws Exception 
	{
		// TODO Auto-generated method stub
		ArrayList<Expression> exprList = (ArrayList<Expression>)(tuple.getExprList());
		for(int i=0;i<exprList.size();i++)
		{
			TypeName type =(TypeName) (exprList.get(i).visit(this,null));
			if(type!=INTEGER)
			{
				String str = tuple.firstToken.getLinePos().toString() +": The "+(i+1)
				             +"th argument in the argument list is not integer type\n";
				throw new TypeCheckException(str);
			}
		}
		return null;
	}
}
