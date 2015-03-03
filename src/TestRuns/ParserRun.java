package TestRuns;
import Frontend.*;
import Graph.*;
import Optimizations.*;

public class ParserRun {
		public static void main(String []args){
			//String filename = "testprogs/arithemetic.txt";
			String filename = "testprogs/test002.txt";
			Parser parse = new Parser(filename);
			BasicBlock bb = parse.compute();
			System.out.println("\n!!! Parsed successfully !!!");
			DominatorTree domtree=new DominatorTree();
			CFG graph=new CFG("test002");
			graph.printCFG();
			CP.doCP();
			CSE.doCSE();
			CFG graph1=new CFG("test002wo");

			graph1.printCFG();
			RA.doLivenessAnalysis();
			
			IG graph2 = new IG("test002_IG");
			graph2.printIG();
			System.out.println("\n!!! Done successfully !!!");
		}		
}
