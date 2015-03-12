package CodeGenerator;
import java.io.IOException;
import java.util.*;

import Frontend.*;
import Frontend.Result.Type;
import Optimizations.*;


public class CodeGenerator {
	private static ArrayList<Instruction> inline_inst_list=new ArrayList<Instruction>();
	int scratch_reg_1=26,scratch_reg_2=27;int adda_register_1=0,adda_register_2=0;
	public ArrayList<Integer> machine_insts;
	public HashMap<String,Integer> array_starting_addr=new HashMap<String,Integer>() ;
	public CodeGenerator(){
		inline_inst_list=generate_inline_inst_list();
		machine_insts=new ArrayList<Integer>();
		int opcode=DLX.ADDI;
		machine_insts.add(DLX.assemble(opcode, 29, 0, 5000));//stack pointer
		machine_insts.add(DLX.assemble(opcode, 28, 0, 5000));//frame pointer
		int SP=5000;
		for(Result array: Parser.array_list){
			int size=1;
			for(int i:array.getArraySize()){
				size*=i;
			}
			String name=array.getName();
			int start_addr=SP-5000;
			array_starting_addr.put(name, start_addr);
			SP+=size*4;
			machine_insts.add(DLX.assemble(DLX.ADDI, 29, 0, SP));//stack pointer
		}
		for(Instruction inst:inline_inst_list){
			System.out.println(inline_inst_list.indexOf(inst)+inst.getOperator());
			generate_assembly(inst);	
		}	
		int[] inst_list = new int[machine_insts.size()];
	    int i = 0;
	    for (Integer n : machine_insts) {
	    	System.out.println(DLX.disassemble(n));
	        inst_list[i++] = n;
	    }
	    DLX.load(inst_list);
	    try{
	    	DLX.execute();
	    }
	    catch(IOException e){
	    	e.printStackTrace();
	    }
	    	
	    
	}
		
	public void generate_assembly(Instruction inst){
		int operand_size=inst.getOperands().size();
		

//for double operand
						
		if(operand_size==2){
			String operator=inst.getOperator();
			Result oper1=inst.getOperands().get(0);
			Result oper2=inst.getOperands().get(1);
			if(oper1.getType()==Type.instruction&&oper2.getType()==Type.instruction){
				int oper1_register=oper1.getInstruction().getRegister();
				int oper2_register=oper2.getInstruction().getRegister();
				int jump_index=inline_inst_list.indexOf(oper2.getInstruction())-inline_inst_list.indexOf(inst);
				System.out.println(inline_inst_list.indexOf(oper2.getInstruction())+"+"+inline_inst_list.indexOf(inst));
				int inst_register=inst.getRegister();
				int opcode;
				switch(operator){
				case "add": opcode=DLX.ADD;
							machine_insts.add(DLX.assemble(opcode, inst_register, oper1_register, oper2_register));
							break;
				case "sub":opcode=DLX.SUB;
							machine_insts.add(DLX.assemble(opcode, inst_register, oper1_register, oper2_register));
							break;
				case "mul":opcode=DLX.MUL;
							machine_insts.add(DLX.assemble(opcode, inst_register, oper1_register, oper2_register));
							break;
				case "div":opcode=DLX.DIV;
							machine_insts.add(DLX.assemble(opcode, inst_register, oper1_register, oper2_register));
							break;
				case "cmp":opcode=DLX.CMP;
							machine_insts.add(DLX.assemble(opcode, inst_register, oper1_register, oper2_register));
							break;
	
						
						
				case "bne":opcode=DLX.BNE;
							machine_insts.add(DLX.assemble(opcode, oper1_register, jump_index));
							break;
				case "beq":opcode=DLX.BEQ;
							machine_insts.add(DLX.assemble(opcode, oper1_register, jump_index));
							break;
				case "ble":opcode=DLX.BLE;
							machine_insts.add(DLX.assemble(opcode, oper1_register, jump_index));
							break;
				case "blt":opcode=DLX.BLT;
							machine_insts.add(DLX.assemble(opcode, oper1_register, jump_index));
							break;
				case "bge":opcode=DLX.BGE;
							machine_insts.add(DLX.assemble(opcode, oper1_register, jump_index));
							break;
				case "bgt":opcode=DLX.BGT;
							machine_insts.add(DLX.assemble(opcode, oper1_register, jump_index));
							break;
				
							
				case "adda":
							adda_register_1=oper1_register;
							adda_register_2=oper2_register;
							break;
				case "store":
						opcode=DLX.STX;
							machine_insts.add(DLX.assemble(opcode, oper1_register, adda_register_1,adda_register_2));
							break;
							
							
				default:
					throw new IllegalArgumentException("error:Code generator wrong instruction");
				}
			}
			
			
			
			
			
			else if(oper1.getType()==Type.instruction&&oper2.getType()==Type.number){
				int oper1_register=oper1.getInstruction().getRegister();
				int inst_register=inst.getRegister();
				int opcode;
				switch(operator){
				case "add":opcode=DLX.ADDI;
							machine_insts.add(DLX.assemble(opcode,inst_register, oper1_register, oper2.getValue()));	
							break;
				case "sub":opcode=DLX.SUBI;
							machine_insts.add(DLX.assemble(opcode,inst_register, oper1_register, oper2.getValue()));	
							break;
				case "mul":opcode=DLX.MULI;
							machine_insts.add(DLX.assemble(opcode,inst_register, oper1_register, oper2.getValue()));	
							break;
				case "div":opcode=DLX.DIVI;
							machine_insts.add(DLX.assemble(opcode,inst_register, oper1_register, oper2.getValue()));	
							break;
				case "cmp":opcode=DLX.CMPI;
							machine_insts.add(DLX.assemble(opcode,inst_register, oper1_register, oper2.getValue()));	
							break;
				case "move":opcode=DLX.ADD;
							machine_insts.add(DLX.assemble(opcode,oper2.getValue(), 0, oper1_register));	
							break;	
				
				default:
					throw new IllegalArgumentException("error:Code generator wrong instruction"+operator);
				}
			}
			
			
			
			
			else if(oper2.getType()==Type.instruction&&oper1.getType()==Type.number){
				int oper2_register=oper2.getInstruction().getRegister();
				int inst_register=inst.getRegister();
				int opcode;
				switch(operator){
				case "add":opcode=DLX.ADDI;
							machine_insts.add(DLX.assemble(opcode,inst_register, oper2_register, oper1.getValue()));	
							break;
				case "sub":opcode=DLX.SUBI;
							machine_insts.add(DLX.assemble(opcode,inst_register, oper2_register, oper1.getValue()));	
							break;
				case "mul":opcode=DLX.MULI;
							machine_insts.add(DLX.assemble(opcode,inst_register, oper2_register, oper1.getValue()));	
							break;
				case "div":opcode=DLX.DIVI;
							machine_insts.add(DLX.assemble(opcode,inst_register, oper2_register, oper1.getValue()));	
							break;
				case "cmp":opcode=DLX.CMPI;
							machine_insts.add(DLX.assemble(opcode,inst_register, oper2_register, oper1.getValue()));	
							break;
					
				
							
				case "store":opcode=DLX.STX;
							machine_insts.add(DLX.assemble(opcode, oper1.getValue(), adda_register_1,adda_register_2));
							break;
				
				default:
					throw new IllegalArgumentException("error:Code generator wrong instruction");
				}
			}
			
			
			
			
			else if(oper1.getType()==Type.number&&oper2.getType()==Type.number){
			
				int inst_register=inst.getRegister();
				int opcode;
				switch(operator){
				case "add":opcode=DLX.ADDI;
							machine_insts.add(DLX.assemble(opcode,inst_register, 0, oper1.getValue()+oper2.getValue()));	
							break;
				case "sub":opcode=DLX.ADDI;
							machine_insts.add(DLX.assemble(opcode,inst_register, 0, oper1.getValue()-oper2.getValue()));	
							break;
				case "mul":opcode=DLX.ADDI;
							machine_insts.add(DLX.assemble(opcode,inst_register, 0, oper1.getValue()*oper2.getValue()));	
							break;
				case "div":opcode=DLX.ADDI;
							machine_insts.add(DLX.assemble(opcode,inst_register, 0, oper1.getValue()/oper2.getValue()));	
							break;
				case "cmp":opcode=DLX.CMPI;
							machine_insts.add(DLX.assemble(opcode,inst_register,0,oper2.getValue()-oper1.getValue()));	
							break;
				case "move":opcode=DLX.ADDI;
							System.out.println(oper2.getValue()+" "+oper1.getValue());
							machine_insts.add(DLX.assemble(opcode,oper2.getValue(), 0, oper1.getValue()));	
							break;	
				
				default:
					throw new IllegalArgumentException("error:Code generator wrong instruction");
				}
			}
			else if(oper1.getType()==Type.arr&&oper2.getType()==Type.FP){
				int inst_register=inst.getRegister();
				int opcode=DLX.ADDI;
				machine_insts.add(DLX.assemble(opcode,inst_register,28,array_starting_addr.get(oper1.getName())));	
			}
			else if(oper1.getType()==Type.instruction&&oper2.getType()==Type.arr){
				int inst_register=inst.getRegister();
				int opcode=DLX.ADD;
				machine_insts.add(DLX.assemble(opcode,inst_register,0,oper1.getInstruction().register));	
			}
			else if(oper1.getType()==Type.number&&oper2.getType()==Type.arr){
				int inst_register=inst.getRegister();
				int opcode=DLX.ADDI;
				machine_insts.add(DLX.assemble(opcode,inst_register,0,oper1.getValue()));	
			}
			else
				throw new IllegalArgumentException("error:Code generator wrong operand type");
		}
		
		
//for single operand
		
		else if(operand_size==1){
			String operator=inst.getOperator();
			Result oper1=inst.getOperands().get(0);
			int inst_register=inst.getRegister();
			int opcode;
			switch(operator){
			case "load":opcode=DLX.LDX;
						machine_insts.add(DLX.assemble(opcode, inst_register, adda_register_1,adda_register_2));
						break;
				
			case "bra":opcode=DLX.BEQ;
						int jump_index=inline_inst_list.indexOf(oper1.getInstruction())-inline_inst_list.indexOf(inst);
						machine_insts.add(DLX.assemble(opcode, 0, jump_index));
						break;
			
			case "write":opcode=DLX.WRD;
						if(oper1.getType()==Type.instruction){
							machine_insts.add(DLX.assemble(opcode,oper1.getInstruction().getRegister()));
							
						}
						else if(oper1.getType()==Type.number){
							machine_insts.add(DLX.assemble(DLX.ADDI, scratch_reg_1, 0, oper1.getValue()));
							machine_insts.add(DLX.assemble(opcode, scratch_reg_1));
						}
						else
							throw new IllegalArgumentException("error:Code generator wrong WRD result");
						break;
			
			default:
					throw new IllegalArgumentException("error:Code generator wrong instruction");
			
			}
		}
		
		
		
//for no operand		
		else if(operand_size==0){
			String operator=inst.getOperator();
			int inst_register=inst.getRegister();
			int opcode;
			switch(operator){
			case "end":opcode=DLX.RET;
						machine_insts.add(DLX.assemble(opcode, 0));
						break;
			case "read":opcode=DLX.RDI;
						machine_insts.add(DLX.assemble(opcode, inst_register));
						break;
			case "writeNL":opcode=DLX.WRL;
						machine_insts.add(DLX.assemble(opcode));
						break;
			default:
				throw new IllegalArgumentException("error:Code generator wrong instruction");
			}
		}
		else
			throw new IllegalArgumentException("error:Code generator wrong operands");
	}
	
	public ArrayList<Instruction> generate_inline_inst_list(){
		ArrayList<Instruction> list=new ArrayList<Instruction>();
		for(int i=0;i< BasicBlock.basicblocks.size();i++)
		{
			BasicBlock bb = BasicBlock.basicblocks.get(i);
			for(Instruction inst:bb.inst_list)
			{list.add(inst);
			}
		}
		return list;
	}
}
