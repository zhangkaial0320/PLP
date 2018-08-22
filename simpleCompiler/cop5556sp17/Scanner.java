package cop5556sp17;

import java.util.*;
public class Scanner 
{
    /**
     * Kind enum
     */
    public static enum Kind 
    {
        IDENT("ident"), INT_LIT("int_lit"), KW_INTEGER("integer"), KW_BOOLEAN("boolean"), 
        KW_IMAGE("image"), KW_URL("url"), KW_FILE("file"), KW_FRAME("frame"), 
        KW_WHILE("while"), KW_IF("if"), KW_TRUE("true"), KW_FALSE("false"), 
        SEMI(";"), COMMA(","), LPAREN("("), RPAREN(")"), LBRACE("{"), 
        RBRACE("}"), ARROW("->"), BARARROW("|->"), OR("|"), AND("&"), 
        EQUAL("=="), NOTEQUAL("!="), LT("<"), GT(">"), LE("<="), GE(">="), 
        PLUS("+"), MINUS("-"), TIMES("*"), DIV("/"), MOD("%"), NOT("!"), 
        ASSIGN("<-"), OP_BLUR("blur"), OP_GRAY("gray"), OP_CONVOLVE("convolve"), 
        KW_SCREENHEIGHT("screenheight"), KW_SCREENWIDTH("screenwidth"), 
        OP_WIDTH("width"), OP_HEIGHT("height"), KW_XLOC("xloc"), KW_YLOC("yloc"), 
        KW_HIDE("hide"), KW_SHOW("show"), KW_MOVE("move"), OP_SLEEP("sleep"), 
        KW_SCALE("scale"), EOF("eof"), PART_EQ("=");

        Kind(String text) 
        {
            this.text = text;
        }

        final String text;

        String getText() 
        {
            return text;
        }
    }
    private static enum States
    {
        START,
        ZEROS,
        NUMBER,
        SEPERATOR,
        OPERATOR,
        STRING,
        COMMENT,
        EPSILON,
        OTHERWISE,
    }
/**
 * Thrown by Scanner when an illegal character is encountered
 */
    @SuppressWarnings("serial")
    public static class IllegalCharException extends Exception 
    {
        public IllegalCharException(String message) 
        {
            super(message);
        }
    }
    
    /**
     * Thrown by Scanner when an int literal is not a value that can be represented by an int.
     */
    @SuppressWarnings("serial")
    public static class IllegalNumberException extends Exception 
    {
        public IllegalNumberException(String message)
        {
            super(message);
        }
    }
    

    /**
     * Holds the line and position in the line of a token.
     */
    static class LinePos 
    {
        public final int line;
        public final int posInLine;
        
        public LinePos(int line, int posInLine) 
        {
            super();
            this.line = line;
            this.posInLine = posInLine;
        }

        @Override
        public String toString()
        {
            return "LinePos [line=" + line + ", posInLine=" + posInLine + "]";
        }
    }
        

    

    public class Token 
    {
        public final Kind kind;
        public final int pos;               //position in input array
        public final int length;  
        private int m_lineNo;
        private int m_colNo;
        
        public String toString()
        {
            return chars.substring(pos, pos+length);
        }
        public String getText() 
        {
            //TODO IMPLEMENT THIS
            return chars.substring(pos, pos+length);
            //return null;
        }
        
        //returns a LinePos object representing the line and column of this Token
        LinePos getLinePos()
        {
            //TODO IMPLEMENT THIS
            LinePos linePos = new LinePos(this.m_lineNo,this.m_colNo);
            return linePos;
        }

        Token(Kind kind, int pos, int length) 
        {
            this.kind = kind;
            this.pos = pos;
            this.length = length;
            this.m_lineNo = m_currentLineNo;
            this.m_colNo = m_currentColNo;
            //System.out.println(getText()+" ---------------- "+kind.getText() + " "+ getLinePos().toString());
        }

        private void SetLinepos(int lineNo, int colNo)
        {
            m_lineNo = lineNo;
            m_colNo = colNo;
        }
        /** 
         * Precondition:  kind = Kind.INT_LIT,  the text can be represented with a Java int.
         * Note that the validity of the input should have been checked when the Token was created.
         * So the exception should never be thrown.
         * 
         * @return  int value of this token, which should represent an INT_LIT
         * @throws NumberFormatException
         */
        public int intVal() throws NumberFormatException
        {
            //TODO IMPLEMENT THIS
            if(this.kind == Kind.INT_LIT)
            {
                String intLiteral = getText();
                int result = Integer.parseInt(intLiteral);
                return result;
            }
            return 0;
        }
        public boolean isKind(Kind... kind)
        {
            for(int i=0;i<kind.length;i++)
            {
                if(this.kind == kind[i])
                    return true;
            }
            return false;
        }

        @Override
        public int hashCode() 
        {
           final int prime = 31;
           int result = 1;
           result = prime * result + getOuterType().hashCode();
           result = prime * result + ((kind == null) ? 0 : kind.hashCode());
           result = prime * result + length;
           result = prime * result + pos;
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
            if (!(obj instanceof Token)) 
            {
                return false;
            }
            Token other = (Token) obj;
            if (!getOuterType().equals(other.getOuterType())) 
            {
                return false;
            }
            if (kind != other.kind) 
            {
                return false;
            }
            if (length != other.length) 
            {
                return false;
            }
            if (pos != other.pos) 
            {
                return false;
            }
            return true;
        }

         

        private Scanner getOuterType() 
        {
           return Scanner.this;
        }
        
    }

     
    final ArrayList<Token> tokens;
    final String chars;

    final HashMap<String,Kind>  m_operatorSet;
    final HashMap<String,Kind>  m_separatorSet;
    final HashMap<String,Kind>  m_reservedSet;

    int inputLength;
    int tokenNum;
    int m_currentLineNo = 0;
    int m_currentColNo = 0;

    Scanner(String chars) 
    {
        this.chars = chars;
        tokens = new ArrayList<Token>();
        inputLength = chars.length();

        List<Kind>  operaterList = Arrays.asList(Kind.OR, Kind.AND, Kind.EQUAL, Kind.NOTEQUAL, Kind.LT,Kind.GT,Kind.LE,Kind.GE,
                                                 Kind.TIMES,Kind.DIV,Kind.PLUS,Kind.MINUS, Kind.MOD,Kind.NOT,Kind.ARROW,
                                                 Kind.BARARROW,Kind.ASSIGN,Kind.PART_EQ);

        List<Kind>  separatorList = Arrays.asList(Kind.SEMI,Kind.COMMA,Kind.LPAREN,Kind.RPAREN,Kind.LBRACE,Kind.RBRACE);

        List<Kind>  keyword = Arrays.asList(Kind.KW_INTEGER, Kind.KW_BOOLEAN, Kind.KW_IMAGE, Kind.KW_URL, Kind.KW_FILE,Kind.KW_FRAME,
                                                Kind.KW_WHILE,Kind.KW_IF,Kind.OP_SLEEP,Kind.KW_SCREENHEIGHT,Kind.KW_SCREENWIDTH,
                                                Kind.OP_GRAY, Kind.OP_CONVOLVE, Kind.OP_BLUR, Kind.KW_SCALE,
                                                Kind.OP_WIDTH, Kind.OP_HEIGHT, Kind.KW_XLOC, Kind.KW_YLOC, Kind.KW_HIDE, Kind.KW_SHOW, 
                                                Kind.KW_MOVE, Kind.KW_TRUE,Kind.KW_FALSE);
        //Initialize Operator Set
        m_operatorSet = new HashMap<String,Kind>();
        for(int i=0;i<operaterList.size();i++)
        {
            m_operatorSet.put(operaterList.get(i).getText(), operaterList.get(i));
        }
        //Initialize Separator Set
        m_separatorSet = new HashMap<String,Kind>();
        for(int i=0;i<separatorList.size();i++)
        {
            m_separatorSet.put(separatorList.get(i).getText(), separatorList.get(i));
        }
        //Initialize Key word Set
        m_reservedSet = new HashMap<String,Kind>();
        for(int i=0;i<keyword.size();i++)
        {
            m_reservedSet.put(keyword.get(i).getText(), keyword.get(i));
        }
    }


    /**
     * Initializes Scanner object by traversing chars and adding tokens to tokens list.
     * 
     * @return this scanner
     * @throws IllegalCharException
     * @throws IllegalNumberException
     */
    public Scanner scan() throws IllegalCharException, IllegalNumberException 
    {
        int pos = 0; 
        //TODO IMPLEMENT THIS!!!!
        StateMachine();
        pos = this.chars.length();
        tokens.add(new Token(Kind.EOF,pos,0));
        return this;  
    }

    private void StateMachine() throws IllegalCharException, IllegalNumberException
    {
        int[]   currentPos = new int[1];
        currentPos[0]=0;
        States  currentState = States.START;
        
        while(currentPos[0]<inputLength)
        {
            switch(currentState)
            {
                case START:
                    currentState = StartStateTransfer(currentPos);
                    break;
                case ZEROS:
                    currentState = ZeroStateTransfer(currentPos);
                    break;
                case NUMBER:
                    currentState = NumberStateTransfer(currentPos);
                    break;
                case SEPERATOR:
                    currentState = SeparatorStateTransfer(currentPos);
                    break;
                case OPERATOR:
                    currentState = OperatorStateTransfer(currentPos);
                    break;
                case STRING:
                    currentState = StringStateTransfer(currentPos);
                    break;
                case COMMENT:
                    break;
                case EPSILON:
                    break;
                case OTHERWISE:
                    LinePos linepos = new LinePos(m_currentLineNo,m_currentColNo); 
                    String errorMsg = linepos.toString() + "token: \'"+this.chars.substring(currentPos[0],currentPos[0]+1)+
                                      "\' is illegal";
                    currentPos[0]++;
                    m_currentColNo++;
                    currentState = States.START;
                    throw new IllegalCharException(errorMsg);

                    //break;
                default:
                    break;
            }
        }
    }
    private States StartStateTransfer(int[] pos)
    {
        char symbol = this.chars.charAt(pos[0]);
        String str = Character.toString(symbol);
        //white space, still loops in start state
        if(symbol=='\n'||symbol==' '||symbol=='\t'||symbol=='\r')   
        {
            pos[0]++;
            if(symbol == '\n'||symbol=='\r')    //change the line NO.
            {
                m_currentLineNo++;
                m_currentColNo=0;
                if(pos[0]<this.inputLength)
                {
                    if(this.chars.substring(pos[0]-1, pos[0]+1).equals("\r\n"))
                        pos[0]++;
                }
            }
            else                                //line NO. doesn't change, so change the column NO.
            {
                m_currentColNo++;
            }
            return States.START;
        }
        else if(('a'<=symbol&&'z'>=symbol) || ('A'<=symbol&&'Z'>=symbol) || ('$'==symbol) || ('_'==symbol) )
        {
            return States.STRING;
        }
        else if('0'==symbol)
        {
            return States.ZEROS;
        }
        else if(('1'<=symbol) && ('9'>=symbol) )
        {
            return States.NUMBER;
        }
        else if(m_operatorSet.containsKey(str))
        {
            return States.OPERATOR;
        }
        else if(m_separatorSet.containsKey(str))
        {
            return States.SEPERATOR;
        }
        else
        {
            return States.OTHERWISE;
        }
    }

    private States ZeroStateTransfer(int[] pos)
    {
        tokens.add(new Token(Kind.INT_LIT,pos[0],1));
        pos[0]++;
        m_currentColNo++;
        return States.START;
    }

    private States NumberStateTransfer(int[] pos) throws IllegalNumberException
    {
        int nextPos=pos[0]+1;
        
        // search next symbols until the number is finished
        for(;nextPos<this.chars.length();nextPos++)
        {
            char nextSymbol = this.chars.charAt(nextPos);
            // if the next symbol is not a digit, then stop searching
            if(!(('0'<=nextSymbol) && ('9'>=nextSymbol)) )
            {
                break;
            }
        }
        String intLiterl = this.chars.substring(pos[0],nextPos);
        try
        {
            int result = Integer.parseInt(intLiterl);
        }
        catch(NumberFormatException e)
        {
            throw new IllegalNumberException("wrong number" + intLiterl);
        }


        //Add a new token, then return to the start state.
        tokens.add(new Token(Kind.INT_LIT,pos[0],nextPos-pos[0]));
        //update new position and column number
        m_currentColNo+= (nextPos-pos[0]);
        pos[0]=nextPos;
        return States.START;
    }

    private States SeparatorStateTransfer(int[] pos)
    {
        String symbol = this.chars.substring(pos[0], pos[0]+1);
        tokens.add(new Token(m_separatorSet.get(symbol),pos[0],1));
        pos[0]+=1;
        m_currentColNo++;
        return States.START;
    }
    private States OperatorStateTransfer(int[] pos)
    {
        char symbol = this.chars.charAt(pos[0]);
        String symbolStr = this.chars.substring(pos[0], pos[0]+1);
        if(pos[0]+1 >= inputLength)     //this is the last character
        {
            if(symbol == '=')
            {
                return States.OTHERWISE;
            }
            tokens.add(new Token(m_operatorSet.get(symbolStr),pos[0]++,1));
            m_currentColNo++;
            return States.START;
        }
        switch(symbol)
        {
            case '!':
            case '<':
            case '>':
                if( this.chars.charAt(pos[0]+1) == '=' || (symbol=='<' && this.chars.charAt(pos[0]+1) == '-'))
                {
                    String str = this.chars.substring(pos[0],pos[0]+2);
                    tokens.add(new Token(m_operatorSet.get(str),pos[0],2));
                    pos[0]+=2;
                    m_currentColNo += 2;
                }
                else
                {
                    tokens.add(new Token(m_operatorSet.get(symbolStr),pos[0]++,1));
                    m_currentColNo += 1;
                }
                break;
            case '-':
                if( this.chars.charAt(pos[0]+1) == '>')
                {
                    String str = this.chars.substring(pos[0],pos[0]+2);
                    tokens.add(new Token(m_operatorSet.get(str),pos[0],2));
                    pos[0]+=2;
                    m_currentColNo +=2;
                }
                else
                {
                    tokens.add(new Token(m_operatorSet.get(symbolStr),pos[0]++,1));
                    m_currentColNo += 1;
                }
                break;
            case '=':
                if( this.chars.charAt(pos[0]+1) == '=')
                {
                    String str = this.chars.substring(pos[0],pos[0]+2);
                    tokens.add(new Token(m_operatorSet.get(str),pos[0],2));
                    pos[0]+=2;
                    m_currentColNo += 2;
                }
                else
                {
                    return States.OTHERWISE;
                }
                break;
            case '|':
                if((pos[0]+2<inputLength)&& (this.chars.substring(pos[0]+1,pos[0]+3).equals("->")))
                {
                    String str = this.chars.substring(pos[0],pos[0]+3);
                    tokens.add(new Token(m_operatorSet.get(str),pos[0],3));
                    pos[0]+=3;
                    m_currentColNo += 3;
                }
                else
                    tokens.add(new Token(m_operatorSet.get(symbolStr),pos[0]++,1));
                break;
            case '/':   //special case for comment.....  Later on
                    if(this.chars.charAt(pos[0]+1) == '*' )
                    {
                        if(EatupComment(pos,pos[0]+2))      //if this is comment, then eat up all the strings
                            return States.START;
                        else                                //else means this is not comment, just "/*", so add '/' into tokens
                        {
                            tokens.add(new Token(m_operatorSet.get(symbolStr),pos[0]++,1));
                            m_currentColNo++;
                        }
                    }
                    else
                    {
                        tokens.add(new Token(m_operatorSet.get(symbolStr),pos[0]++,1));
                        m_currentColNo++;
                    }
                break;
            default:
                tokens.add(new Token(m_operatorSet.get(symbolStr),pos[0]++,1));
                m_currentColNo++;
                break;
        }
        return States.START;
    }
    private States StringStateTransfer(int[] pos)
    {
        int nextPos = pos[0];
        char symbol;
        if((nextPos+1)<this.chars.length())
        {
            for(nextPos = pos[0]+1; nextPos<this.chars.length(); nextPos++)
            {
                symbol = this.chars.charAt(nextPos);
                if(('a'<=symbol&&'z'>=symbol) ||
                   ('A'<=symbol&&'Z'>=symbol) || 
                   ('$'==symbol)              || 
                   ('_'==symbol)              ||
                   ('0'<=symbol&&'9'>=symbol))
                {
                    continue;
                }
                else
                    break;
            }
            nextPos--;
        }
        String str = this.chars.substring(pos[0],nextPos+1);
        if(m_reservedSet.containsKey(str))
        {
            tokens.add(new Token(m_reservedSet.get(str),pos[0],nextPos+1-pos[0]));
        }
        else
        {
            tokens.add(new Token(Kind.IDENT, pos[0], nextPos+1-pos[0]));
        }
        m_currentColNo += (nextPos+1-pos[0]);
        pos[0] = nextPos+1;
        return States.START;
    }
    public boolean EatupComment(int[] pos, int currentPos)
    {
        String subStr = this.chars.substring(currentPos);
        int index = subStr.indexOf("*/");
        if(-1 != index)
        {
            m_currentColNo+= currentPos+2+index - pos[0];
            pos[0] = currentPos+2+index;
            //return true;
        }
        else
        {
            pos[0] = this.inputLength;
            //return false;
        }
        return true;
    }


    /*
     * Return the next token in the token list and update the state so that
     * the next call will return the Token..  
     */
    public Token nextToken() 
    {
        if (tokenNum >= tokens.size())
            return null;
        return tokens.get(tokenNum++);
    }
    
    /*
     * Return the next token in the token list without updating the state.
     * (So the following call to next will return the same token.)
     */
    public Token peek()
    {
        if (tokenNum >= tokens.size())
            return null;
        return tokens.get(tokenNum);      
    }

    

    /**
     * Returns a LinePos object containing the line and position in line of the 
     * given token.  
     * 
     * Line numbers start counting at 0
     * 
     * @param t
     * @return
     */
    public LinePos getLinePos(Token t)
    {
        //TODO IMPLEMENT THIS
        return t.getLinePos();
        //return null;
    }
}


