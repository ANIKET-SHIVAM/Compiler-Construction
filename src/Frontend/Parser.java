package Frontend;

import java.util.*;

import Frontend.TokenType;
import Frontend.Result.Type;


public class Parser{
	Token tt;
	private Scanner scanner;
	private HashMap<Integer,Instruction> Sym_table;	//mapping b/w index and instruction
	private HashMap<String,Result> Result_cache;
	private ArrayList<Instruction> insts;
	private HashMap<String,ArrayList<Result>> Function_param;
	public char sym;
	public int index=0;
	

	public Parser(String filename){
		scanner = new Scanner(filename);
		Result_cache = new HashMap<String,Result>();
		insts=new ArrayList<Instruction>();
		Sym_table = new HashMap<Integer,Instruction>();
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
		System.out.println("inside copute");
		if(tt.getType() == TokenType.commentToken) //if first line is a comment
		Next();
		if(tt.getType() != TokenType.mainToken || tt.getType() == TokenType.errorToken)	//if main is not the first token,error
			error("Syntax error : Missing 'main'");
		else									//after parsing main
		{
			while(tt.getType()!= TokenType.periodToken)	//"." at the end
			{
				Next();
				if(tt.getType() == TokenType.varToken || tt.getType() == TokenType.arrToken)	//if its a var
					var_decl();
			
				if(tt.getType() == TokenType.funcToken || tt.getType() == TokenType.procToken)	//if its a function declaration
					func_decl();
				if(tt.getType() == TokenType.beginToken) //"{"
				{
					
					Next();
					stat_seq();							//statSequence
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
			var_decl();
		}
		stat_seq();

	}
	public void formal_Param(String function_name,ArrayList<Result> param_list){
		Function_param.put(function_name, param_list);
	}
	
	public Result stat_seq()
	{
		Result res=new Result();
		while(tt.getType() != TokenType.endToken)	//"}"
		{
			res = statement();
		}
		return res;
	}
	
	public Result statement()
	{
		Result res=new Result();
		
		if(tt.getType() == TokenType.letToken)	//let 
		{
			res = assignment();
			Next();
		}
		if(tt.getType() == TokenType.callToken)	//call
		{
			res = funcCall();
			Next();
		}
		if(tt.getType() == TokenType.ifToken)	//if
		{
			res=ifStatement();
			Next();
		}
		if(tt.getType() == TokenType.whileToken)	//while
		{
			res=whileStatement();
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
	
	public Result assignment()		//"let"
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
					Sym_table.put(index,i);
					if(x.getType() ==Type.constant)
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
	
	public Result funcCall()
	{
		Result res=new Result();
		//ToDo
		
		return res;
	}
	public boolean isrelop(){
		return (tt.getType()==TokenType.eqlToken ||tt.getType()==TokenType.neqToken ||tt.getType()==TokenType.lssToken ||tt.getType()==TokenType.geqToken || tt.getType()==TokenType.leqToken ||tt.getType()==TokenType.gtrToken);	
	}
	
	public Result ifStatement()
	{
		Result res=new Result();
		Next();
		ArrayList<Result> operands=new ArrayList<Result>();
	
		
		if (tt.getType()==TokenType.ident && tt.getCharacters()!=null){
			int var_inst_id1=String2Id(tt.getCharacters());
			Result oper1= new Result(Type.instruction,Sym_table.get(var_inst_id1));
			operands.add(oper1);
			
			Next();
			Result cond = new Result(Type.condition,tt.getType());
			
			Next();
			if(tt.getType()==TokenType.number){
				Result oper2= new Result(Type.constant,tt.getValue());
				operands.add(oper2);
			}
			else if(tt.getType()==TokenType.ident){
				int var_inst_id2=String2Id(tt.getCharacters());
				Result oper2= new Result(Type.instruction,Sym_table.get(var_inst_id2));
				operands.add(oper2);
			}
			
			// add to instruction for branch command (cond, operands)
		}
		else
			System.out.println("variable in if not assigned yet");
		
		return res;
	}
	
	public Result whileStatement()
	{
		Result res=new Result();
		Next();
		ArrayList<Result> operands=new ArrayList<Result>();
	
		
		if (tt.getType()==TokenType.ident && tt.getCharacters()!=null){
			int var_inst_id1=String2Id(tt.getCharacters());
			Result oper1= new Result(Type.instruction,Sym_table.get(var_inst_id1));
			operands.add(oper1);
			
			Next();
			Result cond = new Result(Type.condition,tt.getType());
			
			Next();
			if(tt.getType()==TokenType.number){
				Result oper2= new Result(Type.constant,tt.getValue());
				operands.add(oper2);
			}
			else if(tt.getType()==TokenType.ident){
				int var_inst_id2=String2Id(tt.getCharacters());
				Result oper2= new Result(Type.instruction,Sym_table.get(var_inst_id2));
				operands.add(oper2);
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
		res = T();
		while(tt.getType() == TokenType.plusToken || tt.getType()==TokenType.minusToken)
		{
				if( tt.getType() == TokenType.plusToken){
				Next();
				res = T();
			}
			else if (tt.getType() == TokenType.minusToken){	
				Next();
				res = T();
			}
			
		}
		return res;
	}
	
	//1+2*3-4
	Result T(){
		Result res;
		Result res1 = new Result();
		Result final_res;
		final_res = res = F();
		while(tt.getType() == TokenType.timesToken || tt.getType()== TokenType.divToken)
		{	
			Next();
			res1 = F();
			if(tt.getType() == TokenType.timesToken)
			{
				Instruction i = new Instruction("mul",res,res1);
				insts.add(i);
				final_res = new Result(Type.instruction,i);
				if(res.getType() == Type.constant)
				{
					if(res1.getType() == Type.constant)	//"3/2"
						System.out.println(insts.indexOf(i)+":" +"mul #"+res.getValue()+" "+ res1.getValue());
					else	//"3/x"
						System.out.println(insts.indexOf(i)+":"+"mul #"+res.getValue()+" "+insts.indexOf(res1.getInstruction()));
				}
				else
				{
					if(res1.getType() == Type.constant)	//"x/2"
						System.out.println(insts.indexOf(i)+":"+"mul ("+insts.indexOf(res.getInstruction())+") "+ res1.getValue());
					else	//"y/x"
						System.out.println(insts.indexOf(i)+":"+"mul ("+insts.indexOf(res.getInstruction())+")"+" ("+insts.indexOf(res1.getInstruction())+")");
				}
			}
			else if (tt.getType() == TokenType.divToken)
			{
				Instruction i = new Instruction("div",res,res1);
				insts.add(i);
				if(res.getType() == Type.constant)
				{
					if(res1.getType() == Type.constant)	//"3/2"
						System.out.println(insts.indexOf(i)+":"+"div #"+res.getValue()+" "+ res1.getValue());
					else	//"3/x"
						System.out.println(insts.indexOf(i)+":"+"div #"+res.getValue()+" "+insts.indexOf(res1.getInstruction()));
				}
				else
				{
					if(res1.getType() == Type.constant)	//"x/2"
						System.out.println(insts.indexOf(i)+":"+"div ("+insts.indexOf(res.getInstruction())+") "+ res1.getValue());
					else	//"y/x"
						System.out.println(insts.indexOf(i)+":"+"div ("+insts.indexOf(res.getInstruction())+")"+" ("+insts.indexOf(res1.getInstruction())+")");
				}
				
			}

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
					res1 = new Result(Type.instruction,Sym_table.get(var_id));
					res = res1;
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
		res = new Result(Type.constant,Integer.parseInt(sym));
		
		return res;
	}																																																																																																																			
}



