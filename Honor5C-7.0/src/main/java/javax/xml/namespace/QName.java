package javax.xml.namespace;

import dalvik.bytecode.Opcodes;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import org.xmlpull.v1.XmlPullParser;

public class QName implements Serializable {
    private static final long compatibilitySerialVersionUID = 4418622981026545151L;
    private static final long defaultSerialVersionUID = -9120448754896609940L;
    private static final long serialVersionUID = 0;
    private final String localPart;
    private final String namespaceURI;
    private String prefix;
    private transient String qNameAsString;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: javax.xml.namespace.QName.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: javax.xml.namespace.QName.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: javax.xml.namespace.QName.<clinit>():void");
    }

    public QName(String namespaceURI, String localPart) {
        this(namespaceURI, localPart, XmlPullParser.NO_NAMESPACE);
    }

    public QName(String namespaceURI, String localPart, String prefix) {
        if (namespaceURI == null) {
            this.namespaceURI = XmlPullParser.NO_NAMESPACE;
        } else {
            this.namespaceURI = namespaceURI;
        }
        if (localPart == null) {
            throw new IllegalArgumentException("local part cannot be \"null\" when creating a QName");
        }
        this.localPart = localPart;
        if (prefix == null) {
            throw new IllegalArgumentException("prefix cannot be \"null\" when creating a QName");
        }
        this.prefix = prefix;
    }

    public QName(String localPart) {
        this(XmlPullParser.NO_NAMESPACE, localPart, XmlPullParser.NO_NAMESPACE);
    }

    public String getNamespaceURI() {
        return this.namespaceURI;
    }

    public String getLocalPart() {
        return this.localPart;
    }

    public String getPrefix() {
        return this.prefix;
    }

    public final boolean equals(Object objectToTest) {
        boolean z = false;
        if (objectToTest == this) {
            return true;
        }
        if (!(objectToTest instanceof QName)) {
            return false;
        }
        QName qName = (QName) objectToTest;
        if (this.localPart.equals(qName.localPart)) {
            z = this.namespaceURI.equals(qName.namespaceURI);
        }
        return z;
    }

    public final int hashCode() {
        return this.namespaceURI.hashCode() ^ this.localPart.hashCode();
    }

    public String toString() {
        String _qNameAsString = this.qNameAsString;
        if (_qNameAsString == null) {
            int nsLength = this.namespaceURI.length();
            if (nsLength == 0) {
                _qNameAsString = this.localPart;
            } else {
                StringBuilder buffer = new StringBuilder((this.localPart.length() + nsLength) + 2);
                buffer.append('{');
                buffer.append(this.namespaceURI);
                buffer.append('}');
                buffer.append(this.localPart);
                _qNameAsString = buffer.toString();
            }
            this.qNameAsString = _qNameAsString;
        }
        return _qNameAsString;
    }

    public static QName valueOf(String qNameAsString) {
        if (qNameAsString == null) {
            throw new IllegalArgumentException("cannot create QName from \"null\" or \"\" String");
        } else if (qNameAsString.length() == 0) {
            return new QName(XmlPullParser.NO_NAMESPACE, qNameAsString, XmlPullParser.NO_NAMESPACE);
        } else {
            if (qNameAsString.charAt(0) != '{') {
                return new QName(XmlPullParser.NO_NAMESPACE, qNameAsString, XmlPullParser.NO_NAMESPACE);
            }
            if (qNameAsString.startsWith("{}")) {
                throw new IllegalArgumentException("Namespace URI .equals(XMLConstants.NULL_NS_URI), .equals(\"\"), only the local part, \"" + qNameAsString.substring(XmlPullParser.NO_NAMESPACE.length() + 2) + "\", " + "should be provided.");
            }
            int endOfNamespaceURI = qNameAsString.indexOf(Opcodes.OP_NEG_LONG);
            if (endOfNamespaceURI != -1) {
                return new QName(qNameAsString.substring(1, endOfNamespaceURI), qNameAsString.substring(endOfNamespaceURI + 1), XmlPullParser.NO_NAMESPACE);
            }
            throw new IllegalArgumentException("cannot create QName from \"" + qNameAsString + "\", missing closing \"}\"");
        }
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        if (this.prefix == null) {
            this.prefix = XmlPullParser.NO_NAMESPACE;
        }
    }
}
