package interpreter;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

//import cop5556fa19.BuildSymbolTable;
import cop5556fa19.Parser;
import cop5556fa19.Scanner;
import cop5556fa19.Token.Kind;
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
				element.visit(this, arg);
			}else if (element instanceof RetStat) {
				//result.add(new LuaInt(42));
				result = (List<LuaValue>) element.visit(this,arg);
				//result.add(retTemp);
			}else if (element instanceof StatIf) {
				
			}
			else {
				throw new UnsupportedOperationException("visitBlock");
			}
			
		}
		if (result.size() == 0) result = null;
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
			if (varEle instanceof ExpName) varTemp = _G.get(((ExpName) varEle).name);
			
			/*
			 * if (varEle instanceof ExpTableLookup) { varTemp =
			 * (LuaValue)varEle.visit(this, _G); }else if (varEle instanceof ExpName) {
			 * varTemp = (LuaString) varEle.visit(this, _G); }
			 */
			 
			Exp expEle = expList.get(i);
			//LuaValue expTemp = (LuaValue) expEle.visit(this, _G);
			LuaValue expTemp = expVisitJudge(expEle, _G);
			if (expEle instanceof ExpName) expTemp = _G.get(((ExpName) expEle).name);
			if (varEle instanceof ExpName) {
				_G.put(((ExpName) varEle).name, expTemp);
				continue;
			}
			
			if (varEle instanceof ExpTableLookup) { 
				Exp t = ((ExpTableLookup)varEle).table;
				LuaValue luaT = expVisitJudge(t, arg);
				LuaTable tabletemp = (LuaTable)_G.get(luaT);
				Exp k = ((ExpTableLookup)varEle).key;
				
				LuaValue luaK = expVisitJudge(k, arg);
				if (k instanceof ExpName) {
					luaK = _G.get(((ExpName) k).name);
				}
				tabletemp.put(luaK, expTemp);
			}
			 
			else _G.put(varTemp, expTemp);
		}
		if (m > n) {
			for (int i = 0; i < m - n; i++) {
				Exp varEle = varList.get(i);
				LuaValue varTemp = expVisitJudge(varEle, arg);
				_G.put(varTemp, LuaNil.nil);
			}
		}
		return _G;
	}
	
	@Override
	public Object visitStatIf(StatIf statIf, Object arg) throws Exception {
		List<Exp> es = statIf.es;
		List<Block> bs = statIf.bs;
		for (int i = 0; i < es.size(); i++) {
			Exp element = es.get(i);
			LuaBoolean temp = (LuaBoolean)element.visit(this, arg);
			if (temp.value == true) {
				Block btemp = bs.get(i);
				List<LuaValue> result = (List<LuaValue>) btemp.visit(this, arg);
				return result;
			}
		}
		Block btemp = bs.get(bs.size() - 1);
		List<LuaValue> result = (List<LuaValue>) btemp.visit(this, arg);
		return result;
	}
	
	public LuaValue expVisitJudge(Exp element, Object arg) throws Exception {
		if (element instanceof ExpTableLookup) {
			return (LuaValue) element.visit(this, arg);
		}else if (element instanceof ExpName) {
			return (LuaString) element.visit(this, arg);
		}else if (element instanceof ExpString) {
			return (LuaString) element.visit(this, arg);
		}else if (element instanceof ExpInt) {
			return (LuaInt) element.visit(this, arg);
		}else if (element instanceof ExpTrue) {
			return (LuaBoolean) element.visit(this, arg);
		}else if (element instanceof ExpFalse) {
			return (LuaBoolean) element.visit(this, arg);
		}else if (element instanceof ExpTable) {
			return (LuaTable) element.visit(this, arg);
		}else if (element instanceof ExpBinary) {
			return (LuaValue) element.visit(this, arg);
		}
		else return LuaNil.nil;
 	}
	
	@Override
	public Object visitExpBin(ExpBinary expBin, Object arg) throws Exception {
		LuaValue val0; LuaValue val1; 
		if (expBin.e0 instanceof ExpName) val0 = _G.get((LuaValue) expBin.e0.visit(this, arg)); 
		else val0 = (LuaValue) expBin.e0.visit(this, arg); 
		if (expBin.e1 instanceof ExpName) val1 = _G.get((LuaValue) expBin.e1.visit(this, arg)); 
		else val1 = (LuaValue) expBin.e1.visit(this, arg);
		switch(expBin.op) {
		case OP_PLUS: {
			if (!(val0 instanceof LuaInt) || !(val1 instanceof LuaInt)) throw new UnsupportedOperationException("visitExpBinPlus");
			int val = ((LuaInt)val0).v + ((LuaInt)val1).v;
			return new LuaInt(val);
		}
		case OP_MINUS: {
			if (!(val0 instanceof LuaInt) || !(val1 instanceof LuaInt)) throw new UnsupportedOperationException("visitExpBinMinus");
			int val = ((LuaInt)val0).v - ((LuaInt)val1).v;
			return new LuaInt(val);
		}
		case OP_TIMES: {
			if (!(val0 instanceof LuaInt) || !(val1 instanceof LuaInt)) throw new UnsupportedOperationException("visitExpBinTime");
			int val = ((LuaInt)val0).v * ((LuaInt)val1).v;
			return new LuaInt(val);
		}
		case OP_DIV: {
			if (!(val0 instanceof LuaInt) || !(val1 instanceof LuaInt)) throw new UnsupportedOperationException("visitExpBinDiv");
			int val = ((LuaInt)val0).v / ((LuaInt)val1).v;
			return new LuaInt(val);
		}
		case OP_DIVDIV: {
			if (!(val0 instanceof LuaInt) || !(val1 instanceof LuaInt)) throw new UnsupportedOperationException("visitExpBinDivDiv");
			int val = Math.floorDiv(((LuaInt)val0).v, ((LuaInt)val1).v);
			return new LuaInt(val);
		}
		case OP_MOD: {
			if (!(val0 instanceof LuaInt) || !(val1 instanceof LuaInt)) throw new UnsupportedOperationException("visitExpBinMOD");
			int val = ((LuaInt)val0).v % ((LuaInt)val1).v;
			return new LuaInt(val);
		}
		case BIT_SHIFTL: {
			if (!(val0 instanceof LuaInt) || !(val1 instanceof LuaInt)) throw new UnsupportedOperationException("visitExpBinSHIFTL");
			int val = ((LuaInt)val0).v << ((LuaInt)val1).v;
			return new LuaInt(val);
		}
		case BIT_SHIFTR: {
			if (!(val0 instanceof LuaInt) || !(val1 instanceof LuaInt)) throw new UnsupportedOperationException("visitExpBinSHIFTR");
			int val = ((LuaInt)val0).v >> ((LuaInt)val1).v;
			return new LuaInt(val);
		}
		case BIT_AMP: {
			if (!(val0 instanceof LuaInt) || !(val1 instanceof LuaInt)) throw new UnsupportedOperationException("visitExpBinAMP");
			int val = ((LuaInt)val0).v & ((LuaInt)val1).v;
			return new LuaInt(val);
		}
		case BIT_OR: {
			if (!(val0 instanceof LuaInt) || !(val1 instanceof LuaInt)) throw new UnsupportedOperationException("visitExpBinOr");
			int val = ((LuaInt)val0).v | ((LuaInt)val1).v;
			return new LuaInt(val);
		}
		case BIT_XOR: {
			if (!(val0 instanceof LuaInt) || !(val1 instanceof LuaInt)) throw new UnsupportedOperationException("visitExpBinXOR");
			int val = ((LuaInt)val0).v ^ ((LuaInt)val1).v;
			return new LuaInt(val);
		}
		case OP_POW: {
			if (!(val0 instanceof LuaInt) || !(val1 instanceof LuaInt)) throw new UnsupportedOperationException("visitExpBinDivDiv");
			int val = (int) Math.pow(((LuaInt)val0).v, ((LuaInt)val1).v);
			return new LuaInt(val);
		}
		case KW_and: {
			if (val0 instanceof LuaString || val1 instanceof LuaString) return new LuaBoolean(false);
			if (val0 instanceof LuaBoolean && ((LuaBoolean)val0).value == false) return val0;
			if (val0 instanceof LuaNil) return val0;
			return val1;
		}
		case KW_or: {
			if (val0 instanceof LuaString || val1 instanceof LuaString) return new LuaBoolean(false);
			if ((!(val0 instanceof LuaBoolean) || !(((LuaBoolean)val0).value == false)) && (!(val0 instanceof LuaNil))) return val0;
			return val1;
		}
		case REL_EQEQ: {
			if (val0.getClass() != val1.getClass()) return new LuaBoolean(false);
			if (val0 instanceof LuaBoolean) {
				if (((LuaBoolean)val0).equals(val1)) return new LuaBoolean(true);
				else return new LuaBoolean(false);
			}else if (val0 instanceof LuaTable) {
				if (((LuaTable)val0).equals(val1)) return new LuaBoolean(true);
				else return new LuaBoolean(false);
			}else if (val0 instanceof LuaString) {
				if (((LuaString)val0).equals(val1)) return new LuaBoolean(true);
				else return new LuaBoolean(false);
			}else if (val0 instanceof LuaInt) {
				if (((LuaInt)val0).equals(val1)) return new LuaBoolean(true);
				else return new LuaBoolean(false);
			}else {
				if (((LuaNil)val0).equals(val1)) return new LuaBoolean(true);
				else return new LuaBoolean(false);
			}
		}
		case REL_NOTEQ: {
			if (val0.getClass() != val1.getClass()) return new LuaBoolean(false);
			if (val0 instanceof LuaBoolean) {
				if (!((LuaBoolean)val0).equals(val1)) return new LuaBoolean(true);
				else return new LuaBoolean(false);
			}else if (val0 instanceof LuaTable) {
				if (!((LuaTable)val0).equals(val1)) return new LuaBoolean(true);
				else return new LuaBoolean(false);
			}else if (val0 instanceof LuaString) {
				if (!((LuaString)val0).equals(val1)) return new LuaBoolean(true);
				else return new LuaBoolean(false);
			}else if (val0 instanceof LuaInt) {
				if (!((LuaInt)val0).equals(val1)) return new LuaBoolean(true);
				else return new LuaBoolean(false);
			}else {
				if (!((LuaNil)val0).equals(val1)) return new LuaBoolean(true);
				else return new LuaBoolean(false);
			}
		}
		case REL_LT: {
			if (val0.getClass() != val1.getClass()) return new LuaBoolean(false);
			if (val0 instanceof LuaBoolean) {
				throw new UnsupportedOperationException("visitExpBinLTBoolean");
			}else if (val0 instanceof LuaTable) {
				throw new UnsupportedOperationException("visitExpBinLTTABLE");
			}else if (val0 instanceof LuaString) {
				throw new UnsupportedOperationException("visitExpBinLTSTRING");
			}else if (val0 instanceof LuaInt) {
				if (((LuaInt)val0).v < ((LuaInt)val1).v) return new LuaBoolean(true);
				else return new LuaBoolean(false);
			}else {
				throw new UnsupportedOperationException("visitExpBinLTNIL");
			}
		}
		case REL_GT: {
			if (val0.getClass() != val1.getClass()) return new LuaBoolean(false);
			if (val0 instanceof LuaInt) {
				if (((LuaInt)val0).v > ((LuaInt)val1).v) return new LuaBoolean(true);
				else return new LuaBoolean(false);
			}else {
				throw new UnsupportedOperationException("visitExpBinGT");
			}
		}
		case REL_LE: {
			if (val0.getClass() != val1.getClass()) return new LuaBoolean(false);
			if (val0 instanceof LuaInt) {
				if (((LuaInt)val0).v <= ((LuaInt)val1).v) return new LuaBoolean(true);
				else return new LuaBoolean(false);
			}else {
				throw new UnsupportedOperationException("visitExpBinLE");
			}
		}
		case REL_GE: {
			if (val0.getClass() != val1.getClass()) return new LuaBoolean(false);
			if (val0 instanceof LuaInt) {
				if (((LuaInt)val0).v >= ((LuaInt)val1).v) return new LuaBoolean(true);
				else return new LuaBoolean(false);
			}else {
				throw new UnsupportedOperationException("visitExpBinGE");
			}
		}
		}
		return val1;
		
	}
	
	
	public String StringBin(Kind op, String v0, String v1) {
		String str = "";
		switch(op) {
		case DOTDOT: str = v0 + v1;break;
		default: str = "";break;
		}
		return str;
	}
	
	public int intBin(Kind op, int v0, int v1) {
		int val = Integer.MIN_VALUE;
		switch(op) {
		case OP_PLUS: val = v0 + v1;break;
		case OP_MINUS: val = v0 - v1;break;
		case OP_TIMES: val = v0 * v1;break;
		case OP_DIV: val = v0 / v1;break;
		case OP_DIVDIV: val = Math.floorDiv(v0, v1);break;
		case OP_MOD: val = v0 % v1;break;
		case BIT_SHIFTL: val = v0 << v1;break;
		case BIT_SHIFTR: val = v0 >> v1;break;
		case BIT_AMP: val = v0 & v1;break;
		case BIT_OR: val = v0 | v1;break;
		case BIT_XOR: val = v0 ^ v1;break;
		case OP_POW: val = (int) Math.pow(v0, v1);break;
		case KW_and: val = v1;break;
		case KW_or: val = v0;break;
		}
		return val;
	}
	
	@Override
	public Object visitExpTableLookup(ExpTableLookup expTableLookup, Object arg) throws Exception {
		Exp t = expTableLookup.table;
		LuaValue luaT = expVisitJudge(t, arg);
		Exp k = expTableLookup.key;
		
		LuaValue luaK = expVisitJudge(k, arg);
		if (k instanceof ExpName) {
			luaK = _G.get(((ExpName) k).name);
		}
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
				//if (_G.get(key) != LuaNil.nil) key = _G.get(key);
				if (k instanceof ExpName) key = _G.get(((ExpName) k).name);
				LuaValue value = expVisitJudge(v, arg);
				if (v instanceof ExpName) value = _G.get(((ExpName) v).name);
				res.put(key, value);
			}else if (element instanceof FieldNameKey) {
				Name n = ((FieldNameKey) element).name;
				Exp ex = ((FieldNameKey) element).exp;
				/*
				 * LuaValue name = new LuaString(n.name); if (_G.get(name) != LuaNil.nil) name =
				 * _G.get(name);
				 */
				LuaValue exp = expVisitJudge(ex, arg);
				if (ex instanceof ExpName) exp = _G.get(((ExpName) ex).name);
				res.put(n.name, exp);
			}else if (element instanceof FieldImplicitKey) {
				Exp ex = ((FieldImplicitKey) element).exp;
				LuaValue exp = expVisitJudge(ex, arg);
				if (ex instanceof ExpName) exp = _G.get(((ExpName) ex).name);
				res.putImplicit(exp);
			}else {
				throw new UnsupportedOperationException("visitExpTable");
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
			}else if (ele instanceof ExpName || ele instanceof ExpTableLookup){
				//LuaString temp = (LuaString) ele.visit(this,arg);
				result.add(_G.get(temp));
			}else result.add(temp);
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
