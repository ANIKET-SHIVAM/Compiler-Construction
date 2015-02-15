package TestRuns;
import Frontend.*;
//import Graph.CFG;

public class ParserRun {
		public static void main(String []args){
			String filename = "testprogs/arithemetic.txt";
			Parser parse = new Parser(filename);
			BasicBlock bb = parse.compute();
			System.out.println("\n!!! Parsed successfully !!!");
			while(bb.getnextblock()!=null){
				System.out.println(BasicBlock.mainblock.getblockno());
				BasicBlock.mainblock=BasicBlock.mainblock.getnextblock();
				}
		}		
}
