package ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util;

import java.util.Vector;
import ohos.com.sun.org.apache.bcel.internal.generic.Type;

public final class MethodType extends Type {
    private final Vector _argsType;
    private final Type _resultType;

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type
    public Type toJCType() {
        return null;
    }

    public MethodType(Type type) {
        this._argsType = null;
        this._resultType = type;
    }

    public MethodType(Type type, Type type2) {
        if (type2 != Type.Void) {
            this._argsType = new Vector();
            this._argsType.addElement(type2);
        } else {
            this._argsType = null;
        }
        this._resultType = type;
    }

    public MethodType(Type type, Type type2, Type type3) {
        this._argsType = new Vector(2);
        this._argsType.addElement(type2);
        this._argsType.addElement(type3);
        this._resultType = type;
    }

    public MethodType(Type type, Type type2, Type type3, Type type4) {
        this._argsType = new Vector(3);
        this._argsType.addElement(type2);
        this._argsType.addElement(type3);
        this._argsType.addElement(type4);
        this._resultType = type;
    }

    public MethodType(Type type, Vector vector) {
        this._resultType = type;
        this._argsType = vector.size() <= 0 ? null : vector;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type
    public String toString() {
        StringBuffer stringBuffer = new StringBuffer("method{");
        Vector vector = this._argsType;
        if (vector != null) {
            int size = vector.size();
            for (int i = 0; i < size; i++) {
                stringBuffer.append(this._argsType.elementAt(i));
                if (i != size - 1) {
                    stringBuffer.append(',');
                }
            }
        } else {
            stringBuffer.append("void");
        }
        stringBuffer.append('}');
        return stringBuffer.toString();
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type
    public String toSignature() {
        return toSignature("");
    }

    public String toSignature(String str) {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append('(');
        Vector vector = this._argsType;
        if (vector != null) {
            int size = vector.size();
            for (int i = 0; i < size; i++) {
                stringBuffer.append(((Type) this._argsType.elementAt(i)).toSignature());
            }
        }
        stringBuffer.append(str);
        stringBuffer.append(')');
        stringBuffer.append(this._resultType.toSignature());
        return stringBuffer.toString();
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type
    public boolean identicalTo(Type type) {
        if (!(type instanceof MethodType)) {
            return false;
        }
        MethodType methodType = (MethodType) type;
        if (!this._resultType.identicalTo(methodType._resultType)) {
            return false;
        }
        int argsCount = argsCount();
        boolean z = argsCount == methodType.argsCount();
        for (int i = 0; i < argsCount && z; i++) {
            z = ((Type) this._argsType.elementAt(i)).identicalTo((Type) methodType._argsType.elementAt(i));
        }
        return z;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type
    public int distanceTo(Type type) {
        if (!(type instanceof MethodType)) {
            return Integer.MAX_VALUE;
        }
        MethodType methodType = (MethodType) type;
        Vector vector = this._argsType;
        if (vector == null) {
            return methodType._argsType == null ? 0 : Integer.MAX_VALUE;
        }
        int size = vector.size();
        if (size != methodType._argsType.size()) {
            return Integer.MAX_VALUE;
        }
        int i = 0;
        for (int i2 = 0; i2 < size; i2++) {
            Type type2 = (Type) this._argsType.elementAt(i2);
            Type type3 = (Type) methodType._argsType.elementAt(i2);
            int distanceTo = type2.distanceTo(type3);
            if (distanceTo == Integer.MAX_VALUE) {
                return distanceTo;
            }
            i += type2.distanceTo(type3);
        }
        return i;
    }

    public Type resultType() {
        return this._resultType;
    }

    public Vector argsType() {
        return this._argsType;
    }

    public int argsCount() {
        Vector vector = this._argsType;
        if (vector == null) {
            return 0;
        }
        return vector.size();
    }
}
