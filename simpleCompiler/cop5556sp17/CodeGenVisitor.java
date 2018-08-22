package cop5556sp17;

import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.util.*;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.util.TraceClassVisitor;

import cop5556sp17.Scanner.Kind;
import cop5556sp17.Scanner.Token;
import cop5556sp17.AST.*;



import static cop5556sp17.AST.Type.*;
import static cop5556sp17.Scanner.Kind.*;

public class CodeGenVisitor implements ASTVisitor, Opcodes 
{

	/**
	 * @param DEVEL
	 *            used as parameter to genPrint and genPrintTOS
	 * @param GRADE
	 *            used as parameter to genPrint and genPrintTOS
	 * @param sourceFileName
	 *            name of source file, may be null.
	 */
	public CodeGenVisitor(boolean DEVEL, boolean GRADE, String sourceFileName) 
	{
		super();
		this.DEVEL = DEVEL;
		this.GRADE = GRADE;
		this.sourceFileName = sourceFileName;
		List<Kind> binaryKind = Arrays.asList(PLUS,MINUS,TIMES,DIV,MOD,AND,OR,
												LT,GT,LE,GE,EQUAL,NOTEQUAL);
		List<Integer> binaryOp = Arrays.asList(IADD,ISUB,IMUL,IDIV,IREM,IAND,IOR,
												IF_ICMPLT,IF_ICMPGT,IF_ICMPLE,IF_ICMPGE,IF_ICMPEQ,IF_ICMPNE);
		List<String> binarFuncName = Arrays.asList("add","sub","mul","div","mod");
		List<String> binarFuncDesc = Arrays.asList(PLPRuntimeImageOps.addSig,PLPRuntimeImageOps.subSig,
												   PLPRuntimeImageOps.mulSig, PLPRuntimeImageOps.divSig,
												   PLPRuntimeImageOps.modSig);
		for(int i=0; i< binaryKind.size();i++)
		{
			m_opBinayCodeTable.put(binaryKind.get(i), binaryOp.get(i));
		}
		for(int i=0;i<5;i++)
		{
			m_opBinayFuncName.put(binaryKind.get(i), binarFuncName.get(i));
			m_opBinayFuncDesc.put(binaryKind.get(i), binarFuncDesc.get(i));
		}
	}

	ClassWriter cw;
	String className;
	String classDesc;
	String sourceFileName;

	Hashtable<Kind,Integer> m_opBinayCodeTable = new Hashtable<Kind,Integer>();
	Hashtable<Kind,String> m_opBinayFuncName = new Hashtable<Kind,String>();
	Hashtable<Kind,String> m_opBinayFuncDesc = new Hashtable<Kind,String>();
	MethodVisitor mv; // visitor of method currently under construction

	SymbolTable m_symtab = new SymbolTable();
	int 		m_slotNum = 0;
	ArrayList<Dec> m_localDecList;
	/** Indicates whether genPrint and genPrintTOS should generate code. */
	final boolean DEVEL;
	final boolean GRADE;

	@Override
	public Object visitProgram(Program program, Object arg) throws Exception 
	{
		cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		className = program.getName();					//First token of program
		classDesc = "L" + className + ";";				// "LclassName"
		String sourceFileName = (String) arg;			
		cw.visit(52, ACC_PUBLIC + ACC_SUPER, className, null, "java/lang/Object",
				new String[] { "java/lang/Runnable" });	//only one interface ?
		cw.visitSource(sourceFileName, null);

		// generate constructor code
		// get a MethodVisitor
		mv = cw.visitMethod(ACC_PUBLIC, "<init>", "([Ljava/lang/String;)V", null,	//new a MethodWriter
				null);
		mv.visitCode();								//empty 
		// Create label at start of code
		Label constructorStart = new Label();		//don't care
		mv.visitLabel(constructorStart);
		// this is for convenience during development--you can see that the code
		// is doing something.
		CodeGenUtils.genPrint(DEVEL, mv, "\nentering <init>");
		// generate code to call superclass constructor
		mv.visitVarInsn(ALOAD, 0);						//load a object (this)
		mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);	//call this's constructor
		// visit parameter decs to add each as field to the class
		// pass in mv so decs can add their initialization code to the
		// constructor.
		ArrayList<ParamDec> params = program.getParams();		//visit parameter list
		for (int i=0;i<params.size();i++)
        {		
			params.get(i).visit(this, i);
		}
        mv.visitInsn(RETURN);									//add a return instruction
		// create label at end of code
		Label constructorEnd = new Label();
		mv.visitLabel(constructorEnd);
		// finish up by visiting local vars of constructor
		// the fourth and fifth arguments are the region of code where the local
		// variable is defined as represented by the labels we inserted.
		mv.visitLocalVariable("this", classDesc, null, constructorStart, constructorEnd, 0);				//program name
		mv.visitLocalVariable("args", "[Ljava/lang/String;", null, constructorStart, constructorEnd, 1);	//parameters
		// indicates the max stack size for the method.
		// because we used the COMPUTE_FRAMES parameter in the classwriter
		// constructor, asm
		// will do this for us. The parameters to visitMaxs don't matter, but
		// the method must
		// be called.
		mv.visitMaxs(1, 1);
		// finish up code generation for this method.
		mv.visitEnd();
		// end of constructor

		// create main method which does the following
		// 1. instantiate an instance of the class being generated, passing the
		// String[] with command line arguments
		// 2. invoke the run method.
		mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null,	//byte code for main(String[] args)
				null);
		mv.visitCode();
		Label mainStart = new Label();			//start lable
		mv.visitLabel(mainStart);
		// this is for convenience during development--you can see that the code
		// is doing something.
		CodeGenUtils.genPrint(DEVEL, mv, "\nentering main");
		mv.visitTypeInsn(NEW, className);	
		mv.visitInsn(DUP);				//since invoke special will comsume one instance
		mv.visitVarInsn(ALOAD, 0);		// args
		mv.visitMethodInsn(INVOKESPECIAL, className, "<init>", "([Ljava/lang/String;)V", false);// new Name(args)
		mv.visitMethodInsn(INVOKEVIRTUAL, className, "run", "()V", false);
		mv.visitInsn(RETURN);
		Label mainEnd = new Label();
		mv.visitLabel(mainEnd);
		mv.visitLocalVariable("args", "[Ljava/lang/String;", null, mainStart, mainEnd, 0);
		mv.visitLocalVariable("instance", classDesc, null, mainStart, mainEnd, 1);
		mv.visitMaxs(1, 1);
		mv.visitEnd();

		// create run method
		mv = cw.visitMethod(ACC_PUBLIC, "run", "()V", null, null);
		mv.visitCode();
		m_slotNum = 1;
		m_localDecList = new ArrayList<Dec>();
		Label startRun = new Label();
		mv.visitLabel(startRun);
		CodeGenUtils.genPrint(DEVEL, mv, "\nentering run");
		program.getB().visit(this, null);
		mv.visitInsn(RETURN);
		Label endRun = new Label();
		mv.visitLabel(endRun);

		mv.visitLocalVariable("this", classDesc, null, startRun, endRun, 0);
		//TODO  visit the local variables

		for(int i=0;i<m_localDecList.size();i++)
		{
			TypeName type = Type.getTypeName(m_localDecList.get(i).getType());
			mv.visitLocalVariable(m_localDecList.get(i).getIdent().getText(), type.getJVMTypeDesc(), null,
				                  startRun, endRun, m_localDecList.get(i).GetSlotNum());
		}

		mv.visitMaxs(2, 2);
		mv.visitEnd(); // end of run method
		
		
		cw.visitEnd();//end of class
		
		//generate classfile and return it
		return cw.toByteArray();
	}

	@Override
	public Object visitAssignmentStatement(AssignmentStatement assignStatement, Object arg) throws Exception 
	{
		assignStatement.getE().visit(this, arg);
		CodeGenUtils.genPrint(DEVEL, mv, "\nassignment: " + assignStatement.var.getText() + "=");

		CodeGenUtils.genPrintTOS(GRADE, mv, assignStatement.getE().GetType());
		assignStatement.getVar().visit(this, arg);
		return null;
	}

	//Some problems here
	@Override
	public Object visitBinaryChain(BinaryChain binaryChain, Object arg) throws Exception 
	{
		//assert false : "not yet implemented";
		binaryChain.getE0().visit(this, true);		//true means the chain is left

		binaryChain.getE1().visit(this, false);	//false means the chain is right


		return null;
	}
	private void InsertLogicExpression(int opCode)
	{
		Label hit = new Label();
		Label after = new Label();
		mv.visitJumpInsn(opCode,hit);
		mv.visitLdcInsn(0);
		mv.visitJumpInsn(Opcodes.GOTO,after);
		mv.visitLabel(hit);
		mv.visitLdcInsn(1);
		mv.visitLabel(after);
	}
	@Override
	public Object visitBinaryExpression(BinaryExpression binaryExpression, Object arg) throws Exception 
	{
      	//TODO  Implement this
		binaryExpression.getE0().visit(this,null);		//result on the second position
		Token op = binaryExpression.getOp();
		binaryExpression.getE1().visit(this,null);		//result on the first position
			
		switch(op.kind)
		{
			case PLUS:case MINUS:case TIMES:case DIV:
			case AND:case OR:case MOD:
				if((binaryExpression.getE0().GetType() == TypeName.INTEGER && binaryExpression.getE1().GetType()==TypeName.INTEGER) ||
				   (binaryExpression.getE0().GetType() == TypeName.BOOLEAN && binaryExpression.getE1().GetType()==TypeName.BOOLEAN)	)
					mv.visitInsn(m_opBinayCodeTable.get(op.kind));
				else
				{
					if(binaryExpression.getE1().GetType() == TypeName.IMAGE) //since +-*/ image function, the image on the left param
						mv.visitInsn(Opcodes.SWAP);

					mv.visitMethodInsn(Opcodes.INVOKESTATIC,PLPRuntimeImageOps.JVMName, m_opBinayFuncName.get(op.kind),
									   m_opBinayFuncDesc.get(op.kind),false);
				}
				break;
			case LT:case GT:case LE:case GE:case EQUAL:
			case NOTEQUAL:
				InsertLogicExpression(m_opBinayCodeTable.get(op.kind));
				break;
			default:
				assert false : "not yet implemented";
				break;
		}
		return null;
	}

	@Override
	public Object visitBlock(Block block, Object arg) throws Exception 
	{
		//TODO  Implement this
		m_symtab.enterScope();

		ArrayList<Dec> decList = block.getDecs();

		for(int i=0;i<decList.size();i++)
		{
			decList.get(i).visit(this, m_slotNum++);	//visitDec();
			m_localDecList.add(decList.get(i));			//add all variables into localDecList
		}

		ArrayList<Statement> statementList = block.getStatements();
		for(int i=0; i<statementList.size(); i++)
		{
			Statement statement = statementList.get(i);
			statement.visit(this,null);
			if(statement instanceof BinaryChain)
			{
				mv.visitInsn(Opcodes.POP);
			}
		}

		m_symtab.leaveScope();
		return null;
	}

	@Override
	public Object visitBooleanLitExpression(BooleanLitExpression booleanLitExpression, Object arg) throws Exception 
	{
		//TODO Implement this
		mv.visitLdcInsn(booleanLitExpression.getValue());		//load bool const on top of stack
		return 1;												// 1 value is on TOS
	}

	@Override
	public Object visitConstantExpression(ConstantExpression constantExpression, Object arg) 
	{
		Token op = constantExpression.firstToken;
		String funcName = op.isKind(KW_SCREENWIDTH)? "getScreenWidth":"getScreenHeight";
		mv.visitMethodInsn(Opcodes.INVOKESTATIC, PLPRuntimeFrame.JVMClassName,funcName,"()I", false);
		return null;											// 1 value is on TOS
	}

	@Override
	public Object visitDec(Dec declaration, Object arg) throws Exception 
	{
		//TODO Implement this
		Integer slot = (Integer) arg;										//set slot number
		declaration.SetSlotNum(slot);

		TypeName type = Type.getTypeName( declaration.getType());			//set type
		declaration.SetType(type);

		m_symtab.insert(declaration.getIdent().getText(),declaration);		//insert into symbol table
		if(type == TypeName.IMAGE || type == TypeName.FRAME)
		{
			mv.visitInsn(Opcodes.ACONST_NULL);
			mv.visitVarInsn(Opcodes.ASTORE, declaration.GetSlotNum());
		}
		return null;
	}

	//After visit FilterOpChain, a image will be on TOS
	@Override
	public Object visitFilterOpChain(FilterOpChain filterOpChain, Object arg) throws Exception 
	{
		Kind op = filterOpChain.GetOp();
		mv.visitInsn(Opcodes.ACONST_NULL);
		switch(op)
		{
			case OP_BLUR:
				mv.visitMethodInsn(Opcodes.INVOKESTATIC, PLPRuntimeFilterOps.JVMName, "blurOp", PLPRuntimeFilterOps.opSig,false);
				break;
			case OP_GRAY:
				mv.visitMethodInsn(Opcodes.INVOKESTATIC, PLPRuntimeFilterOps.JVMName, "grayOp", PLPRuntimeFilterOps.opSig,false);
				break;
			case OP_CONVOLVE:
				mv.visitMethodInsn(Opcodes.INVOKESTATIC, PLPRuntimeFilterOps.JVMName, "convolveOp", PLPRuntimeFilterOps.opSig,false);
				break;
			default:
				assert false : "not yet implemented"; 
				break;
		}
		return 1;						// 1 value is on the TOS
	}

	//After visit ImageOpChain, an integer or image will be on TOS
	@Override
	public Object visitImageOpChain(ImageOpChain imageOpChain, Object arg) throws Exception 
	{
		//BufferedImage is on the TOS
		Token op = imageOpChain.firstToken;
		Tuple tuple = imageOpChain.getArg();
		tuple.visit(this,null);
		if(op.isKind(OP_WIDTH)||op.isKind(OP_HEIGHT))					//Followings are action rules
		{
			// integer will be on the TOS
			String funcName = op.isKind(OP_WIDTH)? "getWidth" : "getHeight";
			String desc = op.isKind(OP_WIDTH)? PLPRuntimeImageOps.getWidthSig : PLPRuntimeImageOps.getHeightSig;
			//call BufferedImage.getWidth() or getHeight, which is normal member function
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,PLPRuntimeImageIO.BufferedImageClassName,funcName,desc,false);
		}
		else if (op.isKind(KW_SCALE))
		{
			// image will be on the TOS
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, PLPRuntimeImageOps.JVMName, "scale", PLPRuntimeImageOps.scaleSig,false);
		}
		else
		{
			assert false : "not yet implemented";
		}
		return 1;						//1 value is on the TOS
	}

	//After visit FrameOpChain, a Frame will be on TOS
	@Override
	public Object visitFrameOpChain(FrameOpChain frameOpChain, Object arg) throws Exception 
	{
		Token op = frameOpChain.firstToken;
		Tuple tuple = frameOpChain.getArg();
		tuple.visit(this,null);
		if(op.isKind(KW_SHOW)||op.isKind(KW_HIDE))					//Followings are action rules
		{
			String funcName = op.isKind(KW_SHOW)? "showImage" : "hideImage";
			String desc = op.isKind(KW_SHOW)? PLPRuntimeFrame.showImageDesc : PLPRuntimeFrame.hideImageDesc;
			//call show or hide, these functions will put frame on the TOS
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, PLPRuntimeFrame.JVMClassName, funcName, desc,false);
		}
		else if(op.isKind(KW_XLOC)||op.isKind(KW_YLOC))
		{
			String funcName = op.isKind(KW_XLOC)? "getXVal" : "getYVal";
			String desc = op.isKind(KW_XLOC)? PLPRuntimeFrame.getXValDesc : PLPRuntimeFrame.getYValDesc;
			//call show or hide, these functions will put integer on the TOS
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, PLPRuntimeFrame.JVMClassName, funcName, desc,false);
		}
		else if(op.isKind(KW_MOVE))
		{
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, PLPRuntimeFrame.JVMClassName, "moveFrame", PLPRuntimeFrame.moveFrameDesc,false);
		}
		else
		{
			assert false : "not yet implemented";
		}
		return 1;						//1 value is on the TOS
	}
	private void LoadVariableOnStack(TypeName type, Dec dec)
	{
		int opCode = (type == TypeName.INTEGER)? Opcodes.ILOAD: Opcodes.ALOAD;
		if(dec.GetSlotNum() == -1)
		{
			mv.visitVarInsn(Opcodes.ALOAD, 0);	// load this
			mv.visitFieldInsn(Opcodes.GETFIELD, className, dec.getIdent().getText(), dec.GetType().getJVMTypeDesc());		
		}
		else
		{
			mv.visitVarInsn(opCode, dec.GetSlotNum());
		}
	}
	private void StoreVariableFromStack(TypeName type, Dec dec)
	{
		int opCode = (type == TypeName.INTEGER)? Opcodes.ISTORE: Opcodes.ASTORE;
		if(dec.GetSlotNum() == -1)
		{
			mv.visitVarInsn(Opcodes.ALOAD, 0);	// load this
			mv.visitInsn(Opcodes.SWAP);
			mv.visitFieldInsn(Opcodes.PUTFIELD, className, dec.getIdent().getText(), dec.GetType().getJVMTypeDesc());		
		}
		else
		{
			mv.visitVarInsn(opCode, dec.GetSlotNum());
		}
	}
	@Override
	public Object visitIdentChain(IdentChain identChain, Object arg) throws Exception 
	{
		//assert false : "not yet implemented";
		Dec dec = m_symtab.lookup(identChain.firstToken.getText());		//lookup whether ident is defined
		TypeName type = identChain.GetType();
		Boolean isLeft = (Boolean) arg;
		if(isLeft == true)
		{
			LoadVariableOnStack(type, dec);
			if(type == TypeName.URL)
				mv.visitMethodInsn(Opcodes.INVOKESTATIC, PLPRuntimeImageIO.className, "readFromURL", PLPRuntimeImageIO.readFromURLSig,false);
			if(type == TypeName.FILE)
				mv.visitMethodInsn(Opcodes.INVOKESTATIC, PLPRuntimeImageIO.className, "readFromFile", PLPRuntimeImageIO.readFromFileDesc,false);
		}
		else	// identChain is on the right of binary chain
		{
			
			if(type == TypeName.IMAGE || type == TypeName.INTEGER)
			{
				mv.visitInsn(Opcodes.DUP);							// leave one image on TOS
				StoreVariableFromStack(type, dec);					// store TOS to variable
			}
			else if(type == TypeName.FILE)
			{
				LoadVariableOnStack(type, dec);		//load file on TOS
				//call PLPRuntimeImageIO.write(BufferImage, File), image will be on TOS!!!!(Notice, important)
				mv.visitMethodInsn(Opcodes.INVOKESTATIC, PLPRuntimeImageIO.className, "write", PLPRuntimeImageIO.writeImageDesc,false);
			}
			else if(type == TypeName.FRAME)
			{
				//call PLPRuntimeFrame.createOrSetFrame(image, frame); frame will be on TOS
				mv.visitVarInsn(Opcodes.ALOAD,dec.GetSlotNum());	
				mv.visitMethodInsn(Opcodes.INVOKESTATIC,PLPRuntimeFrame.JVMClassName, "createOrSetFrame",
								   PLPRuntimeFrame.createOrSetFrameSig,false);
				mv.visitInsn(Opcodes.DUP);
				mv.visitVarInsn(Opcodes.ASTORE, dec.GetSlotNum());
				//CodeGenUtils.genPrintTOS()
			}
		}
		return null;
	}

	@Override
	public Object visitIdentExpression(IdentExpression identExpression, Object arg) throws Exception 
	{
		//TODO Implement this
		Token ident = identExpression.firstToken;
		Dec dec = m_symtab.lookup(ident.getText());
		TypeName type = Type.getTypeName(dec.getType());
		int slotNum = dec.GetSlotNum();
		if(slotNum == -1)							//if the slot number is -1, then it must be a field
		{											//load field on the top of stack 
			mv.visitVarInsn(ALOAD, 0);
			mv.visitFieldInsn(Opcodes.GETFIELD, className, ident.getText(), dec.GetType().getJVMTypeDesc());
		}
		else
		{
			switch(type)
			{
				case INTEGER: case BOOLEAN:
					mv.visitVarInsn(Opcodes.ILOAD, slotNum);		//store top of stack into local variable
					break;
				case IMAGE: case FRAME: case URL: case FILE:
					mv.visitVarInsn(Opcodes.ALOAD, slotNum);		
					break;
				default:
					assert false : "not yet implemented";
					break;
			}
		}

		return null;
	}

	@Override
	public Object visitIdentLValue(IdentLValue identX, Object arg) throws Exception 
	{
		//TODO Implement this
		//Notice, since we have type checking, so don't worry no definition or re-definition

		Dec dec = m_symtab.lookup(identX.getText());		//lookup in the symbol to find dec
		TypeName type = dec.GetType();
		int slotNum = dec.GetSlotNum();
		if(slotNum == -1)									//if the slot number is -1, then it must be a field
		{													//store the top of stack value into field
			mv.visitVarInsn(Opcodes.ALOAD, 0);				//load this
			mv.visitInsn(Opcodes.SWAP);
			mv.visitFieldInsn(Opcodes.PUTFIELD, className, identX.getText(), dec.GetType().getJVMTypeDesc());
		}
		else
		{
			switch(type)
			{
				case INTEGER:
				case BOOLEAN:
					mv.visitVarInsn(Opcodes.ISTORE, slotNum);		//store top of stack into local variable
					break;
				case IMAGE:											//call PLPRuntimeImageOps.copyImage function
					mv.visitMethodInsn(Opcodes.INVOKESTATIC, PLPRuntimeImageOps.JVMName,"copyImage",PLPRuntimeImageOps.copyImageSig,false);
					mv.visitVarInsn(Opcodes.ASTORE, slotNum);
					break;
				default:
					assert false : "not yet implemented";
					break;
			}
		}
		return null;
	}

	@Override
	public Object visitIfStatement(IfStatement ifStatement, Object arg) throws Exception 
	{
		//TODO Implement this
		Label after = new Label();						//create a lable
		ifStatement.getE().visit(this,null);			//result is on the top of stack
														
		mv.visitJumpInsn(Opcodes.IFEQ, after);			//if the expression == 0 , skip block
		ifStatement.getB().visit(this,null);
		mv.visitLabel(after);
		return null;
	}

	@Override
	public Object visitIntLitExpression(IntLitExpression intLitExpression, Object arg) throws Exception 
	{
		//TODO Implement this
		mv.visitLdcInsn((intLitExpression.value));
		return null;
	}


	@Override
	public Object visitParamDec(ParamDec paramDec, Object arg) throws Exception 
	{
		//TODO Implement this
		//For assignment 5, only needs to handle integers and booleans
		TypeName type = Type.getTypeName(paramDec.getType());
		paramDec.SetType(type);
		FieldVisitor fv = cw.visitField(Opcodes.ACC_PUBLIC,paramDec.getIdent().getText(),type.getJVMTypeDesc(),null,null);
		fv.visitEnd();

		Integer index = (Integer) arg;
		mv.visitVarInsn(Opcodes.ALOAD,0);					// load this

        switch(type)
        {
            case INTEGER:
            	mv.visitVarInsn(Opcodes.ALOAD,1);       			// load args[]
				mv.visitLdcInsn(index);								//load index into top of stack
				mv.visitInsn(Opcodes.AALOAD);			//load args[index] into top of stack, which is a String
                mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Integer", "parseInt", "(Ljava/lang/String;)I", false);
                break;
            case BOOLEAN:
            	mv.visitVarInsn(Opcodes.ALOAD,1);       			// load args[]
				mv.visitLdcInsn(index);								//load index into top of stack
				mv.visitInsn(Opcodes.AALOAD);			//load args[index] into top of stack, which is a String
                mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Boolean", "parseBoolean", "(Ljava/lang/String;)Z", false);
                break;
            case URL:
            	mv.visitVarInsn(Opcodes.ALOAD,1);       			// load args[]
				mv.visitLdcInsn(index);								//load index into top of stack
            	mv.visitMethodInsn(Opcodes.INVOKESTATIC,PLPRuntimeImageIO.className, "getURL", PLPRuntimeImageIO.getURLSig,false);
            	break;
            case FILE:
            	mv.visitTypeInsn(NEW, "java/io/File");				// allocate space for File
				mv.visitInsn(DUP);									// duplicate on top of stack
				mv.visitVarInsn(Opcodes.ALOAD,1);       			// load args[]
				mv.visitLdcInsn(index);								// load index into top of stack
            	mv.visitInsn(Opcodes.AALOAD);						// args
				mv.visitMethodInsn(INVOKESPECIAL, "java/io/File", "<init>", "(Ljava/lang/String;)V", false);// new Name(args)
				break;
            default:
                assert false : "not yet implemented";
                break;
        }
		mv.visitFieldInsn(Opcodes.PUTFIELD, className, paramDec.getIdent().getText(), type.getJVMTypeDesc()); //store top of stack into field
		m_symtab.insert(paramDec.getIdent().getText(), paramDec);	        //also insert into symbol table
		return null;
	}

	@Override
	public Object visitSleepStatement(SleepStatement sleepStatement, Object arg) throws Exception 
	{
		//assert false : "not yet implemented";
		sleepStatement.getE().visit(this,null);				//put value of expression on the top of stack
		mv.visitInsn(Opcodes.I2L);					//change the type from int to long
		mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Thread","sleep","(J)V",false); //call sleep(long ms);
		return null;
	}

	@Override
	public Object visitTuple(Tuple tuple, Object arg) throws Exception 
	{
		//assert false : "not yet implemented";
		ArrayList<Expression> exprList = (ArrayList<Expression>)(tuple.getExprList());
		for(int i=0;i<exprList.size();i++)
		{
			exprList.get(i).visit(this,null);	//put all the values of expressions on the TOS, from left to right
		}
		return null;
	}

	@Override
	public Object visitWhileStatement(WhileStatement whileStatement, Object arg) throws Exception 
	{
		//TODO Implement this
		Label guard = new Label();
		Label body = new Label();

		mv.visitJumpInsn(Opcodes.GOTO,guard);
		
		mv.visitLabel(body);
		whileStatement.getB().visit(this,null);		//body is the block

		mv.visitLabel(guard);
		whileStatement.getE().visit(this,null);
		mv.visitJumpInsn(Opcodes.IFNE,body);		//take the top of value, check if it's not eq 0, then jump to body

		Label finish = new Label();
		mv.visitLabel(finish);
		return null;
	}

}
