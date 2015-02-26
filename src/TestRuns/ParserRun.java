package TestRuns;
import Frontend.*;
import Graph.*;
import Optimizations.*;

public class ParserRun {
		public static void main(String []args){
			String filename = "testprogs/test001.txt";
			Parser parse = new Parser(filename);
			BasicBlock bb = parse.compute();
			System.out.println("\n!!! Parsed successfully !!!");
			DominatorTree domtree=new DominatorTree();
			CFG graph=new CFG("test001");
			graph.printCFG();
			CP.doCP();
			CSE.doCSE();
			CFG graph1=new CFG("test001wo");
			graph1.printCFG();
			System.out.println("\n!!! Done successfully !!!");
		}		
}
