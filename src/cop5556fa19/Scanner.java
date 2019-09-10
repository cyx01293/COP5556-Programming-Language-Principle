

/* *
 * Developed  for the class project in COP5556 Programming Language Principles 
 * at the University of Florida, Fall 2019.
 * 
 * This software is solely for the educational benefit of students 
 * enrolled in the course during the Fall 2019 semester.  
 * 
 * This software, and any software derived from it,  may not be shared with others or posted to public web sites or repositories,
 * either during the course or afterwards.
 * 
 *  @Beverly A. Sanders, 2019
 */
package cop5556fa19;


import static cop5556fa19.Token.Kind.*;


import java.io.IOException;
import java.io.Reader;

public class Scanner {
	
	Reader r;
	public enum State {
		START, HAVE_EQ, IN_NUMLIT, IN_IDENT, HAVE_DIV, HAVE_XOR, HAVE_MINUS, HAVE_LT, HAVE_GT, HAVE_COLON, HAVE_DOT, HAVE_2DOTS, 
		IN_COMMENT
	}
	
	public enum Kind {
		NAME,
		INTLIT,
		KW_and,
		KW_break,
		KW_do,
		KW_else,
		KW_elseif,
		KW_end,
		KW_false,
		KW_for,
		KW_function,
		KW_goto,
		KW_if,
		KW_in,
		KW_local,
		KW_nil,
		KW_not,
		KW_or,
		KW_repeat,
		KW_return,
		KW_then,
		KW_true,
		KW_until,
		KW_while,
		OP_PLUS, // +
		OP_MINUS, // -
		OP_TIMES, // *
		OP_DIV, // /
		OP_MOD, // %
		OP_POW, // ^
		OP_HASH, // #
		BIT_AMP, // &
		BIT_XOR, // ~
		BIT_OR,  //  |
		BIT_SHIFTL, // <<
		BIT_SHIFTR, //  >>
		OP_DIVDIV, // //
		REL_EQEQ,  // ==
		REL_NOTEQ, // ~=
		REL_LE, // <=
		REL_GE, // >=
		REL_LT, // <
		REL_GT, // >
		ASSIGN, // =
		LPAREN, 
		RPAREN,
		LCURLY,
		RCURLY,
		LSQUARE,
		RSQUARE,
		COLONCOLON, // ::
		SEMI,
		COLON,
		COMMA,
		DOT,   // .
		DOTDOT,  // ..
		DOTDOTDOT, // ...
		STRINGLIT, 
		EOF;
	}
	
	@SuppressWarnings("serial")
	public static class LexicalException extends Exception {	
		public LexicalException(String arg0) {
			super(arg0);
		}
	}
	
	public Scanner(Reader r) throws IOException {
		this.r = r;
		getChar();
	}
	int currentPos = -1;
	int currentLine = 0;
	int ch;
	boolean afterCR = false;
	public void getChar() throws IOException{
		ch = r.read();
		if (ch == '\n' && !afterCR) {
			currentPos = -1;
			currentLine++;
		} else if (ch == '\r') {
			afterCR = true;
			currentPos = -1;
			currentLine++;
		} else if (ch == '\n' && afterCR) {
			afterCR = false;
		} else {
			afterCR = false;
			currentPos++;
		}
	}
	public boolean isLineTerminator() throws Exception {
		if (ch == '\n' || ch == '\r') {
			return true;
		} else return false;
	}
	public boolean isOtherTokens() throws Exception {
		if (ch == '+' || ch == '-' || ch == '*' || ch == '/' || ch == '%' || ch == '^' || ch == '#' || ch == '&' || ch == '~'
		|| ch == '|' || ch == '<' || ch == '>' || ch == '=' || ch == '(' || ch == ')' || ch == '{' || ch == '}' || ch == '['
		|| ch == ']' || ch == ';' || ch == ':' || ch == ',' || ch == '.') {
			return true;
		} else return false;
	}
	public Kind isKeyWords(String s) throws Exception {
		if (s.equals("and")) return Kind.KW_and;
		else if (s.equals("break")) return Kind.KW_break;
		else if (s.equals("do")) return Kind.KW_do;
		else if (s.equals("else")) return Kind.KW_else;
		else if (s.equals("elseif")) return Kind.KW_elseif;
		else if (s.equals("end")) return Kind.KW_end;
		else if (s.equals("false")) return Kind.KW_false;
		else if (s.equals("for")) return Kind.KW_for;
		else if (s.equals("function")) return Kind.KW_function;
		else if (s.equals("goto")) return Kind.KW_goto;
		else if (s.equals("if")) return Kind.KW_if;
		else if (s.equals("in")) return Kind.KW_in;
		else if (s.equals("local")) return Kind.KW_local;
		else if (s.equals("nil")) return Kind.KW_nil;
		else if (s.equals("not")) return Kind.KW_not;
		else if (s.equals("or")) return Kind.KW_or;
		else if (s.equals("repeat")) return Kind.KW_repeat;
		else if (s.equals("return")) return Kind.KW_return;
		else if (s.equals("then")) return Kind.KW_then;
		else if (s.equals("true")) return Kind.KW_true;
		else if (s.equals("until")) return Kind.KW_until;
		else if (s.equals("while")) return Kind.KW_while;
		else return null;
	}
	public void skipWhiteSpace() throws Exception {
		while (ch == ' ' || ch == '\t' || ch == '\f' || isLineTerminator()) {
			getChar();
		}
	}

	/*
	 * public static class showException extends LexicalException { public
	 * showException(String message) {
	 * 
	 * } }
	 */
	public Token getNext() throws Exception {
		    //replace this code.  Just for illustration
		Token t = null;
		StringBuilder sb = new StringBuilder();;
		int pos = -1;
		int line = -1;
		State state = State.START;
		while (t == null) {
			switch(state) {
			case START: {
				// skip white space
				skipWhiteSpace();
			    pos = currentPos;
			    line = currentLine;
			    switch (ch) {
			    case '+': {t = new Token(OP_PLUS, "+", pos, line);getChar();}break;
			    case '-': {state = State.HAVE_MINUS;getChar();}break;
			    case '*': {t = new Token(OP_TIMES, "*", pos, line);getChar();}break;
			    case '/': {state = State.HAVE_DIV;getChar();}break;
			    case '%': {t = new Token(OP_MOD, "%", pos, line);getChar();}break;
			    case '^': {t = new Token(OP_POW, "^", pos, line);getChar();}break;
			    case '#': {t = new Token(OP_HASH, "#", pos, line);getChar();}break;
			    case '&': {t = new Token(BIT_AMP, "&", pos, line);getChar();}break;
			    case '~': {state = State.HAVE_XOR;getChar();}break;
			    case '|': {t = new Token(BIT_OR, "|", pos, line);getChar();}break;
			    case '<': {state = State.HAVE_LT;getChar();}break;
			    case '>': {state = State.HAVE_GT;getChar();}break;
			    case '=': {state = State.HAVE_EQ; getChar();}break;
			    case '(': {t = new Token(LPAREN, "(", pos, line);getChar();}break;
			    case ')': {t = new Token(RPAREN, ")", pos, line);getChar();}break;
			    case '{': {t = new Token(LCURLY, "{", pos, line);getChar();}break;
			    case '}': {t = new Token(RCURLY, "}", pos, line);getChar();}break;
			    case '[': {t = new Token(LSQUARE, "[", pos, line);getChar();}break;
			    case ']': {t = new Token(RSQUARE, "]", pos, line);getChar();}break;
			    case ';': {t = new Token(SEMI, ";", pos, line);getChar();}break;
			    case ':': {state = State.HAVE_COLON; getChar();}break;
			    case ',': {t = new Token(COMMA, ",", pos, line);getChar();}break;
			    case '.': {state = State.HAVE_DOT; getChar();}break;
			    //case '0': {t = new Token(NUM_LIT,"0",pos,line);getChar();}break;
			    case  -1: {t = new Token(EOF, "EOF", pos, line); break;}
			    default: {
			            if (Character.isDigit(ch)) {
			            	//state = State.IN_DIGIT; 		
			                sb = new StringBuilder();
			                sb.append((char)ch);
			                getChar();
			            } 
			            else if ((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z')) {
			                 state = State.IN_IDENT; 
			                 sb = new StringBuilder();
			                 sb.append((char)ch);
			                 getChar();
			            } else {
			            	throw new LexicalException("illegal character " +(char)ch+" at position "+(pos + 1) + " spotted");
			            	}
			          	}break;
			    } // switch (ch)

			}break;
			case IN_NUMLIT:break;
			case IN_IDENT: {
			      if (Character.isJavaIdentifierPart(ch)) {
			            sb.append((char)ch);
			            Kind temp = isKeyWords(sb.toString());
			            if (temp != null) {
			            	t = new Token(temp, sb.toString(), pos, line);
			            }
			            getChar();
			      } else {
			    	  state = State.START;
			    	  t = new Token(NAME,sb.toString(), pos, line);
			      }
			                 //we are done building the ident.  Create Token
			                 //if we had keywords, we would check for that here
				  
			      }	break;
			case HAVE_MINUS: {
				if (ch == '-') {
					state = State.IN_COMMENT;
					getChar();
				}else if (ch == -1 || Character.isJavaIdentifierPart(ch) || isOtherTokens()){
					state = State.START;
					t = new Token(OP_MINUS, "-", pos, line);
					//getChar();
				} else {
					throw new LexicalException("illegal character " +(char)ch+" at position "+(pos + 1) + " spotted");
				}
				
			}break;
			case IN_COMMENT: {
				if ((Character.isJavaIdentifierPart(ch) || isOtherTokens()) && !isLineTerminator()) {
					state = State.IN_COMMENT;
					pos++;
					getChar();
				} else if (isLineTerminator()) {
					state = State.START;
					getChar();
				} else if (ch == -1) {
					t = new Token(EOF, "EOF", pos, line);
				} else {
					throw new LexicalException("illegal character " +(char)ch+" at position "+(pos + 1) + " spotted");
				}
			}break;
			case HAVE_DIV: {
				if (ch == '/') {
					state = State.START;
					t = new Token(OP_DIVDIV, "//", pos, line);
					getChar();
				} else if (ch == -1 || Character.isJavaIdentifierPart(ch) || isOtherTokens()) {
					state = State.START;
					t = new Token(OP_DIV, "/", pos, line);
				} else {
					throw new LexicalException("illegal character " +(char)ch+" at position "+(pos + 1) + " spotted");
				}
			}break;
			case HAVE_XOR: {
				if (ch == '=') {
					state = State.START;
					t = new Token(REL_NOTEQ, "~=", pos, line);
					getChar();
				} else if (ch == -1 || Character.isJavaIdentifierPart(ch) || isOtherTokens()) {
					state = State.START;
					t = new Token(BIT_XOR, "~", pos, line);
				} else {
					throw new LexicalException("illegal character " +(char)ch+" at position "+(pos + 1) + " spotted");
				}
			}break;
			case HAVE_LT: {
				if (ch == '<') {
					state = State.START;
					t = new Token(BIT_SHIFTL, "<<", pos, line);
					getChar();
				} else if (ch == '=') {
					state = State.START;
					t = new Token(REL_LE, "<=", pos, line);
					getChar();
				} else if (ch == -1 || Character.isJavaIdentifierPart(ch) || isOtherTokens()) {
					state = State.START;
					t = new Token(REL_LT, "<", pos, line);
				} else {
					throw new LexicalException("illegal character " +(char)ch+" at position "+(pos + 1) + " spotted");
				}
			}break;
			case HAVE_GT: {
				if (ch == '>') {
					state = State.START;
					t = new Token(BIT_SHIFTR, ">>", pos, line);
					getChar();
				} else if (ch == '=') {
					state = State.START;
					t = new Token(REL_GE, ">=", pos, line);
					getChar();
				} else if (ch == -1 || Character.isJavaIdentifierPart(ch) || isOtherTokens()) {
					state = State.START;
					t = new Token(REL_GT, ">", pos, line);
				} else {
					throw new LexicalException("illegal character " +(char)ch+" at position "+(pos + 1) + " spotted");
				}
			}break;
			case HAVE_EQ: {
				if (ch == '=') {
					state = State.START;
					t = new Token(REL_EQEQ, "==", pos, line);
					getChar();
				} else if (ch == -1 || Character.isJavaIdentifierPart(ch) || isOtherTokens()) {
					state = State.START;
					t = new Token(ASSIGN, "=", pos, line);
				} else {
					throw new LexicalException("illegal character " +(char)ch+" at position "+(pos + 1) + " spotted");
				}
			}break;
			case HAVE_COLON: {
				if (ch == ':') {
					state = State.START;
					t = new Token(COLONCOLON, "::", pos, line);
					getChar();
				} else if (ch == -1 || Character.isJavaIdentifierPart(ch) || isOtherTokens()) {
					state = State.START;
					t = new Token(ASSIGN, ":", pos, line);
				} else {
					throw new LexicalException("illegal character " +(char)ch+" at position "+(pos + 1) + " spotted");
				}
			}break;
			case HAVE_DOT: {
				if (ch == '.') {
					state = State.HAVE_2DOTS;
					getChar();
				} else if (ch == -1 || Character.isJavaIdentifierPart(ch) || isOtherTokens()) {
					state = State.START;
					t = new Token(DOT, ".", pos, line);
				} else {
					throw new LexicalException("illegal character " +(char)ch+" at position "+(pos + 1) + " spotted");
				}
			}break;
			case HAVE_2DOTS: {
				if (ch == '.') {
					state = State.START;
					t = new Token(DOTDOTDOT, "...", pos, line);
					getChar();
				} else if (ch == -1 || Character.isJavaIdentifierPart(ch) || isOtherTokens()) {
					state = State.START;
					t = new Token(DOTDOT, "..", pos, line);
				} else {
					throw new LexicalException("illegal character " +(char)ch+" at position "+(pos + 1) + " spotted");
				}
			}break;
			
			default: {}break;
			}
		}
		return t;
		//if (r.read() == -1) { return new Token(EOF,"eof",0,0);}
			//throw new LexicalException("Useful error message");
		}

}
