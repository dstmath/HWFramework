package ohos.javax.xml.stream.events;

public interface StartDocument extends XMLEvent {
    boolean encodingSet();

    String getCharacterEncodingScheme();

    String getSystemId();

    String getVersion();

    boolean isStandalone();

    boolean standaloneSet();
}
