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
	public static ArrayList<Instruction> insts;			//list for instructions
	private HashMap<Integer,Stack<Instruction>> Sym_table;		//stack per variable
	private HashMap<String,ArrayList<Result>> Function_param;
	public Stack<Instruction> if_stack = new Stack<Instruction>();
	public Stack<Instruction> while_stack = new Stack<Instruction>();
	public char sym;
	public int index=0;
	public BasicBlock currentblock;

	public Parser(String filename){
		scanner = new Scanner(filename);				//initialize scanner
		Result_cache = new HashMap<String,Result>();	//initialize result_cache
		insts=new ArrayList<Instruction>();				//initialize instruction list
		//Sym_table = new HashMap<Integer,Instruction>();	//initialize symbol table
		Sym_table = new HashMap<Integer,Stack<Instruction>>();		//initialize per variable stack	
		tt = scanner.getToken();
	}
	
	void Next()
	{
		tt = scanner.getToken();
	}

	public int String2Id(String s){
	
		int res=0;
		res = scanner.var_cache.get(s);
		return res;
	}
	
	public String IdtoString(int index)
	{
		//String o = new String();
		for (String o : scanner.var_cache.keySet()) {
		      if (scanner.var_cache.get(o).equals(index)) {
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
			currentblock = new BasicBlock(); // main block
			BasicBlock.mainblock=currentblock;
			System.out.println("Basic Block: "+ BasicBlock.block_id);
			BasicBlock.block_id++;
			while(tt.getType()!= TokenType.periodToken)	//"." at the end
			{
				Next();
				if(tt.getType() == TokenType.varToken || tt.getType() == TokenType.arrToken)	//if its a var
					var_decl(currentblock);
				
				if(tt.getType() == TokenType.funcToken || tt.getType() == TokenType.procToken)	//if its a function declaration
					func_decl(currentblock);
				if(tt.getType() == TokenType.beginToken) //"{"
				{
					while(tt.getType() != TokenType.endToken){
						//Next();
						stat_seq(currentblock);							//statSequence
					}
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
	
	public void var_decl(BasicBlock currentblock)    //TODO: take care of scope
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
				while(tt.getType() != TokenType.ident)
				{
					if(tt.getType() == TokenType.openbracketToken)	//array [
					{
						Next();
						if(tt.getType() == TokenType.number)	//array [ 4
						{
							num+=tt.getValue();					//num = 4
						}
						
						Next();
						if(tt.getType() != TokenType.closebracketToken)
						{
							Token.checkToken("");
							error("Syntax error : Missing ']' in array declaration");
							return;
						}
							
					}
					Next();
				}
				if(tt.getType() == TokenType.ident)
				{
					s1 = tt.getCharacters();
					Result x = new Result(Type.arr,s1,num);
					Result_cache.put(s1,x);
				}
				Next();
			}
			 
		}
			
	}
	
	public void func_decl(BasicBlock currentblock)
	{	
		String funcname = null;
		Next();
		if(tt.getType() == TokenType.ident)
		{
			funcname = tt.getCharacters();
			Result x = new Result(Type.function,funcname);
			Result_cache.put(funcname,x); //store function name in hash map
		}
		else
			System.out.println("error: function declaration");
		Next();//for (
		if (tt.getType()==TokenType.openparenToken)
		{	Next();
			ArrayList<Result> param_list = new ArrayList<Result>();
			while(tt.getType()!=TokenType.closeparenToken)
			{	
				Next();//to skip comma
				String ss = tt.getCharacters();
				Result x = new Result(Type.param,ss);
				param_list.add(x);
				Result_cache.put(ss,x); //store function name in hash map
				Next();
			}
			formal_Param(funcname,param_list);
		}
		Next();
		if(tt.getType() == TokenType.varToken){
			var_decl(currentblock);
		}
		stat_seq(currentblock);

	}
	
	public void formal_Param(String function_name,ArrayList<Result> param_list){
		Function_param.put(function_name, param_list);
	}
	
	public Result stat_seq(BasicBlock currentblock)
	{
		Result res=new Result();
		//while(tt.getType() != TokenType.endToken)	//"}"
		//{
			res = statement(currentblock);
		//}
		return res;
	}
	
	public Result statement(BasicBlock currentblock)
	{
		BasicBlock bb;
		Result res=new Result();
		if(tt.getType() == TokenType.semiToken)	//";"
			Next();
		if(tt.getType() == TokenType.beginToken) //"{"
			Next();
		if(tt.getType() == TokenType.letToken)	//let 
		{System.out.println("asdasdasd"+currentblock.getblockno());
			res = assignment(currentblock);
			Next();
		}
		if(tt.getType() == TokenType.callToken)	//call
		{
			res = funcCall(currentblock);
			Next();
		}
		if(tt.getType() == TokenType.ifToken)	//if
		{
			bb=ifStatement(currentblock);
			Next();
		}
		if(tt.getType() == TokenType.whileToken)	//while
		{
			res=whileStatement(currentblock);
			//Next();
		}
		if(tt.getType() == TokenType.returnToken)	//return
		{
			res = E(currentblock);
		}
		
		return res;
	}
	
	public Result assignment(BasicBlock currentblock)		//"let"
	{
		//int res=0;
		int index=0;
		String var;
		Result x = new Result();
		Next();
		if(tt.getType() == TokenType.ident)				//if its a var
		{
			var = tt.getCharacters();
			index = String2Id(tt.getCharacters());
			Next();
			if(tt.getType() == TokenType.openbracketToken)	//"["
			{
				x = E(currentblock);
				Next();
			
				if(tt.getType()!= TokenType.closebracketToken)	//"]"
				{
					error("Syntax error: ']' missing");
				}
				else
					Next();
			}
			if(tt.getType() == TokenType.becomesToken) //"<-"
			{
				Next();
				
				x = E(currentblock);
				//if(x.getType() == Type.number)			//e.g. let x <- 51;
				//{
					Instruction i = new Instruction("move",x, Result_cache.get(var));
					currentblock.inst_list.add(i);
					insts.add(i);				//add instruction to instruction list
					i.basicblock = currentblock;
					i.block_id = BasicBlock.block_id;
					Instruction ii;
					
					//Fixup jump location of if
					if(currentblock.getType() == BlockType.follow || currentblock.getType() == BlockType.ifelse)	//if we are in "else" block
					{	
						if(currentblock.getType() == BlockType.ifelse)
							ii = if_stack.pop();
						else 
							ii = while_stack.pop();
						int len = ii.getOperands().size() - 1;	//index of last operand
						Result res = ii.getOperands().get(len);
						Instruction fix_loc = res.getFixupLocation();
						fix_loc = i;
						if(currentblock.getType() == BlockType.ifelse)
						{
							if(x.getType() != Type.number)
								System.out.println("Fixup loc for 'if' is :"+insts.indexOf(x.getInstruction()));
							else
								System.out.println("Fixup loc for 'if' is :"+insts.indexOf(i));
						}
						else
						{
							if(x.getType() == Type.number)
								System.out.println("Fixup loc for 'while' is :"+insts.indexOf(i));
							else
								System.out.println("Fixup loc for 'while' is :"+insts.indexOf(x.getInstruction()));
							//if_stack.push(ii);
						}
						
					}
					if(!Sym_table.containsKey(index))	//if sym_table is empty
					{
						Stack<Instruction> ss = new Stack<Instruction>();
						ss.push(i);
						Sym_table.put(index, ss);
					}
					else										//if the entry is present
					{
						Sym_table.get(index).push(i);	//push new value on stack
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
				//}
			}
		}
		
		return x;
	}
	
	public Result funcCall(BasicBlock currentblock)
	{
		Result res=new Result();
		//ToDo
		
		return res;
	}
	
	public boolean isrelop(){
		return (tt.getType()==TokenType.eqlToken ||tt.getType()==TokenType.neqToken ||tt.getType()==TokenType.lssToken ||tt.getType()==TokenType.geqToken || tt.getType()==TokenType.leqToken ||tt.getType()==TokenType.gtrToken);	
	}
	
	public BasicBlock ifStatement(BasicBlock currentblock)
	{
		int if_count=1;
		int else_flag=0;
		BasicBlock if_block,else_block,phi_block;
		BasicBlock nested_if_block = currentblock;
		Result res=new Result();
		
		//if(tt.getType() == TokenType.ifToken)		//nested if
			//{
				if(if_count != 1)
					if_count++;
				nested_if_block=else_block=phi_block=if_block = currentblock.createIfTrue();
				
				
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
					Result fix_res = new Result();
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
						currentblock.inst_list.add(i);
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
					if(tt.getType() == TokenType.ifToken)
					{	
						nested_if_block=ifStatement(if_block);
						if(tt.getType() == TokenType.semiToken)
							Next();
						//Next();
					}
					else
					{	while((tt.getType() != TokenType.elseToken)&&(tt.getType() != TokenType.fiToken)){
							Result rr = stat_seq(if_block);
						}
					}
				}
			//while(!if_stack.isEmpty()){
			if(tt.getType() == TokenType.elseToken)	//else
			{
				else_flag=1;
				else_block = currentblock.createElse();
				System.out.println("Basic Block: "+ BasicBlock.block_id+"\n");
				BasicBlock.block_id++;
				Next();
				while(tt.getType() != TokenType.fiToken){
					Result else_res = stat_seq(else_block);
				}	
			}
			if(else_flag==0)
				Next();
			
			if(tt.getType() == TokenType.fiToken)	//fi
			{
				String var;
				int i=1;
				phi_block = nested_if_block.createjoin();
				if(else_flag==1)
					else_block.setjoin(phi_block);
				else
					currentblock.setjoin(phi_block);
				System.out.println("\nJoin block: "+ BasicBlock.block_id+"\n");
				BasicBlock.block_id++;
				for(i=1;i<=Sym_table.size();i++)	//iterate thru each var and check if it has more than 1 value in its stack
				{
					if(Sym_table.get(i).size()>1)
					{
							var = IdtoString(i);//Todo
							Instruction i1 = Sym_table.get(i).pop();
							Instruction i2 = Sym_table.get(i).pop();
							Result oper1= new Result(Type.instruction,i1);
							Result oper2= new Result(Type.instruction,i2);
							Instruction ii = new Instruction("phi",var,oper1,oper2);
							ii.basicblock=phi_block;
							insts.add(ii);
							phi_block.inst_list.add(ii);
							Sym_table.get(i).push(ii);
							System.out.println(insts.indexOf(ii)+":"+"phi "+ var +"_"+insts.indexOf(ii)+ " (" + insts.indexOf(i1)+") " + "(" + insts.indexOf(i2) + ")");
					}
				
				}
				BasicBlock.block_id++;	
				Next();
				else_flag=0;
			}

		//}
			//currentblock=phi_block;
		return phi_block;
	}
	
	public Result whileStatement(BasicBlock currentblock)
	{
		int while_count=1;
		int counter=0;
		Result res=new Result();
		

		while(while_count != 0)
		{
			BasicBlock while_block = currentblock.createWhile();
			BasicBlock follow_block = currentblock.createfollow();
			currentblock=currentblock.getnextblock();			// while block for conditions and phi inst
			if(tt.getType() == TokenType.whileToken)		//nested while
			{
				if(while_count != 1)
					while_count++;
				
			//	System.out.println("created while block");
				System.out.println("Basic Block: "+ BasicBlock.block_id+"\n");
				BasicBlock.block_id++;
				
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
					Result fix_res = new Result();
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
					return res;
				}
				if(tt.getType() == TokenType.doToken)	//"do"
				{	
					BasicBlock do_block = currentblock.createdo();
					Next();
					System.out.println("Basic Block: "+ BasicBlock.block_id+"\n");
					BasicBlock.block_id++;
					while(tt.getType() != TokenType.odToken){
						Result rr = stat_seq(do_block);
					}
					int phi_counter=0;
					for(counter=1;counter<Sym_table.size();counter++)	//iterate thru each var and check if it has more than 1 value in its stack
					{
						if(Sym_table.get(counter).size()>1)
						{
								String var = IdtoString(counter);//Todo
								
								Instruction i1 = Sym_table.get(counter).peek();
								int top = Sym_table.get(counter).size()-2;			//second element from top
								while(Sym_table.get(counter).elementAt(top).block_id >= i1.block_id)
									top--;
								Instruction i2 = Sym_table.get(counter).elementAt(top);
								Result oper1= new Result(Type.instruction,i1);
								Result oper2= new Result(Type.instruction,i2);
								Instruction ii = new Instruction("phi",var,oper1,oper2);
								ii.basicblock=while_block;
								insts.add(ii);
								while_block.inst_list.add(phi_counter++, ii);
								Sym_table.get(counter).push(ii);
								System.out.println(insts.indexOf(ii)+":"+"phi "+ var +"_"+insts.indexOf(ii)+ " (" + insts.indexOf(i1)+") " + "(" + insts.indexOf(i2) + ")");
						}
					
					}
					
					Instruction jump_ins = while_block.inst_list.get(0);
					Result jump_res = new Result(Type.instruction,jump_ins);
					Instruction branch_inst = new Instruction("bra",jump_res);
					branch_inst.basicblock = currentblock;
					branch_inst.block_id = BasicBlock.block_id;
					insts.add(branch_inst);
				}
				else 
					System.out.println("no do token after while");
			}
			
			if(tt.getType() == TokenType.odToken)	//od
			{	
				//TODO: join block
				while_count--;
				Next();
				stat_seq(follow_block);
				
			}
			
		}
		currentblock=currentblock.getfollowblock();
		System.out.println("Basic Block: "+ BasicBlock.block_id+"\n");
		BasicBlock.block_id++;
		return res;
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
			res1 = T(currentblock);
			
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
			res1 = F(currentblock);
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
			if(tt.getType() == TokenType.openbracketToken) //"("
			{
				Next();
				res=E(currentblock);
				if(tt.getType() == TokenType.closebracketToken)
					Next();
				else
					error("Syntax error : Missing ')'");
			}
			else if(tt.getType() == TokenType.ident)//if its a var
			{
				var_id = String2Id(tt.getCharacters());
				if(Sym_table.containsKey(var_id))
				{
					Result res1;
					Stack<Instruction> s = Sym_table.get(var_id);	//stack for the variable
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



