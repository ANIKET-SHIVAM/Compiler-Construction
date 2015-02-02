package Frontend;
import java.util.ArrayList;

public class Instruction {
	private int type;   //0-zero operands,1->one operands,2->two operands,3->phi instruction
	private String operator;
	private ArrayList<String> operands=new ArrayList<>();
	
	
	Instruction(String instoperator, ArrayList<String> instoperands){
		if (instoperator=="end"||instoperator=="read"||instoperator=="writeNL"){
			this.type=0;
			this.operator=instoperator;
			this.operands=null;
		}
		else if(instoperator=="neg"||instoperator=="load"||instoperator=="bra"||instoperator=="write"){
			this.type=1;
			this.operator=instoperator;
			this.operands=instoperands;
		}
		else if(instoperator=="add"||instoperator=="sub"||instoperator=="mul"||instoperator=="div"||instoperator=="cmp"||instoperator=="adda"||instoperator=="store"||instoperator=="move"||instoperator=="bne"||instoperator=="beq"||instoperator=="ble"||instoperator=="blt"||instoperator=="bge"||instoperator=="bgt"){
			this.type=2;
			this.operator=instoperator;
			this.operands=instoperands;
		}
		else if(instoperator=="phi"){
			this.type=3;
			this.operator=instoperator;
			this.operands=instoperands;
		}
		else
			System.out.println("Error:Invalid Instruction");
	}

	public int getType(){
		return this.type;
	}
	public String getOperator(){
		return this.operator;
	}
	public ArrayList<String> getOperands(){
		return this.operands;
	}
	
}
