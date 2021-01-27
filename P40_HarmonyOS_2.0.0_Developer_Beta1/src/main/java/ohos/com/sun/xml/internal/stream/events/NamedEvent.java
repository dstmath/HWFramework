package ohos.com.sun.xml.internal.stream.events;

import java.io.IOException;
import java.io.Writer;
import ohos.javax.xml.namespace.QName;

public class NamedEvent extends DummyEvent {
    private QName name;

    public NamedEvent() {
    }

    public NamedEvent(QName qName) {
        this.name = qName;
    }

    public NamedEvent(String str, String str2, String str3) {
        this.name = new QName(str2, str3, str);
    }

    public String getPrefix() {
        return this.name.getPrefix();
    }

    public QName getName() {
        return this.name;
    }

    public void setName(QName qName) {
        this.name = qName;
    }

    public String nameAsString() {
        if ("".equals(this.name.getNamespaceURI())) {
            return this.name.getLocalPart();
        }
        if (this.name.getPrefix() != null) {
            return "['" + this.name.getNamespaceURI() + "']:" + getPrefix() + ":" + this.name.getLocalPart();
        }
        return "['" + this.name.getNamespaceURI() + "']:" + this.name.getLocalPart();
    }

    public String getNamespace() {
        return this.name.getNamespaceURI();
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.xml.internal.stream.events.DummyEvent
    public void writeAsEncodedUnicodeEx(Writer writer) throws IOException {
        writer.write(nameAsString());
    }
}
