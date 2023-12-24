package parseTree.nodeTypes;

import parseTree.ParseNode;
import parseTree.ParseNodeVisitor;
import semanticAnalyzer.types.ArrayType;
import semanticAnalyzer.types.Type;
import lexicalAnalyzer.Keyword;
import lexicalAnalyzer.Lextant;
import tokens.LextantToken;
import tokens.Token;

public class ArrayNode extends ParseNode {
	public boolean isEmpty = false;

	public ArrayNode(Token token) {
		super(token);
		assert(token.isLextant(Keyword.NEW));
	}
	public ArrayNode(ParseNode node) {
		super(node);
	}
	
////////////////////////////////////////////////////////////
// attributes
	
	public Lextant getOperator() {
		return lextantToken().getLextant();
	}
	public LextantToken lextantToken() {
		return (LextantToken)token;
	}

	public void setSubtype(Type subtype) {
		ArrayType type = new ArrayType();
		type.setSubtype(subtype);
		super.setType(type);		
	}

	public Type getSubtype() {
		return ((ArrayType)super.getType()).getSubtype();
	}

	public void setIsEmpty(boolean isEmpty) {
		this.isEmpty = isEmpty;
	}
	public boolean isEmpty() {
		return isEmpty;
	}

	public int getAllocateSize() {
		int typeSize = this.getSubtype().getSize();
		int arrayElements = this.nChildren();
		int size = 16 + (typeSize * arrayElements);
		return size;
	}

	public int getOffset(int i) {
		int headerSize = 16;
		int typeSize = this.getSubtype().getSize();
		int offset = headerSize + (typeSize * i);
		return offset;
	}
	
	
////////////////////////////////////////////////////////////
// convenience factory
	
	public static ArrayNode withChildren(Token token, Type type, ParseNode inside, boolean isEmpty) {
		ArrayNode node = new ArrayNode(token);
		node.appendChild(inside);
		node.setType(type);
		node.isEmpty = isEmpty;
		return node;
	}

	public static ArrayNode withChildren(Token token, ParseNode inside, boolean isEmpty) {
		ArrayNode node = new ArrayNode(token);
		node.appendChild(inside);
		node.isEmpty = isEmpty;
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
