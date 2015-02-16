package Graph;
import java.util.*;
import Frontend.*;

import java.util.ArrayList;
import java.io.*;

public class CFG {
	
	Stack<BasicBlock> blocks= new Stack<BasicBlock>();
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
        printer.println("graph: { title: \"Control Flow Graph\"");
        printer.println("layoutalgorithm: dfs");
        printer.println("manhattan_edges: yes");
        printer.println("smanhattan_edges: yes");
        blocks.push(bb);
        while(!blocks.isEmpty()){
        	bb=blocks.pop();
        	printBlock(bb);
        	
        	if(bb.getprevblock()!=null)
        		printEdge(bb.getprevblock().getblockno(),bb.getblockno());
        	if(bb.getprevblock2()!=null)
        		printEdge(bb.getprevblock2().getblockno(),bb.getblockno());
        	
        	if(bb.getnextblock()!=null)
        		blocks.push(bb.getnextblock());
        	if(bb.getifelseblock()!=null)
        		blocks.push(bb.getifelseblock());
        	if(bb.getfollowblock()!=null)
        		blocks.push(bb.getfollowblock());
        	if(bb.getjoinblock()!=null && bb.getType()!=BasicBlock.BlockType.ifelse)
        		blocks.push(bb.getjoinblock());
        } 	
        printer.println("}");
        printer.close();
    }
    
   
    
    private void printBlock(BasicBlock bb) {
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
