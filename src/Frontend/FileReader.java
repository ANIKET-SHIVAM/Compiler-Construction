package Frontend;
import java.io.*;

public class FileReader {
	
	private java.io.BufferedReader br;
	private String str;
	private int position=0;
	
	public FileReader(String filename) throws IOException {
		try {
			//private java.io.BufferedReader br = null;
            java.io.FileReader fr = new java.io.FileReader(filename);
            br = new BufferedReader(fr);
            StringBuffer stringBuffer = new StringBuffer();
            
            String line;
    		while ((line = br.readLine()) != null){
    			stringBuffer.append(line);
    			stringBuffer.append("\0");
    		}
    		fr.close();
    		System.out.println("Contents of file:");
    		str=stringBuffer.toString();
    		System.out.println(str);
            
        	} catch (FileNotFoundException e) {
        	System.out.println("File not found");
        }  
	}
	
	public char getSym()
	{	 
		char outchar=0x00;
		//try
		//{	
			//int inchar = this.br.read();
			if(position == str.length()-1)
				return '\0';
			int inchar = str.charAt(position++);
		
			if (inchar == -1)
			{
				outchar = '|';
			}
			else
			{
				outchar = (char) inchar;
			}
		//}
		//catch (IOException e)
	//	{
		//	this.Error(e.getMessage());
		//}
		return outchar;
	}
	
	public char getPrevSym()
	{	 
		char outchar=0x00;
		//try
		//{	
			//int inchar = this.br.read();
		//	if(position == str.length()-1)
			//	return '\0';
			position = position-1;
			int inchar = str.charAt(position);
			position++;
			if (inchar == -1)
			{
				outchar = '|';
			}
			else
			{
				outchar = (char) inchar;
			}
		//}
		//catch (IOException e)
	//	{
		//	this.Error(e.getMessage());
		//}
		return outchar;
	}
	
	public void Error(String error)
	{
		System.out.println(error);
	}
}