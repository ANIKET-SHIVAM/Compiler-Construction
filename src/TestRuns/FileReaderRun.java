package TestRuns;
import java.io.IOException;

import Frontend.FileReader;

public class FileReaderRun {

	public static void main(String[] args) throws IOException {
		char c;
		String filename = "testprogs/arithemetic.txt";		
		FileReader fr = new FileReader(filename);	
		while (( c = fr.getSym())!= '|') { 
			System.out.print(c);
			
		}
	}

}