import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;


import java.util.Scanner;

public class lex{
	
	//exceptions to handle invalid tokens and mismatched brackets and no input
	public class notFoundException extends Exception
	{
		private static final long serialVersionUID = 1L;

		public notFoundException(String s)
		{
			super(s);
		}
	}
	
	public class bracketMismatchException extends Exception
	{
		private static final long serialVersionUID = 1L;
		public bracketMismatchException(String s)
		{
			super(s);
		}
	}
	
	public class noTokLexPairsDefinedException extends Exception
	{
		private static final long serialVersionUID = 1L;
		public noTokLexPairsDefinedException(String s)
		{
			super(s);
		}
	}

	public class noInputFileException extends Exception
	{
		private static final long serialVersionUID = 1L;
		public noInputFileException(String s)
		{
			super(s);
		}
	}
	
	public class incompleteStringException extends Exception
	{
		private static final long serialVersionUID = 1L;
		public incompleteStringException(String s)
		{
			super(s);
		}
	}

	
	
	//Valid things in language
	private ArrayList<String> keyWords;
	private ArrayList<String> arithematicOperators;
	private HashMap<String, String> relationalOperators;
	private ArrayList<String> numericConstants;
	private ArrayList<String> brackets;
	private ArrayList<pair> tokLexPairs;
	private ArrayList<String> symbolTable;
	
	//private String currentKeyword;
	
	private boolean MLComment;
	
	private StringBuffer fileName;
	private StringBuffer symFileName;
	
	//Making class singleton
	
	private static lex instance = null;
	
	//to check for brackets
	private Stack<Character> leftBrackets;
	
	//Initializing valid things
	
	private void keyWordInitializer()
	{
		keyWords =new ArrayList<String>();
		keyWords.add("def");
		keyWords.add("if");
		keyWords.add("else");
		keyWords.add("while");
		keyWords.add("ret");
		keyWords.add("print");
		keyWords.add("read");
		keyWords.add("int");
		keyWords.add("char");
	}
	private void arithematicOpertorsInitializer()
	{
		arithematicOperators = new ArrayList<String>();
		arithematicOperators.add("+");
		arithematicOperators.add("-");
		arithematicOperators.add("*");
		arithematicOperators.add("/");
	}
	private void relationalOperatorsInitializer()
	{
		relationalOperators = new HashMap<String,String>();
		relationalOperators.put("<", "LT");
		relationalOperators.put("<=","LE");
		relationalOperators.put(">","GT");
		relationalOperators.put(">=","GE");
		relationalOperators.put("==", "EE");
		relationalOperators.put("<>","NE");
	}
	private void numericConstantInitializer()
	{
		numericConstants = new ArrayList<String>();
		numericConstants.add("0");
		numericConstants.add("1");
		numericConstants.add("2");
		numericConstants.add("3");
		numericConstants.add("4");
		numericConstants.add("5");
		numericConstants.add("6");
		numericConstants.add("7");
		numericConstants.add("8");
		numericConstants.add("9");
	}
	private void bracketInitializer()
	{
		brackets = new ArrayList<String>();
		brackets.add("(");
		brackets.add(")");
		brackets.add("{");
		brackets.add("}");
		brackets.add("[");
		brackets.add("]");
	}
	
	
	//Constructor
	private lex()
	{
		keyWordInitializer();
		arithematicOpertorsInitializer();
		relationalOperatorsInitializer();
		numericConstantInitializer();
		bracketInitializer();
		symbolTable = new ArrayList<String>();
		tokLexPairs = new ArrayList<pair>();
		MLComment = false;
		leftBrackets = new Stack<Character>();
	}
	
	public static lex getInstance()
	{
		if(instance == null)
		{
			instance = new lex();
		}
		return instance;
	}
	
	//Print a given ArrayList
	void print(ArrayList<String> arr)
	{
		for (int i = 0; i < arr.size(); i++)
		{
			System.out.println(arr.get(i));
		}
		System.out.println();
	}
	
	//Check class of given token
	
	private pair isKeyWord(String token) throws notFoundException, bracketMismatchException
	{
		int a = keyWords.indexOf(token);
		pair retValue = new pair();
		if(a == -1)
		{
			return isArithematicOperator(token);
		}
		else
		{
			retValue.lexeme = "^";
			retValue.token = token.toUpperCase();
			
			return retValue;
		}
	}
	private pair isArithematicOperator(String token) throws notFoundException, bracketMismatchException
	{
		int a = arithematicOperators.indexOf(token);
		pair retValue = new pair();
		if(a == -1)
		{
			return isRelationalOperator(token);
		}
		else
		{
			retValue.lexeme = "^";
			retValue.token = token;
			return retValue;
		}
	}
	private pair isRelationalOperator(String token) throws notFoundException, bracketMismatchException
	{
		pair retValue = new pair();
		if(!relationalOperators.containsKey(token))
		{
			return isIdentifier(token);
		}
		else
		{
			retValue.lexeme = relationalOperators.get(token);
			retValue.token = "RO";
			return retValue;
		}
	}
	private pair isIdentifier(String token) throws notFoundException, bracketMismatchException
	{
		if (!(token.charAt(0) >= 'a' && token.charAt(0) <= 'z' || token.charAt(0) >= 'A' && token.charAt(0) <= 'Z'))
		{
			return isNumericConstant(token);
		}
		for (int i = 0; i < token.length(); i++)
		{
			if (!(token.charAt(i) >= 'a' && token.charAt(i) <= 'z' || token.charAt(i) >= 'A' && token.charAt(i) <= 'Z' || token.charAt(i) >= '0' && token.charAt(i) <= '9'))
			{
				return isLiteralConstant(token);
			}
		}
		if(!symbolTable.contains(token)) {
			symbolTable.add(token);
		}
		pair retValue = new pair();
		retValue.lexeme = token;
		retValue.token = "ID";
		return retValue;
	}
	private pair isNumericConstant(String token) throws notFoundException, bracketMismatchException
	{
		for (int i = 0; i < token.length(); i++)
		{
			if (!(token.charAt(i) >= '0' && token.charAt(i) <= '9'))
			{
				return isLiteralConstant(token);
			}
		}
		pair retValue = new pair();
		retValue.lexeme = token;
		retValue.token = "NC";
		return retValue;
	}
	private pair isLiteralConstant(String token) throws notFoundException, bracketMismatchException
	{
		if (token.length() != 3 || token.charAt(0) != '\'' || token.charAt(2) != '\'')
		{
			return isBracket(token);
		}
		else if ((token.charAt(1) < 'a' || token.charAt(1)>'z') && (token.charAt(1)<'A' || token.charAt(1)>'Z'))
		{
			return isBracket(token);
		}
		else
		{
			pair retValue = new pair();
			retValue.lexeme = token;
			retValue.token = "LC";
			return retValue;
		}
	}
	private pair isBracket(String token) throws notFoundException,bracketMismatchException
	{
		int a = brackets.indexOf(token);
		char tok = token.charAt(0);
		pair retValue = new pair();
		if(a == -1)
		{
			return isAssignmentOperator(token);
		}
		else
		{
			if(tok == '(' || tok == '{' || tok=='[')
			{
				leftBrackets.push(token.charAt(0));
			}
			else if(tok == ')')
			{
				Character check = leftBrackets.pop();
				if(check != '(')
				{
					throw new bracketMismatchException("Unmatched '('");
				}
			}
			else if(tok == '}')
			{
				Character check = leftBrackets.pop();
				if(check != '{')
				{
					throw new bracketMismatchException("Unmatched '}'");
				}
			}
			else if(tok==']')
			{
				Character check = leftBrackets.pop();
				if(check != ']')
				{
					throw new bracketMismatchException("Unmatched ']'");
				}
			}
			retValue.lexeme = "^";
			retValue.token = token;
			return retValue;
		}
	}
	private pair isAssignmentOperator(String token) throws notFoundException
	{
		if (token.length()== 2 && token.charAt(0) == '<' && token.charAt(1) == '-')
		{
			pair retValue = new pair();
			retValue.lexeme = "^";
			retValue.token = "<-";
			return retValue;
		}
		else
		{
			throw new notFoundException(token);
		}
	}
	
	private pair comma()
	{
		pair t = new pair();
		t.lexeme = "^";
		t.token = ",";
		return t;
	}
	private pair semicolon()
	{
		pair t = new pair();
		t.lexeme = "^";
		t.token = ";";
		return t;
	}
	private pair stringAdd(String str)
	{
		pair t = new pair();
		t.token = "STR";
		t.lexeme = str;
		return t;
	}
	
	//match brackets
	boolean checkBrackets()
	{
		if(leftBrackets.size()>0)
		{
			return false;
		}
		return true;
	}
	
	private void createTokenLexemePairs(String sloc) throws notFoundException, incompleteStringException, bracketMismatchException
	{
		//if multiline comments have been previously detected, check if the input string ends at ##. in that case, end multiline comment for next input
		if(MLComment == true)
		{
			if((sloc.charAt(sloc.length()-1) == '#') && (sloc.charAt(sloc.length()-1)=='#'))
			{
				MLComment = false;
			}
			return;
		}
		
		
		StringBuffer check = new StringBuffer();
		int size = sloc.length();
		
		for(int i=0;i<size;i++)
		{
			if(sloc.charAt(i) == ',')
			{
				tokLexPairs.add(isKeyWord(check.toString()));
				tokLexPairs.add(comma());
				check.delete(0,check.length());
				i++;
			}
			else if(sloc.charAt(i) == ';')
			{
				tokLexPairs.add(isKeyWord(check.toString()));
				tokLexPairs.add(semicolon());
				check.delete(0,check.length());
				i++;
			}
			else if(sloc.charAt(i)==' ')
			{
				if(check.length()>0)
				{
					tokLexPairs.add(isKeyWord(check.toString()));
					check.delete(0,check.length());
				}
			}
			else if(sloc.charAt(i)=='\t')
			{
				if(check.length()>0)
				{
					tokLexPairs.add(isKeyWord(check.toString()));
					check.delete(0,check.length());
				}
			}
			else if(sloc.charAt(i)=='#')
			{
				if(check.length()>0)
				{
					tokLexPairs.add(isKeyWord(check.toString()));
					check.delete(0,check.length());
				}
				if(sloc.charAt(i+1)=='#')
				{
					MLComment = true;
				}
				return;
			}
			else if(sloc.charAt(i) == '\"')
			{
				int index = -1;
				if(check.length()>0)
				{
					tokLexPairs.add(isKeyWord(check.toString()));
					check.delete(0,check.length());
				}
				for(int j=i+1;j<sloc.length();j++)
				{
					if(sloc.charAt(j) == '\"')
					{
						index = j;
					}
				}
				if(index == -1)
				{
					throw new incompleteStringException(sloc);
				}
				else
				{
					tokLexPairs.add(stringAdd(sloc.substring(i, index+1)));
					i=index+1;
				}
			}
			else if(check.length() > 0 && check.charAt(0) == '<' && sloc.charAt(i) == '-')
			{
				check.append(sloc.charAt(i++));
				tokLexPairs.add(isAssignmentOperator(check.toString()));
				check.delete(0,check.length());
			}
			else if(check.length() > 0 && brackets.contains(check.toString()))
			{
				tokLexPairs.add(isBracket(check.toString()));
				check.delete(0,check.length());
			}
			else if((check.length()>0) && (sloc.charAt(i)<'A' || sloc.charAt(i)>'Z') && (sloc.charAt(i)<'a' || sloc.charAt(i)>'z') && (sloc.charAt(i)<'0' || sloc.charAt(i)>'9'))
			{
				if(relationalOperators.containsKey(check.toString()) && sloc.charAt(i)=='=')
				{
					check.append("=");
					i++;
				}
				if((sloc.length()>1) && !(check.charAt(0) == '<' && sloc.charAt(i+1) == '-'))
				{
					tokLexPairs.add(isKeyWord(check.toString()));
					check.delete(0,check.length());
				}
			}
			if(i<sloc.length())
			{
				if((sloc.charAt(i)!=' ' && sloc.charAt(i)!='\t'))
				{
					check.append(sloc.charAt(i));
				}
			}
		}
		if(check.length()>0)
		{
			if(check.charAt(0) == ',')
			{
				tokLexPairs.add(comma());
				check.delete(0,check.length());
			}
			else if(check.charAt(0) == ';')
			{
				tokLexPairs.add(semicolon());
				check.delete(0,check.length());
			}
			else {
				tokLexPairs.add(isKeyWord(check.toString()));
			}
		}
	}
	
	public void readCodeFromFile(String fileName) throws FileNotFoundException, bracketMismatchException
	{
		this.fileName = new StringBuffer(fileName);
		for(int i = 0; i<3;i++)
		{
			this.fileName.deleteCharAt(this.fileName.length()-1);
		}
		this.symFileName = new StringBuffer(this.fileName);
		this.fileName.append(".lex");
		this.symFileName.append(".sym");
		
		int count = 0;
		String sloc;
		Scanner read = new Scanner(new File(fileName));
		while(read.hasNextLine())
		{
			sloc = read.nextLine();
			count++;
			try
			{
				createTokenLexemePairs(sloc);
			}
			catch (notFoundException e)
			{
				System.out.println("\n\nException: Unrecognized Token at line: " + count + ". Please fix this and try again\n");
				break;
			}
			catch (incompleteStringException e)
			{
				System.out.println("\n\nException: Incomplete string at line: " + count + ". Please fix this and try again\n");
				break;
			}
			
		}
		if(!checkBrackets())
		{
			read.close();
			throw new bracketMismatchException("Brackets not matched");
		}
		read.close();
	}
	
	public void writeTokLexToFile() throws noTokLexPairsDefinedException, noInputFileException, FileNotFoundException, UnsupportedEncodingException, parser.badTokenException
	{
		if(tokLexPairs.isEmpty())
		{
			throw new noTokLexPairsDefinedException("Empty toklex pairs array");
		}
		if(this.fileName.toString().isEmpty())
		{
			throw new noInputFileException("No input cmm file provided");
		}
		PrintWriter writer = new PrintWriter(this.fileName.toString(), "UTF-8");
		
		for(int i = 0; i<tokLexPairs.size();i++)
		{
			writer.println("( " + tokLexPairs.get(i).token + " , " + tokLexPairs.get(i).lexeme + " )");
		}
		
		writer.close();
		
		parse();
	}
	
	public void parse() throws parser.badTokenException
	{
		new parser(tokLexPairs,symbolTable, symFileName);
	}
	
	public static void main(String[] args)
	{
		lex a = lex.getInstance();
		if(args[0].isEmpty())
		{
			System.out.println("You must specify an input (.cm) file!");
			return;
		}
		System.out.println("----- Lexical Analyser for cmm -----\n"
				+ "Your input file is: "+args[0]+". If "
						+ "file is not available, program will throw "
						+ "file not found exception.\nPlease check the file "
						+ "path in that case.\n"
						+ "Program produces the following output files:\n"
						+ "1. <InputFileName>.lex  		-- contains <key,value> pairs\n"
						+ "2. <InputFileName>.sym  		-- contains all identifiers\n"
						+ "3. <InputFileName>.tac  		-- contains the three address code\n"
						+ "4. ParseTree.txt		   		-- contains the parse tree\n"
						+ "5. TranslatorSymbolTable.Sym	-- contains symbol table generated by translator\n"
						+ "Program may throw one of the many custom defined exceptions "
						+ "in case of errors in the input file.\nThese exceptions may "
						+ "point out the line number at which the exception occured.\n"
						+ "Some other exceptions may occur, but program will point out the "
						+ "possible cause\n");
		try {
			Thread.sleep(500);
			System.out.println("Reading cm code from " + args[0]);
			a.readCodeFromFile(args[0]);
			Thread.sleep(500);
			System.out.println("File has been read succesfully. Generating tok-lex pairs..");
			a.writeTokLexToFile();
			Thread.sleep(500);
			System.out.println("Token Lexeme pairs generated succesfully!");
			System.out.println("Code parsed succesfully!");
			System.out.println("Three address code generated succesfully!");
		} catch (FileNotFoundException e) {
			System.out.println("Exception: File " + args[0] + " not found");
		}
		catch(bracketMismatchException e)
		{
			System.out.println("\n\nException: Mismatched brackets in code. Please check if you have closed all open brackets");
		}
		catch(noInputFileException e)
		{
			System.out.println("\n\nException: No Input File Specified");
		}
		catch(noTokLexPairsDefinedException e)
		{
			System.out.println("\n\nException: No token-lexeme pairs to write. Please check if input file contains valid cmm code");
		}
		catch(parser.badTokenException e)
		{
			System.out.println("\n\nException: Parser error. Invalid token-lexeme pair found!");
		}
		catch (UnsupportedEncodingException e) {
			System.out.println(e.getMessage());
		}
		catch (Exception e)
		{
			System.out.println("Generic Exception occured. Likely cause: " + e.getCause());
		}
		
	}
}