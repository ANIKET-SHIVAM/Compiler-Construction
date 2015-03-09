package Graph;
import java.util.*;
import java.io.*;

import Frontend.*;


public class DominatorTree {
	private static HashMap<Integer,LinkedList<Integer>> Dominator= new  HashMap<Integer,LinkedList<Integer>>();
//	private BasicBlock main;
	
	public DominatorTree(){
		int offset=0;
		for(BasicBlock main:BasicBlock.Functions_list){
			LinkedList<Integer> ll=new LinkedList<Integer>();
			//ll.add(main.getblockno());
			Dominator.put(main.getblockno(),ll);
		
		if(main.getnextblock()!= null)
			createDT(main.getnextblock(),main);
		if(main.getifelseblock()!= null)
			createDT(main.getifelseblock(),main);

		for(int i=main.getblockno()+1;i<=main.getblockno()+Dominator.size()-offset-1;i++){
			ll=Dominator.get(i);
			LinkedList<Integer> llnew=new LinkedList<Integer>();
			int max=0;
			for(int j=main.getblockno();j<=main.getblockno()+Dominator.size()-offset-1;j++){
				if(Collections.frequency(ll, j)>max){
					max=Collections.frequency(ll, j);
				}
			}
			for(int j=main.getblockno();j<=main.getblockno()+Dominator.size()-offset-1;j++){
				if(Collections.frequency(ll, j)==max){
					llnew.add(j);
				}
			}		
			if(!llnew.isEmpty()){
				Dominator.put(i, llnew);
			}
		}
		/*for(int i=0;i<=n;i++){
			System.out.print("block"+i+":");
			LinkedList<Integer>llx=Dominator.get(i);
			for(int z:llx)
				System.out.print(z+"   ");
			System.out.println();
		}*/
		offset+=Dominator.size();
		}
		printDT("testDT");
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
	public void printDT(String name){
		  PrintWriter printer;
		 try{
	           printer = new PrintWriter(new FileWriter(name+".vcg"));
	     for(BasicBlock main:BasicBlock.Functions_list){   
		    printer.println("graph: { title: \"Control Flow Graph\"");
	        printer.println("layoutalgorithm: dfs");
	        printer.println("manhattan_edges: yes");
	        printer.println("smanhattan_edges: yes");
	        printer.println("node: {");
	        printer.println("title: \"" + main.getblockno() + "\"");
	        printer.println("label: \"" + main.getblockno() + "[");
	        printer.println("Block #:"+main.getblockno());
	        printer.println("]\"");
	        printer.println("}");
	        int n=Dominator.size()-1;
			for(int i=main.getblockno()+1;i<main.getblockno()+Dominator.size()-1;i++){
		        printer.println("node: {");
		        printer.println("title: \"" + i + "\"");
		        printer.println("label: \"" + i + "[");
		        printer.println("Block #:"+i);
		        printer.println("]\"");
		        printer.println("}");
		        printer.println("edge: { sourcename: \""+i+"\"");
		        if(!Dominator.get(i).isEmpty())
		        printer.println("targetname: \""+Dominator.get(i).getLast()+"\"");
		        printer.println("color: blue");
		        printer.println("}");
			}
			 printer.println("}");
		     printer.close();
		 } 
		 } catch (IOException ex) {
	            System.out.println(ex.getMessage());
	        }
	}

}
