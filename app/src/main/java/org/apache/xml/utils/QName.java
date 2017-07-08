package org.apache.xml.utils;

import java.io.Serializable;
import java.util.Stack;
import java.util.StringTokenizer;
import org.apache.xalan.templates.Constants;
import org.apache.xml.res.XMLErrorResources;
import org.apache.xml.res.XMLMessages;
import org.apache.xpath.res.XPATHErrorResources;
import org.w3c.dom.Element;

public class QName implements Serializable {
    public static final String S_XMLNAMESPACEURI = "http://www.w3.org/XML/1998/namespace";
    static final long serialVersionUID = 467434581652829920L;
    protected String _localName;
    protected String _namespaceURI;
    protected String _prefix;
    private int m_hashCode;

    public QName(String namespaceURI, String localName) {
        this(namespaceURI, localName, false);
    }

    public QName(String namespaceURI, String localName, boolean validate) {
        if (localName == null) {
            throw new IllegalArgumentException(XMLMessages.createXMLMessage(XMLErrorResources.ER_ARG_LOCALNAME_NULL, null));
        } else if (!validate || XML11Char.isXML11ValidNCName(localName)) {
            this._namespaceURI = namespaceURI;
            this._localName = localName;
            this.m_hashCode = toString().hashCode();
        } else {
            throw new IllegalArgumentException(XMLMessages.createXMLMessage(XMLErrorResources.ER_ARG_LOCALNAME_INVALID, null));
        }
    }

    public QName(String namespaceURI, String prefix, String localName) {
        this(namespaceURI, prefix, localName, false);
    }

    public QName(String namespaceURI, String prefix, String localName, boolean validate) {
        if (localName == null) {
            throw new IllegalArgumentException(XMLMessages.createXMLMessage(XMLErrorResources.ER_ARG_LOCALNAME_NULL, null));
        }
        if (validate) {
            if (!XML11Char.isXML11ValidNCName(localName)) {
                throw new IllegalArgumentException(XMLMessages.createXMLMessage(XMLErrorResources.ER_ARG_LOCALNAME_INVALID, null));
            } else if (!(prefix == null || XML11Char.isXML11ValidNCName(prefix))) {
                throw new IllegalArgumentException(XMLMessages.createXMLMessage(XMLErrorResources.ER_ARG_PREFIX_INVALID, null));
            }
        }
        this._namespaceURI = namespaceURI;
        this._prefix = prefix;
        this._localName = localName;
        this.m_hashCode = toString().hashCode();
    }

    public QName(String localName) {
        this(localName, false);
    }

    public QName(String localName, boolean validate) {
        if (localName == null) {
            throw new IllegalArgumentException(XMLMessages.createXMLMessage(XMLErrorResources.ER_ARG_LOCALNAME_NULL, null));
        } else if (!validate || XML11Char.isXML11ValidNCName(localName)) {
            this._namespaceURI = null;
            this._localName = localName;
            this.m_hashCode = toString().hashCode();
        } else {
            throw new IllegalArgumentException(XMLMessages.createXMLMessage(XMLErrorResources.ER_ARG_LOCALNAME_INVALID, null));
        }
    }

    public QName(String qname, Stack namespaces) {
        this(qname, namespaces, false);
    }

    public QName(String qname, Stack namespaces, boolean validate) {
        String str = null;
        String str2 = null;
        int indexOfNSSep = qname.indexOf(58);
        if (indexOfNSSep > 0) {
            str2 = qname.substring(0, indexOfNSSep);
            if (str2.equals(SerializerConstants.XML_PREFIX)) {
                str = S_XMLNAMESPACEURI;
            } else if (!str2.equals(SerializerConstants.XMLNS_PREFIX)) {
                int i = namespaces.size() - 1;
                while (i >= 0) {
                    NameSpace ns = (NameSpace) namespaces.elementAt(i);
                    while (ns != null) {
                        if (ns.m_prefix != null && str2.equals(ns.m_prefix)) {
                            str = ns.m_uri;
                            i = -1;
                            break;
                        }
                        ns = ns.m_next;
                    }
                    i--;
                }
            } else {
                return;
            }
            if (str == null) {
                throw new RuntimeException(XMLMessages.createXMLMessage(XPATHErrorResources.ER_PREFIX_MUST_RESOLVE, new Object[]{str2}));
            }
        }
        if (indexOfNSSep >= 0) {
            qname = qname.substring(indexOfNSSep + 1);
        }
        this._localName = qname;
        if (!validate || (this._localName != null && XML11Char.isXML11ValidNCName(this._localName))) {
            this._namespaceURI = str;
            this._prefix = str2;
            this.m_hashCode = toString().hashCode();
            return;
        }
        throw new IllegalArgumentException(XMLMessages.createXMLMessage(XMLErrorResources.ER_ARG_LOCALNAME_INVALID, null));
    }

    public QName(String qname, Element namespaceContext, PrefixResolver resolver) {
        this(qname, namespaceContext, resolver, false);
    }

    public QName(String qname, Element namespaceContext, PrefixResolver resolver, boolean validate) {
        this._namespaceURI = null;
        int indexOfNSSep = qname.indexOf(58);
        if (indexOfNSSep > 0 && namespaceContext != null) {
            String prefix = qname.substring(0, indexOfNSSep);
            this._prefix = prefix;
            if (prefix.equals(SerializerConstants.XML_PREFIX)) {
                this._namespaceURI = S_XMLNAMESPACEURI;
            } else if (!prefix.equals(SerializerConstants.XMLNS_PREFIX)) {
                this._namespaceURI = resolver.getNamespaceForPrefix(prefix, namespaceContext);
            } else {
                return;
            }
            if (this._namespaceURI == null) {
                throw new RuntimeException(XMLMessages.createXMLMessage(XPATHErrorResources.ER_PREFIX_MUST_RESOLVE, new Object[]{prefix}));
            }
        }
        if (indexOfNSSep >= 0) {
            qname = qname.substring(indexOfNSSep + 1);
        }
        this._localName = qname;
        if (!validate || (this._localName != null && XML11Char.isXML11ValidNCName(this._localName))) {
            this.m_hashCode = toString().hashCode();
            return;
        }
        throw new IllegalArgumentException(XMLMessages.createXMLMessage(XMLErrorResources.ER_ARG_LOCALNAME_INVALID, null));
    }

    public QName(String qname, PrefixResolver resolver) {
        this(qname, resolver, false);
    }

    public QName(String qname, PrefixResolver resolver, boolean validate) {
        String prefix = null;
        this._namespaceURI = null;
        int indexOfNSSep = qname.indexOf(58);
        if (indexOfNSSep > 0) {
            prefix = qname.substring(0, indexOfNSSep);
            if (prefix.equals(SerializerConstants.XML_PREFIX)) {
                this._namespaceURI = S_XMLNAMESPACEURI;
            } else {
                this._namespaceURI = resolver.getNamespaceForPrefix(prefix);
            }
            if (this._namespaceURI == null) {
                throw new RuntimeException(XMLMessages.createXMLMessage(XPATHErrorResources.ER_PREFIX_MUST_RESOLVE, new Object[]{prefix}));
            }
            this._localName = qname.substring(indexOfNSSep + 1);
        } else if (indexOfNSSep == 0) {
            throw new RuntimeException(XMLMessages.createXMLMessage(XMLErrorResources.ER_NAME_CANT_START_WITH_COLON, null));
        } else {
            this._localName = qname;
        }
        if (!validate || (this._localName != null && XML11Char.isXML11ValidNCName(this._localName))) {
            this.m_hashCode = toString().hashCode();
            this._prefix = prefix;
            return;
        }
        throw new IllegalArgumentException(XMLMessages.createXMLMessage(XMLErrorResources.ER_ARG_LOCALNAME_INVALID, null));
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

    public String toString() {
        if (this._prefix != null) {
            return this._prefix + ":" + this._localName;
        }
        return this._namespaceURI != null ? "{" + this._namespaceURI + "}" + this._localName : this._localName;
    }

    public String toNamespacedString() {
        return this._namespaceURI != null ? "{" + this._namespaceURI + "}" + this._localName : this._localName;
    }

    public String getNamespace() {
        return getNamespaceURI();
    }

    public String getLocalPart() {
        return getLocalName();
    }

    public int hashCode() {
        return this.m_hashCode;
    }

    public boolean equals(String ns, String localPart) {
        String thisnamespace = getNamespaceURI();
        if (!getLocalName().equals(localPart)) {
            return false;
        }
        if (thisnamespace != null && ns != null) {
            return thisnamespace.equals(ns);
        }
        if (thisnamespace == null && ns == null) {
            return true;
        }
        return false;
    }

    public boolean equals(Object object) {
        boolean z = true;
        if (object == this) {
            return true;
        }
        if (!(object instanceof QName)) {
            return false;
        }
        QName qname = (QName) object;
        String thisnamespace = getNamespaceURI();
        String thatnamespace = qname.getNamespaceURI();
        if (!getLocalName().equals(qname.getLocalName())) {
            z = false;
        } else if (thisnamespace != null && thatnamespace != null) {
            z = thisnamespace.equals(thatnamespace);
        } else if (!(thisnamespace == null && thatnamespace == null)) {
            z = false;
        }
        return z;
    }

    public static QName getQNameFromString(String name) {
        String s2;
        StringTokenizer tokenizer = new StringTokenizer(name, "{}", false);
        String s1 = tokenizer.nextToken();
        if (tokenizer.hasMoreTokens()) {
            s2 = tokenizer.nextToken();
        } else {
            s2 = null;
        }
        if (s2 == null) {
            return new QName(null, s1);
        }
        return new QName(s1, s2);
    }

    public static boolean isXMLNSDecl(String attRawName) {
        if (!attRawName.startsWith(SerializerConstants.XMLNS_PREFIX)) {
            return false;
        }
        if (attRawName.equals(SerializerConstants.XMLNS_PREFIX)) {
            return true;
        }
        return attRawName.startsWith(Constants.ATTRNAME_XMLNS);
    }

    public static String getPrefixFromXMLNSDecl(String attRawName) {
        int index = attRawName.indexOf(58);
        return index >= 0 ? attRawName.substring(index + 1) : SerializerConstants.EMPTYSTRING;
    }

    public static String getLocalPart(String qname) {
        int index = qname.indexOf(58);
        return index < 0 ? qname : qname.substring(index + 1);
    }

    public static String getPrefixPart(String qname) {
        int index = qname.indexOf(58);
        return index >= 0 ? qname.substring(0, index) : SerializerConstants.EMPTYSTRING;
    }
}
