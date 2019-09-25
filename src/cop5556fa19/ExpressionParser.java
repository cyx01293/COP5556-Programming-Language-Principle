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

public class ExpressionParser {
	
	@SuppressWarnings("serial")
	class SyntaxException extends Exception {
		Token t;
		
		public SyntaxException(Token t, String message) {
			super(t.line + ":" + t.pos + " " + message);
		}
	}
	
	final Scanner scanner;
	Token t;  //invariant:  this is the next token


	ExpressionParser(Scanner s) throws Exception {
		this.scanner = s;
		t = scanner.getNext(); //establish invariant
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

	private boolean isBinop () {
		if(isKind(OP_PLUS) || isKind(OP_MINUS) || isKind(OP_TIMES) || isKind(OP_DIV) || isKind(OP_DIVDIV) || isKind(OP_POW)
			|| isKind(OP_MOD) || isKind(BIT_AMP) || isKind(BIT_XOR) || isKind(BIT_OR) || isKind(BIT_SHIFTR) || isKind(BIT_SHIFTL) || 
			isKind(DOTDOT) || isKind(REL_LT) || isKind(REL_LE) || isKind(REL_GT) || isKind(REL_GE) || isKind(REL_EQEQ) || isKind(REL_NOTEQ)
			|| isKind(KW_and) || isKind(KW_or)) {
		return true;
		}else return false;
	}
	private boolean isUnop() {
		if (isKind(OP_MINUS) || isKind(KW_not) || isKind(OP_HASH) || isKind(BIT_XOR)) return true;
		else return false;
	}
	private Exp andExp() throws Exception{
		// TODO Auto-generated method stub
		Exp e0 = null;
		Exp e1 = null;
		Token first = t;
		while(isUnop()) {
			Kind kind = t.kind;
			match(OP_MINUS, KW_not, OP_HASH, BIT_XOR);
			e0 = andExp();
			consume();
			return e0 = new ExpUnary(first, kind, e0);
		}
		switch (first.kind) {
		case NAME: {	
			Token tk = t;
			e0 = prefixexp();
		}break;
		case INTLIT: {			
			Token tk = t;
			match(INTLIT);
			e0 = new ExpInt(tk);
		}break;
		case KW_true: {
			Token tk = t;
			match(KW_true);
			e0 = new ExpTrue(tk);
		}break;
		case KW_false: {	
			Token tk = t;
			match(KW_false);
			e0 = new ExpFalse(tk);
		}break;
		case KW_nil: {		
			Token tk = t;
			match(KW_nil);
			e0 = new ExpNil(tk);
		}break;
		case STRINGLIT: {
			Token tk = t;
			match(STRINGLIT);
			e0 = new ExpString(tk);
		}break;
		case DOTDOTDOT: {
			Token tk = t;
			match(DOTDOTDOT);
			e0 = new ExpVarArgs(tk);
		}break;
		case KW_function: {
			Token tk = t;
			match(KW_function);
			e0 = functiondef();
		}break;
		case LCURLY: {
			Token tk = t;
			match(LCURLY);
			e0 = tableconstructor();
		}break;
		default:
			
		}		
		while(isBinop())
		{
			Token op = t;
			match(OP_PLUS, OP_MINUS, OP_TIMES, OP_DIV, OP_DIVDIV, OP_POW, OP_MOD, BIT_AMP, BIT_XOR, BIT_OR, BIT_SHIFTR, BIT_SHIFTL, 
			DOTDOT, REL_LT, REL_LE, REL_GT, REL_GE, REL_EQEQ, REL_NOTEQ, KW_and, KW_or);
			e1 = andExp();
			e0 = new ExpBinary(first,e0,op,e1);
		}
		if (e0 == null) {
			throw new UnsupportedOperationException("andExp");  //I find this is a more useful placeholder than returning null.
		}else return e0;
	}
	private Exp tableconstructor() throws Exception {
		Token first = t;
		Exp e0 = null;
		List<Field> fl = filedlist();
		e0 = new ExpTable(first, fl);
		match(RPAREN);
		if (e0 == null) {
			throw new UnsupportedOperationException("funcdef");
		} else return e0;
	}
	private List<Field> fieldlist() throws Exception {
		Token first = t;
		List<Field> list = new ArrayList<>();
		
	}
	private List<Field> fieldList() throws Exception {
		Token first = t;
		List<Field> list = new ArrayList<>();
		Field f = null;
		Name n0 = new Name(first, first.text);
		list.add(n0);
		while (isKind(COLON)) {
			match(COLON);
			Token temp = t;
			match(NAME);
			n0 = new Name(first, temp.text);
			list.add(n0);
		}
		return list;
	}
	private Exp prefixexp() throws Exception {
		Token first = t;
		Exp e0 = null;
		if (isKind(NAME)) {
			match(NAME);
			e0 = new ExpName(first);
		} else if (isKind(LPAREN)) {
			match(LPAREN);
			e0 = andExp();
			match(RPAREN);
		}
		if (e0 == null) {
			throw new UnsupportedOperationException("prefixexp");  //I find this is a more useful placeholder than returning null.
		}else return e0;
	}
	private Exp functiondef() throws Exception {
		match(KW_function);
		Exp func = null;
		Token first = t;
		FuncBody body = funcbody();
		func = new ExpFunction(first, body);
		if (func == null) {
			throw new UnsupportedOperationException("funcdef");
		} else return func;
	}
	private FuncBody funcbody() throws Exception {
		FuncBody fb = null;
		Token first = t;
		match(LPAREN);
		ParList pl = parlist();
		match(RPAREN);
		Block b = block();
		match(KW_end);
		fb = new FuncBody(first, pl, b);
		if (fb == null) {
			throw new UnsupportedOperationException("funcbody");
		} else return fb;
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
		List<Name> nl = nameList();
		if (isKind(COLON)) {
			match(COLON);
			if (isKind(DOTDOTDOT)) {
				Token temp = t;
				match(DOTDOTDOT);
				pl = new ParList(first, nl, true);
			}
		}else {
			pl = new ParList(first, nl, false);
		}
		return pl;
	}
	private List<Name> nameList() throws Exception {
		Token first = t;
		List<Name> list = new ArrayList<>();
		match(NAME);
		Name n0 = new Name(first, first.text);
		list.add(n0);
		while (isKind(COLON)) {
			match(COLON);
			Token temp = t;
			match(NAME);
			n0 = new Name(first, temp.text);
			list.add(n0);
		}
		return list;
	}
	private Exp term() throws Exception{
		Exp e0 = null;
		Exp e1 = null;
		Token first = t;
		e0 = factor();
		while(isKind(OP_PLUS) || isKind(OP_MINUS) || isKind(KW_or))
		{
			Token op = t;
			match(OP_PLUS, OP_MINUS, KW_or);
			e1 = factor();
			e0 = new ExpBinary(first,e0,op,e1);
		}
		if (e0 == null) {
			throw new UnsupportedOperationException("term");  //I find this is a more useful placeholder than returning null.
		}else return e0;
	}
	private Exp factor() throws Exception{
		Kind kind = t.kind;
		Exp e0 = null;
		switch (kind) {
		case NAME: {			
			e0 = new ExpName(t);
			consume();
		}break;
		case INTLIT: {			
			e0 = new ExpInt(t);
			consume();
		}break;
		case KW_true: {
			e0 = new ExpTrue(t);
			consume();
		}break;
		case KW_false: {			
			e0 = new ExpFalse(t);
			consume();
		}break;
		case KW_nil: {			
			e0 = new ExpNil(t);
			consume();
		}break;
		default:
			//you will want to provide a more useful error message
			throw new UnsupportedOperationException("factor");
		}		
		return e0;
	}
	private Block block() {
		return new Block(null);  //this is OK for Assignment 2
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
