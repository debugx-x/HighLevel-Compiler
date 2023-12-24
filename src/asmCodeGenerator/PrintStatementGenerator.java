package asmCodeGenerator;

import static asmCodeGenerator.codeStorage.ASMOpcode.Jump;
import static asmCodeGenerator.codeStorage.ASMOpcode.JumpTrue;
import static asmCodeGenerator.codeStorage.ASMOpcode.Label;
import static asmCodeGenerator.codeStorage.ASMOpcode.Printf;
import static asmCodeGenerator.codeStorage.ASMOpcode.PushD;
import static asmCodeGenerator.codeStorage.ASMOpcode.PushI;
import static asmCodeGenerator.codeStorage.ASMOpcode.Add;
import parseTree.ParseNode;
import parseTree.nodeTypes.NewlineNode;
import parseTree.nodeTypes.PrintStatementNode;
import parseTree.nodeTypes.SpaceNode;
import parseTree.nodeTypes.TabNode;
import semanticAnalyzer.types.ArrayType;
import semanticAnalyzer.types.FunctionType;
import semanticAnalyzer.types.PrimitiveType;
import semanticAnalyzer.types.Type;
import asmCodeGenerator.ASMCodeGenerator.CodeVisitor;
import asmCodeGenerator.codeStorage.ASMCodeFragment;
import asmCodeGenerator.codeStorage.ASMOpcode;
import asmCodeGenerator.runtime.RunTime;

public class PrintStatementGenerator {
	ASMCodeFragment code;
	ASMCodeGenerator.CodeVisitor visitor;
	
	
	public PrintStatementGenerator(ASMCodeFragment code, CodeVisitor visitor) {
		super();
		this.code = code;
		this.visitor = visitor;
	}

	public void generate(PrintStatementNode node) {
		for(ParseNode child : node.getChildren()) {
			if(child instanceof NewlineNode || child instanceof SpaceNode || child instanceof TabNode) {
				ASMCodeFragment childCode = visitor.removeVoidCode(child);
				code.append(childCode);
			}
			else {
				appendPrintCode(child);
			}
		}
	}

	private void appendPrintCode(ParseNode node) {
		code.append(visitor.removeValueCode(node));
		
		if (node.getType() instanceof ArrayType) {
			PrintArray(((ArrayType)node.getType()).getSubtype());
		} else {
			String format = printFormat(node.getType());
			convertToStringIfBoolean(node);
			convertToValueIfString(node);
			code.add(PushD, format);
			code.add(Printf);
		}
	}
	
	private void PrintArray(Type subType) {
		Labeller labeller = new Labeller("print-array");
		String startLabel  = labeller.newLabel("");
		String loopLabel  = labeller.newLabel("loop");
		String joinLabel  = labeller.newLabel("join");

		// Start of array
		code.add(Label, startLabel);
		
		// ADDRESS OF ARRAY 		--> ARRAY_TEMP_1
		code.add(ASMOpcode.PushD, RunTime.ARRAY_TEMP_1);
		code.add(ASMOpcode.Exchange);
		code.add(ASMOpcode.StoreI);

		// Get array length
		code.add(ASMOpcode.PushD, RunTime.ARRAY_TEMP_1);
		code.add(ASMOpcode.LoadI);
		code.add(ASMOpcode.PushI, 12);
		code.add(ASMOpcode.Add);
		code.add(ASMOpcode.LoadI);
		code.add(ASMOpcode.PushD, RunTime.PRINT_TEMP_1);	// 1 = ARRAY LENGTH
		code.add(ASMOpcode.Exchange);
		code.add(ASMOpcode.StoreI);

		// Get subtype size
		code.add(ASMOpcode.PushD, RunTime.ARRAY_TEMP_1);
		code.add(ASMOpcode.LoadI);
		code.add(ASMOpcode.PushI, 8);
		code.add(ASMOpcode.Add);
		code.add(ASMOpcode.LoadI);
		code.add(ASMOpcode.PushD, RunTime.PRINT_TEMP_2);	// 2 = SUBTYPE SIZE
		code.add(ASMOpcode.Exchange);
		code.add(ASMOpcode.StoreI);

		// Offset counter (start after Array record)
		code.add(ASMOpcode.PushD, RunTime.PRINT_TEMP_3);	// 3 = OFFSET FROM BASE
		code.add(ASMOpcode.PushI, 16);
		code.add(ASMOpcode.StoreI);

		// Print open bracket '['
		code.add(ASMOpcode.PushI, 91);
		code.add(ASMOpcode.PushD, RunTime.CHARACTER_PRINT_FORMAT);
		code.add(ASMOpcode.Printf);

		// Start printing loop
		code.add(ASMOpcode.Label, loopLabel);
		code.add(ASMOpcode.PushD, RunTime.PRINT_TEMP_1);
		code.add(ASMOpcode.LoadI);
		code.add(ASMOpcode.JumpFalse, joinLabel);

		// Print value at array[index]
		code.add(ASMOpcode.PushD, RunTime.ARRAY_TEMP_1);
		code.add(ASMOpcode.LoadI);
		code.add(ASMOpcode.PushD, RunTime.PRINT_TEMP_3);
		code.add(ASMOpcode.LoadI);
		code.add(ASMOpcode.Add);
		opcodesForPrint(subType);

		// Modify array offset
		code.add(ASMOpcode.PushD, RunTime.PRINT_TEMP_3);
		code.add(ASMOpcode.LoadI);
		code.add(ASMOpcode.PushD, RunTime.PRINT_TEMP_2);
		code.add(ASMOpcode.LoadI);
		code.add(ASMOpcode.Add);
		code.add(ASMOpcode.PushD, RunTime.PRINT_TEMP_3);
		code.add(ASMOpcode.Exchange);
		code.add(ASMOpcode.StoreI);

		// Update loop counter
		code.add(ASMOpcode.PushD, RunTime.PRINT_TEMP_1);
		code.add(ASMOpcode.LoadI);
		code.add(ASMOpcode.PushI, 1);
		code.add(ASMOpcode.Subtract);
		code.add(ASMOpcode.PushD, RunTime.PRINT_TEMP_1);
		code.add(ASMOpcode.Exchange);
		code.add(ASMOpcode.StoreI);

		code.add(ASMOpcode.PushD, RunTime.PRINT_TEMP_1);
		code.add(ASMOpcode.LoadI);
		code.add(ASMOpcode.JumpFalse, joinLabel);

		// If not last element, print comma ',' and space ' '
		code.add(ASMOpcode.PushI, 44);
		code.add(ASMOpcode.PushD, RunTime.CHARACTER_PRINT_FORMAT);
		code.add(ASMOpcode.Printf);
		code.add(ASMOpcode.PushI, 32);
		code.add(ASMOpcode.PushD, RunTime.CHARACTER_PRINT_FORMAT);
		code.add(ASMOpcode.Printf);

		code.add(ASMOpcode.Jump, loopLabel);

		code.add(ASMOpcode.Label, joinLabel);

		// Print closed bracket ']'
		code.add(ASMOpcode.PushI, 93);
		code.add(ASMOpcode.PushD, RunTime.CHARACTER_PRINT_FORMAT);
		code.add(ASMOpcode.Printf);
	}
	
	private void opcodesForPrint(Type type) {
		if(type == PrimitiveType.INTEGER) {
			code.add(ASMOpcode.LoadI);
			code.add(ASMOpcode.PushD, RunTime.INTEGER_PRINT_FORMAT);
			code.add(ASMOpcode.Printf);
		}
		else if(type == PrimitiveType.FLOAT) {
			code.add(ASMOpcode.LoadF);
			code.add(ASMOpcode.PushD, RunTime.FLOAT_PRINT_FORMAT);
			code.add(ASMOpcode.Printf);
		}
		else if(type == PrimitiveType.BOOLEAN) {
			code.add(ASMOpcode.LoadC);

			Labeller labeller = new Labeller("print-boolean");
			String trueLabel = labeller.newLabel("true");
			String endLabel = labeller.newLabel("join");
			
			code.add(ASMOpcode.JumpTrue, trueLabel);
			code.add(ASMOpcode.PushD, RunTime.BOOLEAN_FALSE_STRING);
			code.add(ASMOpcode.Jump, endLabel);
			code.add(ASMOpcode.Label, trueLabel);
			code.add(ASMOpcode.PushD, RunTime.BOOLEAN_TRUE_STRING);
			code.add(ASMOpcode.Label, endLabel);
			code.add(ASMOpcode.PushD, RunTime.BOOLEAN_PRINT_FORMAT);
			code.add(ASMOpcode.Printf);
		}
		else if(type == PrimitiveType.CHARACTER) {
			code.add(ASMOpcode.LoadC);
			code.add(ASMOpcode.PushD, RunTime.CHARACTER_PRINT_FORMAT);
			code.add(ASMOpcode.Printf);
		}
		else if(type == PrimitiveType.STRING) {
			code.add(ASMOpcode.LoadI);
			code.add(ASMOpcode.PushI, 12);
			code.add(ASMOpcode.Add);
			code.add(ASMOpcode.PushD, RunTime.STRING_PRINT_FORMAT);
			code.add(ASMOpcode.Printf);
		}
		else if(type instanceof ArrayType) {
			code.add(ASMOpcode.PushD, RunTime.ARRAY_TEMP_1);
			code.add(ASMOpcode.LoadI);
			code.add(ASMOpcode.Exchange);
			code.add(ASMOpcode.PushD, RunTime.PRINT_TEMP_1);
			code.add(ASMOpcode.LoadI);
			code.add(ASMOpcode.Exchange);
			code.add(ASMOpcode.PushD, RunTime.PRINT_TEMP_2);
			code.add(ASMOpcode.LoadI);
			code.add(ASMOpcode.Exchange);
			code.add(ASMOpcode.PushD, RunTime.PRINT_TEMP_3);
			code.add(ASMOpcode.LoadI);
			code.add(ASMOpcode.Exchange);

			code.add(ASMOpcode.LoadI);
			Type subType = ((ArrayType) type).getSubtype();
			PrintArray(subType);

			code.add(ASMOpcode.PushD, RunTime.PRINT_TEMP_3);
			code.add(ASMOpcode.Exchange);
			code.add(ASMOpcode.StoreI);
			code.add(ASMOpcode.PushD, RunTime.PRINT_TEMP_2);
			code.add(ASMOpcode.Exchange);
			code.add(ASMOpcode.StoreI);
			code.add(ASMOpcode.PushD, RunTime.PRINT_TEMP_1);
			code.add(ASMOpcode.Exchange);
			code.add(ASMOpcode.StoreI);
			code.add(ASMOpcode.PushD, RunTime.ARRAY_TEMP_1);
			code.add(ASMOpcode.Exchange);
			code.add(ASMOpcode.StoreI);
		}
		else {
			assert false: "Type " + type + " unimplemented in opcodeForPrint()";
		}
	}
	
	private void convertToValueIfString(ParseNode node) {
		if(node.getType() != PrimitiveType.STRING) {
			return;
		}
		code.add(PushI, 12); //Skip string record code 3 and 9
		code.add(Add);	//Add and then pushD
	}

	private void convertToStringIfBoolean(ParseNode node) {
		if(node.getType() != PrimitiveType.BOOLEAN) {
			return;
		}
		
		Labeller labeller = new Labeller("print-boolean");
		String trueLabel = labeller.newLabel("true");
		String endLabel = labeller.newLabel("join");

		code.add(JumpTrue, trueLabel);
		code.add(PushD, RunTime.BOOLEAN_FALSE_STRING);
		code.add(Jump, endLabel);
		code.add(Label, trueLabel);
		code.add(PushD, RunTime.BOOLEAN_TRUE_STRING);
		code.add(Label, endLabel);
	}


	private static String printFormat(Type type) {
		//assert type instanceof PrimitiveType;

		// check if type is an function
		if(type instanceof FunctionType) {
			return RunTime.FUNCTION_PRINT_FORMAT;
		}
		
		switch((PrimitiveType)type) {
		case INTEGER:	return RunTime.INTEGER_PRINT_FORMAT;
		case BOOLEAN:	return RunTime.BOOLEAN_PRINT_FORMAT;
		case FLOAT:		return RunTime.FLOAT_PRINT_FORMAT;
		case CHARACTER:	return RunTime.CHARACTER_PRINT_FORMAT;
		case STRING:	return RunTime.STRING_PRINT_FORMAT;
		default:		
			assert false : "Type " + type + " unimplemented in PrintStatementGenerator.printFormat()";
			return "";
		}
	}
}
