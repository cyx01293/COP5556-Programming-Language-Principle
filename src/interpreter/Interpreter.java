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
	
	/*
	 * @Override public Object visitChunk(Chunk chunk, Object arg) throws Exception
	 * { System.out.print("adada"); ExpInt e1 = Expressions.makeExpInt(42); LuaInt
	 * bb = (LuaInt) e1.visit(this,_G); List<LuaInt> list = new ArrayList<>();
	 * list.add(bb); return list; }
	 */
	
	@Override
	public Object visitChunk(Chunk chunk, Object arg) throws Exception {
		Block b = chunk.block;
		return b.visit(this,arg);
	}
	
	@Override
	public Object visitBlock(Block block, Object arg) throws Exception {
		List<Stat> list = block.stats;
		List<LuaValue> result = new ArrayList<>();
		for (int i = 0; i < list.size(); i++) {
			Stat element = list.get(i);
			//String type = list.get(i).getClass().getName();
			if(element instanceof StatAssign) {
				//_G = (LuaTable) element.visit(this,  _G);
				element.visit(this,  arg);
			}else if (element instanceof RetStat) {
				//result.add(new LuaInt(42));
				result = (List<LuaValue>) element.visit(this,arg);
				//result.add(retTemp);
			};
			
		}
		return result;
	}
	
	@Override
	public Object visitStatAssign(StatAssign statAssign, Object arg) throws Exception {
		List<Exp> varList = statAssign.varList;
		List<Exp> expList = statAssign.expList;
		int m = varList.size(), n = expList.size();
		int min = Math.min(m, n);
		for (int i = 0; i < min; i++) {
			Exp varEle = varList.get(i);
			LuaValue varTemp = expVisitJudge(varEle, arg);
			
			/*
			 * if (varEle instanceof ExpTableLookup) { varTemp =
			 * (LuaValue)varEle.visit(this, _G); }else if (varEle instanceof ExpName) {
			 * varTemp = (LuaString) varEle.visit(this, _G); }
			 */
			 
			Exp expEle = expList.get(i);
			//LuaValue expTemp = (LuaValue) expEle.visit(this, _G);
			LuaValue expTemp = expVisitJudge(expEle, _G);
			_G.put(varTemp, expTemp);
		}
		return _G;
	}
	
	public LuaValue expVisitJudge(Exp element, Object arg) throws Exception {
		if (element instanceof ExpTableLookup) {
			return (LuaTable) element.visit(this, arg);
		}else if (element instanceof ExpName) {
			return (LuaString) element.visit(this, arg);
		}else if (element instanceof ExpString) {
			return (LuaString) element.visit(this, arg);
		}else if (element instanceof ExpInt) {
			return (LuaInt) element.visit(this, arg);
		}else if (element instanceof ExpTrue) {
			return (LuaBoolean) element.visit(this, arg);
		}else if (element instanceof ExpTableLookup) {
			return (LuaValue) element.visit(this, arg);
		}else if (element instanceof ExpTable) {
			return (LuaTable) element.visit(this, arg);
		}
		else return LuaNil.nil;
 	}
	
	@Override
	public Object visitExpTableLookup(ExpTableLookup expTableLookup, Object arg) throws Exception {
		Exp t = expTableLookup.table;
		LuaValue luaT = expVisitJudge(t, arg);
		Exp k = expTableLookup.key;
		LuaValue luaK = expVisitJudge(k, arg);
		if (_G.get(luaT) instanceof LuaTable) {
			LuaTable temp = (LuaTable)_G.get(luaT);
			return temp.get(luaK);
		}else {
			throw new UnsupportedOperationException("visitExpTableLookup");
		}
	}
	
	@Override
	public Object visitExpTable(ExpTable expTableConstr, Object arg) throws Exception {
		List<Field> fieldList = expTableConstr.fields;
		LuaTable res = new LuaTable();
		for (int i = 0; i < fieldList.size(); i++) {
			Field element = fieldList.get(i);
			if (element instanceof FieldExpKey) {
				Exp k = ((FieldExpKey) element).key;
				Exp v = ((FieldExpKey) element).value;
				LuaValue key = expVisitJudge(k, arg);
				LuaValue value = expVisitJudge(v, arg);
				res.put(key, value);
			}else if (element instanceof FieldNameKey) {
				Name n = ((FieldNameKey) element).name;
				Exp ex = ((FieldNameKey) element).exp;
				LuaString name = new LuaString(n.name);
				LuaValue exp = expVisitJudge(ex, arg);
				res.put(name, exp);
			}else if (element instanceof FieldImplicitKey) {
				Exp ex = ((FieldImplicitKey) element).exp;
				LuaValue exp = expVisitJudge(ex, arg);
				res.putImplicit(exp);
			}
		}
		return res;
	}
	
	@Override
	public Object visitExpName(ExpName expName, Object arg) {
		LuaString val = new LuaString(expName.name);
		return val;
	}
	
	@Override
	public Object visitExpTrue(ExpTrue expTrue, Object arg) {
		LuaBoolean val = new LuaBoolean(true);
		return val;
	}
	
	@Override
	public Object visitExpFalse(ExpFalse expFalse, Object arg) {
		LuaBoolean val = new LuaBoolean(false);
		return val;
	}
	
	@Override
	public Object visitRetStat(RetStat retStat, Object arg) throws Exception {
		List<Exp> list = retStat.el;
		List<LuaValue> result = new ArrayList<>();
		for (int i = 0; i < list.size(); i++) {
			Exp ele = list.get(i);
			LuaValue temp = expVisitJudge(ele, arg);
			if (ele instanceof ExpInt) {
				//LuaInt temp = (LuaInt) ele.visit(this,arg);
				result.add(temp);
			}else if (ele instanceof ExpName) {
				//LuaString temp = (LuaString) ele.visit(this,arg);
				result.add(_G.get(temp));
			}
		}
		return result;
	}
	
	@Override
	public Object visitExpList(ExpList expList, Object arg) throws Exception {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitParList(ParList parList, Object arg) throws Exception {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public Object visitExpInt(ExpInt expInt, Object arg) {
		LuaInt val = new LuaInt(expInt.v);
		return val;
	}
	
	@Override
	public Object visitExpString(ExpString expString, Object arg) {
		LuaString val = new LuaString(expString.v);
		return val;
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
