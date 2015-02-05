package Frontend;
//import java.util.ArrayList;

public class BasicBlock {
	private enum Type{
		main,iftrue,ifelse,join,whileblock,doblock,follow
	}
	public static BasicBlock mainblock;
	private int blockno;
	private Type kind;
	private BasicBlock nextblock;	// for if,while and do
	private BasicBlock ifelseblock;	// for if
	private BasicBlock joinblock;   // for if
	private BasicBlock prevblock;
	private BasicBlock prevblock2;// for whileblock and joinblock only
//	private BasicBlock whileblock;	//for while
//	private BasicBlock doblock;		//for while
	private BasicBlock followblock;		//for while
	
	public int start_instruction_index;
	public int end_instruction_index;
	
	
	BasicBlock(){
		this.kind=Type.main;
	}
	
	BasicBlock(Type kind){
		this.kind=kind;
	}
	
	public BasicBlock createIfTrue(){
		
		BasicBlock iftrue=new BasicBlock(Type.iftrue);
		this.nextblock=iftrue;
		iftrue.prevblock=this;
		return iftrue;
	}
	
	public BasicBlock createIfElse(){
		BasicBlock ifelse=new BasicBlock(Type.ifelse);
		this.ifelseblock=ifelse;
		ifelse.prevblock=this;
		return ifelse;
	}
	
	public BasicBlock createWhile(){
		BasicBlock whileblock=new BasicBlock(Type.whileblock);
		this.nextblock=whileblock;
		whileblock.prevblock=this;
		return whileblock;
	}
	
	public BasicBlock createdo(){
		BasicBlock doblock=new BasicBlock(Type.doblock);
		this.nextblock=doblock;
		doblock.prevblock=this;
		doblock.nextblock=this;
		this.prevblock2=doblock;
		return doblock;
	}
	
	public BasicBlock createfollow(){
		BasicBlock follow=new BasicBlock(Type.follow);
		this.followblock=follow;
		follow.prevblock=this;
		return follow;
	}
	
	public BasicBlock createjoin(){		//only if will do this
		BasicBlock join=new BasicBlock(Type.join);
		this.joinblock=join;
		join.prevblock=this;
		return join;
	}
	
	public void setjoin(){		//only else and main(when no else is there) can do this
		if (this.kind==Type.ifelse){
			this.joinblock=this.prevblock.nextblock.joinblock;	
			this.prevblock.nextblock.joinblock.prevblock2=this;	
		}
		else{
			this.joinblock=this.nextblock.joinblock;	
			this.nextblock.joinblock.prevblock2=this;
		}
	}
	
	public void setStartInstructionIndex(int index){
		start_instruction_index=index;
	}
	public void setEndInstructionIndex(int index){
		end_instruction_index=index;
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
	public BasicBlock getprevblock2(){
		return prevblock2;
	}
	public BasicBlock getnextblock(){
		return nextblock;
	}
	public BasicBlock getifelseblock(){
		return ifelseblock;
	}
	public BasicBlock getjoinblock(){
		return joinblock;
	}
	public BasicBlock getfollowblock(){
		return followblock;
	}
	public int getStartInstructionIndex(){
		return start_instruction_index;
	}
	public int getEndInstructionIndex(){
		return end_instruction_index;
	}
	

}
