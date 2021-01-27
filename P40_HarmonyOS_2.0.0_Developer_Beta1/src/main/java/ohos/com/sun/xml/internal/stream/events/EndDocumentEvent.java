package ohos.com.sun.xml.internal.stream.events;

import java.io.IOException;
import java.io.Writer;
import ohos.javax.xml.stream.events.EndDocument;

public class EndDocumentEvent extends DummyEvent implements EndDocument {
    public String toString() {
        return "ENDDOCUMENT";
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.xml.internal.stream.events.DummyEvent
    public void writeAsEncodedUnicodeEx(Writer writer) throws IOException {
    }

    public EndDocumentEvent() {
        init();
    }

    /* access modifiers changed from: protected */
    public void init() {
        setEventType(8);
    }
}
