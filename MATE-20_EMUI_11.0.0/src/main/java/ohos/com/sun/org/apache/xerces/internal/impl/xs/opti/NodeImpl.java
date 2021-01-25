package ohos.com.sun.org.apache.xerces.internal.impl.xs.opti;

public class NodeImpl extends DefaultNode {
    boolean hidden;
    String localpart;
    short nodeType;
    String prefix;
    String rawname;
    String uri;

    public NodeImpl() {
    }

    public NodeImpl(String str, String str2, String str3, String str4, short s) {
        this.prefix = str;
        this.localpart = str2;
        this.rawname = str3;
        this.uri = str4;
        this.nodeType = s;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.opti.DefaultNode
    public String getNodeName() {
        return this.rawname;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.opti.DefaultNode
    public String getNamespaceURI() {
        return this.uri;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.opti.DefaultNode
    public String getPrefix() {
        return this.prefix;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.opti.DefaultNode
    public String getLocalName() {
        return this.localpart;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.opti.DefaultNode
    public short getNodeType() {
        return this.nodeType;
    }

    public void setReadOnly(boolean z, boolean z2) {
        this.hidden = z;
    }

    public boolean getReadOnly() {
        return this.hidden;
    }
}
