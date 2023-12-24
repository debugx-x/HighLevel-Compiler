package parseTree;

import parseTree.nodeTypes.*;

// Visitor pattern with pre- and post-order visits
public interface ParseNodeVisitor {
	
	// non-leaf nodes: visitEnter and visitLeave
	void visitEnter(OperatorNode node);
	void visitLeave(OperatorNode node);
	
	void visitEnter(MainBlockNode node);
	void visitLeave(MainBlockNode node);
	
	void visitEnter(BlockStatementNode node);
	void visitLeave(BlockStatementNode node);

	void visitEnter(DeclarationNode node);
	void visitLeave(DeclarationNode node);

	void visitEnter(AssignmentStatementNode node);
	void visitLeave(AssignmentStatementNode node);
	
	void visitEnter(ParseNode node);
	void visitLeave(ParseNode node);
	
	void visitEnter(PrintStatementNode node);
	void visitLeave(PrintStatementNode node);
	
	void visitEnter(ProgramNode node);
	void visitLeave(ProgramNode node);

	void visitEnter(TypeCastingNode node);
	void visitLeave(TypeCastingNode node);
	
	void visitEnter(IfNode node);
	void visitLeave(IfNode node);
	
	void visitEnter(WhileNode node);
	void visitLeave(WhileNode node);
	
	void visitEnter(ArrayNode node);
	void visitLeave(ArrayNode node);
	
	void visitEnter(IdentifierNode node);
	void visitLeave(IdentifierNode node);

	void visitEnter(FuncDefNode node);
	void visitLeave(FuncDefNode node);

	void visitEnter(FuncInvocNode node);
	void visitLeave(FuncInvocNode node);

	void visitEnter(CallNode node);
	void visitLeave(CallNode node);

	void visitEnter(ReturnNode node);
	void visitLeave(ReturnNode node);

	void visitEnter(FunctionNode node);
	void visitLeave(FunctionNode node);

	void visitEnter(FuncParamTypeNode node);
	void visitLeave(FuncParamTypeNode node);

	void visitEnter(FuncParamNode node);
	void visitLeave(FuncParamNode node);
	

	// leaf nodes: visitLeaf only
	void visit(FuncTypeNode node);
	void visit(BooleanConstantNode node);
	void visit(ErrorNode node);
//	void visit(IdentifierNode node);
	void visit(IntegerConstantNode node);
	void visit(FloatConstantNode node);
	void visit(StringConstantNode node);
	void visit(CharacterConstantNode node);
	void visit(NewlineNode node);
	void visit(SpaceNode node);
	void visit(TabNode node);

	
	public static class Default implements ParseNodeVisitor
	{
		public void defaultVisit(ParseNode node) {	}
		public void defaultVisitEnter(ParseNode node) {
			defaultVisit(node);
		}
		public void defaultVisitLeave(ParseNode node) {
			defaultVisit(node);
		}		
		public void defaultVisitForLeaf(ParseNode node) {
			defaultVisit(node);
		}
		
		public void visitEnter(OperatorNode node) {
			defaultVisitEnter(node);
		}
		public void visitLeave(OperatorNode node) {
			defaultVisitLeave(node);
		}
		public void visitEnter(DeclarationNode node) {
			defaultVisitEnter(node);
		}
		public void visitLeave(DeclarationNode node) {
			defaultVisitLeave(node);
		}
		public void visitEnter(AssignmentStatementNode node) {
			defaultVisitEnter(node);
		}
		public void visitLeave(AssignmentStatementNode node) {
			defaultVisitLeave(node);
		}
		public void visitEnter(MainBlockNode node) {
			defaultVisitEnter(node);
		}
		public void visitLeave(MainBlockNode node) {
			defaultVisitLeave(node);
		}				
		public void visitEnter(BlockStatementNode node) {
			defaultVisitEnter(node);
		}
		public void visitLeave(BlockStatementNode node) {
			defaultVisitLeave(node);
		}
		public void visitEnter(ParseNode node) {
			defaultVisitEnter(node);
		}
		public void visitLeave(ParseNode node) {
			defaultVisitLeave(node);
		}
		public void visitEnter(PrintStatementNode node) {
			defaultVisitEnter(node);
		}
		public void visitLeave(PrintStatementNode node) {
			defaultVisitLeave(node);
		}
		public void visitEnter(ProgramNode node) {
			defaultVisitEnter(node);
		}
		public void visitLeave(ProgramNode node) {
			defaultVisitLeave(node);
		}
		public void visitEnter(TypeCastingNode node) {
			defaultVisitEnter(node);
		}
		public void visitLeave(TypeCastingNode node) {
			defaultVisitLeave(node);
		}
		public void visitEnter(IfNode node) {
			defaultVisitEnter(node);			
		}
		public void visitLeave(IfNode node) {
			defaultVisitLeave(node);			
		}
		public void visitEnter(WhileNode node) {
			defaultVisitEnter(node);			
		}
		public void visitLeave(WhileNode node) {
			defaultVisitLeave(node);			
		}
		public void visitEnter(ArrayNode node) {
			defaultVisitEnter(node);			
		}
		public void visitLeave(ArrayNode node) {
			defaultVisitLeave(node);			
		}
		public void visitEnter(IdentifierNode node) {
			defaultVisitEnter(node);			
		}
		public void visitLeave(IdentifierNode node) {
			defaultVisitLeave(node);			
		}		
		public void visitEnter(FuncDefNode node) {
			defaultVisitEnter(node);
		}
		public void visitLeave(FuncDefNode node) {
			defaultVisitLeave(node);
		}
		public void visitEnter(FuncInvocNode node) {
			defaultVisitEnter(node);
		}
		public void visitLeave(FuncInvocNode node) {
			defaultVisitLeave(node);
		}
		public void visitEnter(CallNode node) {
			defaultVisitEnter(node);
		}
		public void visitLeave(CallNode node) {
			defaultVisitLeave(node);
		}
		public void visitEnter(ReturnNode node) {
			defaultVisitEnter(node);
		}
		public void visitLeave(ReturnNode node) {
			defaultVisitLeave(node);
		}
		public void visitEnter(FunctionNode node) {
			defaultVisitEnter(node);
		}
		public void visitLeave(FunctionNode node) {
			defaultVisitLeave(node);
		}
		public void visitEnter(FuncParamTypeNode node) {
			defaultVisitEnter(node);
		}
		public void visitLeave(FuncParamTypeNode node) {
			defaultVisitLeave(node);
		}
		public void visitEnter(FuncParamNode node) {
			defaultVisitEnter(node);
		}
		public void visitLeave(FuncParamNode node) {
			defaultVisitLeave(node);
		}		
		

		public void visit(FuncTypeNode node) {
			defaultVisitForLeaf(node);
		}
		public void visit(BooleanConstantNode node) {
			defaultVisitForLeaf(node);
		}
		public void visit(ErrorNode node) {
			defaultVisitForLeaf(node);
		}
//		public void visit(IdentifierNode node) {
//			defaultVisitForLeaf(node);
//		}
		public void visit(IntegerConstantNode node) {
			defaultVisitForLeaf(node);
		}
		public void visit(FloatConstantNode node) {
			defaultVisitForLeaf(node);
		}
		public void visit(StringConstantNode node) {
			defaultVisitForLeaf(node);
		}
		public void visit(CharacterConstantNode node) {
			defaultVisitForLeaf(node);
		}
		public void visit(NewlineNode node) {
			defaultVisitForLeaf(node);
		}	
		public void visit(SpaceNode node) {
			defaultVisitForLeaf(node);
		}
		public void visit(TabNode node) {
			defaultVisitForLeaf(node);
		}
	}
}
