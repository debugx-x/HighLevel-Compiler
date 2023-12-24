package semanticAnalyzer;

import parseTree.*;


public class SemanticAnalyzer {
	ParseNode ASTree;
	
	public static ParseNode analyze(ParseNode ASTree) {
		SemanticAnalyzer analyzer = new SemanticAnalyzer(ASTree);
		PreSemanticAnalysisVisitor spv = new PreSemanticAnalysisVisitor();
		
		ASTree.accept(spv);
		return analyzer.analyze();
	}
	public SemanticAnalyzer(ParseNode ASTree) {
		this.ASTree = ASTree;
	}
	
	public ParseNode analyze() {
		SemanticAnalysisVisitor visitor = new SemanticAnalysisVisitor();
		ASTree.accept(visitor);
		visitor.promotion.promote();
		
		return ASTree;
	}
}
