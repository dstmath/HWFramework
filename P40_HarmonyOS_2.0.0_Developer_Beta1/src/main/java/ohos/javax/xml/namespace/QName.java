package ohos.javax.xml.namespace;

import java.io.Serializable;
import java.security.AccessController;
import java.security.PrivilegedAction;

public class QName implements Serializable {
    private static final long compatibleSerialVersionUID = 4418622981026545151L;
    private static final long defaultSerialVersionUID = -9120448754896609940L;
    private static final long serialVersionUID;
    private static boolean useDefaultSerialVersionUID = true;
    private final String localPart;
    private final String namespaceURI;
    private final String prefix;

    static {
        try {
            String str = (String) AccessController.doPrivileged(new PrivilegedAction() {
                /* class ohos.javax.xml.namespace.QName.AnonymousClass1 */

                @Override // java.security.PrivilegedAction
                public Object run() {
                    return System.getProperty("com.sun.xml.namespace.QName.useCompatibleSerialVersionUID");
                }
            });
            useDefaultSerialVersionUID = str == null || !str.equals("1.0");
        } catch (Exception unused) {
            useDefaultSerialVersionUID = true;
        }
        if (useDefaultSerialVersionUID) {
            serialVersionUID = defaultSerialVersionUID;
        } else {
            serialVersionUID = compatibleSerialVersionUID;
        }
    }

    public QName(String str, String str2) {
        this(str, str2, "");
    }

    public QName(String str, String str2, String str3) {
        if (str == null) {
            this.namespaceURI = "";
        } else {
            this.namespaceURI = str;
        }
        if (str2 != null) {
            this.localPart = str2;
            if (str3 != null) {
                this.prefix = str3;
                return;
            }
            throw new IllegalArgumentException("prefix cannot be \"null\" when creating a QName");
        }
        throw new IllegalArgumentException("local part cannot be \"null\" when creating a QName");
    }

    public QName(String str) {
        this("", str, "");
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

    @Override // java.lang.Object
    public final boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || !(obj instanceof QName)) {
            return false;
        }
        QName qName = (QName) obj;
        return this.localPart.equals(qName.localPart) && this.namespaceURI.equals(qName.namespaceURI);
    }

    @Override // java.lang.Object
    public final int hashCode() {
        return this.localPart.hashCode() ^ this.namespaceURI.hashCode();
    }

    @Override // java.lang.Object
    public String toString() {
        if (this.namespaceURI.equals("")) {
            return this.localPart;
        }
        return "{" + this.namespaceURI + "}" + this.localPart;
    }

    public static QName valueOf(String str) {
        if (str == null) {
            throw new IllegalArgumentException("cannot create QName from \"null\" or \"\" String");
        } else if (str.length() == 0) {
            return new QName("", str, "");
        } else {
            if (str.charAt(0) != '{') {
                return new QName("", str, "");
            }
            if (!str.startsWith("{}")) {
                int indexOf = str.indexOf(125);
                if (indexOf != -1) {
                    return new QName(str.substring(1, indexOf), str.substring(indexOf + 1), "");
                }
                throw new IllegalArgumentException("cannot create QName from \"" + str + "\", missing closing \"}\"");
            }
            throw new IllegalArgumentException("Namespace URI .equals(XMLConstants.NULL_NS_URI), .equals(\"\"), only the local part, \"" + str.substring(2) + "\", should be provided.");
        }
    }
}
