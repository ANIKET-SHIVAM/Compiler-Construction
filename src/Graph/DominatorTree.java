package Graph;
import java.util.*;

import Frontend.*;


public class DominatorTree {
	private static HashMap<Integer,LinkedList<Integer>> Dominator= new  HashMap<Integer,LinkedList<Integer>>();
	private BasicBlock main;
	
	public DominatorTree(){
		main=BasicBlock.mainblock;
			LinkedList<Integer> ll=new LinkedList<Integer>();
			Dominator.put(main.getblockno(),ll);
		
		if(main.getnextblock()!= null)
			createDT(main.getnextblock(),main);
		if(main.getifelseblock()!= null)
			createDT(main.getifelseblock(),main);

		
		int n=Dominator.size()-1;
		for(int i=1;i<=n;i++){
			ll=Dominator.get(i);
			LinkedList<Integer> llnew=new LinkedList<Integer>();
			int max=0;
			for(int j=0;j<=n;j++){
				if(Collections.frequency(ll, j)>max){
					max=Collections.frequency(ll, j);
				}
			}
			for(int j=0;j<=n;j++){
				if(Collections.frequency(ll, j)==max){
					llnew.add(j);
				}
			}	
				if(!llnew.isEmpty()){
					Dominator.put(i, llnew);
				}
			
		}
		for(int i=0;i<=n;i++){
			System.out.print("block"+i+":");
			LinkedList<Integer>llx=Dominator.get(i);
			for(int z:llx)
				System.out.print(z+"   ");
			System.out.println();
		}
		
	}
	public void createDT(BasicBlock bb,BasicBlock bbdom){
		if(Dominator.containsKey(bb.getblockno())){
			Dominator.get(bb.getblockno()).addAll(Dominator.get(bbdom.getblockno()));
			Dominator.get(bb.getblockno()).add(bbdom.getblockno());
		}
		else{
			LinkedList<Integer> ll=new LinkedList<Integer>();
			Dominator.put(bb.getblockno(),ll);
			Dominator.get(bb.getblockno()).addAll(Dominator.get(bbdom.getblockno()));
			Dominator.get(bb.getblockno()).add(bbdom.getblockno());
		}
		
	
		if(bb.getnextblock()!= null)
			createDT(bb.getnextblock(),bb);
		if(bb.getifelseblock()!= null)
			createDT(bb.getifelseblock(),bb);
		if(bb.getjoinblock()!= null)
			createDT(bb.getjoinblock(),bb);
		if(bb.getfollowblock()!= null)
			createDT(bb.getfollowblock(),bb);
	}
	public static LinkedList<Integer> getDominators(int blockno){
		return Dominator.get(blockno);
	}

}
