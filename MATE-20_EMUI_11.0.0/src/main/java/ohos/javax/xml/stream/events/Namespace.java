package ohos.javax.xml.stream.events;

public interface Namespace extends Attribute {
    String getNamespaceURI();

    String getPrefix();

    boolean isDefaultNamespaceDeclaration();
}
