package parseTree.nodeTypes;

import parseTree.ParseNode;
import parseTree.ParseNodeVisitor;
import lexicalAnalyzer.Keyword;
import lexicalAnalyzer.Lextant;
import tokens.LextantToken;
import tokens.Token;

public class IfNode extends ParseNode {

	public IfNode(Token token) {
		super(token);
		assert(token.isLextant(Keyword.IF));
	}

	public IfNode(ParseNode node) {
		super(node);
		initChildren();
	}
	
	public IfNode(Token token, ParseNode expression, ParseNode blockStatement) {
		super(token);
		assert(token.isLextant(Keyword.IF));
		this.appendChild(expression);
		this.appendChild(blockStatement);
	}
	
	////////////////////////////////////////////////////////////
	// attributes
	
	public Lextant getDeclarationType() {
		return lextantToken().getLextant();
	}
	public LextantToken lextantToken() {
		return (LextantToken)token;
	}	
	
	
	////////////////////////////////////////////////////////////
	// convenience factory
	
	public static IfNode withChildren(Token token, ParseNode expression, ParseNode blockStatement, ParseNode elseBlock) {
		IfNode node = new IfNode(token, expression, blockStatement);
		node.appendChild(elseBlock);
		return node;
	}
	
	
	///////////////////////////////////////////////////////////
	// boilerplate for visitors
			
	public void accept(ParseNodeVisitor visitor) {
		visitor.visitEnter(this);
		visitChildren(visitor);
		visitor.visitLeave(this);
	}
}
