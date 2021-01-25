package ohos.com.sun.org.apache.bcel.internal.classfile;

public interface Visitor {
    void visitCode(Code code);

    void visitCodeException(CodeException codeException);

    void visitConstantClass(ConstantClass constantClass);

    void visitConstantDouble(ConstantDouble constantDouble);

    void visitConstantFieldref(ConstantFieldref constantFieldref);

    void visitConstantFloat(ConstantFloat constantFloat);

    void visitConstantInteger(ConstantInteger constantInteger);

    void visitConstantInterfaceMethodref(ConstantInterfaceMethodref constantInterfaceMethodref);

    void visitConstantLong(ConstantLong constantLong);

    void visitConstantMethodref(ConstantMethodref constantMethodref);

    void visitConstantNameAndType(ConstantNameAndType constantNameAndType);

    void visitConstantPool(ConstantPool constantPool);

    void visitConstantString(ConstantString constantString);

    void visitConstantUtf8(ConstantUtf8 constantUtf8);

    void visitConstantValue(ConstantValue constantValue);

    void visitDeprecated(Deprecated deprecated);

    void visitExceptionTable(ExceptionTable exceptionTable);

    void visitField(Field field);

    void visitInnerClass(InnerClass innerClass);

    void visitInnerClasses(InnerClasses innerClasses);

    void visitJavaClass(JavaClass javaClass);

    void visitLineNumber(LineNumber lineNumber);

    void visitLineNumberTable(LineNumberTable lineNumberTable);

    void visitLocalVariable(LocalVariable localVariable);

    void visitLocalVariableTable(LocalVariableTable localVariableTable);

    void visitLocalVariableTypeTable(LocalVariableTypeTable localVariableTypeTable);

    void visitMethod(Method method);

    void visitSignature(Signature signature);

    void visitSourceFile(SourceFile sourceFile);

    void visitStackMap(StackMap stackMap);

    void visitStackMapEntry(StackMapEntry stackMapEntry);

    void visitSynthetic(Synthetic synthetic);

    void visitUnknown(Unknown unknown);
}
