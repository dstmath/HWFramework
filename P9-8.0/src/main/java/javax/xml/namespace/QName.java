package javax.xml.namespace;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

public class QName implements Serializable {
    private static final long compatibilitySerialVersionUID = 4418622981026545151L;
    private static final long defaultSerialVersionUID = -9120448754896609940L;
    private static final long serialVersionUID;
    private final String localPart;
    private final String namespaceURI;
    private String prefix;
    private transient String qNameAsString;

    static {
        long j;
        if ("1.0".equals(System.getProperty("org.apache.xml.namespace.QName.useCompatibleSerialVersionUID"))) {
            j = compatibilitySerialVersionUID;
        } else {
            j = defaultSerialVersionUID;
        }
        serialVersionUID = j;
    }

    public QName(String namespaceURI, String localPart) {
        this(namespaceURI, localPart, "");
    }

    public QName(String namespaceURI, String localPart, String prefix) {
        if (namespaceURI == null) {
            this.namespaceURI = "";
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
        this("", localPart, "");
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
            return new QName("", qNameAsString, "");
        } else {
            if (qNameAsString.charAt(0) != '{') {
                return new QName("", qNameAsString, "");
            }
            if (qNameAsString.startsWith("{}")) {
                throw new IllegalArgumentException("Namespace URI .equals(XMLConstants.NULL_NS_URI), .equals(\"\"), only the local part, \"" + qNameAsString.substring("".length() + 2) + "\", " + "should be provided.");
            }
            int endOfNamespaceURI = qNameAsString.indexOf(125);
            if (endOfNamespaceURI != -1) {
                return new QName(qNameAsString.substring(1, endOfNamespaceURI), qNameAsString.substring(endOfNamespaceURI + 1), "");
            }
            throw new IllegalArgumentException("cannot create QName from \"" + qNameAsString + "\", missing closing \"}\"");
        }
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        if (this.prefix == null) {
            this.prefix = "";
        }
    }
}
