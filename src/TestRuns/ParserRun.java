package TestRuns;
import Frontend.*;
import Graph.CFG;

public class ParserRun {
		public static void main(String []args){
			String filename = "testprogs/arithemetic.txt";
			Parser parse = new Parser(filename);
			BasicBlock bb = parse.compute();
			System.out.println("\n!!! Parsed successfully !!!");
			CFG graph=new CFG("test001");
			graph.printCFG();
			for (Instruction i:Parser.insts){
				System.out.println(i.getOperator() + i.getbb());
			}
				
		}		
}
