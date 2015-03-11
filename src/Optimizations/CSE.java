package Optimizations;
import Frontend.*;
import Frontend.BasicBlock.BlockType;
import Frontend.Result.Type;
import Graph.*;

import java.util.*;

public class CSE {
	
	public static void doCSE(){
		BasicBlock bb;
		for(int bbno=0;bbno<BasicBlock.basicblocks.size();bbno++){
			ArrayList<Instruction> replace=new ArrayList<Instruction>();
			HashMap<Instruction,Instruction> replaceInstList=new HashMap<Instruction,Instruction>();
		    HashMap<String,LinkedList<Instruction>> sameInstList=new HashMap<String,LinkedList<Instruction>>();
			bb=Frontend.BasicBlock.basicblocks.get(bbno);
			for(Instruction inst:bb.inst_list){
				if(!replace.contains(inst)	){
				if(inst.getOperands().size()==2){
					if(sameInstList.containsKey(inst.getOperator())){
								sameInstList.get(inst.getOperator()).add(inst);		
					}
					else
					{
						LinkedList<Instruction> ll=new LinkedList<Instruction>(); 
						ll.add(inst);
						sameInstList.put(inst.getOperator(), ll);
					}
					for(int instno=bb.inst_list.indexOf(inst)+1;instno<bb.inst_list.size();instno++){
						Instruction laterinst=bb.inst_list.get(instno);
						if(matchInstruction(laterinst,inst)&&!replace.contains(inst)){
							replaceInstList.put(laterinst,inst);
							replace.add(laterinst);
						}		
					}
				}	
			}
			}
			replaceInst(replaceInstList,replace,bbno);
			replaceInstList.clear();	
			replace.clear();
			for (int laterbbno=bbno+1;laterbbno<BasicBlock.basicblocks.size();laterbbno++){
				if(DominatorTree.getDominators(laterbbno).contains(bbno)){		
					BasicBlock laterbb=Frontend.BasicBlock.basicblocks.get(laterbbno);
					for(int instno=0;instno<BasicBlock.basicblocks.get(laterbbno).inst_list.size();instno++){
						Instruction laterinst=BasicBlock.basicblocks.get(laterbbno).inst_list.get(instno);
						if(laterinst.getOperands().size()==2){
							if(sameInstList.containsKey(laterinst.getOperator())){
								LinkedList<Instruction> ll=new LinkedList<Instruction>(); 
								ll=sameInstList.get(laterinst.getOperator());
								for(int i=0;i<ll.size();i++){
									Instruction in=ll.get(i);
									if(matchInstruction(laterinst,in)&&!replace.contains(in)){
										replaceInstList.put(laterinst,in);
										replace.add(laterinst);
									}	
								}
							}		
						}
					}
					
					replaceInst(replaceInstList,replace,laterbbno);
					replaceInstList.clear();
					replace.clear();
				}
			}
			
		}
	}
	private static void replaceInst(HashMap<Instruction,Instruction> replaceInst,ArrayList<Instruction>remove, int no){
		BasicBlock bb;
		for(int bbno=no;bbno<BasicBlock.basicblocks.size();bbno++){
			if(DominatorTree.getDominators(bbno).contains(no)||bbno==no){
				bb=Frontend.BasicBlock.basicblocks.get(bbno);
				for(Instruction inst:bb.inst_list){
					if(inst.getOperands().size()==2){
						if(inst.getOperands().get(0).getType()==Result.Type.instruction){
							if(replaceInst.containsKey(inst.getOperands().get(0).getInstruction())){
								Instruction replaceinst=replaceInst.get(inst.getOperands().get(0).getInstruction());
								Result replacewith=new Result(Type.instruction,replaceinst);
								inst.getOperands().set(0, replacewith);
							}	
						}	
						if(inst.getOperands().get(1).getType()==Result.Type.instruction){
							if(replaceInst.containsKey(inst.getOperands().get(1).getInstruction())){
								Instruction replaceinst=replaceInst.get(inst.getOperands().get(1).getInstruction());
								Result replacewith=new Result(Type.instruction,replaceinst);
								inst.getOperands().set(1, replacewith);
							}	
						}			
					}
					else if(inst.getOperands().size()==1){
						if(inst.getOperands().get(0).getType()==Result.Type.instruction){
							if(replaceInst.containsKey(inst.getOperands().get(0).getInstruction())){
								Instruction replaceinst=replaceInst.get(inst.getOperands().get(0).getInstruction());
								Result replacewith=new Result(Type.instruction,replaceinst);
								inst.getOperands().set(0, replacewith);
							}	
						}	
							
					}
					
					}
				}
			}
		bb=Frontend.BasicBlock.basicblocks.get(no);
		for(Instruction inst:remove){
			Instruction replaceinst=replaceInst.get(inst);
			Result replacewith=new Result(Type.instruction,replaceinst);
			replaceInPhis(inst,replacewith);
			if(bb.inst_list.contains(inst))
			bb.inst_list.remove(bb.inst_list.indexOf(inst));
			if(Parser.insts.contains(inst))
			Parser.insts.remove(Parser.insts.indexOf(inst));
		}	
	}
	
	
	private static boolean matchInstruction(Instruction inst1,Instruction inst2){
		
		if (inst1.getOperator().equals(inst2.getOperator())){
		if(inst1.isOperation()==inst2.isOperation()){
			Result inst1op1=inst1.getOperands().get(0);
			Result inst1op2=inst1.getOperands().get(1);
			Result inst2op1=inst2.getOperands().get(0);
			Result inst2op2=inst2.getOperands().get(1);
			if(inst1.isOperation()==1){
				if(inst1op1.getValue()==inst2op1.getValue()&&inst1op2.getInstruction()==inst2op2.getInstruction())
					return true;
			}
			else if(inst1.isOperation()==2){
				if(inst1op1.getValue()==inst2op1.getValue()&&inst1op2.getValue()==inst2op2.getValue())
					return true;
			}
			else if(inst1.isOperation()==4){
				if(inst1op1.getInstruction()==inst2op1.getInstruction()&&inst1op2.getValue()==inst2op2.getValue())
					return true;
			}
			else if(inst1.isOperation()==3){
				if(inst1op1.getInstruction()==inst2op1.getInstruction()&&inst1op2.getInstruction()==inst2op2.getInstruction())
					return true;
			}
			else 
				return false;
			}
		else 
			return false;
	}
		return false;	
	}
	
	private static void replaceInPhis(Instruction inst, Result operand){
		for(int i=0;i<Parser.insts.size();i++){
			Instruction phi=Parser.insts.get(i);
			if(phi.getOperator()=="phi"){
				if(Parser.insts.indexOf(phi.getOperands().get(0).getInstruction())==Parser.insts.indexOf(inst)){
					phi.getOperands().set(0, operand);patchBranchInstruction(inst,phi);
				}
				else if(Parser.insts.indexOf(phi.getOperands().get(1).getInstruction())==Parser.insts.indexOf(inst)){
					phi.getOperands().set(1, operand);patchBranchInstruction(inst,phi);
				}
				
			}
		}
	}
	private static void patchBranchInstruction(Instruction inst,Instruction patch){
		for(int i=0;i<Parser.insts.size();i++){
			Instruction instruction=Parser.insts.get(i);
			if(instruction.getOperator()=="beq"||instruction.getOperator()=="ble"||instruction.getOperator()=="blt"||
					instruction.getOperator()=="bne"||instruction.getOperator()=="bge"||instruction.getOperator()=="bgt")
			{	
				if(instruction.getOperands().get(1).getInstruction()==inst){
				Result res=new Result(Type.instruction,patch);
				instruction.getOperands().set(1, res);
				
				}
				
			}
		}
	}
	
}
