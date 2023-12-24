package parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import logging.TanLogger;
import parseTree.*;
import semanticAnalyzer.types.ArrayType;
import semanticAnalyzer.types.PrimitiveType;
import semanticAnalyzer.types.Type;
import semanticAnalyzer.types.*;
import tokens.*;
import parseTree.nodeTypes.*;
import lexicalAnalyzer.Keyword;
import lexicalAnalyzer.Lextant;
import lexicalAnalyzer.Punctuator;
import lexicalAnalyzer.Scanner;

public class Parser {
	private Scanner scanner;
	private Token nowReading;
	private Token previouslyRead;

	public static ParseNode parse(Scanner scanner) {
		Parser parser = new Parser(scanner);
		return parser.parse();
	}

	public Parser(Scanner scanner) {
		super();
		this.scanner = scanner;
	}

	public ParseNode parse() {
		readToken();
		return parseProgram();
	}

	////////////////////////////////////////////////////////////
	// "program" is the start symbol S
	// S -> globalDefinition* MAIN blockStatement

	private ParseNode parseProgram() {
		if (!startsProgram(nowReading)) {
			return syntaxErrorNode("program");
		}
		ParseNode program = new ProgramNode(nowReading);

		// globalDefinition*
		parseGlobalDefinition(program);

		// MAIN
		expect(Keyword.MAIN);

		// blockStatement
		ParseNode mainBlock = parseMainBlock();
		program.appendChild(mainBlock);

		if (!(nowReading instanceof NullToken)) {
			return syntaxErrorNode("end of program");
		}

		return program;
	}

	private boolean startsProgram(Token token) {
		return token.isLextant(Keyword.MAIN) || startsGlobalDefinition(token);
	}

	// globalDefinition -> functionDefinition
	private void parseGlobalDefinition (ParseNode parent) {
		List<ParseNode> functionDefinitions = new ArrayList<ParseNode>();

		// globalDefinition*
		while (startsGlobalDefinition(nowReading)) {			
			if (startsFunctionDefinition(nowReading)) {
				ParseNode functionDefinition = parseFunctionDefinition();
				functionDefinitions.add(functionDefinition);
			}
		}
		
		// Append function definitions
		for (ParseNode node : functionDefinitions) {
			parent.appendChild(node);
		}
	}

	private boolean startsGlobalDefinition(Token token){
		return startsFunctionDefinition(token);
	}

	///////////////////////////////////////////////////////////
	// mainBlock

	// mainBlock -> { statement* }
	private ParseNode parseMainBlock() {
		if (!startsMainBlock(nowReading)) {
			return syntaxErrorNode("mainBlock");
		}
		ParseNode mainBlock = new MainBlockNode(nowReading);
		expect(Punctuator.OPEN_BRACE);

		while (startsStatement(nowReading)) {
			ParseNode statement = parseStatement();
			mainBlock.appendChild(statement);
		}
		expect(Punctuator.CLOSE_BRACE);
		return mainBlock;
	}

	private boolean startsMainBlock(Token token) {
		return token.isLextant(Punctuator.OPEN_BRACE);
	}

	///////////////////////////////////////////////////////////
	// statements
	
	// statement-> declaration | printStmt | assignmentStatement | blockStatement
	private ParseNode parseStatement() {
		if (!startsStatement(nowReading)) {
			return syntaxErrorNode("statement");
		}
		if (startsFunctionDefinition(nowReading)) {
			return parseFunctionDefinition();
		}
		if (startsDeclaration(nowReading)) {
			return parseDeclaration();
		}
		if (startsCallStatement(nowReading)) {
			return parseCallStatement();
		}
		if (startsReturnStatement(nowReading)) {
			return parseReturnStatement();
		}
		if (startsIfStatement(nowReading)) {
			return parseIfStatement();
		}
		if (startsWhileStatement(nowReading)) {
			return parseWhileStatement();
		}
		if (startsPrintStatement(nowReading)) {
			return parsePrintStatement();
		}
		if (startsAssignmentStatement(nowReading)) {
			return parseAssignmentStatement();
		}
		if(startsBlockStatement(nowReading)) {
			return parseBlockStatement();
		}
		return syntaxErrorNode("statement");
	}

	
	private boolean startsStatement(Token token) {
		return startsFunctionDefinition(token) || 
			   startsPrintStatement(token) ||
			   startsDeclaration(token) ||
			   startsAssignmentStatement(token) ||
			   startsCallStatement(nowReading) ||
			   startsReturnStatement(nowReading) ||
			   startsIfStatement(token) ||
			   startsWhileStatement(token) ||
			   startsBlockStatement(token);
	}

	// functionDefinition -> subr type identifier (parameterList) blockStatement
	private ParseNode parseFunctionDefinition() {
		if (!startsFunctionDefinition(nowReading)) {
			return syntaxErrorNode("function definition");
		}

		// subr
		expect(Keyword.SUBR);

		Token functoken = previouslyRead;

		// type
		Type returnType = parseFunctionType();

		// identifier            
		ParseNode identifier = parseIdentifier();

		// parameterList + blockStatement
		ParseNode FunctionDef = parseFunction(returnType);

		// Create Function def Node
		ParseNode node = FuncDefNode.withChildren(functoken, identifier, FunctionDef);

		return node;
	}

	private boolean startsFunctionDefinition(Token token) {
		return token.isLextant(Keyword.SUBR);
	}

	private ParseNode parseFunction(Type returnType) {
		if (!startsFunction(nowReading)) {
			return syntaxErrorNode("function");
		}

		Token functoken = previouslyRead;
		ParseNode parameterList = parseParameterList(returnType);
		ParseNode statement = parseBlockStatement();

		// Create Function Node
		ParseNode FuncNode = FunctionNode.withChildren(functoken, parameterList, statement);

		return FuncNode;
	}

	private boolean startsFunction(Token token) {
		return token.isLextant(Punctuator.OPEN_PARENTHESIS);
	}
 
	// parameterList -> (type identifier ⋈ ,) 
	private ParseNode parseParameterList(Type returnType) {

		FuncParamTypeNode node = new FuncParamTypeNode(previouslyRead);

		// (
		expect(Punctuator.OPEN_PARENTHESIS);

		// type identifier ⋈ ,
		while(!nowReading.isLextant(Punctuator.CLOSE_PARENTHESIS)) {
			// ,
			if(nowReading.isLextant(Punctuator.COMMA)) {
				readToken();
			}

			// type identifier	
			if(startsType(nowReading)) {

				// type
				Type type = parseFunctionType();

				Token token = nowReading;

				// identifier
				ParseNode identifier = parseIdentifier();

				FuncParamNode child = FuncParamNode.withChildren(token, type, identifier);

				//save child
				node.appendChild(child);

			} else {
				node.appendChild(syntaxErrorNode("parameter list"));
			}
		}

		// )
		expect(Punctuator.CLOSE_PARENTHESIS);

		node.setType(returnType);

		return node;
	}

	// callStatement -> CALL functionInvocation TERMINATOR
	private ParseNode parseCallStatement() {
		if (!startsCallStatement(nowReading)) {
			return syntaxErrorNode("call statement");
		}

		Token callToken = nowReading;
		readToken();
		
		ParseNode identifier = parseIdentifier();

		ParseNode callStatement = parseFunctionInvocation(identifier);

		expect(Punctuator.TERMINATOR);
		return CallNode.withChild(callToken, callStatement);
	}

	// identifier( expression (, expression)*)
	private ParseNode parseFunctionInvocation( ParseNode node) {
		while (nowReading.isLextant(Punctuator.OPEN_PARENTHESIS)) {
			// Check for function invocation
			if (nowReading.isLextant(Punctuator.OPEN_PARENTHESIS)) {
				ParseNode invocation = new FuncInvocNode(node);
				invocation.appendChild(node);
				node = parseArguments(invocation);
			}
		}
		
		return node;
	}

	private boolean startsCallStatement(Token token) {
		return token.isLextant(Keyword.CALL);
	}

	// returnStatement -> RETURN expression TERMINATOR
	private ParseNode parseReturnStatement() {
		if (!startsReturnStatement(nowReading)) {
			return syntaxErrorNode("return statement");
		}

		Token returnToken = nowReading;

		readToken();

		if (nowReading.isLextant(Punctuator.TERMINATOR)) {
			readToken();
			return new ReturnNode(returnToken);
		}
		
		ParseNode returnStatement = parseExpression();

		expect(Punctuator.TERMINATOR);
		return ReturnNode.withChild(returnToken, returnStatement);
	}

	private boolean startsReturnStatement(Token token) {
		return token.isLextant(Keyword.RETURN);
	}
	
	private ParseNode parseWhileStatement() {
		if(!startsWhileStatement(nowReading)) {
			return syntaxErrorNode("while statement");
		}
		// while (expression) {block} 
		expect(Keyword.WHILE);
		
		Token while_Token = previouslyRead;
		ParseNode expression = parseExpression();
		ParseNode block = parseBlockStatement();
		
		ParseNode While = new WhileNode(while_Token, expression, block); 
		return While;
	}
	
	private boolean startsWhileStatement(Token token) {
		return token.isLextant(Keyword.WHILE);
	}
	
	private ParseNode parseIfStatement() {
		if(!startsIfStatement(nowReading)) {
			return syntaxErrorNode("if statement");
		}
		// if (expression) {block} 
		expect(Keyword.IF);
		
		Token if_Token = previouslyRead;
		ParseNode expression = parseExpression();
		ParseNode block = parseBlockStatement();
		
		// else {block}
		if(startsElseStatement(nowReading)) {
			expect(Keyword.ELSE);
			ParseNode elseBlock = parseBlockStatement();
			return IfNode.withChildren(if_Token, expression, block, elseBlock);
		}
		else {
			ParseNode If = new IfNode(if_Token, expression, block); 
			return If;
		}
	}
	
	private boolean startsIfStatement(Token token) {
		return token.isLextant(Keyword.IF);
	}
	private boolean startsElseStatement(Token token) {
		return token.isLextant(Keyword.ELSE);
	}
	
	private ParseNode parseBlockStatement() {
		if(!startsBlockStatement(nowReading)) {
			return syntaxErrorNode("block statement");
		}
		ParseNode Block = new BlockStatementNode(nowReading);
		expect(Punctuator.OPEN_BRACE);
		
		while(startsStatement(nowReading)) {
			ParseNode statement = parseStatement();
			Block.appendChild(statement);
		}
		expect(Punctuator.CLOSE_BRACE);
		return Block;
		
	}
	
	private boolean startsBlockStatement(Token token) {
		return token.isLextant(Punctuator.OPEN_BRACE);
	}
	
	// printStmt -> PRINT printExpressionList TERMINATOR
	private ParseNode parsePrintStatement() {
		if (!startsPrintStatement(nowReading)) {
			return syntaxErrorNode("print statement");
		}
		ParseNode result = new PrintStatementNode(nowReading);

		readToken();
		result = parsePrintExpressionList(result);

		expect(Punctuator.TERMINATOR);
		return result;
	}

	private boolean startsPrintStatement(Token token) {
		return token.isLextant(Keyword.PRINT);
	}

	// This adds the printExpressions it parses to the children of the given parent
	// printExpressionList -> printSeparator* (expression printSeparator+)*
	// expression? (note that this is nullable)

	private ParseNode parsePrintExpressionList(ParseNode parent) {
		if (!startsPrintExpressionList(nowReading)) {
			return syntaxErrorNode("printExpressionList");
		}

		while (startsPrintSeparator(nowReading)) {
			parsePrintSeparator(parent);
		}
		while (startsExpression(nowReading)) {
			parent.appendChild(parseExpression());
			if (nowReading.isLextant(Punctuator.TERMINATOR)) {
				return parent;
			}
			do {
				parsePrintSeparator(parent);
			} while (startsPrintSeparator(nowReading));
		}
		return parent;
	}

	private boolean startsPrintExpressionList(Token token) {
		return startsExpression(token) || startsPrintSeparator(token) || token.isLextant(Punctuator.TERMINATOR);
	}

	// This adds the printSeparator it parses to the children of the given parent
	// printSeparator -> PRINT_SEPARATOR | PRINT_SPACE | PRINT_NEWLINE | PRINT_TAB

	private void parsePrintSeparator(ParseNode parent) {
		if (!startsPrintSeparator(nowReading)) {
			ParseNode child = syntaxErrorNode("print separator");
			parent.appendChild(child);
			return;
		}

		if (nowReading.isLextant(Punctuator.PRINT_NEWLINE)) {
			readToken();
			ParseNode child = new NewlineNode(previouslyRead);
			parent.appendChild(child);
		} else if (nowReading.isLextant(Punctuator.PRINT_SPACE)) {
			readToken();
			ParseNode child = new SpaceNode(previouslyRead);
			parent.appendChild(child);
		} else if (nowReading.isLextant(Punctuator.PRINT_TAB)) {
			readToken();
			ParseNode child = new TabNode(previouslyRead);
			parent.appendChild(child);
		} else if (nowReading.isLextant(Punctuator.PRINT_SEPARATOR)) {
			readToken();
		}
	}

	private boolean startsPrintSeparator(Token token) {
		return token.isLextant(Punctuator.PRINT_SEPARATOR, Punctuator.PRINT_SPACE, Punctuator.PRINT_NEWLINE,
				Punctuator.PRINT_TAB);
	}

	// declaration -> CONST identifier := expression TERMINATOR
	private ParseNode parseDeclaration() {
		if (!startsDeclaration(nowReading)) {
			return syntaxErrorNode("declaration");
		}
		Token declarationToken = nowReading;
		readToken();
		ParseNode identifier = parseIdentifier();
		expect(Punctuator.ASSIGN);
		ParseNode initializer = parseExpression();
		initializer = parseFunctionInvocation(initializer);
		expect(Punctuator.TERMINATOR);

		return DeclarationNode.withChildren(declarationToken, identifier, initializer);
	}

	private boolean startsDeclaration(Token token) {
		return token.isLextant(Keyword.CONST, Keyword.VAR);
	}

	// assignmentStmt -> target := expression TERMINATOR
	private ParseNode parseAssignmentStatement() {
		if (!startsAssignmentStatement(nowReading)) {
			return syntaxErrorNode("assignment");
		}
		Token assignmentToken = nowReading;

		ParseNode target = parseIdentifier();
		expect(Punctuator.ASSIGN);
		ParseNode expression = parseExpression();
		expect(Punctuator.TERMINATOR);
		return AssignmentStatementNode.withChildren(assignmentToken, target, expression);
	}

	// Check if identifier
	private boolean startsAssignmentStatement(Token token) {
		return startsIdentifier(token);
	}

	///////////////////////////////////////////////////////////
	// expressions
	// expr -> comparisonExpression
	// comparisonExpression -> additiveExpression [> additiveExpression]?
	// additiveExpression -> multiplicativeExpression [+ multiplicativeExpression]*
	/////////////////////////////////////////////////////////// (left-assoc)
	// multiplicativeExpression -> atomicExpression [MULT atomicExpression]*
	/////////////////////////////////////////////////////////// (left-assoc)
	// atomicExpression -> unaryExpression | (expression) | literal
	// typeCastExpression -> <type>(expression)
	// unaryExpression -> UNARYOP atomicExpression
	// booleanOperator -> && | ||
	// literal -> intNumber | identifier | booleanConstant | charConstant

	// expr -> comparisonExpression
	private ParseNode parseExpression() {
		if (!startsExpression(nowReading)) {
			return syntaxErrorNode("expression");
		}
		return parseComparisonExpression();
	}

	private boolean startsExpression(Token token) {
		return startsComparisonExpression(token);
	}

	// comparisonExpression -> additiveExpression [>|>=|==|!=|<|<= | && | ||
	// additiveExpression]?
	private ParseNode parseComparisonExpression() {
		if (!startsComparisonExpression(nowReading)) {
			return syntaxErrorNode("comparison expression");
		}

		ParseNode left = parseAdditiveExpression();
		if (nowReading.isLextant(Punctuator.GREATER, Punctuator.GREATEREQUAL, Punctuator.EQUALEQUAL,
				Punctuator.NOTEQUAL, Punctuator.LESSER, Punctuator.LESSEREQUAL, Punctuator.AND, Punctuator.OR)) {
			Token compareToken = nowReading;
			readToken();
			ParseNode right = parseAdditiveExpression();

			return OperatorNode.withChildren(compareToken, left, right);
		}
		return left;

	}

	private boolean startsComparisonExpression(Token token) {
		return startsAdditiveExpression(token);
	}

	// additiveExpression -> multiplicativeExpression [+ multiplicativeExpression]*
	// (left-assoc)
	// also subtract
	private ParseNode parseAdditiveExpression() {
		if (!startsAdditiveExpression(nowReading)) {
			return syntaxErrorNode("additiveExpression");
		}

		ParseNode left = parseMultiplicativeExpression();
		while (nowReading.isLextant(Punctuator.ADD, Punctuator.SUBTRACT)) {
			Token additiveToken = nowReading;
			readToken();
			ParseNode right = parseMultiplicativeExpression();

			left = OperatorNode.withChildren(additiveToken, left, right);
		}
		return left;
	}

	private boolean startsAdditiveExpression(Token token) {
		return startsMultiplicativeExpression(token);
	}

	// multiplicativeExpression -> atomicExpression [MULT atomicExpression]*
	// (left-assoc)
	private ParseNode parseMultiplicativeExpression() {
		if (!startsMultiplicativeExpression(nowReading)) {
			return syntaxErrorNode("multiplicativeExpression");
		}

		ParseNode left = parseAtomicExpression();
		while (nowReading.isLextant(Punctuator.MULTIPLY, Punctuator.DIVIDE)) {
			Token multiplicativeToken = nowReading;
			readToken();
			ParseNode right = parseAtomicExpression();

			left = OperatorNode.withChildren(multiplicativeToken, left, right);
		}
		return left;
	}

	private boolean startsMultiplicativeExpression(Token token) {
		return startsAtomicExpression(token);
	}

	// atomicExpression -> unaryExpression | literal | indexing
	private ParseNode parseAtomicExpression() {
		if (!startsAtomicExpression(nowReading)) {
			return syntaxErrorNode("atomic expression");
		}
		if (startsUnaryExpression(nowReading)) {
			return parseUnaryExpression();
		}
		if (startsParenthesisExpression(nowReading)) {
			return parseParenthesisExpression();
		}
		if (startsTypeCastExpression(nowReading)) {
			return parseTypeCastExpression();
		}
		if (startsArrayExpression(nowReading)) {
			return parseArrayExpression();
		}
		return parseLiteral();
	}

	private boolean startsAtomicExpression(Token token) {
		return startsLiteral(token) || startsUnaryExpression(token) || startsParenthesisExpression(token)
				|| startsTypeCastExpression(token) || startsArrayExpression(token);
	}
	
	// new [type] (expression)
	private ParseNode parseArrayExpression() {
		if (!startsArrayExpression(nowReading)) {
			return syntaxErrorNode("array expression");
		}
		// new [type] (expression)
		if(nowReading.isLextant(Keyword.NEW)) {
			expect(Keyword.NEW);
	
			Token token = previouslyRead;
	
			ArrayType type = parseArrayType();
	
			expect(Punctuator.OPEN_PARENTHESIS);
			ParseNode inside = parseExpression();
			expect(Punctuator.CLOSE_PARENTHESIS);
	
			boolean reference = true;
			return ArrayNode.withChildren(token, type, inside, reference);
			}
		
		// [expression]
		expect(Punctuator.OPEN_BRACKET);
		ParseNode inside = parseExpression();
		Token identifier = previouslyRead;		// PreviouslyRead expression1
		expect(Punctuator.CLOSE_BRACKET, Punctuator.COMMA, Punctuator.INDEX);
		if(previouslyRead.isLextant(Punctuator.CLOSE_BRACKET, Punctuator.COMMA)) {
			return parseArrayExpression(inside);
		}
		// Identifier
		else {
			ParseNode index = parseIndex();
			return IdentifierNode.withChildren(identifier, index);
		}
	}
	
	private ParseNode parseArrayExpression(ParseNode inside) {
		if (!previouslyRead.isLextant(Punctuator.CLOSE_BRACKET, Punctuator.COMMA)) {
			return syntaxErrorNode("closed array");
		}
		Token token = Keyword.NEW.prototype();
		ArrayNode arr = ArrayNode.withChildren(token, inside, false);

		if (!previouslyRead.isLextant(Punctuator.CLOSE_BRACKET)) {
			arr = parseArrayExpressionList(arr);
			expect(Punctuator.CLOSE_BRACKET);
		}

		return arr;
	}
	
	private ArrayNode parseArrayExpressionList(ArrayNode cascade) {
		while(startsExpression(nowReading) || nowReading.isLextant(Punctuator.COMMA)) {
			if (startsExpression(nowReading)) {
				ParseNode child = parseExpression();
				cascade.appendChild(child);
			}

			if (nowReading.isLextant(Punctuator.COMMA)) {
				readToken();
			}
		}
		return cascade;
	}
	
	private ArrayType parseArrayType() {
		expect(Punctuator.OPEN_BRACKET);
		ArrayType type = new ArrayType();

		PrimitiveType subtype = PrimitiveType.fromToken(nowReading);
		// Multiple arrays within arrays
		if (subtype == PrimitiveType.ARRAY) {
			type.setSubtype(parseArrayType());	
		} else {
			type.setSubtype(subtype);
			readToken();
		}

		expect(Punctuator.CLOSE_BRACKET);

		return type;
	}
	
	private boolean startsArrayExpression(Token token) {
		return token.isLextant(Punctuator.OPEN_BRACKET) || token.isLextant(Keyword.NEW);
	}

	private ParseNode parseParenthesisExpression() {
		if (!startsParenthesisExpression(nowReading)) {
			return syntaxErrorNode("parenthesis expression");
		}
		expect(Punctuator.OPEN_PARENTHESIS);
		ParseNode inside = parseExpression();
		expect(Punctuator.CLOSE_PARENTHESIS);
		return inside;
	}

	private boolean startsParenthesisExpression(Token token) {
		return token.isLextant(Punctuator.OPEN_PARENTHESIS);
	}

	// unaryExpression -> UNARYOP atomicExpression
	private ParseNode parseUnaryExpression() {
		if (!startsUnaryExpression(nowReading)) {
			return syntaxErrorNode("unary expression");
		}
		Token operatorToken = nowReading;
		readToken();
		ParseNode child = parseAtomicExpression();

		return OperatorNode.withChildren(operatorToken, child);
	}

	private boolean startsUnaryExpression(Token token) {
		return token.isLextant(Punctuator.SUBTRACT, Punctuator.ADD, Punctuator.NOT, Keyword.LENGTH);
	}

	// typeCastExpression -> <type>(expression)
	private ParseNode parseTypeCastExpression() {
		if (!startsTypeCastExpression(nowReading)) {
			return syntaxErrorNode("type cast expression");
		}

		// <type>
		expect(Punctuator.LESSER);
		ParseNode type = parseType();
		Token castToken = LextantToken.make(nowReading, "<>()", Punctuator.CAST);
		expect(Punctuator.GREATER);

		// (expression)
		expect(Punctuator.OPEN_PARENTHESIS);
		ParseNode expression = parseExpression();
		expect(Punctuator.CLOSE_PARENTHESIS);

		return OperatorNode.withChildren(castToken, type, expression);
	}

	private boolean startsTypeCastExpression(Token token) {
		return token.isLextant(Punctuator.LESSER);
	}

	// type -> bool | char | string | int | float
	private ParseNode parseType() {
		if (!startsType(nowReading)) {
			return syntaxErrorNode("type");
		}
		readToken();
		return new TypeCastingNode(previouslyRead);
	}

	private boolean startsType(Token token) {
		return token.isLextant(Keyword.BOOL, Keyword.CHAR, Keyword.STRING, Keyword.INT, Keyword.FLOAT);
	}

	private Type parseFunctionType() {
		if (startsFunctionType(nowReading)) {
			Type type = PrimitiveType.fromToken(nowReading);
			readToken();
			return type;
		}
		return PrimitiveType.ERROR;
	}

	private boolean startsFunctionType(Token token) {
		return token.isLextant(Keyword.BOOL, Keyword.CHAR, Keyword.STRING, Keyword.INT, Keyword.FLOAT, Keyword.VOID);
	}
	

	// literal -> number | identifier | booleanConstant
	private ParseNode parseLiteral() {
		if (!startsLiteral(nowReading)) {
			return syntaxErrorNode("literal");
		}

		if (startsIntLiteral(nowReading)) {
			return parseIntLiteral();
		}
		if (startsFloatLiteral(nowReading)) {
			return parseFloatLiteral();
		}
		if (startsIdentifier(nowReading)) {
			
			ParseNode identifer = parseIdentifier();
			identifer = parseFunctionInvocation(identifer);
			
			return identifer;
		}
		if (startsBooleanLiteral(nowReading)) {
			return parseBooleanLiteral();
		}
		if (startsStringLiteral(nowReading)) {
			return parseStringLiteral();
		}
		if (startsCharLiteral(nowReading)) {
			return parseCharLiteral();
		} 

		return syntaxErrorNode("literal");
	}

	private boolean startsLiteral(Token token) {
		return startsIntLiteral(token) || startsFloatLiteral(token) ||
				startsIdentifier(token) || startsBooleanLiteral(token) ||
				startsStringLiteral(token) || startsCharLiteral(token) || startsFunctionType(token);
	}

	// number (literal)
	private ParseNode parseIntLiteral() {
		if (!startsIntLiteral(nowReading)) {
			return syntaxErrorNode("integer constant");
		}
		readToken();
		return new IntegerConstantNode(previouslyRead);
	}

	private boolean startsIntLiteral(Token token) {
		return token instanceof NumberToken;
	}

	// floating (literal)
	private ParseNode parseFloatLiteral() {
		if (!startsFloatLiteral(nowReading)) {
			return syntaxErrorNode("float constant");
		}
		readToken();
		return new FloatConstantNode(previouslyRead);
	}

	private boolean startsFloatLiteral(Token token) {
		return token instanceof FloatingLiteralToken;
	}

	// string (literal)
	private ParseNode parseStringLiteral() {
		if (!startsStringLiteral(nowReading)) {
			return syntaxErrorNode("string constant");
		}
		readToken();
		return new StringConstantNode(previouslyRead);
	}

	private boolean startsStringLiteral(Token token) {
		return token instanceof StringToken;
	}

	// char (literal)
	private ParseNode parseCharLiteral() {
		if (!startsCharLiteral(nowReading)) {
			return syntaxErrorNode("char constant");
		}
		readToken();
		return new CharacterConstantNode(previouslyRead);
	}

	private boolean startsCharLiteral(Token token) {
		return token instanceof CharacterToken;
	}

	// identifier (terminal)
	private ParseNode parseIdentifier() {
		if (!startsIdentifier(nowReading) && !previouslyArrayType(previouslyRead)) {
			return syntaxErrorNode("identifier");
		}
		// [array[T] : int]
		readToken();
		if(nowReading instanceof ArrayType || nowReading instanceof IdentifierToken) {
			Token identifier = nowReading;
			readToken();
			expect(Punctuator.INDEX);
			ParseNode index = parseIndex();
			return IdentifierNode.withChildren(identifier, index);
		} else if (nowReading.isLextant(Punctuator.OPEN_PARENTHESIS)){
			Token identifier = previouslyRead;
			ParseNode node = new IdentifierNode(identifier);
			// node = parseFunctionInvocation(node)
			return node;
		} else {
			return new IdentifierNode(previouslyRead);
		}
	}

	private ParseNode parseArguments(ParseNode node) {
		expect(Punctuator.OPEN_PARENTHESIS);

		while (!nowReading.isLextant(Punctuator.CLOSE_PARENTHESIS)) {
			if (nowReading.isLextant(Punctuator.COMMA)) {
				readToken();
			}

			ParseNode child = parseExpression();
			node.appendChild(child);
		}

		expect(Punctuator.CLOSE_PARENTHESIS);
		return node;
	}

	private boolean startsIdentifier(Token token) {
		//return token instanceof IdentifierToken
		return token instanceof IdentifierToken || token.isLextant(Punctuator.OPEN_BRACKET);
	}
	
	private boolean previouslyArrayType(Token token) {
		return token instanceof ArrayType;
	}

	private ParseNode parseIndex() {
		ParseNode index = parseExpression();	
		expect(Punctuator.CLOSE_BRACKET);
		return index;
	}
	
	// boolean literal
	private ParseNode parseBooleanLiteral() {
		if (!startsBooleanLiteral(nowReading)) {
			return syntaxErrorNode("boolean constant");
		}
		readToken();
		return new BooleanConstantNode(previouslyRead);
	}

	private boolean startsBooleanLiteral(Token token) {
		return token.isLextant(Keyword.TRUE, Keyword.FALSE);
	}

	private void readToken() {
		previouslyRead = nowReading;
		nowReading = scanner.next();
	}

	// if the current token is one of the given lextants, read the next token.
	// otherwise, give a syntax error and read next token (to avoid endless
	// looping).
	private void expect(Lextant... lextants) {
		if (!nowReading.isLextant(lextants)) {
			syntaxError(nowReading, "expecting " + Arrays.toString(lextants));
		}
		readToken();
	}

	private ErrorNode syntaxErrorNode(String expectedSymbol) {
		syntaxError(nowReading, "expecting " + expectedSymbol);
		ErrorNode errorNode = new ErrorNode(nowReading);
		readToken();
		return errorNode;
	}

	private void syntaxError(Token token, String errorDescription) {
		String message = "" + token.getLocation() + " " + errorDescription;
		error(message);
	}

	private void error(String message) {
		TanLogger log = TanLogger.getLogger("compiler.Parser");
		log.severe("syntax error: " + message);
	}
}
