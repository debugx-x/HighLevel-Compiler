package parseTree.nodeTypes;

import parseTree.ParseNode;
import parseTree.ParseNodeVisitor;
import semanticAnalyzer.signatures.FunctionSignature;
import symbolTable.Binding;
import symbolTable.Scope;
import lexicalAnalyzer.Keyword;
import lexicalAnalyzer.Lextant;
import tokens.LextantToken;
import tokens.Token;

public class FuncDefNode extends ParseNode {
	protected FunctionSignature signature;
	private Binding binding;
	private Scope parameterScope;
	String functionLabel;
	
	public FuncDefNode(Token token) {
		super(token);
		assert(token.isLextant(Keyword.SUBR));
		this.binding = null;
	}
	
	public FuncDefNode(ParseNode node) {
		super(node);
		
		if(node instanceof FuncDefNode) {
			this.binding = ((FuncDefNode) node).binding;
		} else {
			this.binding = null;
		}
		
		initChildren();
	}
	
	
	////////////////////////////////////////////////////////////
	// attributes
	
	public void setBinding(Binding binding) {
		this.binding = binding;
	}
	public Binding getBinding() {
		return binding;
	}
	
	public Lextant getReturnType() {
		return lextantToken().getLextant();
	}
	public LextantToken lextantToken() {
		return (LextantToken)token;
	}
	
	public void setSignature(FunctionSignature signature) {
		this.signature = signature;
	}
	public FunctionSignature getSignature() {
		return signature;
	}
	
	public void setLabel(String label) {
		this.functionLabel = label;
	}
	public String getLabel() {
		return functionLabel;
	}
	
	
	////////////////////////////////////////////////////////////
	// convenience factory
	
	public static FuncDefNode withChildren(Token token, ParseNode identifier, ParseNode Function) {
		FuncDefNode node = new FuncDefNode(token);
		node.appendChild(identifier);
		node.appendChild(Function);
		return node;
	}
	
	
	////////////////////////////////////////////////////////////
	//Speciality functions
	
	public Binding findVariableBinding() {
		String identifier = token.getLexeme();
		
		for(ParseNode current : pathToRoot()) {
			if(current.containsBindingOf(identifier)) {
				parameterScope = current.getScope();
				return current.bindingOf(identifier);
			}
		}
		return Binding.nullInstance();
	}

	public Scope getParameterScope() {
		findVariableBinding();
		return parameterScope;
	}

	///////////////////////////////////////////////////////////
	// boilerplate for visitors
			
	public void accept(ParseNodeVisitor visitor) {
		visitor.visitEnter(this);
		visitChildren(visitor);
		visitor.visitLeave(this);
	}
}