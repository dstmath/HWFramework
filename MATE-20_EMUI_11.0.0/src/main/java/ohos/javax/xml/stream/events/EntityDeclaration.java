package ohos.javax.xml.stream.events;

public interface EntityDeclaration extends XMLEvent {
    String getBaseURI();

    String getName();

    String getNotationName();

    String getPublicId();

    String getReplacementText();

    String getSystemId();
}
