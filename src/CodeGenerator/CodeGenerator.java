package CodeGenerator;
import java.io.IOException;
import java.util.*;

import Frontend.*;
import Frontend.BasicBlock.BlockType;
import Frontend.Result.Type;
import Optimizations.*;


public class CodeGenerator {
	private static ArrayList<Instruction> inline_inst_list=new ArrayList<Instruction>();
	int scratch_reg_1=26,scratch_reg_2=27;int adda_register_1=0,adda_register_2=0;
	public ArrayList<Integer> machine_insts;
	public HashMap<String,Integer> array_starting_addr=new HashMap<String,Integer>() ;
	public HashMap<String,Instruction> function_starting_addr=new HashMap<String,Instruction>();
	public HashMap<Integer,Instruction> JumpToInst=new HashMap<Integer,Instruction>();
	public HashMap<Integer,Instruction> JumpType=new HashMap<Integer,Instruction>();
	public ArrayList<Integer> JumpIndex= new ArrayList<Integer>();
	public HashMap<Instruction,Integer> InstToIndex=new HashMap<Instruction,Integer>();
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
			System.out.println(inline_inst_list.indexOf(inst)+inst.getOperator()+inst.register);
			generate_assembly(inst);	
		}	
		patchBranches();
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
		InstToIndex.put(inst, machine_insts.size());
		
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
							JumpIndex.add(machine_insts.size());
							JumpToInst.put(machine_insts.size(), oper2.getInstruction());
							JumpType.put(machine_insts.size(), inst);
							machine_insts.add(DLX.assemble(opcode, oper1_register, jump_index));
							break;
				case "beq":opcode=DLX.BEQ;
							JumpIndex.add(machine_insts.size());
							JumpToInst.put(machine_insts.size(), oper2.getInstruction());
							JumpType.put(machine_insts.size(), inst);
							machine_insts.add(DLX.assemble(opcode, oper1_register, jump_index));
							break;
				case "ble":opcode=DLX.BLE;
							JumpIndex.add(machine_insts.size());
							JumpToInst.put(machine_insts.size(), oper2.getInstruction());
							JumpType.put(machine_insts.size(), inst);
							machine_insts.add(DLX.assemble(opcode, oper1_register, jump_index));
							break;
				case "blt":opcode=DLX.BLT;
							JumpIndex.add(machine_insts.size());
							JumpToInst.put(machine_insts.size(), oper2.getInstruction());
							JumpType.put(machine_insts.size(), inst);
							machine_insts.add(DLX.assemble(opcode, oper1_register, jump_index));
							break;
				case "bge":opcode=DLX.BGE;
							JumpIndex.add(machine_insts.size());
							JumpToInst.put(machine_insts.size(), oper2.getInstruction());
							JumpType.put(machine_insts.size(), inst);
							machine_insts.add(DLX.assemble(opcode, oper1_register, jump_index));
							break;
				case "bgt":opcode=DLX.BGT;
							JumpIndex.add(machine_insts.size());
							JumpToInst.put(machine_insts.size(), oper2.getInstruction());
							JumpType.put(machine_insts.size(), inst);
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
			
			else if(oper1.getType()==Type.instruction&&oper2.getType()==Type.param){
				int param_index=oper2.param_index+1;
				int oper1_register=oper1.getInstruction().getRegister();
				int inst_register=inst.getRegister();
				int opcode;
				switch(operator){
				case "add":opcode=DLX.ADD;
							machine_insts.add(DLX.assemble(DLX.LDW,26, 28,-param_index*4 -4 ));	
							machine_insts.add(DLX.assemble(opcode,inst_register, oper1_register,26 ));	
							break;
				case "sub":opcode=DLX.SUB;
							machine_insts.add(DLX.assemble(DLX.LDW,26, 28,-param_index*4 -4 ));	
							machine_insts.add(DLX.assemble(opcode,inst_register, oper1_register,26 ));	
							break;
				case "mul":opcode=DLX.MUL;
							machine_insts.add(DLX.assemble(DLX.LDW,26, 28,-param_index*4 -4 ));	
							machine_insts.add(DLX.assemble(opcode,inst_register, oper1_register,26 ));	
							break;
				case "div":opcode=DLX.DIV;
							machine_insts.add(DLX.assemble(DLX.LDW,26, 28,-param_index*4 -4 ));	
							machine_insts.add(DLX.assemble(opcode,inst_register, oper1_register,26 ));	
							break;
				case "cmp":opcode=DLX.CMP;
							machine_insts.add(DLX.assemble(DLX.LDW,26, 28,-param_index*4 -4 ));	
							machine_insts.add(DLX.assemble(opcode,inst_register, oper1_register,26 ));	
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
				case "cmp":opcode=DLX.CMP;
							machine_insts.add(DLX.assemble(DLX.ADDI,26, 0, oper1.getValue()));	
							machine_insts.add(DLX.assemble(opcode,inst_register,26, oper2_register));	
							break;
					
				
							
				case "store":opcode=DLX.STX;
							machine_insts.add(DLX.assemble(opcode, oper1.getValue(), adda_register_1,adda_register_2));
							break;
				
				default:
					throw new IllegalArgumentException("error:Code generator wrong instruction");
				}
			}
			
			else if(oper2.getType()==Type.instruction&&oper1.getType()==Type.param){
				int param_index=oper1.param_index+1;
				int oper2_register=oper2.getInstruction().getRegister();
				int inst_register=inst.getRegister();
				int opcode;
				switch(operator){
				case "add":opcode=DLX.ADD;
							machine_insts.add(DLX.assemble(DLX.LDW,26, 28,-param_index*4 -4 ));	
							machine_insts.add(DLX.assemble(opcode,inst_register, oper2_register,26 ));	
							break;
				case "sub":opcode=DLX.SUB;
							machine_insts.add(DLX.assemble(DLX.LDW,26, 28,-param_index*4 -4 ));	
							machine_insts.add(DLX.assemble(opcode,inst_register, oper2_register,26 ));	
							break;
				case "mul":opcode=DLX.MUL;
							machine_insts.add(DLX.assemble(DLX.LDW,26, 28,-param_index*4 -4 ));	
							machine_insts.add(DLX.assemble(opcode,inst_register, oper2_register,26 ));	
							break;
				case "div":opcode=DLX.DIV;
							machine_insts.add(DLX.assemble(DLX.LDW,26, 28,-param_index*4 -4 ));	
							machine_insts.add(DLX.assemble(opcode,inst_register, oper2_register,26 ));	
							break;
				case "cmp":opcode=DLX.CMP;
							machine_insts.add(DLX.assemble(DLX.LDW,26, 28,-param_index*4 -4 ));	
							machine_insts.add(DLX.assemble(opcode,inst_register,26,oper2_register ));	
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
			
			else if(oper1.getType()==Type.number&&oper2.getType()==Type.param){
				int param_index=oper2.param_index+1;
				int inst_register=inst.getRegister();
				int opcode;
				switch(operator){
				case "add":opcode=DLX.ADD;
							machine_insts.add(DLX.assemble(DLX.LDW,26, 28,-param_index*4 -4 ));	
							machine_insts.add(DLX.assemble(DLX.ADDI,27, 0, oper1.getValue()));	
							machine_insts.add(DLX.assemble(opcode,inst_register, 27,26));	
							break;
				case "sub":opcode=DLX.SUB;
							machine_insts.add(DLX.assemble(DLX.LDW,26, 28,-param_index*4 -4 ));	
							machine_insts.add(DLX.assemble(DLX.ADDI,27, 0, oper1.getValue()));	
							machine_insts.add(DLX.assemble(opcode,inst_register, 27,26));
							break;
				case "mul":opcode=DLX.MUL;
							machine_insts.add(DLX.assemble(DLX.LDW,26, 28,-param_index*4 -4 ));	
							machine_insts.add(DLX.assemble(DLX.ADDI,27, 0, oper1.getValue()));	
							machine_insts.add(DLX.assemble(opcode,inst_register, 27,26));
							break;
				case "div":opcode=DLX.DIV;
							machine_insts.add(DLX.assemble(DLX.LDW,26, 28,-param_index*4 -4 ));	
							machine_insts.add(DLX.assemble(opcode,inst_register, 26, oper1.getValue()));	
							break;
				case "cmp":opcode=DLX.CMP;
							machine_insts.add(DLX.assemble(DLX.LDW,26, 28,-param_index*4 -4 ));	
							machine_insts.add(DLX.assemble(DLX.ADDI,27, 0, oper1.getValue()));	
							machine_insts.add(DLX.assemble(opcode,inst_register, 27, 26));	
							break;
				
				
				default:
					throw new IllegalArgumentException("error:Code generator wrong instruction");
				}
			}
			
			else if(oper1.getType()==Type.param&&oper2.getType()==Type.number){
				int param_index=oper1.param_index+1;
				int inst_register=inst.getRegister();
				int opcode;
				switch(operator){
				case "add":opcode=DLX.ADDI;
							machine_insts.add(DLX.assemble(DLX.LDW,26, 28,-param_index*4 -4 ));	
							machine_insts.add(DLX.assemble(opcode,inst_register,26,oper2.getValue()));	
							break;
				case "sub":opcode=DLX.SUBI;
							machine_insts.add(DLX.assemble(DLX.LDW,26, 28,-param_index*4 -4 ));	
							machine_insts.add(DLX.assemble(opcode,inst_register,26,oper2.getValue()));	
							break;
				case "mul":opcode=DLX.MULI;
							machine_insts.add(DLX.assemble(DLX.LDW,26, 28,-param_index*4 -4 ));	
							machine_insts.add(DLX.assemble(opcode,inst_register,26,oper2.getValue()));	
							break;
				case "div":opcode=DLX.DIVI;
							machine_insts.add(DLX.assemble(DLX.LDW,26, 28,-param_index*4 -4 ));	
							machine_insts.add(DLX.assemble(opcode,inst_register,26,oper2.getValue()));	
							break;
				case "cmp":opcode=DLX.CMPI;
							machine_insts.add(DLX.assemble(DLX.LDW,26, 28,-param_index*4 -4 ));	
							machine_insts.add(DLX.assemble(opcode,inst_register,26,oper2.getValue()));	
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
						JumpIndex.add(machine_insts.size());
						JumpToInst.put(machine_insts.size(), oper1.getInstruction());
						JumpType.put(machine_insts.size(), inst);
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
						else if(oper1.getType()==Type.param){
							int param_index=oper1.param_index+1;
							machine_insts.add(DLX.assemble(DLX.LDW,26, 28,-param_index*4 -4 ));
							machine_insts.add(DLX.assemble(opcode, 26));
						}
						else	
							throw new IllegalArgumentException("error:Code generator wrong WRD result");
						break;
						
			case "jump_else":opcode=DLX.BEQ;
					JumpIndex.add(machine_insts.size());
					JumpToInst.put(machine_insts.size(), oper1.getInstruction());
					JumpType.put(machine_insts.size(), inst);
					int jump_else=inline_inst_list.indexOf(oper1.getInstruction())-inline_inst_list.indexOf(inst);
					machine_insts.add(DLX.assemble(opcode, 0, jump_else));
					break;
						
			case "call":
				System.out.println(inst_register+"xxxxxxxxxxxxxxxxxxxx");
				Function func=Parser.Function_list.get(oper1.getName());
				ArrayList<Result> params=func.func_call_params.get(inst);
				int jump_inst=inline_inst_list.indexOf(function_starting_addr.get(oper1.getName()));
				jump_index=jump_inst-inline_inst_list.indexOf(inst);
				
				//push registers1-8
				for(int i=1;i<=8;i++){
					machine_insts.add(DLX.assemble(DLX.PSH,i, 29,4));
				}
				//push params
				if(func.func_call_params.get(inst)!=null){
				for(int i=func.func_call_params.get(inst).size()-1;i>=0;i--){
					Result param=func.func_call_params.get(inst).get(i);
					if(param.getType()==Type.instruction){
						machine_insts.add(DLX.assemble(DLX.PSH,param.getInstruction().register, 29,4));
					}
					else{ //number
						machine_insts.add(DLX.assemble(DLX.ADDI,26, 0,param.getValue()));
						machine_insts.add(DLX.assemble(DLX.PSH,26, 29,4));
					}
				}
				}
				//push FP
				machine_insts.add(DLX.assemble(DLX.PSH,28, 29,4));
				//push RA=0
				machine_insts.add(DLX.assemble(DLX.PSH,0, 29,4));
				//make FP=SP
				machine_insts.add(DLX.assemble(DLX.ADD,28, 0,29));
				

				JumpIndex.add(machine_insts.size());
				JumpToInst.put(machine_insts.size(), function_starting_addr.get(oper1.getName()));
				System.out.println(function_starting_addr.get(oper1.getName()).getOperator());
				Instruction ins=new Instruction("bsr");
				JumpType.put(machine_insts.size(), ins);
				machine_insts.add(DLX.assemble(DLX.BSR, jump_index));
				//make SP=FP
				machine_insts.add(DLX.assemble(DLX.ADD,29, 0,28));
				//pop RA to scratch register
				machine_insts.add(DLX.assemble(DLX.POP,26, 29,-4));
				//pop FP
				machine_insts.add(DLX.assemble(DLX.POP,28, 29,-4));
				//pop params
				for(int i=0;i<func.params.size();i++){
					machine_insts.add(DLX.assemble(DLX.POP,0, 29,-4));
				}
				//pop registers
				for(int i=8;i>=1;i--){
					machine_insts.add(DLX.assemble(DLX.POP,i, 29,-4));
				}
				//result scratch to register
				System.out.println(inst_register+"xxxxxxxxxxxxxxxxxxxx");
				machine_insts.add(DLX.assemble(DLX.ADD,inst_register, 0,26));
				
				
				
				break;
		case "ret":	
				if(oper1.getType()==Type.number)
					machine_insts.add(DLX.assemble(DLX.STW,oper1.getValue(), 28,0));
				else if(oper1.getType()==Type.instruction)
					machine_insts.add(DLX.assemble(DLX.STW,oper1.getInstruction().register, 28,0));
				else
					throw new IllegalArgumentException("error:Code generator wrong return type");
				//PC to next instruction
				machine_insts.add(DLX.assemble(DLX.RET,31));
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
		{	BasicBlock bb = BasicBlock.basicblocks.get(i);
			if(bb.getType()==BlockType.join){
				if(bb.inst_list.size() != 0){
					String operator=bb.getprevblock().inst_list.get(bb.getprevblock().inst_list.size()-1).getOperator();
					if(!operator.equals("ble")&&!operator.equals("blt")&&!operator.equals("bgt")&&!operator.equals("bne")&&!operator.equals("bge")&&!operator.equals("beq")){
						
						int k=0;
						for(int j=0;j<bb.inst_list.size();j++){
							if(bb.inst_list.get(j).getOperator()=="phi")
							k++;
						}
						if(k==bb.inst_list.size()){
							if(bb.getjoinblock() != null)
								bb = bb.getjoinblock();
							else
								bb = bb.getnextblock();
							k=0;
							if(bb != null && bb.inst_list != null){
							for(int j=0;j<bb.inst_list.size();j++){
								if(bb.inst_list.get(j).getOperator()=="phi")
								k++;
							}}
						}
					if(bb!=null){	
					Result jump_to=new Result(Type.instruction,bb.inst_list.get(k));
					Instruction jump_else= new Instruction("jump_else",jump_to);
					bb.getprevblock().inst_list.add(jump_else);}
					}
				}
				else
				{
					if(bb.getjoinblock() != null)
						bb = bb.getjoinblock();
					else
						bb = bb.getnextblock();
					String operator=bb.getprevblock().inst_list.get(bb.getprevblock().inst_list.size()-1).getOperator();
					if(!operator.equals("ble")&&!operator.equals("blt")&&!operator.equals("bgt")&&!operator.equals("bne")&&!operator.equals("bge")&&!operator.equals("beq")){
					
					int k=0;
					for(int j=0;j<bb.inst_list.size();j++){
						if(bb.inst_list.get(j).getOperator()=="phi")
							k++;
					}
					Result jump_to=new Result(Type.instruction,bb.inst_list.get(k));
					Instruction jump_else= new Instruction("jump_else",jump_to);
					bb.getprevblock().getprevblock().inst_list.add(jump_else);
					}
				}
			}
			if(bb!=null && bb.getType()==BlockType.whileblock){
				BasicBlock newbb=bb.getwhiletodo();
				if(newbb.inst_list.get(newbb.inst_list.size()-1).getOperator()=="bra"){
					int k=0;
					for(int j=0;j<bb.inst_list.size();j++){
						if(bb.inst_list.get(j).getOperator()=="phi")
							k++;
					}
					Instruction bra=newbb.inst_list.get(newbb.inst_list.size()-1);
					Instruction patchbra=bb.inst_list.get(k);
					Result patchbraup=new Result(Type.instruction,patchbra);
					bra.getOperands().set(0, patchbraup);
				}
				else
					System.out.println("RA bra inst patchup fail");
			}

			if(bb != null && bb.getType()==BlockType.join){
				BasicBlock newbb=bb.getprevblock2();
				if(newbb != null && newbb.inst_list.size() != 0){
				String operator=newbb.inst_list.get(newbb.inst_list.size()-1).getOperator();
				if(operator.equals("ble")||operator.equals("blt")||operator.equals("bgt")||operator.equals("bne")||operator.equals("bge")||operator.equals("beq")){
					
					int k=0;
					for(int j=0;j<bb.inst_list.size();j++){
						if(bb.inst_list.get(j).getOperator()=="phi")
							k++;
					}
					if(k==bb.inst_list.size()){
						if(bb.getjoinblock() != null)
							bb = bb.getjoinblock();
						else
							bb = bb.getnextblock();
						k=0;
						for(int j=0;j<bb.inst_list.size();j++){
							if(bb.inst_list.get(j).getOperator()=="phi")
							k++;
						}
					}
					Instruction bra=newbb.inst_list.get(newbb.inst_list.size()-1);
					Instruction patchbra=bb.inst_list.get(k);
					Result patchbraup=new Result(Type.instruction,patchbra);
					bra.getOperands().set(1, patchbraup);
				}
				else
					System.out.println("RA bra inst patchup failkjkjklp");
				}
				 newbb=bb.getprevblock();
				if(newbb != null && newbb.inst_list.size() != 0){
				String operator=newbb.inst_list.get(newbb.inst_list.size()-1).getOperator();
				
				if(operator.equals("ble")||operator.equals("blt")||operator.equals("bgt")||operator.equals("bne")||operator.equals("bge")||operator.equals("beq")){
					
					int k=0;
					for(int j=0;j<bb.inst_list.size();j++){
						if(bb.inst_list.get(j).getOperator()=="phi")
							k++;
					}
					if(k==bb.inst_list.size()){
						if(bb.getjoinblock() != null)
							bb = bb.getjoinblock();
						else
							bb = bb.getnextblock();
						k=0;
						for(int j=0;j<bb.inst_list.size();j++){
							if(bb.inst_list.get(j).getOperator()=="phi")
							k++;
						}
					}
					Instruction bra=newbb.inst_list.get(newbb.inst_list.size()-1);
					Instruction patchbra=bb.inst_list.get(k);
					Result patchbraup=new Result(Type.instruction,patchbra);
					bra.getOperands().set(1, patchbraup);
				}
				else
					System.out.println("RA bra inst patchup fail");
				}
			}
			if(bb!=null && bb.getType()==BlockType.follow){
				BasicBlock newbb=bb.getprevblock();
				if(newbb != null && newbb.inst_list.size() != 0){
				String operator=newbb.inst_list.get(newbb.inst_list.size()-1).getOperator();
				if(operator.equals("ble")||operator.equals("blt")||operator.equals("bgt")||operator.equals("bne")||operator.equals("bge")||operator.equals("beq")){
					int k=0;
					for(int j=0;j<bb.inst_list.size();j++){
						if(bb.inst_list.get(j).getOperator()=="phi")
							k++;
					}
					if(k==bb.inst_list.size()){
						if(bb.getjoinblock() != null)
							bb = bb.getjoinblock();
						else
							bb = bb.getnextblock();
						k=0;
						for(int j=0;j<bb.inst_list.size();j++){
							if(bb.inst_list.get(j).getOperator()=="phi")
							k++;
						}
					}
					Instruction bra=newbb.inst_list.get(newbb.inst_list.size()-1);
					Instruction patchbra=bb.inst_list.get(k);
					Result patchbraup=new Result(Type.instruction,patchbra);
					bra.getOperands().set(1, patchbraup);
				}
				else
					System.out.println("RA bra inst patchup failkjkjklp");
				}
			}
		}
		for(int i=BasicBlock.mainblock.getblockno();i< BasicBlock.basicblocks.size();i++)
		{
			BasicBlock bb = BasicBlock.basicblocks.get(i);
			for(Instruction inst:bb.inst_list)
			{	if(!inst.getOperator().equals("phi"))
				list.add(inst);
			}
		}
		for(Function func:Parser.Functions){
			if(!func.getfirstbb().inst_list.isEmpty())
				function_starting_addr.put(func.funcname, func.getfirstbb().inst_list.get(0));
			else
				function_starting_addr.put(func.funcname, func.getfirstbb().getnextblock().inst_list.get(0));
			for(int i=func.getfirstbb().getblockno();i<= func.getreturnbb().getblockno();i++){
				BasicBlock bb = BasicBlock.basicblocks.get(i);
				for(Instruction inst:bb.inst_list)
				{if(!inst.getOperator().equals("phi"))
					list.add(inst);
				}
			}
		}
		return list;
	}
	public void patchBranches(){
		for(int i:JumpIndex){		//i-jump inst index
			int jump_by=0;
			System.out.println("jump by: "+i);
			Instruction JumpTo=JumpToInst.get(i);
			Instruction JumpInst=JumpType.get(i);
			System.out.println("jump by: "+InstToIndex.get(JumpTo)+JumpTo.getPhiVar()+JumpTo.register+JumpInst.getOperator());
			if(InstToIndex!=null)
				jump_by=InstToIndex.get(JumpTo)-i;
			String operator=JumpInst.getOperator();
			int opcode;
			switch(operator){
			
			case "bne":opcode=DLX.BNE; 
					machine_insts.set(i, DLX.assemble(opcode, JumpInst.getOperands().get(0).getInstruction().register, jump_by));
					break;
			case "beq":opcode=DLX.BEQ;
					machine_insts.set(i, DLX.assemble(opcode, JumpInst.getOperands().get(0).getInstruction().register, jump_by));
					break;
			case "ble":opcode=DLX.BLE;
					machine_insts.set(i, DLX.assemble(opcode, JumpInst.getOperands().get(0).getInstruction().register, jump_by));
					break;
			case "blt":opcode=DLX.BLT;
					machine_insts.set(i, DLX.assemble(opcode, JumpInst.getOperands().get(0).getInstruction().register, jump_by));
					break;
			case "bge":opcode=DLX.BGE;
					machine_insts.set(i, DLX.assemble(opcode, JumpInst.getOperands().get(0).getInstruction().register, jump_by));
					break;
			case "bgt":opcode=DLX.BGT;
					machine_insts.set(i, DLX.assemble(opcode, JumpInst.getOperands().get(0).getInstruction().register, jump_by));
					break;
			case "bsr":opcode=DLX.BSR;
					machine_insts.set(i, DLX.assemble(opcode, jump_by));
					break;
			case "bra":opcode=DLX.BEQ;
					machine_insts.set(i, DLX.assemble(opcode,0, jump_by));
					break;
			case "jump_else":opcode=DLX.BEQ;
					machine_insts.set(i, DLX.assemble(opcode,0, jump_by));
					break;
			}		

		}
	}
}
