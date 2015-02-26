package Optimizations;
import Frontend.*;
import Frontend.Result.Type;
import Graph.*;

import java.util.*;

public class CSE {
	private static ArrayList<Instruction> differentInstList=new ArrayList<Instruction>();
	private static HashMap<Integer,HashMap<Instruction,Instruction>> sameInstList=new HashMap<Integer,HashMap<Instruction,Instruction>>();
	public static void doCSE(){
		makeList();
		replaceInst();
	}
	private static void replaceInst(){
		BasicBlock bb;
		for(int bbno=0;bbno<BasicBlock.basicblocks.size();bbno++){
			bb=Frontend.BasicBlock.basicblocks.get(bbno);
			for(Instruction inst:bb.inst_list){
				if(sameInstList.containsKey(bbno)){
					if(sameInstList.get(bbno).containsKey(inst)){
						Instruction replacewith=sameInstList.get(bbno).get(inst);
						Result replacement=new Result(Type.instruction,replacewith);
						for(int instno=bb.inst_list.indexOf(inst)+1;instno<bb.inst_list.size();instno++){
							Instruction laterinst=bb.inst_list.get(instno);
							if(laterinst.getOperands().size()==2){
								if(laterinst.getOperands().get(0).getType()==Result.Type.instruction){
									if(laterinst.getOperands().get(0).getInstruction()==inst)
										laterinst.getOperands().set(0, replacement);
								}	
								if(laterinst.getOperands().get(1).getType()==Result.Type.instruction){
									if(laterinst.getOperands().get(1).getInstruction()==inst){
										laterinst.getOperands().set(1, replacement);
									}
								}
							}
						}
						for (int laterbb=bbno+1;laterbb<BasicBlock.basicblocks.size();laterbb++){
								for(int instno=0;instno<BasicBlock.basicblocks.get(laterbb).inst_list.size();instno++){
									Instruction laterinst=bb.inst_list.get(instno);
									if(laterinst.getOperands().size()==2){
										if(laterinst.getOperands().get(0).getType()==Result.Type.instruction){
											if(laterinst.getOperands().get(0).getInstruction()==inst)
												laterinst.getOperands().set(0, replacement);
										}	
										if(laterinst.getOperands().get(1).getType()==Result.Type.instruction){
											if(laterinst.getOperands().get(1).getInstruction()==inst){
												laterinst.getOperands().set(1, replacement);
											}
										}
									}
								}
						}		

						bb.inst_list.remove(bb.inst_list.indexOf(inst));
						Parser.insts.remove(Parser.insts.indexOf(inst));
					}
				}
			}
		}	
	}
	
	private static void makeList(){
		BasicBlock bb;
		for(int bbno=0;bbno<BasicBlock.basicblocks.size();bbno++){
			bb=Frontend.BasicBlock.basicblocks.get(bbno);
			for(Instruction inst:bb.inst_list){
				if(inst.getOperands().size()==2){
					for(Instruction listInst:differentInstList){
						if(matchInstruction(inst,listInst)){
							HashMap<Instruction,Instruction> insttoinst=new HashMap<Instruction,Instruction>();
							insttoinst.put(inst, listInst);
							sameInstList.put(bbno,insttoinst);
						}	
					}
				}
			}
		}
	}
	
	private static boolean matchInstruction(Instruction inst1,Instruction inst2){
		if (inst1.getOperator()==inst2.getOperator()){	
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
	
}
