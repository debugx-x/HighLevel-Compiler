package parseTree.nodeTypes;

import parseTree.ParseNode;
import parseTree.ParseNodeVisitor;
import semanticAnalyzer.types.*;
import tokens.Token;

public class FuncTypeNode extends ParseNode {

    public FuncTypeNode(Token token) {
        super(token);
        this.setType(new FunctionType());
    }

    public FuncTypeNode(ParseNode node) {
        super(node);
    }

    ////////////////////////////////////////////////////////////
    // attributes

    public Type returnType() {
        return ((FunctionType) this.getType()).getReturnType();
    }

    public void setReturnType(Type type) {
        ((FunctionType) this.getType()).setReturnType(type);
    }

    public void addChildType(Type type) {
        ((FunctionType) this.getType()).addType(type);
    }

    ///////////////////////////////////////////////////////////
    // accept a visitor

    public void accept(ParseNodeVisitor visitor) {
        visitor.visit(this);
    }

}