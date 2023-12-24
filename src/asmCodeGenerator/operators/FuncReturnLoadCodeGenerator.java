package asmCodeGenerator.operators;

import asmCodeGenerator.codeStorage.*;
import asmCodeGenerator.codeStorage.ASMCodeFragment.CodeType;
import parseTree.ParseNode;
import semanticAnalyzer.types.*;

public class FuncReturnLoadCodeGenerator implements SimpleCodeGenerator {
    public Type type;

    public FuncReturnLoadCodeGenerator(Type type) {
        this.type = type;
    }

    @Override
    public ASMCodeFragment generate(ParseNode node) {
        ASMCodeFragment frag = new ASMCodeFragment(CodeType.GENERATES_VALUE);

        if (type == PrimitiveType.INTEGER || type == TypeLiteral.TYPE_INTEGER) {
            frag.add(ASMOpcode.LoadI);
        } else if (type == PrimitiveType.FLOAT || type == TypeLiteral.TYPE_FLOATING) {
            frag.add(ASMOpcode.LoadF);
        } else if (type == PrimitiveType.BOOLEAN || type == TypeLiteral.TYPE_BOOLEAN) {
            frag.add(ASMOpcode.LoadC);
        } else if (type == PrimitiveType.CHARACTER || type == TypeLiteral.TYPE_CHARACTER) {
            frag.add(ASMOpcode.LoadC);
        } else if (type == PrimitiveType.STRING || type == TypeLiteral.TYPE_STRING) {
            frag.add(ASMOpcode.LoadI);
        } else if (type == PrimitiveType.VOID || type == TypeLiteral.TYPE_VOID) {
        } else if (type instanceof ArrayType) {
            frag.add(ASMOpcode.LoadI);
        } else if (type instanceof FunctionType) {
            frag.add(ASMOpcode.LoadI);
        } else {
            assert false : "Type " + type + " unimplemented in opcodeForLoad()";
        }

        return frag;
    }
}