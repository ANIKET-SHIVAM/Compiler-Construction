package TestRuns;
import Frontend.*;
import Graph.*;

public class ParserRun {
		public static void main(String []args){
			String filename = "testprogs/test001.txt";
			Parser parse = new Parser(filename);
			BasicBlock bb = parse.compute();
			System.out.println("\n!!! Parsed successfully !!!");
			CFG graph=new CFG("test001");
			graph.printCFG();
			DominatorTree domtree=new DominatorTree();
		}		
}
