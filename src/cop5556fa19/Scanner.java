

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
		START, HAVE_EQ, IN_NUMLIT, IN_IDENT
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
			    case '*': {t = new Token(OP_TIMES, "*", pos, line);getChar();}break;        
			    case '=': {state = State.HAVE_EQ; getChar();}break;
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
			            } 
			            else {
			            	throw new LexicalException("illegal character " +(char)ch+" at position "+(pos + 1) + " spotted");
			            	}
			          	}break;
			    } // switch (ch)

			}
			case HAVE_EQ:
			case IN_NUMLIT:
			case IN_IDENT:
			}
		}
		return t;
		//if (r.read() == -1) { return new Token(EOF,"eof",0,0);}
			//throw new LexicalException("Useful error message");
		}

}
