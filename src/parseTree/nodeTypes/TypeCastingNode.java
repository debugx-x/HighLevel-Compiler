package parseTree.nodeTypes;

import parseTree.ParseNode;
import parseTree.ParseNodeVisitor;
import semanticAnalyzer.signatures.FunctionSignature;
import semanticAnalyzer.types.Type;
import tokens.Token;

public class TypeCastingNode extends ParseNode {
	public Type type;
	public FunctionSignature signature;

	public TypeCastingNode(Token token) {
		super(token);
		initChildren();
	}

	public TypeCastingNode(ParseNode node) {
		super(node);
		initChildren();
	}


	////////////////////////////////////////////////////////////
	// attributes

	public Token typeToken() {
		return token;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public Type getType() {
		return type;
	}

	public void setSignature(FunctionSignature signature) {
		this.signature = signature;
	}

	public FunctionSignature getSignature() {
		return signature;
	}


	////////////////////////////////////////////////////////////
	// convenience factory
	
	public static TypeCastingNode withChildren(Token token, ParseNode... children) {
		TypeCastingNode node = new TypeCastingNode(token);
		
		for (ParseNode child : children) {
			node.appendChild(child);
		}

		return node;
	}

	public static TypeCastingNode withChildren(Token token, ParseNode child, Type type) {
		TypeCastingNode node = new TypeCastingNode(token);
		node.appendChild(child);
		node.setType(type);
		return node;
	}
	
////////////////////////////////////////////////////////////
//Speciality functions
	public Type getExpressionType() {
		return this.child(0).getType();
	}
	
	
	///////////////////////////////////////////////////////////
	// boilerplate for visitors
			
	public void accept(ParseNodeVisitor visitor) {
		visitor.visitEnter(this);
		visitChildren(visitor);
		visitor.visitLeave(this);
	}
}