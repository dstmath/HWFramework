package ohos.com.sun.org.apache.bcel.internal.generic;

public interface Visitor {
    void visitAALOAD(AALOAD aaload);

    void visitAASTORE(AASTORE aastore);

    void visitACONST_NULL(ACONST_NULL aconst_null);

    void visitALOAD(ALOAD aload);

    void visitANEWARRAY(ANEWARRAY anewarray);

    void visitARETURN(ARETURN areturn);

    void visitARRAYLENGTH(ARRAYLENGTH arraylength);

    void visitASTORE(ASTORE astore);

    void visitATHROW(ATHROW athrow);

    void visitAllocationInstruction(AllocationInstruction allocationInstruction);

    void visitArithmeticInstruction(ArithmeticInstruction arithmeticInstruction);

    void visitArrayInstruction(ArrayInstruction arrayInstruction);

    void visitBALOAD(BALOAD baload);

    void visitBASTORE(BASTORE bastore);

    void visitBIPUSH(BIPUSH bipush);

    void visitBREAKPOINT(BREAKPOINT breakpoint);

    void visitBranchInstruction(BranchInstruction branchInstruction);

    void visitCALOAD(CALOAD caload);

    void visitCASTORE(CASTORE castore);

    void visitCHECKCAST(CHECKCAST checkcast);

    void visitCPInstruction(CPInstruction cPInstruction);

    void visitConstantPushInstruction(ConstantPushInstruction constantPushInstruction);

    void visitConversionInstruction(ConversionInstruction conversionInstruction);

    void visitD2F(D2F d2f);

    void visitD2I(D2I d2i);

    void visitD2L(D2L d2l);

    void visitDADD(DADD dadd);

    void visitDALOAD(DALOAD daload);

    void visitDASTORE(DASTORE dastore);

    void visitDCMPG(DCMPG dcmpg);

    void visitDCMPL(DCMPL dcmpl);

    void visitDCONST(DCONST dconst);

    void visitDDIV(DDIV ddiv);

    void visitDLOAD(DLOAD dload);

    void visitDMUL(DMUL dmul);

    void visitDNEG(DNEG dneg);

    void visitDREM(DREM drem);

    void visitDRETURN(DRETURN dreturn);

    void visitDSTORE(DSTORE dstore);

    void visitDSUB(DSUB dsub);

    void visitDUP(DUP dup);

    void visitDUP2(DUP2 dup2);

    void visitDUP2_X1(DUP2_X1 dup2_x1);

    void visitDUP2_X2(DUP2_X2 dup2_x2);

    void visitDUP_X1(DUP_X1 dup_x1);

    void visitDUP_X2(DUP_X2 dup_x2);

    void visitExceptionThrower(ExceptionThrower exceptionThrower);

    void visitF2D(F2D f2d);

    void visitF2I(F2I f2i);

    void visitF2L(F2L f2l);

    void visitFADD(FADD fadd);

    void visitFALOAD(FALOAD faload);

    void visitFASTORE(FASTORE fastore);

    void visitFCMPG(FCMPG fcmpg);

    void visitFCMPL(FCMPL fcmpl);

    void visitFCONST(FCONST fconst);

    void visitFDIV(FDIV fdiv);

    void visitFLOAD(FLOAD fload);

    void visitFMUL(FMUL fmul);

    void visitFNEG(FNEG fneg);

    void visitFREM(FREM frem);

    void visitFRETURN(FRETURN freturn);

    void visitFSTORE(FSTORE fstore);

    void visitFSUB(FSUB fsub);

    void visitFieldInstruction(FieldInstruction fieldInstruction);

    void visitFieldOrMethod(FieldOrMethod fieldOrMethod);

    void visitGETFIELD(GETFIELD getfield);

    void visitGETSTATIC(GETSTATIC getstatic);

    void visitGOTO(GOTO v);

    void visitGOTO_W(GOTO_W goto_w);

    void visitGotoInstruction(GotoInstruction gotoInstruction);

    void visitI2B(I2B i2b);

    void visitI2C(I2C i2c);

    void visitI2D(I2D i2d);

    void visitI2F(I2F i2f);

    void visitI2L(I2L i2l);

    void visitI2S(I2S i2s);

    void visitIADD(IADD iadd);

    void visitIALOAD(IALOAD iaload);

    void visitIAND(IAND iand);

    void visitIASTORE(IASTORE iastore);

    void visitICONST(ICONST iconst);

    void visitIDIV(IDIV idiv);

    void visitIFEQ(IFEQ ifeq);

    void visitIFGE(IFGE ifge);

    void visitIFGT(IFGT ifgt);

    void visitIFLE(IFLE ifle);

    void visitIFLT(IFLT iflt);

    void visitIFNE(IFNE ifne);

    void visitIFNONNULL(IFNONNULL ifnonnull);

    void visitIFNULL(IFNULL ifnull);

    void visitIF_ACMPEQ(IF_ACMPEQ if_acmpeq);

    void visitIF_ACMPNE(IF_ACMPNE if_acmpne);

    void visitIF_ICMPEQ(IF_ICMPEQ if_icmpeq);

    void visitIF_ICMPGE(IF_ICMPGE if_icmpge);

    void visitIF_ICMPGT(IF_ICMPGT if_icmpgt);

    void visitIF_ICMPLE(IF_ICMPLE if_icmple);

    void visitIF_ICMPLT(IF_ICMPLT if_icmplt);

    void visitIF_ICMPNE(IF_ICMPNE if_icmpne);

    void visitIINC(IINC iinc);

    void visitILOAD(ILOAD iload);

    void visitIMPDEP1(IMPDEP1 impdep1);

    void visitIMPDEP2(IMPDEP2 impdep2);

    void visitIMUL(IMUL imul);

    void visitINEG(INEG ineg);

    void visitINSTANCEOF(INSTANCEOF v);

    void visitINVOKEINTERFACE(INVOKEINTERFACE invokeinterface);

    void visitINVOKESPECIAL(INVOKESPECIAL invokespecial);

    void visitINVOKESTATIC(INVOKESTATIC invokestatic);

    void visitINVOKEVIRTUAL(INVOKEVIRTUAL invokevirtual);

    void visitIOR(IOR ior);

    void visitIREM(IREM irem);

    void visitIRETURN(IRETURN ireturn);

    void visitISHL(ISHL ishl);

    void visitISHR(ISHR ishr);

    void visitISTORE(ISTORE istore);

    void visitISUB(ISUB isub);

    void visitIUSHR(IUSHR iushr);

    void visitIXOR(IXOR ixor);

    void visitIfInstruction(IfInstruction ifInstruction);

    void visitInvokeInstruction(InvokeInstruction invokeInstruction);

    void visitJSR(JSR jsr);

    void visitJSR_W(JSR_W jsr_w);

    void visitJsrInstruction(JsrInstruction jsrInstruction);

    void visitL2D(L2D l2d);

    void visitL2F(L2F l2f);

    void visitL2I(L2I l2i);

    void visitLADD(LADD ladd);

    void visitLALOAD(LALOAD laload);

    void visitLAND(LAND land);

    void visitLASTORE(LASTORE lastore);

    void visitLCMP(LCMP lcmp);

    void visitLCONST(LCONST lconst);

    void visitLDC(LDC ldc);

    void visitLDC2_W(LDC2_W ldc2_w);

    void visitLDIV(LDIV ldiv);

    void visitLLOAD(LLOAD lload);

    void visitLMUL(LMUL lmul);

    void visitLNEG(LNEG lneg);

    void visitLOOKUPSWITCH(LOOKUPSWITCH lookupswitch);

    void visitLOR(LOR lor);

    void visitLREM(LREM lrem);

    void visitLRETURN(LRETURN lreturn);

    void visitLSHL(LSHL lshl);

    void visitLSHR(LSHR lshr);

    void visitLSTORE(LSTORE lstore);

    void visitLSUB(LSUB lsub);

    void visitLUSHR(LUSHR lushr);

    void visitLXOR(LXOR lxor);

    void visitLoadClass(LoadClass loadClass);

    void visitLoadInstruction(LoadInstruction loadInstruction);

    void visitLocalVariableInstruction(LocalVariableInstruction localVariableInstruction);

    void visitMONITORENTER(MONITORENTER monitorenter);

    void visitMONITOREXIT(MONITOREXIT monitorexit);

    void visitMULTIANEWARRAY(MULTIANEWARRAY multianewarray);

    void visitNEW(NEW v);

    void visitNEWARRAY(NEWARRAY newarray);

    void visitNOP(NOP nop);

    void visitPOP(POP pop);

    void visitPOP2(POP2 pop2);

    void visitPUTFIELD(PUTFIELD putfield);

    void visitPUTSTATIC(PUTSTATIC putstatic);

    void visitPopInstruction(PopInstruction popInstruction);

    void visitPushInstruction(PushInstruction pushInstruction);

    void visitRET(RET ret);

    void visitRETURN(RETURN v);

    void visitReturnInstruction(ReturnInstruction returnInstruction);

    void visitSALOAD(SALOAD saload);

    void visitSASTORE(SASTORE sastore);

    void visitSIPUSH(SIPUSH sipush);

    void visitSWAP(SWAP swap);

    void visitSelect(Select select);

    void visitStackConsumer(StackConsumer stackConsumer);

    void visitStackInstruction(StackInstruction stackInstruction);

    void visitStackProducer(StackProducer stackProducer);

    void visitStoreInstruction(StoreInstruction storeInstruction);

    void visitTABLESWITCH(TABLESWITCH tableswitch);

    void visitTypedInstruction(TypedInstruction typedInstruction);

    void visitUnconditionalBranch(UnconditionalBranch unconditionalBranch);

    void visitVariableLengthInstruction(VariableLengthInstruction variableLengthInstruction);
}
