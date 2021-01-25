package ohos.com.sun.xml.internal.stream.events;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import ohos.com.sun.xml.internal.stream.util.ReadOnlyIterator;
import ohos.javax.xml.namespace.NamespaceContext;
import ohos.javax.xml.namespace.QName;
import ohos.javax.xml.stream.events.Attribute;
import ohos.javax.xml.stream.events.Namespace;
import ohos.javax.xml.stream.events.StartElement;

public class StartElementEvent extends DummyEvent implements StartElement {
    private Map fAttributes;
    private NamespaceContext fNamespaceContext;
    private List fNamespaces;
    private QName fQName;

    public StartElementEvent(String str, String str2, String str3) {
        this(new QName(str2, str3, str));
    }

    public StartElementEvent(QName qName) {
        this.fNamespaceContext = null;
        this.fQName = qName;
        init();
    }

    public StartElementEvent(StartElement startElement) {
        this(startElement.getName());
        addAttributes(startElement.getAttributes());
        addNamespaceAttributes(startElement.getNamespaces());
    }

    /* access modifiers changed from: protected */
    public void init() {
        setEventType(1);
        this.fAttributes = new HashMap();
        this.fNamespaces = new ArrayList();
    }

    public QName getName() {
        return this.fQName;
    }

    public void setName(QName qName) {
        this.fQName = qName;
    }

    public Iterator getAttributes() {
        Map map = this.fAttributes;
        if (map != null) {
            return new ReadOnlyIterator(map.values().iterator());
        }
        return new ReadOnlyIterator();
    }

    public Iterator getNamespaces() {
        List list = this.fNamespaces;
        if (list != null) {
            return new ReadOnlyIterator(list.iterator());
        }
        return new ReadOnlyIterator();
    }

    public Attribute getAttributeByName(QName qName) {
        if (qName == null) {
            return null;
        }
        return (Attribute) this.fAttributes.get(qName);
    }

    public String getNamespace() {
        return this.fQName.getNamespaceURI();
    }

    public String getNamespaceURI(String str) {
        if (getNamespace() != null && this.fQName.getPrefix().equals(str)) {
            return getNamespace();
        }
        NamespaceContext namespaceContext = this.fNamespaceContext;
        if (namespaceContext != null) {
            return namespaceContext.getNamespaceURI(str);
        }
        return null;
    }

    public String toString() {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("<");
        stringBuffer.append(nameAsString());
        if (this.fAttributes != null) {
            Iterator attributes = getAttributes();
            while (attributes.hasNext()) {
                stringBuffer.append(" ");
                stringBuffer.append(((Attribute) attributes.next()).toString());
            }
        }
        List<Namespace> list = this.fNamespaces;
        if (list != null) {
            for (Namespace namespace : list) {
                stringBuffer.append(" ");
                stringBuffer.append(namespace.toString());
            }
        }
        stringBuffer.append(">");
        return stringBuffer.toString();
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

    public NamespaceContext getNamespaceContext() {
        return this.fNamespaceContext;
    }

    public void setNamespaceContext(NamespaceContext namespaceContext) {
        this.fNamespaceContext = namespaceContext;
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.xml.internal.stream.events.DummyEvent
    public void writeAsEncodedUnicodeEx(Writer writer) throws IOException {
        writer.write(toString());
    }

    /* access modifiers changed from: package-private */
    public void addAttribute(Attribute attribute) {
        if (attribute.isNamespace()) {
            this.fNamespaces.add(attribute);
        } else {
            this.fAttributes.put(attribute.getName(), attribute);
        }
    }

    /* access modifiers changed from: package-private */
    public void addAttributes(Iterator it) {
        if (it != null) {
            while (it.hasNext()) {
                Attribute attribute = (Attribute) it.next();
                this.fAttributes.put(attribute.getName(), attribute);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void addNamespaceAttribute(Namespace namespace) {
        if (namespace != null) {
            this.fNamespaces.add(namespace);
        }
    }

    /* access modifiers changed from: package-private */
    public void addNamespaceAttributes(Iterator it) {
        if (it != null) {
            while (it.hasNext()) {
                this.fNamespaces.add((Namespace) it.next());
            }
        }
    }
}
