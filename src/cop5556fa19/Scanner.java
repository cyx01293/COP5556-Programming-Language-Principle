

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
		START, HAVE_EQ, IN_NUMLIT, IN_IDENT, HAVE_DIV, HAVE_XOR, HAVE_MINUS, HAVE_LT, HAVE_GT, HAVE_COLON, HAVE_DOT, HAVE_2DOTS
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
	
	public void getChar() throws IOException{
		ch = r.read();
		if (ch == '\n' || ch == '\r') {
			currentPos = 0;
			currentLine++;
			
		} else {
			currentPos++;
		}
	}
	public void skipWhiteSpace() throws Exception {
		while (ch == ' ' || ch == '\t' || ch == '\f') {
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
		StringBuilder sb;
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
			            else if (Character.isJavaIdentifierStart(ch)) {
			                 state = State.IN_IDENT; 
			                 sb = new StringBuilder();
			                 sb.append((char)ch);
			                 getChar();
			            } else {
			            	throw new LexicalException("illegal character " +(char)ch+" at position "+(pos + 1) + " spotted");
			            	}
			          	}break;
			    } // switch (ch)

			}
			case HAVE_MINUS: {
				if (ch == '-') {
					//state = State.comment.S;
				}
				
			}break;
			case HAVE_DIV: {
				if (ch == '/') {
					state = State.START;
					t = new Token(OP_DIVDIV, "//", pos, line);
					getChar();
				} else if (Character.isJavaIdentifierPart(ch)) {
					state = State.START;
					t = new Token(OP_DIV, "/", pos, line);
					getChar();
				} else {
					throw new LexicalException("illegal character " +(char)ch+" at position "+(pos + 1) + " spotted");
				}
			}break;
			case HAVE_XOR: {
				if (ch == '=') {
					state = State.START;
					t = new Token(REL_NOTEQ, "~=", pos, line);
					getChar();
				} else if (Character.isJavaIdentifierPart(ch)) {
					state = State.START;
					t = new Token(BIT_XOR, "~", pos, line);
					getChar();
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
				} else if (Character.isJavaIdentifierPart(ch)) {
					state = State.START;
					t = new Token(REL_LT, "<", pos, line);
					getChar();
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
				} else if (Character.isJavaIdentifierPart(ch)) {
					state = State.START;
					t = new Token(REL_GT, ">", pos, line);
					getChar();
				} else {
					throw new LexicalException("illegal character " +(char)ch+" at position "+(pos + 1) + " spotted");
				}
			}break;
			case HAVE_EQ: {
				if (ch == '=') {
					state = State.START;
					t = new Token(REL_EQEQ, "==", pos, line);
					getChar();
				} else if (Character.isJavaIdentifierPart(ch)) {
					state = State.START;
					t = new Token(ASSIGN, "=", pos, line);
					getChar();
				} else {
					throw new LexicalException("illegal character " +(char)ch+" at position "+(pos + 1) + " spotted");
				}
			}break;
			case HAVE_COLON: {
				if (ch == ':') {
					state = State.START;
					t = new Token(COLONCOLON, "::", pos, line);
					getChar();
				} else if (Character.isJavaIdentifierPart(ch)) {
					state = State.START;
					t = new Token(ASSIGN, ":", pos, line);
					getChar();
				} else {
					throw new LexicalException("illegal character " +(char)ch+" at position "+(pos + 1) + " spotted");
				}
			}break;
			case HAVE_DOT: {
				if (ch == '.') {
					state = State.HAVE_2DOTS;
					getChar();
				} else if (Character.isJavaIdentifierPart(ch)) {
					state = State.START;
					t = new Token(DOT, ".", pos, line);
					getChar();
				} else {
					throw new LexicalException("illegal character " +(char)ch+" at position "+(pos + 1) + " spotted");
				}
			}break;
			case HAVE_2DOTS: {
				if (ch == '.') {
					state = State.START;
					t = new Token(DOTDOTDOT, "...", pos, line);
					getChar();
				} else if (Character.isJavaIdentifierPart(ch)) {
					state = State.START;
					t = new Token(DOTDOT, "..", pos, line);
					getChar();
				} else {
					throw new LexicalException("illegal character " +(char)ch+" at position "+(pos + 1) + " spotted");
				}
			}break;
			case IN_NUMLIT:
			case IN_IDENT: {
			     /* if (Character.isJavaIdentifierPart(ch)) {
			            sb.append((char)ch);
			            getChar();
			      } else {*/
			                 //we are done building the ident.  Create Token
			                 //if we had keywords, we would check for that here
				   // t = new Token(Ident,sb.toString(), pos, line));
			      }
			break;

			}
		}
		return t;
		//if (r.read() == -1) { return new Token(EOF,"eof",0,0);}
			//throw new LexicalException("Useful error message");
		}

}
