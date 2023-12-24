package semanticAnalyzer;

import java.util.Arrays;
import java.util.List;
import java.io.Reader;
import java.util.ArrayList;

import lexicalAnalyzer.Keyword;
import lexicalAnalyzer.Lextant;
import lexicalAnalyzer.Punctuator;
import logging.TanLogger;
import parseTree.ParseNode;
import parseTree.ParseNodeVisitor;
import parseTree.nodeTypes.ArrayNode;
import parseTree.nodeTypes.AssignmentStatementNode;
import parseTree.nodeTypes.BlockStatementNode;
import parseTree.nodeTypes.BooleanConstantNode;
import parseTree.nodeTypes.CallNode;
import parseTree.nodeTypes.ReturnNode;
import parseTree.nodeTypes.CharacterConstantNode;
import parseTree.nodeTypes.MainBlockNode;
import parseTree.nodeTypes.DeclarationNode;
import parseTree.nodeTypes.ErrorNode;
import parseTree.nodeTypes.IdentifierNode;
import parseTree.nodeTypes.IfNode;
import parseTree.nodeTypes.IntegerConstantNode;
import parseTree.nodeTypes.FloatConstantNode;
import parseTree.nodeTypes.FuncDefNode;
import parseTree.nodeTypes.FuncInvocNode;
import parseTree.nodeTypes.FuncParamNode;
import parseTree.nodeTypes.FunctionNode;
import parseTree.nodeTypes.NewlineNode;
import parseTree.nodeTypes.OperatorNode;
import parseTree.nodeTypes.PrintStatementNode;
import parseTree.nodeTypes.ProgramNode;
import parseTree.nodeTypes.SpaceNode;
import parseTree.nodeTypes.StringConstantNode;
import parseTree.nodeTypes.TabNode;
import parseTree.nodeTypes.TypeCastingNode;
import parseTree.nodeTypes.WhileNode;
import semanticAnalyzer.signatures.*;
import semanticAnalyzer.types.ArrayType;
import semanticAnalyzer.types.FunctionType;
import semanticAnalyzer.types.PrimitiveType;
import semanticAnalyzer.types.Type;
import symbolTable.Binding;
import symbolTable.Binding.Constancy;
import symbolTable.Scope;
import tokens.LextantToken;
import tokens.Token;

class SemanticAnalysisVisitor extends ParseNodeVisitor.Default {

	// create promotion object
	public Promotion promotion = new Promotion();

	@Override
	public void visitLeave(ParseNode node) {
		throw new RuntimeException("Node class unimplemented in SemanticAnalysisVisitor: " + node.getClass());
	}
	
	///////////////////////////////////////////////////////////////////////////
	// constructs larger than statements
	@Override
	public void visitEnter(ProgramNode node) {
		// enterProgramScope(node);
		enterScope(node);
	}
	public void visitLeave(ProgramNode node) {
		leaveScope(node);
	}
	public void visitEnter(MainBlockNode node) {
	}
	public void visitLeave(MainBlockNode node) {
	}
	public void visitEnter(BlockStatementNode node) {
		//enterSubscope(node);
		enterScope(node);
	}
	public void visitLeave(BlockStatementNode node) {
		leaveScope(node);
	}
	
	
	///////////////////////////////////////////////////////////////////////////
	// helper methods for scoping.
	private void enterProgramScope(ParseNode node) {
		Scope scope = Scope.createProgramScope();
		node.setScope(scope);
	}	
	//@SuppressWarnings("unused")
	private void enterSubscope(ParseNode node) {
		Scope baseScope = node.getLocalScope();
		Scope scope = baseScope.createSubscope();
		node.setScope(scope);
	}
	private void enterScope(ParseNode node) {
		node.getScope().enter();
	}		
	private void leaveScope(ParseNode node) {
		node.getScope().leave();
	}

	///////////////////////////////////////////////////////////////////////////
	// Function definitions, calls, and returns
	@Override
	public void visitLeave(FuncDefNode node) {
	}

	@Override
	public void visitLeave(FunctionNode node){
	}
	
	@Override
	public void visitLeave(CallNode node) {
		if (!(node.child(0) instanceof FuncInvocNode)) {
			typeCheckError(node, Arrays.asList(node.child(0).getType()));
		}
	}

	@Override
	public void visitLeave(FuncInvocNode node) {
		if (node.child(0).getType() instanceof FunctionType) {
			FunctionType type = (FunctionType)node.child(0).getType();
			Type returnType = type.getReturnType();
			
			if (returnType == PrimitiveType.VOID) {
				ParseNode parent = node.getParent();
				while (parent != null && !(parent instanceof CallNode)) {
					parent = parent.getParent();
				}
				
				if (parent == null || !(parent instanceof CallNode)) {
					functionInvocationError(node, "Cannot use VOID function as an expression");
					return;
				}
			}

			FuncInvocNode function = null;
			FunctionSignature signature = type.getSignature();

			if (node.child(0) instanceof CallNode) {
				function = (FuncInvocNode) node.child(0);
			} else if (node instanceof FuncInvocNode) {
				function = (FuncInvocNode) node;
			}

			if (signature == null) {
				functionInvocationError(node, "No signature defined for function");
				return;
			}

			// Get child types
			List<Type> childTypes = new ArrayList<Type>();
			function.getChildren().forEach((child) -> childTypes.add(child.getType()));
			childTypes.remove(0); // Remove identifier node

			// Check that number of arguments is the same
			if (signature.accepts(childTypes)) {
				node.setType(signature.resultType());
			} else {
				typeCheckError(function, childTypes);
			}
		} else {
			functionInvocationError(node, "Invoking function on non-function type");
			return;
		}
	}
	@Override
	public void visitLeave(ReturnNode node){
		ParseNode parent = node.getParent();
		while (parent != null && !(parent instanceof FunctionNode)) {
			parent = parent.getParent();
		}

		if (parent == null) {
			Token token = node.getToken();
			logError("Cannot call return statement outside of a function at " + token.getLocation());

			node.setType(PrimitiveType.ERROR);
			return;
		}

		Type functionReturnType = ((FunctionNode) parent).getReturnType();
		Type returnType;
		String returnTypeString = "";

		// Handle void return
		if (node.nChildren() == 0) {
			returnType = PrimitiveType.VOID;
		} else {
			returnType = node.child(0).getType();
		}

		if (returnType.equals(functionReturnType)) {
			node.setType(returnType);
		} else {
			node.setType(PrimitiveType.ERROR);

			Token token = node.getToken();
			returnTypeString = returnType.infoString();
			logError("Cannot return type " + returnTypeString + " for function with return type "
					+ functionReturnType.infoString() + " at " + token.getLocation());
		}
	}


	///////////////////////////////////////////////////////////////////////////
	// statements and declarations
	@Override
	public void visitLeave(PrintStatementNode node) {
	}
	@Override
	public void visitLeave(DeclarationNode node) {
		if(node.child(0) instanceof ErrorNode) {
			node.setType(PrimitiveType.ERROR);
			return;
		}
		
		IdentifierNode identifier = (IdentifierNode) node.child(0);
		ParseNode initializer = node.child(1);
		
		Type declarationType = initializer.getType();
		node.setType(declarationType);
		
		identifier.setType(declarationType);
		Constancy constancy = (node.getToken().isLextant(Keyword.CONST)) ? Constancy.IS_CONSTANT : Constancy.IS_VARIABLE;
		addBinding(identifier, declarationType, constancy);
	}
	
	@Override
	public void visitLeave(AssignmentStatementNode node) {
		if(node.child(0) instanceof ErrorNode) {
			node.setType(PrimitiveType.ERROR);
			return;
		}
		
		IdentifierNode target = (IdentifierNode) node.child(0);
		ParseNode expression = node.child(1);
		
		Type expressionType = expression.getType();
		Type identifierType = target.getType();
		
		if (identifierType instanceof ArrayType && target.isIndexed()) {
			identifierType = ((ArrayType)identifierType).getSubtype();
		}
		target.setType(identifierType);
		
		if(!expressionType.equals(identifierType)) {
			typeCheckError(node, Arrays.asList(identifierType, expressionType));
			//semanticError("Types don't match in AssignmentStatement");
			return;
		}
		if(target.getBinding().isConstant() && !target.isIndexed()) {
			semanticError("Reassignment to const identifier");
		}
		//node.setType(identifierType);
		
		if ((identifierType instanceof ArrayType) && (expressionType instanceof ArrayType)) {
			Type targetSubtype = ((ArrayType)identifierType).getSubtype();
			Type expressionSubtype = ((ArrayType)expressionType).getSubtype();

			while ((targetSubtype instanceof ArrayType) && (expressionSubtype instanceof ArrayType)) {
				targetSubtype = ((ArrayType)targetSubtype).getSubtype();
				expressionSubtype = ((ArrayType)expressionSubtype).getSubtype();
			}
		}
		
		node.setType(expressionType);
		target.setType(expressionType);
		
	}

	@Override
	public void visitLeave(IfNode node) {
		assert node.nChildren() >= 2;		// if can have childs expression, block, elseblock
	}
	
	@Override
	public void visitLeave(WhileNode node) {
		assert node.nChildren() >= 2;		// while has childs expression and block
	}

	///////////////////////////////////////////////////////////////////////////
	// expressions
	@Override
	public void visitLeave(OperatorNode node) {
		List<Type> childTypes;  
		if(node.nChildren() == 1) {
			ParseNode child = node.child(0);
			childTypes = Arrays.asList(child.getType());
		}
		else {
			assert node.nChildren() == 2;
			ParseNode left  = node.child(0);
			ParseNode right = node.child(1);
			
			childTypes = Arrays.asList(left.getType(), right.getType());		
		}
		
		Lextant operator = operatorFor(node);
		
		if (operator == Keyword.LENGTH) {
			if (childTypes.get(0) instanceof ArrayType) {
				FunctionSignature signature = FunctionSignatures.signaturesOf(operator).get(0);
				node.setSignature(signature);		
				node.setType(signature.resultType());
			} else {
				typeCheckError(node, childTypes);
				//node.setType(PrimitiveType.ERROR);
			}
		}
		else {
			FunctionSignature signature = FunctionSignatures.signature(operator, childTypes);
			//Object variant = signature.getVariant();
					
			if(signature.accepts(childTypes)) {
				node.setType(signature.resultType());
				node.setSignature(signature);
			}
			else {
				typeCheckError(node, childTypes);
				//node.setType(PrimitiveType.ERROR);
			}
		}
	}
	
	@Override
	public void visitLeave(TypeCastingNode node){
		node.setType(PrimitiveType.fromToken(node.typeToken()));
	}


	private Lextant operatorFor(OperatorNode node) {
		LextantToken token = (LextantToken) node.getToken();
		return token.getLextant();
	}

	@Override
	public void visitLeave(ArrayNode node) {
		List<Type> childTypes = new ArrayList<Type>();
		node.getChildren().forEach((child) -> childTypes.add(child.getType()));

		Lextant operator = operatorFor(node);

		if (operator == Keyword.NEW) {
			if (!node.isEmpty()) {

				// Check that all values are of same type
				Type t = childTypes.get(0);
				int numType = 0;
				for (int i = 0; i < childTypes.size(); i++) {
					if(childTypes.get(i) == t) {
						numType += 1;
					}
				}
				
				if (!(t instanceof ArrayType) && numType != node.nChildren()) {
					typeCheckError(node, childTypes);
					return;
					// node.setType(PrimitiveType.ERROR);
				}
				
				if (node instanceof ArrayNode) {
					// Set type
					node.setSubtype(childTypes.get(0));
				}
			} else {
		
				if (childTypes.get(0) != PrimitiveType.INTEGER) {
					typeCheckError(node, childTypes);
					return;
					// node.setType(PrimitiveType.ERROR);
				}
			}
		}
	}
	
	private Lextant operatorFor(ArrayNode node) {
		LextantToken token = (LextantToken) node.getToken();
		return token.getLextant();
	}
	
	


	///////////////////////////////////////////////////////////////////////////
	// simple leaf nodes
	@Override
	public void visit(BooleanConstantNode node) {
		node.setType(PrimitiveType.BOOLEAN);
	}
	@Override
	public void visit(ErrorNode node) {
		node.setType(PrimitiveType.ERROR);
	}
	@Override
	public void visit(IntegerConstantNode node) {
		node.setType(PrimitiveType.INTEGER);
	}
	@Override
	public void visit(FloatConstantNode node) {
		node.setType(PrimitiveType.FLOAT);
	}
	@Override
	public void visit(StringConstantNode node) {
		node.setType(PrimitiveType.STRING);
	}
	@Override
	public void visit(CharacterConstantNode node) {
		node.setType(PrimitiveType.CHARACTER);
	}
	@Override
	public void visit(NewlineNode node) {
	}
	@Override
	public void visit(SpaceNode node) {
	}
	@Override
	public void visit(TabNode node) {
	}
	///////////////////////////////////////////////////////////////////////////
	// IdentifierNodes, with helper methods
	@Override
	public void visitLeave(IdentifierNode node) {
		if(!isBeingDeclared(node) && !isFunctionIdentifier(node) && !isFunctionParameter(node)) {		
			Binding binding = node.findVariableBinding();
			node.setBinding(binding);
			
			Type type = node.getBinding().getType();
			if (type instanceof ArrayType && node.isIndexed()) {
				type = ((ArrayType)type).getSubtype();
			}
			node.setType(type);
		}
		// else parent DeclarationNode does the processing.
	}
	private boolean isBeingDeclared(IdentifierNode node) {
		ParseNode parent = node.getParent();
		return (parent instanceof DeclarationNode) && (node == parent.child(0));
	}
	private void addBinding(IdentifierNode identifierNode, Type type, Constancy constancy) {
		Scope scope = identifierNode.getLocalScope();
		Binding binding = scope.createBinding(identifierNode, type, constancy);
		identifierNode.setBinding(binding);
	}
	
	private boolean isFunctionIdentifier(IdentifierNode node) {
		ParseNode parent = node.getParent();
		return (parent instanceof FuncDefNode) && (node == parent.child(0));
	}

	private boolean isFunctionParameter(IdentifierNode node) {
		ParseNode parent = node.getParent();
		return (parent instanceof FuncParamNode) && (node == parent.child(0));
	}
	
	///////////////////////////////////////////////////////////////////////////
	// error logging/printing
	private void functionInvocationError(ParseNode node, String message) {
		Token token = node.getToken();
		logError("function invocation error: " + message + " at " + token.getLocation());
		node.setType(PrimitiveType.ERROR);
	}

	private void typeCheckError(ParseNode node, List<Type> operandTypes) {
		Token token = node.getToken();

		// Check promotion
		if (node instanceof OperatorNode && promotion.promotable((OperatorNode) node))
			return;
		if (node instanceof ArrayNode && promotion.promotable((ArrayNode) node))
			return;
		if (node instanceof FuncDefNode && promotion.promotable((FuncDefNode) node))
			return;
		if (node instanceof AssignmentStatementNode && promotion.promotable(node))
			return;

			
		// Check for error node
		List<String> errorTypes = new ArrayList<String>();
		operandTypes.forEach((child) -> errorTypes.add(child.infoString()));

		node.setType(PrimitiveType.ERROR);
		
		logError("operator " + token.getLexeme() + " not defined for types " 
				 + operandTypes  + " at " + token.getLocation());	
	}
	
	private void semanticError(String message) {
		TanLogger log = TanLogger.getLogger("compiler.semanticAnalyzer");
		log.severe(message);
	}
	
	private void logError(String message) {
		TanLogger log = TanLogger.getLogger("compiler.semanticAnalyzer");
		log.severe(message);
	}
}