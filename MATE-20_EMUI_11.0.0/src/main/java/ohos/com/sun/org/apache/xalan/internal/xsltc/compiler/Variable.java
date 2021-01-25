package ohos.com.sun.org.apache.xalan.internal.xsltc.compiler;

import ohos.com.sun.org.apache.bcel.internal.classfile.Field;
import ohos.com.sun.org.apache.bcel.internal.generic.ACONST_NULL;
import ohos.com.sun.org.apache.bcel.internal.generic.ConstantPoolGen;
import ohos.com.sun.org.apache.bcel.internal.generic.DCONST;
import ohos.com.sun.org.apache.bcel.internal.generic.ICONST;
import ohos.com.sun.org.apache.bcel.internal.generic.InstructionHandle;
import ohos.com.sun.org.apache.bcel.internal.generic.InstructionList;
import ohos.com.sun.org.apache.bcel.internal.generic.PUTFIELD;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.BooleanType;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ClassGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ErrorMsg;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.IntType;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.MethodGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.NodeType;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.RealType;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.TypeCheckError;
import ohos.com.sun.org.apache.xpath.internal.XPath;

/* access modifiers changed from: package-private */
public final class Variable extends VariableBase {
    Variable() {
    }

    public int getIndex() {
        if (this._local != null) {
            return this._local.getIndex();
        }
        return -1;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.VariableBase, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void parseContents(Parser parser) {
        super.parseContents(parser);
        SyntaxTreeNode parent = getParent();
        if (parent instanceof Stylesheet) {
            this._isLocal = false;
            Variable lookupVariable = parser.getSymbolTable().lookupVariable(this._name);
            if (lookupVariable != null) {
                int importPrecedence = getImportPrecedence();
                int importPrecedence2 = lookupVariable.getImportPrecedence();
                if (importPrecedence == importPrecedence2) {
                    reportError(this, parser, ErrorMsg.VARIABLE_REDEF_ERR, this._name.toString());
                } else if (importPrecedence2 > importPrecedence) {
                    this._ignore = true;
                    copyReferences(lookupVariable);
                    return;
                } else {
                    lookupVariable.copyReferences(this);
                    lookupVariable.disable();
                }
            }
            ((Stylesheet) parent).addVariable(this);
            parser.getSymbolTable().addVariable(this);
            return;
        }
        this._isLocal = true;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.TopLevelElement, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public Type typeCheck(SymbolTable symbolTable) throws TypeCheckError {
        if (this._select != null) {
            this._type = this._select.typeCheck(symbolTable);
        } else if (hasContents()) {
            typeCheckContents(symbolTable);
            this._type = Type.ResultTree;
        } else {
            this._type = Type.Reference;
        }
        return Type.Void;
    }

    public void initialize(ClassGenerator classGenerator, MethodGenerator methodGenerator) {
        classGenerator.getConstantPool();
        InstructionList instructionList = methodGenerator.getInstructionList();
        if (isLocal() && !this._refs.isEmpty()) {
            if (this._local == null) {
                this._local = methodGenerator.addLocalVariable2(getEscapedName(), this._type.toJCType(), null);
            }
            if ((this._type instanceof IntType) || (this._type instanceof NodeType) || (this._type instanceof BooleanType)) {
                instructionList.append(new ICONST(0));
            } else if (this._type instanceof RealType) {
                instructionList.append(new DCONST(XPath.MATCH_SCORE_QNAME));
            } else {
                instructionList.append(new ACONST_NULL());
            }
            this._local.setStart(instructionList.append(this._type.STORE(this._local.getIndex())));
        }
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.TopLevelElement, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void translate(ClassGenerator classGenerator, MethodGenerator methodGenerator) {
        ConstantPoolGen constantPool = classGenerator.getConstantPool();
        InstructionList instructionList = methodGenerator.getInstructionList();
        boolean z = true;
        if (this._refs.isEmpty()) {
            this._ignore = true;
        }
        if (!this._ignore) {
            this._ignore = true;
            String escapedName = getEscapedName();
            if (isLocal()) {
                translateValue(classGenerator, methodGenerator);
                if (this._local != null) {
                    z = false;
                }
                if (z) {
                    mapRegister(methodGenerator);
                }
                InstructionHandle append = instructionList.append(this._type.STORE(this._local.getIndex()));
                if (z) {
                    this._local.setStart(append);
                    return;
                }
                return;
            }
            String signature = this._type.toSignature();
            if (classGenerator.containsField(escapedName) == null) {
                classGenerator.addField(new Field(1, constantPool.addUtf8(escapedName), constantPool.addUtf8(signature), null, constantPool.getConstantPool()));
                instructionList.append(classGenerator.loadTranslet());
                translateValue(classGenerator, methodGenerator);
                instructionList.append(new PUTFIELD(constantPool.addFieldref(classGenerator.getClassName(), escapedName, signature)));
            }
        }
    }
}
