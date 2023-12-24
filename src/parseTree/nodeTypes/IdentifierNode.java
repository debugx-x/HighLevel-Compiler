package parseTree.nodeTypes;

import parseTree.ParseNode;
import parseTree.ParseNodeVisitor;
import logging.TanLogger;
import symbolTable.Binding;
import symbolTable.Scope;
import tokens.IdentifierToken;
import tokens.Token;

public class IdentifierNode extends ParseNode {
	private Binding binding;
	private Scope declarationScope;
	

	public IdentifierNode(Token token) {
		super(token);
		assert(token instanceof IdentifierToken);
		this.binding = null;
		this.declarationScope = null;
	}
	public IdentifierNode(ParseNode node) {
		super(node);
		
		if(node instanceof IdentifierNode) {
			this.binding = ((IdentifierNode)node).binding;
		}
		else {
			this.binding = null;
		}
	}
	
////////////////////////////////////////////////////////////
// attributes
	
	public IdentifierToken identifierToken() {
		return (IdentifierToken)token;
	}

	public void setBinding(Binding binding) {
		this.binding = binding;
	}
	public Binding getBinding() {
		return binding;
	}
	
	public boolean isIndexed() {
		return this.nChildren() > 0;
	}
////////////////////////////////////////////////////////////
// Convenience Factory
	public static IdentifierNode withChildren(Token token, ParseNode index) {
		IdentifierNode node = new IdentifierNode(token);
		node.appendChild(index);
		return node;
	}
	
////////////////////////////////////////////////////////////
// Speciality functions

	public Binding findVariableBinding() {
		String identifier = token.getLexeme();

		for(ParseNode current : pathToRoot()) {
			if(current.containsBindingOf(identifier)) {
				declarationScope = current.getScope();
				return current.bindingOf(identifier);
			}
		}
		useBeforeDefineError();
		return Binding.nullInstance();
	}

	public Scope getDeclarationScope() {
		findVariableBinding();
		return declarationScope;
	}
	public void useBeforeDefineError() {
		TanLogger log = TanLogger.getLogger("compiler.semanticAnalyzer.identifierNode");
		Token token = getToken();
		log.severe("identifier \"" + token.getLexeme() + "\" used before defined at " + token.getLocation());
	}
	
///////////////////////////////////////////////////////////
// accept a visitor
		
	public void accept(ParseNodeVisitor visitor) {
		visitor.visitEnter(this);
		visitChildren(visitor);
		visitor.visitLeave(this);
	}

}