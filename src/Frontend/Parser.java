package Frontend;

import java.util.*;

import Frontend.TokenType;
import Frontend.Result.Type;


public class Parser{
	Token tt;
	private Scanner scanner;
	//private HashMap<Integer,Instruction> Sym_table;	//mapping b/w index and instruction
	private HashMap<String,Result> Result_cache;	//for storing Results
	private ArrayList<Instruction> insts;			//list for instructions
	private HashMap<Integer,Stack<Instruction>> Sym_table;		//stack per variable
	private HashMap<String,ArrayList<Result>> Function_param;
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
	
	public int compute()
	{
		int res=-1;
		//System.out.println("inside copute");
		if(tt.getType() == TokenType.commentToken) //if first line is a comment
		Next();
		if(tt.getType() != TokenType.mainToken || tt.getType() == TokenType.errorToken)	//if main is not the first token,error
			error("Syntax error : Missing 'main'");
		else								 //after parsing main
		{
			currentblock = new BasicBlock(); // main block
			
			while(tt.getType()!= TokenType.periodToken)	//"." at the end
			{
				Next();
				if(tt.getType() == TokenType.varToken || tt.getType() == TokenType.arrToken)	//if its a var
					var_decl(currentblock);
			
				if(tt.getType() == TokenType.funcToken || tt.getType() == TokenType.procToken)	//if its a function declaration
					func_decl();
				if(tt.getType() == TokenType.beginToken) //"{"
				{
					
					Next();
					stat_seq(currentblock);							//statSequence
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
			
		return res;	
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
	
	public void func_decl()
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
		while(tt.getType() != TokenType.endToken)	//"}"
		{
			res = statement(currentblock);
		}
		return res;
	}
	
	public Result statement(BasicBlock currentblock)
	{
		Result res=new Result();
		
		if(tt.getType() == TokenType.letToken)	//let 
		{
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
			res=ifStatement(currentblock);
			Next();
		}
		if(tt.getType() == TokenType.whileToken)	//while
		{
			res=whileStatement(currentblock);
			Next();
		}
		if(tt.getType() == TokenType.returnToken)	//return
		{
			res = E();
		}
		if(tt.getType() == TokenType.semiToken)	//";"
			Next();
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
				x = E();
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
				
				x = E();
				//if(x.getType() == Type.number)			//e.g. let x <- 51;
				//{
					Instruction i = new Instruction("move",x, Result_cache.get(var));
					insts.add(i);				//add instruction to instruction list
					
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
						System.out.println(insts.indexOf(i)+":"+"move #"+ x.getValue() + " " + var);
					}
					else if(x.getType() == Type.instruction)
					{
						System.out.println(insts.indexOf(i)+":"+"move ("+insts.indexOf(x.getInstruction())+") "+ var);
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
	
	public Result ifStatement(BasicBlock currentblock)
	{
		int if_count=1;
		Result res=new Result();
		Stack if_stack = new Stack();

		while(if_count != 0)
		{
			if(tt.getType() == TokenType.ifToken)		//nested if
			{
				if_count++;
				Next();

				Result op1 = E();

				if(isrelop()) {
					Result cond = new Result(Type.condition,tt.getType());
					Next();
					Result op2 = E();
					Instruction i = new Instruction("cmp",op1,op2);
					insts.add(i);
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
						if_stack.push(ii);
					default:
						break;
					}
				}
				else{
					error("Syntax error: Missing condition after 'if'");
					return res;
				}
				if(tt.getType() == TokenType.thenToken)	//"then"
				{
					Next();
					Result rr = stat_seq(currentblock);
				}
			}
			Next();
			if(tt.getType() == TokenType.elseToken)	//else
			{
				Next();
				Result else_res = stat_seq(currentblock);
				Next();
			}
			if(tt.getType() == TokenType.fiToken)	//fi
			{
				if_count--;
				Next();
			}
			//}
			/*if (tt.getType()==TokenType.ident && tt.getCharacters()!=null){

				int var_inst_id1=String2Id(tt.getCharacters());

				Result oper1= new Result(Type.instruction,Sym_table.get(var_inst_id1));

				operands.add(oper1);

				Next();

				Result cond = new Result(Type.condition,tt.getType());

				Next();

				if(tt.getType()==TokenType.number){	//e.g. if y==2

					Result oper2= new Result(Type.number,tt.getValue());
					operands.add(oper2);
				}
				else if(tt.getType()==TokenType.ident){	//e.g. if y==x

					int var_inst_id2=String2Id(tt.getCharacters());

					Result oper2= new Result(Type.instruction,Sym_table.get(var_inst_id2));

					operands.add(oper2);
				}
			// add to instruction for branch command (cond, operands)
			}
			else
				System.out.println("variable in if not assigned yet");
			}*/
		}
		return res;
	}
	
	public Result whileStatement(BasicBlock currentblock)
	{
		Result res=new Result();
		//ToDo
		Next();

		ArrayList<Result> operands=new ArrayList<Result>();

		if (tt.getType()==TokenType.ident && tt.getCharacters()!=null){
			int var_inst_id1=String2Id(tt.getCharacters());
			Instruction i = Sym_table.get(var_inst_id1).pop();
			Result oper1= new Result(Type.instruction,i);
			Sym_table.get(var_inst_id1).push(i);
			operands.add(oper1);

			Next();

			Result cond = new Result(Type.condition,tt.getType());

			Next();
			if(tt.getType()==TokenType.number){

				Result oper2= new Result(Type.number,tt.getValue());

				operands.add(oper2);

			}

			else if(tt.getType()==TokenType.ident){

				int var_inst_id2=String2Id(tt.getCharacters());
				Instruction i2 = Sym_table.get(var_inst_id2).pop();
				Result oper2= new Result(Type.instruction,i2);

				operands.add(oper2);
				Sym_table.get(var_inst_id1).push(i2);

			}
			// add to instruction for branch command (cond, operands)
		}
		else
			System.out.println("variable in if not assigned yet");
		return res;
	}
	
	
	public Result E(){
		//int res=0;
		Result res;
		Result final_res;
		final_res = res = T();
		Result res1 = new Result();
		String oper;
		while(tt.getType() == TokenType.plusToken || tt.getType()==TokenType.minusToken)
		{
			if( tt.getType() == TokenType.plusToken)
				oper = new String("add");
		
			else //if (tt.getType() == TokenType.minusToken){
				oper = new String("sub");
			
			Next();
			res1 = T();
			
			Instruction i = new Instruction(oper,res,res1);
			insts.add(i);
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
					System.out.println(insts.indexOf(i)+":"+oper+" ("+insts.indexOf(res.getInstruction())+") "+ res1.getValue());
				else	//"y+x"
					System.out.println(insts.indexOf(i)+":"+oper+" ("+insts.indexOf(res.getInstruction())+")"+" ("+insts.indexOf(res1.getInstruction())+")");
			}
		}
		return final_res;
	}
	
	//1+2*3-4
	Result T(){
		Result res;
		Result res1 = new Result();
		Result final_res;
		final_res = res = F();
		Token tmp;
		while(tt.getType() == TokenType.timesToken || tt.getType()== TokenType.divToken)
		{	
			tmp=tt;
			String oper;
			Next();
			res1 = F();
			if(tmp.getType() == TokenType.timesToken)	
				oper = "mul";
			else
				oper = "div";
			
				Instruction i = new Instruction(oper,res,res1);
				insts.add(i);
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
			
		/*	else //if (tmp.getType() == TokenType.divToken)
			{
				oper = "div";
				Instruction i = new Instruction("div",res,res1);
				insts.add(i);
				if(res.getType() == Type.number)
				{
					if(res1.getType() == Type.number)	//"3/2"
						System.out.println(insts.indexOf(i)+":"+"div #"+res.getValue()+" "+ res1.getValue());
					else	//"3/x"
						System.out.println(insts.indexOf(i)+":"+"div #"+res.getValue()+" "+insts.indexOf(res1.getInstruction()));
				}
				else
				{
					if(res1.getType() == Type.number)	//"x/2"
						System.out.println(insts.indexOf(i)+":"+"div ("+insts.indexOf(res.getInstruction())+") "+ res1.getValue());
					else	//"y/x"
						System.out.println(insts.indexOf(i)+":"+"div ("+insts.indexOf(res.getInstruction())+")"+" ("+insts.indexOf(res1.getInstruction())+")");
				}
				
			}
*/
		}	
		return final_res;
	}
	
	void error(String s){
		System.out.println(s);
	}
	
	Result F(){
		Result res=new Result();
		int var_id=0;
		if(tt.getType() != TokenType.number)
		{
			if(tt.getType() == TokenType.openbracketToken) //"("
			{
				Next();
				res=E();
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
					Stack<Instruction> s = Sym_table.get(var_id);
					
					res1 = 	new Result(Type.instruction,(Instruction)s.pop());
					res = res1;
					s.push(res1.getInstruction());
					Next();
				}
			}
			else
			{
				res = E();
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



