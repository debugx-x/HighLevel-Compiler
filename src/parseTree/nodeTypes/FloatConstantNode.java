package parseTree.nodeTypes;

import parseTree.ParseNode;
import parseTree.ParseNodeVisitor;
import tokens.FloatingLiteralToken;
import tokens.Token;

public class FloatConstantNode extends ParseNode {
	public FloatConstantNode(Token token) {
		super(token);
		assert(token instanceof FloatingLiteralToken);
	}
	public FloatConstantNode(ParseNode node) {
		super(node);
	}

////////////////////////////////////////////////////////////
// attributes
	
	public double getValue() {
		return FloatingLiteralToken().getValue();
	}

	public FloatingLiteralToken FloatingLiteralToken() {
		return (FloatingLiteralToken)token;
	}	

///////////////////////////////////////////////////////////
// accept a visitor
	
	public void accept(ParseNodeVisitor visitor) {
		visitor.visit(this);
	}

}
