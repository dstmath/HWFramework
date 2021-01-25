package ohos.com.sun.xml.internal.stream.events;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import ohos.com.sun.xml.internal.stream.util.ReadOnlyIterator;
import ohos.javax.xml.namespace.QName;
import ohos.javax.xml.stream.events.EndElement;
import ohos.javax.xml.stream.events.Namespace;

public class EndElementEvent extends DummyEvent implements EndElement {
    List fNamespaces;
    QName fQName;

    public EndElementEvent() {
        this.fNamespaces = null;
        init();
    }

    /* access modifiers changed from: protected */
    public void init() {
        setEventType(2);
        this.fNamespaces = new ArrayList();
    }

    public EndElementEvent(String str, String str2, String str3) {
        this(new QName(str2, str3, str));
    }

    public EndElementEvent(QName qName) {
        this.fNamespaces = null;
        this.fQName = qName;
        init();
    }

    public QName getName() {
        return this.fQName;
    }

    public void setName(QName qName) {
        this.fQName = qName;
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.xml.internal.stream.events.DummyEvent
    public void writeAsEncodedUnicodeEx(Writer writer) throws IOException {
        writer.write("</");
        String prefix = this.fQName.getPrefix();
        if (prefix != null && prefix.length() > 0) {
            writer.write(prefix);
            writer.write(58);
        }
        writer.write(this.fQName.getLocalPart());
        writer.write(62);
    }

    public Iterator getNamespaces() {
        List list = this.fNamespaces;
        if (list != null) {
            list.iterator();
        }
        return new ReadOnlyIterator();
    }

    /* access modifiers changed from: package-private */
    public void addNamespace(Namespace namespace) {
        if (namespace != null) {
            this.fNamespaces.add(namespace);
        }
    }

    public String toString() {
        return ("</" + nameAsString()) + ">";
    }

    public String nameAsString() {
        if ("".equals(this.fQName.getNamespaceURI())) {
            return this.fQName.getLocalPart();
        }
        if (this.fQName.getPrefix() != null) {
            return "['" + this.fQName.getNamespaceURI() + "']:" + this.fQName.getPrefix() + ":" + this.fQName.getLocalPart();
        }
        return "['" + this.fQName.getNamespaceURI() + "']:" + this.fQName.getLocalPart();
    }
}
