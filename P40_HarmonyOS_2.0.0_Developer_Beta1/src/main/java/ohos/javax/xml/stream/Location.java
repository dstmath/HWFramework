package ohos.javax.xml.stream;

public interface Location {
    int getCharacterOffset();

    int getColumnNumber();

    int getLineNumber();

    String getPublicId();

    String getSystemId();
}
