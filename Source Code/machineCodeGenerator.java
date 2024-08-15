import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;


public class machineCodeGenerator {
	private ArrayList<String>tac;
	private String machineCodeFile;
	ArrayList<String>dataTypes;
	ArrayList<String>address;
	ArrayList<String>symbolTable;
	HashMap<String, Integer>opCodes;
	int currentAddress;
	int currentDataSegmentAddress;
	int currentFunctionAddress;
	private ArrayList<String>dataSegment;
	private ArrayList<String>mc;//three address code to machine code format
	
	public machineCodeGenerator(ArrayList<String> tac, StringBuffer tacFile, ArrayList<String>dataTypes, ArrayList<String>symbolTable) {
		this.tac = new ArrayList<String>(tac);
		for(int i=0; i<4;i++)
		{
			tacFile.deleteCharAt(tacFile.length()-1);
		}
		tacFile.append(".mc");
		machineCodeFile = new String(tacFile.toString());
		this.dataTypes = new ArrayList<String>(dataTypes);
		this.symbolTable = new ArrayList<String>(symbolTable);
		currentAddress = 0;
		currentDataSegmentAddress = 0;
		currentFunctionAddress = 0;
		address = new ArrayList<String>();
		mc = new ArrayList<String>();
		opCodes = new HashMap<String,Integer>();
		dataSegment = new ArrayList<String>();
		initializeOpCodes();
		generateMachineCode();
		printMachineCode();
		VirtualMachine vm = new VirtualMachine(mc,dataSegment);
	}
	
	void initializeOpCodes()
	{
		opCodes.put("out", 1);
		opCodes.put("in", 2);
		opCodes.put("mov", 3);
		opCodes.put("ja", 4);
		opCodes.put("jae", 5);
		opCodes.put("jmp", 6);
		opCodes.put("jbe", 7);
		opCodes.put("jb", 8);
		opCodes.put("je", 9);
		opCodes.put("jne", 10);
		opCodes.put("ret", 11);
		opCodes.put("add", 12);
		opCodes.put("sub", 13);
		opCodes.put("mul", 14);
		opCodes.put("div", 15);
	}
	void printMachineCode()
	{
		try {
			PrintWriter writer = new PrintWriter(machineCodeFile.toString(), "UTF-8");
			for(int i=0;i<mc.size();i++)
			{
				writer.println(mc.get(i));
			}
			writer.flush();
			writer.close();
		}
		catch(Exception E)
		{
			System.out.println(E.getMessage());
		}
	}
	
	String replaceHelper(ArrayList<String> tokens, int index)
	{
		String machineCode = new String();
		machineCode += " ";
		if(address.contains(tokens.get(index)))
		{
			machineCode+=address.indexOf(tokens.get(index));
		}
		else
		{
			machineCode += currentAddress;
			currentAddress++;
			address.add(tokens.get(index));
			dataSegment.add("0");
		}
		return machineCode;
	}
	
	void replace(ArrayList<String> tokens)
	{
		String machineCode = new String();
		int lineNumber = Integer.parseInt(tokens.get(0));
		machineCode += lineNumber + " ";//append line number
		if(tokens.get(1).equals("out"))
		{
			machineCode += opCodes.get("out");
			machineCode += replaceHelper(tokens, 2);
		}
		else if ( tokens.get(1).equals("in") ) {
			machineCode += opCodes.get("in");
			machineCode += replaceHelper(tokens, 2);
		}
		else if ( tokens.get(1).equals("ret") ) {
			machineCode += opCodes.get("ret");
			machineCode += replaceHelper(tokens, 2);
		}
		//handle arithmetic operators
		else if(tokens.get(1).equals("add"))
		{
			machineCode += opCodes.get("add");
			machineCode += replaceHelper(tokens, 2);
			machineCode += replaceHelper(tokens, 3);
		}
		else if(tokens.get(1).equals("sub"))
		{
			machineCode += opCodes.get("sub");
			machineCode += replaceHelper(tokens, 2);
			machineCode += replaceHelper(tokens, 3);
		}
		else if(tokens.get(1).equals("mul"))
		{
			machineCode += opCodes.get("mul");
			machineCode += replaceHelper(tokens, 2);
			machineCode += replaceHelper(tokens, 3);
		}
		else if(tokens.get(1).equals("div"))
		{
			machineCode += opCodes.get("div");
			machineCode += replaceHelper(tokens, 2);
			machineCode += replaceHelper(tokens, 3);
		}
		//handle assignment operator, with or without arithmetic operators;
		else if(tokens.get(1).equals("mov"))
		{
			machineCode += opCodes.get("mov");
			machineCode += replaceHelper(tokens, 2);
			String whateverIcantThinkofAName = new String();
			if(tokens.get(3).charAt(0) == '\"')
			{
				for (int indexer = 3; indexer<tokens.size();indexer++)
				{
					whateverIcantThinkofAName += tokens.get(indexer) + " ";
				}
				dataSegment.set(address.indexOf(tokens.get(2)), whateverIcantThinkofAName);
				machineCode += " " + dataSegment.indexOf(whateverIcantThinkofAName);
			}
			else if(tokens.get(3).chars().allMatch(Character::isDigit))
			{
				dataSegment.set(address.indexOf(tokens.get(2)), tokens.get(3));
			}
			else if(dataSegment.contains(tokens.get(3)))
			{
				machineCode += " " + dataSegment.get(dataSegment.indexOf(tokens.get(3))) + " ";
			}
			else if(address.contains(tokens.get(3)))
			{
				machineCode += address.indexOf(tokens.get(3)) + " ";
				
			}
			else {
				machineCode += " "+ currentAddress + " ";
				address.add(tokens.get(3));
				dataSegment.add("0");
				currentAddress++;
			}
		}
		//handle conditionals
		else if(tokens.get(1).equals("if"))
		{
			String a1, a2;
			a1 = tokens.get(2);
			a2 = tokens.get(4);
			
			if(tokens.contains("GT"))
			{
				machineCode += opCodes.get("ja")+" " + address.indexOf(a1) + " " + address.indexOf(a2) + " "+tokens.get(tokens.size()-1);			//jump if above
			}
			else if(tokens.contains("GE"))
			{
				machineCode += opCodes.get("jae")+" " + address.indexOf(a1) + " " + address.indexOf(a2) + " "+tokens.get(tokens.size()-1);		//jump if above or equal
			}
			else if(tokens.contains("LT"))
			{
				machineCode += opCodes.get("jb")+" " + address.indexOf(a1) + " " + address.indexOf(a2) + " "+tokens.get(tokens.size()-1);		//jump if below
			}
			else if(tokens.contains("LE"))
			{
				machineCode += opCodes.get("jbe")+" " + address.indexOf(a1) + " " + address.indexOf(a2) + " "+tokens.get(tokens.size()-1);		//jump if below or equal;
			}
			else if(tokens.contains("EE"))
			{
				machineCode += opCodes.get("je")+" " + address.indexOf(a1) + " " + address.indexOf(a2) + " "+tokens.get(tokens.size()-1);		//jump if equal;
			}
			else if(tokens.contains("NE"))
			{
				machineCode += opCodes.get("jne")+" " + address.indexOf(a1) + " " + address.indexOf(a2) + " "+tokens.get(tokens.size()-1);	//jump if not equal
			}
		}
		else if(tokens.get(1).equals("goto"))
		{
			machineCode += opCodes.get("jmp")+ " " + tokens.get(tokens.size()-1);
		}
		mc.add(machineCode);
	}
	
	private void generateMachineCode()
	{
		for(int i=0;i<tac.size();i++)
		{
			StringTokenizer st = new StringTokenizer(tac.get(i)," ");
			ArrayList<String>tokens = new ArrayList<String>();
			while(st.hasMoreTokens())
			{
				tokens.add(st.nextToken());
			}
			replace(tokens);
		}
		
	}
}
