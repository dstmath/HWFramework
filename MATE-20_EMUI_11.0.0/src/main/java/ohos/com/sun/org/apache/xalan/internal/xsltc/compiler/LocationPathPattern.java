package ohos.com.sun.org.apache.xalan.internal.xsltc.compiler;

import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ClassGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.MethodGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.TypeCheckError;

public abstract class LocationPathPattern extends Pattern {
    private int _importPrecedence;
    private int _position = 0;
    private double _priority = Double.NaN;
    private Template _template;

    public double getDefaultPriority() {
        return 0.5d;
    }

    public abstract StepPattern getKernelPattern();

    public abstract boolean isWildcard();

    public abstract void reduceKernelPattern();

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression
    public String toString() {
        return "root()";
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Pattern, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void translate(ClassGenerator classGenerator, MethodGenerator methodGenerator) {
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Pattern, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public Type typeCheck(SymbolTable symbolTable) throws TypeCheckError {
        return Type.Void;
    }

    public void setTemplate(Template template) {
        this._template = template;
        this._priority = template.getPriority();
        this._importPrecedence = template.getImportPrecedence();
        this._position = template.getPosition();
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public Template getTemplate() {
        return this._template;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Pattern
    public final double getPriority() {
        return Double.isNaN(this._priority) ? getDefaultPriority() : this._priority;
    }

    public boolean noSmallerThan(LocationPathPattern locationPathPattern) {
        int i = this._importPrecedence;
        int i2 = locationPathPattern._importPrecedence;
        if (i > i2) {
            return true;
        }
        if (i != i2) {
            return false;
        }
        double d = this._priority;
        double d2 = locationPathPattern._priority;
        if (d > d2) {
            return true;
        }
        if (d != d2 || this._position <= locationPathPattern._position) {
            return false;
        }
        return true;
    }

    public int getAxis() {
        StepPattern kernelPattern = getKernelPattern();
        if (kernelPattern != null) {
            return kernelPattern.getAxis();
        }
        return 3;
    }
}
