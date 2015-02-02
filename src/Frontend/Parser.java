package Frontend;

import java.util.*;

import Frontend.Result.Type;


public class Parser{
	Token tt;
	private Scanner scanner;
	private HashMap<String,Result>Result_cache;
	private HashMap<String,ArrayList<Result>> Function_param;
	public char sym;
	public int index=0;
	

	public Parser(String filename){
		scanner = new Scanner(filename);
		Result_cache = new HashMap<String,Result>();
		tt = scanner.getToken();
	}
	
	void Next()
	{
		tt = scanner.getToken();
	}

	public int compute()
	{
		int res=-1;
		
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
					res = stat_seq();							//statSequence
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
	{	String funcname = null;
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
	
	public int stat_seq()
	{
		int res=0;
		res = statement();
		return res;
	}
	
	public int statement()
	{
		int res=0;
		if(tt.getType() == TokenType.letToken)
		{
			res = assignment();
			Next();
		}
		if(tt.getType() == TokenType.callToken)
		{
			res = funcCall();
			Next();
		}
		if(tt.getType() == TokenType.ifToken)
		{
			res=ifStatement();
			Next();
		}
		if(tt.getType() == TokenType.whileToken)
		{
			res=whileStatement();
			Next();
		}
		if(tt.getType() == TokenType.returnToken)
		{
			res = E();
		}
		return res;
	}
	
	public int assignment()		//"let"
	{
		int res=0;
		Next();
		if(tt.getType() == TokenType.ident)
		{
			Next();
			if(tt.getType() == TokenType.openbracketToken)	//"["
			{
				res = E();
				Next();
			}
			if(tt.getType()!= TokenType.closebracketToken)//"]"
			{
				error("Syntax error: ']' missing");
			}
			Next();
			if(tt.getType() == TokenType.becomesToken) //"<-"
			{
				Next();
				res = E();
			}
		}
		
		return res;
	}
	
	public int funcCall()
	{
		int res=0;
		//ToDo
		
		return res;
	}
	public boolean isrelop(){
		return (tt.getType()==TokenType.eqlToken ||tt.getType()==TokenType.neqToken ||tt.getType()==TokenType.lssToken ||tt.getType()==TokenType.geqToken || tt.getType()==TokenType.leqToken ||tt.getType()==TokenType.gtrToken);	
	}
	
	public int ifStatement()
	{
		int res=0;
		
		return res;
	}
	
	public int whileStatement()
	{
		int res=0;
		//ToDo
		return res;
	}
	
	
	public int E(){
		int res=0;
		
		res = T();
		while(tt.getType() == TokenType.plusToken || tt.getType()==TokenType.minusToken)
		{
				if( tt.getType() == TokenType.plusToken){
				Next();
				res += T();
			}
			else if (tt.getType() == TokenType.minusToken){	
				Next();
				res -= T();
			}
			
		}
		return res;
	}
	
	//1+2*3-4
	int T(){
		int res=0;
		
		res=F();
		while(tt.getType() == TokenType.timesToken || tt.getType()== TokenType.divToken)
		{	
			if(tt.getType() == TokenType.timesToken)
			{
				Next();
				res *= F();
			}
			else if (tt.getType() == TokenType.divToken)
			{
				Next();
				res /= F();
			}

		}	
		return res;
	}
	
	void error(String s){
		System.out.println(s);
	}
	
	int F(){
		int res=0;
		
		if(tt.getType() != TokenType.number)
		{
			Next();
			res=E();
			if(tt.getType() == TokenType.closebracketToken)
				Next();
			else
				error("Syntax error : Missing ')'");
		}
		else
		{
			res = num();
			Next();
		}
		return res;
	}
	
	int num(){
		int res=0;
		String sym;
		sym = tt.getCharacters();
		res = Integer.parseInt(sym);
		
		return res;
	}
																																																																																																																				
}



