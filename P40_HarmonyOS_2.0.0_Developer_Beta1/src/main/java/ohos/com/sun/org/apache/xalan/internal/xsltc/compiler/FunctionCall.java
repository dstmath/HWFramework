package ohos.com.sun.org.apache.xalan.internal.xsltc.compiler;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Vector;
import ohos.com.sun.org.apache.bcel.internal.Constants;
import ohos.com.sun.org.apache.bcel.internal.generic.BranchInstruction;
import ohos.com.sun.org.apache.bcel.internal.generic.ConstantPoolGen;
import ohos.com.sun.org.apache.bcel.internal.generic.IFEQ;
import ohos.com.sun.org.apache.bcel.internal.generic.INVOKEINTERFACE;
import ohos.com.sun.org.apache.bcel.internal.generic.INVOKESPECIAL;
import ohos.com.sun.org.apache.bcel.internal.generic.INVOKESTATIC;
import ohos.com.sun.org.apache.bcel.internal.generic.INVOKEVIRTUAL;
import ohos.com.sun.org.apache.bcel.internal.generic.Instruction;
import ohos.com.sun.org.apache.bcel.internal.generic.InstructionConstants;
import ohos.com.sun.org.apache.bcel.internal.generic.InstructionList;
import ohos.com.sun.org.apache.bcel.internal.generic.LocalVariableGen;
import ohos.com.sun.org.apache.bcel.internal.generic.NEW;
import ohos.com.sun.org.apache.bcel.internal.generic.PUSH;
import ohos.com.sun.org.apache.xalan.internal.utils.ObjectFactory;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.BooleanType;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ClassGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ErrorMsg;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.IntType;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.MethodGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.MethodType;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.MultiHashtable;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ObjectType;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ReferenceType;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.TypeCheckError;
import ohos.com.sun.org.apache.xml.internal.utils.LocaleUtility;
import ohos.jdk.xml.internal.JdkXmlFeatures;

/* access modifiers changed from: package-private */
public class FunctionCall extends Expression {
    private static final Vector EMPTY_ARG_LIST = new Vector(0);
    protected static final String EXSLT_COMMON = "http://exslt.org/common";
    protected static final String EXSLT_DATETIME = "http://exslt.org/dates-and-times";
    protected static final String EXSLT_MATH = "http://exslt.org/math";
    protected static final String EXSLT_SETS = "http://exslt.org/sets";
    protected static final String EXSLT_STRINGS = "http://exslt.org/strings";
    private static final Map<String, String> EXTENSIONFUNCTION;
    private static final Map<String, String> EXTENSIONNAMESPACE;
    protected static final String EXT_XALAN = "http://xml.apache.org/xalan";
    protected static final String EXT_XSLTC = "http://xml.apache.org/xalan/xsltc";
    private static final Map<Class<?>, Type> JAVA2INTERNAL;
    protected static final String JAVA_EXT_XALAN = "http://xml.apache.org/xalan/java";
    protected static final String JAVA_EXT_XALAN_OLD = "http://xml.apache.org/xslt/java";
    protected static final String JAVA_EXT_XSLTC = "http://xml.apache.org/xalan/xsltc/java";
    protected static final int NAMESPACE_FORMAT_CLASS = 1;
    protected static final int NAMESPACE_FORMAT_CLASS_OR_PACKAGE = 3;
    protected static final int NAMESPACE_FORMAT_JAVA = 0;
    protected static final int NAMESPACE_FORMAT_PACKAGE = 2;
    protected static final String XALAN_CLASSPACKAGE_NAMESPACE = "xalan://";
    private static final MultiHashtable<Type, JavaType> _internal2Java = new MultiHashtable<>();
    private final Vector _arguments;
    private Constructor _chosenConstructor;
    private Method _chosenMethod;
    private MethodType _chosenMethodType;
    private String _className;
    private Class _clazz;
    private QName _fname;
    private boolean _isExtConstructor;
    private boolean _isStatic;
    private int _namespace_format;
    Expression _thisArgument;
    private boolean unresolvedExternal;

    static {
        try {
            Class<?> cls = Class.forName("ohos.org.w3c.dom.Node");
            Class<?> cls2 = Class.forName("ohos.org.w3c.dom.NodeList");
            _internal2Java.put(Type.Boolean, new JavaType(Boolean.TYPE, 0));
            _internal2Java.put(Type.Boolean, new JavaType(Boolean.class, 1));
            _internal2Java.put(Type.Boolean, new JavaType(Object.class, 2));
            _internal2Java.put(Type.Real, new JavaType(Double.TYPE, 0));
            _internal2Java.put(Type.Real, new JavaType(Double.class, 1));
            _internal2Java.put(Type.Real, new JavaType(Float.TYPE, 2));
            _internal2Java.put(Type.Real, new JavaType(Long.TYPE, 3));
            _internal2Java.put(Type.Real, new JavaType(Integer.TYPE, 4));
            _internal2Java.put(Type.Real, new JavaType(Short.TYPE, 5));
            _internal2Java.put(Type.Real, new JavaType(Byte.TYPE, 6));
            _internal2Java.put(Type.Real, new JavaType(Character.TYPE, 7));
            _internal2Java.put(Type.Real, new JavaType(Object.class, 8));
            _internal2Java.put(Type.Int, new JavaType(Double.TYPE, 0));
            _internal2Java.put(Type.Int, new JavaType(Double.class, 1));
            _internal2Java.put(Type.Int, new JavaType(Float.TYPE, 2));
            _internal2Java.put(Type.Int, new JavaType(Long.TYPE, 3));
            _internal2Java.put(Type.Int, new JavaType(Integer.TYPE, 4));
            _internal2Java.put(Type.Int, new JavaType(Short.TYPE, 5));
            _internal2Java.put(Type.Int, new JavaType(Byte.TYPE, 6));
            _internal2Java.put(Type.Int, new JavaType(Character.TYPE, 7));
            _internal2Java.put(Type.Int, new JavaType(Object.class, 8));
            _internal2Java.put(Type.String, new JavaType(String.class, 0));
            _internal2Java.put(Type.String, new JavaType(Object.class, 1));
            _internal2Java.put(Type.NodeSet, new JavaType(cls2, 0));
            _internal2Java.put(Type.NodeSet, new JavaType(cls, 1));
            _internal2Java.put(Type.NodeSet, new JavaType(Object.class, 2));
            _internal2Java.put(Type.NodeSet, new JavaType(String.class, 3));
            _internal2Java.put(Type.Node, new JavaType(cls2, 0));
            _internal2Java.put(Type.Node, new JavaType(cls, 1));
            _internal2Java.put(Type.Node, new JavaType(Object.class, 2));
            _internal2Java.put(Type.Node, new JavaType(String.class, 3));
            _internal2Java.put(Type.ResultTree, new JavaType(cls2, 0));
            _internal2Java.put(Type.ResultTree, new JavaType(cls, 1));
            _internal2Java.put(Type.ResultTree, new JavaType(Object.class, 2));
            _internal2Java.put(Type.ResultTree, new JavaType(String.class, 3));
            _internal2Java.put(Type.Reference, new JavaType(Object.class, 0));
            _internal2Java.makeUnmodifiable();
            HashMap hashMap = new HashMap();
            HashMap hashMap2 = new HashMap();
            HashMap hashMap3 = new HashMap();
            hashMap.put(Boolean.TYPE, Type.Boolean);
            hashMap.put(Void.TYPE, Type.Void);
            hashMap.put(Character.TYPE, Type.Real);
            hashMap.put(Byte.TYPE, Type.Real);
            hashMap.put(Short.TYPE, Type.Real);
            hashMap.put(Integer.TYPE, Type.Real);
            hashMap.put(Long.TYPE, Type.Real);
            hashMap.put(Float.TYPE, Type.Real);
            hashMap.put(Double.TYPE, Type.Real);
            hashMap.put(String.class, Type.String);
            hashMap.put(Object.class, Type.Reference);
            hashMap.put(cls2, Type.NodeSet);
            hashMap.put(cls, Type.NodeSet);
            hashMap2.put("http://xml.apache.org/xalan", "ohos.com.sun.org.apache.xalan.internal.lib.Extensions");
            hashMap2.put("http://exslt.org/common", "ohos.com.sun.org.apache.xalan.internal.lib.ExsltCommon");
            hashMap2.put("http://exslt.org/math", "ohos.com.sun.org.apache.xalan.internal.lib.ExsltMath");
            hashMap2.put("http://exslt.org/sets", "ohos.com.sun.org.apache.xalan.internal.lib.ExsltSets");
            hashMap2.put("http://exslt.org/dates-and-times", "ohos.com.sun.org.apache.xalan.internal.lib.ExsltDatetime");
            hashMap2.put("http://exslt.org/strings", "ohos.com.sun.org.apache.xalan.internal.lib.ExsltStrings");
            hashMap3.put("http://exslt.org/common:nodeSet", "nodeset");
            hashMap3.put("http://exslt.org/common:objectType", "objectType");
            hashMap3.put("http://xml.apache.org/xalan:nodeset", "nodeset");
            JAVA2INTERNAL = Collections.unmodifiableMap(hashMap);
            EXTENSIONNAMESPACE = Collections.unmodifiableMap(hashMap2);
            EXTENSIONFUNCTION = Collections.unmodifiableMap(hashMap3);
        } catch (ClassNotFoundException unused) {
            throw new ExceptionInInitializerError(new ErrorMsg(ErrorMsg.CLASS_NOT_FOUND_ERR, "ohos.org.w3c.dom.Node or NodeList").toString());
        }
    }

    /* access modifiers changed from: package-private */
    public static class JavaType {
        public int distance;
        public Class<?> type;

        public JavaType(Class cls, int i) {
            this.type = cls;
            this.distance = i;
        }

        public int hashCode() {
            return Objects.hashCode(this.type);
        }

        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (obj.getClass().isAssignableFrom(JavaType.class)) {
                return ((JavaType) obj).type.equals(this.type);
            }
            return obj.equals(this.type);
        }
    }

    public FunctionCall(QName qName, Vector vector) {
        this._namespace_format = 0;
        this._thisArgument = null;
        this._isExtConstructor = false;
        this._isStatic = false;
        this._fname = qName;
        this._arguments = vector;
        this._type = null;
    }

    public FunctionCall(QName qName) {
        this(qName, EMPTY_ARG_LIST);
    }

    public String getName() {
        return this._fname.toString();
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void setParser(Parser parser) {
        super.setParser(parser);
        Vector vector = this._arguments;
        if (vector != null) {
            int size = vector.size();
            for (int i = 0; i < size; i++) {
                Expression expression = (Expression) this._arguments.elementAt(i);
                expression.setParser(parser);
                expression.setParent(this);
            }
        }
    }

    public String getClassNameFromUri(String str) {
        String str2 = EXTENSIONNAMESPACE.get(str);
        if (str2 != null) {
            return str2;
        }
        if (str.startsWith(JAVA_EXT_XSLTC)) {
            if (str.length() > 39) {
                return str.substring(39);
            }
            return "";
        } else if (str.startsWith("http://xml.apache.org/xalan/java")) {
            if (str.length() > 33) {
                return str.substring(33);
            }
            return "";
        } else if (!str.startsWith("http://xml.apache.org/xslt/java")) {
            int lastIndexOf = str.lastIndexOf(47);
            return lastIndexOf > 0 ? str.substring(lastIndexOf + 1) : str;
        } else if (str.length() > 32) {
            return str.substring(32);
        } else {
            return "";
        }
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public Type typeCheck(SymbolTable symbolTable) throws TypeCheckError {
        if (this._type != null) {
            return this._type;
        }
        String namespace = this._fname.getNamespace();
        String localPart = this._fname.getLocalPart();
        if (isExtension()) {
            this._fname = new QName(null, null, localPart);
            return typeCheckStandard(symbolTable);
        } else if (isStandard()) {
            return typeCheckStandard(symbolTable);
        } else {
            try {
                this._className = getClassNameFromUri(namespace);
                int lastIndexOf = localPart.lastIndexOf(46);
                if (lastIndexOf > 0) {
                    this._isStatic = true;
                    if (this._className == null || this._className.length() <= 0) {
                        this._namespace_format = 0;
                        this._className = localPart.substring(0, lastIndexOf);
                    } else {
                        this._namespace_format = 2;
                        this._className += "." + localPart.substring(0, lastIndexOf);
                    }
                    this._fname = new QName(namespace, null, localPart.substring(lastIndexOf + 1));
                } else {
                    if (this._className == null || this._className.length() <= 0) {
                        this._namespace_format = 0;
                    } else {
                        try {
                            this._clazz = ObjectFactory.findProviderClass(this._className, true);
                            this._namespace_format = 1;
                        } catch (ClassNotFoundException unused) {
                            this._namespace_format = 2;
                        }
                    }
                    if (localPart.indexOf(45) > 0) {
                        localPart = replaceDash(localPart);
                    }
                    String str = EXTENSIONFUNCTION.get(namespace + ":" + localPart);
                    if (str != null) {
                        this._fname = new QName(null, null, str);
                        return typeCheckStandard(symbolTable);
                    }
                    this._fname = new QName(namespace, null, localPart);
                }
                return typeCheckExternal(symbolTable);
            } catch (TypeCheckError e) {
                ErrorMsg errorMsg = e.getErrorMsg();
                if (errorMsg == null) {
                    errorMsg = new ErrorMsg(ErrorMsg.METHOD_NOT_FOUND_ERR, this._fname.getLocalPart());
                }
                getParser().reportError(3, errorMsg);
                Type type = Type.Void;
                this._type = type;
                return type;
            }
        }
    }

    public Type typeCheckStandard(SymbolTable symbolTable) throws TypeCheckError {
        this._fname.clearNamespace();
        int size = this._arguments.size();
        MethodType lookupPrimop = lookupPrimop(symbolTable, this._fname.getLocalPart(), new MethodType(Type.Void, typeCheckArgs(symbolTable)));
        if (lookupPrimop != null) {
            for (int i = 0; i < size; i++) {
                Type type = (Type) lookupPrimop.argsType().elementAt(i);
                Expression expression = (Expression) this._arguments.elementAt(i);
                if (!type.identicalTo(expression.getType())) {
                    try {
                        this._arguments.setElementAt(new CastExpr(expression, type), i);
                    } catch (TypeCheckError unused) {
                        throw new TypeCheckError(this);
                    }
                }
            }
            this._chosenMethodType = lookupPrimop;
            Type resultType = lookupPrimop.resultType();
            this._type = resultType;
            return resultType;
        }
        throw new TypeCheckError(this);
    }

    public Type typeCheckConstructor(SymbolTable symbolTable) throws TypeCheckError {
        Type type;
        Vector findConstructors = findConstructors();
        if (findConstructors != null) {
            int size = findConstructors.size();
            int size2 = this._arguments.size();
            Vector typeCheckArgs = typeCheckArgs(symbolTable);
            this._type = null;
            int i = Integer.MAX_VALUE;
            for (int i2 = 0; i2 < size; i2++) {
                Constructor constructor = (Constructor) findConstructors.elementAt(i2);
                Class[] parameterTypes = constructor.getParameterTypes();
                int i3 = 0;
                int i4 = 0;
                while (true) {
                    if (i3 >= size2) {
                        break;
                    }
                    Class cls = parameterTypes[i3];
                    Type type2 = (Type) typeCheckArgs.elementAt(i3);
                    JavaType maps = _internal2Java.maps(type2, new JavaType(cls, 0));
                    if (maps == null) {
                        if (!(type2 instanceof ObjectType)) {
                            break;
                        }
                        ObjectType objectType = (ObjectType) type2;
                        if (objectType.getJavaClass() != cls) {
                            if (!cls.isAssignableFrom(objectType.getJavaClass())) {
                                break;
                            }
                            i4++;
                        } else {
                            continue;
                        }
                    } else {
                        i4 += maps.distance;
                    }
                    i3++;
                }
                i4 = Integer.MAX_VALUE;
                if (i3 == size2 && i4 < i) {
                    this._chosenConstructor = constructor;
                    this._isExtConstructor = true;
                    Class cls2 = this._clazz;
                    if (cls2 != null) {
                        type = Type.newObjectType(cls2);
                    } else {
                        type = Type.newObjectType(this._className);
                    }
                    this._type = type;
                    i = i4;
                }
            }
            if (this._type != null) {
                return this._type;
            }
            throw new TypeCheckError(ErrorMsg.ARGUMENT_CONVERSION_ERR, getMethodSignature(typeCheckArgs));
        }
        throw new TypeCheckError(ErrorMsg.CONSTRUCTOR_NOT_FOUND, this._className);
    }

    public Type typeCheckExternal(SymbolTable symbolTable) throws TypeCheckError {
        int i;
        Class cls;
        int size = this._arguments.size();
        String localPart = this._fname.getLocalPart();
        if (this._fname.getLocalPart().equals("new")) {
            return typeCheckConstructor(symbolTable);
        }
        if (size == 0) {
            this._isStatic = true;
        }
        if (!this._isStatic) {
            int i2 = this._namespace_format;
            boolean z = i2 == 0 || i2 == 2;
            Type typeCheck = ((Expression) this._arguments.elementAt(0)).typeCheck(symbolTable);
            if (this._namespace_format == 1 && (typeCheck instanceof ObjectType) && (cls = this._clazz) != null && cls.isAssignableFrom(((ObjectType) typeCheck).getJavaClass())) {
                z = true;
            }
            if (z) {
                this._thisArgument = (Expression) this._arguments.elementAt(0);
                this._arguments.remove(0);
                size--;
                if (typeCheck instanceof ObjectType) {
                    this._className = ((ObjectType) typeCheck).getJavaClassName();
                } else {
                    throw new TypeCheckError(ErrorMsg.NO_JAVA_FUNCT_THIS_REF, localPart);
                }
            }
        } else if (this._className.length() == 0) {
            Parser parser = getParser();
            if (parser != null) {
                reportWarning(this, parser, ErrorMsg.FUNCTION_RESOLVE_ERR, this._fname.toString());
            }
            this.unresolvedExternal = true;
            Type type = Type.Int;
            this._type = type;
            return type;
        }
        Vector findMethods = findMethods();
        if (findMethods != null) {
            int size2 = findMethods.size();
            Vector typeCheckArgs = typeCheckArgs(symbolTable);
            this._type = null;
            int i3 = 0;
            int i4 = Integer.MAX_VALUE;
            while (i3 < size2) {
                Method method = (Method) findMethods.elementAt(i3);
                Class<?>[] parameterTypes = method.getParameterTypes();
                int i5 = 0;
                int i6 = 0;
                while (true) {
                    if (i5 >= size) {
                        i = size2;
                        break;
                    }
                    Class<?> cls2 = parameterTypes[i5];
                    Type type2 = (Type) typeCheckArgs.elementAt(i5);
                    i = size2;
                    JavaType maps = _internal2Java.maps(type2, new JavaType(cls2, 0));
                    if (maps != null) {
                        i6 += maps.distance;
                    } else {
                        if (!(type2 instanceof ReferenceType)) {
                            if (!(type2 instanceof ObjectType)) {
                                break;
                            }
                            ObjectType objectType = (ObjectType) type2;
                            if (!cls2.getName().equals(objectType.getJavaClassName())) {
                                if (!cls2.isAssignableFrom(objectType.getJavaClass())) {
                                    break;
                                }
                            } else {
                                i6 += 0;
                            }
                        }
                        i6++;
                    }
                    i5++;
                    size2 = i;
                }
                i6 = Integer.MAX_VALUE;
                if (i5 == size) {
                    Class<?> returnType = method.getReturnType();
                    this._type = JAVA2INTERNAL.get(returnType);
                    if (this._type == null) {
                        this._type = Type.newObjectType(returnType);
                    }
                    if (this._type != null && i6 < i4) {
                        this._chosenMethod = method;
                        i4 = i6;
                    }
                }
                i3++;
                size2 = i;
            }
            Method method2 = this._chosenMethod;
            if (method2 != null && this._thisArgument == null && !Modifier.isStatic(method2.getModifiers())) {
                throw new TypeCheckError(ErrorMsg.NO_JAVA_FUNCT_THIS_REF, getMethodSignature(typeCheckArgs));
            } else if (this._type != null) {
                if (this._type == Type.NodeSet) {
                    getXSLTC().setMultiDocument(true);
                }
                return this._type;
            } else {
                throw new TypeCheckError(ErrorMsg.ARGUMENT_CONVERSION_ERR, getMethodSignature(typeCheckArgs));
            }
        } else {
            throw new TypeCheckError(ErrorMsg.METHOD_NOT_FOUND_ERR, this._className + "." + localPart);
        }
    }

    public Vector typeCheckArgs(SymbolTable symbolTable) throws TypeCheckError {
        Vector vector = new Vector();
        Enumeration elements = this._arguments.elements();
        while (elements.hasMoreElements()) {
            vector.addElement(((Expression) elements.nextElement()).typeCheck(symbolTable));
        }
        return vector;
    }

    /* access modifiers changed from: protected */
    public final Expression argument(int i) {
        return (Expression) this._arguments.elementAt(i);
    }

    /* access modifiers changed from: protected */
    public final Expression argument() {
        return argument(0);
    }

    /* access modifiers changed from: protected */
    public final int argumentCount() {
        return this._arguments.size();
    }

    /* access modifiers changed from: protected */
    public final void setArgument(int i, Expression expression) {
        this._arguments.setElementAt(expression, i);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression
    public void translateDesynthesized(ClassGenerator classGenerator, MethodGenerator methodGenerator) {
        Type type = Type.Boolean;
        MethodType methodType = this._chosenMethodType;
        if (methodType != null) {
            type = methodType.resultType();
        }
        InstructionList instructionList = methodGenerator.getInstructionList();
        translate(classGenerator, methodGenerator);
        if ((type instanceof BooleanType) || (type instanceof IntType)) {
            this._falseList.add(instructionList.append((BranchInstruction) new IFEQ(null)));
        }
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void translate(ClassGenerator classGenerator, MethodGenerator methodGenerator) {
        Instruction instruction;
        int argumentCount = argumentCount();
        ConstantPoolGen constantPool = classGenerator.getConstantPool();
        InstructionList instructionList = methodGenerator.getInstructionList();
        boolean isSecureProcessing = classGenerator.getParser().getXSLTC().isSecureProcessing();
        boolean feature = classGenerator.getParser().getXSLTC().getFeature(JdkXmlFeatures.XmlFeature.ENABLE_EXTENSION_FUNCTION);
        if (isStandard() || isExtension()) {
            for (int i = 0; i < argumentCount; i++) {
                Expression argument = argument(i);
                argument.translate(classGenerator, methodGenerator);
                argument.startIterator(classGenerator, methodGenerator);
            }
            String str = this._fname.toString().replace(LocaleUtility.IETF_SEPARATOR, '_') + "F";
            String str2 = "";
            if (str.equals("sumF")) {
                instructionList.append(methodGenerator.loadDOM());
                str2 = Constants.DOM_INTF_SIG;
            } else if (str.equals("normalize_spaceF") && this._chosenMethodType.toSignature(str2).equals("()Ljava/lang/String;")) {
                instructionList.append(methodGenerator.loadContextNode());
                instructionList.append(methodGenerator.loadDOM());
                str2 = "ILohos.com.sun.org.apache.xalan.internal.xsltc.DOM;";
            }
            instructionList.append(new INVOKESTATIC(constantPool.addMethodref(Constants.BASIS_LIBRARY_CLASS, str, this._chosenMethodType.toSignature(str2))));
        } else if (this.unresolvedExternal) {
            int addMethodref = constantPool.addMethodref(Constants.BASIS_LIBRARY_CLASS, "unresolved_externalF", "(Ljava/lang/String;)V");
            instructionList.append(new PUSH(constantPool, this._fname.toString()));
            instructionList.append(new INVOKESTATIC(addMethodref));
        } else if (this._isExtConstructor) {
            if (isSecureProcessing && !feature) {
                translateUnallowedExtension(constantPool, instructionList);
            }
            String name = this._chosenConstructor.getDeclaringClass().getName();
            Class<?>[] parameterTypes = this._chosenConstructor.getParameterTypes();
            LocalVariableGen[] localVariableGenArr = new LocalVariableGen[argumentCount];
            for (int i2 = 0; i2 < argumentCount; i2++) {
                Expression argument2 = argument(i2);
                Type type = argument2.getType();
                argument2.translate(classGenerator, methodGenerator);
                argument2.startIterator(classGenerator, methodGenerator);
                type.translateTo(classGenerator, methodGenerator, parameterTypes[i2]);
                localVariableGenArr[i2] = methodGenerator.addLocalVariable("function_call_tmp" + i2, type.toJCType(), null, null);
                localVariableGenArr[i2].setStart(instructionList.append(type.STORE(localVariableGenArr[i2].getIndex())));
            }
            instructionList.append(new NEW(constantPool.addClass(this._className)));
            instructionList.append(InstructionConstants.DUP);
            for (int i3 = 0; i3 < argumentCount; i3++) {
                localVariableGenArr[i3].setEnd(instructionList.append(argument(i3).getType().LOAD(localVariableGenArr[i3].getIndex())));
            }
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append('(');
            for (Class<?> cls : parameterTypes) {
                stringBuffer.append(getSignature(cls));
            }
            stringBuffer.append(')');
            stringBuffer.append("V");
            instructionList.append(new INVOKESPECIAL(constantPool.addMethodref(name, Constants.CONSTRUCTOR_NAME, stringBuffer.toString())));
            Type.Object.translateFrom(classGenerator, methodGenerator, this._chosenConstructor.getDeclaringClass());
        } else {
            if (isSecureProcessing && !feature) {
                translateUnallowedExtension(constantPool, instructionList);
            }
            String name2 = this._chosenMethod.getDeclaringClass().getName();
            Class<?>[] parameterTypes2 = this._chosenMethod.getParameterTypes();
            Expression expression = this._thisArgument;
            if (expression != null) {
                expression.translate(classGenerator, methodGenerator);
            }
            for (int i4 = 0; i4 < argumentCount; i4++) {
                Expression argument3 = argument(i4);
                argument3.translate(classGenerator, methodGenerator);
                argument3.startIterator(classGenerator, methodGenerator);
                argument3.getType().translateTo(classGenerator, methodGenerator, parameterTypes2[i4]);
            }
            StringBuffer stringBuffer2 = new StringBuffer();
            stringBuffer2.append('(');
            for (Class<?> cls2 : parameterTypes2) {
                stringBuffer2.append(getSignature(cls2));
            }
            stringBuffer2.append(')');
            stringBuffer2.append(getSignature(this._chosenMethod.getReturnType()));
            if (this._thisArgument == null || !this._clazz.isInterface()) {
                int addMethodref2 = constantPool.addMethodref(name2, this._fname.getLocalPart(), stringBuffer2.toString());
                if (this._thisArgument != null) {
                    instruction = new INVOKEVIRTUAL(addMethodref2);
                } else {
                    instruction = new INVOKESTATIC(addMethodref2);
                }
                instructionList.append(instruction);
            } else {
                instructionList.append(new INVOKEINTERFACE(constantPool.addInterfaceMethodref(name2, this._fname.getLocalPart(), stringBuffer2.toString()), argumentCount + 1));
            }
            this._type.translateFrom(classGenerator, methodGenerator, this._chosenMethod.getReturnType());
        }
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression
    public String toString() {
        return "funcall(" + this._fname + ", " + this._arguments + ')';
    }

    public boolean isStandard() {
        String namespace = this._fname.getNamespace();
        return namespace == null || namespace.equals("");
    }

    public boolean isExtension() {
        String namespace = this._fname.getNamespace();
        return namespace != null && namespace.equals("http://xml.apache.org/xalan/xsltc");
    }

    private Vector findMethods() {
        String namespace = this._fname.getNamespace();
        String str = this._className;
        Vector vector = null;
        if (str != null && str.length() > 0) {
            int size = this._arguments.size();
            try {
                if (this._clazz == null) {
                    boolean isSecureProcessing = getXSLTC().isSecureProcessing();
                    boolean feature = getXSLTC().getFeature(JdkXmlFeatures.XmlFeature.ENABLE_EXTENSION_FUNCTION);
                    if (namespace == null || !isSecureProcessing || !feature || (!namespace.startsWith("http://xml.apache.org/xalan/java") && !namespace.startsWith(JAVA_EXT_XSLTC) && !namespace.startsWith("http://xml.apache.org/xslt/java") && !namespace.startsWith(XALAN_CLASSPACKAGE_NAMESPACE))) {
                        this._clazz = ObjectFactory.findProviderClass(this._className, true);
                    } else {
                        this._clazz = getXSLTC().loadExternalFunction(this._className);
                    }
                    if (this._clazz == null) {
                        getParser().reportError(3, new ErrorMsg(ErrorMsg.CLASS_NOT_FOUND_ERR, this._className));
                    }
                }
                String localPart = this._fname.getLocalPart();
                Method[] methods = this._clazz.getMethods();
                for (int i = 0; i < methods.length; i++) {
                    if (Modifier.isPublic(methods[i].getModifiers()) && methods[i].getName().equals(localPart) && methods[i].getParameterTypes().length == size) {
                        if (vector == null) {
                            vector = new Vector();
                        }
                        vector.addElement(methods[i]);
                    }
                }
            } catch (ClassNotFoundException unused) {
                getParser().reportError(3, new ErrorMsg(ErrorMsg.CLASS_NOT_FOUND_ERR, this._className));
            }
        }
        return vector;
    }

    private Vector findConstructors() {
        this._fname.getNamespace();
        int size = this._arguments.size();
        Vector vector = null;
        try {
            if (this._clazz == null) {
                this._clazz = ObjectFactory.findProviderClass(this._className, true);
                if (this._clazz == null) {
                    getParser().reportError(3, new ErrorMsg(ErrorMsg.CLASS_NOT_FOUND_ERR, this._className));
                }
            }
            Constructor<?>[] constructors = this._clazz.getConstructors();
            for (int i = 0; i < constructors.length; i++) {
                if (Modifier.isPublic(constructors[i].getModifiers()) && constructors[i].getParameterTypes().length == size) {
                    if (vector == null) {
                        vector = new Vector();
                    }
                    vector.addElement(constructors[i]);
                }
            }
        } catch (ClassNotFoundException unused) {
            getParser().reportError(3, new ErrorMsg(ErrorMsg.CLASS_NOT_FOUND_ERR, this._className));
        }
        return vector;
    }

    static final String getSignature(Class cls) {
        if (cls.isArray()) {
            StringBuffer stringBuffer = new StringBuffer();
            while (cls.isArray()) {
                stringBuffer.append("[");
                cls = cls.getComponentType();
            }
            stringBuffer.append(getSignature(cls));
            return stringBuffer.toString();
        } else if (!cls.isPrimitive()) {
            return "L" + cls.getName().replace('.', '/') + ';';
        } else if (cls == Integer.TYPE) {
            return "I";
        } else {
            if (cls == Byte.TYPE) {
                return "B";
            }
            if (cls == Long.TYPE) {
                return "J";
            }
            if (cls == Float.TYPE) {
                return "F";
            }
            if (cls == Double.TYPE) {
                return "D";
            }
            if (cls == Short.TYPE) {
                return "S";
            }
            if (cls == Character.TYPE) {
                return "C";
            }
            if (cls == Boolean.TYPE) {
                return Constants.HASIDCALL_INDEX_SIG;
            }
            if (cls == Void.TYPE) {
                return "V";
            }
            throw new Error(new ErrorMsg(ErrorMsg.UNKNOWN_SIG_TYPE_ERR, cls.toString()).toString());
        }
    }

    static final String getSignature(Method method) {
        Class<?>[] parameterTypes;
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append('(');
        for (Class<?> cls : method.getParameterTypes()) {
            stringBuffer.append(getSignature(cls));
        }
        stringBuffer.append(')');
        stringBuffer.append(getSignature(method.getReturnType()));
        return stringBuffer.toString();
    }

    static final String getSignature(Constructor constructor) {
        Class<?>[] parameterTypes;
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append('(');
        for (Class<?> cls : constructor.getParameterTypes()) {
            stringBuffer.append(getSignature(cls));
        }
        stringBuffer.append(")V");
        return stringBuffer.toString();
    }

    private String getMethodSignature(Vector vector) {
        StringBuffer stringBuffer = new StringBuffer(this._className);
        stringBuffer.append('.');
        stringBuffer.append(this._fname.getLocalPart());
        stringBuffer.append('(');
        int size = vector.size();
        for (int i = 0; i < size; i++) {
            stringBuffer.append(((Type) vector.elementAt(i)).toString());
            if (i < size - 1) {
                stringBuffer.append(", ");
            }
        }
        stringBuffer.append(')');
        return stringBuffer.toString();
    }

    protected static String replaceDash(String str) {
        StringBuilder sb = new StringBuilder("");
        for (int i = 0; i < str.length(); i++) {
            if (i > 0 && str.charAt(i - 1) == '-') {
                sb.append(Character.toUpperCase(str.charAt(i)));
            } else if (str.charAt(i) != '-') {
                sb.append(str.charAt(i));
            }
        }
        return sb.toString();
    }

    private void translateUnallowedExtension(ConstantPoolGen constantPoolGen, InstructionList instructionList) {
        int addMethodref = constantPoolGen.addMethodref(Constants.BASIS_LIBRARY_CLASS, "unallowed_extension_functionF", "(Ljava/lang/String;)V");
        instructionList.append(new PUSH(constantPoolGen, this._fname.toString()));
        instructionList.append(new INVOKESTATIC(addMethodref));
    }
}
