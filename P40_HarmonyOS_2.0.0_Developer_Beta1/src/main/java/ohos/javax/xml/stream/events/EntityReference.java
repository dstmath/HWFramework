package ohos.javax.xml.stream.events;

public interface EntityReference extends XMLEvent {
    EntityDeclaration getDeclaration();

    String getName();
}
