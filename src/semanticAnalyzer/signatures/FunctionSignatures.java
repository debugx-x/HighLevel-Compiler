package semanticAnalyzer.signatures;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import asmCodeGenerator.codeStorage.ASMOpcode;
import asmCodeGenerator.operators.ArrayLengthCodeGenerator;
import asmCodeGenerator.operators.CharToBoolCodeGenerator;
import asmCodeGenerator.operators.IntToBoolCodeGenerator;
import asmCodeGenerator.operators.IntToCharCodeGenerator;
import semanticAnalyzer.types.ArrayType;
import semanticAnalyzer.types.Type;
import semanticAnalyzer.types.TypeVariable;

import static semanticAnalyzer.types.PrimitiveType.*;

import lexicalAnalyzer.Keyword;
import lexicalAnalyzer.Punctuator;

public class FunctionSignatures extends ArrayList<FunctionSignature> {
	private static final long serialVersionUID = -4907792488209670697L;
	private static Map<Object, FunctionSignatures> signaturesForKey = new HashMap<Object, FunctionSignatures>();

	Object key;

	public FunctionSignatures(Object key, FunctionSignature... functionSignatures) {
		this.key = key;
		for (FunctionSignature functionSignature : functionSignatures) {
			add(functionSignature);
		}
		signaturesForKey.put(key, this);
	}

	public Object getKey() {
		return key;
	}

	public boolean hasKey(Object key) {
		return this.key.equals(key);
	}

	public FunctionSignature acceptingSignature(List<Type> types) {
		for (FunctionSignature functionSignature : this) {
			if (functionSignature.accepts(types)) {
				return functionSignature;
			}
		}
		return FunctionSignature.nullInstance();
	}

	public boolean accepts(List<Type> types) {
		return !acceptingSignature(types).isNull();
	}

	/////////////////////////////////////////////////////////////////////////////////
	// access to FunctionSignatures by key object.

	public static FunctionSignatures nullSignatures = new FunctionSignatures(0, FunctionSignature.nullInstance());

	public static FunctionSignatures signaturesOf(Object key) {
		if (signaturesForKey.containsKey(key)) {
			return signaturesForKey.get(key);
		}
		return nullSignatures;
	}

	public static FunctionSignature signature(Object key, List<Type> types) {
		FunctionSignatures signatures = FunctionSignatures.signaturesOf(key);
		return signatures.acceptingSignature(types);
	}

	/////////////////////////////////////////////////////////////////////////////////
	// Put the signatures for operators in the following static block.

	static {
		// here's one example to get you started with FunctionSignatures: the signatures
		// for addition.
		// for this to work, you should statically import PrimitiveType.*

		// Defining arithmetic operations in ASMCodeGenerator
		new FunctionSignatures(Punctuator.ADD,
				new FunctionSignature(ASMOpcode.Nop, INTEGER, INTEGER),
				new FunctionSignature(ASMOpcode.Nop, FLOAT, FLOAT),
				new FunctionSignature(ASMOpcode.Add, INTEGER, INTEGER, INTEGER),
				new FunctionSignature(ASMOpcode.FAdd, FLOAT, FLOAT, FLOAT));

		new FunctionSignatures(Punctuator.SUBTRACT,
				new FunctionSignature(ASMOpcode.Negate, INTEGER, INTEGER),
				new FunctionSignature(ASMOpcode.FNegate, FLOAT, FLOAT),
				new FunctionSignature(ASMOpcode.Subtract, INTEGER, INTEGER, INTEGER),
				new FunctionSignature(ASMOpcode.FSubtract, FLOAT, FLOAT, FLOAT));

		new FunctionSignatures(Punctuator.MULTIPLY,
				new FunctionSignature(ASMOpcode.Multiply, INTEGER, INTEGER, INTEGER),
				new FunctionSignature(ASMOpcode.FMultiply, FLOAT, FLOAT, FLOAT));

		new FunctionSignatures(Punctuator.DIVIDE,
				new FunctionSignature(ASMOpcode.Divide, INTEGER, INTEGER, INTEGER),
				new FunctionSignature(ASMOpcode.FDivide, FLOAT, FLOAT, FLOAT));

		// Defining comparison operations in ASMCodeGenerator
		new FunctionSignatures(Punctuator.GREATER,
				new FunctionSignature(ASMOpcode.Nop, INTEGER, INTEGER, BOOLEAN),
				new FunctionSignature(ASMOpcode.Nop, FLOAT, FLOAT, BOOLEAN),
				new FunctionSignature(ASMOpcode.Nop, CHARACTER, CHARACTER, BOOLEAN));

		new FunctionSignatures(Punctuator.GREATEREQUAL,
				new FunctionSignature(ASMOpcode.Nop, INTEGER, INTEGER, BOOLEAN),
				new FunctionSignature(ASMOpcode.Nop, FLOAT, FLOAT, BOOLEAN),
				new FunctionSignature(ASMOpcode.Nop, CHARACTER, CHARACTER, BOOLEAN));

		new FunctionSignatures(Punctuator.EQUALEQUAL,
				new FunctionSignature(ASMOpcode.Nop, INTEGER, INTEGER, BOOLEAN),
				new FunctionSignature(ASMOpcode.Nop, FLOAT, FLOAT, BOOLEAN),
				new FunctionSignature(ASMOpcode.Nop, BOOLEAN, BOOLEAN, BOOLEAN),
				new FunctionSignature(ASMOpcode.Nop, CHARACTER, CHARACTER, BOOLEAN),
				new FunctionSignature(ASMOpcode.Nop, STRING, STRING, BOOLEAN));

		new FunctionSignatures(Punctuator.NOTEQUAL,
				new FunctionSignature(ASMOpcode.Nop, INTEGER, INTEGER, BOOLEAN),
				new FunctionSignature(ASMOpcode.Nop, FLOAT, FLOAT, BOOLEAN),
				new FunctionSignature(ASMOpcode.Nop, BOOLEAN, BOOLEAN, BOOLEAN),
				new FunctionSignature(ASMOpcode.Nop, CHARACTER, CHARACTER, BOOLEAN),
				new FunctionSignature(ASMOpcode.Nop, STRING, STRING, BOOLEAN));

		new FunctionSignatures(Punctuator.LESSER,
				new FunctionSignature(ASMOpcode.Nop, INTEGER, INTEGER, BOOLEAN),
				new FunctionSignature(ASMOpcode.Nop, FLOAT, FLOAT, BOOLEAN),
				new FunctionSignature(ASMOpcode.Nop, CHARACTER, CHARACTER, BOOLEAN));

		new FunctionSignatures(Punctuator.LESSEREQUAL,
				new FunctionSignature(ASMOpcode.Nop, INTEGER, INTEGER, BOOLEAN),
				new FunctionSignature(ASMOpcode.Nop, FLOAT, FLOAT, BOOLEAN),
				new FunctionSignature(ASMOpcode.Nop, CHARACTER, CHARACTER, BOOLEAN));

		TypeVariable T = new TypeVariable("T");
		List<TypeVariable> T_list = Arrays.asList(T);

		new FunctionSignatures(Punctuator.CAST,
				new FunctionSignature(ASMOpcode.Nop, BOOLEAN, BOOLEAN, BOOLEAN), // boolean to boolean
				new FunctionSignature(ASMOpcode.Nop, CHARACTER, CHARACTER, CHARACTER), // char to char
				new FunctionSignature(ASMOpcode.Nop, INTEGER, INTEGER, INTEGER), // int to int
				new FunctionSignature(ASMOpcode.Nop, FLOAT, FLOAT, FLOAT), // float to float
				new FunctionSignature(ASMOpcode.Nop, STRING, STRING, STRING), // string to string
				new FunctionSignature(ASMOpcode.Nop, T_list, new ArrayType(T), new ArrayType(T), new ArrayType(T)), // array to array

				new FunctionSignature(ASMOpcode.Nop, CHARACTER, INTEGER, INTEGER), 
				new FunctionSignature(ASMOpcode.ConvertF, INTEGER, FLOAT, FLOAT),
				new FunctionSignature(ASMOpcode.ConvertI, FLOAT, INTEGER, INTEGER),

				new FunctionSignature(new IntToBoolCodeGenerator(), BOOLEAN, INTEGER, BOOLEAN), // int to boolean
				new FunctionSignature(new IntToCharCodeGenerator(), CHARACTER, INTEGER, CHARACTER), // int to char
				new FunctionSignature(new CharToBoolCodeGenerator(), BOOLEAN, CHARACTER, BOOLEAN) // char to boolean

				// new FunctionSignature(ASMOpcode.Nop, INTEGER, CHARACTER, INTEGER), // int to char
				// new FunctionSignature(ASMOpcode.ConvertI, INTEGER, FLOAT, INTEGER), // float to int
				// new FunctionSignature(ASMOpcode.ConvertF, FLOAT, INTEGER, FLOAT), // int to float
				// new FunctionSignature(new IntToBoolCodeGenerator(), BOOLEAN, INTEGER, BOOLEAN), // int to boolean
				// new FunctionSignature(new IntToCharCodeGenerator(), CHARACTER, INTEGER, CHARACTER), // int to char
				// new FunctionSignature(new CharToBoolCodeGenerator(), BOOLEAN, CHARACTER, BOOLEAN) // char to boolean
				); 
		
		new FunctionSignatures(Punctuator.AND,
				new FunctionSignature(ASMOpcode.And, BOOLEAN, BOOLEAN, BOOLEAN));
		
		new FunctionSignatures(Punctuator.OR,
				new FunctionSignature(ASMOpcode.Or, BOOLEAN, BOOLEAN, BOOLEAN));
		
		new FunctionSignatures(Punctuator.NOT,
				new FunctionSignature(ASMOpcode.BNegate, BOOLEAN, BOOLEAN));
		
		new FunctionSignatures(Keyword.LENGTH,
				new FunctionSignature(new ArrayLengthCodeGenerator(), new ArrayType(), INTEGER));

		// First, we use the operator itself (in this case the Punctuator ADD) as the
		// key.
		// Then, we give that key two signatures: one an (INT x INT -> INT) and the
		// other
		// a (FLOAT x FLOAT -> FLOAT). Each signature has a "whichVariant" parameter
		// where
		// I'm placing the instruction (ASMOpcode) that needs to be executed.
		//
		// I'll follow the convention that if a signature has an ASMOpcode for its
		// whichVariant,
		// then to generate code for the operation, one only needs to generate the code
		// for
		// the operands (in order) and then add to that the Opcode. For instance, the
		// code for
		// floating addition should look like:
		//
		// (generate argument 1) : may be many instructions
		// (generate argument 2) : ditto
		// FAdd : just one instruction
		//
		// If the code that an operator should generate is more complicated than this,
		// then
		// I will not use an ASMOpcode for the whichVariant. In these cases I typically
		// use
		// a small object with one method (the "Command" design pattern) that generates
		// the
		// required code.

	}

}
