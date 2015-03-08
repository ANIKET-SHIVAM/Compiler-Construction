package Optimizations;
import Frontend.*;
import Frontend.BasicBlock.BlockType;
import Frontend.Result.Type;
//import Graph.*;

import java.util.*;
public class RA {
	//Set for Liveness Analysis
	public static HashMap<Integer,ArrayList<Integer>> Live_Set = new HashMap<Integer,ArrayList<Integer>>(); 
	public static final int K = 20;
	public static HashMap<Integer,ArrayList<Integer>>Reg = new HashMap<Integer,ArrayList<Integer>>();	//Register set
	//Matrix(2-D Array) for creating Interference Graph
	public static int [][]IGMatrix = new int[Parser.insts.size()][Parser.insts.size()];
	public static Stack<Integer> node_stack = new Stack<Integer>();
	//calculate total number of edges from a node
	public static int no_of_edges(int [] arr)
	{
		int edge=0,i=0;
		
		for(i=0;i< arr.length;i++)
		{
			if(arr[i] == 1)
				edge++;
		}
		return edge;
	}

	//remove the node from IG
	public static void remove(int node)
	{
		int i=0;
		for(i=0;i< IGMatrix[node].length;i++)
		{
			if(IGMatrix[node][i] ==1)
			{
				IGMatrix[node][i] = IGMatrix[i][node] = 0;	//remove the edge
				node_stack.push(node);
			}
		}
	}
	
	//add the node to IG
	public static void add(int node)
	{
		fill_matrix(Live_Set.get(node));
	}
	
	//assign register to node
	public static void assign_reg(int node)
	{
		int i=0;
		for(i=0;i < K;i++)
		{
			if(Reg.containsKey(i)){
				if(Reg.get(i).isEmpty())
				{
					Reg.get(i).add(node);	//assign the register to the node
				}
			}
			else
			{
				ArrayList<Integer> arr = new ArrayList<Integer>();
				arr.add(node);
				Reg.put(i, arr);
			}
			
		}
		if(i == K)	//all registers have been assigned
		{
			for(i=0;i<K;i++)
			{
				int n=0;
				for(n =0;n< Reg.get(i).size();n++)
				{
					//if there is interference among nodes
					if(IGMatrix[node][Reg.get(i).get(n)] == 1 || IGMatrix[Reg.get(i).get(n)][node] == 1)
					{
						break;
					}
				}
				//if no interference,then assign the same register
				if(n == Reg.get(i).size())
				{
					Reg.get(i).add(node);
				}
			}
		}
	}
	
	//color the nodes of graph
	public static void color_node()
	{
		int node=0;
		for(node=0;node < Parser.insts.size();node++)
		{
			if(node != (Parser.insts.size()-1)) 	//IG is empty
			{
				if(no_of_edges(IGMatrix[node]) > 0 && no_of_edges(IGMatrix[node]) < K)
				{
					remove(node);
					//color_node();	//color rest of the graph
				}
				//add(node);
				//assign_reg(node);
			}
		}
		for(int n=0;n<node_stack.size();n++)
		{
			int nodes = node_stack.pop();
			add(nodes);
			assign_reg(nodes);
		}
		
		return;
	}
	
	
	public static void fill_matrix(ArrayList<Integer> set)
	{
		int i=0,j=0;
		
		for(i=0;i<set.size()-1;i++)
		{
			for(j=i+1;j< set.size();j++)
			{
					IGMatrix[set.get(i)][set.get(j)] =  1; //IGMatrix[set[i]][set[j]]=1
					IGMatrix[set.get(j)][set.get(i)] =  1;
			}
		}
	}
	
	public static void doLivenessAnalysis()
	{
		BasicBlock bb;
		for(int bbno=BasicBlock.basicblocks.size()-1;bbno>=0;bbno--){
			bb=BasicBlock.basicblocks.get(bbno);
			
			/*int last_inst = bb.inst_list.size()-1;	//last instruction in BasicBlock
			for(int index=last_inst;index>=0;index--)
			{
				Instruction ii = bb.inst_list.get(index);
				if(ii.getOperator() == "end")
				{
					continue;
				}
			 	*/		
				create_liveset(bb);	//create live set for each instruction in basic block
				if(bb.getType() == BlockType.follow)
				{
					bbno = bb.getprevblock().getblockno();
				}
			//}
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
	
	public static void create_liveset(BasicBlock bb)
	{
		int last_inst = bb.inst_list.size()-1;	//last instruction in BasicBlock
		for(int index1=last_inst;index1>=0;index1--){
			Instruction ii = bb.inst_list.get(index1);
			if(ii.getOperator() == "end")
			{
				continue;
			}
		ArrayList<Integer> set = new ArrayList<Integer>();
		int inst_index = Parser.insts.indexOf(ii);
		int next_index = bb.inst_list.indexOf(ii) + 1;	//index of next instruction in basic block
		
		String ss = ii.getOperator();
		
		//if-else blocks
		if(bb.getType() == BlockType.iftrue ||  bb.getType() == BlockType.ifelse)
			bb.out_set = bb.getjoinblock().in_set;
		
		else if(bb.getnextblock()!= null && bb.getnextblock().getType() == BlockType.iftrue && ss == "bge" || ss=="ble" || ss == "beq" || ss == "bne" || ss == "bgt" || ss == "blt")
		{
			if(bb.getifelseblock() != null)
				bb.out_set = merge_set(bb.getnextblock().in_set,bb.getifelseblock().in_set);
			else
				bb.out_set = merge_set(bb.getnextblock().in_set,bb.getjoinblock().in_set);
		}
		else if(bb.getType() == BlockType.join)	 
		{
			if(bb.getjoinblock() != null)  //phi of nested if
			{
				bb.out_set = bb.getjoinblock().in_set;
			}
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
		{	if(bb.getnextblock() != null){
			
				for(int i=0;i<bb.out_set.size();i++)
				{
					set.add(bb.out_set.get(i));		//add elements of set of next instruction to this set
				}
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

		if(bb.inst_list.indexOf(ii) == 0){
			bb.in_set = set;	//setting in_set to set at first instruction in basic block

			
			if(bb.getType() == BlockType.follow)	//if its follow block of while
			{
				BasicBlock while_block = bb.getprevblock();
				BasicBlock do_block = while_block.getnextblock();
				
				while_block.out_set = bb.in_set;
				
				create_liveset(while_block);	//liveset for while block
				
				//check if there is nested while
				Instruction first_ins = do_block.inst_list.get(0);
				if(first_ins.getOperator() == "phi")	//nested while
				{
					create_liveset(do_block.getfollowblock());
				}
				else {	//no nested while
					create_liveset(do_block);	//liveset for do block
				}
					while_block.out_set = merge_set(while_block.out_set,do_block.in_set);
					create_liveset(while_block);	//second iteration for while block
				
				if(while_block.getprevblock().inst_list.get(0).getOperator() != "phi")
				{
					while_block.getprevblock().out_set = while_block.in_set;
				}
			}
		}
			Live_Set.put(inst_index, set);	//add set corresponding to instruction

			fill_matrix(set);	//fill matrix for IG creation
			System.out.println("Live Set for BasicBlock:"+ bb.getblockno());
			System.out.print(inst_index+":- ");
			for(int i=0;i<set.size();i++)
				System.out.print(set.get(i)+",");
			System.out.println("\n");
		
		}	
	}
}
