package Frontend;
import java.util.*;

import Frontend.BasicBlock.BlockType;

public class Function {
	public enum Type{procedure,function};
	private String funcname;
	private Type type;
	//private ArrayList<Result> localvars;
	private ArrayList<Result> globalvars;
	private ArrayList<Result>  params;
	private HashMap<Integer,Stack<Instruction>> Sym_table;
	private Result returninst;
	private BasicBlock firstbb;
	private BasicBlock returnbb;
	
	Function(Type kind,String name){
		type=kind;
		funcname=name;
		globalvars=new ArrayList<Result>();
		Sym_table=new HashMap<Integer,Stack<Instruction>>();
	}
	public Function(Type kind,String name,ArrayList<Result>parameters){
		type=kind;
		funcname=name;
		globalvars=new ArrayList<Result>();
		params=parameters;
		Sym_table=new HashMap<Integer,Stack<Instruction>>() ;
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
	
}
