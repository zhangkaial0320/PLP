package cop5556sp17.AST;

import cop5556sp17.Scanner.Token;

public class IdentExpression extends Expression 
{
 	private Dec m_dec;
	public IdentExpression(Token firstToken) 
	{
		super(firstToken);
	}

	@Override
	public String toString() 
	{
		return "IdentExpression [firstToken=" + firstToken + "]";
	}

	@Override
	public Object visit(ASTVisitor v, Object arg) throws Exception 
	{
		return v.visitIdentExpression(this, arg);
	}

	public void SetDec(Dec dec)
	{
		m_dec = dec;
	}
}
