package ohos.com.sun.xml.internal.stream.events;

import java.io.IOException;
import java.io.Writer;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLResourceIdentifier;
import ohos.javax.xml.stream.events.EntityDeclaration;

public class EntityDeclarationImpl extends DummyEvent implements EntityDeclaration {
    private String fEntityName;
    private String fNotationName;
    private String fReplacementText;
    private XMLResourceIdentifier fXMLResourceIdentifier;

    public EntityDeclarationImpl() {
        init();
    }

    public EntityDeclarationImpl(String str, String str2) {
        this(str, str2, null);
    }

    public EntityDeclarationImpl(String str, String str2, XMLResourceIdentifier xMLResourceIdentifier) {
        init();
        this.fEntityName = str;
        this.fReplacementText = str2;
        this.fXMLResourceIdentifier = xMLResourceIdentifier;
    }

    public void setEntityName(String str) {
        this.fEntityName = str;
    }

    public String getEntityName() {
        return this.fEntityName;
    }

    public void setEntityReplacementText(String str) {
        this.fReplacementText = str;
    }

    public void setXMLResourceIdentifier(XMLResourceIdentifier xMLResourceIdentifier) {
        this.fXMLResourceIdentifier = xMLResourceIdentifier;
    }

    public XMLResourceIdentifier getXMLResourceIdentifier() {
        return this.fXMLResourceIdentifier;
    }

    public String getSystemId() {
        XMLResourceIdentifier xMLResourceIdentifier = this.fXMLResourceIdentifier;
        if (xMLResourceIdentifier != null) {
            return xMLResourceIdentifier.getLiteralSystemId();
        }
        return null;
    }

    public String getPublicId() {
        XMLResourceIdentifier xMLResourceIdentifier = this.fXMLResourceIdentifier;
        if (xMLResourceIdentifier != null) {
            return xMLResourceIdentifier.getPublicId();
        }
        return null;
    }

    public String getBaseURI() {
        XMLResourceIdentifier xMLResourceIdentifier = this.fXMLResourceIdentifier;
        if (xMLResourceIdentifier != null) {
            return xMLResourceIdentifier.getBaseSystemId();
        }
        return null;
    }

    public String getName() {
        return this.fEntityName;
    }

    public String getNotationName() {
        return this.fNotationName;
    }

    public void setNotationName(String str) {
        this.fNotationName = str;
    }

    public String getReplacementText() {
        return this.fReplacementText;
    }

    /* access modifiers changed from: protected */
    public void init() {
        setEventType(15);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.xml.internal.stream.events.DummyEvent
    public void writeAsEncodedUnicodeEx(Writer writer) throws IOException {
        writer.write("<!ENTITY ");
        writer.write(this.fEntityName);
        if (this.fReplacementText != null) {
            writer.write(" \"");
            charEncode(writer, this.fReplacementText);
        } else {
            String publicId = getPublicId();
            if (publicId != null) {
                writer.write(" PUBLIC \"");
                writer.write(publicId);
            } else {
                writer.write(" SYSTEM \"");
                writer.write(getSystemId());
            }
        }
        writer.write("\"");
        if (this.fNotationName != null) {
            writer.write(" NDATA ");
            writer.write(this.fNotationName);
        }
        writer.write(">");
    }
}
