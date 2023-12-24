package lexicalAnalyzer;


import logging.TanLogger;
import inputHandler.InputHandler;
import inputHandler.LocatedChar;
import inputHandler.LocatedCharStream;
import inputHandler.PushbackCharStream;
import tokens.IdentifierToken;
import tokens.LextantToken;
import tokens.NullToken;
import tokens.NumberToken;
import tokens.StringToken;
import tokens.CharacterToken;
import tokens.FloatingLiteralToken;
import tokens.Token;

import static lexicalAnalyzer.PunctuatorScanningAids.*;

public class LexicalAnalyzer extends ScannerImp implements Scanner {
	private static final char decimal_point = '.';


	public static LexicalAnalyzer make(String filename) {
		InputHandler handler = InputHandler.fromFilename(filename);
		PushbackCharStream charStream = PushbackCharStream.make(handler);
		return new LexicalAnalyzer(charStream);
	}

	public LexicalAnalyzer(PushbackCharStream input) {
		super(input);
	}

	
	//////////////////////////////////////////////////////////////////////////////
	// Token-finding main dispatch	

	@Override
	protected Token findNextToken() {
		LocatedChar ch = nextNonWhitespaceChar();
		LocatedChar pk;
		// handle comments   # ... (# | \n)
		while(ch.isComment()) {
			ch = input.next();
			pk = input.peek();
			while(!(ch.isComment() || !(ch.isSlash() && pk.isN()))) {
				ch = input.next();
				pk = input.peek();
			}
			ch = nextNonWhitespaceChar();
		}
		
		if(ch.isDigit()) {
			return scanNumber(ch);
		}
		// handle strings
		else if(ch.isString()) { 
			return scanString(ch); 
		} 
		// handle characters else
		else if(ch.getCharacter() == '%' || ch.getCharacter() == '\'') { 
			return scanCharacter(ch); 
		}
		else if(ch.isLowerCase() || ch.isUpperCase() || ch.isUnderScore() || ch.isAtSign()) {
			return scanIdentifier(ch);
		}
		else if(isPunctuatorStart(ch)) {
			return PunctuatorScanner.scan(ch, input);
		}
		else if(isEndOfInput(ch)) {
			return NullToken.make(ch);
		}
		else {
			lexicalError(ch);
			return findNextToken();
		}
	}


	private LocatedChar nextNonWhitespaceChar() {
		LocatedChar ch = input.next();
		while(ch.isWhitespace()) {
			ch = input.next();
		}
		return ch;
	}
	
	
	//////////////////////////////////////////////////////////////////////////////
	// Integer lexical analysis	

	private Token scanNumber(LocatedChar firstChar) {
		StringBuffer buffer = new StringBuffer();
		buffer.append(firstChar.getCharacter());
		
		appendSubsequentDigits(buffer);						// Keep appending to buffer until not a digit
		
		if(input.peek().getCharacter() == decimal_point) {
			LocatedChar decimal = input.next();
			buffer.append(decimal.getCharacter());
			if(!input.peek().isDigit()) {
				lexicalError("Malformed floating-point literal", decimal);
				return findNextToken();
			}
			appendSubsequentDigits(buffer);					// After decimal point, keep appending number until end or E
			
			LocatedChar eE = input.next();
			if(eE.getCharacter() == 'e' || eE.getCharacter() == 'E') {
				buffer.append(eE.getCharacter());			// Append e/E
				if ( input.peek().getCharacter() != '+' && input.peek().getCharacter() != '-' ) {
					lexicalError("Malformed floating-point literal", eE);
					return findNextToken();
				}
				LocatedChar plusminus = input.next();
				buffer.append(plusminus.getCharacter());    // Append +/-
				if(!input.peek().isDigit()) {
					lexicalError("Malformed floating-point literal", decimal);
					return findNextToken();
				}
				appendSubsequentDigits(buffer);	
			}
			else {
				input.pushback(eE);
			}
			return FloatingLiteralToken.make(firstChar, buffer.toString());
		}
		return NumberToken.make(firstChar, buffer.toString());
	}
	

	private void appendSubsequentDigits(StringBuffer buffer) {
		LocatedChar c = input.next();
		while(c.isDigit()) {
			buffer.append(c.getCharacter());
			c = input.next();
		}
		input.pushback(c);
	}
	
	
	//////////////////////////////////////////////////////////////////////////////
	// String lexical analysis
	
	private Token scanString(LocatedChar firstChar) {
		StringBuffer buffer = new StringBuffer();
		appendSubsequentString(buffer);
		
		//buffer.append(firstChar.getCharacter());
		
		return StringToken.make(firstChar, buffer.toString());
	}
	
	private void appendSubsequentString(StringBuffer buffer) {
		LocatedChar c = input.next();
		
		// read until the next double quote or newline
		while (!( c.getCharacter() == '"' || c.getCharacter() == '\n' )){
			buffer.append(c.getCharacter());
			c = input.next();
		}
		
		// if the string is not closed by a double quote, throw an error
		if(!(c.getCharacter() == '"')) {
			lexicalError(c);
		}
	}
	
	//////////////////////////////////////////////////////////////////////////////
	// Character lexical analysis
	
	private Token scanCharacter(LocatedChar firstChar) {
		StringBuffer buffer = new StringBuffer();
//		buffer.append(firstChar.getCharacter());
//		if (appendSubsequentCharacter(buffer)) {
//			return CharacterToken.make(firstChar.getLocation(), buffer.toString());
//		} 
//		else {
//			return findNextToken();
//		}
		LocatedChar c = input.next();
		LocatedChar p = input.peek();
		int first, second, third;
		int total = 0;
		if(firstChar.getCharacter() == '\'') {
			if(c.isASCII() && p.getCharacter() == '\'') {
				buffer.append(c.getCharacter());
				input.next();
			}
			else {
				if(!c.isASCII()) {
					lexicalError(c);
				}
				else if(p.getCharacter() != '\'') {
					lexicalError(p);
				}
			return findNextToken();
			}
		}
		if(firstChar.getCharacter() == '%') {
			StringBuffer wrong_buffer = new StringBuffer();
			wrong_buffer.append(firstChar.getCharacter());
			if(c.isDigit() && Integer.parseInt(String.valueOf(c.getCharacter())) < 8) {	// first octal
				first = Integer.parseInt(String.valueOf(c.getCharacter()));
				wrong_buffer.append(c.getCharacter());
				c = input.next();
				if(c.isDigit() && Integer.parseInt(String.valueOf(c.getCharacter())) < 8) { // second octal
					second = Integer.parseInt(String.valueOf(c.getCharacter()));
					wrong_buffer.append(c.getCharacter());
					c = input.next();
					if(c.isDigit() && Integer.parseInt(String.valueOf(c.getCharacter())) < 8) { // third octal
						third = Integer.parseInt(String.valueOf(c.getCharacter()));
						wrong_buffer.append(c.getCharacter());
						total = (8*8*first) + (8*second) + third;
					}
					else {
						total = (8*first) + second;
						input.pushback(c);
					}
				}
				else {
					total = first;
					input.pushback(c);
				}
			}
			
			if(total >= 0 && total <= 127) {
				buffer.append((char)total);
			}
			else {
				lexicalError(wrong_buffer.toString() + " Character representation exceeded ", firstChar);
				return findNextToken();
			}
		}
		return CharacterToken.make(firstChar.getLocation(), buffer.toString());
	}
	
//	private boolean appendSubsequentCharacter(StringBuffer buffer) {
//		LocatedChar c = input.next();
//		while(c.isDigit()) {
//			buffer.append(c.getCharacter());
//			c = input.next();
//		}
//		input.pushback(c);
//		
//		if(c.isASCII()){
//			buffer.append(c.getCharacter());
//			c = input.peek();
//		
//			if(c.getCharacter() == '%') {
//				buffer.append(input.next().getCharacter());
//				return true;
//			}
//		}
//	
//		lexicalError(c);
//		return false;
//	}
	
	//////////////////////////////////////////////////////////////////////////////
	// Identifier and keyword lexical analysis	

	private Token scanIdentifier(LocatedChar firstChar) {
		StringBuffer buffer = new StringBuffer();
		buffer.append(firstChar.getCharacter());
		appendSubsequentLowercase(buffer);

		String lexeme = buffer.toString();
		if(Keyword.isAKeyword(lexeme)) {
			return LextantToken.make(firstChar, lexeme, Keyword.forLexeme(lexeme));
		}
		else {
			return IdentifierToken.make(firstChar, lexeme);
		}
	}
	private void appendSubsequentLowercase(StringBuffer buffer) {
		LocatedChar c = input.next();
		while(c.isLowerCase() || c.isUpperCase() || c.isUnderScore() || c.isAtSign() || c.isDigit()) {
			buffer.append(c.getCharacter());
			c = input.next();
		}
		input.pushback(c);
	}
	
	
	//////////////////////////////////////////////////////////////////////////////
	// Punctuator lexical analysis	
	// old method left in to show a simple scanning method.
	// current method is the algorithm object PunctuatorScanner.java

	@SuppressWarnings("unused")
	private Token oldScanPunctuator(LocatedChar ch) {
		
		switch(ch.getCharacter()) {
		case '*':
			return LextantToken.make(ch, "*", Punctuator.MULTIPLY);
		case '+':
			return LextantToken.make(ch, "+", Punctuator.ADD);
		case '>':
			return LextantToken.make(ch, ">", Punctuator.GREATER);
		case ':':
			if(ch.getCharacter()=='=') {
				return LextantToken.make(ch, ":=", Punctuator.ASSIGN);
			}
			else {
				lexicalError(ch);
				return(NullToken.make(ch));
			}
		case ',':
			return LextantToken.make(ch, ",", Punctuator.PRINT_SEPARATOR);
		case ';':
			return LextantToken.make(ch, ";", Punctuator.TERMINATOR);
		default:
			lexicalError(ch);
			return(NullToken.make(ch));
		}
	}

	

	//////////////////////////////////////////////////////////////////////////////
	// Character-classification routines specific to tan scanning.	

	private boolean isPunctuatorStart(LocatedChar lc) {
		char c = lc.getCharacter();
		return isPunctuatorStartingCharacter(c);
	}

	private boolean isEndOfInput(LocatedChar lc) {
		return lc == LocatedCharStream.FLAG_END_OF_INPUT;
	}
	
	
	//////////////////////////////////////////////////////////////////////////////
	// Error-reporting	
	private void lexicalError(LocatedChar ch) {
		TanLogger log = TanLogger.getLogger("compiler.lexicalAnalyzer");
		log.severe("Lexical error: invalid character " + ch);
	}
	
	private void lexicalError(String errorMsg, LocatedChar decimal) {
		TanLogger log = TanLogger.getLogger("compiler.lexicalAnalyzer");
		log.severe( "Lexical error: " + errorMsg + "at" + decimal.getLocation());
	}

	
}
