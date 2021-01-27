package ohos.javax.xml.stream.events;

public interface NotationDeclaration extends XMLEvent {
    String getName();

    String getPublicId();

    String getSystemId();
}
