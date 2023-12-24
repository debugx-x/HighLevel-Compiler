package asmCodeGenerator;

import java.util.*;

import asmCodeGenerator.codeStorage.ASMCodeFragment;
import asmCodeGenerator.codeStorage.ASMCodeFragment.CodeType;
import asmCodeGenerator.codeStorage.ASMCodeChunk;
import asmCodeGenerator.codeStorage.ASMOpcode;
import asmCodeGenerator.operators.ArrayLengthCodeGenerator;
import asmCodeGenerator.operators.FunctionReturnStoreCodeGenerator;
import asmCodeGenerator.operators.FuncArgStoreCodeGenerator;
import asmCodeGenerator.operators.SimpleCodeGenerator;
import asmCodeGenerator.runtime.MemoryManager;
import asmCodeGenerator.runtime.RunTime;
import lexicalAnalyzer.Keyword;
import lexicalAnalyzer.Lextant;
import lexicalAnalyzer.Punctuator;
import parseTree.*;
import parseTree.nodeTypes.*;
import semanticAnalyzer.types.ArrayType;
import semanticAnalyzer.types.PrimitiveType;
import semanticAnalyzer.types.Type;
import symbolTable.Binding;
import symbolTable.Scope;
import static asmCodeGenerator.codeStorage.ASMCodeFragment.CodeType.*;
import static asmCodeGenerator.codeStorage.ASMOpcode.*;

// do not call the code generator if any errors have occurred during analysis.
public class ASMCodeGenerator {
	ParseNode root;
	List<IdentifierNode> functionDefs;
	ASMCodeFragment functions;

	public static ASMCodeFragment generate(ParseNode syntaxTree) {
		ASMCodeGenerator codeGenerator = new ASMCodeGenerator(syntaxTree);
		return codeGenerator.makeASM();
	}

	public ASMCodeGenerator(ParseNode root) {
		super();
		this.root = root;
		this.functionDefs = new ArrayList<IdentifierNode>();
		this.functions = new ASMCodeFragment(GENERATES_VOID);
	}

	public ASMCodeFragment makeASM() {
		ASMCodeFragment code = new ASMCodeFragment(GENERATES_VOID);

		code.append(MemoryManager.codeForInitialization());
		code.append(callStackBlockASM());
		code.append(RunTime.getEnvironment());
		code.append(globalVariableBlockASM());
		code.append(programASM());
		code.append(MemoryManager.codeForAfterApplication());

		return code;
	}

	private ASMCodeFragment callStackBlockASM() {
		ASMCodeFragment code = new ASMCodeFragment(GENERATES_VOID);

		code.add(DLabel, RunTime.STACK_POINTER);
		code.add(DataI, 0);
		code.add(DLabel, RunTime.FRAME_POINTER);
		code.add(DataI, 0);

		code.add(PushD, RunTime.STACK_POINTER);
		code.add(Memtop);
		code.add(StoreI);

		code.add(PushD, RunTime.FRAME_POINTER);
		code.add(Memtop);
		code.add(StoreI);

		return code;
	}

	private ASMCodeFragment globalVariableBlockASM() {
		assert root.hasScope();
		Scope scope = root.getScope();
		int globalBlockSize = scope.getAllocatedSize();

		ASMCodeFragment code = new ASMCodeFragment(GENERATES_VOID);
		code.add(DLabel, RunTime.GLOBAL_MEMORY_BLOCK);
		code.add(DataZ, globalBlockSize);
		return code;
	}

	private ASMCodeFragment programASM() {
		ASMCodeFragment code = new ASMCodeFragment(GENERATES_VOID);

		// code.add(Label, RunTime.MAIN_PROGRAM_LABEL);
		code.append(programCode());
		code.add(Halt, "", "%% End of Execution");
		code.append(functions);
		return code;
	}

	private ASMCodeFragment programCode() {
		CodeVisitor visitor = new CodeVisitor();
		root.accept(visitor);
		return visitor.removeRootCode(root);
	}

	protected class CodeVisitor extends ParseNodeVisitor.Default {
		private Map<ParseNode, ASMCodeFragment> codeMap;
		ASMCodeFragment code;

		public CodeVisitor() {
			codeMap = new HashMap<ParseNode, ASMCodeFragment>();
		}

		////////////////////////////////////////////////////////////////////
		// Make the field "code" refer to a new fragment of different sorts.
		private void newAddressCode(ParseNode node) {
			code = new ASMCodeFragment(GENERATES_ADDRESS);
			codeMap.put(node, code);
		}

		private void newValueCode(ParseNode node) {
			code = new ASMCodeFragment(GENERATES_VALUE);
			codeMap.put(node, code);
		}

		private void newVoidCode(ParseNode node) {
			code = new ASMCodeFragment(GENERATES_VOID);
			codeMap.put(node, code);
		}

		////////////////////////////////////////////////////////////////////
		// Get code from the map.
		private ASMCodeFragment getAndRemoveCode(ParseNode node) {
			ASMCodeFragment result = codeMap.get(node);
			codeMap.remove(node);
			return result;
		}

		public ASMCodeFragment removeRootCode(ParseNode tree) {
			return getAndRemoveCode(tree);
		}

		ASMCodeFragment removeValueCode(ParseNode node) {
			ASMCodeFragment frag = getAndRemoveCode(node);
			makeFragmentValueCode(frag, node);
			return frag;
		}

		private ASMCodeFragment removeAddressCode(ParseNode node) {
			ASMCodeFragment frag = getAndRemoveCode(node);
			assert frag.isAddress();
			return frag;
		}

		ASMCodeFragment removeVoidCode(ParseNode node) {
			ASMCodeFragment frag = getAndRemoveCode(node);
			assert frag.isVoid();
			return frag;
		}

		////////////////////////////////////////////////////////////////////
		// convert code to value-generating code.
		private void makeFragmentValueCode(ASMCodeFragment code, ParseNode node) {
			assert !code.isVoid();

			if (code.isAddress()) {
				turnAddressIntoValue(code, node);
			}
		}

		private void turnAddressIntoValue(ASMCodeFragment code, ParseNode node) {
			if (node.getType() == PrimitiveType.INTEGER || node.getType() == PrimitiveType.STRING
					|| node.getType() instanceof ArrayType) {
				code.add(LoadI);
			} else if (node.getType() == PrimitiveType.FLOAT) {
				code.add(LoadF);
			} else if (node.getType() == PrimitiveType.BOOLEAN || node.getType() == PrimitiveType.CHARACTER) {
				code.add(LoadC);
			} else {
				assert false : "node " + node;
			}
			code.markAsValue();
		}

		////////////////////////////////////////////////////////////////////
		// ensures all types of ParseNode in given AST have at least a visitLeave
		public void visitLeave(ParseNode node) {
			assert false : "node " + node + " not handled in ASMCodeGenerator";
		}

		///////////////////////////////////////////////////////////////////////////
		// constructs larger than statements
		public void visitLeave(ProgramNode node) {
			newVoidCode(node);
			for (ParseNode child : node.getChildren()) {
				ASMCodeFragment childCode = removeVoidCode(child);
				code.append(childCode);
			}
		}

		public void visitLeave(MainBlockNode node) {
			newVoidCode(node);
			code.add(Label, RunTime.MAIN_PROGRAM_LABEL);

			// store function definitions
			for (IdentifierNode identifier : functionDefs) {
				String label = identifier.getBinding().getLabel();
				ASMCodeFragment address = removeAddressCode(identifier);
				code.append(address);  // [ 275 ] 275 = identifier address
				code.add(PushD, label); // [ 275 61 ] 61 = identifier binding label address
				code.add(StoreI);  // []
			}

			for (ParseNode child : node.getChildren()) {
				ASMCodeFragment childCode = removeVoidCode(child);
				code.append(childCode);
			}
		}

		public void visitLeave(BlockStatementNode node) {
			newVoidCode(node);
			for (ParseNode child : node.getChildren()) {
				ASMCodeFragment childCode = removeVoidCode(child);
				code.append(childCode);
			}
		}

		///////////////////////////////////////////////////////////////////////////
		// functions
		public void visitLeave(FuncDefNode node) {
			newVoidCode(node);

			if (node.child(0) instanceof IdentifierNode) {
				functionDefs.add((IdentifierNode) node.child(0));
			}
		}

		// FRAME AND STACK POINTERS ARE ADDRESSES
		public void visitLeave(FunctionNode node) {
			newValueCode(node);

			// Jump over lambda if it is inline
			if (!(node.getParent() instanceof FuncDefNode)) {
				// code.add(Jump, node.getJumpLabel());
			}
			functions.add(Label, node.getStartLabel());
			// Put return address on Frame Stack below Dynamic Link
			functions.add(PushD, RunTime.STACK_POINTER, "%% store return addr."); // [ 627699 6 50 20] 20 = SP address
			functions.add(LoadI); 		// [ 627699 6 50 627699 ] -4 = SP value
			functions.add(PushI, -8); // [ 627699 6 50 -4 -8 ]
			functions.add(Add); 	// [ 627699 6 50 -12 ]
			functions.add(Exchange);  // [ 627699 6 -12 50 ]
			functions.add(StoreI);   // [ 627699 6 ]  // stored return address 50 to MEM(-12)

			// Store Dynamic Link (current value of Frame Pointer) below the Stack Pointer
			functions.add(PushD, RunTime.STACK_POINTER, "%% store dyn. link");  // [ 627699 6 20 ]
			functions.add(LoadI);      // [ 627699 6 -4 ]
			functions.add(PushI, -4);  // [ 627699 6 -4 -4 ]
			functions.add(Add);        // [ 627699 6 -8 ]
			functions.add(PushD, RunTime.FRAME_POINTER);   // [ -4 6 -8 24 ]
			functions.add(LoadI);     // [ 627699 6 -8 627703 ]
			functions.add(StoreI);    // [ 627699 6 ]

			// Move Frame Pointer to Stack Pointer
			functions.add(PushD, RunTime.FRAME_POINTER, "%% move frame pointer");  // [ 627699 6 24 ]
			functions.add(PushD, RunTime.STACK_POINTER);  // [ 627699 6 24 20 ]
			functions.add(LoadI);  // [ 627699 6 24 627699 ]
			functions.add(StoreI); // [ 627699 6 ]

			// Move Stack Pointer to end of frame
			functions.add(PushD, RunTime.STACK_POINTER, "%% move stack pointer");  // [ 627703 -4 6 24 ]
			functions.add(PushD, RunTime.STACK_POINTER);  // [ 6703 -4 6 24 24 ]
			functions.add(LoadI);  // [ -4 6 24 -4 ]
			functions.add(PushI, node.getFrameSize());  // [ -4 6 24 -4 8 ]
			functions.add(Subtract);  // [ -4 6 24 -12 ]
			functions.add(StoreI);  // [ -4 6 ]

			// Lambda execution code
			ASMCodeFragment childCode = removeVoidCode(node.child(1));
			functions.append(childCode);

			// Runoff error handling
			functions.add(Label, node.getExitErrorLabel());
			functions.add(Jump, RunTime.FUNCTION_RUNOFF_RUNTIME_ERROR);

			// Exit handshake
			functions.add(Label, node.getExitHandshakeLabel());

			// Push the return address onto the accumulator stack
			functions.add(PushD, RunTime.FRAME_POINTER, "%% get return addr.");
			functions.add(LoadI);
			functions.add(PushI, -8);
			functions.add(Add);
			functions.add(LoadI);

			// Replace the Frame Pointer with the dynamic link
			functions.add(PushD, RunTime.FRAME_POINTER, "%% restore frame pointer");
			functions.add(PushD, RunTime.FRAME_POINTER);
			functions.add(LoadI);
			functions.add(PushI, -4);
			functions.add(Add);
			functions.add(LoadI);
			functions.add(StoreI);

			// Move Stack Pointer above current Parameter Scope
			functions.add(PushD, RunTime.STACK_POINTER, "%% pop frame stack");
			functions.add(PushD, RunTime.STACK_POINTER);
			functions.add(LoadI);
			functions.add(PushI, node.getFrameSize());
			functions.add(Add);
			functions.add(PushI, node.getArgSize());
			functions.add(Add);

			// Decrease the stack pointer by the return value size
			Type returnType = node.getReturnType();
			functions.add(PushI, returnType.getSize(), "%% store return val.");
			functions.add(Subtract);
			functions.add(StoreI);

			// Store return address in temp until value is stored
			functions.add(PushD, RunTime.FUNC_RETURN_ADDR_TEMP);
			functions.add(Exchange);
			functions.add(StoreI);
			// Store return value
			FunctionReturnStoreCodeGenerator generator = new FunctionReturnStoreCodeGenerator(returnType);
			ASMCodeFragment frag = generator.generate(node);
			functions.append(frag);

			// Load return address from temp
			functions.add(PushD, RunTime.FUNC_RETURN_ADDR_TEMP);
			functions.add(LoadI);
			
			functions.add(Return);

			// Push lambda jump and address onto accumulator if inline
			if (!(node.getParent() instanceof FuncDefNode)) {
				// code.add(Label, node.getJumpLabel());
				code.add(PushD, node.getStartLabel());
			}
		}

		public void visitLeave(ReturnNode node) {
			newVoidCode(node);

			// Get the return value
			if (node.nChildren() > 0 && node.getType() != PrimitiveType.VOID) {
				ASMCodeFragment returnValue = removeValueCode(node.child(0));
				code.append(returnValue);
			}

			// Jump to exit handshake
			FunctionNode lambda = (FunctionNode) node.getFunction();
			code.add(Jump, lambda.getExitHandshakeLabel());
		}

		public void visitLeave(FuncParamTypeNode node) {
		}

		public void visitLeave(FuncParamNode node) {
		}

		public void visitLeave(CallNode node) {
			newVoidCode(node);

			ASMCodeFragment lambdaCode = removeValueCode(node.child(0));
			code.append(lambdaCode);

			// Remove value if function return isn't VOID
			if (node.getType() != PrimitiveType.VOID) {
				code.add(Pop);
			}
		}

		public void visitLeave(FuncInvocNode node) {
			newValueCode(node);

			// Push arguments onto Frame Stack
			for (int i = 1; i < node.nChildren(); i++) {
				Type argType = node.child(i).getType();
				int argSize = argType.getSize();

				// Move Stack Pointer
				ASMCodeFragment argFrag = new ASMCodeFragment(CodeType.GENERATES_VALUE);
				argFrag.add(PushD, RunTime.STACK_POINTER);  // [ 20 ]  20 = SP Address
				argFrag.add(PushD, RunTime.STACK_POINTER);  // [ 20 20 ]
				argFrag.add(LoadI);  // [ 20 627703 ]  627703 = SP Value
				argFrag.add(PushI, argSize);  // [ 20 627703 4 ]
				argFrag.add(Subtract);  // [ 20 627699  ]
				argFrag.add(StoreI);  // []  Store -4 into SP address 24
				code.append(argFrag);

				// Put argument value
				code.add(PushD, RunTime.STACK_POINTER, "%% store arg " + i);  // [ 20 ]
				code.add(LoadI);  // [ 627699 ]
				ASMCodeFragment argValue = removeValueCode(node.child(i));
				code.append(argValue);  // [ 627699 6 ]

				// Store argument value

				FuncArgStoreCodeGenerator generator = new FuncArgStoreCodeGenerator(argType);
				ASMCodeFragment frag = generator.generate(node);
				code.append(frag);
			}

			// Push lambda location
			if (node.child(0) instanceof IdentifierNode) {
				ASMCodeFragment identifier = removeAddressCode(node.child(0));
				code.append(identifier);  // [ 627699 6 275 ]  275 = Identifier p address
				code.add(LoadI);  // [ 627699 6 61 ]  61 = Identifier p value
			} else {
				ASMCodeFragment subroutineCode = removeValueCode(node.child(0));
				code.append(subroutineCode);
			}

			// Call function
			code.add(CallV);

			// Get return value
			Type returnType = node.getType();
			code.add(PushD, RunTime.STACK_POINTER);
			code.add(LoadI);
			FunctionReturnStoreCodeGenerator generator = new FunctionReturnStoreCodeGenerator(returnType);
			ASMCodeFragment frag = generator.generate(node);
			code.append(frag);

			// Move the Stack Pointer up by the size of the return value
			code.add(PushD, RunTime.STACK_POINTER, "%% restore stack pointer");
			code.add(PushD, RunTime.STACK_POINTER);
			code.add(LoadI);
			code.add(PushI, returnType.getSize());
			code.add(Add);
			code.add(StoreI);
		}

		///////////////////////////////////////////////////////////////////////////
		// identifiers
		public void visitLeave(IdentifierNode node) {
			newAddressCode(node);
			Binding binding = node.getBinding();
			binding.generateAddress(code);

			if (node.isIndexed()) {
				// Store array base in INDEX_TEMP_1
				code.add(LoadI);
				code.add(PushD, RunTime.INDEX_TEMP_1);
				code.add(Exchange);
				code.add(StoreI);

				// Store array index in INDEX_TEMP_2
				ASMCodeFragment offset = removeValueCode(node.child(0));
				code.append(offset);
				code.add(PushD, RunTime.INDEX_TEMP_2);
				code.add(Exchange);
				code.add(StoreI);

				// Check index bounds are valid
				ArrayIndexBounding(node);

				// Generate Offset
				// Total Offset = Record + Index
				code.add(PushD, RunTime.INDEX_TEMP_1);
				code.add(LoadI);
				code.add(PushD, RunTime.INDEX_TEMP_1);
				code.add(LoadI);
				code.add(PushI, 8);
				code.add(Add);
				code.add(LoadI); // Sub-type Size
				code.add(PushD, RunTime.INDEX_TEMP_2);
				code.add(LoadI);
				code.add(Multiply); // Index Offset = Sub-type * Index
				code.add(PushI, 16);
				code.add(Add); // Record Offset
				code.add(Add);
			}
		}

		public void ArrayIndexBounding(IdentifierNode node) {
			// Check if index is negative
			code.add(ASMOpcode.PushD, RunTime.INDEX_TEMP_2);
			code.add(ASMOpcode.LoadI);
			code.add(ASMOpcode.JumpNeg, RunTime.NEGATIVE_INDEX_RUNTIME_ERROR);

			// Check if index is larger than array length
			code.add(ASMOpcode.PushD, RunTime.INDEX_TEMP_1);
			code.add(ASMOpcode.LoadI);
			code.add(ASMOpcode.PushI, 12);
			code.add(ASMOpcode.Add);
			code.add(ASMOpcode.LoadI); // Array length
			code.add(ASMOpcode.PushI, 1);
			code.add(ASMOpcode.Subtract);

			code.add(ASMOpcode.PushD, RunTime.INDEX_TEMP_2);
			code.add(ASMOpcode.LoadI);
			code.add(ASMOpcode.Subtract); // Check if index is bigger than length
			code.add(ASMOpcode.JumpNeg, RunTime.OUT_OF_BOUNDS_INDEX_RUNTIME_ERROR);
		}
		///////////////////////////////////////////////////////////////////////////
		// statements and declarations

		public void visitLeave(PrintStatementNode node) {
			newVoidCode(node);
			new PrintStatementGenerator(code, this).generate(node);
		}

		public void visit(NewlineNode node) {
			newVoidCode(node);
			code.add(PushD, RunTime.NEWLINE_PRINT_FORMAT);
			code.add(Printf);
		}

		public void visit(SpaceNode node) {
			newVoidCode(node);
			code.add(PushD, RunTime.SPACE_PRINT_FORMAT);
			code.add(Printf);
		}

		public void visit(TabNode node) {
			newVoidCode(node);
			code.add(PushD, RunTime.TAB_PRINT_FORMAT);
			code.add(Printf);
		}

		public void visitLeave(TypeCastingNode node) {
			newValueCode(node);

			ASMCodeFragment arg1 = removeValueCode(node.child(0));
			code.append(arg1);

			Object variant = node.getSignature().getVariant();

			if (variant instanceof SimpleCodeGenerator) {
				SimpleCodeGenerator generator = (SimpleCodeGenerator) variant;
				ASMCodeFragment frag = generator.generate(node);
				code.append(frag);
			}

			if (variant instanceof ASMOpcode) {
				ASMOpcode opcode = (ASMOpcode) variant;
				code.add(opcode);
			}
		}

		public void visitLeave(DeclarationNode node) {
			newVoidCode(node);
			ASMCodeFragment lvalue = removeAddressCode(node.child(0));
			ASMCodeFragment rvalue = removeValueCode(node.child(1));

			code.append(lvalue);
			code.append(rvalue);

			Type type = node.getType();
			code.add(opcodeForStore(type));
		}

		public void visitLeave(AssignmentStatementNode node) {
			newVoidCode(node);
			ASMCodeFragment lvalue = removeAddressCode(node.child(0));
			ASMCodeFragment rvalue = removeValueCode(node.child(1));

			code.append(lvalue);
			code.append(rvalue);

			Type type = node.child(0).getType();
			code.add(opcodeForStore(type));
		}

		private ASMOpcode opcodeForStore(Type type) {
			if (type == PrimitiveType.INTEGER || type == PrimitiveType.STRING || type instanceof ArrayType) {
				return StoreI;
			} else if (type == PrimitiveType.FLOAT) {
				return StoreF;
			}
			if (type == PrimitiveType.BOOLEAN || type == PrimitiveType.CHARACTER) {
				return StoreC;
			}
			assert false : "Type " + type + " unimplemented in opcodeForStore()";
			return null;
		}

		// IF
		public void visitLeave(IfNode node) {
			newVoidCode(node);

			ASMCodeFragment expression = removeValueCode(node.child(0));
			ASMCodeFragment block = removeVoidCode(node.child(1));

			Labeller labeller = new Labeller("IF");
			String startLabel = labeller.newLabel("if");
			String elseLabel = labeller.newLabel("else");
			String joinLabel = labeller.newLabel("join");

			code.add(Label, startLabel);
			code.append(expression);
			code.add(JumpFalse, elseLabel);

			code.append(block);
			code.add(Jump, joinLabel);

			code.add(Label, elseLabel);
			if (node.nChildren() == 3) {
				ASMCodeFragment elseBlock = removeVoidCode(node.child(2));
				code.append(elseBlock);
			}
			code.add(Label, joinLabel);
		}

		// IF
		public void visitLeave(WhileNode node) {
			newVoidCode(node);

			ASMCodeFragment expression = removeValueCode(node.child(0));
			ASMCodeFragment block = removeVoidCode(node.child(1));

			Labeller labeller = new Labeller("WHILE");
			String startLabel = labeller.newLabel("while");
			String joinLabel = labeller.newLabel("join");

			code.add(Label, startLabel);
			code.append(expression);

			code.add(JumpFalse, joinLabel); // Ends while
			code.append(block);
			code.add(Jump, startLabel); // Do block and go back to start

			code.add(Label, joinLabel);
		}

		///////////////////////////////////////////////////////////////////////////
		// expressions
		public void visitLeave(OperatorNode node) {
			Lextant operator = node.getOperator();

			// Only 1 child, so +/-/!/length the child
			if ((operator == Punctuator.SUBTRACT || operator == Punctuator.ADD || operator == Punctuator.NOT ||
					operator == Keyword.LENGTH) && node.nChildren() == 1) {
				visitUnaryOperatorNode(node);
			} else if (operator == Punctuator.GREATER || operator == Punctuator.GREATEREQUAL ||
					operator == Punctuator.EQUALEQUAL || operator == Punctuator.NOTEQUAL ||
					operator == Punctuator.LESSER || operator == Punctuator.LESSEREQUAL ||
					operator == Punctuator.AND || operator == Punctuator.OR) {
				visitComparisonOperatorNode(node, operator);
			}

			else {
				visitNormalBinaryOperatorNode(node);
			}
		}

		private void visitComparisonOperatorNode(OperatorNode node,
				Lextant operator) {

			ASMCodeFragment arg1 = removeValueCode(node.child(0));
			ASMCodeFragment arg2 = removeValueCode(node.child(1));

			Labeller labeller = new Labeller("compare");

			String startLabel = labeller.newLabel("arg1");
			String arg2Label = labeller.newLabel("arg2");
			String subLabel = labeller.newLabel("sub");
			String trueLabel = labeller.newLabel("true");
			String falseLabel = labeller.newLabel("false");
			String joinLabel = labeller.newLabel("join");

			String dummyLabel = labeller.newLabel("dummy");

			if (operator == Punctuator.GREATER) {
				newValueCode(node);
				if (node.child(0).getType() == PrimitiveType.INTEGER
						|| node.child(0).getType() == PrimitiveType.CHARACTER) {
					code.add(Label, startLabel);
					code.append(arg1);
					code.add(Label, arg2Label);
					code.append(arg2);
					code.add(Label, subLabel);
					code.add(Subtract);

					code.add(JumpPos, trueLabel);
					code.add(Jump, falseLabel);

					code.add(Label, trueLabel);
					code.add(PushI, 1);
					code.add(Jump, joinLabel);
					code.add(Label, falseLabel);
					code.add(PushI, 0);
					code.add(Jump, joinLabel);
					code.add(Label, joinLabel);
				} else if (node.child(0).getType() == PrimitiveType.FLOAT) {
					code.add(Label, startLabel);
					code.append(arg1);
					code.add(Label, arg2Label);
					code.append(arg2);
					code.add(Label, subLabel);
					code.add(FSubtract);

					code.add(JumpFPos, trueLabel);
					code.add(Jump, falseLabel);

					code.add(Label, trueLabel);
					code.add(PushI, 1);
					code.add(Jump, joinLabel);
					code.add(Label, falseLabel);
					code.add(PushI, 0);
					code.add(Jump, joinLabel);
					code.add(Label, joinLabel);
				}
			} else if (operator == Punctuator.GREATEREQUAL) {
				newValueCode(node);
				if (node.child(0).getType() == PrimitiveType.INTEGER
						|| node.child(0).getType() == PrimitiveType.CHARACTER) {
					code.add(Label, startLabel);
					code.append(arg1);
					code.add(Label, arg2Label);
					code.append(arg2);
					code.add(Label, subLabel);
					code.add(Subtract);

					code.add(Duplicate); // [a-b] -> [a-b a-b]
					code.add(JumpPos, trueLabel);
					code.add(JumpFalse, dummyLabel);
					code.add(Jump, falseLabel);

					code.add(Label, trueLabel);
					code.add(Pop);
					code.add(Label, dummyLabel);
					code.add(PushI, 1);
					code.add(Jump, joinLabel);
					code.add(Label, falseLabel);
					code.add(PushI, 0);
					code.add(Jump, joinLabel);
					code.add(Label, joinLabel);
				} else if (node.child(0).getType() == PrimitiveType.FLOAT) {
					code.add(Label, startLabel);
					code.append(arg1);
					code.add(Label, arg2Label);
					code.append(arg2);
					code.add(Label, subLabel);
					code.add(FSubtract);

					code.add(Duplicate); // [a-b] -> [a-b a-b]
					code.add(JumpFPos, trueLabel);
					code.add(JumpFZero, dummyLabel);
					code.add(Jump, falseLabel);

					code.add(Label, trueLabel);
					code.add(Pop);
					code.add(Label, dummyLabel);
					code.add(PushI, 1);
					code.add(Jump, joinLabel);
					code.add(Label, falseLabel);
					code.add(PushI, 0);
					code.add(Jump, joinLabel);
					code.add(Label, joinLabel);
				}
			} else if (operator == Punctuator.EQUALEQUAL) {
				newValueCode(node);
				if (node.child(0).getType() == PrimitiveType.INTEGER
						|| node.child(0).getType() == PrimitiveType.CHARACTER ||
						node.child(0).getType() == PrimitiveType.STRING
						|| node.child(0).getType() == PrimitiveType.BOOLEAN) {
					code.add(Label, startLabel);
					code.append(arg1);
					code.add(Label, arg2Label);
					code.append(arg2);
					code.add(Label, subLabel);
					code.add(Subtract);

					code.add(JumpFalse, trueLabel);
					code.add(Jump, falseLabel);

					code.add(Label, trueLabel);
					code.add(PushI, 1);
					code.add(Jump, joinLabel);
					code.add(Label, falseLabel);
					code.add(PushI, 0);
					code.add(Jump, joinLabel);
					code.add(Label, joinLabel);
				} else if (node.child(0).getType() == PrimitiveType.FLOAT) {
					code.add(Label, startLabel);
					code.append(arg1);
					code.add(Label, arg2Label);
					code.append(arg2);
					code.add(Label, subLabel);
					code.add(FSubtract);

					code.add(JumpFZero, trueLabel);
					code.add(Jump, falseLabel);

					code.add(Label, trueLabel);
					code.add(PushI, 1);
					code.add(Jump, joinLabel);
					code.add(Label, falseLabel);
					code.add(PushI, 0);
					code.add(Jump, joinLabel);
					code.add(Label, joinLabel);
				}
			} else if (operator == Punctuator.NOTEQUAL) {
				newValueCode(node);
				if (node.child(0).getType() == PrimitiveType.INTEGER
						|| node.child(0).getType() == PrimitiveType.CHARACTER ||
						node.child(0).getType() == PrimitiveType.STRING
						|| node.child(0).getType() == PrimitiveType.BOOLEAN) {
					code.add(Label, startLabel);
					code.append(arg1);
					code.add(Label, arg2Label);
					code.append(arg2);
					code.add(Label, subLabel);
					code.add(Subtract);

					code.add(JumpTrue, trueLabel);
					code.add(Jump, falseLabel);

					code.add(Label, trueLabel);
					code.add(PushI, 1);
					code.add(Jump, joinLabel);
					code.add(Label, falseLabel);
					code.add(PushI, 0);
					code.add(Jump, joinLabel);
					code.add(Label, joinLabel);
				} else if (node.child(0).getType() == PrimitiveType.FLOAT) {
					code.add(Label, startLabel);
					code.append(arg1);
					code.add(Label, arg2Label);
					code.append(arg2);
					code.add(Label, subLabel);
					code.add(FSubtract);

					code.add(Duplicate);
					code.add(JumpFPos, trueLabel);
					code.add(JumpFNeg, dummyLabel);
					code.add(Jump, falseLabel);

					code.add(Label, trueLabel);
					code.add(Pop);
					code.add(Label, dummyLabel);
					code.add(PushI, 1);
					code.add(Jump, joinLabel);
					code.add(Label, falseLabel);
					code.add(PushI, 0);
					code.add(Jump, joinLabel);
					code.add(Label, joinLabel);
				}
			} else if (operator == Punctuator.LESSER) {
				newValueCode(node);
				if (node.child(0).getType() == PrimitiveType.INTEGER
						|| node.child(0).getType() == PrimitiveType.CHARACTER) {
					code.add(Label, startLabel);
					code.append(arg1);
					code.add(Label, arg2Label);
					code.append(arg2);
					code.add(Label, subLabel);
					code.add(Subtract);

					code.add(JumpNeg, trueLabel);
					code.add(Jump, falseLabel);

					code.add(Label, trueLabel);
					code.add(PushI, 1);
					code.add(Jump, joinLabel);
					code.add(Label, falseLabel);
					code.add(PushI, 0);
					code.add(Jump, joinLabel);
					code.add(Label, joinLabel);
				} else if (node.child(0).getType() == PrimitiveType.FLOAT) {
					code.add(Label, startLabel);
					code.append(arg1);
					code.add(Label, arg2Label);
					code.append(arg2);
					code.add(Label, subLabel);
					code.add(FSubtract);

					code.add(JumpFNeg, trueLabel);
					code.add(Jump, falseLabel);

					code.add(Label, trueLabel);
					code.add(PushI, 1);
					code.add(Jump, joinLabel);
					code.add(Label, falseLabel);
					code.add(PushI, 0);
					code.add(Jump, joinLabel);
					code.add(Label, joinLabel);
				}
			} else if (operator == Punctuator.LESSEREQUAL) {
				newValueCode(node);
				if (node.child(0).getType() == PrimitiveType.INTEGER
						|| node.child(0).getType() == PrimitiveType.CHARACTER) {
					code.add(Label, startLabel);
					code.append(arg1);
					code.add(Label, arg2Label);
					code.append(arg2);
					code.add(Label, subLabel);
					code.add(Subtract);

					code.add(Duplicate); // [a-b] -> [a-b a-b]
					code.add(JumpNeg, trueLabel);
					code.add(JumpFalse, dummyLabel);
					code.add(Jump, falseLabel);

					code.add(Label, trueLabel);
					code.add(Pop);
					code.add(Label, dummyLabel);
					code.add(PushI, 1);
					code.add(Jump, joinLabel);
					code.add(Label, falseLabel);
					code.add(PushI, 0);
					code.add(Jump, joinLabel);
					code.add(Label, joinLabel);
				} else if (node.child(0).getType() == PrimitiveType.FLOAT) {
					code.add(Label, startLabel);
					code.append(arg1);
					code.add(Label, arg2Label);
					code.append(arg2);
					code.add(Label, subLabel);
					code.add(FSubtract);

					code.add(Duplicate); // [a-b] -> [a-b a-b]
					code.add(JumpFNeg, trueLabel);
					code.add(JumpFZero, dummyLabel);
					code.add(Jump, falseLabel);

					code.add(Label, trueLabel);
					code.add(Pop);
					code.add(Label, dummyLabel);
					code.add(PushI, 1);
					code.add(Jump, joinLabel);
					code.add(Label, falseLabel);
					code.add(PushI, 0);
					code.add(Jump, joinLabel);
					code.add(Label, joinLabel);
				}
			} else if (operator == Punctuator.AND) {
				newValueCode(node);
				code.add(Label, startLabel);
				code.append(arg1);
				code.add(Duplicate);
				code.add(JumpFalse, dummyLabel); // Jumps to short-circuit false
				code.add(Label, arg2Label);
				code.append(arg2);
				code.add(Label, subLabel);
				code.add(And);
				code.add(Duplicate); // [(a AND b)] -> [(a AND b) (a AND b)]

				code.add(JumpFalse, falseLabel);
				code.add(Jump, trueLabel);

				code.add(Label, dummyLabel);
				code.add(Pop); // Removes extra arg1
				code.add(PushI, 0);
				code.add(Jump, joinLabel);
				code.add(Duplicate);
				code.add(Label, trueLabel);
				code.add(PushI, 1);
				code.add(Jump, joinLabel);
				code.add(Label, falseLabel);
				code.add(Pop); // Removes extra [(a AND b)]
				code.add(PushI, 0);
				code.add(Jump, joinLabel);
				code.add(Label, joinLabel);

			} else if (operator == Punctuator.OR) {
				newValueCode(node);
				code.add(Label, startLabel);
				code.append(arg1);
				code.add(Duplicate);
				code.add(JumpTrue, dummyLabel); // Jumps to short-circuit true
				code.add(Label, arg2Label);
				code.append(arg2);
				code.add(Label, subLabel);
				code.add(Or);
				code.add(Duplicate); // [(a OR b)] -> [(a OR b) (a OR b)]

				code.add(JumpFalse, falseLabel);
				code.add(Jump, trueLabel);

				code.add(Label, dummyLabel);
				code.add(Pop); // Removes extra arg1
				code.add(PushI, 1);
				code.add(Jump, joinLabel);
				code.add(Duplicate);
				code.add(Label, trueLabel);
				code.add(PushI, 1);
				code.add(Jump, joinLabel);
				code.add(Label, falseLabel);
				code.add(Pop); // Removes extra [(a OR b)]
				code.add(PushI, 0);
				code.add(Jump, joinLabel);
				code.add(Label, joinLabel);
			}

		}

		private void visitUnaryOperatorNode(OperatorNode node) {
			newValueCode(node);
			ASMCodeFragment arg1 = removeValueCode(node.child(0));

			code.append(arg1);

			if (node.getOperator() == Keyword.LENGTH) {
				code.add(ASMOpcode.PushI, 12);
				code.add(ASMOpcode.Add);
				code.add(ASMOpcode.LoadI);
			} else {
				ASMOpcode opcode = opcodeForOperator(node.getOperator(), node);

				code.add(opcode); // type-dependent! (opcode is different for floats and for ints)
			}
		}

		private void visitNormalBinaryOperatorNode(OperatorNode node) {
			newValueCode(node);
			ASMCodeFragment arg1 = removeValueCode(node.child(0));
			ASMCodeFragment arg2 = removeValueCode(node.child(1));

			Object variant = node.getSignature().getVariant();

			if (variant instanceof ASMOpcode) {
				code.append(arg1);
				code.append(arg2);

				// OperatorNode extends ParseNode,nChildren
				ASMOpcode opcode = opcodeForOperator(node.getOperator(), node);

				// case handling for type casting issue
				if (opcode == Return) {
					opcode = (ASMOpcode) variant;
				}

				if (opcode == Divide) {
					Labeller labeller = new Labeller("divide");

					String startLabel = labeller.newLabel("2-stack");
					String zeroLabel = labeller.newLabel("zero");
					String joinLabel = labeller.newLabel("join");

					code.add(Label, startLabel);
					code.add(Duplicate);
					code.add(JumpFalse, zeroLabel);
					code.add(Jump, joinLabel);
					code.add(Label, zeroLabel);
					code.add(Jump, asmCodeGenerator.runtime.RunTime.INTEGER_DIVIDE_BY_ZERO_RUNTIME_ERROR);
					code.add(Label, joinLabel);
					code.add(opcode);
				} else if (opcode == FDivide) {
					Labeller labeller = new Labeller("fdivide");

					String startLabel = labeller.newLabel("2-stack");
					String zeroLabel = labeller.newLabel("zero");
					String joinLabel = labeller.newLabel("join");

					code.add(Label, startLabel);
					code.add(Duplicate);
					code.add(JumpFZero, zeroLabel);
					code.add(Jump, joinLabel);
					code.add(Label, zeroLabel);
					code.add(Jump, asmCodeGenerator.runtime.RunTime.FLOAT_DIVIDE_BY_ZERO_RUNTIME_ERROR);
					code.add(Label, joinLabel);
					code.add(opcode);
				} else {
					code.add(opcode); // type-dependent! (opcode is different for floats and for ints)
				}
			} else if (variant instanceof SimpleCodeGenerator) {
				code.append(arg1);
				code.append(arg2);

				SimpleCodeGenerator generator = (SimpleCodeGenerator) variant;
				ASMCodeFragment frag = generator.generate(node);
				code.append(frag);

				if (frag.isAddress()) {
					code.markAsAddress();
				}
			} else {
				assert false : "unimplemented operator variant";
			}
		}

		private ASMOpcode opcodeForOperator(Lextant lextant, OperatorNode node) {
			assert (lextant instanceof Punctuator);
			Punctuator punctuator = (Punctuator) lextant;
			int nChilds = node.nChildren();

			if (nChilds == 1) {
				if (node.child(0).getType() == PrimitiveType.INTEGER) {
					switch (punctuator) {
						case ADD:
							return Nop; // (unary add)
						case SUBTRACT:
							return Negate; // (unary subtract) type-dependent!
						default:
							assert false : "unimplemented operator in opcodeForOperator";
					}
				} else if (node.child(0).getType() == PrimitiveType.FLOAT) {
					switch (punctuator) {
						case ADD:
							return Nop; // (unary add)
						case SUBTRACT:
							return FNegate; // (unary subtract) type-dependent!
						default:
							assert false : "unimplemented operator in opcodeForOperator";
					}
				} else if (node.child(0).getType() == PrimitiveType.BOOLEAN) {
					switch (punctuator) {
						case NOT:
							return BNegate; // (!)
						default:
							assert false : "unimplemented operator in opcodeForOperator";
					}
				}

			}
			if (nChilds == 2) {

				// type-dependent!
				// integer
				if (node.child(0).getType() == PrimitiveType.INTEGER) {
					switch (punctuator) {
						case ADD:
							return Add; // type-dependent!
						case SUBTRACT:
							return Subtract; // (subtraction) type-dependent!
						case MULTIPLY:
							return Multiply; // type-dependent!
						case DIVIDE:
							return Divide; // type-dependent!
						case CAST:
							return Return;
						default:
							assert false : "unimplemented operator in opcodeForOperator";
					}
				} else if (node.child(0).getType() == PrimitiveType.FLOAT) {
					switch (punctuator) {
						case ADD:
							return FAdd; // type-dependent!
						case SUBTRACT:
							return FSubtract; // (subtraction) type-dependent!
						case MULTIPLY:
							return FMultiply; // type-dependent!
						case DIVIDE:
							return FDivide; // type-dependent!
						case CAST:
							return Return;
						default:
							assert false : "unimplemented operator in opcodeForOperator";
					}
				}
			}
			return null;
		}

		public void visitLeave(ArrayNode node) {
			newValueCode(node);

			Labeller labeller = new Labeller("array");
			String startLabel = labeller.newLabel("");
			String recordLabel = labeller.newLabel("create-record");
			String startChildrenLabel = labeller.newLabel("start-store-children");
			String endChildrenLabel = labeller.newLabel("end-store-children");
			code.add(ASMOpcode.Label, startLabel);

			ASMCodeChunk subtypeChunk = new ASMCodeChunk();

			// Push length onto stack
			if (node.isEmpty()) { // new [Type] (expression)
				ASMCodeFragment length = removeValueCode(node.child(0));
				code.append(length);
				code.append(length);
				ArrayNegativeIndex(node);
			} else { // Populated
				code.add(ASMOpcode.PushI, node.nChildren());
				code.add(ASMOpcode.PushI, node.nChildren());
			}

			// Push sub-type size onto stack
			code.addChunk(subtypeChunk);
			code.add(ASMOpcode.PushI, node.getSubtype().getSize());
			code.add(ASMOpcode.Multiply);

			// Allocate memory for array
			ArrayAllocate(node);

			// Store header
			code.add(ASMOpcode.Label, recordLabel);
			ArrayGenerateRecord(node); // last line exchanged length with address + offset for length

			// Push address onto stack
			ArrayTempToStack(node);

			code.add(ASMOpcode.Label, startChildrenLabel);
			if (!node.isEmpty()) {
				for (int i = 0; i < node.nChildren(); i++) {
					code.add(ASMOpcode.Duplicate);
					code.add(ASMOpcode.PushI, node.getOffset(i));
					code.add(ASMOpcode.Add);

					ASMCodeFragment child = removeValueCode(node.child(i));
					code.append(child);

					code.add(opcodeForStore(node.getSubtype()));
				}
			}
			code.add(ASMOpcode.Label, endChildrenLabel);
		}

		private void ArrayNegativeIndex(ArrayNode node) {
			code.add(ASMOpcode.Duplicate);
			code.add(ASMOpcode.JumpNeg, RunTime.OUT_OF_BOUNDS_INDEX_RUNTIME_ERROR);
		}

		private void ArrayAllocate(ArrayNode node) {
			// allocationSize = header + arraySize
			// Add 16 bytes for header record
			code.add(ASMOpcode.PushI, 16);
			code.add(ASMOpcode.Add);

			// Allocate memory for array
			code.add(ASMOpcode.Call, MemoryManager.MEM_MANAGER_ALLOCATE);

			// Store heap address in stack
			ArrayStacktoTemp(node);
		}

		private void ArrayStacktoTemp(ArrayNode node) {
			code.add(ASMOpcode.PushD, RunTime.ARRAY_TEMP_1);
			code.add(ASMOpcode.Exchange);
			code.add(ASMOpcode.StoreI);
		}

		private void ArrayGenerateRecord(ArrayNode node) {

			// Push Address -> Load Content of address Address -> Push type identifier ->
			// Store into that address
			code.add(ASMOpcode.PushD, RunTime.ARRAY_TEMP_1);
			code.add(ASMOpcode.LoadI);
			code.add(ASMOpcode.PushI, 7);
			code.add(ASMOpcode.StoreI); // type identifier

			// Push Address -> Load Content of address -> Push 4 to add 4 byte offset ->
			// Push subtype is reference -> Store into that address
			code.add(ASMOpcode.PushD, RunTime.ARRAY_TEMP_1);
			code.add(ASMOpcode.LoadI);
			code.add(ASMOpcode.PushI, 4);
			code.add(ASMOpcode.Add);
			if (node.getSubtype() instanceof ArrayType) {
				code.add(ASMOpcode.PushI, 4);
			} else {
				code.add(ASMOpcode.PushI, 0);
			}
			code.add(ASMOpcode.StoreI); // subtype-is-reference

			// Array address + 8 offset to put in subtype size
			code.add(ASMOpcode.PushD, RunTime.ARRAY_TEMP_1);
			code.add(ASMOpcode.LoadI);
			code.add(ASMOpcode.PushI, 8);
			code.add(ASMOpcode.Add);
			code.add(ASMOpcode.PushI, node.getSubtype().getSize());
			code.add(ASMOpcode.StoreI); // subtype size

			// Array address + 12 offset to put in array length
			code.add(ASMOpcode.PushD, RunTime.ARRAY_TEMP_1);
			code.add(ASMOpcode.LoadI);
			code.add(ASMOpcode.PushI, 12);
			code.add(ASMOpcode.Add);
			code.add(ASMOpcode.Exchange);
			code.add(ASMOpcode.StoreI); // length
		}

		// Load Address
		private void ArrayTempToStack(ArrayNode node) {
			code.add(ASMOpcode.PushD, RunTime.ARRAY_TEMP_1);
			code.add(ASMOpcode.LoadI);
		}

		///////////////////////////////////////////////////////////////////////////
		// leaf nodes (ErrorNode not necessary)
		public void visit(BooleanConstantNode node) {
			newValueCode(node);
			code.add(PushI, node.getValue() ? 1 : 0);
		}

		// public void visit(IdentifierNode node) {
		// newAddressCode(node);
		// Binding binding = node.getBinding();
		//
		// binding.generateAddress(code);
		// }

		public void visit(IntegerConstantNode node) {
			newValueCode(node);

			code.add(PushI, node.getValue());
		}

		public void visit(FloatConstantNode node) {
			newValueCode(node);

			code.add(PushF, node.getValue());
		}

		public void visit(StringConstantNode node) {
			newValueCode(node);
			Labeller labeller = new Labeller("string");
			String stringLabel = labeller.newLabel(node.getValue());

			code.add(DLabel, stringLabel);
			code.add(DataI, 3);
			code.add(DataI, 9);
			code.add(DataI, node.getValue().length());
			code.add(DataS, node.getValue());
			code.add(PushD, stringLabel);
		}

		public void visit(CharacterConstantNode node) {
			newValueCode(node);

			code.add(PushI, node.getValue());
		}

		public void visit(FuncTypeNode node) {
			newValueCode(node);

		}
	}

}
