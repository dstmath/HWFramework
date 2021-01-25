package ohos.javax.xml.stream;

public class XMLStreamException extends Exception {
    protected Location location;
    protected Throwable nested;

    public XMLStreamException() {
    }

    public XMLStreamException(String str) {
        super(str);
    }

    public XMLStreamException(Throwable th) {
        super(th);
        this.nested = th;
    }

    public XMLStreamException(String str, Throwable th) {
        super(str, th);
        this.nested = th;
    }

    public XMLStreamException(String str, Location location2, Throwable th) {
        super("ParseError at [row,col]:[" + location2.getLineNumber() + "," + location2.getColumnNumber() + "]\nMessage: " + str);
        this.nested = th;
        this.location = location2;
    }

    public XMLStreamException(String str, Location location2) {
        super("ParseError at [row,col]:[" + location2.getLineNumber() + "," + location2.getColumnNumber() + "]\nMessage: " + str);
        this.location = location2;
    }

    public Throwable getNestedException() {
        return this.nested;
    }

    public Location getLocation() {
        return this.location;
    }
}
