package ohos.com.sun.xml.internal.stream.events;

import java.io.IOException;
import java.io.Writer;
import ohos.javax.xml.stream.events.Comment;

public class CommentEvent extends DummyEvent implements Comment {
    private String fText;

    public CommentEvent() {
        init();
    }

    public CommentEvent(String str) {
        init();
        this.fText = str;
    }

    /* access modifiers changed from: protected */
    public void init() {
        setEventType(5);
    }

    public String toString() {
        return "<!--" + getText() + "-->";
    }

    public String getText() {
        return this.fText;
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.xml.internal.stream.events.DummyEvent
    public void writeAsEncodedUnicodeEx(Writer writer) throws IOException {
        writer.write("<!--" + getText() + "-->");
    }
}
