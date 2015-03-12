package Frontend;
import java.util.ArrayList;

import Graph.*;
import Frontend.Result.Type;

public class Instruction {
	private int type;   //0-zero operands,1->one operands,2->two operands,3->phi instruction
	private String var;
	private String operator;
	private ArrayList<Result> operands=new ArrayList<Result>();
	public BasicBlock basicblock;
	public int block_id;
	public int register;
	public int cluster;
	public boolean is_cluster;
	public Instruction PhitoMove;
	public Instruction(){}
	
	Instruction(String instoperator){
		//if (instoperator=="end"||instoperator=="read"||instoperator=="writeNL"){
			this.type=0;
			this.operator=instoperator;
			this.is_cluster =false;
			this.cluster = 0;
		}
	public Instruction(String instoperator,Result res){
//		else if(instoperator=="neg"||instoperator=="load"||instoperator=="bra"||instoperator=="write"){
			this.type=1;
			this.operator=instoperator;
			this.operands.add(res);
			this.is_cluster =false;
			this.cluster = 0;
		}
	Instruction(String instoperator,int op1,Result op2){
//		else if(instoperator=="neg"||instoperator=="load"||instoperator=="bra"||instoperator=="write"){
			this.type=1;
			this.operator=instoperator;
			this.operands.add(op2);
			this.is_cluster =false;
			this.cluster = 0;
		}
	public Instruction(String instoperator,Result res1,Result res2){
		//else if(instoperator=="add"||instoperator=="sub"||instoperator=="mul"||instoperator=="div"||instoperator=="cmp"||instoperator=="adda"||instoperator=="store"||instoperator=="move"||instoperator=="bne"||instoperator=="beq"||instoperator=="ble"||instoperator=="blt"||instoperator=="bge"||instoperator=="bgt"){
			this.type=2;
			this.operator=instoperator;
			this.operands.add(res1);
			this.operands.add(res2);
			this.is_cluster =false;
			this.cluster = 0;
		}
	Instruction(String instoperator,String var,Result res1,Result res2)
	{
		this.type=3;
		this.operator=instoperator;
		this.var = var;
		this.operands.add(res1);
		this.operands.add(res2);
		this.is_cluster =false;
		this.cluster = 0;
	}
	Instruction(String instoperator,ArrayList<Result> instoperands){
		//else if(instoperator=="phi"){
			this.type=3;
			this.operator=instoperator;
			this.operands=instoperands;
			this.is_cluster =false;
			this.cluster = 0;
		}
		//else
			//System.out.println("Error:Invalid Instruction");
	

	public int getType(){
		return this.type;
	}
	public int getbb(){
		return this.block_id;
	}
	public String getOperator(){
		return this.operator;
	}
	public ArrayList<Result> getOperands(){
		return this.operands;
	}
	public String getPhiVar(){
		return this.var;
	}
	public int getRegister(){
		return this.register;
	}
	public boolean isConstantAssignment(){
		if(this.getOperator()=="move"){
			if((this.operands.get(0).getType()==Type.number)&&(this.operands.get(1).getType()==Type.variable)){
				return true;
			}
			else
				return false;
		}
		else 
			return false;
	}
	public boolean isVariableAssignment(){
		if(this.getOperator()=="move"){
			if(this.operands.get(0).getType()==Type.instruction&&(this.operands.get(1).getType()==Type.variable||this.operands.get(1).getType()==Type.arr)){
				Result op1=this.operands.get(0);
				//if(op1.getInstruction().getOperator()=="move"||isReadAssignment()){
					return true;
				//}
			//	else 
				//	return false;
			}
			else
				return false;
		}
		else 
			return false;
	}
	public boolean isReadAssignment(){
		if(this.getOperator()=="read"){
			if(this.operands.get(0).getType()==Type.variable)
					return true;
			else 
					return false;
		}
		else 
			return false;
	}
	public void removeUselessPhi(){
		boolean b;
		if(this.operands.get(0).getType()==Type.instruction){
			Instruction op1=operands.get(0).getInstruction();
			int op1bb=op1.basicblock.getblockno();
			Instruction op2=operands.get(1).getInstruction();
			int op2bb=op2.basicblock.getblockno();
			int dombb=DominatorTree.getDominators(this.basicblock.getblockno()).getLast();
			if((op1bb<=dombb&&op2bb<=dombb))
				b= true;
			else if(!(op2bb<=dombb&&op1bb>this.basicblock.getblockno()))
				b= true;
			else
				b= false;
		}
		else 
			b=true;
		if(b){
			for(int i=0;i<Parser.insts.size()-1;i++){
				Instruction inst=Parser.insts.get(i);
				if(inst.operands.get(0).getType()==Type.instruction){
					if(Parser.insts.indexOf(inst.getOperands().get(0).getInstruction())==Parser.insts.indexOf(this)){
						inst.getOperands().set(0, this.operands.get(0));
					}
				}
				if(inst.operands.get(1).getType()==Type.instruction){
					if(Parser.insts.indexOf(inst.getOperands().get(1).getInstruction())==Parser.insts.indexOf(this)){
						inst.getOperands().set(1, this.operands.get(0));
					}
				}
			}
		}
	}	
	public int isOperation(){  
		if((this.operands.get(0).getType()==Type.number)&&(this.operands.get(1).getType()==Type.variable))
			return 1;
		else if((this.operands.get(0).getType()==Type.number)&&(this.operands.get(1).getType()==Type.number))
			return 2;
		else if((this.operands.get(0).getType()==Type.variable)&&(this.operands.get(1).getType()==Type.variable))
			return 3;
		else if((this.operands.get(0).getType()==Type.variable)&&(this.operands.get(1).getType()==Type.number))
			return 4;
		else 
			return 0;
	}
	
}
