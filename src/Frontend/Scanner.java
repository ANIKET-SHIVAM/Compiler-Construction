package Frontend;
import java.io.*;
import java.util.*;

import java.util.ArrayList;
import java.util.HashMap;

import Frontend.Token;

public class Scanner {
	private char inputSym;
	//public int getSym(){return int a;};
	private FileReader fr;
	//public int number;
	public static int id;
	public static ArrayList<String> ident;
	public HashMap<String,Integer> var_cache;
	
	private void Next(){
		this.inputSym=this.fr.getSym();
	}
	private void Prev(){
		this.inputSym = this.fr.getPrevSym();
	}
	public Scanner(String filename){
		try {
			this.fr=new FileReader(filename);
			var_cache= new HashMap<String,Integer>();
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.inputSym= fr.getSym();
		id=0;
	}
	public Token getToken(){
		//Token token=null;
		//char ch;String characters=null;
		if(inputSym == '\0')
		{	//return Token;
		//if(inputSym=='|'){
			return Token.checkToken("|");
		}
		//if((Comment())!=null) {
				if(inputSym == '/'){
				Comment();
				}

				if(inputSym == 'm')
					return check_main();
				NoUseCh();
		
		
		
		if(inputSym >='0' && inputSym<= '9') {
			return isNumber();}
	
		if((inputSym >= 'a' || inputSym >='A') && (inputSym <= 'z' || inputSym <='Z')) {
			return isIdentOrKeyword();
			}
		
		if(inputSym == '/'){
			Comment();
			}
		
		switch(inputSym){
		case '*':
			Next();
			return Token.checkToken("*");
			//break;
		/*case '/':
			return(Comment());*/
		case '+':
			Next();return Token.checkToken("+");
			//break;
		case '-':
			Next();
			return Token.checkToken("-");
			//break;
		case '=':
			Next();
			if(inputSym=='=')
			{
				//Next();
				return Token.checkToken("==");
			}
			else 
			{
				NoUseCh();
				
//				System.out.println("Syntax Error");
				return Token.checkToken(Character.toString(inputSym));
			}
		case '!':
			Next();if(inputSym=='='){Next();return Token.checkToken("!=");}
					else {System.out.println("Syntax Error");return null;}
		case '<':
			Next();if(inputSym=='='){Next();return Token.checkToken("<=");}
					else if(inputSym=='-'){Next();return Token.checkToken("<-");}
					else {Next();return Token.checkToken("<");}
		case '>':
			Next();if(inputSym=='='){Next();return Token.checkToken(">=");}
					else {Next();return Token.checkToken(">");}
		case '.':
			Next();return Token.checkToken(".");	
		case ',':
			Next();return Token.checkToken(",");
		case '[':
			Next();return Token.checkToken("[");
		case ']':
			Next();return Token.checkToken("]");
		case '(':
			Next();return Token.checkToken("(");
		case ')':
			Next();return Token.checkToken(")");
		case ';':
			Next();return Token.checkToken(";");
		case '{':
			Next();return Token.checkToken("{");	
		case '}':
			Next();return Token.checkToken("}");
		}
		System.out.println("Syntax Error");return null;
	}
	
	
	public Token isNumber(){
		int number=0;int i=0;
		int digit=0;
		while(inputSym>='0' && inputSym<='9'){
		digit = Character.getNumericValue(inputSym);
		number=10*number + digit;
		Next();i++;
		}
		if (i>0)
			return Token.checkToken(Integer.toString(number));
		else
			return null;
		}
	
	public void NoUseCh(){
		while(inputSym=='\t'||inputSym=='\n'||inputSym=='\r'||inputSym==' '){
			Next();
		}
	}
	
	public Token isIdentOrKeyword(){
		StringBuffer strbfr=new StringBuffer();
		Token tt;
		//if(Character.isLetter(inputSym)){
		while (Character.isLetterOrDigit(inputSym)){
			strbfr.append(inputSym);
			Next();
		}
		String str = strbfr.toString();
		
		tt=Token.checkToken(str);
		if(tt.getType() == TokenType.ident)
		{
			if(!var_cache.containsKey(str))	//entry for variable is not present then put var in cache
				var_cache.put(str, ++id);
		}
			return tt;
		//}
		}
		//}
	
	public Token Comment(){
		if (inputSym=='#'){
			while (inputSym!='\n'){
				Next();
			}
		}
		else if(inputSym=='/'){
			Next();
			if(inputSym=='/'){
				while (inputSym!='\n'){
					Next();}
				Next();
			}
			else {
				Prev();
				return Token.checkToken("/");
			}
		}
		return Token.checkToken("//");	
	}
	
	public Token check_main()
	{
		int i=0;
		while(inputSym != 'n'){
			i++;
			Next();
		}
		if(i>3)
			return Token.checkToken("");//error
		else{
			Next();
			return Token.checkToken("main");
		}
	}
}