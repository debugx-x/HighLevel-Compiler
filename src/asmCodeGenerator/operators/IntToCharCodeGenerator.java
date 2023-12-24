package asmCodeGenerator.operators;

import asmCodeGenerator.codeStorage.ASMCodeFragment;
import asmCodeGenerator.codeStorage.ASMOpcode;
import asmCodeGenerator.codeStorage.ASMCodeFragment.CodeType;
import parseTree.ParseNode;

public class IntToCharCodeGenerator implements SimpleCodeGenerator {

    @Override
    public ASMCodeFragment generate(ParseNode node) {
        ASMCodeFragment frag = new ASMCodeFragment(CodeType.GENERATES_VALUE);

        frag.add(ASMOpcode.PushI, 127);
        frag.add(ASMOpcode.BTAnd);

        return frag;

    }

}
