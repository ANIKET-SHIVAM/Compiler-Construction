package TestRuns;
import Frontend.*;

public class ParserRun {
		public static void main(String []args){
			String filename = "testprogs/arithemetic.txt";
			Parser parse = new Parser(filename);
			BasicBlock bb = parse.compute();
			System.out.println("\n!!! Parsed successfully !!!");
		}		
}
