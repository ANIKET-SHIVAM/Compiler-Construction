package Frontend;

public class Token {
	private TokenType type;
	private String characters;
	private int value;

	private Token (TokenType tt, String characters, int value){
		this.type=tt;
		this.characters=characters;
		this.value=value;
	}
	private Token(){
		this.type=null;
		this.characters=null;
		this.value=0;
	}
	public static Token checkToken(String characters){
	    Token token = null;
		switch(characters){
			case "*":
				token = new Token(TokenType.timesToken, characters, 1);
				break;
				
			case "/":
				token = new Token(TokenType.divToken, characters, 2);
				break;
			
			case "+":
				token = new Token(TokenType.plusToken, characters, 11);
				break;
			case "-":
				token = new Token(TokenType.minusToken, characters, 12);
				break;
			case "==":
				token = new Token(TokenType.eqlToken, characters, 20);
				break;
			case "!=":
				token = new Token(TokenType.neqToken, characters, 21);
				break;
			case "<":
				token = new Token(TokenType.lssToken, characters, 22);
				break;
			case ">=":
				token = new Token(TokenType.geqToken, characters, 23);
				break;
			case "<=":
				token = new Token(TokenType.leqToken, characters, 24);
				break;
			case ">":
				token = new Token(TokenType.gtrToken, characters, 25);
				break;
			case ".":
				token = new Token(TokenType.periodToken, characters, 30);
				break;
			case ",":
				token = new Token(TokenType.commaToken, characters, 31);
				break;
			case "[":
				token = new Token(TokenType.openbracketToken, characters, 32);
				break;
			case "]":
				token = new Token(TokenType.closebracketToken, characters, 34);
				break;
			case ")":
				token = new Token(TokenType.closeparenToken, characters, 35);
				break;
			case "<-":
				token = new Token(TokenType.becomesToken, characters, 40);
				break;
			case "then":
				token = new Token(TokenType.thenToken, characters, 41);
				break;
			case "do":
				token = new Token(TokenType.doToken, characters, 42);
				break;
			case "(":
				token = new Token(TokenType.openparenToken, characters, 50);
				break;
			case ";":
				token = new Token(TokenType.semiToken, characters, 70);
				break;	
			case "}":
				token = new Token(TokenType.endToken, characters, 80);
				break;
			case "od":
				token = new Token(TokenType.odToken, characters, 81);
				break;
			case "fi":
				token = new Token(TokenType.fiToken, characters, 82);
				break;
			case "else":
				token = new Token(TokenType.elseToken, characters, 90);
				break;
			case "let":
				token = new Token(TokenType.letToken, characters, 100);
				break;
			case "call":
				token = new Token(TokenType.callToken, characters, 101);
				break;
			case "if":
				token = new Token(TokenType.ifToken, characters, 102);
				break;
			case "while":
				token = new Token(TokenType.whileToken, characters, 103);
				break;
			case "token =":
				token = new Token(TokenType.returnToken, characters, 104);
				break;
			case "var":
				token = new Token(TokenType.varToken, characters, 110);
				break;
			case "array":
				token = new Token(TokenType.arrToken, characters, 111);
				break;
			case "function":
				token = new Token(TokenType.funcToken, characters, 112);
				break;
			case "procedure":
				token = new Token(TokenType.procToken, characters, 113);
				break;
			case "{":
				token = new Token(TokenType.beginToken, characters, 150);
				break;
			case "main":
				token = new Token(TokenType.mainToken, characters, 200);
				break;
			case "//":
				token = new Token(TokenType.commentToken,characters,201);
				break;
			case "|":
				token = new Token(TokenType.eofToken, characters, 255);
				break;	
			default:{
			if(characters.matches("[0-9]+")==true)
				token = new Token(TokenType.number, characters, 60);
				else if(characters.matches("([a-zA-Z])(a-zA-Z0-9)*")==true){
				token = new Token(TokenType.ident, characters, 61);
				//Scanner.ident.add(characters);
				}
				else
				token = new Token(TokenType.errorToken, characters, 0);
				}
			break;
		}
		
		return token;
		}
	public TokenType getType(){return this.type;}
	public String getCharacters(){return this.characters;}
	public int getValue(){return this.value;}
}
