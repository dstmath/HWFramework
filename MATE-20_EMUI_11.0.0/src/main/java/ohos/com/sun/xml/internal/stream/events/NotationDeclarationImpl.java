package ohos.com.sun.xml.internal.stream.events;

import java.io.IOException;
import java.io.Writer;
import ohos.com.sun.xml.internal.stream.dtd.nonvalidating.XMLNotationDecl;
import ohos.javax.xml.stream.events.NotationDeclaration;

public class NotationDeclarationImpl extends DummyEvent implements NotationDeclaration {
    String fName = null;
    String fPublicId = null;
    String fSystemId = null;

    public NotationDeclarationImpl() {
        setEventType(14);
    }

    public NotationDeclarationImpl(String str, String str2, String str3) {
        this.fName = str;
        this.fPublicId = str2;
        this.fSystemId = str3;
        setEventType(14);
    }

    public NotationDeclarationImpl(XMLNotationDecl xMLNotationDecl) {
        this.fName = xMLNotationDecl.name;
        this.fPublicId = xMLNotationDecl.publicId;
        this.fSystemId = xMLNotationDecl.systemId;
        setEventType(14);
    }

    public String getName() {
        return this.fName;
    }

    public String getPublicId() {
        return this.fPublicId;
    }

    public String getSystemId() {
        return this.fSystemId;
    }

    /* access modifiers changed from: package-private */
    public void setPublicId(String str) {
        this.fPublicId = str;
    }

    /* access modifiers changed from: package-private */
    public void setSystemId(String str) {
        this.fSystemId = str;
    }

    /* access modifiers changed from: package-private */
    public void setName(String str) {
        this.fName = str;
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.xml.internal.stream.events.DummyEvent
    public void writeAsEncodedUnicodeEx(Writer writer) throws IOException {
        writer.write("<!NOTATION ");
        writer.write(getName());
        if (this.fPublicId != null) {
            writer.write(" PUBLIC \"");
            writer.write(this.fPublicId);
            writer.write("\"");
        } else if (this.fSystemId != null) {
            writer.write(" SYSTEM");
            writer.write(" \"");
            writer.write(this.fSystemId);
            writer.write("\"");
        }
        writer.write(62);
    }
}
