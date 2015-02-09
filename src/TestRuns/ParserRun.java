package TestRuns;
import Frontend.*;

public class ParserRun {
		public static void main(String []args){
			String filename = "/home/gaurav/eclipse_java/Bit_project/testprogs/test001.txt";
			Parser parse = new Parser(filename);
			BasicBlock bb = parse.compute();
			System.out.println("\n!!! Parsed successfully !!!");
		}		
}
