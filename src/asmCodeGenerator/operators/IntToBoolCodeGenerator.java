package asmCodeGenerator.operators;

import static asmCodeGenerator.codeStorage.ASMOpcode.And;
import static asmCodeGenerator.codeStorage.ASMOpcode.PushI;

import asmCodeGenerator.codeStorage.ASMCodeFragment;
import asmCodeGenerator.codeStorage.ASMCodeFragment.CodeType;
import parseTree.ParseNode;

public class IntToBoolCodeGenerator implements SimpleCodeGenerator {

    @Override
    public ASMCodeFragment generate(ParseNode node) {
        ASMCodeFragment frag = new ASMCodeFragment(CodeType.GENERATES_VALUE);

        frag.add(PushI, 1);
        frag.add(And);

        return frag;

    }

}
