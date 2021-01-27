package ohos.com.sun.org.apache.xalan.internal.xsltc.compiler;

import ohos.com.sun.org.apache.bcel.internal.generic.CHECKCAST;
import ohos.com.sun.org.apache.bcel.internal.generic.ConstantPoolGen;
import ohos.com.sun.org.apache.bcel.internal.generic.GETFIELD;
import ohos.com.sun.org.apache.bcel.internal.generic.INVOKEINTERFACE;
import ohos.com.sun.org.apache.bcel.internal.generic.InstructionList;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ClassGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.MethodGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.NodeSetType;

/* access modifiers changed from: package-private */
public final class VariableRef extends VariableRefBase {
    public VariableRef(Variable variable) {
        super(variable);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void translate(ClassGenerator classGenerator, MethodGenerator methodGenerator) {
        ConstantPoolGen constantPool = classGenerator.getConstantPool();
        InstructionList instructionList = methodGenerator.getInstructionList();
        if (!this._type.implementedAsMethod()) {
            String escapedName = this._variable.getEscapedName();
            String signature = this._type.toSignature();
            if (!this._variable.isLocal()) {
                String className = classGenerator.getClassName();
                instructionList.append(classGenerator.loadTranslet());
                if (classGenerator.isExternal()) {
                    instructionList.append(new CHECKCAST(constantPool.addClass(className)));
                }
                instructionList.append(new GETFIELD(constantPool.addFieldref(className, escapedName, signature)));
            } else if (classGenerator.isExternal()) {
                Closure closure = this._closure;
                while (closure != null && !closure.inInnerClass()) {
                    closure = closure.getParentClosure();
                }
                if (closure != null) {
                    instructionList.append(ALOAD_0);
                    instructionList.append(new GETFIELD(constantPool.addFieldref(closure.getInnerClassName(), escapedName, signature)));
                } else {
                    instructionList.append(this._variable.loadInstruction());
                }
            } else {
                instructionList.append(this._variable.loadInstruction());
            }
            if (this._variable.getType() instanceof NodeSetType) {
                instructionList.append(new INVOKEINTERFACE(constantPool.addInterfaceMethodref(Constants.NODE_ITERATOR, "cloneIterator", "()Lohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;"), 1));
            }
        }
    }
}
