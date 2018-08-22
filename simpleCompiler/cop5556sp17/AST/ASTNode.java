package cop5556sp17.AST;

import cop5556sp17.Scanner.Token;
import cop5556sp17.AST.Type.*;
public abstract class ASTNode 
{

	protected ASTNode(Token firstToken)
	{
		this.firstToken=firstToken;
	}
	
	final public Token firstToken;

	public Token getFirstToken() 
	{
		return firstToken;
	}
	private TypeName m_type;
    public void SetType(TypeName type)
    {
        m_type = type;
    }
    public TypeName GetType()
    {
        return m_type;
    }
	
	@Override
	public int hashCode() 
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((firstToken == null) ? 0 : firstToken.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) 
	{
		if (this == obj) 
		{
			return true;
		}
		if (obj == null) 
		{
			return false;
		}
		if (!(obj instanceof ASTNode)) 
		{
			return false;
		}
		ASTNode other = (ASTNode) obj;
		if (firstToken == null) 
		{
			if (other.firstToken != null) 
			{
				return false;
			}
		} 
		else if (!firstToken.equals(other.firstToken)) 
		{
			return false;
		}
		return true;
	}

	public abstract Object visit(ASTVisitor v, Object arg) throws Exception;

}

