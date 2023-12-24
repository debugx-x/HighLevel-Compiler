package asmCodeGenerator.operators;

import asmCodeGenerator.codeStorage.ASMCodeFragment;
import asmCodeGenerator.codeStorage.ASMOpcode;
import asmCodeGenerator.codeStorage.ASMCodeFragment.CodeType;
import parseTree.ParseNode;

public class CharToBoolCodeGenerator implements SimpleCodeGenerator {

    @Override
    public ASMCodeFragment generate(ParseNode node) {
        ASMCodeFragment frag = new ASMCodeFragment(CodeType.GENERATES_VALUE);

        frag.add(ASMOpcode.PushI, 1);
        frag.add(ASMOpcode.And);

        return frag;

    }

}
