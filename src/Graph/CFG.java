package Graph;
import java.util.*;
import Frontend.*;

import java.util.ArrayList;
import java.io.*;

public class CFG {
	
	Queue<BasicBlock> blocks= new LinkedList<BasicBlock>();
    private PrintWriter printer;
    
    public CFG(String name){
        try{
            printer = new PrintWriter(new FileWriter(name + ".vcg"));
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }
    
    public void printCFG() {
    	
    	BasicBlock bb = BasicBlock.mainblock;
    	int block_length =bb.block_id;
    	int [] block_done = new int[block_length];
        printer.println("graph: { title: \"Control Flow Graph\"");
        printer.println("layoutalgorithm: dfs");
        printer.println("manhattan_edges: yes");
        printer.println("smanhattan_edges: yes");
        
        if(bb.getblockno()>0){
        	for(int i=0;i<bb.getblockno();i++){
        		blocks.add(BasicBlock.getblockbyid(i));
        	}
        }
        blocks.add(bb);
        while(!blocks.isEmpty()){
        	bb=blocks.remove();
        	if(block_done[bb.getblockno()] == 0)
        	printBlock(bb,block_done);
        	
        	if(bb.getprevblock()!=null)
        		printEdge(bb.getprevblock().getblockno(),bb.getblockno());
        	if(bb.getprevblock2()!=null)
        		printEdge(bb.getprevblock2().getblockno(),bb.getblockno());
        	//if(bb.getType()==BasicBlock.BlockType.doblock && bb.checkdotowhile())
        	//	printEdge(bb.getblockno(),bb.getprevblock().getblockno());
        	if(bb.checkdotowhile())
        		printEdge(bb.getblockno(),bb.getdotowhile().getblockno());
        	
        	if(bb.getnextblock()!=null &&!blocks.contains(bb.getnextblock()))
        		blocks.add(bb.getnextblock());
        	if(bb.getifelseblock()!=null && !blocks.contains(bb.getifelseblock()))
        		blocks.add(bb.getifelseblock());
        	if(bb.getfollowblock()!=null &&!blocks.contains(bb.getfollowblock()))
        		blocks.add(bb.getfollowblock());
        	//if(bb.getjoinblock()!=null && (bb.getType()==BasicBlock.BlockType.iftrue ||(bb.getType()==BasicBlock.BlockType.follow&&bb.getjoinblock().getprevblock().getType()!=BasicBlock.BlockType.iftrue)||(bb.getType()==BasicBlock.BlockType.join&&bb.getjoinblock().getprevblock2()!=bb)))
        	if(bb.getjoinblock()!=null&&!blocks.contains(bb.getjoinblock()))	
        		blocks.add(bb.getjoinblock());
        } 	
        printer.println("}");
        printer.close();
    }
    
   
    
    private void printBlock(BasicBlock bb,int [] block_done) {
    	block_done[bb.getblockno()]=1;
        printer.println("node: {");
        printer.println("title: \"" + bb.getblockno() + "\"");
        	printer.println("label: \"" + bb.getblockno() + "[");
        ArrayList<String> insts=new ArrayList<>();
        insts=bb.printInstructions();
        for(String inst:insts)
        	 printer.println(inst);
        printer.println("]\"");
        printer.println("}");
        
 
    }
    
    
    public void printEdge(int source, int target){
        printer.println("edge: { sourcename: \""+source+"\"");
        printer.println("targetname: \""+target+"\"");
        printer.println("color: blue");
        printer.println("}");
    }
    
   
}
