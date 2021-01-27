package ohos.com.sun.org.apache.xalan.internal.xsltc.compiler;

import java.io.PrintStream;
import ohos.com.sun.org.apache.bcel.internal.classfile.Field;
import ohos.com.sun.org.apache.bcel.internal.generic.BranchHandle;
import ohos.com.sun.org.apache.bcel.internal.generic.BranchInstruction;
import ohos.com.sun.org.apache.bcel.internal.generic.CHECKCAST;
import ohos.com.sun.org.apache.bcel.internal.generic.ConstantPoolGen;
import ohos.com.sun.org.apache.bcel.internal.generic.IFNONNULL;
import ohos.com.sun.org.apache.bcel.internal.generic.INVOKEVIRTUAL;
import ohos.com.sun.org.apache.bcel.internal.generic.Instruction;
import ohos.com.sun.org.apache.bcel.internal.generic.InstructionList;
import ohos.com.sun.org.apache.bcel.internal.generic.PUSH;
import ohos.com.sun.org.apache.bcel.internal.generic.PUTFIELD;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ClassGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ErrorMsg;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.MethodGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ObjectType;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ReferenceType;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.TypeCheckError;
import ohos.com.sun.org.apache.xalan.internal.xsltc.runtime.BasisLibrary;

/* access modifiers changed from: package-private */
public final class Param extends VariableBase {
    private boolean _isInSimpleNamedTemplate = false;

    Param() {
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.VariableBase
    public String toString() {
        return "param(" + this._name + ")";
    }

    public Instruction setLoadInstruction(Instruction instruction) {
        Instruction instruction2 = this._loadInstruction;
        this._loadInstruction = instruction;
        return instruction2;
    }

    public Instruction setStoreInstruction(Instruction instruction) {
        Instruction instruction2 = this._storeInstruction;
        this._storeInstruction = instruction;
        return instruction2;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.VariableBase, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.TopLevelElement, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void display(int i) {
        indent(i);
        PrintStream printStream = System.out;
        printStream.println("param " + this._name);
        if (this._select != null) {
            indent(i + 4);
            PrintStream printStream2 = System.out;
            printStream2.println("select " + this._select.toString());
        }
        displayContents(i + 4);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.VariableBase, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void parseContents(Parser parser) {
        super.parseContents(parser);
        SyntaxTreeNode parent = getParent();
        if (parent instanceof Stylesheet) {
            this._isLocal = false;
            Param lookupParam = parser.getSymbolTable().lookupParam(this._name);
            if (lookupParam != null) {
                int importPrecedence = getImportPrecedence();
                int importPrecedence2 = lookupParam.getImportPrecedence();
                if (importPrecedence == importPrecedence2) {
                    reportError(this, parser, ErrorMsg.VARIABLE_REDEF_ERR, this._name.toString());
                } else if (importPrecedence2 > importPrecedence) {
                    this._ignore = true;
                    copyReferences(lookupParam);
                    return;
                } else {
                    lookupParam.copyReferences(this);
                    lookupParam.disable();
                }
            }
            ((Stylesheet) parent).addParam(this);
            parser.getSymbolTable().addParam(this);
        } else if (parent instanceof Template) {
            Template template = (Template) parent;
            this._isLocal = true;
            template.addParameter(this);
            if (template.isSimpleNamedTemplate()) {
                this._isInSimpleNamedTemplate = true;
            }
        }
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.TopLevelElement, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public Type typeCheck(SymbolTable symbolTable) throws TypeCheckError {
        if (this._select != null) {
            this._type = this._select.typeCheck(symbolTable);
            if (!(this._type instanceof ReferenceType) && !(this._type instanceof ObjectType)) {
                this._select = new CastExpr(this._select, Type.Reference);
            }
        } else if (hasContents()) {
            typeCheckContents(symbolTable);
        }
        this._type = Type.Reference;
        return Type.Void;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.TopLevelElement, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void translate(ClassGenerator classGenerator, MethodGenerator methodGenerator) {
        ConstantPoolGen constantPool = classGenerator.getConstantPool();
        InstructionList instructionList = methodGenerator.getInstructionList();
        if (!this._ignore) {
            this._ignore = true;
            String mapQNameToJavaName = BasisLibrary.mapQNameToJavaName(this._name.toString());
            String signature = this._type.toSignature();
            String className = this._type.getClassName();
            if (isLocal()) {
                if (this._isInSimpleNamedTemplate) {
                    instructionList.append(loadInstruction());
                    BranchHandle append = instructionList.append((BranchInstruction) new IFNONNULL(null));
                    translateValue(classGenerator, methodGenerator);
                    instructionList.append(storeInstruction());
                    append.setTarget(instructionList.append(NOP));
                    return;
                }
                instructionList.append(classGenerator.loadTranslet());
                instructionList.append(new PUSH(constantPool, mapQNameToJavaName));
                translateValue(classGenerator, methodGenerator);
                instructionList.append(new PUSH(constantPool, true));
                instructionList.append(new INVOKEVIRTUAL(constantPool.addMethodref(Constants.TRANSLET_CLASS, Constants.ADD_PARAMETER, Constants.ADD_PARAMETER_SIG)));
                if (className != "") {
                    instructionList.append(new CHECKCAST(constantPool.addClass(className)));
                }
                this._type.translateUnBox(classGenerator, methodGenerator);
                if (this._refs.isEmpty()) {
                    instructionList.append(this._type.POP());
                    this._local = null;
                    return;
                }
                this._local = methodGenerator.addLocalVariable2(mapQNameToJavaName, this._type.toJCType(), instructionList.getEnd());
                instructionList.append(this._type.STORE(this._local.getIndex()));
            } else if (classGenerator.containsField(mapQNameToJavaName) == null) {
                classGenerator.addField(new Field(1, constantPool.addUtf8(mapQNameToJavaName), constantPool.addUtf8(signature), null, constantPool.getConstantPool()));
                instructionList.append(classGenerator.loadTranslet());
                instructionList.append(DUP);
                instructionList.append(new PUSH(constantPool, mapQNameToJavaName));
                translateValue(classGenerator, methodGenerator);
                instructionList.append(new PUSH(constantPool, true));
                instructionList.append(new INVOKEVIRTUAL(constantPool.addMethodref(Constants.TRANSLET_CLASS, Constants.ADD_PARAMETER, Constants.ADD_PARAMETER_SIG)));
                this._type.translateUnBox(classGenerator, methodGenerator);
                if (className != "") {
                    instructionList.append(new CHECKCAST(constantPool.addClass(className)));
                }
                instructionList.append(new PUTFIELD(constantPool.addFieldref(classGenerator.getClassName(), mapQNameToJavaName, signature)));
            }
        }
    }
}
