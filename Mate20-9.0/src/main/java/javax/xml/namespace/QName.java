package javax.xml.namespace;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

public class QName implements Serializable {
    private static final long compatibilitySerialVersionUID = 4418622981026545151L;
    private static final long defaultSerialVersionUID = -9120448754896609940L;
    private static final long serialVersionUID = (!"1.0".equals(System.getProperty("org.apache.xml.namespace.QName.useCompatibleSerialVersionUID")) ? defaultSerialVersionUID : compatibilitySerialVersionUID);
    private final String localPart;
    private final String namespaceURI;
    private String prefix;
    private transient String qNameAsString;

    public QName(String namespaceURI2, String localPart2) {
        this(namespaceURI2, localPart2, "");
    }

    public QName(String namespaceURI2, String localPart2, String prefix2) {
        if (namespaceURI2 == null) {
            this.namespaceURI = "";
        } else {
            this.namespaceURI = namespaceURI2;
        }
        if (localPart2 != null) {
            this.localPart = localPart2;
            if (prefix2 != null) {
                this.prefix = prefix2;
                return;
            }
            throw new IllegalArgumentException("prefix cannot be \"null\" when creating a QName");
        }
        throw new IllegalArgumentException("local part cannot be \"null\" when creating a QName");
    }

    public QName(String localPart2) {
        this("", localPart2, "");
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
        boolean z = true;
        if (objectToTest == this) {
            return true;
        }
        if (!(objectToTest instanceof QName)) {
            return false;
        }
        QName qName = (QName) objectToTest;
        if (!this.localPart.equals(qName.localPart) || !this.namespaceURI.equals(qName.namespaceURI)) {
            z = false;
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
                StringBuilder buffer = new StringBuilder(this.localPart.length() + nsLength + 2);
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

    public static QName valueOf(String qNameAsString2) {
        if (qNameAsString2 == null) {
            throw new IllegalArgumentException("cannot create QName from \"null\" or \"\" String");
        } else if (qNameAsString2.length() == 0) {
            return new QName("", qNameAsString2, "");
        } else {
            if (qNameAsString2.charAt(0) != '{') {
                return new QName("", qNameAsString2, "");
            }
            if (!qNameAsString2.startsWith("{}")) {
                int endOfNamespaceURI = qNameAsString2.indexOf(125);
                if (endOfNamespaceURI != -1) {
                    return new QName(qNameAsString2.substring(1, endOfNamespaceURI), qNameAsString2.substring(endOfNamespaceURI + 1), "");
                }
                throw new IllegalArgumentException("cannot create QName from \"" + qNameAsString2 + "\", missing closing \"}\"");
            }
            throw new IllegalArgumentException("Namespace URI .equals(XMLConstants.NULL_NS_URI), .equals(\"\"), only the local part, \"" + qNameAsString2.substring(2 + "".length()) + "\", should be provided.");
        }
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        if (this.prefix == null) {
            this.prefix = "";
        }
    }
}
