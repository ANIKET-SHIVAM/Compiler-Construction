package Frontend;
import java.util.*;

public class Function {
	public enum Type{procedure,function};
	private String funcname;
	private Type type;
	private ArrayList<Result> localvars;
	private ArrayList<Result> globalvars;
	private ArrayList<Result>  params;
	private Result returninst;
	private BasicBlock firstbb;
	private BasicBlock returnbb;
	
	Function(Type kind,String name){
		type=kind;
		funcname=name;
		localvars=new ArrayList<Result>();
		globalvars=new ArrayList<Result>();
		firstbb=new BasicBlock();
	}
	public Function(Type kind,String name,ArrayList<Result>parameters){
		type=kind;
		funcname=name;
		localvars=new ArrayList<Result>();
		globalvars=new ArrayList<Result>();
		params=parameters;
		firstbb=new BasicBlock();
	}
	public BasicBlock getfirstbb(){
		return firstbb;
	}
	public BasicBlock getreturnbb(){
		return returnbb;
	}
	public void setreturnbb(BasicBlock bb){
		returnbb=bb;;
	}
	public void setreturninst(Instruction inst){
		returninst=new Result(Result.Type.instruction, inst);
	}
	public Result getreturninst(){
		return returninst;
	}
	
}
