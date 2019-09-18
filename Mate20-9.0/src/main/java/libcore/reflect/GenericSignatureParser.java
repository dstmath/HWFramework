package libcore.reflect;

import java.lang.reflect.Constructor;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.GenericSignatureFormatError;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import libcore.util.EmptyArray;

public final class GenericSignatureParser {
    char[] buffer;
    private boolean eof;
    public ListOfTypes exceptionTypes;
    public Type fieldType;
    public TypeVariable[] formalTypeParameters;
    GenericDeclaration genericDecl;
    String identifier;
    public ListOfTypes interfaceTypes;
    public ClassLoader loader;
    public ListOfTypes parameterTypes;
    int pos;
    public Type returnType;
    public Type superclassType;
    char symbol;

    public GenericSignatureParser(ClassLoader loader2) {
        this.loader = loader2;
    }

    /* access modifiers changed from: package-private */
    public void setInput(GenericDeclaration genericDecl2, String input) {
        if (input != null) {
            this.genericDecl = genericDecl2;
            this.buffer = input.toCharArray();
            this.eof = false;
            scanSymbol();
            return;
        }
        this.eof = true;
    }

    public void parseForClass(GenericDeclaration genericDecl2, String signature) {
        setInput(genericDecl2, signature);
        if (!this.eof) {
            parseClassSignature();
        } else if (genericDecl2 instanceof Class) {
            Class c = (Class) genericDecl2;
            this.formalTypeParameters = EmptyArray.TYPE_VARIABLE;
            this.superclassType = c.getSuperclass();
            Class<?>[] interfaces = c.getInterfaces();
            if (interfaces.length == 0) {
                this.interfaceTypes = ListOfTypes.EMPTY;
            } else {
                this.interfaceTypes = new ListOfTypes((Type[]) interfaces);
            }
        } else {
            this.formalTypeParameters = EmptyArray.TYPE_VARIABLE;
            this.superclassType = Object.class;
            this.interfaceTypes = ListOfTypes.EMPTY;
        }
    }

    public void parseForMethod(GenericDeclaration genericDecl2, String signature, Class<?>[] rawExceptionTypes) {
        setInput(genericDecl2, signature);
        if (!this.eof) {
            parseMethodTypeSignature(rawExceptionTypes);
            return;
        }
        Method m = (Method) genericDecl2;
        this.formalTypeParameters = EmptyArray.TYPE_VARIABLE;
        Class<?>[] parameterTypes2 = m.getParameterTypes();
        if (parameterTypes2.length == 0) {
            this.parameterTypes = ListOfTypes.EMPTY;
        } else {
            this.parameterTypes = new ListOfTypes((Type[]) parameterTypes2);
        }
        Class<?>[] exceptionTypes2 = m.getExceptionTypes();
        if (exceptionTypes2.length == 0) {
            this.exceptionTypes = ListOfTypes.EMPTY;
        } else {
            this.exceptionTypes = new ListOfTypes((Type[]) exceptionTypes2);
        }
        this.returnType = m.getReturnType();
    }

    public void parseForConstructor(GenericDeclaration genericDecl2, String signature, Class<?>[] rawExceptionTypes) {
        setInput(genericDecl2, signature);
        if (!this.eof) {
            parseMethodTypeSignature(rawExceptionTypes);
            return;
        }
        Constructor c = (Constructor) genericDecl2;
        this.formalTypeParameters = EmptyArray.TYPE_VARIABLE;
        Class<?>[] parameterTypes2 = c.getParameterTypes();
        if (parameterTypes2.length == 0) {
            this.parameterTypes = ListOfTypes.EMPTY;
        } else {
            this.parameterTypes = new ListOfTypes((Type[]) parameterTypes2);
        }
        Class<?>[] exceptionTypes2 = c.getExceptionTypes();
        if (exceptionTypes2.length == 0) {
            this.exceptionTypes = ListOfTypes.EMPTY;
        } else {
            this.exceptionTypes = new ListOfTypes((Type[]) exceptionTypes2);
        }
    }

    public void parseForField(GenericDeclaration genericDecl2, String signature) {
        setInput(genericDecl2, signature);
        if (!this.eof) {
            this.fieldType = parseFieldTypeSignature();
        }
    }

    /* access modifiers changed from: package-private */
    public void parseClassSignature() {
        parseOptFormalTypeParameters();
        this.superclassType = parseClassTypeSignature();
        this.interfaceTypes = new ListOfTypes(16);
        while (this.symbol > 0) {
            this.interfaceTypes.add(parseClassTypeSignature());
        }
    }

    /* access modifiers changed from: package-private */
    public void parseOptFormalTypeParameters() {
        ListOfVariables typeParams = new ListOfVariables();
        if (this.symbol == '<') {
            scanSymbol();
            typeParams.add(parseFormalTypeParameter());
            while (this.symbol != '>' && this.symbol > 0) {
                typeParams.add(parseFormalTypeParameter());
            }
            expect('>');
        }
        this.formalTypeParameters = typeParams.getArray();
    }

    /* access modifiers changed from: package-private */
    public TypeVariableImpl<GenericDeclaration> parseFormalTypeParameter() {
        scanIdentifier();
        String name = this.identifier.intern();
        ListOfTypes bounds = new ListOfTypes(8);
        expect(':');
        if (this.symbol == 'L' || this.symbol == '[' || this.symbol == 'T') {
            bounds.add(parseFieldTypeSignature());
        }
        while (this.symbol == ':') {
            scanSymbol();
            bounds.add(parseFieldTypeSignature());
        }
        return new TypeVariableImpl<>(this.genericDecl, name, bounds);
    }

    /* access modifiers changed from: package-private */
    public Type parseFieldTypeSignature() {
        char c = this.symbol;
        if (c == 'L') {
            return parseClassTypeSignature();
        }
        if (c == 'T') {
            return parseTypeVariableSignature();
        }
        if (c == '[') {
            scanSymbol();
            return new GenericArrayTypeImpl(parseTypeSignature());
        }
        throw new GenericSignatureFormatError();
    }

    /* access modifiers changed from: package-private */
    public Type parseClassTypeSignature() {
        expect('L');
        StringBuilder qualIdent = new StringBuilder();
        scanIdentifier();
        while (this.symbol == '/') {
            scanSymbol();
            qualIdent.append(this.identifier);
            qualIdent.append(".");
            scanIdentifier();
        }
        qualIdent.append(this.identifier);
        ListOfTypes typeArgs = parseOptTypeArguments();
        ParameterizedTypeImpl parentType = new ParameterizedTypeImpl(null, qualIdent.toString(), typeArgs, this.loader);
        ListOfTypes listOfTypes = typeArgs;
        ParameterizedTypeImpl type = parentType;
        while (this.symbol == '.') {
            scanSymbol();
            scanIdentifier();
            qualIdent.append("$");
            qualIdent.append(this.identifier);
            type = new ParameterizedTypeImpl(parentType, qualIdent.toString(), parseOptTypeArguments(), this.loader);
        }
        expect(';');
        return type;
    }

    /* access modifiers changed from: package-private */
    public ListOfTypes parseOptTypeArguments() {
        ListOfTypes typeArgs = new ListOfTypes(8);
        if (this.symbol == '<') {
            scanSymbol();
            typeArgs.add(parseTypeArgument());
            while (this.symbol != '>' && this.symbol > 0) {
                typeArgs.add(parseTypeArgument());
            }
            expect('>');
        }
        return typeArgs;
    }

    /* access modifiers changed from: package-private */
    public Type parseTypeArgument() {
        ListOfTypes extendsBound = new ListOfTypes(1);
        ListOfTypes superBound = new ListOfTypes(1);
        if (this.symbol == '*') {
            scanSymbol();
            extendsBound.add(Object.class);
            return new WildcardTypeImpl(extendsBound, superBound);
        } else if (this.symbol == '+') {
            scanSymbol();
            extendsBound.add(parseFieldTypeSignature());
            return new WildcardTypeImpl(extendsBound, superBound);
        } else if (this.symbol != '-') {
            return parseFieldTypeSignature();
        } else {
            scanSymbol();
            superBound.add(parseFieldTypeSignature());
            extendsBound.add(Object.class);
            return new WildcardTypeImpl(extendsBound, superBound);
        }
    }

    /* access modifiers changed from: package-private */
    public TypeVariableImpl<GenericDeclaration> parseTypeVariableSignature() {
        expect('T');
        scanIdentifier();
        expect(';');
        return new TypeVariableImpl<>(this.genericDecl, this.identifier);
    }

    /* access modifiers changed from: package-private */
    public Type parseTypeSignature() {
        char c = this.symbol;
        if (c == 'F') {
            scanSymbol();
            return Float.TYPE;
        } else if (c == 'S') {
            scanSymbol();
            return Short.TYPE;
        } else if (c != 'Z') {
            switch (c) {
                case 'B':
                    scanSymbol();
                    return Byte.TYPE;
                case 'C':
                    scanSymbol();
                    return Character.TYPE;
                case 'D':
                    scanSymbol();
                    return Double.TYPE;
                default:
                    switch (c) {
                        case 'I':
                            scanSymbol();
                            return Integer.TYPE;
                        case 'J':
                            scanSymbol();
                            return Long.TYPE;
                        default:
                            return parseFieldTypeSignature();
                    }
            }
        } else {
            scanSymbol();
            return Boolean.TYPE;
        }
    }

    /* access modifiers changed from: package-private */
    public void parseMethodTypeSignature(Class<?>[] rawExceptionTypes) {
        parseOptFormalTypeParameters();
        this.parameterTypes = new ListOfTypes(16);
        expect('(');
        while (this.symbol != ')' && this.symbol > 0) {
            this.parameterTypes.add(parseTypeSignature());
        }
        expect(')');
        this.returnType = parseReturnType();
        if (this.symbol == '^') {
            this.exceptionTypes = new ListOfTypes(8);
            do {
                scanSymbol();
                if (this.symbol == 'T') {
                    this.exceptionTypes.add(parseTypeVariableSignature());
                } else {
                    this.exceptionTypes.add(parseClassTypeSignature());
                }
            } while (this.symbol == '^');
        } else if (rawExceptionTypes != null) {
            this.exceptionTypes = new ListOfTypes((Type[]) rawExceptionTypes);
        } else {
            this.exceptionTypes = new ListOfTypes(0);
        }
    }

    /* access modifiers changed from: package-private */
    public Type parseReturnType() {
        if (this.symbol != 'V') {
            return parseTypeSignature();
        }
        scanSymbol();
        return Void.TYPE;
    }

    /* access modifiers changed from: package-private */
    public void scanSymbol() {
        if (this.eof) {
            throw new GenericSignatureFormatError();
        } else if (this.pos < this.buffer.length) {
            this.symbol = this.buffer[this.pos];
            this.pos++;
        } else {
            this.symbol = 0;
            this.eof = true;
        }
    }

    /* access modifiers changed from: package-private */
    public void expect(char c) {
        if (this.symbol == c) {
            scanSymbol();
            return;
        }
        throw new GenericSignatureFormatError();
    }

    static boolean isStopSymbol(char ch) {
        switch (ch) {
            case '.':
            case '/':
                break;
            default:
                switch (ch) {
                    case ':':
                    case ';':
                    case '<':
                        break;
                    default:
                        return false;
                }
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public void scanIdentifier() {
        if (!this.eof) {
            StringBuilder identBuf = new StringBuilder(32);
            if (!isStopSymbol(this.symbol)) {
                identBuf.append(this.symbol);
                do {
                    char ch = this.buffer[this.pos];
                    if ((ch < 'a' || ch > 'z') && ((ch < 'A' || ch > 'Z') && isStopSymbol(ch))) {
                        this.identifier = identBuf.toString();
                        scanSymbol();
                        return;
                    }
                    identBuf.append(ch);
                    this.pos++;
                } while (this.pos != this.buffer.length);
                this.identifier = identBuf.toString();
                this.symbol = 0;
                this.eof = true;
                return;
            }
            this.symbol = 0;
            this.eof = true;
            throw new GenericSignatureFormatError();
        }
        throw new GenericSignatureFormatError();
    }
}
