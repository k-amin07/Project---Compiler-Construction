import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class VirtualMachine {
	private ArrayList<String>mc;
	private ArrayList<String>dataSegment;
	private HashMap<String, String>variables;
	Scanner input;
	String cin;
	public VirtualMachine(ArrayList<String> mc, ArrayList<String>dataSegment) {
		this.mc = new ArrayList<String>(mc);
		this.dataSegment = new ArrayList<String>(dataSegment);
		variables = new HashMap<String, String>();
		input = new Scanner(System.in);
		runVM();
	}
	
	int extractIndex(String input)
	{
		Pattern lastIntPattern = Pattern.compile("[^0-9]+([0-9]+)$");
		int lastNumberInt = -1;
		Matcher matcher = lastIntPattern.matcher(input);
		if (matcher.find()) {
		    String someNumberStr = matcher.group(1);
		    lastNumberInt = Integer.parseInt(someNumberStr);
		}
		return lastNumberInt;
	}
	void runVM()
	{
		for(int i=0;i<mc.size();i++)
		{
			StringTokenizer st = new StringTokenizer(mc.get(i)," ");
			String temp = new String();
			st.nextToken();
			temp += st.nextToken();
			int opCode = Integer.parseInt(temp);
			temp = new String();
			temp += st.nextToken();
			int operand1 = Integer.parseInt(temp);
			temp = new String();
			int operand2 = 0;
			int output;
			if(st.hasMoreTokens())
			{
				temp += st.nextToken();
				operand2 = Integer.parseInt(temp);
			}
			switch(opCode)
			{
			case 1:
				System.out.println(variables.get(""+operand1));
				break;
			case 2:
				String thisShouldNotEvenBeHappening = input.next();
				variables.put(""+operand1, thisShouldNotEvenBeHappening);
				dataSegment.set(operand1, thisShouldNotEvenBeHappening);
				break;
			case 3:
				variables.put(""+operand1, dataSegment.get(operand2));
				break;
			case 4:
				if(Integer.parseInt(dataSegment.get(operand1)) > Integer.parseInt(dataSegment.get(operand2)))
				{
					i = extractIndex(mc.get(i));
				}
				break;
			case 5:
				if(Integer.parseInt(dataSegment.get(operand1)) >= Integer.parseInt(dataSegment.get(operand2)))
				{
					i = extractIndex(mc.get(i));
				}
				break;
			case 6:
				i = extractIndex(mc.get(i));
				break;
			case 7:
				if(Integer.parseInt(dataSegment.get(operand1)) <= Integer.parseInt(dataSegment.get(operand2)))
				{
					i = extractIndex(mc.get(i));
				}
				break;
			case 8:
				if(Integer.parseInt(dataSegment.get(operand1)) < Integer.parseInt(dataSegment.get(operand2)))
				{
					i = extractIndex(mc.get(i));
				}
				break;
			case 9:
				if(Integer.parseInt(dataSegment.get(operand1)) == Integer.parseInt(dataSegment.get(operand2)))
				{
					i = extractIndex(mc.get(i));
				}
				break;
			case 10:
				if(Integer.parseInt(dataSegment.get(operand1)) != Integer.parseInt(dataSegment.get(operand2)))
				{
					i = extractIndex(mc.get(i));
				}
				break;
			case 11:
				return;
			case 12:
				output = Integer.parseInt(dataSegment.get(operand1)) + Integer.parseInt(dataSegment.get(operand2));
				variables.put(""+operand1, ""+output);
				break;
			case 13:
				output = Integer.parseInt(dataSegment.get(operand1)) - Integer.parseInt(dataSegment.get(operand2));
				variables.put(""+operand1, ""+output);
				break;
			case 14:
				output = Integer.parseInt(dataSegment.get(operand1)) * Integer.parseInt(dataSegment.get(operand2));
				variables.put(""+operand1, ""+output);
				break;
			case 15:
				output = Integer.parseInt(dataSegment.get(operand1)) / Integer.parseInt(dataSegment.get(operand2));
				variables.put(""+operand1, ""+output);
			}
				
		}
	}
}
