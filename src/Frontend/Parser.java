package Frontend;

import java.util.*;

import Frontend.BasicBlock.BlockType;
import Frontend.TokenType;
import Frontend.Result.Type;


public class Parser{
	Token tt;
	private Scanner scanner;
	//private HashMap<Integer,Instruction> Sym_table;	//mapping b/w index and instruction
	private HashMap<String,Result> Result_cache;	//for storing Results
	public static ArrayList<Result> array_list=new  ArrayList<Result>();
	public static ArrayList<Instruction> insts;			//list for instructions
	public static HashMap<Integer,Stack<Instruction>> Sym_table;		//stack per variable
//	public static HashMap<String,HashMap<Integer,Stack<Instruction>>> Func_Sym_table;
	public static HashMap<String,Function> Function_list=new HashMap<String,Function>();
	public Stack<Instruction> if_stack = new Stack<Instruction>();
	public Stack<Instruction> while_stack = new Stack<Instruction>();
	public char sym;
	public int index=0;
	public static BasicBlock currentblock;
	public Function calledfunction;
	public boolean read_statement_boolean=false;
	public HashMap<Integer,ArrayList<Result>> store_inst_bb=new HashMap<Integer,ArrayList<Result>>();

	public static HashMap<Integer,String> func_mapping = new HashMap<Integer,String>();
	
	public Parser(String filename){
		scanner = new Scanner(filename);				//initialize scanner
		Result_cache = new HashMap<String,Result>();	//initialize result_cache
		insts=new ArrayList<Instruction>();				//initialize instruction list
		//Sym_table = new HashMap<Integer,Instruction>();	//initialize symbol table
		Sym_table = new HashMap<Integer,Stack<Instruction>>();		//initialize per variable stack	
	//	HashMap<String,HashMap<Integer,Stack<Instruction>>> Func_Sym_table=new HashMap<String,HashMap<Integer,Stack<Instruction>>>();
		tt = scanner.getToken();
		Function.func_id =0;
	}
	
	void Next()
	{
		tt = scanner.getToken();
		if(tt.getType()==TokenType.commentToken)
			Next();
	}

	public static int String2Id(String s){
	
		int res=0;
		res = Scanner.var_cache.get(s);
		return res;
	}
	
	public static String IdtoString(int index)
	{
		//String o = new String();
		for (String o : Scanner.var_cache.keySet()) {
		      if (Scanner.var_cache.get(o).equals(index)) {
		        return o;
		      }
		}
		return "";
	}
	
	public BasicBlock compute()
	{
		int res=-1;
		if(tt.getType() == TokenType.commentToken) //if first line is a comment
			Next();
		if(tt.getType() != TokenType.mainToken || tt.getType() == TokenType.errorToken)	//if main is not the first token,error
			error("Syntax error : Missing 'main'");
		else								 //after parsing main
		{
			while(tt.getType()!= TokenType.periodToken)	//"." at the end
			{
				Next();
				while(tt.getType() == TokenType.varToken || tt.getType() == TokenType.arrToken)	//if its a var
					var_decl();
			
				while(tt.getType() == TokenType.funcToken || tt.getType() == TokenType.procToken){	//if its a function declaration
					if(tt.getType() == TokenType.funcToken)
						func_decl(Function.Type.function);
					else
						func_decl(Function.Type.procedure);
				}	
				if(tt.getType() == TokenType.beginToken) //"{"
				{	currentblock = new BasicBlock(); // main block
					BasicBlock.mainblock=currentblock;
					System.out.println("Basic Block: "+ BasicBlock.block_id);
					BasicBlock.block_id++;
					int errorcheck=0;
					while(tt.getType() != TokenType.endToken&&tt.getType() != TokenType.periodToken){
						currentblock=stat_seq(currentblock);	errorcheck++;					//statSequence
						System.out.println(tt.getCharacters());
						if(errorcheck>1500)
							throw new IllegalArgumentException("error:missing semicolon");
					}
					if(tt.getType() == TokenType.periodToken){
						System.out.println("error:missing semicolon");
					}
					Instruction end=new Instruction("end");
					if(insts.get(insts.size()-1).getOperator()!="end"){
					insts.add(end);
					currentblock.inst_list.add(end);}
					if(tt.getType() != TokenType.endToken) //"}"
					{
						Token.checkToken("");
						error("Syntax error : Missing '}'");
					}
				}
			}
			/*if(tt.getType() != TokenType.periodToken)	//"."
			{
				Token.checkToken("");
				error("Syntax error : Missing '.' at the end");
			}*/
		}
			
		return currentblock;	
	}
	
	public void var_decl()    //TODO: take care of scope
	{
		if(tt.getType() == TokenType.varToken)
		{
			while(tt.getType() != TokenType.semiToken)
			{
				Next();
				if(tt.getType() == TokenType.ident)
				{
					String ss = tt.getCharacters();
					Result x = new Result(Type.variable,ss);
					Result_cache.put(ss,x); //store var in hash map
				}
				
			}
			Next();
		}
		if(tt.getType() == TokenType.arrToken)	//array
		{
			int num=0;
			String s1 = new String();
			while(tt.getType() != TokenType.semiToken)
			{					
				Next();
				while(tt.getType() == TokenType.ident)
				{	s1=tt.getCharacters();
					ArrayList<Integer> arrsize=new ArrayList<Integer>();
					Next();
					while(tt.getType() == TokenType.openbracketToken)	//array [
					{	
						Next();
						if(tt.getType() == TokenType.number)	//array [ 4
						{
							num=Integer.parseInt(tt.getCharacters());					//num = 4
							arrsize.add(num);
						}
						
						Next();
						if(tt.getType() != TokenType.closebracketToken)
						{
							Token.checkToken("");
							error("Syntax error : Missing ']' in array declaration");
							return;
						}
						Next();	
					}
					if(tt.getType() != TokenType.ident)
					{
						Result x = new Result(Type.arr,s1,arrsize);
						array_list.add(x);
						Result_cache.put(s1,x);
					}
					
				}
				
				
			}
			 if(Result_cache.get("FP")==null) {
				 Result FP = new Result(Type.FP,"FP");
				 Result_cache.put("FP",FP);
			 }
		}
			
	}
	
	public String get_funcname(int index)
	{
		return func_mapping.get(index);
	}
	
	public void func_decl(Function.Type type)
	{	
		String funcname = null;
		Next();
		if(tt.getType() == TokenType.ident)
		{
			funcname = tt.getCharacters();
		//	Result x = new Result(Type.function,funcname);
		//	Result_cache.put(funcname,x); //store function name in hash map
		}
		else
			System.out.println("error: function declaration"+tt.getType());
		Next();//for (
		ArrayList<Result> param_list = new ArrayList<Result>();
		if (tt.getType()==TokenType.openparenToken)
		{	Next();
			
			while(tt.getType()!=TokenType.closeparenToken)
			{	
				if(tt.getType()==TokenType.commaToken)	
					Next();//to skip comma
				String ss = tt.getCharacters();
				Result x = new Result(Type.param,ss);
				param_list.add(x);
				Result_cache.put(ss,x); //store function name in hash map
				Next();
			}
		}
		Function func;
		if(param_list.size()==0){
			func=new Function(type,funcname);
			func.setfirstbb();}
		else{
			func= new Function(type,funcname,param_list);
			func.setfirstbb();}
		BasicBlock funcbb= func.getfirstbb();
		BasicBlock.block_id++;
		func_mapping.put(Function.func_id, funcname);
		
		Function.func_id++;
		
		Function_list.put(funcname, func);
		Next();Next();
		while(tt.getType() == TokenType.varToken||tt.getType() == TokenType.arrToken){
			var_decl();
		}
		Next();
		BasicBlock returnblock=funcbb;
		calledfunction = func;
		while(tt.getType()!=TokenType.endToken)
			returnblock=stat_seq(returnblock);
		func.setreturnbb(returnblock);
		
		//func.setreturninst(insts.get(insts.size()-1)); // for  last inst is end at -1
		Next();

	}
	
	/*public void formal_Param(String function_name,ArrayList<Result> param_list){
		Function_param.put(function_name, param_list);
	}*/
	

	
	public BasicBlock stat_seq(BasicBlock currentblock)
	{
		return statement(currentblock);
	}
	
	public BasicBlock statement(BasicBlock currentblock)
	{	System.out.println(tt.getCharacters());
		BasicBlock bb;
		if(tt.getType() == TokenType.semiToken){	//";"
			Next();
			bb=currentblock;}
		else if(tt.getType() == TokenType.beginToken){ //"{"
			Next();
			bb=currentblock;}
		else if(tt.getType() == TokenType.letToken)	//let 
		{	assignment(currentblock);
			Next();
			bb=currentblock;
		}
		else if(tt.getType() == TokenType.callToken)	//call
		{
			bb = funcCall(currentblock);
			if(tt.getType()!= TokenType.semiToken)
			Next();
		}
		else if(tt.getType() == TokenType.ifToken)	//if
		{	
			bb=ifStatement(currentblock);
			if(tt.getType()==TokenType.fiToken)Next();
		}
		else if(tt.getType() == TokenType.whileToken)	//while
		{
			bb=whileStatement(currentblock);
			//Next();
		}
		else if(tt.getType() == TokenType.returnToken)	//return
		{
			Next();
			Result res = E(currentblock);
			calledfunction.setreturninst(res.getInstruction());
			bb=currentblock;
		}
		else if(tt.getType() == TokenType.periodToken||tt.getType() == TokenType.endToken){
			bb=currentblock;
		}
		else bb=currentblock;
		return bb;
	}
	
	public Result assignment(BasicBlock currentblock)		//"let"
	{
		boolean arrflag=false;
		Instruction adda=null,arrmulins=null,arraddfpins=null;
		int index=0;
		String var;
		Result x = new Result();
		Result arr=null;
		Next();
		if(tt.getType() == TokenType.ident)				//if its a var
		{
			var = tt.getCharacters();
			index = String2Id(tt.getCharacters());
			Next();
			if(tt.getType() == TokenType.openbracketToken)	//"["	//for array
			{	arrflag=true;
				int indexrelloc=0;
				arr=Result_cache.get(var);
				ArrayList<Integer> arrsize=arr.getArraySize();
				ArrayList<Result> arrindex=new ArrayList<Result>();
				while(tt.getType()!=TokenType.becomesToken){
					Next();
					x=E(currentblock);
					arrindex.add(x);
					if(tt.getType()!= TokenType.closebracketToken)	//"]"
					{
						error("Syntax error: ']' missing");
					}
					else
						Next();
				}
				boolean flagarray=false;//false means size is 1
				for(int i=0;i<arrsize.size()-1;i++){
					int j=(arrsize.size())-(arrsize.size()-i-1),mul=1;flagarray=true;
					while(j<arrsize.size()){
						mul*=arrsize.get(j);
						j++;
					}
					Result intsize = new Result(Type.number,mul);
					arrmulins = new Instruction("mul",arrindex.get(i),intsize);
					currentblock.inst_list.add(arrmulins);
					insts.add(arrmulins);				//add instruction to instruction list
					arrmulins.basicblock = currentblock; 
					arrmulins.block_id = BasicBlock.block_id;
					if(i%2-1==0){
						Result finalindex1 = new Result(Type.instruction,insts.get(insts.size()-2));
						Result finalindex2 = new Result(Type.instruction,arrmulins);
						Instruction arrfinalindexins = new Instruction("add",finalindex1,finalindex2);
						currentblock.inst_list.add(arrfinalindexins);
						insts.add(arrfinalindexins);				//add instruction to instruction list
						arrfinalindexins.basicblock = currentblock; 
						arrfinalindexins.block_id = BasicBlock.block_id;
					}
				}
				arrmulins=null;
				if(!flagarray){
					Result intsize = new Result(Type.number,4);
				    arrmulins = new Instruction("mul",intsize,arrindex.get(arrindex.size()-1));
					currentblock.inst_list.add(arrmulins);
					insts.add(arrmulins);				//add instruction to instruction list
					arrmulins.basicblock = currentblock; 
					arrmulins.block_id = BasicBlock.block_id;
				}
				else
				{
				Result finalindex = new Result(Type.instruction,insts.get(insts.size()-1));
				Instruction arrfinalindexins = new Instruction("add",arrindex.get(arrindex.size()-1),finalindex);
				currentblock.inst_list.add(arrfinalindexins);
				insts.add(arrfinalindexins);				//add instruction to instruction list
				arrfinalindexins.basicblock = currentblock; 
				arrfinalindexins.block_id = BasicBlock.block_id;
				
				Result arrrelloc = new Result(Type.instruction,arrfinalindexins);
				Result intsize = new Result(Type.number,4);
				arrmulins = new Instruction("mul",intsize,arrrelloc);
				currentblock.inst_list.add(arrmulins);
				insts.add(arrmulins);				//add instruction to instruction list
				arrmulins.basicblock = currentblock; 
				arrmulins.block_id = BasicBlock.block_id;
				}
				
			    arraddfpins = new Instruction("add",Result_cache.get(var),Result_cache.get("FP"));
				currentblock.inst_list.add(arraddfpins);
				insts.add(arraddfpins);				//add instruction to instruction list
				arraddfpins.basicblock = currentblock;
				arraddfpins.block_id = BasicBlock.block_id;
				
			}
			if(tt.getType() == TokenType.becomesToken) //"<-"
			{
				Next();
				if (tt.getType()==TokenType.callToken){
						currentblock=stat_seq(currentblock);
						if (read_statement_boolean==true){
							read_statement_boolean=false;
							x=new Result(Type.instruction,insts.get(insts.size()-1));}
						else	
							x=calledfunction.getreturninst();
				}
				else {
					x = E(currentblock);
				}
							//e.g. let x <- 51;
					Instruction i = new Instruction("move",x, Result_cache.get(var));
					currentblock.inst_list.add(i);
					insts.add(i);				//add instruction to instruction list
					i.basicblock = currentblock;
					i.block_id = BasicBlock.block_id;
					
					
					if(arrflag==false){
					if(!currentblock.get_Sym_table().containsKey(index))	//if sym_table is empty
					{
						Stack<Instruction> ss = new Stack<Instruction>();
						ss.push(i);
						currentblock.get_Sym_table().put(index, ss);
					}
					else										//if the entry is present
					{
						currentblock.get_Sym_table().get(index).push(i);	//push new value on stack
					}
					//Sym_table.put(index,i);		//map instruction to variable index 
						
					if(x.getType() ==Type.number)
					{
						System.out.println(insts.indexOf(i)+":"+"move #"+ x.getValue() + " " + var+"_"+insts.indexOf(i));
					}
					else if(x.getType() == Type.instruction)
					{
						System.out.println(insts.indexOf(i)+":"+"move ("+insts.indexOf(x.getInstruction())+") "+ var+"_"+insts.indexOf(i));
					}
					}
					else{
						Result addares1=new Result(Type.instruction,arrmulins);
						Result addares2=new Result(Type.instruction,arraddfpins);
						Instruction arradda = new Instruction("adda",addares1,addares2);
						currentblock.inst_list.add(arradda);
						insts.add(arradda);				//add instruction to instruction list
						arradda.basicblock = currentblock;
						arradda.block_id = BasicBlock.block_id;
						adda=arradda;
						
						Result arrstore1=new Result(Type.instruction,i);
						Result addainst=new Result(Type.instruction,adda);
						Instruction arrstore = new Instruction("store",arrstore1,addainst);
						currentblock.inst_list.add(arrstore);
						insts.add(arrstore);				//add instruction to instruction list
						arrstore.basicblock = currentblock;
						arrstore.block_id = BasicBlock.block_id;
						if(store_inst_bb.containsKey(currentblock.getblockno())){
							if(!store_inst_bb.get(currentblock.getblockno()).contains(arr))
								store_inst_bb.get(currentblock.getblockno()).add(arr);
						}
						else{
							ArrayList<Result> arrays=new ArrayList<Result>();
							arrays.add(arr);
							store_inst_bb.put(currentblock.getblockno(),arrays);
						}
						
					}
				
				
				
			}
			else 
				throw new IllegalArgumentException("error:assignment not complete");
		}
		
		return x;
	}
	
	public BasicBlock funcCall(BasicBlock currentblock){
		BasicBlock bb=currentblock;
		Next();
		if(tt.getCharacters().equals("InputNum")){
			Next();read_statement_boolean=true;
			if (tt.getType()==TokenType.openparenToken)
			{	Next();
				
				//	String var = tt.getCharacters();
				//	int index = String2Id(tt.getCharacters());
					Instruction read_inst = new Instruction("read");
					read_inst.basicblock = currentblock;
					read_inst.block_id = BasicBlock.block_id;
					currentblock.inst_list.add(read_inst);
					insts.add(read_inst);
			/*		Result read=new Result(Type.instruction,read_inst);
					Instruction move_read = new Instruction("move",read, Result_cache.get(var));
					currentblock.inst_list.add(move_read);
					insts.add(move_read);				//add instruction to instruction list
					move_read.basicblock = currentblock;
					move_read.block_id = BasicBlock.block_id;
			*/		
			/*		if(!currentblock.get_Sym_table().containsKey(index))	//if sym_table is empty
					{
						Stack<Instruction> ss = new Stack<Instruction>();
						ss.push(move_read);
						currentblock.get_Sym_table().put(index, ss);
					}
					else										//if the entry is present
					{
						currentblock.get_Sym_table().get(index).push(move_read);	//push new value on stack
					}*/
				Next();
				return  currentblock;
			}
			else 
				throw new IllegalArgumentException("error:no paranthesis in read");
		}
		else if(tt.getCharacters().equals("OutputNum")){
			Next();
			if (tt.getType()==TokenType.openparenToken)
			{	Next();
				if(tt.getType()==TokenType.callToken){
					currentblock=stat_seq(currentblock);
					Result x=calledfunction.getreturninst();
					Instruction i = new Instruction("write",x);
					currentblock.inst_list.add(i);
					insts.add(i);				//add instruction to instruction list
					i.basicblock = currentblock;
					i.block_id = BasicBlock.block_id;
				}
				else if(tt.getType()==TokenType.ident||tt.getType()==TokenType.number){
					Result x=E(currentblock);
					Instruction i = new Instruction("write",x);
					currentblock.inst_list.add(i);
					insts.add(i);				//add instruction to instruction list
					i.basicblock = currentblock;
					i.block_id = BasicBlock.block_id;
					System.out.println("write");
				}
				Next();
				return  currentblock;
			}
			else 
				throw new IllegalArgumentException("error:no paranthesis in outputnum");
		}
		else if(tt.getCharacters().equals("OutputNewLine")){
			Next();
			if (tt.getType()==TokenType.openparenToken)
			{	Next();
				Instruction i = new Instruction("writeNL");
				currentblock.inst_list.add(i);
				insts.add(i);				//add instruction to instruction list
				i.basicblock = currentblock;
				i.block_id = BasicBlock.block_id;
				Next();
				return  currentblock;
			}
			else 
				throw new IllegalArgumentException("error:no paranthesis in outputNL");
		}
		else if(Function_list.containsKey(tt.getCharacters())){
			Function func=Function_list.get(tt.getCharacters());
			calledfunction=func;
			//Instruction jump_ins = func.getfirstbb().inst_list.get(0);
			Result jump_res = new Result(Type.variable,tt.getCharacters());
			Instruction call_inst = new Instruction("call",jump_res);
			call_inst.basicblock = currentblock;
			call_inst.block_id = BasicBlock.block_id;
			currentblock.inst_list.add(call_inst);
			insts.add(call_inst);
			Next();
			if (tt.getType()==TokenType.openparenToken)
			{	Next();
				ArrayList<Result> params=new ArrayList<Result>();
				
				while(tt.getType()!=TokenType.closeparenToken)
				{	
					if(tt.getType()==TokenType.commaToken)	
						Next();//to skip comma
					Result x = E(currentblock);
					params.add(x);
				}
				func.add_param_call(call_inst, params);
			}
		//	BasicBlock nextblock=currentblock.createafterfunction(func.getfirstbb());
		//	BasicBlock.block_id++;
		//	return nextblock;	
			return  currentblock;
		}
		else
			throw new IllegalArgumentException("error:undefined function");
	
	}
	
	public boolean isrelop(){
		return (tt.getType()==TokenType.eqlToken ||tt.getType()==TokenType.neqToken ||tt.getType()==TokenType.lssToken ||tt.getType()==TokenType.geqToken || tt.getType()==TokenType.leqToken ||tt.getType()==TokenType.gtrToken);	
	}
	
	public BasicBlock ifStatement(BasicBlock currentblock)
	{
		int if_count=1;
		int else_flag=0;
		BasicBlock if_block,else_block,phi_block,iftruebb=null,elsebb=null;
		BasicBlock nested_if_block = currentblock;
		
				if(if_count != 1)
					if_count++;
				iftruebb=elsebb=nested_if_block=else_block=phi_block=if_block = currentblock.createIfTrue();
				
				Next();
				Result op1 = E(currentblock);

				if(isrelop()) {
					Result cond = new Result(Type.condition,tt.getType());
					Next();
					Result op2 = E(currentblock);
					Instruction i = new Instruction("cmp",op1,op2);
					insts.add(i);
					currentblock.inst_list.add(i);
					i.basicblock = currentblock;
					i.block_id = BasicBlock.block_id;
					Result ins_res = new Result(Type.instruction,i);
					
					if(op1.getType() == Type.instruction)	//first operand is a pointer to a instruction.
					{
						if(op2.getType() == Type.instruction)	//second operand is also pointer to instruction
						{
							System.out.println(insts.indexOf(i)+":"+"cmp (" + insts.indexOf(op1.getInstruction()) + ") (" + insts.indexOf(op2.getInstruction()) + ")"); 
						}
						else	//second operand is a number
						{
							System.out.println(insts.indexOf(i)+":"+"cmp (" + insts.indexOf(op1.getInstruction()) + ") #" + op2.getValue());
						}
					}
					else	//first operand is a number
					{
						if(op2.getType() == Type.instruction)
						{
							System.out.println(insts.indexOf(i)+":"+"cmp #" + op1.getValue() + " (" + insts.indexOf(op2.getInstruction()) + ")");
						}
						else
						{
							System.out.println(insts.indexOf(i)+":"+"cmp #" + op1.getValue() +" #" + op2.getValue());
						}
					}
					
					String ss = cond.getCondition().name();
					Result fix_res = new Result(Type.instruction,i);// i just given to initialize, fixup will replace this
					switch(ss)
					{
					case "bge":
					case "ble":
					case "beq":
					case "bne":		
					case "bgt":
					case "blt":
						Instruction ii = new Instruction(ss,ins_res,fix_res);//to be fixed to location of jump
						currentblock.end_Instr = ii;
						ii.basicblock = currentblock;
						ii.block_id = BasicBlock.block_id;
						currentblock.inst_list.add(ii);
						insts.add(ii);
						if_stack.push(ii);
						
						System.out.println(insts.indexOf(ii)+":"+ss+" ("+insts.indexOf(ins_res.getInstruction())+") 0");
						break;
					default:
						break;
					}
				}
				else{
					error("Syntax error: Missing condition after 'if'");
					return phi_block;
				}
				if(tt.getType() == TokenType.thenToken)	//"then"
				{
					System.out.println("Basic Block: "+ BasicBlock.block_id+"\n");
					BasicBlock.block_id++;
					Next();
					if(tt.getType() == TokenType.ifToken||tt.getType() == TokenType.whileToken)
					{	
						while((tt.getType() != TokenType.elseToken)&&(tt.getType() != TokenType.fiToken))
							iftruebb=stat_seq(if_block);
						if(tt.getType() == TokenType.semiToken)
							Next();
						//Next();
					}
					else
					{	while((tt.getType() != TokenType.elseToken)&&(tt.getType() != TokenType.fiToken)){
							iftruebb = stat_seq(iftruebb);
						}

					}
				}
			//while(!if_stack.isEmpty()){
				System.out.println(tt.getCharacters());
			if(tt.getType() == TokenType.elseToken)	//else
			{
				else_flag=1;
				elsebb= else_block = currentblock.createElse();
				System.out.println("Basic Block: "+ BasicBlock.block_id+"\n");
				BasicBlock.block_id++;
				Next();
				
				if(tt.getType() == TokenType.ifToken||tt.getType() == TokenType.whileToken)
				{	
					// elsebb=ifStatement(else_block);
					elsebb=stat_seq(else_block);
					if(tt.getType() == TokenType.semiToken)
						Next();
					//Next();
				}
				else
				{	while((tt.getType() != TokenType.fiToken)){
					elsebb = stat_seq(elsebb);
					}

				}
				Instruction	my_fix = if_stack.pop();
				int len = my_fix.getOperands().size() - 1;	//index of last operand
				Result res2 = my_fix.getOperands().get(len);
				
				//Instruction fix_loc = res.getFixupLocation();
				Instruction fix_loc = currentblock.getifelseblock().inst_list.get(0);
				//res2.setFixupLocation(fix_loc);
				res2.setInstruction(fix_loc);
			}
			if(tt.getType()== TokenType.semiToken)
				Next();
			if(tt.getType() == TokenType.fiToken)	//fi
			{
				String var;
				int i=1;
				
				if(else_flag==1){
					if(elsebb.inst_list.get(0).getOperator()=="end"&&elsebb.inst_list.size()==1){
						BasicBlock.decblockid();
						elsebb=elsebb.getprevblock();
						elsebb.setfollowblocknull();
					}
				}
				if(iftruebb.inst_list.get(0).getOperator()=="end"&&iftruebb.inst_list.size()==1){
					phi_block=iftruebb;
					phi_block.changeType(BasicBlock.BlockType.join);
					insts.remove(iftruebb.inst_list.get(0));
					phi_block.inst_list.remove(0);
				}			
				else	
				{phi_block = iftruebb.createjoin();}
				
				
				if(else_flag==1){
					elsebb.setjoin(phi_block);
				}	
				else
					currentblock.setjoin(phi_block);
				System.out.println("\nJoin block: "+ BasicBlock.block_id+"\n");
				BasicBlock.block_id++;
				if(store_inst_bb.containsKey(iftruebb.getblockno())){
					for(Result arrays:store_inst_bb.get(iftruebb.getblockno())){
						Instruction ii = new Instruction("kill",arrays);
						ii.basicblock=phi_block;
						insts.add(ii);
						phi_block.inst_list.add(ii);
					}
				}
				else if(else_flag==1){
					if(store_inst_bb.containsKey(elsebb.getblockno())){
						for(Result arrays:store_inst_bb.get(elsebb.getblockno())){
							Instruction ii = new Instruction("kill",arrays);
							ii.basicblock=phi_block;
							insts.add(ii);
							phi_block.inst_list.add(ii);
						}
					}	
						
				}	
				
				for(i=1;i<=currentblock.get_Sym_table().size();i++)	//iterate thru each var and check if it has more than 1 value in its stack
				{
					if(currentblock.get_Sym_table().get(i).size()>1)
					{
							var = IdtoString(i);//Todo
							Instruction i1 = currentblock.get_Sym_table().get(i).pop();
							Instruction i2;
							if(else_flag != 0){
								//int top = currentblock.get_Sym_table().get(i).size()-1;
								while(currentblock.get_Sym_table().get(i).peek().block_id >= i1.block_id && currentblock.get_Sym_table().get(i).size()>1){
									i2 = currentblock.get_Sym_table().get(i).pop();	
									//top--;
								}
								if(currentblock.get_Sym_table().get(i).size() > 1)
									i2 = currentblock.get_Sym_table().get(i).pop();
								else
									i2 = currentblock.get_Sym_table().get(i).peek();
							}
							else
							{
								
								int top = currentblock.get_Sym_table().get(i).size()-1;
								while(currentblock.get_Sym_table().get(i).elementAt(top).block_id >= i1.block_id && top>0){
									top--;
								}
								
								i2 = currentblock.get_Sym_table().get(i).elementAt(top);
							}
							
							Result oper1= new Result(Type.instruction,i1);
							Result oper2= new Result(Type.instruction,i2);
							Instruction ii;
							if(else_flag==0)
								ii = new Instruction("phi",var,oper1,oper2);
							else
							{
								if(oper1.getInstruction().basicblock.getType() == BlockType.iftrue)
									ii = new Instruction("phi",var,oper1,oper2);
								else
									ii = new Instruction("phi",var,oper2,oper1);
							}
							ii.basicblock=phi_block;
							insts.add(ii);
							ii.block_id = BasicBlock.block_id;
							phi_block.inst_list.add(ii);
							currentblock.get_Sym_table().get(i).push(ii);
							System.out.println(insts.indexOf(ii)+":"+"phi "+ var +"_"+insts.indexOf(ii)+ " (" + insts.indexOf(i1)+") " + "(" + insts.indexOf(i2) + ")");
							
						}
				
				}
				if(else_flag==0)
				{
					Instruction	my_fix = if_stack.pop();
					int len = my_fix.getOperands().size() - 1;	//index of last operand
					Result res2 = my_fix.getOperands().get(len);
					
					//Instruction fix_loc = res.getFixupLocation();
					Instruction fix_loc = phi_block.inst_list.get(0);
					//res2.setFixupLocation(fix_loc);
					res2.setInstruction(fix_loc);
				}
				//BasicBlock.block_id++;	
				Next();
				else_flag=0;
			}
	
		return phi_block;
		
	}
	
	public BasicBlock whileStatement(BasicBlock currentblock)
	{
		int while_count=1;
		int counter=0;
		BasicBlock dobb=null;

		while(while_count != 0)
		{
			if(tt.getType() == TokenType.whileToken)		//nested while
			{
				if(while_count != 1)
					while_count++;
				BasicBlock while_block;
				if(currentblock.inst_list.size()!=0){
					while_block = currentblock.createWhile();
					currentblock=currentblock.getnextblock();
					System.out.println("Basic Block: "+ BasicBlock.block_id+"\n");
					BasicBlock.block_id++;}		// while block for conditions and phi inst
				else{
					currentblock.changeType(BasicBlock.BlockType.whileblock);
					while_block = currentblock;
					}
				
			//	System.out.println("created while block");
				
				
				
				Next();

				Result op1 = E(currentblock);

				if(isrelop()) {
					Result cond = new Result(Type.condition,tt.getType());
					Next();
					Result op2 = E(currentblock);
					Instruction i = new Instruction("cmp",op1,op2);
					insts.add(i);
					i.basicblock = currentblock;
					i.block_id = BasicBlock.block_id;
					currentblock.inst_list.add(i);		//add this instruction to current block
					Result ins_res = new Result(Type.instruction,i);
					
					if(op1.getType() == Type.instruction)	//first operand is a pointer to a instruction.
					{
						if(op2.getType() == Type.instruction)	//second operand is also pointer to instruction
						{
							System.out.println(insts.indexOf(i)+":"+"cmp (" + insts.indexOf(op1.getInstruction()) + ") (" + insts.indexOf(op2.getInstruction()) + ")"); 
						}
						else	//second operand is a number
						{
							System.out.println(insts.indexOf(i)+":"+"cmp (" + insts.indexOf(op1.getInstruction()) + ") #" + op2.getValue());
						}
					}
					else	//first operand is a number
					{
						if(op2.getType() == Type.instruction)
						{
							System.out.println(insts.indexOf(i)+":"+"cmp #" + op1.getValue() + " (" + insts.indexOf(op2.getInstruction()) + ")");
						}
						else
						{
							System.out.println(insts.indexOf(i)+":"+"cmp #" + op1.getValue() +" #" + op2.getValue());
						}
					}
					String ss = cond.getCondition().name();
					Result fix_res = new Result(Type.instruction,i);
					switch(ss)
					{
					case "bge":
					case "ble":
					case "beq":
					case "bne":		
					case "bgt":
					case "blt":
						Instruction ii = new Instruction(ss,ins_res,fix_res);//to be fixed to location of jump
						currentblock.end_Instr = ii;
						currentblock.inst_list.add(ii);
						ii.basicblock = currentblock;
						ii.block_id = BasicBlock.block_id;
						insts.add(ii);
						while_stack.push(ii);
						
						System.out.println(insts.indexOf(ii)+":"+ss+" ("+insts.indexOf(ins_res.getInstruction())+") 0");
						break;
					default:
						break;
					}
				}
				else{
					error("Syntax error: Missing condition after 'while'");
					return dobb=currentblock;
				}
				if(tt.getType() == TokenType.doToken)	//"do"
				{	
					BasicBlock do_block = currentblock.createdo();
					Next();
					System.out.println("Basic Block: "+ BasicBlock.block_id+"\n");
					BasicBlock.block_id++;
					dobb=do_block;
					
						while(tt.getType() != TokenType.odToken){
							dobb = stat_seq(dobb);
						}
					
					dobb.setdotowhile(while_block);
					int phi_counter=0;
					HashMap<Instruction,Instruction> varsInDo=new HashMap<Instruction,Instruction>();
					Instruction firstWhileInst=while_block.inst_list.get(0);
					for(counter=1;counter<=currentblock.get_Sym_table().size();counter++)	//iterate thru each var and check if it has more than 1 value in its stack
					{	
						if(currentblock.get_Sym_table().get(counter).size()>1)
						{
								String var = IdtoString(counter);//Todo
								Instruction i1 = currentblock.get_Sym_table().get(counter).peek();
								int top = currentblock.get_Sym_table().get(counter).size()-2;			//second element from top
								while(currentblock.get_Sym_table().get(counter).elementAt(top).block_id >= i1.block_id && top>0){
									top--;
								}
								Instruction i2 = currentblock.get_Sym_table().get(counter).elementAt(top);
								Result oper1= new Result(Type.instruction,i1);
								Result oper2= new Result(Type.instruction,i2);
								Instruction ii = new Instruction("phi",var,oper1,oper2);
								ii.basicblock=while_block;
								insts.add(ii);
								while_block.inst_list.add(phi_counter++, ii);
								varsInDo.put(oper2.getInstruction(), ii);
								currentblock.get_Sym_table().get(counter).push(ii);
								System.out.println(insts.indexOf(ii)+":"+"phi "+ var +"_"+insts.indexOf(ii)+ " (" + insts.indexOf(i1)+") " + "(" + insts.indexOf(i2) + ")");
						}
					
					}
					for(int i=insts.indexOf(firstWhileInst);i<insts.size()-phi_counter;i++){
						Instruction inst=insts.get(i);
						if(inst.getOperands().size()==2){
							Result operator1=inst.getOperands().get(0);
							Result operator2=inst.getOperands().get(1);
							if(operator1.getType()==Type.instruction){
								if(varsInDo.containsKey(operator1.getInstruction())){
									Result newRes=new Result(Type.instruction,varsInDo.get(operator1.getInstruction()));
									inst.getOperands().set(0, newRes);
								}
							}
							if(operator2.getType()==Type.instruction){
								if(varsInDo.containsKey(operator2.getInstruction())){
									Result newRes=new Result(Type.instruction,varsInDo.get(operator2.getInstruction()));
									inst.getOperands().set(1, newRes);
								}
							}
						}
					}
					if(dobb.inst_list.get(dobb.inst_list.size()-1).getOperator()=="end"){
						Parser.insts.remove(dobb.inst_list.get(dobb.inst_list.size()-1));
						dobb.inst_list.remove(dobb.inst_list.size()-1);

					}
						Instruction jump_ins = while_block.inst_list.get(0);
						Result jump_res = new Result(Type.instruction,jump_ins);
						Instruction branch_inst = new Instruction("bra",jump_res);
						//System.out.println(bb.getType().toString());
						branch_inst.basicblock = dobb;
						branch_inst.block_id = BasicBlock.block_id;
						dobb.inst_list.add(branch_inst);
						insts.add(branch_inst);
					
				}
				else 
					System.out.println("no do token after while");
			}
			
			if(tt.getType() == TokenType.odToken)	//od
			{	
				BasicBlock follow_block = currentblock.createfollow();
				currentblock=follow_block;		// while block for conditions and phi inst
				System.out.println("Basic Block: "+ BasicBlock.block_id+"\n");
				BasicBlock.block_id++;
			
				while_count--;
				Next();
				while(tt.getType() != TokenType.odToken && tt.getType() != TokenType.elseToken&&tt.getType() != TokenType.endToken&&tt.getType() != TokenType.fiToken&&tt.getType() != TokenType.eofToken&&tt.getType() != TokenType.periodToken){
					follow_block=stat_seq(follow_block);}
				
				Instruction	my_fix = while_stack.pop();
				int len = my_fix.getOperands().size() - 1;	//index of last operand
				Result res2 = my_fix.getOperands().get(len);
				Instruction fix_loc;
				if(!follow_block.inst_list.isEmpty())
					fix_loc = follow_block.inst_list.get(0);
				else{
					Instruction end=new Instruction("end");
					insts.add(end);
					follow_block.inst_list.add(end);
					fix_loc = follow_block.inst_list.get(0);
				}
					
				res2.setInstruction(fix_loc);
			currentblock=follow_block;
			}
			
		}
	/*	System.out.println("Basic Block: "+ BasicBlock.block_id+"\n");
		BasicBlock.block_id++;*/

		return currentblock;
}
	
	
	public Result E(BasicBlock currentblock){
		//int res=0;
		Result res;
		Result final_res;
		final_res = res = T(currentblock);
		Result res1 = new Result();
		String oper;
		while(tt.getType() == TokenType.plusToken || tt.getType()==TokenType.minusToken)
		{
			if( tt.getType() == TokenType.plusToken)
				oper = new String("add");
		
			else //if (tt.getType() == TokenType.minusToken){
				oper = new String("sub");
			
			Next();
			res1 = E(currentblock);
			Instruction i = new Instruction(oper,res,res1);
			insts.add(i);
			currentblock.inst_list.add(i);
			i.basicblock = currentblock;
			i.block_id = BasicBlock.block_id;
			final_res = new Result(Type.instruction,i);
			if(res.getType() == Type.number)
			{
				if(res1.getType() == Type.number)	//"3+2"
					System.out.println(insts.indexOf(i)+":" +oper+" #"+res.getValue()+" "+ res1.getValue());
				else	//"3+x"
					System.out.println(insts.indexOf(i)+":"+oper+" #"+res.getValue()+" ("+insts.indexOf(res1.getInstruction())+")");
			}
			else
			{
				if(res1.getType() == Type.number)	//"x+2"
					System.out.println(insts.indexOf(i)+":"+oper+" ("+insts.indexOf(res.getInstruction())+") #"+ res1.getValue());
				else	//"y+x"
					System.out.println(insts.indexOf(i)+":"+oper+" ("+insts.indexOf(res.getInstruction())+")"+" ("+insts.indexOf(res1.getInstruction())+")");
			}
		}
		return final_res;
	}
	
	//1+2*3-4
	Result T(BasicBlock currentblock){
		Result res;
		Result res1 = new Result();
		Result final_res;
		final_res = res = F(currentblock);
		Token tmp;
		while(tt.getType() == TokenType.timesToken || tt.getType()== TokenType.divToken)
		{	
			tmp=tt;
			String oper;
			Next();
			res1 = E(currentblock);
			if(tmp.getType() == TokenType.timesToken)	
				oper = "mul";
			else
				oper = "div";
			
				Instruction i = new Instruction(oper,res,res1);
				insts.add(i);
				currentblock.inst_list.add(i);
				i.basicblock = currentblock;
				i.block_id= BasicBlock.block_id;
				final_res = new Result(Type.instruction,i);
				if(res.getType() == Type.number)
				{
					if(res1.getType() == Type.number)	//"3/2"
						System.out.println(insts.indexOf(i)+":" +oper+" #"+res.getValue()+" "+ res1.getValue());
					else	//"3/x"
						System.out.println(insts.indexOf(i)+":"+oper+" #"+res.getValue()+" ("+insts.indexOf(res1.getInstruction())+")");
				}
				else
				{
					if(res1.getType() == Type.number)	//"x/2"
						System.out.println(insts.indexOf(i)+":"+oper+" ("+insts.indexOf(res.getInstruction())+") "+ res1.getValue());
					else	//"y/x"
						System.out.println(insts.indexOf(i)+":"+oper+" ("+insts.indexOf(res.getInstruction())+")"+" ("+insts.indexOf(res1.getInstruction())+")");
				}
			
		}	
		return final_res;
	}
	
	void error(String s){
		System.out.println(s);
	}
	
	Result F(BasicBlock currentblock){
		Result res=new Result();
		int var_id=0;
		if(tt.getType() != TokenType.number)
		{
			if(tt.getType() == TokenType.openparenToken) //"("
			{
				Next();
				res=E(currentblock);
				if(tt.getType() == TokenType.closeparenToken)
					Next();
				else
					error("Syntax error : Missing ')'");
			}
			else if(tt.getType() == TokenType.ident)//if its a var
			{
				if(Result_cache.get(tt.getCharacters()).getType()==Type.arr){
					String var = tt.getCharacters();
					index = String2Id(tt.getCharacters());
					Next();
						int indexrelloc=0;
						Result arr=Result_cache.get(var);
						ArrayList<Integer> arrsize=arr.getArraySize();
						ArrayList<Result> arrindex= new ArrayList<Result> ();
						for(int i=0;i<arr.getArraySize().size();i++){
							Next();
							Result x=E(currentblock);
							arrindex.add(x);
							if(arr.getArraySize().size()!=1)
								Next();
							if(tt.getType()!= TokenType.closebracketToken)	//"]"
							{
								error("Syntax error   : ']' missing");
							}
							else
								Next();
						}
						boolean flagarray=false;//false means size is 1
						for(int i=0;i<arrsize.size()-1;i++){
							int j=(arrsize.size())-(arrsize.size()-i-1),mul=1;flagarray=true;
							while(j<arrsize.size()){
								mul*=arrsize.get(j);
								j++;
							}
							Result intsize = new Result(Type.number,mul);
							Instruction arrmulins = new Instruction("mul",arrindex.get(i),intsize);
							currentblock.inst_list.add(arrmulins);
							insts.add(arrmulins);				//add instruction to instruction list
							arrmulins.basicblock = currentblock; 
							arrmulins.block_id = BasicBlock.block_id;
							if(i%2-1==0){
								Result finalindex1 = new Result(Type.instruction,insts.get(insts.size()-2));
								Result finalindex2 = new Result(Type.instruction,arrmulins);
								Instruction arrfinalindexins = new Instruction("add",finalindex1,finalindex2);
								currentblock.inst_list.add(arrfinalindexins);
								insts.add(arrfinalindexins);				//add instruction to instruction list
								arrfinalindexins.basicblock = currentblock; 
								arrfinalindexins.block_id = BasicBlock.block_id;
							}
						}
						Instruction arrmulins=null;
						if(!flagarray){
							Result intsize = new Result(Type.number,4);
						    arrmulins = new Instruction("mul",intsize,arrindex.get(arrindex.size()-1));
							currentblock.inst_list.add(arrmulins);
							insts.add(arrmulins);				//add instruction to instruction list
							arrmulins.basicblock = currentblock; 
							arrmulins.block_id = BasicBlock.block_id;
						}
						else
						{
						Result finalindex = new Result(Type.instruction,insts.get(insts.size()-1));
						Instruction arrfinalindexins = new Instruction("add",arrindex.get(arrindex.size()-1),finalindex);
						currentblock.inst_list.add(arrfinalindexins);
						insts.add(arrfinalindexins);				//add instruction to instruction list
						arrfinalindexins.basicblock = currentblock; 
						arrfinalindexins.block_id = BasicBlock.block_id;
						
						Result arrrelloc = new Result(Type.instruction,arrfinalindexins);
						Result intsize = new Result(Type.number,4);
						arrmulins = new Instruction("mul",intsize,arrrelloc);
						currentblock.inst_list.add(arrmulins);
						insts.add(arrmulins);				//add instruction to instruction list
						arrmulins.basicblock = currentblock; 
						arrmulins.block_id = BasicBlock.block_id;
						}
						
						Instruction arraddfpins = new Instruction("add",Result_cache.get(var),Result_cache.get("FP"));
						currentblock.inst_list.add(arraddfpins);
						insts.add(arraddfpins);				//add instruction to instruction list
						arraddfpins.basicblock = currentblock;
						arraddfpins.block_id = BasicBlock.block_id;
						
						Result addares1=new Result(Type.instruction,arrmulins);
						Result addares2=new Result(Type.instruction,arraddfpins);
						Instruction arradda = new Instruction("adda",addares1,addares2);
						currentblock.inst_list.add(arradda);
						insts.add(arradda);				//add instruction to instruction list
						arradda.basicblock = currentblock;
						arradda.block_id = BasicBlock.block_id;
						
						Result arrload1=new Result(Type.instruction,arradda);
						Instruction arrload = new Instruction("load",arrload1);
						currentblock.inst_list.add(arrload);
						insts.add(arrload);				//add instruction to instruction list
						arrload.basicblock = currentblock;
						arrload.block_id = BasicBlock.block_id;
						res=new Result(Type.instruction,arrload);
						
					}
					else{
						var_id = String2Id(tt.getCharacters());
						if(currentblock.get_Sym_table().containsKey(var_id))
						{
							Result res1;
							Stack<Instruction> s = currentblock.get_Sym_table().get(var_id);	//stack for the variable
							if(currentblock.getType() != BlockType.ifelse)
							res1 = 	new Result(Type.instruction,(Instruction)s.peek());
							else											//if its in else block
							{
								int top = s.size()-1;
								while(top >0 && currentblock.getprevblock().getblockno() <= s.get(top).basicblock.getprevblock().block_id)
									top--;
								res1 = new Result(Type.instruction,(Instruction)s.get(top));
							}
							res = res1;
							//s.push(res1.getInstruction());
							Next();
						}
						else if(Result_cache.containsKey(tt.getCharacters())&&Sym_table.containsKey(var_id))
						{
							Result res1=new Result();
							Stack<Instruction> s = Sym_table.get(var_id);	//stack for the variable
							if(currentblock.getType() != BlockType.ifelse){
								if(!s.isEmpty()){
									res1 = 	new Result(Type.instruction,(Instruction)s.peek());
								}
								
							}
							else											//if its in else block
							{
								int top = s.size()-1;
								while(top >0 && currentblock.getprevblock().getblockno() <= s.get(top).basicblock.getprevblock().block_id)
									top--;
								res1 = new Result(Type.instruction,(Instruction)s.get(top));
							}
							res = res1;
							//s.push(res1.getInstruction());
							Next();
						}
						else{			//for initailizing with zero
							if(Result_cache.containsKey(tt.getCharacters())){
								res=new Result(Type.number,0);
								Next();
							}
							else
								throw new IllegalArgumentException("error:undefined variable");
							
						}
					}
					
			}
			else
			{
				res = E(currentblock);
				Next();
			}
			
		
		}
		else
		{
			res = num();
			Next();
		}
		return res;
	}
	
	Result num(){
		Result res;
		String sym;
		sym = tt.getCharacters();
		res = new Result(Type.number,Integer.parseInt(sym));
		//res = new Result(Type.number,sym);
		return res;
	}																																																																																																																			
}



