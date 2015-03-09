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
			
	
	//optimizations
			CP.doCP();
			CSE.doCSE();
			//CFG graph1=new CFG("arithemeticwo");
			CFG graph1=new CFG("test001wo");
			graph1.printCFG(Ra);
			RA.doLivenessAnalysis();
			Ra=1;
			//IG graph2 = new IG("arithemetic_IG");
			IG graph2 = new IG("test001_IG");
			graph2.printIG();

			RA.color_node();
			for(int i=0;i<RA.Reg.size();i++)
			{
				System.out.println("For reg:"+i);
				for(int j=0;j < RA.Reg.get(i).size();j++)
				{
					System.out.print(RA.Reg.get(i).get(j)+",");
				}
				System.out.println();
			}
			CFG graph3 = new CFG("testRA");
			graph3.printCFG(Ra);
			
			System.out.println("\n!!! Done successfully !!!");
		}		
}
