package Frontend;
import java.util.ArrayList;

import Frontend.Result.Type;

public class BasicBlock {
	public enum BlockType{
		main,iftrue,ifelse,join,whileblock,doblock,follow
	}
	public static int block_id;
	public static BasicBlock mainblock;
	private int blockno;
	private BlockType kind;
	private BasicBlock nextblock;	// for if,while and do
	private BasicBlock ifelseblock;	// for if
	private BasicBlock joinblock;   // for if
	private BasicBlock prevblock;
	private BasicBlock prevblock2;// for whileblock and joinblock only
	private BasicBlock followblock;		//for while
	
	public int start_instruction_index;
	public int end_instruction_index;
	
	public Instruction start_Instr;	//starting instruction
	public Instruction end_Instr;	//ending instruction
	
	public ArrayList<Instruction> inst_list;

	BasicBlock(){
		inst_list = new ArrayList<Instruction>();
		this.kind=BlockType.main;
		this.block_id = 0;
		this.blockno = block_id;
	}
	
	BasicBlock(BlockType kind){
		inst_list = new ArrayList<Instruction>();
		this.kind	=kind;
		this.blockno = block_id;
	}
	
	public BasicBlock createIfTrue(){
		
		BasicBlock iftrue=new BasicBlock(BlockType.iftrue);
		this.nextblock=iftrue;
		iftrue.prevblock=this;
		return iftrue;
	}
	
	public BasicBlock createElse(){
		BasicBlock ifelse=new BasicBlock(BlockType.ifelse);
		this.ifelseblock=ifelse;
		ifelse.prevblock=this;
		return ifelse;
	}
	
	public BasicBlock createWhile(){
		BasicBlock whileblock=new BasicBlock(BlockType.whileblock);
		this.nextblock=whileblock;
		whileblock.prevblock=this;
		return whileblock;
	}
	
	public BasicBlock createdo(){
		BasicBlock doblock=new BasicBlock(BlockType.doblock);
		this.nextblock=doblock;
		doblock.prevblock=this;
	//	doblock.nextblock=this;
		//this.prevblock2=doblock;
		return doblock;
	}
	
	public BasicBlock createfollow(){
		BasicBlock follow=new BasicBlock(BlockType.follow);
		this.followblock=follow;
		follow.prevblock=this;
		return follow;
	}
		
	public BasicBlock createjoin(){		//only if will do this
		BasicBlock join=new BasicBlock(BlockType.join);
		this.joinblock=join;
		join.prevblock=this;
		return join;
	}
	
	public void setjoin(BasicBlock phi_block){		//only else and main(when no else is there) can do this
	//	if (this.kind==BlockType.ifelse){
			this.joinblock = phi_block;
			//this.joinblock=this.prevblock.nextblock.joinblock;	
			this.joinblock.prevblock2=this;	
		/*}
		else{
			this.joinblock=this.nextblock.joinblock;	
			this.nextblock.joinblock.prevblock2=this;
		}*/
	}
	
	public void setStartInstructionIndex(int index){
		start_instruction_index=index;
	}
	public void setEndInstructionIndex(int index){
		end_instruction_index=index;
	}
	public BlockType getType(){
		return this.kind;
	}
	public int getblockno(){
		return this.blockno;
	}
	public BasicBlock getprevblock(){
		return this.prevblock;
	}
	public BasicBlock getprevblock2(){
		return this.prevblock2;
	}
	public BasicBlock getnextblock(){
		return this.nextblock;
	}
	public BasicBlock getifelseblock(){
		return this.ifelseblock;
	}
	public BasicBlock getjoinblock(){
		return this.joinblock;
	}
	public BasicBlock getfollowblock(){
		return this.followblock;
	}
	public int getStartInstructionIndex(){
		return this.start_instruction_index;
	}
	public int getEndInstructionIndex(){
		return this.end_instruction_index;
	}
	public ArrayList<String> printInstructions(){
		ArrayList<String> bb_insts=new ArrayList<>();
		for(Instruction inst:inst_list){
			int index = Parser.insts.indexOf(inst);
			String oper1;String oper2;
			StringBuilder instruction_print= new StringBuilder(index).append(":").append(inst.getOperator());
			ArrayList<Result> operands=inst.getOperands();
			if(operands.size()==2){
				Result op1=operands.get(0);
				if(op1.getType()==Type.number)
					oper1= new StringBuilder("#").append(op1.getValue()).toString();
				else if(op1.getType()==Type.variable)
					oper1= new StringBuilder(" (").append(Parser.insts.indexOf(op1.getInstruction())).append(") ").toString();
				else
					oper1="error";
			
				Result op2=operands.get(1);
				if(op2.getType()==Type.number)
					oper2= new StringBuilder(" #").append(op2.getValue()).toString();
				else if(op2.getType()==Type.variable)
					oper2= new StringBuilder(" (").append(Parser.insts.indexOf(op2.getInstruction())).append(") ").toString();
				else
					oper2="error";
				
				instruction_print.append(oper1).append(oper2);
			}
			else if (operands.size()==1){
				Result op1=operands.get(0);
				if(op1.getType()==Type.number)
					oper1= new StringBuilder(" #").append(op1.getValue()).toString();
				else if(op1.getType()==Type.variable)
					oper1= new StringBuilder(" (").append(Parser.insts.indexOf(op1.getInstruction())).append(") ").toString();
				else
					oper1="error";
				
				instruction_print.append(oper1);
			}
			  bb_insts.add(instruction_print.toString());
		}
		return bb_insts;
	}
	

}
