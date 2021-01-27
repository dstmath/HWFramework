package ohos.com.sun.org.apache.xalan.internal.xsltc.compiler;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Vector;
import ohos.com.sun.org.apache.bcel.internal.generic.PUSH;
import ohos.com.sun.org.apache.xalan.internal.utils.ObjectFactory;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ClassGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ErrorMsg;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.MethodGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.TypeCheckError;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Util;
import ohos.com.sun.org.apache.xpath.internal.compiler.Keywords;

final class FunctionAvailableCall extends FunctionCall {
    private Expression _arg;
    private boolean _isFunctionAvailable = false;
    private String _nameOfFunct = null;
    private String _namespaceOfFunct = null;

    public FunctionAvailableCall(QName qName, Vector vector) {
        super(qName, vector);
        this._arg = (Expression) vector.elementAt(0);
        this._type = null;
        Expression expression = this._arg;
        if (expression instanceof LiteralExpr) {
            LiteralExpr literalExpr = (LiteralExpr) expression;
            this._namespaceOfFunct = literalExpr.getNamespace();
            this._nameOfFunct = literalExpr.getValue();
            if (!isInternalNamespace()) {
                this._isFunctionAvailable = hasMethods();
            }
        }
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.FunctionCall, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public Type typeCheck(SymbolTable symbolTable) throws TypeCheckError {
        if (this._type != null) {
            return this._type;
        }
        if (this._arg instanceof LiteralExpr) {
            Type type = Type.Boolean;
            this._type = type;
            return type;
        }
        throw new TypeCheckError(new ErrorMsg(ErrorMsg.NEED_LITERAL_ERR, (Object) Keywords.FUNC_EXT_FUNCTION_AVAILABLE_STRING, (SyntaxTreeNode) this));
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression
    public Object evaluateAtCompileTime() {
        return getResult() ? Boolean.TRUE : Boolean.FALSE;
    }

    private boolean hasMethods() {
        String str;
        String classNameFromUri = getClassNameFromUri(this._namespaceOfFunct);
        int indexOf = this._nameOfFunct.indexOf(58);
        if (indexOf > 0) {
            String substring = this._nameOfFunct.substring(indexOf + 1);
            int lastIndexOf = substring.lastIndexOf(46);
            if (lastIndexOf > 0) {
                str = substring.substring(lastIndexOf + 1);
                classNameFromUri = (classNameFromUri == null || classNameFromUri.length() == 0) ? substring.substring(0, lastIndexOf) : classNameFromUri + "." + substring.substring(0, lastIndexOf);
            } else {
                str = substring;
            }
        } else {
            str = this._nameOfFunct;
        }
        if (!(classNameFromUri == null || str == null)) {
            if (str.indexOf(45) > 0) {
                str = replaceDash(str);
            }
            try {
                Class<?> findProviderClass = ObjectFactory.findProviderClass(classNameFromUri, true);
                if (findProviderClass == null) {
                    return false;
                }
                Method[] methods = findProviderClass.getMethods();
                for (int i = 0; i < methods.length; i++) {
                    int modifiers = methods[i].getModifiers();
                    if (Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers) && methods[i].getName().equals(str)) {
                        return true;
                    }
                }
            } catch (ClassNotFoundException unused) {
            }
        }
        return false;
    }

    public boolean getResult() {
        if (this._nameOfFunct == null) {
            return false;
        }
        if (isInternalNamespace()) {
            this._isFunctionAvailable = getParser().functionSupported(Util.getLocalName(this._nameOfFunct));
        }
        return this._isFunctionAvailable;
    }

    private boolean isInternalNamespace() {
        String str = this._namespaceOfFunct;
        return str == null || str.equals("") || this._namespaceOfFunct.equals(Constants.TRANSLET_URI);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.FunctionCall, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void translate(ClassGenerator classGenerator, MethodGenerator methodGenerator) {
        methodGenerator.getInstructionList().append(new PUSH(classGenerator.getConstantPool(), getResult()));
    }
}
