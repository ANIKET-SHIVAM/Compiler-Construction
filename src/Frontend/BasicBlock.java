package Frontend;
import java.util.*;

import Frontend.Result.Type;
import Graph.DominatorTree;
import Optimizations.*;
public class BasicBlock {
	public enum BlockType{
		main,iftrue,ifelse,join,whileblock,doblock,follow,function,call
	}
	public static int block_id;
	public static ArrayList<Instruction> inline_inst_list=new ArrayList<Instruction>();
	public static ArrayList<BasicBlock>Functions_list=new ArrayList<BasicBlock>();
	public static BasicBlock mainblock;
	public static HashMap<Integer,BasicBlock>basicblocks=new HashMap<Integer,BasicBlock>();
	private int blockno;
	private BlockType kind;
	private BasicBlock nextblock;	// for if,while and do
	private BasicBlock ifelseblock;	// for if
	private BasicBlock joinblock;   // for if
	private BasicBlock prevblock;	
	private BasicBlock prevblock2;  // for joinblock only
	private BasicBlock followblock;	//for while
	private BasicBlock dotowhileblock;
	private BasicBlock whiletodoblock;
	private BasicBlock functionblock;
	public HashMap<Integer,Stack<Instruction>> Sym_table;
	public BasicBlock jointoif;
	public int start_instruction_index;
	public int end_instruction_index;
	
	public Instruction start_Instr;	//starting instruction
	public Instruction end_Instr;	//ending instruction
	
	public ArrayList<Instruction> 	 inst_list;
	public ArrayList<Integer> out_set;	//live set after end of basic block
	public ArrayList<Integer> in_set;	//live set before beginning of basic block
	
	BasicBlock(){
		inst_list = new ArrayList<Instruction>();
		this.kind=BlockType.main;
		this.blockno = block_id;
		basicblocks.put(block_id, this);
		this.Sym_table=Parser.Sym_table;
		Functions_list.add(this);
		
			
	}
	
	BasicBlock(BlockType kind, Function func){							// for function first bb
		inst_list = new ArrayList<Instruction>();
		this.kind	=kind;
		this.blockno = block_id;
		basicblocks.put(block_id, this);
		this.Sym_table=func.get_Sym_table();
		Functions_list.add(this);
		
			
	}
	
	BasicBlock(BlockType kind,BasicBlock bb){
		inst_list = new ArrayList<Instruction>();
		this.kind	=kind;
		this.blockno = block_id;
		basicblocks.put(block_id, this);
		this.Sym_table=bb.Sym_table;
		
	}
	
	public BasicBlock createIfTrue(){
		
		BasicBlock iftrue=new BasicBlock(BlockType.iftrue,this);
		this.nextblock=iftrue;
		iftrue.prevblock=this;
		basicblocks.put(block_id, iftrue);
		return iftrue;
	}
	
	public BasicBlock createElse(){
		BasicBlock ifelse=new BasicBlock(BlockType.ifelse,this);
		this.ifelseblock=ifelse;
		ifelse.prevblock=this;
		basicblocks.put(block_id, ifelse);
		return ifelse;
	}
	
	public BasicBlock createWhile(){
		BasicBlock whileblock=new BasicBlock(BlockType.whileblock,this);
		this.nextblock=whileblock;
		whileblock.prevblock=this;
		basicblocks.put(block_id, whileblock);
		return whileblock;
	}
	
	public BasicBlock createdo(){
		BasicBlock doblock=new BasicBlock(BlockType.doblock,this);
		this.nextblock=doblock;
		doblock.prevblock=this;
		basicblocks.put(block_id, doblock);
	//	doblock.nextblock=this;
		//this.prevblock2=doblock;
		return doblock;
	}
	
	public BasicBlock createfollow(){
		BasicBlock follow=new BasicBlock(BlockType.follow,this);
		this.followblock=follow;
		follow.prevblock=this;
		basicblocks.put(block_id, follow);
		return follow;
	}
		
	public BasicBlock createjoin(){		//only if will do this
		BasicBlock join=new BasicBlock(BlockType.join,this);
		this.joinblock=join;
		join.prevblock=this;
		basicblocks.put(block_id, join);
		return join;
	}
	public BasicBlock createafterfunction(BasicBlock functionbb){		//only if will do this
		BasicBlock afterfunction=new BasicBlock(this.kind,this);
		this.functionblock=functionbb;
		this.nextblock=afterfunction;
		afterfunction.prevblock=this;
		basicblocks.put(block_id, afterfunction);
		return afterfunction;
	}
	
	public void setjoin(BasicBlock phi_block){		//only else and main(when no else is there) can do this
	//	if (this.kind==BlockType.ifelse){
			this.joinblock = phi_block;
			//this.joinblock=this.prevblock.nextblock.joinblock;	
			this.joinblock.prevblock2=this;	
		/*}
		else{
			this.joinblock=this.nextblock.joinblock;	
			this.nextblock.joinblock.prevblock2=this;
		}*/
	}
	public void setdotowhile(BasicBlock while_block){	
		this.dotowhileblock=while_block;
		while_block.whiletodoblock = this;
	}
	public boolean checkdotowhile(){
		boolean b;
		if(this.dotowhileblock!=null)
			b=true;
		else
			b=false;
		return b;
	}
	public BasicBlock getdotowhile(){	
		return this.dotowhileblock;
	}
	public BasicBlock getwhiletodo(){	
		return this.whiletodoblock;
	}
	public void setStartInstructionIndex(int index){
		start_instruction_index=index;
	}
	public void setEndInstructionIndex(int index){
		end_instruction_index=index;
	}
	public BlockType getType(){
		return this.kind;
	}
	public int getblockno(){
		return this.blockno;
	}
	public void changeType(BlockType bbtype){
		this.kind=bbtype;
	}
	public BasicBlock getprevblock(){
		return this.prevblock;
	}
	public BasicBlock getprevblock2(){
		return this.prevblock2;
	}
	public BasicBlock getnextblock(){
		return this.nextblock;
	}
	public BasicBlock getifelseblock(){
		return this.ifelseblock;
	}
	public BasicBlock getjoinblock(){
		return this.joinblock;
	}
	public BasicBlock getfunctionblock(){
		return this.functionblock;
	}
	public BasicBlock getfollowblock(){
		return this.followblock;
	}
	public static BasicBlock getblockbyid(int id){
		return basicblocks.get(id);
	}
	public void setfollowblocknull(){
		this.followblock=null;
	}
	public int getStartInstructionIndex(){
		return this.start_instruction_index;
	}
	public int getEndInstructionIndex(){
		return this.end_instruction_index;
	}
	public static void decblockid(){
		block_id--;
	}
	public HashMap<Integer,Stack<Instruction>> get_Sym_table(){
		return this.Sym_table;
	}
	
	public ArrayList<String> printInstructions(){
		ArrayList<String> bb_insts=new ArrayList<>();
		for(Instruction inst:inst_list){
			inline_inst_list.add(inst);
			String oper1;String oper2;
			StringBuilder instruction_print= new StringBuilder(Integer.toString(Parser.insts.indexOf(inst))).append(":").append(inst.getOperator());
			ArrayList<Result> operands=inst.getOperands();
			if (operands!= null){
			if(operands.size()==2){
				Result op1=operands.get(0);
				if(op1.getType()==Type.number)
					oper1= new StringBuilder(" #").append(op1.getValue()).toString();
				else if(op1.getType()==Type.instruction)
					oper1= new StringBuilder(" (").append(Parser.insts.indexOf(op1.getInstruction())).append(") ").toString();
				else if (op1.getType()==Type.variable)
					oper1= new StringBuilder(" ").append(op1.getName()).toString();
				else if (op1.getType()==Type.param)
					oper1= new StringBuilder(" ").append(op1.getName()).toString();
				else if(op1.getType()==Type.arr)
					oper1= new StringBuilder(" ").append(op1.getName()).toString();
				else
					oper1="error";
				System.out.println(inst.getOperator());
				Result op2=operands.get(1);
				if(op2.getType()==Type.number)
					oper2= new StringBuilder(" #").append(op2.getValue()).toString();
				else if(op2.getType()==Type.instruction){
					if(Parser.insts.indexOf(op2.getInstruction())==-1&&this.kind==BlockType.whileblock){
						op2.setInstruction(this.getfollowblock().inst_list.get(0));}
					if(Parser.insts.indexOf(op2.getInstruction())==-1&&this.getifelseblock()!=null){
						op2.setInstruction(this.getifelseblock().inst_list.get(0));}
					if(Parser.insts.indexOf(op2.getInstruction())==-1&&this.getjoinblock()!=null){
						op2.setInstruction(this.getjoinblock().inst_list.get(0));
						}
					if(op2.getInstruction().getOperator()=="patch branch")
						op2.setInstruction(this.getjoinblock().inst_list.get(0));
						//oper2= new StringBuilder(" (").append(Parser.insts.indexOf(op2.getInstruction())).append(") ").toString();
					//}
					//else	
						oper2= new StringBuilder(" (").append(Parser.insts.indexOf(op2.getInstruction())).append(") ").toString();
				}
				else if (op2.getType()==Type.variable)
					oper2= new StringBuilder(" ").append(op2.getName()).toString();
				else if (op2.getType()==Type.param)
					oper2= new StringBuilder(" ").append(op2.getName()).toString();
				else if(op2.getType()==Type.arr)
					oper2= new StringBuilder(" ").append(op2.getName()).toString();
				else if(op2.getType()==Type.FP)
					oper2= new StringBuilder(" ").append("FP").toString();
				
				else
					oper2="error";
				
				if(inst.getOperator()=="phi"){
					String phivar=inst.getPhiVar();
					instruction_print.append(" ").append(phivar).append(oper1).append(oper2);
				}
				else
					instruction_print.append(oper1).append(oper2);
			}
			else if (operands.size()==1){
				Result op1=operands.get(0);
				if(inst.getOperator()=="ret")
					System.out.println("ssssssssssssssssss"+op1.getType());
				if(op1.getType()==Type.number)
					oper1= new StringBuilder(" #").append(op1.getValue()).toString();
				if(op1.getType()==Type.variable||op1.getType()==Type.arr)
					oper1= new StringBuilder(" ").append(op1.getName()).toString();
				else if(op1.getType()==Type.instruction)
					oper1= new StringBuilder(" (").append(Parser.insts.indexOf(op1.getInstruction())).append(") ").toString();
				else if (op1.getType()==Type.param)
					oper1= new StringBuilder(" ").append(op1.getName()).toString();
				else
					oper1="error";
				
				instruction_print.append(oper1);
			}
			}	
			  bb_insts.add(instruction_print.toString());
		}
		return bb_insts;
	}

	public ArrayList<String> printRegisters(){
		ArrayList<String> bb_insts=new ArrayList<>();
		for(Instruction inst:inst_list){
			String oper1=new String();
			String oper2 = new String();
			int n=0;
			for(n=0;n< RA.Reg.size();n++)
			{
				if(RA.Reg.get(n+1).contains(Parser.insts.indexOf(inst)))
					break;
			}
			StringBuilder instruction_print= new StringBuilder("r").append(n+1).append(":").append(inst.getOperator());
			ArrayList<Result> operands=inst.getOperands();
			if (operands!= null){
			if(operands.size()==2){
				Result op1=operands.get(0);
				if(op1.getType()==Type.number){
					oper1= new StringBuilder(" #").append(op1.getValue()).toString();}
				else if(op1.getType()==Type.instruction)
				{
					int ins_index = Parser.insts.indexOf(op1.getInstruction());
					for(int i=0;i< RA.Reg.size();i++)
					{
						if(RA.Reg.get(i+1).contains(ins_index))
						oper1= new StringBuilder(" r").append(i+1).toString();
						
					}
				}	
				else if (op1.getType()==Type.variable)
					oper1= new StringBuilder(" ").append(op1.getName()).toString();
				else if(op1.getType()==Type.arr)
					oper1= new StringBuilder(" ").append(op1.getName()).toString();
				else
					oper1="error";
			
				Result op2=operands.get(1);
				if(op2.getType()==Type.number)
				{if(inst.getOperator()=="move")
					oper2= new StringBuilder(" r").append(op2.getValue()).toString();
				else
					oper2= new StringBuilder(" #").append(op2.getValue()).toString();	}
				else if(op2.getType()==Type.instruction){
					//if(Parser.insts.indexOf(op2.getInstruction())==-1){
						int ins_index = Parser.insts.indexOf(op2.getInstruction());
					//	if(ins_index ==-1)
						//	op2.setInstruction(this.getfollowblock().inst_list.get(0));
						for(int i=0;i< RA.Reg.size();i++)
						{
							if(RA.Reg.get(i+1).contains(ins_index))
								oper2 = new StringBuilder(" r").append(i+1).toString();
	
						}
				}
					//else{	
						//oper2= new StringBuilder(" (").append(Parser.insts.indexOf(op2.getInstruction())).append(") ").toString();
				//}
				else if (op2.getType()==Type.variable)
					oper2= new StringBuilder(" ").append(op2.getName()).toString();
				else if(op2.getType()==Type.arr)
					oper2= new StringBuilder(" ").append(op2.getName()).toString();
				else
					oper2="error";
				
				if(inst.getOperator()=="phi"){
					String phivar=inst.getPhiVar();
					instruction_print.append(" ").append(phivar).append(oper1).append(oper2);
				}
				else
					instruction_print.append(oper1).append(oper2);
			}
			else if (operands.size()==1){
				Result op1=operands.get(0);
				if(op1.getType()==Type.number)
					oper1= new StringBuilder(" #").append(op1.getValue()).toString();
				else if(op1.getType()==Type.instruction){
					int ins_index = Parser.insts.indexOf(op1.getInstruction());
					for(int i=0;i< RA.Reg.size();i++)
					{
						if(RA.Reg.get(i+1).contains(ins_index))
						oper1= new StringBuilder(" r").append(i+1).toString();
						
					}
				}
				else
					oper1="error";
				
				instruction_print.append(oper1);
			}
			}	
			  bb_insts.add(instruction_print.toString());
		}
		return bb_insts;
	}
	public static void kill_remove(){
		BasicBlock bb;
		for(int bbno=0;bbno<BasicBlock.basicblocks.size();bbno++){
				bb=Frontend.BasicBlock.basicblocks.get(bbno);
				Instruction instkill=new Instruction();
				for(Instruction inst:bb.inst_list){
					if(inst.getOperator()=="kill"){
						Parser.insts.remove(inst);
						instkill=inst;
					}
						bb.inst_list.remove(instkill);
				}
	
		}
	}
}
