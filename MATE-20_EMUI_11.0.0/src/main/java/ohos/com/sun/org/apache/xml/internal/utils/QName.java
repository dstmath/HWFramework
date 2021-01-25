package ohos.com.sun.org.apache.xml.internal.utils;

import java.io.Serializable;
import java.util.Stack;
import java.util.StringTokenizer;
import ohos.com.sun.org.apache.xml.internal.res.XMLMessages;
import ohos.org.w3c.dom.Element;

public class QName implements Serializable {
    public static final String S_XMLNAMESPACEURI = "http://www.w3.org/XML/1998/namespace";
    static final long serialVersionUID = 467434581652829920L;
    protected String _localName;
    protected String _namespaceURI;
    protected String _prefix;
    private int m_hashCode;

    public QName() {
    }

    public QName(String str, String str2) {
        this(str, str2, false);
    }

    public QName(String str, String str2, boolean z) {
        if (str2 == null) {
            throw new IllegalArgumentException(XMLMessages.createXMLMessage("ER_ARG_LOCALNAME_NULL", null));
        } else if (!z || XML11Char.isXML11ValidNCName(str2)) {
            this._namespaceURI = str;
            this._localName = str2;
            this.m_hashCode = toString().hashCode();
        } else {
            throw new IllegalArgumentException(XMLMessages.createXMLMessage("ER_ARG_LOCALNAME_INVALID", null));
        }
    }

    public QName(String str, String str2, String str3) {
        this(str, str2, str3, false);
    }

    public QName(String str, String str2, String str3, boolean z) {
        if (str3 != null) {
            if (z) {
                if (!XML11Char.isXML11ValidNCName(str3)) {
                    throw new IllegalArgumentException(XMLMessages.createXMLMessage("ER_ARG_LOCALNAME_INVALID", null));
                } else if (str2 != null && !XML11Char.isXML11ValidNCName(str2)) {
                    throw new IllegalArgumentException(XMLMessages.createXMLMessage("ER_ARG_PREFIX_INVALID", null));
                }
            }
            this._namespaceURI = str;
            this._prefix = str2;
            this._localName = str3;
            this.m_hashCode = toString().hashCode();
            return;
        }
        throw new IllegalArgumentException(XMLMessages.createXMLMessage("ER_ARG_LOCALNAME_NULL", null));
    }

    public QName(String str) {
        this(str, false);
    }

    public QName(String str, boolean z) {
        if (str == null) {
            throw new IllegalArgumentException(XMLMessages.createXMLMessage("ER_ARG_LOCALNAME_NULL", null));
        } else if (!z || XML11Char.isXML11ValidNCName(str)) {
            this._namespaceURI = null;
            this._localName = str;
            this.m_hashCode = toString().hashCode();
        } else {
            throw new IllegalArgumentException(XMLMessages.createXMLMessage("ER_ARG_LOCALNAME_INVALID", null));
        }
    }

    public QName(String str, Stack stack) {
        this(str, stack, false);
    }

    public QName(String str, Stack stack, boolean z) {
        String str2;
        String str3;
        String str4;
        int indexOf = str.indexOf(58);
        if (indexOf > 0) {
            str3 = str.substring(0, indexOf);
            if (str3.equals("xml")) {
                str2 = "http://www.w3.org/XML/1998/namespace";
            } else if (!str3.equals("xmlns")) {
                int size = stack.size() - 1;
                String str5 = null;
                while (size >= 0) {
                    NameSpace nameSpace = (NameSpace) stack.elementAt(size);
                    while (true) {
                        if (nameSpace != null) {
                            if (nameSpace.m_prefix != null && str3.equals(nameSpace.m_prefix)) {
                                str5 = nameSpace.m_uri;
                                size = -1;
                                break;
                            }
                            nameSpace = nameSpace.m_next;
                        } else {
                            break;
                        }
                    }
                    size--;
                }
                str2 = str5;
            } else {
                return;
            }
            if (str2 == null) {
                throw new RuntimeException(XMLMessages.createXMLMessage("ER_PREFIX_MUST_RESOLVE", new Object[]{str3}));
            }
        } else {
            str2 = null;
            str3 = null;
        }
        this._localName = indexOf >= 0 ? str.substring(indexOf + 1) : str;
        if (!z || ((str4 = this._localName) != null && XML11Char.isXML11ValidNCName(str4))) {
            this._namespaceURI = str2;
            this._prefix = str3;
            this.m_hashCode = toString().hashCode();
            return;
        }
        throw new IllegalArgumentException(XMLMessages.createXMLMessage("ER_ARG_LOCALNAME_INVALID", null));
    }

    public QName(String str, Element element, PrefixResolver prefixResolver) {
        this(str, element, prefixResolver, false);
    }

    public QName(String str, Element element, PrefixResolver prefixResolver, boolean z) {
        String str2;
        this._namespaceURI = null;
        int indexOf = str.indexOf(58);
        if (indexOf > 0 && element != null) {
            String substring = str.substring(0, indexOf);
            this._prefix = substring;
            if (substring.equals("xml")) {
                this._namespaceURI = "http://www.w3.org/XML/1998/namespace";
            } else if (!substring.equals("xmlns")) {
                this._namespaceURI = prefixResolver.getNamespaceForPrefix(substring, element);
            } else {
                return;
            }
            if (this._namespaceURI == null) {
                throw new RuntimeException(XMLMessages.createXMLMessage("ER_PREFIX_MUST_RESOLVE", new Object[]{substring}));
            }
        }
        this._localName = indexOf >= 0 ? str.substring(indexOf + 1) : str;
        if (!z || ((str2 = this._localName) != null && XML11Char.isXML11ValidNCName(str2))) {
            this.m_hashCode = toString().hashCode();
            return;
        }
        throw new IllegalArgumentException(XMLMessages.createXMLMessage("ER_ARG_LOCALNAME_INVALID", null));
    }

    public QName(String str, PrefixResolver prefixResolver) {
        this(str, prefixResolver, false);
    }

    public QName(String str, PrefixResolver prefixResolver, boolean z) {
        String str2;
        String str3;
        this._namespaceURI = null;
        int indexOf = str.indexOf(58);
        if (indexOf > 0) {
            str2 = str.substring(0, indexOf);
            if (str2.equals("xml")) {
                this._namespaceURI = "http://www.w3.org/XML/1998/namespace";
            } else {
                this._namespaceURI = prefixResolver.getNamespaceForPrefix(str2);
            }
            if (this._namespaceURI != null) {
                this._localName = str.substring(indexOf + 1);
            } else {
                throw new RuntimeException(XMLMessages.createXMLMessage("ER_PREFIX_MUST_RESOLVE", new Object[]{str2}));
            }
        } else if (indexOf != 0) {
            this._localName = str;
            str2 = null;
        } else {
            throw new RuntimeException(XMLMessages.createXMLMessage("ER_NAME_CANT_START_WITH_COLON", null));
        }
        if (!z || ((str3 = this._localName) != null && XML11Char.isXML11ValidNCName(str3))) {
            this.m_hashCode = toString().hashCode();
            this._prefix = str2;
            return;
        }
        throw new IllegalArgumentException(XMLMessages.createXMLMessage("ER_ARG_LOCALNAME_INVALID", null));
    }

    public String getNamespaceURI() {
        return this._namespaceURI;
    }

    public String getPrefix() {
        return this._prefix;
    }

    public String getLocalName() {
        return this._localName;
    }

    @Override // java.lang.Object
    public String toString() {
        if (this._prefix != null) {
            return this._prefix + ":" + this._localName;
        } else if (this._namespaceURI == null) {
            return this._localName;
        } else {
            return "{" + this._namespaceURI + "}" + this._localName;
        }
    }

    public String toNamespacedString() {
        if (this._namespaceURI == null) {
            return this._localName;
        }
        return "{" + this._namespaceURI + "}" + this._localName;
    }

    public String getNamespace() {
        return getNamespaceURI();
    }

    public String getLocalPart() {
        return getLocalName();
    }

    @Override // java.lang.Object
    public int hashCode() {
        return this.m_hashCode;
    }

    public boolean equals(String str, String str2) {
        String namespaceURI = getNamespaceURI();
        return getLocalName().equals(str2) && (namespaceURI == null || str == null ? namespaceURI == null && str == null : namespaceURI.equals(str));
    }

    @Override // java.lang.Object
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof QName)) {
            return false;
        }
        QName qName = (QName) obj;
        String namespaceURI = getNamespaceURI();
        String namespaceURI2 = qName.getNamespaceURI();
        if (getLocalName().equals(qName.getLocalName())) {
            if (namespaceURI == null || namespaceURI2 == null) {
                if (namespaceURI == null && namespaceURI2 == null) {
                    return true;
                }
            } else if (namespaceURI.equals(namespaceURI2)) {
                return true;
            }
        }
        return false;
    }

    public static QName getQNameFromString(String str) {
        StringTokenizer stringTokenizer = new StringTokenizer(str, "{}", false);
        String nextToken = stringTokenizer.nextToken();
        String nextToken2 = stringTokenizer.hasMoreTokens() ? stringTokenizer.nextToken() : null;
        if (nextToken2 == null) {
            return new QName((String) null, nextToken);
        }
        return new QName(nextToken, nextToken2);
    }

    public static boolean isXMLNSDecl(String str) {
        return str.startsWith("xmlns") && (str.equals("xmlns") || str.startsWith("xmlns:"));
    }

    public static String getPrefixFromXMLNSDecl(String str) {
        int indexOf = str.indexOf(58);
        return indexOf >= 0 ? str.substring(indexOf + 1) : "";
    }

    public static String getLocalPart(String str) {
        int indexOf = str.indexOf(58);
        return indexOf < 0 ? str : str.substring(indexOf + 1);
    }

    public static String getPrefixPart(String str) {
        int indexOf = str.indexOf(58);
        return indexOf >= 0 ? str.substring(0, indexOf) : "";
    }
}
