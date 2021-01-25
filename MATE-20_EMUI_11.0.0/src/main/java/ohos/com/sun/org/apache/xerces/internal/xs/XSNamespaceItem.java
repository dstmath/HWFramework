package ohos.com.sun.org.apache.xerces.internal.xs;

public interface XSNamespaceItem {
    XSObjectList getAnnotations();

    XSAttributeDeclaration getAttributeDeclaration(String str);

    XSAttributeGroupDefinition getAttributeGroup(String str);

    XSNamedMap getComponents(short s);

    StringList getDocumentLocations();

    XSElementDeclaration getElementDeclaration(String str);

    XSModelGroupDefinition getModelGroupDefinition(String str);

    XSNotationDeclaration getNotationDeclaration(String str);

    String getSchemaNamespace();

    XSTypeDefinition getTypeDefinition(String str);
}
