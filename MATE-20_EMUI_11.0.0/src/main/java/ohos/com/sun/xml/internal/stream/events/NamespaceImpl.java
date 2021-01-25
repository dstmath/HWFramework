package ohos.com.sun.xml.internal.stream.events;

import ohos.javax.xml.namespace.QName;
import ohos.javax.xml.stream.events.Namespace;

public class NamespaceImpl extends AttributeImpl implements Namespace {
    @Override // ohos.com.sun.xml.internal.stream.events.DummyEvent
    public int getEventType() {
        return 13;
    }

    @Override // ohos.com.sun.xml.internal.stream.events.DummyEvent
    public boolean isNamespace() {
        return true;
    }

    public NamespaceImpl() {
        init();
    }

    public NamespaceImpl(String str) {
        super("xmlns", "http://www.w3.org/2000/xmlns/", "", str, (String) null);
        init();
    }

    public NamespaceImpl(String str, String str2) {
        super("xmlns", "http://www.w3.org/2000/xmlns/", str, str2, (String) null);
        init();
    }

    public boolean isDefaultNamespaceDeclaration() {
        QName name = getName();
        return name != null && name.getLocalPart().equals("");
    }

    /* access modifiers changed from: package-private */
    public void setPrefix(String str) {
        if (str == null) {
            setName(new QName("http://www.w3.org/2000/xmlns/", "", "xmlns"));
        } else {
            setName(new QName("http://www.w3.org/2000/xmlns/", str, "xmlns"));
        }
    }

    public String getPrefix() {
        QName name = getName();
        if (name != null) {
            return name.getLocalPart();
        }
        return null;
    }

    public String getNamespaceURI() {
        return getValue();
    }

    /* access modifiers changed from: package-private */
    public void setNamespaceURI(String str) {
        setValue(str);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.xml.internal.stream.events.AttributeImpl
    public void init() {
        setEventType(13);
    }
}
