package Optimizations;
import Frontend.*;
import Frontend.BasicBlock.BlockType;
import Frontend.Result.Type;
//import Graph.*;

import java.util.*;
public class RA {
	//Set for Liveness Analysis
	public static HashMap<Integer,ArrayList<Integer>> Live_Set = new HashMap<Integer,ArrayList<Integer>>(); 

	//Matrix(2-D Array) for creating Interference Graph
	public static int [][]IGMatrix = new int[Parser.insts.size()][Parser.insts.size()];
	//	public static ArrayList<List<Integer>> IGMatrix = new ArrayList<List<Integer>>();
	
	public static void fill_matrix(ArrayList<Integer> set)
	{
		int i=0,j=0;
		
		for(i=0;i<set.size()-1;i++)
		{
			for(j=i+1;j< set.size();j++)
			{
					IGMatrix[set.get(i)][set.get(j)] =  1; //IGMatrix[set[i]][set[j]]=1
			}
		}
	}
	
	public static void doLivenessAnalysis()
	{
		BasicBlock bb;
		for(int bbno=BasicBlock.basicblocks.size()-1;bbno>=0;bbno--){
			bb=BasicBlock.basicblocks.get(bbno);
			System.out.println("Live Set for BasicBlock:"+ bb.getblockno());
			int last_inst = bb.inst_list.size()-1;	//last instruction in BasicBlock
			for(int index=last_inst;index>=0;index--)
			{
				Instruction ii = bb.inst_list.get(index);
				if(ii.getOperator() == "end")
				{
					continue;
				}
		
				create_liveset(bb,ii);	//create live set for each instruction in basic block
			}
		}
		
	}
	
	public static ArrayList<Integer> merge_set(ArrayList <Integer>if_set,ArrayList<Integer>else_set)
	{
		ArrayList<Integer> final_set = new ArrayList<Integer>();
		int i=0;
		for(i=0;i < if_set.size();i++)
			final_set.add(if_set.get(i));
		for(i=0;i < else_set.size();i++)
		{
			if(!final_set.contains(else_set.get(i)))
				final_set.add(else_set.get(i));
		}
		return final_set;
	}
	
	public static void create_liveset(BasicBlock bb,Instruction ii)
	{
		ArrayList<Integer> set = new ArrayList<Integer>();
		int inst_index = Parser.insts.indexOf(ii);
		int next_index = bb.inst_list.indexOf(ii) + 1;	//index of next instruction in basic block
		
		String ss = ii.getOperator();
		
		//if-else blocks
		if(bb.getType() == BlockType.iftrue ||  bb.getType() == BlockType.ifelse)
			bb.out_set = bb.getjoinblock().in_set;
		else if(ss == "bge" || ss=="ble" || ss == "beq" || ss == "bne" || ss == "bgt" || ss == "blt")
		{
			if(bb.getifelseblock() != null)
				bb.out_set = merge_set(bb.getnextblock().in_set,bb.getifelseblock().in_set);
			else
				bb.out_set = merge_set(bb.getnextblock().in_set,bb.getjoinblock().in_set);
		}
		
		if(next_index < bb.inst_list.size())	//its not the last instruction in the block
		{
			Instruction next_ins = bb.inst_list.get(next_index);	//next instruction in basic block
			if(next_ins.getOperator() != "end")
			{
				ArrayList<Integer> tmp = Live_Set.get(Parser.insts.indexOf(next_ins));	//get elements of set of next instruction	
		
				for(int i=0;i<tmp.size();i++)
				{
					set.add(tmp.get(i));		//add elements of set of next instruction to this set
				}
			}
		}
		else	//its the last instruction in the block
		{	
			for(int i=0;i<bb.out_set.size();i++)
			{
				set.add(bb.out_set.get(i));		//add elements of set of next instruction to this set
			}
		}
		
		for(int count = 0;count< ii.getOperands().size();count++)
		{
			Result res = ii.getOperands().get(count);
			if(res.getType() == Type.instruction)
			{
				int node = Parser.insts.indexOf(res.getInstruction());
				if(!set.contains(node))
				set.add(node);
			}
		}
		
		if(set.contains(inst_index)) 
			set.remove(set.indexOf(inst_index));  //current instruction not live at this point,so remove from set

		if(bb.inst_list.indexOf(ii) == 0)
			bb.in_set = set;	//setting in_set to set at first instruction in basic block
		
		Live_Set.put(inst_index, set);	//add set corresponding to instruction

		fill_matrix(set);	//fill matrix for IG creation
		
		System.out.print(inst_index+":- ");
		for(int i=0;i<set.size();i++)
			System.out.print(set.get(i)+",");
		System.out.println("\n");
		
	}
}
