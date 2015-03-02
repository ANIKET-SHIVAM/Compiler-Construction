package CodeGenerator;
import java.util.*;

public class CodeGenerator {
	
	//ARITHEMETIC INSTRUCTIONS	- F2
		private static int ADD = 0;
		private static int SUB = 1;
		private static int MUL = 2;
		private static int DIV = 3;
		private static int MOD = 4;
		private static int CMP = 5;
		private static int OR = 8;
		private static int AND = 9;
		private static int BIC = 10;
		private static int XOR = 11;
		
		private static int LSH = 12;
		private static int ASH = 13;
		
		private static int CHK = 14;
		
	//IMMEDIATE ARITHEMETIC INSTRUCTION	-F1
		private static int ADDI = 16;
		private static int SUBI = 17;
		private static int MULI = 18;
		private static int DIVI = 19;
		private static int MODI = 20;
		private static int CMPI = 21;
		private static int ORI = 24;
		private static int ANDI = 25;
		private static int BICI = 26;
		private static int XORI = 27;
		
		private static int LSHI = 28;
		private static int ASHI = 29;
		
		private static int CHKI = 30;
		
	//LOAD/STORE INSTRUCTIONS	
		private static int LDW = 32;	//F1
		private static int LDX = 33;	//F2
		private static int POP = 34;	//F1
		private static int STW = 36;	//F1
		private static int STX = 37;	//F2
		private static int PSH = 38;	//F1
		
	//CONTROL INSTRUCTIONS - F1
		private static int BEQ = 40;	
		private static int BNE = 41;
		private static int BLT = 42;
		private static int BGE = 43;
		private static int BLE = 44;
		private static int BGT = 45;
	
		private static int BSR = 46;	//F1
		private static int JSR = 48;	//F3
		private static int RET = 49;	//F2
		
	//I/O INSTRUCTIONS	
		private static int RDD = 50;	//F2
		private static int WRD = 51;	//F2
		private static int WRH = 52;	//F2
		private static int WRL = 53;	//F1
		
		
	
}
