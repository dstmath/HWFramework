package ohos.com.sun.xml.internal.stream.events;

import java.io.IOException;
import java.io.Writer;
import ohos.javax.xml.stream.Location;
import ohos.javax.xml.stream.events.ProcessingInstruction;

public class ProcessingInstructionEvent extends DummyEvent implements ProcessingInstruction {
    private String fContent;
    private String fName;

    public ProcessingInstructionEvent() {
        init();
    }

    public ProcessingInstructionEvent(String str, String str2) {
        this(str, str2, null);
    }

    public ProcessingInstructionEvent(String str, String str2, Location location) {
        init();
        this.fName = str;
        this.fContent = str2;
        setLocation(location);
    }

    /* access modifiers changed from: protected */
    public void init() {
        setEventType(3);
    }

    public String getTarget() {
        return this.fName;
    }

    public void setTarget(String str) {
        this.fName = str;
    }

    public void setData(String str) {
        this.fContent = str;
    }

    public String getData() {
        return this.fContent;
    }

    public String toString() {
        if (this.fContent != null && this.fName != null) {
            return "<?" + this.fName + " " + this.fContent + "?>";
        } else if (this.fName != null) {
            return "<?" + this.fName + "?>";
        } else if (this.fContent == null) {
            return "<??>";
        } else {
            return "<?" + this.fContent + "?>";
        }
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.xml.internal.stream.events.DummyEvent
    public void writeAsEncodedUnicodeEx(Writer writer) throws IOException {
        writer.write(toString());
    }
}
