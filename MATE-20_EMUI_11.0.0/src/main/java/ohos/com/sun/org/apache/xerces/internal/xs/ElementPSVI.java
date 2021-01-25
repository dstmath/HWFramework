package ohos.com.sun.org.apache.xerces.internal.xs;

public interface ElementPSVI extends ItemPSVI {
    XSElementDeclaration getElementDeclaration();

    boolean getNil();

    XSNotationDeclaration getNotation();

    XSModel getSchemaInformation();
}
