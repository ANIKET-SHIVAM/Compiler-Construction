package Optimizations;
import Frontend.*;
import Frontend.BasicBlock.BlockType;
import Frontend.Result.Type;
//import Graph.*;


import java.util.*;
public class RA {
	//Set for Liveness Analysis
	public static HashMap<Integer,ArrayList<Integer>> Live_Set = new HashMap<Integer,ArrayList<Integer>>(); 
	public static final int K = 30;
	public static int top=-1;
	public static ArrayDeque<Instruction> phi_list = new ArrayDeque<Instruction>();
	public static HashMap<Integer,ArrayList<Integer>>Reg = new HashMap<Integer,ArrayList<Integer>>();	//Register set
	//Matrix(2-D Array) for creating Interference Graph
	public static int [][]IGMatrix = new int[Parser.insts.size()][Parser.insts.size()];
	public static Stack<Integer> node_stack = new Stack<Integer>();

	public static HashMap<Integer,ArrayList<Integer>> clusters = new HashMap<Integer,ArrayList<Integer>>();
	
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
				
			}
		}
		node_stack.push(node);
		top++;
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
		//if its the top most node
		if(top == node_stack.size()-1)
		{
			ArrayList<Integer> arr = new ArrayList<Integer>();
			arr.add(node);
			
			Parser.insts.get(node).register = 1;
		
			if(Parser.insts.get(node).getOperator() == "phi")
			{
				if(!clusters.isEmpty()){

				if(clusters.containsKey(node) && !clusters.get(node).isEmpty()){

				for(i=0;i<clusters.get(node).size();i++)
				{
					arr.add(clusters.get(node).get(i));
					Parser.insts.get(clusters.get(node).get(i)).register=1;
				}
				}
				}
			}
			Reg.put(1, arr);
			return;
		}
		else
		{
			if(is_reg_assigned(node) == 1)
				return;
			for(i=1;i < K;i++)
			{
				if(Reg.containsKey(i)){
					
					int k=0;
					for(k=0;k< Reg.get(i).size();k++)
					{
						if(is_interfering(node,Reg.get(i).get(k)))
							break;
					}
					//if not interfering,assign same register
					if(k == Reg.get(i).size()){
						Reg.get(i).add(node);
						Parser.insts.get(node).register= i;
						if(Parser.insts.get(node).getOperator() == "phi")
						{
							if(!clusters.isEmpty()){
						if(clusters.containsKey(node) && !clusters.get(node).isEmpty()){

							for(int j=0;j<clusters.get(node).size();j++)
							{
								Reg.get(i).add(clusters.get(node).get(j));
								Parser.insts.get(clusters.get(node).get(j)).register=i;
							}
							}
							}
						}

						break;
					}
				}
				else
				{
					ArrayList<Integer> arr = new ArrayList<Integer>();
					arr.add(node);
					Reg.put(i, arr);
					Parser.insts.get(node).register = i;
					if(Parser.insts.get(node).getOperator() == "phi")
					{
						if(!clusters.isEmpty()){

						if(clusters.containsKey(node) && !clusters.get(node).isEmpty()){

						for(int j=0;j<clusters.get(node).size();j++)
						{
							Reg.get(i).add(clusters.get(node).get(j));
							Parser.insts.get(clusters.get(node).get(j)).register=i;
						}
						}
						}
					}

					break;
				}	
			}
			
			/*if(i == K)	//all registers have been assigned
			{
				for(i=0;i<K;i++)
				{
					int n=0;
					for(n =0;n< Reg.get(i).size();n++)
					{
						//if there is interference among nodes
						if(IGMatrix[node][Reg.get(i).get(n)] == 1 || IGMatrix[Reg.get(i).get(n)][node] == 1)
							break;
					}
					//if no interference,then assign the same register
					if(n == Reg.get(i).size())
					{
						Reg.get(i).add(node);
						break;
					}
				}
			}*/
		}
	}
	
	public static int is_reg_assigned(int node)
	{
		int res=0;
		for(int i=0;i< Reg.size();i++)
		{
			if(Reg.get(i+1).contains(node))
				res=1;
		}
			
		return res;
	}
	
	public static int present_in_liveset(int node)
	{
		int res=0;
		for(int i=0;i<Live_Set.size();i++)
		{
			if(Live_Set.get(i).contains(node))
				res=1;
		}
		return res;
	}
	
	public static boolean is_interfering(int res_node,int oper_node)
	{
		int count=0;
		for(int i=0;i<Live_Set.size();i++)
		{
			count=0;
			for(int k=0;k<Live_Set.get(i).size();k++)
			{
				if(Live_Set.get(i).get(k) == res_node || Live_Set.get(i).get(k) == oper_node)
				{
					count++;
				}
				if(count==2)
					return true;
			}
		}
		return false;
	}
	
	public static void coalese_phis()
	{
		while(!phi_list.isEmpty())
		{
			Instruction i = phi_list.poll();
			Result res = new Result(Type.instruction,i);
			ArrayList<Integer> phi_operands = new ArrayList<Integer>();

			//if 1st operand is instruction 
			if(i.getOperands().get(0).getType() == Type.instruction)
			{
				int oper1 = Parser.insts.indexOf(i.getOperands().get(0).getInstruction());
				if(is_interfering(Parser.insts.indexOf(i),oper1))
				{
					Instruction ins = new Instruction("move",i.getOperands().get(0) ,res);
					i.basicblock.getprevblock().inst_list.add(ins);
				}
				else
				{
					//not interfering, create a cluster of them
					phi_operands.add(oper1);
					clusters.put(Parser.insts.indexOf(i),phi_operands);
					
					i.cluster  = Parser.insts.indexOf(i);
					Parser.insts.get(oper1).cluster = Parser.insts.indexOf(i);
					i.is_cluster=true;
					Parser.insts.get(oper1).is_cluster = true;
					
					//remove node from the interference graph
					int j=0;
					for(j=0;j<IGMatrix[oper1].length;j++)
					{
						if(IGMatrix[oper1][j] == 1)
							IGMatrix[oper1][j] = IGMatrix[j][oper1] = 0;
					}
					//assign same neighbors in a cluster
					for(j=0;j<IGMatrix[Parser.insts.indexOf(i)].length;j++)
					{
						if(IGMatrix[Parser.insts.indexOf(i)][j] == 1)
							IGMatrix[oper1][j] = IGMatrix[j][oper1] = 1;
					}
				}
			}
			else	//it is a constant
			{
				Result res1 = new Result(Type.number,i.getOperands().get(0).getValue());
				Instruction ins = new Instruction("move",res1,res);
				i.basicblock.getprevblock().inst_list.add(ins);
			}
		
			//if 2nd operand is instruction 
			if(i.getOperands().get(1).getType() == Type.instruction)
			{
				int oper2 = Parser.insts.indexOf(i.getOperands().get(1).getInstruction());
				if(is_interfering(Parser.insts.indexOf(i),oper2))
				{
					Instruction ins = new Instruction("move",i.getOperands().get(1) ,res);

					if(i.basicblock.getprevblock2().getType() == BlockType.ifelse)
						i.basicblock.getprevblock2().inst_list.add(ins);
					else
					{
						int pos = i.basicblock.getprevblock2().inst_list.size()-1; 
						i.basicblock.getprevblock2().inst_list.add(pos, ins);
					}
				}
				else
				{
					phi_operands.add(oper2);
					clusters.put(Parser.insts.indexOf(i),phi_operands);
					
					i.cluster  = Parser.insts.indexOf(i);
					Parser.insts.get(oper2).cluster = Parser.insts.indexOf(i);
					i.is_cluster=true;
					Parser.insts.get(oper2).is_cluster = true;
					
					int j=0;
					for(j=0;j<IGMatrix[oper2].length;j++)
					{
						if(IGMatrix[oper2][j] == 1)
							IGMatrix[oper2][j] = IGMatrix[j][oper2] = 0;
					}
					//assign same neighbors in a cluster
					for(j=0;j<IGMatrix[Parser.insts.indexOf(i)].length;j++)
					{
						if(IGMatrix[Parser.insts.indexOf(i)][j] == 1)
							IGMatrix[oper2][j] = IGMatrix[j][oper2] = 1;
					}
				}
			}
			else	//its a constant
			{
				Result res1 = new Result(Type.number,i.getOperands().get(1).getValue());
				Instruction ins = new Instruction("move",res1,res);
				if(i.basicblock.getprevblock2().getType() == BlockType.ifelse)
					i.basicblock.getprevblock2().inst_list.add(ins);
				else
				{
					int pos = i.basicblock.getprevblock2().inst_list.size()-1;
					i.basicblock.getprevblock2().inst_list.add(pos, ins);
				}
			}
			
		}
	}
	
	public static void remove_phis()
	{
		for(int i=0;i< BasicBlock.basicblocks.size();i++)
		{
			BasicBlock bb = BasicBlock.basicblocks.get(i);
			for(int j=0;j<bb.inst_list.size();j++)
			{
				//correct the move instruction,if any added on above block
				if(bb.inst_list.get(j).getOperator() == "move")
				{

					Result oper2 = bb.inst_list.get(j).getOperands().get(1);
					if(oper2.getType() == Type.instruction)
					{
						if(oper2.getInstruction().getOperator() == "phi")
						{
							Result res = new Result(Type.number,oper2.getInstruction().register);
							bb.inst_list.get(j).getOperands().set(1, res);
						}
					}

				}
				
				if(bb.inst_list.get(j).getOperator()== "phi")
				{

					Parser.insts.remove(bb.inst_list.get(j));
					bb.inst_list.remove(j);
					
					j--;

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
					color_node();	//color rest of the graph
					break;
				}
				else
				{
					if(no_of_edges(IGMatrix[node]) == 0 && !node_stack.contains(node) && is_reg_assigned(node) == 0)
					{
						//if(present_in_liveset(node) == 0)
						//assign_reg(node);
						//else
						//{
							remove(node);
							color_node();
							break;
						//}
					}
				}
			}
			else
				return;
		}

			int nodes = node_stack.get(top);
			
			add(nodes);
			assign_reg(nodes);
			top--;
		
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
			create_liveset(bb);	//create live set for each instruction in basic block
			if(bb.getType() == BlockType.follow)
			{
				bbno = bb.getprevblock().getblockno();
			}
		}
		
	}
	
	public static ArrayList<Integer> merge_set(ArrayList <Integer>if_set,ArrayList<Integer>else_set)
	{
		ArrayList<Integer> final_set = new ArrayList<Integer>();
		int i=0;
		if(if_set.size() != 0){
		for(i=0;i < if_set.size();i++)
			final_set.add(if_set.get(i));
		}
		if(else_set.size()!= 0){
		for(i=0;i < else_set.size();i++)
		{
			if(!final_set.contains(else_set.get(i)))
				final_set.add(else_set.get(i));
		}
		}
		return final_set;
	}
	
	public static void create_liveset(BasicBlock bb)
	{
		int last_inst = bb.inst_list.size()-1;	//last instruction in BasicBlock
		for(int index1=last_inst;index1>=0;index1--){
			Instruction ii = bb.inst_list.get(index1);
			
			if(ii.getOperator() == "phi")
				phi_list.add(ii);
			if(ii.getOperator() == "end")
			{
				continue;
			}
		ArrayList<Integer> set = new ArrayList<Integer>();
		int inst_index = Parser.insts.indexOf(ii);
		int next_index = bb.inst_list.indexOf(ii) + 1;	//index of next instruction in basic block
		
		String ss = ii.getOperator();
		
		//if-else blocks
		if((bb.getType() == BlockType.iftrue ||  bb.getType() == BlockType.ifelse) && bb.getjoinblock() != null)
			bb.out_set = bb.getjoinblock().in_set;
		
		else if(bb.getnextblock()!= null && bb.getnextblock().getType() == BlockType.iftrue && ss == "bge" || ss=="ble" || ss == "beq" || ss == "bne" || ss == "bgt" || ss == "blt")
		{
			if(bb.getifelseblock() != null)
			{
				if(bb.getnextblock().inst_list.size() == 0 && bb.getifelseblock().inst_list.size() == 0)
				{
					bb.out_set = bb.getnextblock().getjoinblock().in_set;
				}
				else
					bb.out_set = merge_set(bb.getnextblock().in_set,bb.getifelseblock().in_set);
			}
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
		{	if(bb.getnextblock() != null || bb.getjoinblock() != null){
			
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
