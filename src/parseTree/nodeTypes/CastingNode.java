package parseTree.nodeTypes;

import parseTree.ParseNode;
import parseTree.ParseNodeVisitor;
import tokens.Token;

public class CastingNode extends ParseNode {

	public CastingNode(Token token) {
		super(token);
	}
	public CastingNode(ParseNode node) {
		super(node);
	}
	
	////////////////////////////////////////////////////////////
	// no attributes

	
	///////////////////////////////////////////////////////////
	// boilerplate for visitors
	
	public void accept(ParseNodeVisitor visitor) {
		visitor.visitEnter(this);
		visitChildren(visitor);
		visitor.visitLeave(this);
	}
	public static ParseNode withChildren(Token castingToken) {
		CastingNode node = new CastingNode(castingToken);
		return node;
	}
}
