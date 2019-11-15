package interpreter;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

//import cop5556fa19.BuildSymbolTable;
import cop5556fa19.Parser;
import cop5556fa19.Scanner;
import cop5556fa19.AST.*;
import interpreter.built_ins.print;
import interpreter.built_ins.println;
import interpreter.built_ins.toNumber;


public class Interpreter extends ASTVisitorAdapter{



	
	LuaTable _G; //global environment

	/* Instantiates and initializes global environment
	 * 
	 * Initially, the "standard library" routines implemented in Java are loaded.  For this assignment,
	 * this is just print and println.  
	 * 
	 * These functions impl
	 */
	void init_G() {
		_G = new LuaTable();
		_G.put("print", new print());
		_G.put("println", new println());
		_G.put("toNumber", new toNumber());
	}
	
	ASTNode root; //useful for debugging
		
	public Interpreter() {
		init_G();
	}
	
	@Override
	public Object visitChunk(Chunk chunk, Object arg) throws Exception {
		System.out.print("adada");
		LuaInt bb = new LuaInt(42);
		List<LuaInt> list = new ArrayList<>();
		list.add(bb);
		return list;
	}
	
	
	
	@SuppressWarnings("unchecked")
	public List<LuaValue> load(Reader r) throws Exception {
		Scanner scanner = new Scanner(r); 
		Parser parser = new Parser(scanner);
		Chunk chunk = parser.parse();
		root = chunk;
		//Perform static analysis to prepare for goto.  Uncomment after u
//		StaticAnalysis hg = new StaticAnalysis();
//		chunk.visit(hg,null);	
		//Interpret the program and return values returned from chunk.visit
		List<LuaValue> vals = (List<LuaValue>) chunk.visit(this,_G);
		return vals;
	}
	


	

}
