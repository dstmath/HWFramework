package ohos.com.sun.org.apache.xerces.internal.impl.validation;

public interface EntityState {
    boolean isEntityDeclared(String str);

    boolean isEntityUnparsed(String str);
}
