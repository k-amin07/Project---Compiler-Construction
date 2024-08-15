import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Stack;

public class Translater {
	//////////////////////////////////////
	private ArrayList<String> symbolTable;
	private String symFileName;
	private String translatorSymFile;
	/////////////////////////////////////
	private ArrayList<pair> tlPairs;
	private ArrayList<String> tac;
	private ArrayList<String> dataTypes;
	private int lineNumber;
	private Stack<Integer> backpatchLineNumber;
	private ArrayList<String> extraSymbolTable;
	private Stack<String>jump;
	private int tempVarCount;
	private int symCursor;			//for indexing symbol table, to add initial values
	Translater(ArrayList<pair> tLPairs, ArrayList<String> symtable, String sName, ArrayList<String> dataTypes)
	{
		tlPairs = new ArrayList<pair>(tLPairs);
		symbolTable = new ArrayList<String>(symtable);
		extraSymbolTable = new ArrayList<String>(symtable);
		symFileName = new String(sName);
		backpatchLineNumber = new Stack<Integer>();
		tac = new ArrayList<String>();
		jump = new Stack<String>();
		translatorSymFile = new String("TranslatorSymbolTable.sym");
		this.dataTypes = new ArrayList<String>(dataTypes);
		lineNumber = 0;
		tempVarCount = 0;
		symCursor = 1;
		translate();
	}
	void loop(int i) {
		String tempString = tempVarGenerator();
		tac.add(lineNumber++ + "  mov "+tempString + " " + tlPairs.get(i+2).lexeme);
		int n = lineNumber+2;
		tac.add(lineNumber++ + "  if "+tlPairs.get(i).lexeme+" "+tlPairs.get(++i).lexeme+" "+ tempString + " goto " + n);
		tac.add(lineNumber++ + "  goto");
		backpatchLineNumber.push(tac.size()-1);
		jump.push("LOOP");
	}
	void conditional(int i) {
		String tempString = tempVarGenerator();
		tac.add(lineNumber++ + "  mov "+tempString + " " + tlPairs.get(i+2).lexeme);
		int n = lineNumber+2;
		tac.add(lineNumber++ + "  if "+tlPairs.get(i).lexeme+" "+tlPairs.get(++i).lexeme+" "+tempString + " goto " + n);
		tac.add(lineNumber + "  goto");
		backpatchLineNumber.push(tac.size()-1);
		jump.push("IF");
	}
	void backPatch(int l)
	{
		String a = tac.get(l);
		a = a+" "+lineNumber;
		tac.set(l, a);
	}
	
	String tempVarGenerator()
	{
		String temp = "t" + tempVarCount++;
		extraSymbolTable.add(temp);
		dataTypes.add("TMP");
		return temp;
	}
	
	void printTac()
	{
		StringBuffer tacFile = new StringBuffer(symFileName);
		for(int i = 0; i<4;i++)
		{
			tacFile.deleteCharAt(tacFile.length()-1);
		}
		tacFile.append(".tac");
		try {
			PrintWriter writer = new PrintWriter(tacFile.toString(), "UTF-8");
			PrintWriter symbolTableWriter = new PrintWriter(translatorSymFile.toString(), "UTF-8");
			for(int i=0;i<tac.size();i++)
			{
				writer.println(tac.get(i));
			}
			for(int i=1;i<symbolTable.size();i++)
			{
				symbolTableWriter.println(dataTypes.get(i)+"  "+symbolTable.get(i));
			}
			writer.close();
			symbolTableWriter.close();
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			System.out.println("Translater error: Unsupported encoding exception for output file");
		}
		machineCodeGenerator mcg = new machineCodeGenerator(tac, tacFile,dataTypes, extraSymbolTable);
	}
	void translate()
	{
		for(int i = 0; i<tlPairs.size();i++)
		{
			if(tlPairs.get(i).token.equals("DEF"))
			{
				while(!tlPairs.get(i).token.equals("("))
				{
					i++;
				}
				i++;
				String initialValue = "0";
				while(!tlPairs.get(i).token.equals("{"))
				{
					if(tlPairs.get(i).token.equals("ID"))	//skip commas
					{
						String temp = symbolTable.get(symCursor);
						if(i+1 < tlPairs.size() && tlPairs.get(i+1).token.equals("<-"))
						{
							temp+=tlPairs.get(i+2).lexeme;
						}
						else
						{
							temp = temp + "  " + initialValue;
						}
						symbolTable.set(symCursor++, temp);
					}
					i++;
				}
			}
			else if(tlPairs.get(i).token.equals("INT") || tlPairs.get(i).token.equals("CHAR"))
			{
				i++;
				String initialValue = "0";			//assuming initial value of 0 for all unspecified variables.
				while(!tlPairs.get(i).token.equals(";"))
				{
					//add initial values
					//if an identifier is found, and the next token is assignment operator, use the token after that
					//as initial value. Otherwise, use zero
					if(tlPairs.get(i).token.equals("ID"))	//skip commas
					{
						String temp = symbolTable.get(symCursor);
						if(i+1 < tlPairs.size() && tlPairs.get(i+1).token.equals("<-"))
						{
							temp+=tlPairs.get(i+2).lexeme;
						}
						else
						{
							temp = temp + "  " + initialValue;
						}
						symbolTable.set(symCursor++, temp);
					}
					
					i++;
				}
			}
			else if(tlPairs.get(i).token.equals("PRINT"))
			{
				String tempString = tempVarGenerator();		//easier to generate machine code if string is first moved to a temporary variable
				tac.add(lineNumber++ + "  mov "+tempString + " " + tlPairs.get(++i).lexeme);
				tac.add(lineNumber++ + "  out "+tempString);
			}
			else if(tlPairs.get(i).token.equals("READ"))
			{
				tac.add(lineNumber++ + "  in "+tlPairs.get(++i).lexeme);
			}
			else if(tlPairs.get(i).token.equals("WHILE"))
			{
				++i;		//token at i = (
				++i;		//token at i = ID
				loop(i);
				++i;		//token at i = RO, already used in function
				++i;		//token at i = ID
				++i;		//token at i = )
			}
			else if(tlPairs.get(i).token.equals("IF"))
			{
				++i;		//token at i = (
				++i;		//token at i = ID
				conditional(i);
				++i;		//token at i = RO, already used in function
				++i;		//token at i = ID
				++i;		//token at i = )
			}
			else if(tlPairs.get(i).token.equals("}") && !backpatchLineNumber.isEmpty())
			{
				int l = backpatchLineNumber.pop() - 1 ;
				String type = jump.pop();
				tac.add(lineNumber++ + "  goto " + l);
				if(type.equals("LOOP"))
				{
					backPatch(l+1);
				}
				else if(type.equals("IF"))
				{
					backPatch(l+1);
				}
			}
			else if(tlPairs.get(i).token.equals("RET"))
			{
				tac.add(lineNumber++ + "  ret "+tlPairs.get(++i).lexeme);
			}
			else if(tlPairs.get(i).token.equals("ID"))
			{
				StringBuffer ao = new StringBuffer();
				ao.append(lineNumber++ + "  ");
				int idCount = 0;
				while(!tlPairs.get(i).token.equals(";"))
				{
					if(tlPairs.get(i).token.equals("ID"))
					{
						idCount++;
						if(idCount>3)
						{
							idCount = 0;
							String tempVar = tempVarGenerator();
							ao.append(tempVar);
							tac.add(ao.toString());
							ao = new StringBuffer();
							ao.append(lineNumber++ + "  " + tempVar);
						}
						ao.append(tlPairs.get(i).lexeme);
					}
					else if(tlPairs.get(i).token.equals("<-"))
					{
						lineNumber--;
						String tempString = tempVarGenerator();		//easier to generate machine code if string is first moved to a temporary variable
						tac.add(lineNumber++ + "  mov "+tempString + " " + tlPairs.get(++i).lexeme);
						if(tlPairs.get(i+1).token.equals("+")) 
						{
							ao = new StringBuffer();
							tempString = tempVarGenerator();
							tac.add(lineNumber++ + "  mov "+tempString + " " + tlPairs.get(i+2).lexeme);
							ao.append(lineNumber++ + "  add " + tlPairs.get(i).lexeme + " " + tempString);
							i++;
							i++;
						}
						else if(tlPairs.get(i+1).token.equals("-"))
						{
							ao = new StringBuffer();
							ao.append(lineNumber++ + "  sub " + tlPairs.get(i-2).lexeme);
						}
						else if( tlPairs.get(i+1).token.equals("*"))
						{
							ao = new StringBuffer();
							ao.append(lineNumber++ + "  mul " + tlPairs.get(i-2).lexeme);
						}
						else if(tlPairs.get(i+1).token.equals("/"))
						{
							ao = new StringBuffer();
							ao.append(lineNumber++ + "  div " + tlPairs.get(i-2).lexeme);
						}
						else
						{
							ao = new StringBuffer();
							tac.add(lineNumber++ + "  mov "+tlPairs.get(i-2).lexeme+ " " +tempString);
						}
					}
					else if(tlPairs.get(i).token.equals("+") || tlPairs.get(i).token.equals("-") || tlPairs.get(i).token.equals("*") || tlPairs.get(i).token.equals("/")) 
					{
						ao.append(" "+tlPairs.get(i).lexeme+" ");
					}
					else
					{
						ao.append(tlPairs.get(i).lexeme);
					}
					i++;
				}
				if(!ao.toString().isEmpty())
				{
					tac.add(ao.toString());
				}
			}
		}
		printTac();
	}
}
