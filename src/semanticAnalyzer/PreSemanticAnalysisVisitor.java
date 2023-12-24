package semanticAnalyzer;

import java.util.ArrayList;
import java.util.List;
import asmCodeGenerator.codeStorage.ASMOpcode;
import lexicalAnalyzer.Keyword;
import logging.TanLogger;
import parseTree.ParseNode;
import parseTree.ParseNodeVisitor;
import parseTree.nodeTypes.*;
import semanticAnalyzer.signatures.*;
import semanticAnalyzer.types.*;
import symbolTable.Binding;
import symbolTable.Scope;
import tokens.Token;

class PreSemanticAnalysisVisitor extends ParseNodeVisitor.Default {
	
	@Override
	public void visitLeave(ParseNode node) {
		throw new RuntimeException("Node class unimplemented in SemanticAnalysisVisitor: " + node.getClass());
	}
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructs larger than statements
	@Override
	public void visitEnter(ProgramNode node) {
		Scope scope = Scope.createProgramScope();
		node.setScope(scope);
	}
	@Override
	public void visitEnter(MainBlockNode node) {
		createSubscope(node);
	}
	@Override
	public void visitEnter(BlockStatementNode node) {
		if (node.getParent() instanceof FunctionNode) {
			createProcedureScope(node);
		} else {
			createSubscope(node);
		}
	}

	
	///////////////////////////////////////////////////////////////////////////
	// helper methods for scoping.
	private void createParameterScope(ParseNode node) {
		Scope scope = Scope.createParameterScope();
		node.setScope(scope);
	}
	private void createProcedureScope(ParseNode node) {
		Scope baseScope = node.getLocalScope();
		Scope scope = baseScope.createProcedureScope();
		node.setScope(scope);
	}
	private void createSubscope(ParseNode node) {
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
	// functionDefinitions
	@Override	
	public void visitLeave(FuncDefNode node) {
		if (node.child(0) instanceof IdentifierNode && node.child(1) instanceof FunctionNode) {
			IdentifierNode identifier = (IdentifierNode) node.child(0);
			FunctionNode lambda = (FunctionNode) node.child(1);

			Type functionType = lambda.getType();
			FunctionSignature signature = ((FunctionNode) node.child(1)).getSignature();
			
			node.setSignature(signature);
			node.setType(functionType);
			identifier.setType(functionType);
			
			addBinding(identifier, functionType, lambda.getStartLabel(), signature);		
		}		
	}

	
	///////////////////////////////////////////////////////////////////////////
	// lambda
	@Override
	public void visitEnter(FunctionNode node) {
		node.generateLabels();
		createParameterScope(node);
		enterScope(node);
	}
	@Override
	public void visitLeave(FunctionNode node) {		
		if (node.child(0) instanceof FuncParamTypeNode) {
			FuncParamTypeNode params = (FuncParamTypeNode) node.child(0);
			
			List<Type> childTypes = new ArrayList<Type>();
			FunctionType functionType = new FunctionType();

			params.getChildren().forEach((child) -> {
				functionType.addType(child.getType());
				childTypes.add(getType(child));
			});
			
			Type returnType = getType(params);
			
			FunctionSignature signature = new FunctionSignature(ASMOpcode.Nop, childTypes, returnType);
			functionType.setReturnType(returnType);
			
			node.setSignature(signature);
			node.setType(functionType);
		}
		
		leaveScope(node);
	}	
	@Override
	public void visitLeave(FuncParamNode node) {
		if (node.child(0) instanceof IdentifierNode) {
			IdentifierNode identifier = (IdentifierNode) node.child(0);

			Type paramType = getType(node);
			if (paramType == PrimitiveType.VOID) {
				node.setType(PrimitiveType.ERROR);
				Token token = node.getToken();
				logError("Parameter cannot be defined as type VOID at " + token.getLocation());
				return;
			}
			
			FunctionSignature signature = null;
			if (paramType instanceof FunctionType) {
				signature = ((FunctionType) paramType).getSignature();
			}
			
			addBinding(identifier, paramType, null, signature);
		}
	}

	
	///////////////////////////////////////////////////////////////////////////
	// lambdaType
	public void visit(FuncTypeNode node) {
	}
	
	
	///////////////////////////////////////////////////////////////////////////
	// helper methods for types
	private Type getType(ParseNode node) {
		Type type = node.getType();

		if (type instanceof PrimitiveType) {
			return type;
		}
		if (type instanceof TypeLiteral) {
			return ((TypeLiteral) type ).getLiteraltype();
		}
		if (type instanceof ArrayType) {
			return type;
		}
		if (type instanceof FunctionType) {
			return type;
		}
		
		return PrimitiveType.ERROR;
	}

	
	///////////////////////////////////////////////////////////////////////////
	// declaration (to ensure inline recursion)
	@Override
	public void visitLeave(DeclarationNode node) {
		if (node.child(0) instanceof IdentifierNode && node.child(1) instanceof FunctionNode) {
			IdentifierNode identifier = (IdentifierNode) node.child(0);
			FunctionNode lambda = (FunctionNode) node.child(1);

			Type functionType = lambda.getType();
			FunctionSignature signature = ((FunctionNode) node.child(1)).getSignature();

			node.setType(functionType);
			identifier.setType(functionType);
			
			addBinding(identifier, functionType, lambda.getStartLabel(), signature);		
		}
	}
	
	
	///////////////////////////////////////////////////////////////////////////
	// helper methods for binding
	private void addBinding(IdentifierNode identifierNode, Type type, String label, FunctionSignature signature) {
		Scope scope = identifierNode.getLocalScope();
		Binding binding = scope.createBinding(identifierNode, type);
		binding.setMutability(false);
		binding.setSignature(signature);
		binding.setLabel(label);
		identifierNode.setBinding(binding);
	}

	
	///////////////////////////////////////////////////////////////////////////
	// error logging/printing
	private void logError(String message) {
		TanLogger log = TanLogger.getLogger("compiler.semanticAnalyzer.PreSemanticAnalysisVisitor");
		log.severe(message);
	}
}