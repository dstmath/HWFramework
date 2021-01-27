package ohos.com.sun.org.apache.xalan.internal.xsltc.compiler;

import java.util.Objects;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.TypeCheckError;

/* access modifiers changed from: package-private */
public class VariableRefBase extends Expression {
    protected Closure _closure;
    protected VariableBase _variable;

    public VariableRefBase(VariableBase variableBase) {
        this._closure = null;
        this._variable = variableBase;
        variableBase.addReference(this);
    }

    public VariableRefBase() {
        this._closure = null;
        this._variable = null;
    }

    public VariableBase getVariable() {
        return this._variable;
    }

    public void addParentDependency() {
        SyntaxTreeNode syntaxTreeNode = this;
        while (syntaxTreeNode != null && !(syntaxTreeNode instanceof TopLevelElement)) {
            syntaxTreeNode = syntaxTreeNode.getParent();
        }
        TopLevelElement topLevelElement = (TopLevelElement) syntaxTreeNode;
        if (topLevelElement != null) {
            VariableBase variableBase = this._variable;
            if (variableBase._ignore) {
                VariableBase variableBase2 = this._variable;
                if (variableBase2 instanceof Variable) {
                    variableBase = topLevelElement.getSymbolTable().lookupVariable(this._variable._name);
                } else if (variableBase2 instanceof Param) {
                    variableBase = topLevelElement.getSymbolTable().lookupParam(this._variable._name);
                }
            }
            topLevelElement.addDependency(variableBase);
        }
    }

    public boolean equals(Object obj) {
        return obj == this || ((obj instanceof VariableRefBase) && this._variable == ((VariableRefBase) obj)._variable);
    }

    public int hashCode() {
        return Objects.hashCode(this._variable);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression
    public String toString() {
        return "variable-ref(" + this._variable.getName() + '/' + this._variable.getType() + ')';
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public Type typeCheck(SymbolTable symbolTable) throws TypeCheckError {
        if (this._type != null) {
            return this._type;
        }
        if (this._variable.isLocal()) {
            SyntaxTreeNode parent = getParent();
            while (true) {
                if (!(parent instanceof Closure)) {
                    if (parent instanceof TopLevelElement) {
                        break;
                    }
                    parent = parent.getParent();
                    if (parent == null) {
                        break;
                    }
                } else {
                    this._closure = (Closure) parent;
                    break;
                }
            }
            Closure closure = this._closure;
            if (closure != null) {
                closure.addVariable(this);
            }
        }
        this._type = this._variable.getType();
        if (this._type == null) {
            this._variable.typeCheck(symbolTable);
            this._type = this._variable.getType();
        }
        addParentDependency();
        return this._type;
    }
}
