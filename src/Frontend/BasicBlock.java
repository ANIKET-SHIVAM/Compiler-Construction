package Frontend;
import java.util.ArrayList;

public class BasicBlock {
	private enum Type{
		main,iftrue,ifelse,join,whiletrue,
	}
	public static BasicBlock mainblock;
	private int blockno;
	private Type kind;
	private BasicBlock iftrueblock;
	private BasicBlock ifelseblock;
	private BasicBlock joinblock;   // this should work for both if and while
	private BasicBlock prevblock;
	private BasicBlock whiletrueblock;
	private ArrayList<Instruction> instructions;
	
	BasicBlock(){
		this.kind=Type.main;
	}
	
	BasicBlock(Type kind,BasicBlock prev){
		this.kind=kind;
		this.prevblock=prev;
	}
	
	public Type getType(){
		return kind;
	}
	public int getblockno(){
		return blockno;
	}
	public BasicBlock getprevblock(){
		return prevblock;
	}
	public BasicBlock getiftrueblock(){
		return iftrueblock;
	}
	public BasicBlock getifelseblock(){
		return ifelseblock;
	}
	public BasicBlock getjoinblock(){
		return joinblock;
	}
	public BasicBlock getwhiletrueblock(){
		return whiletrueblock;
	}
	public void addInstructions(Instruction instr){
		instructions.add(instr);
	}
	public ArrayList<Instruction> getInstructions(){
		return instructions;
	}
	public static void createblock(BasicBlock prev, Type kind){
		if (kind== Type.main){
			BasicBlock newblock= new BasicBlock();
			mainblock=newblock;
		}
		else{
		BasicBlock newblock= new BasicBlock(kind,prev);
		if(kind == Type.iftrue)
			prev.iftrueblock=newblock;  // HOW is this happening without static
		else if(kind == Type.ifelse)
			prev.ifelseblock=newblock;
		else if(kind == Type.whiletrue)
			prev.whiletrueblock=newblock;
		else if(kind == Type.join)
			prev.joinblock=newblock; 
		}
			
			
		//TODO: pointing iftrue and ifelse towards joinblock
	}
	//TODO: numbering the blocks
	
}
