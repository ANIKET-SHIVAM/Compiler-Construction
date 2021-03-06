package Frontend;
import java.util.*;

import Frontend.BasicBlock.BlockType;

public class Function {
	public enum Type{procedure,function};
	public String funcname;
	private Type type;
	//private ArrayList<Result> localvars;
	private ArrayList<Result> globalvars;
	public ArrayList<Result>  params=new ArrayList<Result>();
	private static HashMap<Integer,Stack<Instruction>> Sym_table;
	public HashMap<Instruction,ArrayList<Result>> func_call_params;
	public ArrayList<Instruction>  call_insts=new ArrayList<Instruction>();
	private Result returninst;
	private BasicBlock firstbb;
	private BasicBlock returnbb;
	public static int func_id;
	private int id;
	
	
	Function(Type kind,String name){
		type=kind;
		funcname=name;
		globalvars=new ArrayList<Result>();
		Sym_table=new HashMap<Integer,Stack<Instruction>>();
		func_call_params=new HashMap<Instruction,ArrayList<Result>>();
		this.id = func_id;
	}
	public Function(Type kind,String name,ArrayList<Result>parameters){
		type=kind;
		funcname=name;
		globalvars=new ArrayList<Result>();
		params=parameters;
		Sym_table=new HashMap<Integer,Stack<Instruction>>() ;
		func_call_params=new HashMap<Instruction,ArrayList<Result>>();
		this.id = func_id;
	}
	
	public int getId()
	{
		return this.id;
	}
	
	public void setfirstbb(){
		firstbb=new BasicBlock(BlockType.function,this);
	}
	public BasicBlock getfirstbb(){
		return firstbb;
	}
	public BasicBlock getreturnbb(){
		return returnbb;
	}
	public void setreturnbb(BasicBlock bb){
		returnbb=bb;
	}
	public void setreturninst(Instruction inst){
		returninst=new Result(Result.Type.instruction, inst);
	}
	public Result getreturninst(){
		return returninst;
	}
	public HashMap<Integer,Stack<Instruction>> get_Sym_table(){
		return this.Sym_table;
	}
	public void add_param_call(Instruction inst,ArrayList<Result> params){
		func_call_params.put(inst, params);
	}
	
}
