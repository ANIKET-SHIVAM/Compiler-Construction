package Optimizations;
import Frontend.*;
import Frontend.Result.Type;
import Graph.*;
import java.util.*;


public class CP {
	public static void doCP(){
		BasicBlock bb;
		
		for (int bbno=0;bbno<BasicBlock.basicblocks.size();bbno++){
			bb=Frontend.BasicBlock.basicblocks.get(bbno);
			ArrayList<Instruction> deleteinst=new ArrayList<Instruction>();
			for(Instruction inst:bb.inst_list){
				Result operand=new Result();boolean copy=false,firstpatch=true;Instruction patch=new Instruction();
				if(inst.isConstantAssignment()){
					operand=inst.getOperands().get(0);
					copy=true;
				}
				else if(inst.isVariableAssignment()){
					operand=inst.getOperands().get(0);
					copy=true;
				}
				else if(inst.getOperator()=="phi"){				//remove useless phi
					inst.removeUselessPhi();
				}
				
				
				if(copy){
				for(int instno=bb.inst_list.indexOf(inst)+1;instno<bb.inst_list.size();instno++){
					Instruction laterinst=bb.inst_list.get(instno);
					if(laterinst.getOperator()=="end")
						break;
					if(laterinst.getOperator()!="bra"&&laterinst.getOperator()!="read"){
						if(laterinst.getOperands().get(0).getType()==Result.Type.instruction){
							if(Parser.insts.indexOf(laterinst.getOperands().get(0).getInstruction())==Parser.insts.indexOf(inst)){
								{laterinst.getOperands().set(0, operand);
								if(firstpatch){
									patch=laterinst;
									firstpatch=false;
								}
								}
							}
						}	
						if(laterinst.getOperands().get(1).getType()==Result.Type.instruction){
							if(Parser.insts.indexOf(laterinst.getOperands().get(1).getInstruction())==Parser.insts.indexOf(inst)){
								{laterinst.getOperands().set(1, operand);
								if(firstpatch){
									patch=laterinst;
									firstpatch=false;
								}
								}
							}
						}
					}
				}
				
				
				for (int laterbb=bbno+1;laterbb<BasicBlock.basicblocks.size();laterbb++){
					if(DominatorTree.getDominators(laterbb)==bbno){
						
						for(int instno=0;instno<BasicBlock.basicblocks.get(laterbb).inst_list.size();instno++){
							Instruction laterinst=BasicBlock.basicblocks.get(laterbb).inst_list.get(instno);
							if(laterinst.getOperator()=="end")
								break;
							
							if(laterinst.getOperator()!="bra"&&laterinst.getOperator()!="read"){	
								if(laterinst.getOperands().get(0).getType()==Result.Type.instruction){
									if(Parser.insts.indexOf(laterinst.getOperands().get(0).getInstruction())==Parser.insts.indexOf(inst)){
											laterinst.getOperands().set(0, operand);
											if(firstpatch){
												patch=laterinst;
												firstpatch=false;
											}
									}
								}	
								if(laterinst.getOperands().get(1).getType()==Result.Type.instruction){
									if(Parser.insts.indexOf(laterinst.getOperands().get(1).getInstruction())==Parser.insts.indexOf(inst)){
										laterinst.getOperands().set(1, operand);
										if(firstpatch){
											patch=laterinst;
											firstpatch=false;
										}
									}
								}
							}
						}
					}
					
				}
					deleteinst.add(inst);
					replaceInPhis(inst,operand);
				}
				if(!firstpatch){
					patchBranchInstruction(inst,patch);
				}
				
				
			}
			for(int i=0;i<deleteinst.size();i++){
				bb.inst_list.remove(bb.inst_list.indexOf(deleteinst.get(i)));
				Parser.insts.remove(Parser.insts.indexOf(deleteinst.get(i)));
			}
		}
		
	}
	private static void replaceInPhis(Instruction inst, Result operand){
		for(int i=0;i<Parser.insts.size();i++){
			Instruction phi=Parser.insts.get(i);
			if(phi.getOperator()=="phi"){
				if(Parser.insts.indexOf(phi.getOperands().get(0).getInstruction())==Parser.insts.indexOf(inst)){
					phi.getOperands().set(0, operand);patchBranchInstruction(inst,phi);
					System.out.println("asdasdasd"+Parser.insts.indexOf(phi));
				}
				else if(Parser.insts.indexOf(phi.getOperands().get(1).getInstruction())==Parser.insts.indexOf(inst)){
					phi.getOperands().set(1, operand);patchBranchInstruction(inst,phi);
					System.out.println("asdasdasd"+Parser.insts.indexOf(phi));
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
