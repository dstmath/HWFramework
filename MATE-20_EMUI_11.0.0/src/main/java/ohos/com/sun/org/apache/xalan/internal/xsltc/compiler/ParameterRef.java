package ohos.com.sun.org.apache.xalan.internal.xsltc.compiler;

import ohos.com.sun.org.apache.bcel.internal.generic.CHECKCAST;
import ohos.com.sun.org.apache.bcel.internal.generic.ConstantPoolGen;
import ohos.com.sun.org.apache.bcel.internal.generic.GETFIELD;
import ohos.com.sun.org.apache.bcel.internal.generic.INVOKEINTERFACE;
import ohos.com.sun.org.apache.bcel.internal.generic.InstructionList;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ClassGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.MethodGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.NodeSetType;
import ohos.com.sun.org.apache.xalan.internal.xsltc.runtime.BasisLibrary;

final class ParameterRef extends VariableRefBase {
    QName _name = null;

    public ParameterRef(Param param) {
        super(param);
        this._name = param._name;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.VariableRefBase, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression
    public String toString() {
        return "parameter-ref(" + this._variable.getName() + '/' + this._variable.getType() + ')';
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void translate(ClassGenerator classGenerator, MethodGenerator methodGenerator) {
        ConstantPoolGen constantPool = classGenerator.getConstantPool();
        InstructionList instructionList = methodGenerator.getInstructionList();
        String mapQNameToJavaName = BasisLibrary.mapQNameToJavaName(this._name.toString());
        String signature = this._type.toSignature();
        if (!this._variable.isLocal()) {
            String className = classGenerator.getClassName();
            instructionList.append(classGenerator.loadTranslet());
            if (classGenerator.isExternal()) {
                instructionList.append(new CHECKCAST(constantPool.addClass(className)));
            }
            instructionList.append(new GETFIELD(constantPool.addFieldref(className, mapQNameToJavaName, signature)));
        } else if (classGenerator.isExternal()) {
            Closure closure = this._closure;
            while (closure != null && !closure.inInnerClass()) {
                closure = closure.getParentClosure();
            }
            if (closure != null) {
                instructionList.append(ALOAD_0);
                instructionList.append(new GETFIELD(constantPool.addFieldref(closure.getInnerClassName(), mapQNameToJavaName, signature)));
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
