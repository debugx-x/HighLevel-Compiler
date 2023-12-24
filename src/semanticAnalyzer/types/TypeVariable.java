package semanticAnalyzer.types;

public class TypeVariable implements Type{
	private String name;
	private Type typeConstraint;

	public TypeVariable(String name) { 
		this.setName(name); 
		this.setTypeConstraint(PrimitiveType.NO_TYPE);
	}
	
    ////////////////////////////////////////////////////////////////////
    // accessors

	public String getName() {
		return this.name;
	}

	private void setName(String name) {
		this.name = name;
	}

	public Type getTypeConstraint() {
		return typeConstraint;
	}

	private void setTypeConstraint(Type typeConstraint) {
		this.typeConstraint = typeConstraint;
	}
	
	////////////////////////////////////////
	// attributes
	public void reset() {
		if (typeConstraint instanceof TypeVariable) {
			((TypeVariable)typeConstraint).reset();
		}
		setTypeConstraint(PrimitiveType.NO_TYPE);
	}
	
	@Override
	public int getSize() {
		return 0;
	}

	@Override
	public String infoString() {
		return toString();
	}
	

	public String toString() {
		return "<" + getName() + ">";
	}
	
	public boolean equals(Type otherType) {
		if (otherType instanceof TypeVariable) {
			throw new RuntimeException("equals attempted on two types containing type variables."); 
		}
		if (this.getTypeConstraint() == PrimitiveType.NO_TYPE) { 
			setTypeConstraint(otherType);
			return true;
		}
		return this.getTypeConstraint().equals(otherType);
	}
}