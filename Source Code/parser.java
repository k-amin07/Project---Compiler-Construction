import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

/*
 * Grammar:
* P -> P D | ^
* D -> V | F
* V -> T id ;
* T -> int | char
* F -> def T id ( OPL ) { SL }
* OPL -> PL | ^
* PL -> PAR , PL | PAR
* PAR -> T id
* SL -> S SL | ^
* S -> A | SS | I | PR | RT
* SS -> if ( E ) S | if ( E ) S else S
* A -> id <- E2 ;
* I -> while ( E ) S
* PR -> print L; | print id;
* RT-> ret L; | ret ID;
* L-> 'digit' | 'character' | num
* E2-> id | NC | str
* E->R E'
* E' -> == R E' | <> R E' | ^
* R -> N R'
* R' -> < N R' | > N R' | <= N R' | >= N R' | ^
* N -> G Q N'
* N' -> + Q N' | - Q N' | ^
* Q -> * QN' | / Q N' | ^
* G -> id | num | ( E )
 */

public class parser{
	
	public class badTokenException extends Exception{
		private static final long serialVersionUID = 1L;
		public badTokenException(String s) {
			super(s);
		}
	}
	private pair look;
	private ArrayList<pair> tokLexPairs;
	private int current;
	private ArrayList<String> symbolTable;
	private ArrayList<String> dataTypes;
	private ArrayList<String> lexemes;
	private boolean isFunc = false;
	private String symFileName;
	private String currentDataType;
	private String parseTree;
	private int tabCount;
	
	// Get tok-lex pairs from Lexical Analyser
	parser(ArrayList<pair> tLPairs, ArrayList<String> symtable, StringBuffer sName) throws badTokenException {
		current = 0;
		tokLexPairs = new ArrayList<pair>(tLPairs);
		symbolTable = new ArrayList<String>(symtable);
		dataTypes = new ArrayList<String>();
		lexemes = new ArrayList<String>();
		symFileName = new String(sName.toString());
		parseTree = new String();
		tabCount = 0;
		nextTok();
		P();
		try {
			writeSymbolTable();
			printParseTree();
		} catch (FileNotFoundException e) {
			System.out.println("File not found exception!");
		} catch (UnsupportedEncodingException e) {
			System.out.println("Unsupported encoding exception!");
		}
	}
	
	void printParseTree() throws FileNotFoundException, UnsupportedEncodingException
	{
		PrintWriter parseTreeWriter = new PrintWriter("ParseTree.txt", "UTF-8");
		parseTreeWriter.write(parseTree);
		parseTreeWriter.close();
		translate();
	}
	
	public void translate()
	{
		new Translater(tokLexPairs, symbolTable, symFileName, dataTypes);
	}
	
	void writeSymbolTable() throws FileNotFoundException, UnsupportedEncodingException
	{
		PrintWriter symWriter = new PrintWriter(this.symFileName.toString(), "UTF-8");
		for(int i = 0;i<symbolTable.size();i++)
		{
			symWriter.println(symbolTable.get(i) + "\t" + dataTypes.get(i));
		}
		symWriter.close();
	}
	void addTabs() {
		for(int i = 0; i<tabCount;i++)
		{
			parseTree+="  ";
		}
	}
	
	// Get Next token, update current
	void nextTok() throws badTokenException {
		if(current < tokLexPairs.size())
		{
			look = tokLexPairs.get(current++);
		}
	}
	void P() throws badTokenException {
		parseTree += "P\n";
		addTabs();
		parseTree +="|__";
		tabCount++;
		D();
		tabCount--;
	}
	void D() throws badTokenException {
		parseTree+="D\n";
		addTabs();
		parseTree +="|__";
		tabCount++;
		if(look.token.equals("DEF")) {
			F();
		}
		else throw new badTokenException("Program must start with def");
		tabCount--;
	}
	void V() throws badTokenException {
		parseTree+="V\n";
		addTabs();
		parseTree +="|__";
		tabCount++;
		T();
		if(!lexemes.contains(look.lexeme))
		{
			dataTypes.add(currentDataType);
			lexemes.add(look.lexeme);
		}
		addTabs();
		parseTree +="|__";
		match("ID");
		tabCount--;
	}
	void T() throws badTokenException {
		parseTree+="T\n";
		addTabs();
		parseTree +="|__";
		tabCount++;
		if(look.token.equals("INT")) {
			match("INT");
			currentDataType = "INT";
			if(isFunc)
			{
				dataTypes.add("FUN");
				isFunc = false;
			}
		}
		else if(look.token.equals("CHAR")) {
			match("CHAR");
			currentDataType = "CHAR";
			if(isFunc)
			{
				dataTypes.add("FUN");
				isFunc = false;
				currentDataType = "CHAR";
			}
		}
		tabCount--;
	}
	void F() throws badTokenException {
		parseTree+="F\n";
		addTabs();
		parseTree +="|__";
		if(look.token.equals("DEF"))
		{
			match("DEF");
			isFunc = true;
			addTabs();
			parseTree +="|__";
			tabCount++;
			V();
			addTabs();
			parseTree +="|__";
			match("(");
			addTabs();
			parseTree +="|__";
			tabCount++;
			OPL();
			addTabs();
			parseTree +="|__";
			tabCount++;
			match(")");
			tabCount--;
			addTabs();
			parseTree +="|__";
			tabCount++;
			match("{");
			addTabs();
			parseTree +="|__";
			tabCount++;
			SL();
			addTabs();
			parseTree +="|__";
			match("}");
		}
		else throw new badTokenException("Expected function declaration");
	}
	void OPL() throws badTokenException {
		parseTree+="OPL\n";
		addTabs();
		parseTree +="|__";
		tabCount++;
		if(!look.token.equals(")")) {
			PL();
		}
		tabCount--;
	}
	void PL() throws badTokenException {
		parseTree+="PL\n";
		if(!look.token.equals(")"))
		{
			addTabs();
			parseTree +="|__";
			tabCount++;
			PAR();
			if(!look.token.equals(")"))
			{
				addTabs();
				parseTree +="|__";
				match(",");
				currentDataType = "^";
			}
			tabCount--;
			addTabs();
			parseTree +="|__";
			tabCount++;
			PL();
			tabCount--;
		}
		else {
			tabCount--;
		}
	}
	void PAR() throws badTokenException {
		parseTree+="PAR\n";
		addTabs();
		parseTree +="|__";
		tabCount++;
		T();
		if(!lexemes.contains(look.lexeme) && look.lexeme!="^")
		{
			dataTypes.add(currentDataType);
			lexemes.add(look.lexeme);
		}
		addTabs();
		parseTree +="|__";
		match("ID");
		tabCount--;
	}
	void SL() throws badTokenException {
		parseTree+="SL\n";
		addTabs();
		parseTree +="|__";
		tabCount++;
		if(!look.token.equals("}"))
		{
			S();
			SL();
		}
		tabCount--;
	}
	void S() throws badTokenException {
		parseTree+="S\n";
		addTabs();
		parseTree +="|__";
		tabCount++;
		if(look.token.equals("INT"))
		{
			match("INT");
			currentDataType = "INT";
			addTabs();
			parseTree +="|__";
		}
		else if(look.token.equals("CHAR"))
		{
			match("CHAR");
			currentDataType = "CHAR";
			addTabs();
			parseTree +="|__";
		}
		if(look.token.equals("ID"))
		{
			if(!lexemes.contains(look.lexeme) && currentDataType!="^")
			{
				dataTypes.add(currentDataType);
				lexemes.add(look.lexeme);
			}
			A();
		}
		else if (look.token.equals("IF"))
		{
			SS();
		}
		else if(look.token.equals("WHILE"))
		{
			I();
		}
		else if(look.token.equals("PRINT"))
		{
			PR();
		}
		else if(look.token.equals("RET"))
		{
			RT();
		}
		else if(look.token.equals("READ"))
		{
			read();
		}
		else if(look.token.equals("+") || look.token.equals("-"))
		{
			NPrime();
		}
		else if(look.token.equals("*") || look.token.equals("/"))
		{
			Q();
		}
		else
			throw new badTokenException("Unidentified token for parser");
		matchComSemi();
		tabCount--;
	}
	void matchComSemi() throws badTokenException
	{
		addTabs();
		parseTree +="|__";
		if(look.token.equals(","))
		{
			match(",");
		}
		else if(look.token.equals(";"))
		{
			match(";");
		}
	}
	void read() throws badTokenException
	{
		parseTree+="read\n";
		addTabs();
		parseTree +="|__";
		match("READ");
		addTabs();
		parseTree +="|__";
		match("ID");
		matchComSemi();
		tabCount--;
	}
	void SS() throws badTokenException {
		parseTree+="SS\n";
		addTabs();
		parseTree +="|__";
		tabCount++;
		match("IF");
		match("(");
		E();
		match(")");
		match("{");
		S();
		match("}");
		if(look.token.equals("ELSE"))
		{
			match("ELSE");
			match("{");
			S();
			match("}");
		}
		tabCount--;
	}
	void A() throws badTokenException {
		parseTree+="A\n";
		addTabs();
		parseTree +="|__";
		tabCount++;
		match("ID");
		if(look.token.equals("<-"))
		{
			addTabs();
			parseTree +="|__";
			tabCount++;
			match("<-");
			addTabs();
			parseTree +="|__";
			tabCount++;
			E2();
		}
		matchComSemi();
		tabCount--;
	}
	void I() throws badTokenException {
		parseTree+="I\n";
		addTabs();
		parseTree +="|__";
		tabCount++;
		match("WHILE");
		addTabs();
		parseTree +="|__";
		tabCount++;
		match("(");
		addTabs();
		parseTree +="|__";
		tabCount++;
		E();
		addTabs();
		parseTree +="|__";
		match(")");
		addTabs();
		parseTree +="|__";
		tabCount++;
		match("{");
		addTabs();
		parseTree +="|__";
		SL();
		match("}");
		tabCount--;
	}
	void PR() throws badTokenException {
		parseTree+="PR\n";
		addTabs();
		parseTree +="|__";
		tabCount++;
		match("PRINT");
		addTabs();
		parseTree +="|__";
		tabCount++;
		L();
		matchComSemi();
		tabCount--;
	}
	void RT() throws badTokenException {
		parseTree+="RT\n";
		addTabs();
		parseTree +="|__";
		tabCount++;
		match("RET");
		L();
		matchComSemi();
		tabCount--;
	}
	void L() throws badTokenException {
		parseTree+="L\n";
		addTabs();
		parseTree +="|__";
		tabCount++;
		if(look.token.equals("ID"))
		{
			match("ID");
		}
		else if (look.token.equals("NC"))
		{
			match("NC");
		}
		else if(look.token.equals("STR")) {
			match("STR");
		}
		tabCount--;
	}
	void E2() throws badTokenException {
		parseTree+="E2\n";
		if(look.token.equals("ID"))
		{
			addTabs();
			parseTree +="|__";
			tabCount++;
			match("ID");
		}
		else if(look.token.equals("NC")) {
			addTabs();
			parseTree +="|__";
			tabCount++;
			match("NC");
		}
		else if(look.token.equals("STR")) {
			addTabs();
			parseTree +="|__";
			tabCount++;
			match("STR");
		}
		tabCount--;
	}
	//E2 declared to fix assigment operator
	void E() throws badTokenException {
		parseTree+="E\n";
		addTabs();
		parseTree +="|__";
		tabCount++;
		R();
		addTabs();
		parseTree +="|__";
		tabCount++;
		EPrime();
		tabCount--;
	}
	void EPrime() throws badTokenException {
		parseTree+="E`\n";
		if(look.token.equals("EE"))
		{
			addTabs();
			parseTree +="|__";
			tabCount++;
			match("EE");
			R();
			EPrime();
		}
		else if(look.token.equals("NE"))
		{
			addTabs();
			parseTree +="|__";
			tabCount++;
			match("NE");
			R();
			EPrime();
		}
		tabCount--;
	}
	void R() throws badTokenException {
		parseTree+="R\n";
		addTabs();
		parseTree +="|__";
		tabCount++;
		N();
		addTabs();
		parseTree +="|__";
		tabCount++;
		RPrime();
		tabCount--;
	}
	void RPrime() throws badTokenException {
		parseTree+="R'\n";
		if(look.token.equals("RO"))
		{
			addTabs();
			parseTree +="|__";
			match("RO");
			addTabs();
			parseTree +="|__";
			tabCount++;
			N();
			addTabs();
			parseTree +="|__";
			tabCount++;
			RPrime();
		}
		tabCount--;
	}
	void N() throws badTokenException {
		parseTree+="N\n";
		addTabs();
		parseTree +="|__";
		tabCount++;
		G();
		tabCount--;
		addTabs();
		parseTree +="|__";
		Q();
		tabCount++;
		addTabs();
		parseTree +="|__";
		NPrime();
	}
	
	void NPrime() throws badTokenException {
		parseTree+="N'\n";
		if(look.token.equals("+"))
		{
			addTabs();
			parseTree +="|__";
			match("+");
			if(look.token.equals("ID"))
			{
				addTabs();
				parseTree +="|__";
				match("ID");
			}
			else
			{
				addTabs();
				parseTree +="|__";
				match("NC");
			}
			tabCount++;
			addTabs();
			parseTree +="|__";
			NPrime();
			addTabs();
			parseTree +="|__";
			Q();
		}
		else if(look.token.equals("-")) {
			addTabs();
			parseTree +="|__";
			tabCount++;
			match("-");
			if(look.token.equals("ID"))
				match("ID");
			else match("NC");
			NPrime();
			Q();
		}
		tabCount--;
	}
	
	void Q() throws badTokenException {
		parseTree+="Q\n";
		if(look.token.equals("*"))
		{
			addTabs();
			parseTree +="|__";
			tabCount++;
			match("*");
			if(look.token.equals("ID"))
				match("ID");
			else match("NC");
			NPrime();
			Q();
		}
		else if(look.token.equals("/")) {
			addTabs();
			parseTree +="|__";
			tabCount++;
			match("/");
			if(look.token.equals("ID"))
				match("ID");
			else if (look.token.equals("NC"))
				match("NC");
			else throw new badTokenException("bad token at ");
			NPrime();
			Q();
		}
		tabCount--;
	}
	
	void G() throws badTokenException {
		parseTree+="G\n";
		addTabs();
		parseTree +="|__";
		tabCount++;
		if(look.token.equals("ID"))
		{
			match("ID");
		}
		else if(look.token.equals("NC")) {
			match("NC");
		}
		else {
			match("(");
			E();
			match(")");
		}
		tabCount--;
	}
	
	void match(String a) throws badTokenException {
		if(look.token.equals(a)) {
			parseTree+=look.token + " ("+look.lexeme+")\n";
			nextTok();
		}
		else {
			throw new badTokenException("Bad token "+look.token);
		}
	}
	
}