package Frontend;

public class Result {
	public enum Type{
		variable,
		number,
		register,
		condition,
		arr,
		function,
		param,
		instruction
	}
	
	public enum Cond_Type{
		bne,beq,ble,blt,bge,bgt
	}
	
	private Type kind;
	private String name;
	private int regno;//for register
	private Cond_Type condition;
	private Instruction fixup_location;
	private int value; //for constant
	private int index;//for variable
	private int[] arr;//for array
	private Instruction ins;	//for instruction pointer
	
	Result(){}

	Result(Type kind,TokenType ss)
	{
		if(kind==Type.condition){

			this.kind= kind;

			if (ss==TokenType.neqToken)			//"!="

				condition=Cond_Type.beq;

			else if (ss==TokenType.eqlToken)	//"=="

				condition=Cond_Type.bne;

			else if (ss==TokenType.leqToken)	//"<="

				condition=Cond_Type.bgt;

			else if (ss==TokenType.lssToken)	//"<"

				condition=Cond_Type.bge;

			else if (ss==TokenType.geqToken)	//">="

				condition=Cond_Type.blt;

			else if (ss==TokenType.gtrToken)	//">"

				condition=Cond_Type.ble;
		}
	}
	
	Result(Type kind,String s)
	{
		this.kind = kind;
		this.name = s;
	}
	
	Result(Type kind,Instruction i)
	{
		this.kind = kind;
		this.ins = i;
	}
	
	Result(Type kind,String ss,int val)
	{
		this.kind = kind;
		this.name = ss;
		this.arr = new int[val];
	}
	
	Result(Type kind,int value){
		if(kind==Type.variable){
			this.kind=kind;
			this.index=value;
		}
		else if(kind==Type.number){
			this.kind=kind;
			this.value=value;
		}
		else if(kind==Type.register){
			this.kind=kind;
			this.value=regno;
		}
		else if(kind == Type.arr)
		{
			this.kind = kind;
			
		}
	}
	
	Result(Cond_Type kind,Instruction value){
		this.kind= Type.condition;
		this.condition=kind;
		this.fixup_location=value;
	}

	public Type getType(){
		return this.kind;
	}
	public Instruction getInstruction()
	{
		return this.ins;
	}
	
	public void update_fixup(Instruction i)
	{
		this.fixup_location = i;
	}
	
	public int getValue(){
		int val=9999;//garbage for initializing
		if(this.kind==Type.variable){
			val=this.index;
		}
		else if(kind==Type.number){
			val=this.value;
		}
		else if(kind==Type.register){
			val=this.regno;
		}		
		return val;
	}
	public Cond_Type getCondition(){
		return this.condition;
	}
	public Instruction getFixupLocation(){
		return this.fixup_location;
	}
	public String getName(){
		return this.name;
	}
}
