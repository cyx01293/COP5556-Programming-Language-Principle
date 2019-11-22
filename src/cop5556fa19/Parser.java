/**
 * Developed  for the class project in COP5556 Programming Language Principles 
 * at the University of Florida, Fall 2019.
 * 
 * This software is solely for the educational benefit of students 
 * enrolled in the course during the Fall 2019 semester.  
 * 
 * This software, and any software derived from it,  may not be shared with others or posted to public web sites,
 * either during the course or afterwards.
 * 
 *  @Beverly A. Sanders, 2019
 */

package cop5556fa19;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cop5556fa19.AST.Block;
import cop5556fa19.AST.Chunk;
import cop5556fa19.AST.Exp;
import cop5556fa19.AST.ExpBinary;
import cop5556fa19.AST.ExpFalse;
import cop5556fa19.AST.ExpFunction;
import cop5556fa19.AST.ExpFunctionCall;
import cop5556fa19.AST.ExpInt;
import cop5556fa19.AST.ExpList;
import cop5556fa19.AST.ExpName;
import cop5556fa19.AST.ExpNil;
import cop5556fa19.AST.ExpString;
import cop5556fa19.AST.ExpTable;
import cop5556fa19.AST.ExpTableLookup;
import cop5556fa19.AST.ExpTrue;
import cop5556fa19.AST.ExpUnary;
import cop5556fa19.AST.ExpVarArgs;
import cop5556fa19.AST.Field;
import cop5556fa19.AST.FieldExpKey;
import cop5556fa19.AST.FieldImplicitKey;
import cop5556fa19.AST.FieldNameKey;
import cop5556fa19.AST.FuncBody;
import cop5556fa19.AST.FuncName;
import cop5556fa19.AST.Name;
import cop5556fa19.AST.ParList;
import cop5556fa19.AST.RetStat;
import cop5556fa19.AST.Stat;
import cop5556fa19.AST.StatAssign;
import cop5556fa19.AST.StatBreak;
import cop5556fa19.AST.StatDo;
import cop5556fa19.AST.StatFor;
import cop5556fa19.AST.StatForEach;
import cop5556fa19.AST.StatFunction;
import cop5556fa19.AST.StatGoto;
import cop5556fa19.AST.StatIf;
import cop5556fa19.AST.StatLabel;
import cop5556fa19.AST.StatLocalAssign;
import cop5556fa19.AST.StatLocalFunc;
import cop5556fa19.AST.StatRepeat;
import cop5556fa19.AST.StatWhile;
import cop5556fa19.AST.Var;
import cop5556fa19.Token.Kind;
import static cop5556fa19.Token.Kind.*;

public class Parser {

	@SuppressWarnings("serial")
	class SyntaxException extends Exception {
		Token t;

		public SyntaxException(Token t, String message) {
			super(t.line + ":" + t.pos + " " + message);
		}
	}

	final Scanner scanner;
	Token t; // invariant: this is the next token

	public Parser(Scanner s) throws Exception {
		this.scanner = s;
		t = scanner.getNext(); // establish invariant
	}
	public Chunk parse() throws Exception {
		Token first = t;
		Block b = block();
		if (t.kind != EOF)
			error(t, "tail problem");
		return new Chunk(first, b);
	}
	private Block block() throws Exception {
		Token first = t;
		List<Stat> list = new ArrayList<>();
		Block bl = null;
		RetStat rs = null;
		while (isStat()) {
			switch (t.kind) {
			case SEMI: {
				match(SEMI);
			}break;
			case NAME: {
				List<Exp> vr = varList();
				match(ASSIGN);
				List<Exp> el = explist();
				StatAssign sa = new StatAssign(first, vr, el);
				list.add(sa);
			}break;
			case LPAREN: {
				List<Exp> vr = varList();
				match(ASSIGN);
				List<Exp> el = explist();
				StatAssign sa = new StatAssign(first, vr, el);
				list.add(sa);
			}break;
			case COLONCOLON: {
				StatLabel sl = statlabel();
				list.add(sl);
			}break;
			case KW_break: {
				StatBreak sb = statbreak();
				list.add(sb);
			}break;
			case KW_goto: {
				StatGoto sgt = statgoto();
				list.add(sgt);
			}break;
			case KW_do: {
				StatDo sd = statdo();
				list.add(sd);
			}break;
			case KW_while: {
				StatWhile swh = statwhile();
				list.add(swh);
			}break;
			case KW_repeat: {
				StatRepeat sre = statrepeat();
				list.add(sre);
			}break;
			case KW_if: {
				StatIf si = statif();
				list.add(si);
			}break;
			case KW_for: {
				Stat sf = forjudge();
				list.add(sf);
			}break;
			case KW_function: {
				StatFunction sft = statfunction();
				list.add(sft);
			}break;
			case KW_local: {
				Stat slo = localjudge();
				list.add(slo);
			}break;
			default:
				// you will want to provide a more useful error message
				throw new SyntaxException(first, "block");
			}
 		}
		if (isKind(KW_return)) {
			match(KW_return);
			List<Exp> exlist = null;
			if (isKind(KW_nil) || isKind(KW_false) || isKind(KW_true) || isKind(INTLIT) || isKind(STRINGLIT) || isKind(DOTDOTDOT)
				|| isKind(KW_function) || isKind(NAME) || isKind(LPAREN) || isKind(LCURLY) || isUnop()) {
					exlist = explist();
			}
			if (isKind(SEMI)) {
				match(SEMI);
			}
			rs = new RetStat(first, exlist);
		}
		if (rs != null) list.add(rs);
		bl = new Block(first, list);
		return bl; // this is OK for Assignment 2
	}
	
	private List<Exp> varList() throws Exception {
		Token first = t;
		Exp v0 = null;
		Exp v1 = null;
		List<Exp> vList = new ArrayList<>();
		v0 = var();
		vList.add(v0);
		while (isKind(COMMA)) {
			consume();
			v1 = var();
			vList.add(v1);
		}
		return vList;
	}
	
	private Exp var() throws Exception {
		Token first = t;
		Exp etl = prefixexp();
		return etl;
	}
	
	private Stat localjudge() throws Exception {
		Token first = t;
		match(KW_local);
		if (isKind(KW_function)) {
			match(KW_function);
			Token temp = t;
			match(NAME);
			List<ExpName> eplist = new ArrayList<>();
			ExpName en0 = new ExpName(temp);
			eplist.add(en0);
			FuncName fn = new FuncName(first, eplist, null);
			FuncBody fb = funcbody();
			StatLocalFunc slf = new StatLocalFunc(first, fn, fb);
			return slf;
		}else if (isKind(NAME)) {
			List<ExpName> nlist = new ArrayList<>();
			Token temp = t;
			match(NAME);
			ExpName en0 = new ExpName(temp);
			nlist.add(en0);
			while (isKind(COMMA)) {
				match(COMMA);
				temp = t;
				match(NAME);
				en0 = new ExpName(temp);
				nlist.add(en0);
			}
			match(ASSIGN);
			List<Exp> elist = explist();
			StatLocalAssign sla = new StatLocalAssign(first, nlist, elist);
			return sla;
		}else {
			throw new SyntaxException(first, "localjudge");
		}
	}
	
	private StatFunction statfunction() throws Exception {
		Token first = t;
		match(KW_function);
		FuncName fn = funcname();
		FuncBody fb = funcbody();
		StatFunction sft = new StatFunction(first, fn, fb);
		return sft;
	}
	
	private FuncName funcname() throws Exception {
		Token first = t;
		Token temp = t;
		List<ExpName> eplist = new ArrayList<>();
		match(NAME);
		ExpName en0 = new ExpName(temp);
		ExpName en1 = null;
		eplist.add(en0);
		while (isKind(DOT)) {
			match(DOT);
			temp = t;
			match(NAME);
			en0 = new ExpName(temp);
			eplist.add(en0);
		}
		if (isKind(COLON)) {
			match(COLON);
			temp = t;
			match(NAME);
			en1 = new ExpName(temp);
		}
		FuncName fn = new FuncName(first, eplist, en1);
		return fn;
	}
	
	private Stat forjudge() throws Exception {
		Token first = t;
		match(KW_for);
		Token temp = t;
		match(NAME);
		if (isKind(ASSIGN)) {
			StatFor sfr = statfor(first, temp);
			return sfr;
		}else if (isKind(COMMA) || isKind(KW_in)) {
			StatForEach sfe = statforeach(first, temp);
			return sfe;
		}else {
			throw new SyntaxException(first, "forjudge");
		}
	}
	
	private StatFor statfor(Token first, Token temp) throws Exception {
		ExpName en0 = new ExpName(temp);
		match(ASSIGN);
		Exp e0 = andExp();
		match(COMMA);
		Exp e1 = andExp();
		Exp e2 = null;
		if (isKind(COMMA)) {
			match(COMMA);
			e2 = andExp();
		}
		match(KW_do);
		Block b0 = block();
		match(KW_end);
		StatFor sfr = new StatFor(first, en0, e0, e1, e2, b0);
		return sfr;
	}
	
	private StatForEach statforeach(Token first, Token temp) throws Exception {
		ExpName en0 = new ExpName(temp);
		List<ExpName> enlist = new ArrayList<>();
		enlist.add(en0);
		while (isKind(COMMA)) {
			match(COMMA);
			Token ntemp = t;
			match(NAME);
			en0 = new ExpName(ntemp);
			enlist.add(en0);
		}
		match(KW_in);
		List<Exp> eplist = explist();
		match(KW_do);
		Block b0 = block();
		match(KW_end);
		StatForEach sfe = new StatForEach(first, enlist, eplist, b0);
		return sfe;
	}
	
	private StatIf statif() throws Exception {
		Token first = t;
		match(KW_if);
		List<Exp> elist = new ArrayList<>();
		List<Block> blist = new ArrayList<>();
		Exp e0 = andExp();
		elist.add(e0);
		match(KW_then);
		Block b0 = block();
		blist.add(b0);
		while (isKind(KW_elseif)) {
			match(KW_elseif);
			e0 = andExp();
			elist.add(e0);
			match(KW_then);
			b0 = block();
			blist.add(b0);
		}
		if (isKind(KW_else)) {
			match(KW_else);
			b0 = block();
			blist.add(b0);
		}
		match(KW_end);
		StatIf si = new StatIf(first, elist, blist);
		return si;
	}
	
	private StatRepeat statrepeat() throws Exception {
		Token first = t;
		match(KW_repeat);
		Block b0 = block();
		match(KW_until);
		Exp e0 = andExp();
		StatRepeat sre = new StatRepeat(first, b0, e0);
		return sre;
	}
	
	private StatWhile statwhile() throws Exception {
		Token first = t;
		match(KW_while);
		Exp e0 = andExp();
		match(KW_do);
		Block b0 = block();
		match(KW_end);
		StatWhile swh = new StatWhile(first, e0, b0);
		return swh;
	}
	
	private StatDo statdo() throws Exception {
		Token first = t;
		match(KW_do);
		Block b0 = block();
		StatDo sd = new StatDo(first, b0);
		match(KW_end);
		return sd;
	}
	
	private StatBreak statbreak() throws Exception {
		Token first = t;
		StatBreak sb = new StatBreak(first);
		match(KW_break);
		return sb;
	}
	
	private StatGoto statgoto() throws Exception {
		Token first = t;
		match(KW_goto);
		Token temp = t;
		match(NAME);
		Name n0 = new Name(first, temp.text);
		StatGoto sgt = new StatGoto(first, n0);
		return sgt;
	}
	
	private StatLabel statlabel() throws Exception {
		Token first = t;
		match(COLONCOLON);
		Token temp = t;
		match(NAME);
		Name n0 = new Name(first, temp.text);
		match(COLONCOLON);
		return new StatLabel(first, n0, null, -1);
	}
	
	Exp exp() throws Exception {
		Token first = t;
		Exp e0 = andExp();
		while (isKind(KW_or)) {
			Token op = consume();
			Exp e1 = andExp();
			e0 = new ExpBinary(first, e0, op, e1);
		}
		
		return e0;
	}
	
	private boolean isStat() {
		if (isKind(SEMI) || isKind(NAME) || isKind(LPAREN) || isKind(COLONCOLON) || isKind(KW_break)
				|| isKind(KW_goto) || isKind(KW_do) || isKind(KW_while) || isKind(KW_repeat) || isKind(KW_if)
				|| isKind(KW_for) || isKind(KW_function) || isKind(KW_local)) {
			return true;
		} else
			return false;
	}
	
	private boolean isBinop() {
		if (isKind(OP_PLUS) || isKind(OP_MINUS) || isKind(OP_TIMES) || isKind(OP_DIV) || isKind(OP_DIVDIV)
				|| isKind(OP_MOD) || isKind(BIT_AMP) || isKind(BIT_XOR) || isKind(BIT_OR) || isKind(BIT_SHIFTR)
				|| isKind(BIT_SHIFTL) || isKind(REL_LT) || isKind(REL_LE) || isKind(REL_GT) || isKind(REL_GE)
				|| isKind(REL_EQEQ) || isKind(REL_NOTEQ) || isKind(KW_and) || isKind(KW_or) || isKind(OP_POW)
				|| isKind(DOTDOT)) {
			return true;
		} else
			return false;
	}

	private boolean isUnop() {
		if (isKind(OP_MINUS) || isKind(KW_not) || isKind(OP_HASH) || isKind(BIT_XOR))
			return true;
		else
			return false;
	}
	
	private List<Exp> explist() throws Exception {
		Exp e0 = null;
		Exp e1 = null;
		Token first = t;
		List<Exp> list = new ArrayList<>();
		e0 = andExp();
		list.add(e0);
		while (isKind(COMMA)) {
			consume();
			e1 = andExp();
			list.add(e1);
		}
		ExpList el = new ExpList(first, list);
		return list;
	}
	
	private Exp andExp() throws Exception {
		// TODO Auto-generated method stub
		Exp e0 = null;
		Exp e1 = null;
		Token first = t;
		/*
		 * while(isUnop() && first.kind != OP_POW) { match(OP_MINUS, KW_not, OP_HASH,
		 * BIT_XOR); e0 = level12();
		 * 
		 * e0 = new ExpUnary(first, first.kind, e0); } if (t.kind == EOF) { return e0; }
		 */
		e0 = level1();

		if (e0 == null) {
			throw new UnsupportedOperationException("level1"); // I find this is a more useful placeholder than
																// returning null.
		} else
			return e0;
	}

	private Exp level1() throws Exception {
		Exp e0 = null;
		Exp e1 = null;
		Token first = t;
		e0 = level2();
		while (isKind(KW_or)) {
			Token op = t;
			consume();
			e1 = level2();
			e0 = new ExpBinary(first, e0, op, e1);
		}
		if (e0 == null) {
			throw new UnsupportedOperationException("level1"); // I find this is a more useful placeholder than
																// returning null.
		} else
			return e0;
	}

	private Exp level2() throws Exception {
		Exp e0 = null;
		Exp e1 = null;
		Token first = t;
		e0 = level3();
		while (isKind(KW_and)) {
			Token op = t;
			consume();
			e1 = level3();
			e0 = new ExpBinary(first, e0, op, e1);
		}
		if (e0 == null) {
			throw new UnsupportedOperationException("level2"); // I find this is a more useful placeholder than
																// returning null.
		} else
			return e0;
	}

	private Exp level3() throws Exception {
		Exp e0 = null;
		Exp e1 = null;
		Token first = t;
		e0 = level4();
		while (isKind(REL_LT) || isKind(REL_GT) || isKind(REL_LE) || isKind(REL_GE) || isKind(REL_NOTEQ)
				|| isKind(REL_EQEQ)) {
			Token op = t;
			consume();
			e1 = level4();
			e0 = new ExpBinary(first, e0, op, e1);
		}
		if (e0 == null) {
			throw new UnsupportedOperationException("level3"); // I find this is a more useful placeholder than
																// returning null.
		} else
			return e0;
	}

	private Exp level4() throws Exception {
		Exp e0 = null;
		Exp e1 = null;
		Token first = t;
		e0 = level5();
		while (isKind(BIT_OR)) {
			Token op = t;
			consume();
			e1 = level5();
			e0 = new ExpBinary(first, e0, op, e1);
		}
		if (e0 == null) {
			throw new UnsupportedOperationException("level4"); // I find this is a more useful placeholder than
																// returning null.
		} else
			return e0;
	}

	private Exp level5() throws Exception {
		Exp e0 = null;
		Exp e1 = null;
		Token first = t;
		e0 = level6();
		while (isKind(BIT_XOR)) {
			Token op = t;
			consume();
			e1 = level6();
			e0 = new ExpBinary(first, e0, op, e1);
		}
		if (e0 == null) {
			throw new UnsupportedOperationException("level5"); // I find this is a more useful placeholder than
																// returning null.
		} else
			return e0;
	}

	private Exp level6() throws Exception {
		Exp e0 = null;
		Exp e1 = null;
		Token first = t;
		e0 = level7();
		while (isKind(BIT_AMP)) {
			Token op = t;
			consume();
			e1 = level7();
			e0 = new ExpBinary(first, e0, op, e1);
		}
		if (e0 == null) {
			throw new UnsupportedOperationException("level6"); // I find this is a more useful placeholder than
																// returning null.
		} else
			return e0;
	}

	private Exp level7() throws Exception {
		Exp e0 = null;
		Exp e1 = null;
		Token first = t;
		e0 = level8();
		while (isKind(BIT_SHIFTL) || isKind(BIT_SHIFTR)) {
			Token op = t;
			consume();
			e1 = level8();
			e0 = new ExpBinary(first, e0, op, e1);
		}
		if (e0 == null) {
			throw new UnsupportedOperationException("level7"); // I find this is a more useful placeholder than
																// returning null.
		} else
			return e0;
	}

	private Exp level8() throws Exception {
		Exp e0 = null;
		Exp e1 = null;
		Token first = t;
		e0 = level9();
		List<Exp> list = new ArrayList<>();
		list.add(e0);
		while (isKind(DOTDOT)) {
			Token op = t;
			consume();
			e1 = level9();
			list.add(e1);
		}
		int n = list.size();
		e0 = list.get(n - 1);
		for (int i = n - 2; i >= 0; i--) {
			e1 = list.get(i);
			e0 = new ExpBinary(first, e1, DOTDOT, e0);
		}
		if (e0 == null) {
			throw new UnsupportedOperationException("level8"); // I find this is a more useful placeholder than
																// returning null.
		} else
			return e0;

	}

	private Exp level9() throws Exception {
		Exp e0 = null;
		Exp e1 = null;
		Token first = t;
		e0 = level10();
		while (isKind(OP_PLUS) || isKind(OP_MINUS)) {
			Token op = t;
			consume();
			e1 = level10();
			e0 = new ExpBinary(first, e0, op, e1);
		}
		if (e0 == null) {
			throw new UnsupportedOperationException("level9"); // I find this is a more useful placeholder than
																// returning null.
		} else
			return e0;
	}

	private Exp level10() throws Exception {
		Exp e0 = null;
		Exp e1 = null;
		Token first = t;
		e0 = level11();
		while (isKind(OP_TIMES) || isKind(OP_DIV) || isKind(OP_DIVDIV) || isKind(OP_MOD)) {
			Token op = t;
			consume();
			e1 = level11();
			e0 = new ExpBinary(first, e0, op, e1);
		}
		if (e0 == null) {
			throw new UnsupportedOperationException("level10"); // I find this is a more useful placeholder than
																// returning null.
		} else
			return e0;
	}

	private Exp level11() throws Exception {
		Exp e0 = null;
		Exp e1 = null;
		Token first = t;
		if (!isUnop())
		{
			e0 = level12();
			return e0;
		}
		while (isUnop()) {
			Token op = t;
			consume();
			e1 = level11();
			// e0 = new ExpBinary(first, e0, op, e1);
			e0 = new ExpUnary(first, op.kind, e1);
			// return e0 = new ExpUnary(first, kind, e0);
		}
		if (e0 == null) {
			throw new UnsupportedOperationException("level11"); // I find this is a more useful placeholder than
																// returning null.
		} else
			return e0;
	}

	private Exp level12() throws Exception {
		Exp e0 = null;
		Exp e1 = null;
		Token first = t;
		e0 = level13();
		List<Exp> list = new ArrayList<>();
		list.add(e0);
		while (isKind(OP_POW)) {
			Token op = t;
			consume();
			e1 = level13();
			list.add(e1);
		}
		int n = list.size();
		e0 = list.get(n - 1);
		for (int i = n - 2; i >= 0; i--) {
			e1 = list.get(i);
			e0 = new ExpBinary(first, e1, OP_POW, e0);
		}
		if (e0 == null) {
			throw new UnsupportedOperationException("level12"); // I find this is a more useful placeholder than
																// returning null.
		} else
			return e0;
	}

	private Exp level13() throws Exception {
		Token first = t;
		Exp e0 = null;
		switch (first.kind) {
		case NAME: {
			e0 = prefixexp();
		}
			break;
		case LPAREN: {
			e0 = prefixexp();
		}
			break;
		case INTLIT: {
			e0 = new ExpInt(first);
			consume();
		}
			break;
		case KW_true: {
			e0 = new ExpTrue(first);
			consume();
		}
			break;
		case KW_false: {
			e0 = new ExpFalse(first);
			consume();
		}
			break;
		case KW_nil: {
			e0 = new ExpNil(first);
			consume();
		}
			break;
		case STRINGLIT: {
			e0 = new ExpString(first);
			consume();
		}
			break;
		case DOTDOTDOT: {
			e0 = new ExpVarArgs(first);
			consume();
		}
			break;
		case KW_function: {
			e0 = functiondef();
			//consume();
		}
			break;
		case LCURLY: {
			e0 = tableconstructor();
			
		}
			break;
		default:
			// you will want to provide a more useful error message
			throw new SyntaxException(t, "andExp");
		}
		return e0;
	}

	private Exp tableconstructor() throws Exception {
		Token first = t;
		match(LCURLY);
		Exp e0 = null;
		if (isKind(RCURLY)) {
			consume();
			return new ExpTable(first, new ArrayList<Field>());
		}
		List<Field> fl = fieldlist();
		e0 = new ExpTable(first, fl);
		match(RCURLY);
		if (e0 == null) {
			throw new UnsupportedOperationException("funcdef");
		} else
			return e0;
	}

	private List<Field> fieldlist() throws Exception {
		Token first = t;
		List<Field> res = null;
		List<Field> list = new ArrayList<>();
		Field f = field();
		list.add(f);
		while (isKind(COMMA) || isKind(SEMI)) {
			match(COMMA, SEMI);
			Token temp = t;
			if (temp.kind == RCURLY) break;
			f = field();
			list.add(f);
		}
		// res = new FieldList(first, list);
		return list;
	}

	private Field field() throws Exception {
		Token first = t;
		Field fd = null;
		if (isKind(LSQUARE)) {
			match(LSQUARE);
			Exp key = andExp();
			match(RSQUARE);
			match(ASSIGN);
			Exp value = andExp();
			fd = new FieldExpKey(first, key, value);
			/*
			 * } else if (isKind(NAME)) { Token n = t; Name n0 = new Name(first, n.text);
			 * match(NAME); match(ASSIGN); Exp exp = andExp(); fd = new FieldNameKey(first,
			 * n0, exp);
			 */
		} else {
			Token n = t;
			Exp store = andExp();
			
			if (n.kind == NAME && isKind(ASSIGN)) {
				match(ASSIGN);
				Exp exp = andExp();
				Name n0 = new Name(first, n.text);
				fd = new FieldNameKey(first, n0, exp);
				return fd;
			}else {
				
				fd = new FieldImplicitKey(first, store);
			}
			
		}
		
		  if (fd == null) { throw new UnsupportedOperationException("field"); } else
		 
			return fd;
	}

	/*
	 * private Exp prefixexp() throws Exception { Token first = t; Exp e0 = null; if
	 * (isKind(LPAREN)) { match(LPAREN); e0 = andExp(); match(RPAREN); } else if
	 * (isKind(NAME)) { e0 = new ExpName(first); consume(); } if (e0 == null) {
	 * throw new UnsupportedOperationException("prefixexp"); // I find this is a
	 * more useful placeholder than // returning null. } else return e0; }
	 */
	
	private Exp prefixexp() throws Exception {
		Token first = t;
		Exp e0 = null;
		Exp e1 = null;
		if (isKind(LPAREN)) {
			match(LPAREN);
			e0 = andExp();
			match(RPAREN);
			e1 = prefixexp_tail(e0);
			return e1;
		} else if (isKind(NAME)) {
			e0 = new ExpName(t);
			match(NAME);
			e1 = prefixexp_tail(e0);
			return e1;
		} else {
			throw new UnsupportedOperationException("prefixexp"); // I find this is a more useful placeholder than
																	// returning null.
		}
	}
	
	private Exp prefixexp_tail(Exp n0) throws Exception {
		Token first = t;
		Exp e0 = null;
		Exp e1 = null;
		List<Exp> el = null;
		if (isKind(LSQUARE)) {
			match(LSQUARE);
			e0 = andExp();
			match(RSQUARE);
			ExpTableLookup etl = new ExpTableLookup(first, n0, e0);
			e1 = prefixexp_tail(etl);
			return e1;
		}else if (isKind(DOT)) {
			match(DOT);
			Token temp = t;
			match(NAME);
			ExpString exn0 = new ExpString(temp);
			ExpTableLookup etl = new ExpTableLookup(first, n0, exn0);
			e1 = prefixexp_tail(etl);
			return e1;
		}else if (isKind(LPAREN) || isKind(LCURLY) || isKind(STRINGLIT)) {
			ExpFunctionCall efc = args(first, null, n0);
			e1 = prefixexp_tail(efc);
			return e1;
		}else if (isKind(COLON)) {
			consume();
			Token temp = t;
			match(NAME);
			ExpString exn0 = new ExpString(temp);
			ExpTableLookup etl = new ExpTableLookup(first, n0, exn0);
			//Name n1 = new Name(first, temp.text);
			ExpFunctionCall efc = args(first, etl, n0);
			e1 = prefixexp_tail(efc);
			return e1;
		}else return n0;
	}
	
	private ExpFunctionCall args(Token first, Exp v, Exp n0) throws Exception {
		Exp e0 = null;
		Exp e1 = null;
		List<Exp> el = null;
		
		if (isKind(LPAREN)) {
			match(LPAREN);
			if (isKind(RPAREN)) {
				match(RPAREN);
				if (v != null) el.add(0, v);
				ExpFunctionCall efc = new ExpFunctionCall(first, n0, new ArrayList<Exp>());
				return efc;
			} else {
				el = explist();
				if (v != null) el.add(0, v);
				match(RPAREN);
				ExpFunctionCall efc = new ExpFunctionCall(first, n0, el);
				return efc;
			}
			
		}else if (isKind(LCURLY)) {
			match(LCURLY);
			e0 = tableconstructor();
			el.add(e0);
			if (v != null) el.add(0, v);
			ExpFunctionCall efc = new ExpFunctionCall(first, n0, el);
			match(RCURLY);
			return efc;
		}else if (isKind(STRINGLIT)) {
			e0 = new ExpString(first);
			consume();
			el.add(e0);
			if (v != null) el.add(0, v);
			ExpFunctionCall efc = new ExpFunctionCall(first, n0, el);
			return efc;
		}else {
			throw new UnsupportedOperationException("args");
		}
	}

	private Exp functiondef() throws Exception {
		
		match(KW_function);
		Token first = t;
		Exp func = null;

		FuncBody body = funcbody();
		func = new ExpFunction(first, body);
		if (func == null) {
			throw new UnsupportedOperationException("funcdef");
		} else
			return func;
	}

	private FuncBody funcbody() throws Exception {
		Token first = t;
		FuncBody fb = null;
		match(LPAREN);
		ParList pl = parlist();
		match(RPAREN);
		Block b = block();
		match(KW_end);
		fb = new FuncBody(first, pl, b);
		if (fb == null) {
			throw new UnsupportedOperationException("funcbody");
		} else
			return fb;
	}

	private ParList parlist() throws Exception {
		Token first = t;
		ParList pl = null;
		if (isKind(DOTDOTDOT)) {
			match(DOTDOTDOT);
			List<Name> nl = new ArrayList<>();
			/*
			 * Name n0 = new Name(first, ""); nl.add(n0);
			 */
			Token temp = t;
			pl = new ParList(first, nl, true);
			return pl;
		}else if (isKind(NAME)){
			List<Name> nl = null;
			nl = nameList();
			if (isKind(DOTDOTDOT)) {
				Token temp = t;
				match(DOTDOTDOT);
				pl = new ParList(first, nl, true);
			} else {
				pl = new ParList(first, nl, false);
			}
		}else if (isKind(RPAREN)) {
			pl = new ParList(first, new ArrayList<Name>(), false);
		}else {
			throw new UnsupportedOperationException("parlist");
		}
		
		if (pl == null) {
			throw new UnsupportedOperationException("parlist");
		} else
			return pl;
		
	}

	private List<Name> nameList() throws Exception {
		Token first = t;
		Token temp = t;
		List<Name> list = new ArrayList<>();
		match(NAME);
		Name n0 = new Name(first, temp.text);
		list.add(n0);
		while (isKind(COMMA)) {
			match(COMMA);
			temp = t;
			if (isKind(DOTDOTDOT))
				return list;
			match(NAME);
			n0 = new Name(first, temp.text);
			list.add(n0);
		}
		return list;
	}

	

	

	protected boolean isKind(Kind kind) {
		return t.kind == kind;
	}

	protected boolean isKind(Kind... kinds) {
		for (Kind k : kinds) {
			if (k == t.kind)
				return true;
		}
		return false;
	}

	/**
	 * @param kind
	 * @return
	 * @throws Exception
	 */
	Token match(Kind kind) throws Exception {
		Token tmp = t;
		if (isKind(kind)) {
			consume();
			return tmp;
		}
		error(kind);
		return null; // unreachable
	}

	/**
	 * @param kind
	 * @return
	 * @throws Exception
	 */
	Token match(Kind... kinds) throws Exception {
		Token tmp = t;
		if (isKind(kinds)) {
			consume();
			return tmp;
		}
		StringBuilder sb = new StringBuilder();
		for (Kind kind1 : kinds) {
			sb.append(kind1).append(kind1).append(" ");
		}
		error(kinds);
		return null; // unreachable
	}

	Token consume() throws Exception {
		Token tmp = t;
		t = scanner.getNext();
		return tmp;
	}

	void error(Kind... expectedKinds) throws SyntaxException {
		String kinds = Arrays.toString(expectedKinds);
		String message;
		if (expectedKinds.length == 1) {
			message = "Expected " + kinds + " at " + t.line + ":" + t.pos;
		} else {
			message = "Expected one of" + kinds + " at " + t.line + ":" + t.pos;
		}
		throw new SyntaxException(t, message);
	}

	void error(Token t, String m) throws SyntaxException {
		String message = m + " at " + t.line + ":" + t.pos;
		throw new SyntaxException(t, message);
	}

}
