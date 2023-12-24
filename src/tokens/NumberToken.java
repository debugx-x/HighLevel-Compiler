package tokens;

import inputHandler.Locator;
import logging.TanLogger;

public class NumberToken extends TokenImp {
	protected int value;
	
	protected NumberToken(Locator locator, String lexeme) {
		super(locator, lexeme);
	}
	protected void setValue(int value) {
		this.value = value;
	}
	public int getValue() {
		return value;
	}
	
	public static NumberToken make(Locator locator, String lexeme) {
		NumberToken result = new NumberToken(locator, lexeme);
		try {
			Integer.parseInt(lexeme);
			result.setValue(Integer.parseInt(lexeme));
		}
			catch (NumberFormatException e) {
				TanLogger log = TanLogger.getLogger("compiler.lexicalAnalyzer");
				log.severe( "Lexical error: " + lexeme + " Integer representation exceeded");
			}
		return result;
	}
	
	@Override
	protected String rawString() {
		return "number, " + value;
	}
	
}
