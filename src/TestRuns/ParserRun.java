package TestRuns;
import Frontend.*;

public class ParserRun {
		public static void main(String []args){
			String filename = "testprogs/arithemetic.txt";
			Parser parse = new Parser(filename);
			System.out.println(parse.compute());
		}
		
}
