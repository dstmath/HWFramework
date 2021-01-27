package ohos.com.sun.org.apache.bcel.internal.generic;

import ohos.com.sun.org.apache.bcel.internal.Constants;
import ohos.com.sun.org.apache.bcel.internal.Repository;
import ohos.com.sun.org.apache.bcel.internal.classfile.JavaClass;

public abstract class ReferenceType extends Type {
    protected ReferenceType(byte b, String str) {
        super(b, str);
    }

    ReferenceType() {
        super((byte) 14, "<null object>");
    }

    public boolean isCastableTo(Type type) {
        if (equals(Type.NULL)) {
            return true;
        }
        return isAssignmentCompatibleWith(type);
    }

    public boolean isAssignmentCompatibleWith(Type type) {
        if (!(type instanceof ReferenceType)) {
            return false;
        }
        ReferenceType referenceType = (ReferenceType) type;
        if (equals(Type.NULL)) {
            return true;
        }
        boolean z = this instanceof ObjectType;
        if (z) {
            ObjectType objectType = (ObjectType) this;
            if (objectType.referencesClass()) {
                boolean z2 = referenceType instanceof ObjectType;
                if (z2) {
                    ObjectType objectType2 = (ObjectType) referenceType;
                    if (objectType2.referencesClass() && (equals(referenceType) || Repository.instanceOf(objectType.getClassName(), objectType2.getClassName()))) {
                        return true;
                    }
                }
                if (z2) {
                    ObjectType objectType3 = (ObjectType) referenceType;
                    if (objectType3.referencesInterface() && Repository.implementationOf(objectType.getClassName(), objectType3.getClassName())) {
                        return true;
                    }
                }
            }
        }
        if (z) {
            ObjectType objectType4 = (ObjectType) this;
            if (objectType4.referencesInterface()) {
                boolean z3 = referenceType instanceof ObjectType;
                if (z3 && ((ObjectType) referenceType).referencesClass() && referenceType.equals(Type.OBJECT)) {
                    return true;
                }
                if (z3) {
                    ObjectType objectType5 = (ObjectType) referenceType;
                    if (objectType5.referencesInterface() && (equals(referenceType) || Repository.implementationOf(objectType4.getClassName(), objectType5.getClassName()))) {
                        return true;
                    }
                }
            }
        }
        if (this instanceof ArrayType) {
            boolean z4 = referenceType instanceof ObjectType;
            if (z4 && ((ObjectType) referenceType).referencesClass() && referenceType.equals(Type.OBJECT)) {
                return true;
            }
            if (referenceType instanceof ArrayType) {
                ArrayType arrayType = (ArrayType) this;
                Type elementType = arrayType.getElementType();
                Type elementType2 = arrayType.getElementType();
                if ((elementType instanceof BasicType) && (elementType2 instanceof BasicType) && elementType.equals(elementType2)) {
                    return true;
                }
                if ((elementType2 instanceof ReferenceType) && (elementType instanceof ReferenceType) && ((ReferenceType) elementType).isAssignmentCompatibleWith((ReferenceType) elementType2)) {
                    return true;
                }
            }
            if (z4 && ((ObjectType) referenceType).referencesInterface()) {
                for (int i = 0; i < Constants.INTERFACES_IMPLEMENTED_BY_ARRAYS.length; i++) {
                    if (referenceType.equals(new ObjectType(Constants.INTERFACES_IMPLEMENTED_BY_ARRAYS[i]))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public ReferenceType getFirstCommonSuperclass(ReferenceType referenceType) {
        if (equals(Type.NULL)) {
            return referenceType;
        }
        if (referenceType.equals(Type.NULL) || equals(referenceType)) {
            return this;
        }
        boolean z = this instanceof ArrayType;
        if (z && (referenceType instanceof ArrayType)) {
            ArrayType arrayType = (ArrayType) this;
            ArrayType arrayType2 = (ArrayType) referenceType;
            if (arrayType.getDimensions() == arrayType2.getDimensions() && (arrayType.getBasicType() instanceof ObjectType) && (arrayType2.getBasicType() instanceof ObjectType)) {
                return new ArrayType(((ObjectType) arrayType.getBasicType()).getFirstCommonSuperclass((ObjectType) arrayType2.getBasicType()), arrayType.getDimensions());
            }
        }
        if (z || (referenceType instanceof ArrayType)) {
            return Type.OBJECT;
        }
        if (((this instanceof ObjectType) && ((ObjectType) this).referencesInterface()) || ((referenceType instanceof ObjectType) && ((ObjectType) referenceType).referencesInterface())) {
            return Type.OBJECT;
        }
        ObjectType objectType = (ObjectType) this;
        ObjectType objectType2 = (ObjectType) referenceType;
        JavaClass[] superClasses = Repository.getSuperClasses(objectType.getClassName());
        JavaClass[] superClasses2 = Repository.getSuperClasses(objectType2.getClassName());
        if (!(superClasses == null || superClasses2 == null)) {
            JavaClass[] javaClassArr = new JavaClass[(superClasses.length + 1)];
            JavaClass[] javaClassArr2 = new JavaClass[(superClasses2.length + 1)];
            System.arraycopy(superClasses, 0, javaClassArr, 1, superClasses.length);
            System.arraycopy(superClasses2, 0, javaClassArr2, 1, superClasses2.length);
            javaClassArr[0] = Repository.lookupClass(objectType.getClassName());
            javaClassArr2[0] = Repository.lookupClass(objectType2.getClassName());
            for (int i = 0; i < javaClassArr2.length; i++) {
                for (int i2 = 0; i2 < javaClassArr.length; i2++) {
                    if (javaClassArr[i2].equals(javaClassArr2[i])) {
                        return new ObjectType(javaClassArr[i2].getClassName());
                    }
                }
            }
        }
        return null;
    }

    public ReferenceType firstCommonSuperclass(ReferenceType referenceType) {
        if (equals(Type.NULL)) {
            return referenceType;
        }
        if (referenceType.equals(Type.NULL) || equals(referenceType)) {
            return this;
        }
        if ((this instanceof ArrayType) || (referenceType instanceof ArrayType)) {
            return Type.OBJECT;
        }
        if (((this instanceof ObjectType) && ((ObjectType) this).referencesInterface()) || ((referenceType instanceof ObjectType) && ((ObjectType) referenceType).referencesInterface())) {
            return Type.OBJECT;
        }
        ObjectType objectType = (ObjectType) this;
        ObjectType objectType2 = (ObjectType) referenceType;
        JavaClass[] superClasses = Repository.getSuperClasses(objectType.getClassName());
        JavaClass[] superClasses2 = Repository.getSuperClasses(objectType2.getClassName());
        if (!(superClasses == null || superClasses2 == null)) {
            JavaClass[] javaClassArr = new JavaClass[(superClasses.length + 1)];
            JavaClass[] javaClassArr2 = new JavaClass[(superClasses2.length + 1)];
            System.arraycopy(superClasses, 0, javaClassArr, 1, superClasses.length);
            System.arraycopy(superClasses2, 0, javaClassArr2, 1, superClasses2.length);
            javaClassArr[0] = Repository.lookupClass(objectType.getClassName());
            javaClassArr2[0] = Repository.lookupClass(objectType2.getClassName());
            for (int i = 0; i < javaClassArr2.length; i++) {
                for (int i2 = 0; i2 < javaClassArr.length; i2++) {
                    if (javaClassArr[i2].equals(javaClassArr2[i])) {
                        return new ObjectType(javaClassArr[i2].getClassName());
                    }
                }
            }
        }
        return null;
    }
}
