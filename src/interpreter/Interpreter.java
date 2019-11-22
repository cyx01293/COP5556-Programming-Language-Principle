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
	List<StatGoto> listGoto = new ArrayList<>();
		
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
		/*
		 * List<LuaValue> result = new ArrayList<>(); result =
		 * (List<LuaValue>)b.visit(this,arg); while (result.size() > 0 &&
		 * result.get(0).equals(new LuaString("StatGoto"))) { StatGoto gototemp =
		 * listGoto.remove(0); StatLabel target = gototemp.label; compare(b, target); }
		 */
		return b.visit(this,arg);
	}
	
	
	@Override
	public Object visitBlock(Block block, Object arg) throws Exception {
		List<Stat> list = block.stats;
		List<LuaValue> result = new ArrayList<>();
		for (int i = 0; i < list.size(); i++) {
			if (listGoto.size() > 0) {
				StatGoto gototemp = listGoto.remove(0);
				StatLabel gotoTarget = gototemp.label;
				if (!gotoTarget.enclosingBlock.equals(block)) {
					//return target;
					listGoto.add(gototemp);
					break;
				}else {
					i = gotoTarget.index;
				}
			}
			Stat element = list.get(i);
			//String type = list.get(i).getClass().getName();
			if(element instanceof StatAssign) {
				//_G = (LuaTable) element.visit(this,  _G);
				element.visit(this, arg);
			}else if (element instanceof RetStat) {
				//result.add(new LuaInt(42));
				result = (List<LuaValue>) element.visit(this,arg);
				if (result != null && result.size() != 0) {
					return result;
				}
				//result.add(retTemp);
			}else if (element instanceof StatIf) {
				/*
				 * Object target = element.visit(this, arg); if (target instanceof StatLabel) {
				 * if (!((StatLabel) target).enclosingBlock.equals(block)) { return target;
				 * }else { i = ((StatLabel) target).index; } }
				 */
				result = (List<LuaValue>) element.visit(this, arg);
				if (result != null && result.size() != 0) {
					return result;
				}
			}else if (element instanceof StatRepeat) {
				result = (List<LuaValue>) element.visit(this, arg);
				if (result != null && result.size() != 0) {
					return result;
				}
			}else if (element instanceof StatWhile) {
				result = (List<LuaValue>) element.visit(this, arg);
				if (result != null && result.size() != 0) {
					return result;
				}
			}else if (element instanceof StatDo) {
				result = (List<LuaValue>) element.visit(this, arg);
				if (result != null && result.size() != 0) {
					return result;
				}
			}else if (element instanceof StatBreak) {
				result = (List<LuaValue>) element.visit(this, arg);
				if (result != null && result.size() != 0) {
					return result;
				}
			}else if (element instanceof StatLabel) {
				
			}else if (element instanceof StatGoto) {
				/*
				 * StatLabel target = ((StatGoto)element).label; if
				 * (!target.enclosingBlock.equals(block)) { return target; }else { i =
				 * target.index; }
				 */
				listGoto.add((StatGoto) element);
				if (!((StatGoto) element).label.enclosingBlock.equals(block)) {
					//return target;
					break;
				}else {
					i = ((StatGoto) element).label.index;
				}
				//result.add(new LuaString("StatGoto"));
				
			}
			else {
				throw new TypeException(block.firstToken, "visitBlock");
			}
			
		}
		if (result != null && result.size() == 0) result = null;
		return result;
	}
	
	@Override
	public Object visitStatGoto(StatGoto statGoto, Object arg) throws Exception {
		StatLabel target = statGoto.label;
		return target;
	}
	
	@Override
	public Object visitLabel(StatLabel statLabel, Object ar) {
		throw new UnsupportedOperationException();
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
			if (expEle instanceof ExpFunctionCall) {
				List<Exp> fnargs = ((ExpFunctionCall)expEle).args;
				List<LuaValue> funcInp = new ArrayList<>();
				List<LuaValue> funcRet = new ArrayList<>();
				ExpName fn = (ExpName) ((ExpFunctionCall)expEle).f;
				LuaValue luafn = expVisitJudge(fn, arg);
				for (int j = 0; j < fnargs.size();j++) {
					Exp fnargsGet = fnargs.get(j);
					if (fnargsGet instanceof ExpString) {
						funcInp.add(expVisitJudge(fnargs.get(j), arg));
					} else /*
							 * if (fnargsGet instanceof ExpName) { funcInp.add(_G.get(((ExpName)
							 * fnargsGet).name)); }else if (fnargsGet instanceof ExpTableLookup) { Exp t =
							 * ((ExpTableLookup)fnargsGet).table; LuaValue luaT = expVisitJudge(t, arg);
							 * 
							 * Exp k = ((ExpTableLookup)fnargsGet).key;
							 * 
							 * LuaValue luaK = expVisitJudge(k, arg); if (k instanceof ExpName) { luaK =
							 * _G.get(((ExpName) k).name); }else { luaK = _G.get(luaK); funcInp.add(luaK); }
							 * 
							 * }
							 */
					funcInp.add(check(fnargsGet, arg));
				}
				JavaFunction jf = (JavaFunction) _G.get(luafn);
				funcRet = jf.call(funcInp);
				if (funcRet.size() > 0) expTemp = funcRet.get(0);
				else expTemp = LuaNil.nil;
			}
			if (varEle instanceof ExpName) {
				_G.put(((ExpName) varEle).name, expTemp);
				continue;
			}
			
			if (varEle instanceof ExpTableLookup) { 
				Exp t = ((ExpTableLookup)varEle).table;
				LuaValue luaT = expVisitJudge(t, arg);
				LuaTable tabletemp = (LuaTable)_G.get(luaT);
				Exp k = ((ExpTableLookup)varEle).key;
				LuaValue luaK = check(k, arg);
				/*
				 * LuaValue luaK = expVisitJudge(k, arg); if (k instanceof ExpName) { luaK =
				 * _G.get(((ExpName) k).name); }
				 */
				tabletemp.put(luaK, expTemp);
			}
			 
			else _G.put(varTemp, expTemp);
		}
		if (m > n) {
			for (int i = 0; i < m - n; i++) {
				Exp varEle = varList.get(i);
				LuaValue varTemp = check(varEle, arg);
				_G.put(varTemp, LuaNil.nil);
			}
		}
		return new ArrayList<LuaValue>();
	}
	
	public LuaValue check(Exp exp, Object arg) throws Exception {
		if (exp instanceof ExpName) {
			return _G.get(((ExpName) exp).name);
		}else if (exp instanceof ExpTableLookup) {
			Exp t = ((ExpTableLookup)exp).table;
			
			//LuaValue luaT = expVisitJudge(t, arg);
			LuaValue luaT = check(t,arg);
			Exp k = ((ExpTableLookup)exp).key;
			LuaValue luaK = check(k, arg);
			LuaValue luaKret = (luaT instanceof LuaTable)? ((LuaTable) luaT).get(luaK) : luaT;
			return luaKret;
			
		}else return expVisitJudge(exp, arg);
	}
	
	@Override
	public Object visitStatBreak(StatBreak statBreak, Object arg, Object arg2) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitStatBreak(StatBreak statBreak, Object arg) throws Exception {
		List<LuaValue> result = new ArrayList<>();
		result.add(new LuaString("break"));
		return result;
	}
	
	@Override
	public Object visitStatDo(StatDo statDo, Object arg) throws Exception {
		Block b = statDo.b;
		List<LuaValue> result = (List<LuaValue>) b.visit(this, arg);
		if (_G.get("loop") == LuaNil.nil) {
			if (result != null && result.size() > 0 && result.get(0) instanceof LuaString && ((LuaString) result.get(0)).value.equals("break")) {
				result.remove(result.size() - 1);
			}
		}
		
		return result;
	}

	@Override
	public Object visitStatWhile(StatWhile statWhile, Object arg) throws Exception {
		Exp e = statWhile.e;
		Block b = statWhile.b;
		List<LuaValue> result = new ArrayList<>();
		if (_G.get("loop") == LuaNil.nil) {
			_G.put("loop", new LuaInt(1));
		}else {
			int i = ((LuaInt) _G.get("loop")).v;
			_G.put("loop", new LuaInt(i + 1));
		};
		while (((LuaBoolean) e.visit(this, arg)).value == true) {
			result = (List<LuaValue>) b.visit(this, arg);
			if (result != null && result.size() > 0 && result.get(0) instanceof LuaString && ((LuaString) result.get(0)).value.equals("break")) {
				if (_G.get("loop") != LuaNil.nil) {
					int i = ((LuaInt) _G.get("loop")).v;
					_G.put("loop", new LuaInt(i - 1));
				}else throw new StaticSemanticException(statWhile.firstToken, "while loop not complete") ;
				return new ArrayList<LuaValue>();
			}
		}
		if (_G.get("loop") != LuaNil.nil) {
			int i = ((LuaInt) _G.get("loop")).v;
			_G.put("loop", new LuaInt(i - 1));
		}else throw new StaticSemanticException(statWhile.firstToken, "while loop not complete") ;
		return result;
	}
	
	@Override
	public Object visitStatRepeat(StatRepeat statRepeat, Object arg) throws Exception {
		Block b = statRepeat.b;
		Exp e = statRepeat.e;
		List<LuaValue> result = new ArrayList<>();
		if (_G.get("loop") == LuaNil.nil) {
			_G.put("loop", new LuaInt(1));
		}else {
			int i = ((LuaInt) _G.get("loop")).v;
			_G.put("loop", new LuaInt(i + 1));
		};
		do {
			result = (List<LuaValue>) b.visit(this, arg);
			if (result != null && result.size() > 0 && result.get(0) instanceof LuaString && ((LuaString) result.get(0)).value.equals("break")) {
				if (_G.get("loop") != LuaNil.nil) {
					int i = ((LuaInt) _G.get("loop")).v;
					_G.put("loop", new LuaInt(i - 1));
				}else throw new StaticSemanticException(statRepeat.firstToken, "while loop not complete") ;
				return new ArrayList<LuaValue>();
			}
		}
		while (((LuaBoolean) e.visit(this, arg)).value == false);
		if (_G.get("loop") != LuaNil.nil) {
			int i = ((LuaInt) _G.get("loop")).v;
			_G.put("loop", new LuaInt(i - 1));
		}else throw new StaticSemanticException(statRepeat.firstToken, "repeat loop not complete") ;
		return result;
	}
	
	@Override
	public Object visitStatIf(StatIf statIf, Object arg) throws Exception {
		List<Exp> es = statIf.es;
		List<Block> bs = statIf.bs;
		//List<LuaValue> result = new ArrayList<>();
		for (int i = 0; i < es.size(); i++) {
			Exp element = es.get(i);
			LuaValue temp = (LuaValue)element.visit(this, arg);
			//if (element instanceof ExpName) temp = _G.get(((ExpName) element).name);
			temp = check(element, arg);
			if ((temp instanceof LuaBoolean && ((LuaBoolean) temp).value == false) || temp instanceof LuaNil) {
				temp = new LuaBoolean(false);
			}else temp = new LuaBoolean(true);
			if (((LuaBoolean) temp).value == true) {
				Block btemp = bs.get(i);
				List<LuaValue> result = (List<LuaValue>) btemp.visit(this, arg);
				if (_G.get("loop") == LuaNil.nil) {
					if (result != null && result.size() > 0 && result.get(0) instanceof LuaString && ((LuaString) result.get(0)).value.equals("break")) {
						result.remove(result.size() - 1);
					}
				}
				return result;
			}
		}
		if (es.size() < bs.size()) {
			Block btemp = bs.get(bs.size() - 1);
			List<LuaValue> result = (List<LuaValue>) btemp.visit(this, arg);
			if (_G.get("loop") == LuaNil.nil) {
				if (result != null && result.size() > 0 && result.get(0) instanceof LuaString && ((LuaString) result.get(0)).value.equals("break")) {
					result.remove(result.size() - 1);
				}
			}
			return result;
		}
		
		return new ArrayList<LuaValue>();
	}
	
	@Override
	public Object visitExpFunctionCall(ExpFunctionCall expFunctionCall, Object arg) throws Exception {
		throw new UnsupportedOperationException();
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
	public Object visitUnExp(ExpUnary unExp, Object arg) throws Exception {
		LuaValue input = check(unExp.e, arg);
		switch(unExp.op) {
		case KW_not: {
			if ((input instanceof LuaBoolean && ((LuaBoolean)input).value == false) || input instanceof LuaNil) return new LuaBoolean(true);
			else return new LuaBoolean(false);
		}
		case BIT_XOR: {
			if (!(input instanceof LuaInt) && !(input instanceof LuaString)) throw new TypeException(unExp.firstToken, "visitUnExpTypeError");
			int val;
			if (input instanceof LuaString) val = Integer.valueOf(((LuaString) input).value);
			else val = ((LuaInt) input).v;
			return new LuaInt(~val);
		}
		case OP_MINUS: {
			if (!(input instanceof LuaInt) && !(input instanceof LuaString)) throw new TypeException(unExp.firstToken, "visitUnExpTypeError");
			int val;
			if (input instanceof LuaString) val = Integer.valueOf(((LuaString) input).value);
			else val = ((LuaInt) input).v;
			val = -val;
			return new LuaInt(val);
		}
		case OP_HASH: {
			if (!(input instanceof LuaString)) throw new TypeException(unExp.firstToken, "visitUnExpTypeError");
			int val = ((LuaString) input).value.length();
			return new LuaInt(val);
		}
		default: {
			throw new StaticSemanticException(unExp.firstToken, "visitUnExpTypeError");
		}
		}
	}
	
	@Override
	public Object visitExpBin(ExpBinary expBin, Object arg) throws Exception {
		LuaValue val0; LuaValue val1; 
		/*
		 * if (expBin.e0 instanceof ExpName) val0 = _G.get((LuaValue)
		 * expBin.e0.visit(this, arg)); else val0 = (LuaValue) expBin.e0.visit(this,
		 * arg);
		 */
		val0 = check(expBin.e0, arg);
		val1 = check(expBin.e1, arg);
		switch(expBin.op) {
		case OP_PLUS: {
			if (!(val0 instanceof LuaInt) && !(val0 instanceof LuaString)) throw new TypeException("visitExpBinPlus");
			if (!(val1 instanceof LuaInt) && !(val1 instanceof LuaString)) throw new TypeException("visitExpBinPlus");
			if (val0 instanceof LuaString) val0 = new LuaInt(Integer.valueOf(((LuaString) val0).value));
			if (val1 instanceof LuaString) val1 = new LuaInt(Integer.valueOf(((LuaString) val1).value));
			int val = ((LuaInt)val0).v + ((LuaInt)val1).v;
			return new LuaInt(val);
		}
		case OP_MINUS: {
			if (!(val0 instanceof LuaInt) && !(val0 instanceof LuaString)) throw new TypeException("visitExpBinMinus");
			if (!(val1 instanceof LuaInt) && !(val1 instanceof LuaString)) throw new TypeException("visitExpBinMinus");
			if (val0 instanceof LuaString) val0 = new LuaInt(Integer.valueOf(((LuaString) val0).value));
			if (val1 instanceof LuaString) val1 = new LuaInt(Integer.valueOf(((LuaString) val1).value));
			int val = ((LuaInt)val0).v - ((LuaInt)val1).v;
			return new LuaInt(val);
		}
		case OP_TIMES: {
			if (!(val0 instanceof LuaInt) && !(val0 instanceof LuaString)) throw new TypeException("visitExpBinTimes");
			if (!(val1 instanceof LuaInt) && !(val1 instanceof LuaString)) throw new TypeException("visitExpBinTimes");
			if (val0 instanceof LuaString) val0 = new LuaInt(Integer.valueOf(((LuaString) val0).value));
			if (val1 instanceof LuaString) val1 = new LuaInt(Integer.valueOf(((LuaString) val1).value));
			int val = ((LuaInt)val0).v * ((LuaInt)val1).v;
			return new LuaInt(val);
		}
		case OP_DIV: {
			if (!(val0 instanceof LuaInt) && !(val0 instanceof LuaString)) throw new TypeException("visitExpBinDIV");
			if (!(val1 instanceof LuaInt) && !(val1 instanceof LuaString)) throw new TypeException("visitExpBinDIV");
			if (val0 instanceof LuaString) val0 = new LuaInt(Integer.valueOf(((LuaString) val0).value));
			if (val1 instanceof LuaString) val1 = new LuaInt(Integer.valueOf(((LuaString) val1).value));
			int val = ((LuaInt)val0).v / ((LuaInt)val1).v;
			return new LuaInt(val);
		}
		case OP_DIVDIV: {
			if (!(val0 instanceof LuaInt) && !(val0 instanceof LuaString)) throw new TypeException("visitExpBinDIVDIV");
			if (!(val1 instanceof LuaInt) && !(val1 instanceof LuaString)) throw new TypeException("visitExpBinDIVDIV");
			if (val0 instanceof LuaString) val0 = new LuaInt(Integer.valueOf(((LuaString) val0).value));
			if (val1 instanceof LuaString) val1 = new LuaInt(Integer.valueOf(((LuaString) val1).value));
			int val = Math.floorDiv(((LuaInt)val0).v, ((LuaInt)val1).v);
			return new LuaInt(val);
		}
		case OP_MOD: {
			if (!(val0 instanceof LuaInt) && !(val0 instanceof LuaString)) throw new TypeException("visitExpBinMOD");
			if (!(val1 instanceof LuaInt) && !(val1 instanceof LuaString)) throw new TypeException("visitExpBinMOD");
			if (val0 instanceof LuaString) val0 = new LuaInt(Integer.valueOf(((LuaString) val0).value));
			if (val1 instanceof LuaString) val1 = new LuaInt(Integer.valueOf(((LuaString) val1).value));
			int val = ((LuaInt)val0).v % ((LuaInt)val1).v;
			return new LuaInt(val);
		}
		case BIT_SHIFTL: {
			if (!(val0 instanceof LuaInt) && !(val0 instanceof LuaString)) throw new TypeException("visitExpBinSHIFTL");
			if (!(val1 instanceof LuaInt) && !(val1 instanceof LuaString)) throw new TypeException("visitExpBinSHIFTL");
			if (val0 instanceof LuaString) val0 = new LuaInt(Integer.valueOf(((LuaString) val0).value));
			if (val1 instanceof LuaString) val1 = new LuaInt(Integer.valueOf(((LuaString) val1).value));
			int val = ((LuaInt)val0).v << ((LuaInt)val1).v;
			return new LuaInt(val);
		}
		case BIT_SHIFTR: {
			if (!(val0 instanceof LuaInt) && !(val0 instanceof LuaString)) throw new TypeException("visitExpBinSHIFTR");
			if (!(val1 instanceof LuaInt) && !(val1 instanceof LuaString)) throw new TypeException("visitExpBinSHIFTR");
			if (val0 instanceof LuaString) val0 = new LuaInt(Integer.valueOf(((LuaString) val0).value));
			if (val1 instanceof LuaString) val1 = new LuaInt(Integer.valueOf(((LuaString) val1).value));
			int val = ((LuaInt)val0).v >> ((LuaInt)val1).v;
			return new LuaInt(val);
		}
		case BIT_AMP: {
			if (!(val0 instanceof LuaInt) && !(val0 instanceof LuaString)) throw new TypeException("visitExpBinAMP");
			if (!(val1 instanceof LuaInt) && !(val1 instanceof LuaString)) throw new TypeException("visitExpBinAMP");
			if (val0 instanceof LuaString) val0 = new LuaInt(Integer.valueOf(((LuaString) val0).value));
			if (val1 instanceof LuaString) val1 = new LuaInt(Integer.valueOf(((LuaString) val1).value));
			int val = ((LuaInt)val0).v & ((LuaInt)val1).v;
			return new LuaInt(val);
		}
		case BIT_OR: {
			if (!(val0 instanceof LuaInt) && !(val0 instanceof LuaString)) throw new TypeException("visitExpBinOR");
			if (!(val1 instanceof LuaInt) && !(val1 instanceof LuaString)) throw new TypeException("visitExpBinOR");
			if (val0 instanceof LuaString) val0 = new LuaInt(Integer.valueOf(((LuaString) val0).value));
			if (val1 instanceof LuaString) val1 = new LuaInt(Integer.valueOf(((LuaString) val1).value));
			int val = ((LuaInt)val0).v | ((LuaInt)val1).v;
			return new LuaInt(val);
		}
		case BIT_XOR: {
			if (!(val0 instanceof LuaInt) && !(val0 instanceof LuaString)) throw new TypeException("visitExpBinXOR");
			if (!(val1 instanceof LuaInt) && !(val1 instanceof LuaString)) throw new TypeException("visitExpBinXOR");
			if (val0 instanceof LuaString) val0 = new LuaInt(Integer.valueOf(((LuaString) val0).value));
			if (val1 instanceof LuaString) val1 = new LuaInt(Integer.valueOf(((LuaString) val1).value));
			int val = ((LuaInt)val0).v ^ ((LuaInt)val1).v;
			return new LuaInt(val);
		}
		case OP_POW: {
			if (!(val0 instanceof LuaInt) && !(val0 instanceof LuaString)) throw new TypeException("visitExpBinPOW");
			if (!(val1 instanceof LuaInt) && !(val1 instanceof LuaString)) throw new TypeException("visitExpBinPOW");
			if (val0 instanceof LuaString) val0 = new LuaInt(Integer.valueOf(((LuaString) val0).value));
			if (val1 instanceof LuaString) val1 = new LuaInt(Integer.valueOf(((LuaString) val1).value));
			int val = (int) Math.pow(((LuaInt)val0).v, ((LuaInt)val1).v);
			return new LuaInt(val);
		}
		case KW_and: {
			//if (val0 instanceof LuaString || val1 instanceof LuaString) return new LuaBoolean(false);
			if (val0 instanceof LuaBoolean && ((LuaBoolean)val0).value == false) return val0;
			if (val0 instanceof LuaNil) return val0;
			return val1;
		}
		case KW_or: {
			//if (val0 instanceof LuaString || val1 instanceof LuaString) return new LuaBoolean(false);
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
				throw new TypeException("visitExpBinLTBoolean");
			}else if (val0 instanceof LuaTable) {
				throw new TypeException("visitExpBinLTTABLE");
			}else if (val0 instanceof LuaString) {
				String s1 = ((LuaString) val0).value;
				String s2 = ((LuaString) val1).value;
				return s1.compareTo(s2) < 0? new LuaBoolean(true) : new LuaBoolean(false);
			}else if (val0 instanceof LuaInt) {
				if (((LuaInt)val0).v < ((LuaInt)val1).v) return new LuaBoolean(true);
				else return new LuaBoolean(false);
			}else {
				throw new TypeException("visitExpBinLTNIL");
			}
		}
		case REL_GT: {
			if (val0.getClass() != val1.getClass()) return new LuaBoolean(false);
			if (val0 instanceof LuaInt) {
				if (((LuaInt)val0).v > ((LuaInt)val1).v) return new LuaBoolean(true);
				else return new LuaBoolean(false);
			}else if (val0 instanceof LuaString) {
				String s1 = ((LuaString) val0).value;
				String s2 = ((LuaString) val1).value;
				return s1.compareTo(s2) > 0? new LuaBoolean(true) : new LuaBoolean(false);
			}else {
				throw new TypeException("visitExpBinGT");
			}
		}
		case REL_LE: {
			if (val0.getClass() != val1.getClass()) return new LuaBoolean(false);
			if (val0 instanceof LuaInt) {
				if (((LuaInt)val0).v <= ((LuaInt)val1).v) return new LuaBoolean(true);
				else return new LuaBoolean(false);
			}else if (val0 instanceof LuaString) {
				String s1 = ((LuaString) val0).value;
				String s2 = ((LuaString) val1).value;
				return s1.compareTo(s2) <= 0? new LuaBoolean(true) : new LuaBoolean(false);
			}else {
				throw new TypeException("visitExpBinLE");
			}
		}
		case REL_GE: {
			if (val0.getClass() != val1.getClass()) return new LuaBoolean(false);
			if (val0 instanceof LuaInt) {
				if (((LuaInt)val0).v >= ((LuaInt)val1).v) return new LuaBoolean(true);
				else return new LuaBoolean(false);
			}else if (val0 instanceof LuaString) {
				String s1 = ((LuaString) val0).value;
				String s2 = ((LuaString) val1).value;
				return s1.compareTo(s2) >= 0? new LuaBoolean(true) : new LuaBoolean(false);
			}else {
				throw new TypeException("visitExpBinGE");
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
	
	@Override
	public Object visitExpTableLookup(ExpTableLookup expTableLookup, Object arg) throws Exception {
		Exp t = expTableLookup.table;
		LuaValue luaT = expVisitJudge(t, arg);
		Exp k = expTableLookup.key;
		
		LuaValue luaK = check(k, arg);
		/*
		 * if (k instanceof ExpName) { luaK = _G.get(((ExpName) k).name); }
		 */
		if (_G.get(luaT) instanceof LuaTable) {
			LuaTable temp = (LuaTable)_G.get(luaT);
			return temp.get(luaK);
		}else {
			throw new TypeException("visitExpTableLookup");
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
				LuaValue key = check(k, arg);
				//if (_G.get(key) != LuaNil.nil) key = _G.get(key);
				//if (k instanceof ExpName) key = _G.get(((ExpName) k).name);
				LuaValue value = check(v, arg);
				//if (v instanceof ExpName) value = _G.get(((ExpName) v).name);
				res.put(key, value);
			}else if (element instanceof FieldNameKey) {
				Name n = ((FieldNameKey) element).name;
				Exp ex = ((FieldNameKey) element).exp;
				/*
				 * LuaValue name = new LuaString(n.name); if (_G.get(name) != LuaNil.nil) name =
				 * _G.get(name);
				 */
				LuaValue exp = check(ex, arg);
				//if (ex instanceof ExpName) exp = _G.get(((ExpName) ex).name);
				res.put(n.name, exp);
			}else if (element instanceof FieldImplicitKey) {
				Exp ex = ((FieldImplicitKey) element).exp;
				LuaValue exp = check(ex, arg);
				//if (ex instanceof ExpName) exp = _G.get(((ExpName) ex).name);
				res.putImplicit(exp);
			}else {
				throw new TypeException("visitExpTable");
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
		StaticAnalysis hg = new StaticAnalysis();
		chunk.visit(hg,null);	
		//Interpret the program and return values returned from chunk.visit
		List<LuaValue> vals = (List<LuaValue>) chunk.visit(this,_G);
		return vals;
	}
	


	

}
