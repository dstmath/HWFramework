package ohos.com.sun.org.apache.bcel.internal.generic;

import ohos.com.sun.org.apache.bcel.internal.Repository;
import ohos.com.sun.org.apache.bcel.internal.classfile.JavaClass;
import ohos.dmsdp.sdk.DMSDPConfig;

public final class ObjectType extends ReferenceType {
    private String class_name;

    public ObjectType(String str) {
        super((byte) 14, "L" + str.replace('.', '/') + DMSDPConfig.LIST_TO_STRING_SPLIT);
        this.class_name = str.replace('/', '.');
    }

    public String getClassName() {
        return this.class_name;
    }

    @Override // java.lang.Object
    public int hashCode() {
        return this.class_name.hashCode();
    }

    @Override // java.lang.Object
    public boolean equals(Object obj) {
        if (obj instanceof ObjectType) {
            return ((ObjectType) obj).class_name.equals(this.class_name);
        }
        return false;
    }

    public boolean referencesClass() {
        JavaClass lookupClass = Repository.lookupClass(this.class_name);
        if (lookupClass == null) {
            return false;
        }
        return lookupClass.isClass();
    }

    public boolean referencesInterface() {
        JavaClass lookupClass = Repository.lookupClass(this.class_name);
        if (lookupClass == null) {
            return false;
        }
        return !lookupClass.isClass();
    }

    public boolean subclassOf(ObjectType objectType) {
        if (referencesInterface() || objectType.referencesInterface()) {
            return false;
        }
        return Repository.instanceOf(this.class_name, objectType.class_name);
    }

    public boolean accessibleTo(ObjectType objectType) {
        JavaClass lookupClass = Repository.lookupClass(this.class_name);
        if (lookupClass.isPublic()) {
            return true;
        }
        return Repository.lookupClass(objectType.class_name).getPackageName().equals(lookupClass.getPackageName());
    }
}
