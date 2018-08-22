package cop5556sp17;



import cop5556sp17.AST.Dec;
import java.util.*;

public class SymbolTable 
{

	private HashMap<String,HashMap<Integer, Dec>> m_identTable;
	private int m_currentScope;
	private Stack<Integer> m_scopeStack;
	//TODO  add fields

	/** 
	 * to be called when block entered
	 */
	public void enterScope()
	{
		//TODO:  IMPLEMENT THIS
		m_currentScope++;
		m_scopeStack.push(m_currentScope);
	}
	
	/**
	 * leaves scope
	 */
	public void leaveScope()
	{
		//TODO:  IMPLEMENT THIS
		if(m_scopeStack.empty())
			return ;
		m_scopeStack.pop();
	}
	
	public boolean insert(String ident, Dec dec)
	{
		//TODO:  IMPLEMENT THIS
		if(!m_identTable.containsKey(ident))								//There is no variable having same name
		{
			HashMap<Integer, Dec> newTable = new HashMap<Integer, Dec>();
			newTable.put(m_scopeStack.peek(), dec);
			m_identTable.put(ident,newTable);	
			//System.out.println(ident + m_scopeStack.peek());						 
			return true;
		}
		else																//There exit variables with same name
		{
			HashMap<Integer, Dec> nameConflictTable = m_identTable.get(ident);	//get all the variables with the same name
			if(!nameConflictTable.containsKey(m_scopeStack.peek()))			//Check whether they have the same scope number
			{
				nameConflictTable.put(m_scopeStack.peek(), dec);			//If not, insert the new variable
				//System.out.println(ident + m_scopeStack.peek());
				return true;
			}
			return false;													//if conflict, return false
		}
	}
	
	public Dec lookup(String ident)
	{
		//TODO:  IMPLEMENT THIS
		int currentScope = m_scopeStack.peek();
		if(!m_identTable.containsKey(ident))
			return null;

		HashMap<Integer, Dec> identTable = m_identTable.get(ident);		//find all variables have same name
		Set<Integer> keys = identTable.keySet();
		
		Dec dec = null;
		int minDistance = 0x7fffffff;
		int distance;
		
			// int scopeNum = it.next();
			// distance = currentScope - scopeNum;
			// if(minDistance >= distance && distance >= 0)			//distance if less than 0, it means variables is out of scope.
			// {
			// 	minDistance = distance;
			// 	dec = identTable.get(scopeNum);
			// 	System.out.println("Get" + dec.getIdent().getText() +""+scopeNum);
			// }
		boolean found = false;
		Iterator<Integer> stackIt = m_scopeStack.iterator();		//traverse stack from top to bottom
		for(int i = m_scopeStack.size() - 1; i >= 0; i--)
		{
			int scopeNum = m_scopeStack.get(i);
			Iterator<Integer> it = keys.iterator();				//traverse the chain with same name
			while(it.hasNext())									//traverse all of them to find the one closest to top of stack
			{	
				if(scopeNum == it.next())
				{
					dec = identTable.get(scopeNum);
					//System.out.println("Get" + dec.getIdent().getText() +""+scopeNum);
				}
			} 
			if(dec != null)
				break;
		}
		return dec;
	}
		
	public SymbolTable() 
	{
		//TODO:  IMPLEMENT THIS
		m_currentScope = 0;
		m_identTable = new HashMap<String,HashMap<Integer, Dec>>();
		m_scopeStack = new Stack<Integer>();
		m_scopeStack.push(0);		//make scope stack not empty
	}


	@Override
	public String toString() 
	{
		//TODO:  IMPLEMENT THIS
		return "";
	}
	
}
