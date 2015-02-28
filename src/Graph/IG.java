package Graph;
import java.util.*;

import Frontend.*;
import Optimizations.RA;

import java.util.ArrayList;
import java.io.*;

public class IG {
	
	//Queue<BasicBlock> blocks= new LinkedList<BasicBlock>();
    private PrintWriter printer;
    public int [] node_done = new int[Parser.insts.size()];
    public IG(String name){
        try{
            printer = new PrintWriter(new FileWriter(name + ".vcg"));
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }
    
    public void printIG() {
    //	BasicBlock bb = BasicBlock.mainblock;
        printer.println("graph: { title: \"Control Flow Graph\"");
        printer.println("layoutalgorithm: dfs");
        printer.println("manhattan_edges: yes");
        printer.println("smanhattan_edges: yes");
       
        int i=0,j=0;
        
        
        for(i=0;i<Parser.insts.size();i++)
        {
        	for(j=0;j<Parser.insts.size();j++)
        	{
        		if(RA.IGMatrix[i][j] == 1)
        		{
        			if(node_done[i] == 0)
        				printNode(i);
        			if(node_done[j] ==0)
        				printNode(j);
        			
        			printEdge(i,j);
        		}
        	}
        }
         	
        printer.println("}");
        printer.close();
    }
    
   
    
    private void printNode(int i) {
        printer.println("node: {");
        printer.println("title: \"" +i + "\"");
        	printer.println("label: \"" + i + "[");
        	 printer.println(i);
        printer.println("]\"");
        printer.println("}");
        node_done[i] =1;
        
 
    }
    
    
    public void printEdge(int source, int target){
        printer.println("edge: { sourcename: \""+source+"\"");
        printer.println("targetname: \""+target+"\"");
        printer.println("color: blue");
        printer.println("}");
    }
    
   
}
