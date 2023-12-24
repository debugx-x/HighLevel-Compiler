package semanticAnalyzer.types;

import lexicalAnalyzer.Keyword;
import tokens.Token;

public enum PrimitiveType implements Type {
	BOOLEAN(1, Keyword.BOOL),
	CHARACTER(1, Keyword.CHAR),
	INTEGER(4, Keyword.INT),
	STRING(4, Keyword.STRING),
	ARRAY(4, Keyword.ARRAY),
	FLOAT(8, Keyword.FLOAT),
	VOID(0, Keyword.VOID),
	ERROR(0, Keyword.NULL_KEYWORD), // use as a value when a syntax error has occurred
	NO_TYPE(0, ""); // use as a value when no type has been assigned.

	private int sizeInBytes;
	private String infoString;
	private Keyword type;

	private PrimitiveType(int size, Keyword type) {
		this.sizeInBytes = size;
		this.infoString = toString();
		this.type = type;
	}

	private PrimitiveType(int size, String infoString) {
		this.sizeInBytes = size;
		this.infoString = infoString;
	}
	

	public int getSize() {
		return sizeInBytes;
	}
	
	public Keyword getType() {
		return type;
	}

	public String infoString() {
		return infoString;
	}
	

	public static PrimitiveType fromToken(Token token) {

		for (PrimitiveType type : values()) {
			if (token.isLextant(type.type)) {
				return type;
			}
		}
		return ERROR;
	}
}
