package TestRuns;
import Frontend.*;
import Graph.*;
import Optimizations.*;

public class ParserRun {
		public static void main(String []args){
			int Ra=0;
			//String filename = "testprogs/arithemetic.txt";
			String filename = "testprogs/test001.txt";
			Parser parse = new Parser(filename);
			BasicBlock bb = parse.compute();
			System.out.println("\n!!! Parsed successfully !!!");
			DominatorTree domtree=new DominatorTree();
			CFG graph=new CFG("test001");
			//CFG graph=new CFG("arithemetic");
			graph.printCFG(Ra);
			CP.doCP();
			CSE.doCSE();
			//CFG graph1=new CFG("arithemeticwo");
			CFG graph1=new CFG("test001wo");

			graph1.printCFG(Ra);
			RA.doLivenessAnalysis();
			Ra=1;
		//	IG graph2 = new IG("arithemetic_IG");
			IG graph2 = new IG("test001_IG");
			graph2.printIG();

			RA.color_node();
			CFG graph3 = new CFG("testRA");
			graph3.printCFG(Ra);
			System.out.println("\n!!! Done successfully !!!");
		}		
}
