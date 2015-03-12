package TestRuns;
import Frontend.*;
import Graph.*;
import Optimizations.*;
import CodeGenerator.*;

public class ParserRun {
		public static void main(String []args){
			int Ra=0;
			

			//String filename = "testprogs/arithemetic.txt";
			String filename = "testprogs/test002.txt";

			Parser parse = new Parser(filename);
			BasicBlock bb = parse.compute();
			System.out.println("\n!!! Parsed successfully !!!");
			DominatorTree domtree=new DominatorTree();


			CFG graph=new CFG("test001");
			//CFG graph=new CFG("arithemetic");
			graph.printCFG(Ra);
			
			System.out.println("\n!!! optimization successfully !!!");
			//optimizations
			CP.doCP();CFG graphx=new CFG("test001woCP");graphx.printCFG(Ra);
			CSE.doCSE();

			//CFG graph1=new CFG("arithemeticwo");

			CFG graph1=new CFG("test001wo");

			graph1.printCFG(Ra);
			//for functions
			int blocks =0;
			int first_inst_index=0;
			int last_inst_index=0;
			if(!Parser.func_mapping.isEmpty()){
			for(int i=0;i< Parser.func_mapping.size();i++)
			{
				String fun_name = Parser.func_mapping.get(i);
				BasicBlock first_block=Parser.Function_list.get(fun_name).getfirstbb();
				int first_block_id  = first_block.getblockno();
				first_inst_index = Parser.insts.indexOf(first_block.inst_list.get(0));
				
				BasicBlock last_block=Parser.Function_list.get(fun_name).getreturnbb();
				int last_block_id  = last_block.getblockno();
				last_inst_index = Parser.insts.indexOf(last_block.inst_list.get(last_block.inst_list.size()-1));
				
				RA.doLivenessAnalysis(first_block_id,last_block_id);
				
				blocks++;
				RA.coalese_phis();
				RA.color_node(first_inst_index,last_inst_index);
				
				for(int ii=0;ii<RA.Reg.size();ii++)
				{
					System.out.println("For reg:"+(ii+1));
					for(int j=0;j < RA.Reg.get(ii+1).size();j++)
					{
						System.out.print(RA.Reg.get(ii+1).get(j)+",");
					}
					System.out.println();
				}
				RA.remove_phis(first_block_id,last_block_id);
				last_inst_index++;
			}
			}
			//for normal code
			RA.doLivenessAnalysis(blocks,BasicBlock.basicblocks.size()-1);

			Ra=1;
			//IG graph2 = new IG("arithemetic_IG");
			IG graph2 = new IG("test001_IG");

			graph2.printIG();
			RA.coalese_phis();

			RA.color_node(last_inst_index,Parser.insts.size()-1);
			CFG graph33 = new CFG("testRAb4removal");
			graph33.printCFG(Ra);
			for(int i=0;i<RA.Reg.size();i++)
			{
				System.out.println("For reg:"+(i+1));
				for(int j=0;j < RA.Reg.get(i+1).size();j++)
				{
					System.out.print(RA.Reg.get(i+1).get(j)+",");
				}
				System.out.println();
			}
			RA.remove_phis(blocks,BasicBlock.basicblocks.size()-1);
			CFG graph3 = new CFG("testRA");
			graph3.printCFG(Ra);
			System.out.println("\n!!! RA successfully !!!");

	/*		for(int i=0;i<Parser.insts.size();i++)
			{
				System.out.println("reg forinstruction"+ i +":" + Parser.insts.get(i).register);
			}*/
			CFG graph4=new CFG("test001aRA");
			graph4.printCFG(0);

			System.out.println("\n!!! Compile successfully !!!");
			System.out.println("Result:");
			CodeGenerator cg=new CodeGenerator();

		}		
}
