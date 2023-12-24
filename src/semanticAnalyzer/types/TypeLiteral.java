package semanticAnalyzer.types;

import static semanticAnalyzer.types.PrimitiveType.*;

public class TypeLiteral implements Type {
    private Type literaltype;
    public static final TypeLiteral TYPE_INTEGER = new TypeLiteral(INTEGER);
    public static final TypeLiteral TYPE_FLOATING = new TypeLiteral(FLOAT);
    public static final TypeLiteral TYPE_CHARACTER = new TypeLiteral(CHARACTER);
    public static final TypeLiteral TYPE_BOOLEAN = new TypeLiteral(BOOLEAN);
    public static final TypeLiteral TYPE_STRING = new TypeLiteral(STRING);
    public static final TypeLiteral TYPE_VOID = new TypeLiteral(VOID);
    
    public TypeLiteral(Type literaltype) {
        this.literaltype = literaltype;
    }

    public Type getLiteraltype() {
        return literaltype;
    }

    public void setLiteraltype(Type literaltype) {
        this.literaltype = literaltype;
    }

    @Override
    public int getSize() {
        return literaltype.getSize();
    }

    @Override
    public String infoString() {
        return toString();
    }

    @Override
    public String toString() {
        return "<TYPE " + literaltype.toString() + ">";
    }

    public boolean equals(Type othertype) {
        if (othertype instanceof TypeLiteral) {
            TypeLiteral otherTypeLiteral = (TypeLiteral) othertype;
            return literaltype.equals(otherTypeLiteral.getLiteraltype());
        }
        return false;
    }
}
