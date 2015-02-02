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
	private int fixup_location;
	private int value; //for constant
	private int index;//for variable
	private int[] arr;//for array
	
	Result(){}
	
	Result(Type kind,String s)
	{
		this.kind = kind;
		this.name = s;
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
	
	Result(Cond_Type kind,int value){
		this.kind= Type.condition;
		this.condition=kind;
		this.fixup_location=value;
	}

	public Type getType(){
		return this.kind;
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
	public int getFixupLocation(){
		return this.fixup_location;
	}
	public String getName(){
		return this.name;
	}
}
