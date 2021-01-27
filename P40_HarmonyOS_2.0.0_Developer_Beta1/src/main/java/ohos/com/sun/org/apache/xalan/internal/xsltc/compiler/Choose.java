package ohos.com.sun.org.apache.xalan.internal.xsltc.compiler;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;
import ohos.com.sun.org.apache.bcel.internal.generic.BranchHandle;
import ohos.com.sun.org.apache.bcel.internal.generic.BranchInstruction;
import ohos.com.sun.org.apache.bcel.internal.generic.GOTO;
import ohos.com.sun.org.apache.bcel.internal.generic.IFEQ;
import ohos.com.sun.org.apache.bcel.internal.generic.InstructionHandle;
import ohos.com.sun.org.apache.bcel.internal.generic.InstructionList;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ClassGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ErrorMsg;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.MethodGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.TypeCheckError;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Util;

/* access modifiers changed from: package-private */
public final class Choose extends Instruction {
    Choose() {
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void display(int i) {
        indent(i);
        Util.println("Choose");
        int i2 = i + 4;
        indent(i2);
        displayContents(i2);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Instruction, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void translate(ClassGenerator classGenerator, MethodGenerator methodGenerator) {
        Vector vector = new Vector();
        Iterator<SyntaxTreeNode> elements = elements();
        getLineNumber();
        Otherwise otherwise = null;
        while (elements.hasNext()) {
            SyntaxTreeNode next = elements.next();
            if (next instanceof When) {
                vector.addElement(next);
            } else if (next instanceof Otherwise) {
                if (otherwise == null) {
                    otherwise = (Otherwise) next;
                } else {
                    getParser().reportError(3, new ErrorMsg(ErrorMsg.MULTIPLE_OTHERWISE_ERR, (SyntaxTreeNode) this));
                }
            } else if (next instanceof Text) {
                ((Text) next).ignore();
            } else {
                getParser().reportError(3, new ErrorMsg(ErrorMsg.WHEN_ELEMENT_ERR, (SyntaxTreeNode) this));
            }
        }
        if (vector.size() == 0) {
            getParser().reportError(3, new ErrorMsg(ErrorMsg.MISSING_WHEN_ERR, (SyntaxTreeNode) this));
            return;
        }
        InstructionList instructionList = methodGenerator.getInstructionList();
        Vector vector2 = new Vector();
        Enumeration elements2 = vector.elements();
        BranchHandle branchHandle = null;
        InstructionHandle instructionHandle = null;
        while (elements2.hasMoreElements()) {
            When when = (When) elements2.nextElement();
            Expression test = when.getTest();
            instructionList.getEnd();
            if (branchHandle != null) {
                branchHandle.setTarget(instructionList.append(NOP));
            }
            test.translateDesynthesized(classGenerator, methodGenerator);
            if (test instanceof FunctionCall) {
                try {
                    if (((FunctionCall) test).typeCheck(getParser().getSymbolTable()) != Type.Boolean) {
                        test._falseList.add(instructionList.append((BranchInstruction) new IFEQ(null)));
                    }
                } catch (TypeCheckError unused) {
                }
            }
            InstructionHandle end = instructionList.getEnd();
            if (!when.ignore()) {
                when.translateContents(classGenerator, methodGenerator);
            }
            vector2.addElement(instructionList.append((BranchInstruction) new GOTO(null)));
            if (elements2.hasMoreElements() || otherwise != null) {
                branchHandle = instructionList.append((BranchInstruction) new GOTO(null));
                test.backPatchFalseList(branchHandle);
            } else {
                instructionHandle = instructionList.append(NOP);
                test.backPatchFalseList(instructionHandle);
            }
            test.backPatchTrueList(end.getNext());
        }
        if (otherwise != null) {
            branchHandle.setTarget(instructionList.append(NOP));
            otherwise.translateContents(classGenerator, methodGenerator);
            instructionHandle = instructionList.append(NOP);
        }
        Enumeration elements3 = vector2.elements();
        while (elements3.hasMoreElements()) {
            ((BranchHandle) elements3.nextElement()).setTarget(instructionHandle);
        }
    }
}
