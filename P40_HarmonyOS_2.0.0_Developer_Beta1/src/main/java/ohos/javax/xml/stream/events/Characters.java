package ohos.javax.xml.stream.events;

public interface Characters extends XMLEvent {
    String getData();

    boolean isCData();

    boolean isIgnorableWhiteSpace();

    boolean isWhiteSpace();
}
