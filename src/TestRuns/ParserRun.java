package TestRuns;
import Frontend.*;
import Graph.*;
import Optimizations.*;
import CodeGenerator.*;

public class ParserRun {
		public static void main(String []args){
			int Ra=0;
			
			//String filename = "testprogs/arithemetic.txt";
			String filename = "testprogs/test003.txt";
			Parser parse = new Parser(filename);
			BasicBlock bb = parse.compute();
			System.out.println("\n!!! Parsed successfully !!!");
			DominatorTree domtree=new DominatorTree();
			CFG graph=new CFG("test001");
			//CFG graph=new CFG("arithemetic");
			graph.printCFG(Ra);
			
	
	//optimizations
			CP.doCP();CFG graphx=new CFG("test001woCP");graphx.printCFG(Ra);
			CSE.doCSE();
			//CFG graph1=new CFG("arithemeticwo");
			
			
			CFG graph1=new CFG("test001wo");graph1.printCFG(Ra);
			
		RA.doLivenessAnalysis();
			Ra=1;
		//	IG graph2 = new IG("arithemetic_IG");
			IG graph2 = new IG("test003_IG");

			graph2.printIG();
			RA.coalese_phis();
			RA.color_node();
			for(int i=0;i<RA.Reg.size();i++)
			{
				System.out.println("For reg:"+(i+1));
				for(int j=0;j < RA.Reg.get(i+1).size();j++)
				{
					System.out.print(RA.Reg.get(i+1).get(j)+",");
				}
				System.out.println();
			}
			RA.remove_phis();
			CFG graph3 = new CFG("testRA");
			graph3.printCFG(Ra);
			System.out.println("\n!!! RA successfully !!!");
			CFG graph4=new CFG("test001aRA");
			graph4.printCFG(0);
			System.out.println("\n!!! Compile successfully !!!");
			System.out.println("Result:");
			CodeGenerator cg=new CodeGenerator();

		}		
}
