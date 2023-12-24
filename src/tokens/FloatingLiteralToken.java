package tokens;

import inputHandler.Locator;
import logging.TanLogger;

public class FloatingLiteralToken extends TokenImp {
	protected double value;
	
	protected FloatingLiteralToken(Locator locator, String lexeme) {
		super(locator, lexeme);
	}
	protected void setValue(double value) {
		this.value = value;
	}
	public double getValue() {
		return value;
	}
	
	public static FloatingLiteralToken make(Locator locator, String lexeme) {
		FloatingLiteralToken result = new FloatingLiteralToken(locator, lexeme);
		if( Double.parseDouble(lexeme) == Double.POSITIVE_INFINITY || Double.parseDouble(lexeme) == Double.NEGATIVE_INFINITY){
			TanLogger log = TanLogger.getLogger("compiler.lexicalAnalyzer");
			log.severe( "Lexical error: " + lexeme + " Float representation exceeded");
		}
		else {
			result.setValue(Double.parseDouble(lexeme));
		}
		return result;
	}
	
	@Override
	protected String rawString() {
		return "number, " + value;
	}
}
