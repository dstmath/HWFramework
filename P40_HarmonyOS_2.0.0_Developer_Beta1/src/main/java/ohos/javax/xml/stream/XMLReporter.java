package ohos.javax.xml.stream;

public interface XMLReporter {
    void report(String str, String str2, Object obj, Location location) throws XMLStreamException;
}
