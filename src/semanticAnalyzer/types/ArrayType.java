package semanticAnalyzer.types;

import lexicalAnalyzer.Keyword;

public class ArrayType implements Type {
	private Type subtype;
	private String infoString;
	private int sizeInBytes = 4;
	/** returns the size of an instance of this type, in bytes.
	 * 
	 * @return number of bytes per instance
	 */
	
	public ArrayType() {
		this.infoString = "ARRAY[]";
	}

	public ArrayType(String infoString) {
		this.infoString = infoString;
	}

	public ArrayType(Type subtype) {
		this.subtype = subtype;
		this.infoString = "ARRAY[" + subtype.infoString() + "]";
	}

	public int getSize() {
		return sizeInBytes;
	}
	
	/** Yields a printable string for information about this type.
	 * use this rather than toString() if you want an abbreviated string.
	 * In particular, this yields an empty string for PrimitiveType.NO_TYPE.
	 * 
	 * @return string representation of type.
	 */
	
	public void setSubtype(Type type) {
		this.subtype = type;
		this.infoString = "ARRAY[" + subtype.infoString() + "]";
	}
	public Type getSubtype() {
		return subtype;
	}

	public String infoString() {
		return infoString;
	}
	
	public boolean equals(Type type2) {
		assert type2 instanceof ArrayType;

		Type subtype1 = this.getSubtype();
		Type subtype2 = ((ArrayType)type2).getSubtype();
		Keyword subtypeP1 = Keyword.FALSE;
		Keyword subtypeP2 = Keyword.TRUE;
		

		while ((subtype1 instanceof ArrayType) && (subtype2 instanceof ArrayType)) {
			subtype1 = ((ArrayType)subtype1).getSubtype();
			subtype2 = ((ArrayType)subtype2).getSubtype();
		}

		if (subtype1 instanceof PrimitiveType) {
			subtypeP1 = ((PrimitiveType) subtype1).getType();
		}

		if (subtype2 instanceof PrimitiveType) {
			subtypeP2 = ((PrimitiveType) subtype2).getType();
		}

		if (subtypeP1 == subtypeP2) {
			return true;
		} else {
			return false;
		}
	}
	
	
}
