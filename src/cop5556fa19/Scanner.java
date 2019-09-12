

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

import static cop5556fa19.Token.Kind;

public class Scanner {
	
	Reader r;
	public enum State {
		START, HAVE_EQ, IN_NUMLIT, IN_IDENT, HAVE_DIV, HAVE_XOR, HAVE_MINUS, HAVE_LT, HAVE_GT, HAVE_COLON, HAVE_DOT, HAVE_2DOTS, 
		IN_COMMENT, IN_DIGIT, IN_STRING, IN_DQSTRING, IN_SQSTRING
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
	public Kind isKeyWords(StringBuilder sb) throws Exception {
		String s = sb.toString();
		if (s.equals("and")) return KW_and;
		else if (s.equals("break")) return KW_break;
		else if (s.equals("do")) return KW_do;
		else if (s.equals("else")) return KW_else;
		else if (s.equals("elseif")) return KW_elseif;
		else if (s.equals("end")) return KW_end;
		else if (s.equals("false")) return KW_false;
		else if (s.equals("for")) return KW_for;
		else if (s.equals("function")) return KW_function;
		else if (s.equals("goto")) return KW_goto;
		else if (s.equals("if")) return KW_if;
		else if (s.equals("in")) return KW_in;
		else if (s.equals("local")) return KW_local;
		else if (s.equals("nil")) return KW_nil;
		else if (s.equals("not")) return KW_not;
		else if (s.equals("or")) return KW_or;
		else if (s.equals("repeat")) return KW_repeat;
		else if (s.equals("return")) return KW_return;
		else if (s.equals("then")) return KW_then;
		else if (s.equals("true")) return KW_true;
		else if (s.equals("until")) return KW_until;
		else if (s.equals("while")) return KW_while;
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
			    case '0': {t = new Token(INTLIT,"0",pos,line);getChar();}break;
			    case  -1: {t = new Token(EOF, "EOF", pos, line); break;}
			    default: {
			            if (Character.isDigit(ch)) {
			            	state = State.IN_DIGIT; 		
			                sb = new StringBuilder();
			                sb.append((char)ch);
			                getChar();
			            } 
			            else if ((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z')) {
			                 state = State.IN_IDENT; 
			                 sb = new StringBuilder();
			                 sb.append((char)ch);
			                 getChar();
			            } else if (ch == '\"' || ch == '\''){
			            	sb = new StringBuilder();
			            	if (ch == '\"') {
			            		state = State.IN_DQSTRING;
			            		sb.append((char)ch);
			            	}else {
			            		state = State.IN_SQSTRING;
			            		sb.append((char)ch);
			            	}
			            	getChar();
			            } else {
			            	throw new LexicalException("illegal character " +(char)ch+" at position "+(pos + 1) + ", line "+(line + 1));
			            	}
			          	}break;
			    } // switch (ch)

			}break;
			case IN_DQSTRING:{
				if (ch == '\"') {
					state = State.START;
					sb.append((char)ch);
					t = new Token(STRINGLIT, sb.toString(), pos, line);
					getChar();
				} else if (ch >= 0 && ch <= 127) {
					sb.append((char)ch);
					getChar();
				} else if (ch == -1){
					throw new LexicalException("String is not completed");
				} else {
					throw new LexicalException("illegal character " +(char)ch+" at position "+(pos + 1) + ", line "+(line + 1));
				}
			}break;
			case IN_SQSTRING:{
				if (ch == '\'') {
					state = State.START;
					sb.append((char)ch);
					t = new Token(STRINGLIT, sb.toString(), pos, line);
					getChar();
				} else if (ch == '\"') {
					throw new LexicalException("Double quote mark at position "+(pos + 1)+", line "+(line + 1)+" cannot be inside a pair of single quote marks");
				} else if (ch >= 0 && ch <= 127) {
					sb.append((char)ch);
					getChar();
				} else if (ch == -1) {
					throw new LexicalException("String is not completed");
				} else {
					throw new LexicalException("illegal character " +(char)ch+" at position "+(pos + 1) + ", line "+(line + 1));
				}
			}break;
			case IN_DIGIT:{
				if (Character.isDigit(ch)) {
					sb.append((char)ch);
					getChar();
				} else {
					state = State.START;
					t = new Token(INTLIT, sb.toString(), pos, line);
				}
			}break;
			case IN_IDENT: {
			      if (Character.isJavaIdentifierPart(ch)) {
			            sb.append((char)ch);
			            Kind temp = isKeyWords(sb);
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
					pos++;
					getChar();
				}else {
					state = State.START;
					t = new Token(OP_MINUS, "-", pos, line);
					//getChar();
				}
				
			}break;
			case IN_COMMENT: {
				if ((ch >= 0 && ch <= 127) && !isLineTerminator()) {
					state = State.IN_COMMENT;
					pos++;
					getChar();
				} else if (isLineTerminator()) {
					state = State.START;
					getChar();
				} else if (ch == -1) {
					t = new Token(EOF, "EOF", pos, line);
				} else {
					throw new LexicalException("illegal character " +(char)ch+" at position "+(pos + 1) + ", line "+(line + 1));
				}
			}break;
			case HAVE_DIV: {
				if (ch == '/') {
					state = State.START;
					t = new Token(OP_DIVDIV, "//", pos, line);
					getChar();
				} else {
					state = State.START;
					t = new Token(OP_DIV, "/", pos, line);
				} 
			}break;
			case HAVE_XOR: {
				if (ch == '=') {
					state = State.START;
					t = new Token(REL_NOTEQ, "~=", pos, line);
					getChar();
				} else {
					state = State.START;
					t = new Token(BIT_XOR, "~", pos, line);
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
				} else {
					state = State.START;
					t = new Token(REL_LT, "<", pos, line);
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
				} else {
					state = State.START;
					t = new Token(REL_GT, ">", pos, line);
				}
			}break;
			case HAVE_EQ: {
				if (ch == '=') {
					state = State.START;
					t = new Token(REL_EQEQ, "==", pos, line);
					getChar();
				} else {
					state = State.START;
					t = new Token(ASSIGN, "=", pos, line);
				}
			}break;
			case HAVE_COLON: {
				if (ch == ':') {
					state = State.START;
					t = new Token(COLONCOLON, "::", pos, line);
					getChar();
				} else {
					state = State.START;
					t = new Token(ASSIGN, ":", pos, line);
				}
			}break;
			case HAVE_DOT: {
				if (ch == '.') {
					state = State.HAVE_2DOTS;
					getChar();
				} else {
					state = State.START;
					t = new Token(DOT, ".", pos, line);
				}
			}break;
			case HAVE_2DOTS: {
				if (ch == '.') {
					state = State.START;
					t = new Token(DOTDOTDOT, "...", pos, line);
					getChar();
				} else {
					state = State.START;
					t = new Token(DOTDOT, "..", pos, line);
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
