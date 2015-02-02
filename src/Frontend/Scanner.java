package Frontend;
import java.io.*;
import java.util.ArrayList;
import Frontend.Token;

public class Scanner {
	private char inputSym;
	//public int getSym(){return int a;};
	private FileReader fr;
	//public int number;
	public int id;
	public static ArrayList<String> ident;
	
	private void Next(){
		this.inputSym=this.fr.getSym();
	}
	private void Prev(){
		this.inputSym = this.fr.getPrevSym();
	}
	public Scanner(String filename){
		try {
			this.fr=new FileReader(filename);
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.inputSym= fr.getSym();
	}
	public Token getToken(){
		//Token token=null;
		//char ch;String characters=null;
		if(inputSym == '\0')
		{	//return Token;
		//if(inputSym=='|'){
			return Token.checkToken("|");
		}
		NoUseCh();
		
		if(inputSym == 'm')
			return check_main();
		
		if(inputSym >='0' && inputSym<= '9') {
			return isNumber();}
	
		if((isIdentOrKeyword())!=null) {
			return isIdentOrKeyword();}
		
		//if((Comment())!=null) {
		if(inputSym == '/'){
		Comment();
		}
		
		switch(inputSym){
		case '*':
			Next();
			return Token.checkToken("*");
			//break;
		case '+':
			Next();return Token.checkToken("+");
			//break;
		case '-':
			Next();
			return Token.checkToken("-");
			//break;
		case '=':
			Next();if(inputSym=='='){Next();return Token.checkToken("==");}
					else {System.out.println("Syntax Error");return null;}
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
		int digit=Character.getNumericValue(inputSym);
		while(inputSym>='0' && inputSym<='9'){
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
		if(Character.isLetter(inputSym)){
		while (Character.isLetterOrDigit(inputSym)){
		strbfr.append(inputSym);Next();
		}
		String str = strbfr.toString();
		return Token.checkToken(str);
		}
		else
		return null;
		}
	
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
		else
			return Token.checkToken("main");
	}
}