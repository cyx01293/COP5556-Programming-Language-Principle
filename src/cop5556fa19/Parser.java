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
import cop5556fa19.AST.ExpInt;
import cop5556fa19.AST.ExpName;
import cop5556fa19.AST.ExpNil;
import cop5556fa19.AST.ExpString;
import cop5556fa19.AST.ExpTable;
import cop5556fa19.AST.ExpTrue;
import cop5556fa19.AST.ExpUnary;
import cop5556fa19.AST.ExpVarArgs;
import cop5556fa19.AST.Field;
import cop5556fa19.AST.FieldExpKey;
import cop5556fa19.AST.FieldImplicitKey;
import cop5556fa19.AST.FieldNameKey;
import cop5556fa19.AST.FuncBody;
import cop5556fa19.AST.Name;
import cop5556fa19.AST.ParList;
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

	Parser(Scanner s) throws Exception {
		this.scanner = s;
		t = scanner.getNext(); // establish invariant
	}
	Chunk parse() throws Exception {
		Token first = t;
		Block b = block();
		return new Chunk(first, b);
	}
	private Block block() {
		return new Block(null, null); // this is OK for Assignment 2
	}
	Exp exp() throws Exception {
		Token first = t;
		Exp e0 = andExp();
		while (isKind(KW_or)) {
			Token op = consume();
			Exp e1 = andExp();
			e0 = new ExpBinary(first, e0, op, e1);
		}
		if (t.kind != EOF)
			error(t, "tail problem");
		return e0;
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
			consume();
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
		Exp e0 = null;
		if (isKind(RCURLY)) {
			consume();
			return new ExpTable(first, null);
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
		if (isKind(LPAREN)) {
			match(LPAREN);
			e0 = andExp();
			match(RPAREN);
		} else if (isKind(NAME)) {
			e0 = new ExpName(first);
			consume();
		} 
		if (e0 == null) {
			throw new UnsupportedOperationException("prefixexp"); // I find this is a more useful placeholder than
																	// returning null.
		} else
			return e0;
	}

	private Exp functiondef() throws Exception {
		Token first = t;
		match(KW_function);
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
			Token temp = t;
			pl = new ParList(first, null, true);
			return pl;
		}
		List<Name> nl = null;
		if (isKind(NAME)) {
			nl = nameList();
		} else {
			return new ParList(first, null, false);
		}
		if (isKind(DOTDOTDOT)) {
			Token temp = t;
			match(DOTDOTDOT);
			pl = new ParList(first, nl, true);
		} else {
			pl = new ParList(first, nl, false);
		}
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
