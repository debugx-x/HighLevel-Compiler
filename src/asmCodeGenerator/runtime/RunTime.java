package asmCodeGenerator.runtime;
import static asmCodeGenerator.codeStorage.ASMCodeFragment.CodeType.*;
import static asmCodeGenerator.codeStorage.ASMOpcode.*;
import asmCodeGenerator.codeStorage.ASMCodeFragment;
public class RunTime {
	public static final String EAT_LOCATION_ZERO      = "$eat-location-zero";		// helps us distinguish null pointers from real ones.
	public static final String INTEGER_PRINT_FORMAT   = "$print-format-integer";
	public static final String FLOAT_PRINT_FORMAT 	  = "$print-format-float";
	public static final String BOOLEAN_PRINT_FORMAT   = "$print-format-boolean";
	public static final String CHARACTER_PRINT_FORMAT = "$print-format-character";
	public static final String STRING_PRINT_FORMAT    = "$print-format-string";
	public static final String FUNCTION_PRINT_FORMAT  = "$print-format-function";
	public static final String NEWLINE_PRINT_FORMAT   = "$print-format-newline";
	public static final String SPACE_PRINT_FORMAT     = "$print-format-space";
	public static final String TAB_PRINT_FORMAT       = "$print-format-tab";
	public static final String BOOLEAN_TRUE_STRING    = "$boolean-true-string";
	public static final String BOOLEAN_FALSE_STRING   = "$boolean-false-string";
	public static final String GLOBAL_MEMORY_BLOCK    = "$global-memory-block";
	public static final String USABLE_MEMORY_START    = "$usable-memory-start";
	public static final String STACK_POINTER 		  = "$stack-pointer";
	public static final String FRAME_POINTER 		  = "$frame-pointer";
	public static final String MAIN_PROGRAM_LABEL     = "$$main";
	public static final String FUNC_RETURN_ADDR_TEMP  = "$func-return-addr-temp";
	
	public static final String GENERAL_RUNTIME_ERROR = "$$general-runtime-error";
	public static final String INTEGER_DIVIDE_BY_ZERO_RUNTIME_ERROR = "$$i-divide-by-zero";
	public static final String FLOAT_DIVIDE_BY_ZERO_RUNTIME_ERROR = "$$f-divide-by-zero";
	public static final String NEGATIVE_INDEX_RUNTIME_ERROR 	= "$$negative-index";
	public static final String OUT_OF_BOUNDS_INDEX_RUNTIME_ERROR = "$$out-of-bounds-index";
	public static final String FUNCTION_RUNOFF_RUNTIME_ERROR = "$$function-runoff";
	
	public static final String ARRAY_TEMP_1					= "$array-temp-1";
	public static final String ARRAY_TEMP_2					= "$array-temp-2";
	public static final String ARRAY_TEMP_3					= "$array-temp-3";
	public static final String ARRAY_TEMP_4					= "$array-temp-4";
	public static final String ARRAY_TEMP_5					= "$array-temp-5";
	public static final String ARRAY_TEMP_6					= "$array-temp-6";
	public static final String PRINT_TEMP_1					= "$print-temp-1";
	public static final String PRINT_TEMP_2 				= "$print-temp-2";
	public static final String PRINT_TEMP_3 				= "$print-temp-3";
	public static final String INDEX_TEMP_1					= "$index-temp-1";
	public static final String INDEX_TEMP_2					= "$index-temp-2";

	private ASMCodeFragment environmentASM() {
		ASMCodeFragment result = new ASMCodeFragment(GENERATES_VOID);
		result.append(jumpToMain());
		result.append(stringsForPrintf());
		result.append(runtimeErrors());
		result.append(temporaryStorage());
		result.add(DLabel, USABLE_MEMORY_START);
		return result;
	}
	
	private ASMCodeFragment jumpToMain() {
		ASMCodeFragment frag = new ASMCodeFragment(GENERATES_VOID);
		frag.add(Jump, MAIN_PROGRAM_LABEL);
		return frag;
	}

	private ASMCodeFragment stringsForPrintf() {
		ASMCodeFragment frag = new ASMCodeFragment(GENERATES_VOID);
		frag.add(DLabel, EAT_LOCATION_ZERO);
		frag.add(DataZ, 8);
		frag.add(DLabel, INTEGER_PRINT_FORMAT);
		frag.add(DataS, "%d");
		frag.add(DLabel, FLOAT_PRINT_FORMAT);
		frag.add(DataS, "%f");
		frag.add(DLabel, BOOLEAN_PRINT_FORMAT);
		frag.add(DataS, "%s");
		frag.add(DLabel, CHARACTER_PRINT_FORMAT);
		frag.add(DataS, "%c");
		frag.add(DLabel, STRING_PRINT_FORMAT);
		frag.add(DataS, "%s");
		frag.add(DLabel, FUNCTION_PRINT_FORMAT);
		frag.add(DataS, "<function>");
		frag.add(DLabel, NEWLINE_PRINT_FORMAT);
		frag.add(DataS, "\n");
		frag.add(DLabel, TAB_PRINT_FORMAT);
		frag.add(DataS, "\t");
		frag.add(DLabel, SPACE_PRINT_FORMAT);
		frag.add(DataS, " ");
		frag.add(DLabel, BOOLEAN_TRUE_STRING);
		frag.add(DataS, "true");
		frag.add(DLabel, BOOLEAN_FALSE_STRING);
		frag.add(DataS, "false");
	
		return frag;
	}
	
	
	private ASMCodeFragment runtimeErrors() {
		ASMCodeFragment frag = new ASMCodeFragment(GENERATES_VOID);
		
		generalRuntimeError(frag);
		integerDivideByZeroError(frag);
		floatDivideByZeroError(frag);
		NegativeIndexError(frag);
		OutOfBoundsIndexError(frag);
		functionRunoffError(frag);
		
		return frag;
	}
	
	private ASMCodeFragment generalRuntimeError(ASMCodeFragment frag) {
		String generalErrorMessage = "$errors-general-message";

		frag.add(DLabel, generalErrorMessage);
		frag.add(DataS, "Runtime error: %s\n");
		
		frag.add(Label, GENERAL_RUNTIME_ERROR);
		frag.add(PushD, generalErrorMessage);
		frag.add(Printf);
		frag.add(Halt);
		return frag;
	}
	private void integerDivideByZeroError(ASMCodeFragment frag) {
		String intDivideByZeroMessage = "$errors-int-divide-by-zero";
		
		frag.add(DLabel, intDivideByZeroMessage);
		frag.add(DataS, "integer divide by zero");
		
		frag.add(Label, INTEGER_DIVIDE_BY_ZERO_RUNTIME_ERROR);
		frag.add(PushD, intDivideByZeroMessage);
		frag.add(Jump, GENERAL_RUNTIME_ERROR);
	}

	private void floatDivideByZeroError(ASMCodeFragment frag) {
		String floatDivideByZeroMessage = "$errors-float-divide-by-zero";
		
		frag.add(DLabel, floatDivideByZeroMessage);
		frag.add(DataS, "float divide by zero");
		
		frag.add(Label, FLOAT_DIVIDE_BY_ZERO_RUNTIME_ERROR);
		frag.add(PushD, floatDivideByZeroMessage);
		frag.add(Jump, GENERAL_RUNTIME_ERROR);
	}
	
	private void NegativeIndexError(ASMCodeFragment frag) {
		String negativeIndexMessage = "$errors-negative-index";

		frag.add(DLabel, negativeIndexMessage);
		frag.add(DataS, "negative index used for array");

		frag.add(Label, NEGATIVE_INDEX_RUNTIME_ERROR);
		frag.add(PushD, negativeIndexMessage);
		frag.add(Jump, GENERAL_RUNTIME_ERROR);
	}
	
	private void OutOfBoundsIndexError(ASMCodeFragment frag) {
		String outIndexMessage = "$errors-out-of-bounds-index";

		frag.add(DLabel, outIndexMessage);
		frag.add(DataS, "index out of bounds");

		frag.add(Label, OUT_OF_BOUNDS_INDEX_RUNTIME_ERROR);
		frag.add(PushD, outIndexMessage);
		frag.add(Jump, GENERAL_RUNTIME_ERROR);
	}

	private void functionRunoffError(ASMCodeFragment frag) {
		String functionRunoffMessage = "$errors-function-runoff";

		frag.add(DLabel, functionRunoffMessage);
		frag.add(DataS, "code run off the end of a function");

		frag.add(Label, FUNCTION_RUNOFF_RUNTIME_ERROR);
		frag.add(PushD, functionRunoffMessage);
		frag.add(Jump, GENERAL_RUNTIME_ERROR);
	}
	
	private ASMCodeFragment temporaryStorage() {
		ASMCodeFragment frag = new ASMCodeFragment(GENERATES_VOID);

		frag.add(DLabel, ARRAY_TEMP_1);
		frag.add(DataI, 0);
		frag.add(DLabel, ARRAY_TEMP_2);
		frag.add(DataI, 0);
		frag.add(DLabel, ARRAY_TEMP_3);
		frag.add(DataI, 0);
		frag.add(DLabel, ARRAY_TEMP_4);
		frag.add(DataI, 0);
		frag.add(DLabel, ARRAY_TEMP_5);
		frag.add(DataI, 0);
		frag.add(DLabel, ARRAY_TEMP_6);
		frag.add(DataI, 0);

		frag.add(DLabel, PRINT_TEMP_1);
		frag.add(DataI, 0);
		frag.add(DLabel, PRINT_TEMP_2);
		frag.add(DataI, 0);
		frag.add(DLabel, PRINT_TEMP_3);
		frag.add(DataI, 0);
		
		frag.add(DLabel, INDEX_TEMP_1);
		frag.add(DataI, 0);
		frag.add(DLabel, INDEX_TEMP_2);
		frag.add(DataI, 0);

		frag.add(DLabel, FUNC_RETURN_ADDR_TEMP);
		frag.add(DataI, 0);

		return frag;
	}
	
	
	public static ASMCodeFragment getEnvironment() {
		RunTime rt = new RunTime();
		return rt.environmentASM();
	}
}
