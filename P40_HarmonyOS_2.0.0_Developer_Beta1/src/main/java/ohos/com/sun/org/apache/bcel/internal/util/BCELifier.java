package ohos.com.sun.org.apache.bcel.internal.util;

import java.io.OutputStream;
import java.io.PrintWriter;
import ohos.aafwk.ability.Ability;
import ohos.com.sun.org.apache.bcel.internal.Constants;
import ohos.com.sun.org.apache.bcel.internal.Repository;
import ohos.com.sun.org.apache.bcel.internal.classfile.ClassParser;
import ohos.com.sun.org.apache.bcel.internal.classfile.ConstantValue;
import ohos.com.sun.org.apache.bcel.internal.classfile.EmptyVisitor;
import ohos.com.sun.org.apache.bcel.internal.classfile.Field;
import ohos.com.sun.org.apache.bcel.internal.classfile.JavaClass;
import ohos.com.sun.org.apache.bcel.internal.classfile.Method;
import ohos.com.sun.org.apache.bcel.internal.classfile.Utility;
import ohos.com.sun.org.apache.bcel.internal.generic.ArrayType;
import ohos.com.sun.org.apache.bcel.internal.generic.ConstantPoolGen;
import ohos.com.sun.org.apache.bcel.internal.generic.MethodGen;
import ohos.com.sun.org.apache.bcel.internal.generic.Type;

public class BCELifier extends EmptyVisitor {
    private JavaClass _clazz;
    private ConstantPoolGen _cp = new ConstantPoolGen(this._clazz.getConstantPool());
    private PrintWriter _out;

    public BCELifier(JavaClass javaClass, OutputStream outputStream) {
        this._clazz = javaClass;
        this._out = new PrintWriter(outputStream);
    }

    public void start() {
        visitJavaClass(this._clazz);
        this._out.flush();
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.classfile.EmptyVisitor, ohos.com.sun.org.apache.bcel.internal.classfile.Visitor
    public void visitJavaClass(JavaClass javaClass) {
        String className = javaClass.getClassName();
        String superclassName = javaClass.getSuperclassName();
        String packageName = javaClass.getPackageName();
        String printArray = Utility.printArray(javaClass.getInterfaceNames(), false, true);
        if (!"".equals(packageName)) {
            className = className.substring(packageName.length() + 1);
            this._out.println("package " + packageName + ";\n");
        }
        this._out.println("import ohos.com.sun.org.apache.bcel.internal.generic.*;");
        this._out.println("import ohos.com.sun.org.apache.bcel.internal.classfile.*;");
        this._out.println("import ohos.com.sun.org.apache.bcel.internal.*;");
        this._out.println("import java.io.*;\n");
        this._out.println("public class " + className + "Creator implements Constants {");
        this._out.println("  private InstructionFactory _factory;");
        this._out.println("  private ConstantPoolGen    _cp;");
        this._out.println("  private ClassGen           _cg;\n");
        this._out.println("  public " + className + "Creator() {");
        PrintWriter printWriter = this._out;
        StringBuilder sb = new StringBuilder();
        sb.append("    _cg = new ClassGen(\"");
        if (!"".equals(packageName)) {
            className = packageName + "." + className;
        }
        sb.append(className);
        sb.append("\", \"");
        sb.append(superclassName);
        sb.append("\", \"");
        sb.append(javaClass.getSourceFileName());
        sb.append("\", ");
        sb.append(printFlags(javaClass.getAccessFlags(), true));
        sb.append(", new String[] { ");
        sb.append(printArray);
        sb.append(" });\n");
        printWriter.println(sb.toString());
        this._out.println("    _cp = _cg.getConstantPool();");
        this._out.println("    _factory = new InstructionFactory(_cg, _cp);");
        this._out.println("  }\n");
        printCreate();
        Field[] fields = javaClass.getFields();
        if (fields.length > 0) {
            this._out.println("  private void createFields() {");
            this._out.println("    FieldGen field;");
            for (Field field : fields) {
                field.accept(this);
            }
            this._out.println("  }\n");
        }
        Method[] methods = javaClass.getMethods();
        for (int i = 0; i < methods.length; i++) {
            this._out.println("  private void createMethod_" + i + "() {");
            methods[i].accept(this);
            this._out.println("  }\n");
        }
        printMain();
        this._out.println("}");
    }

    private void printCreate() {
        this._out.println("  public void create(OutputStream out) throws IOException {");
        if (this._clazz.getFields().length > 0) {
            this._out.println("    createFields();");
        }
        Method[] methods = this._clazz.getMethods();
        for (int i = 0; i < methods.length; i++) {
            PrintWriter printWriter = this._out;
            printWriter.println("    createMethod_" + i + "();");
        }
        this._out.println("    _cg.getJavaClass().dump(out);");
        this._out.println("  }\n");
    }

    private void printMain() {
        String className = this._clazz.getClassName();
        this._out.println("  public static void _main(String[] args) throws Exception {");
        PrintWriter printWriter = this._out;
        printWriter.println(Ability.PREFIX + className + "Creator creator = new " + className + "Creator();");
        PrintWriter printWriter2 = this._out;
        StringBuilder sb = new StringBuilder();
        sb.append("    creator.create(new FileOutputStream(\"");
        sb.append(className);
        sb.append(".class\"));");
        printWriter2.println(sb.toString());
        this._out.println("  }");
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.classfile.EmptyVisitor, ohos.com.sun.org.apache.bcel.internal.classfile.Visitor
    public void visitField(Field field) {
        PrintWriter printWriter = this._out;
        printWriter.println("\n    field = new FieldGen(" + printFlags(field.getAccessFlags()) + ", " + printType(field.getSignature()) + ", \"" + field.getName() + "\", _cp);");
        ConstantValue constantValue = field.getConstantValue();
        if (constantValue != null) {
            String constantValue2 = constantValue.toString();
            PrintWriter printWriter2 = this._out;
            printWriter2.println("    field.setInitValue(" + constantValue2 + ")");
        }
        this._out.println("    _cg.addField(field.getField());");
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.classfile.EmptyVisitor, ohos.com.sun.org.apache.bcel.internal.classfile.Visitor
    public void visitMethod(Method method) {
        MethodGen methodGen = new MethodGen(method, this._clazz.getClassName(), this._cp);
        Type returnType = methodGen.getReturnType();
        Type[] argumentTypes = methodGen.getArgumentTypes();
        this._out.println("    InstructionList il = new InstructionList();");
        PrintWriter printWriter = this._out;
        printWriter.println("    MethodGen method = new MethodGen(" + printFlags(method.getAccessFlags()) + ", " + printType(returnType) + ", " + printArgumentTypes(argumentTypes) + ", new String[] { " + Utility.printArray(methodGen.getArgumentNames(), false, true) + " }, \"" + method.getName() + "\", \"" + this._clazz.getClassName() + "\", il, _cp);\n");
        new BCELFactory(methodGen, this._out).start();
        this._out.println("    method.setMaxStack();");
        this._out.println("    method.setMaxLocals();");
        this._out.println("    _cg.addMethod(method.getMethod());");
        this._out.println("    il.dispose();");
    }

    static String printFlags(int i) {
        return printFlags(i, false);
    }

    static String printFlags(int i, boolean z) {
        if (i == 0) {
            return "0";
        }
        StringBuffer stringBuffer = new StringBuffer();
        int i2 = 1;
        for (int i3 = 0; i3 <= 2048; i3++) {
            if ((i & i2) != 0) {
                if (i2 != 32 || !z) {
                    stringBuffer.append("ACC_" + Constants.ACCESS_NAMES[i3].toUpperCase() + " | ");
                } else {
                    stringBuffer.append("ACC_SUPER | ");
                }
            }
            i2 <<= 1;
        }
        String stringBuffer2 = stringBuffer.toString();
        return stringBuffer2.substring(0, stringBuffer2.length() - 3);
    }

    static String printArgumentTypes(Type[] typeArr) {
        if (typeArr.length == 0) {
            return "Type.NO_ARGS";
        }
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < typeArr.length; i++) {
            stringBuffer.append(printType(typeArr[i]));
            if (i < typeArr.length - 1) {
                stringBuffer.append(", ");
            }
        }
        return "new Type[] { " + stringBuffer.toString() + " }";
    }

    static String printType(Type type) {
        return printType(type.getSignature());
    }

    static String printType(String str) {
        Type type = Type.getType(str);
        byte type2 = type.getType();
        if (type2 <= 12) {
            return "Type." + Constants.TYPE_NAMES[type2].toUpperCase();
        } else if (type.toString().equals("java.lang.String")) {
            return "Type.STRING";
        } else {
            if (type.toString().equals(ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Constants.OBJECT_CLASS)) {
                return "Type.OBJECT";
            }
            if (type.toString().equals(ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Constants.STRING_BUFFER_CLASS)) {
                return "Type.STRINGBUFFER";
            }
            if (type instanceof ArrayType) {
                ArrayType arrayType = (ArrayType) type;
                return "new ArrayType(" + printType(arrayType.getBasicType()) + ", " + arrayType.getDimensions() + ")";
            }
            return "new ObjectType(\"" + Utility.signatureToString(str, false) + "\")";
        }
    }

    public static void _main(String[] strArr) throws Exception {
        String str = strArr[0];
        JavaClass lookupClass = Repository.lookupClass(str);
        if (lookupClass == null) {
            lookupClass = new ClassParser(str).parse();
        }
        new BCELifier(lookupClass, System.out).start();
    }
}
