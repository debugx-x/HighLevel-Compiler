package asmCodeGenerator.operators;

import asmCodeGenerator.codeStorage.*;
import asmCodeGenerator.codeStorage.ASMCodeFragment.CodeType;
import asmCodeGenerator.runtime.RunTime;
import parseTree.ParseNode;
import semanticAnalyzer.types.*;

public class FunctionReturnStoreCodeGenerator implements SimpleCodeGenerator {
    public Type type;

    public FunctionReturnStoreCodeGenerator(Type type) {
        this.type = type;
    }

    @Override
    public ASMCodeFragment generate(ParseNode node) {
        ASMCodeFragment frag = new ASMCodeFragment(CodeType.GENERATES_VALUE);

        if (type == PrimitiveType.INTEGER) {
            frag.add(ASMOpcode.PushD, RunTime.STACK_POINTER);
            frag.add(ASMOpcode.LoadI);
            frag.add(ASMOpcode.Exchange);
            frag.add(ASMOpcode.StoreI);
        } else if (type == PrimitiveType.FLOAT) {
            frag.add(ASMOpcode.PushD, RunTime.STACK_POINTER);
            frag.add(ASMOpcode.LoadI);
            frag.add(ASMOpcode.Exchange);
            frag.add(ASMOpcode.StoreF);
        } else if (type == PrimitiveType.BOOLEAN) {
            frag.add(ASMOpcode.PushD, RunTime.STACK_POINTER);
            frag.add(ASMOpcode.LoadI);
            frag.add(ASMOpcode.Exchange);
            frag.add(ASMOpcode.StoreC);
        } else if (type == PrimitiveType.CHARACTER) {
            frag.add(ASMOpcode.PushD, RunTime.STACK_POINTER);
            frag.add(ASMOpcode.LoadI);
            frag.add(ASMOpcode.Exchange);
            frag.add(ASMOpcode.StoreC);
        } else if (type == PrimitiveType.STRING) {
            frag.add(ASMOpcode.PushD, RunTime.STACK_POINTER);
            frag.add(ASMOpcode.LoadI);
            frag.add(ASMOpcode.Exchange);
            frag.add(ASMOpcode.StoreI);
        } else if (type == PrimitiveType.VOID) {
        } else if (type instanceof ArrayType) {
            frag.add(ASMOpcode.PushD, RunTime.STACK_POINTER);
            frag.add(ASMOpcode.LoadI);
            frag.add(ASMOpcode.Exchange);
            frag.add(ASMOpcode.StoreI);
        } else if (type instanceof FunctionType) {
            frag.add(ASMOpcode.PushD, RunTime.STACK_POINTER);
            frag.add(ASMOpcode.LoadI);
            frag.add(ASMOpcode.Exchange);
            frag.add(ASMOpcode.StoreI);
        } else {
            assert false : "Type " + type + " unimplemented in opcodeForStore()";
        }

        return frag;
    }
}