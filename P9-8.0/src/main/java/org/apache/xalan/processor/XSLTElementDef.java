package org.apache.xalan.processor;

import java.util.Enumeration;
import java.util.Hashtable;
import org.apache.xml.utils.Constants;
import org.apache.xml.utils.QName;

public class XSLTElementDef {
    static final int T_ANY = 3;
    static final int T_ELEMENT = 1;
    static final int T_PCDATA = 2;
    private XSLTAttributeDef[] m_attributes;
    private Class m_classObject;
    private XSLTElementProcessor m_elementProcessor;
    private XSLTElementDef[] m_elements;
    private boolean m_has_required;
    boolean m_isOrdered;
    private int m_lastOrder;
    private boolean m_multiAllowed;
    private String m_name;
    private String m_nameAlias;
    private String m_namespace;
    private int m_order;
    private boolean m_required;
    Hashtable m_requiredFound;
    private int m_type;

    XSLTElementDef() {
        this.m_type = 1;
        this.m_has_required = false;
        this.m_required = false;
        this.m_isOrdered = false;
        this.m_order = -1;
        this.m_lastOrder = -1;
        this.m_multiAllowed = true;
    }

    XSLTElementDef(XSLTSchema schema, String namespace, String name, String nameAlias, XSLTElementDef[] elements, XSLTAttributeDef[] attributes, XSLTElementProcessor contentHandler, Class classObject) {
        this.m_type = 1;
        this.m_has_required = false;
        this.m_required = false;
        this.m_isOrdered = false;
        this.m_order = -1;
        this.m_lastOrder = -1;
        this.m_multiAllowed = true;
        build(namespace, name, nameAlias, elements, attributes, contentHandler, classObject);
        if (namespace == null) {
            return;
        }
        if (namespace.equals(Constants.S_XSLNAMESPACEURL) || namespace.equals("http://xml.apache.org/xalan") || namespace.equals(Constants.S_BUILTIN_OLD_EXTENSIONS_URL)) {
            schema.addAvailableElement(new QName(namespace, name));
            if (nameAlias != null) {
                schema.addAvailableElement(new QName(namespace, nameAlias));
            }
        }
    }

    XSLTElementDef(XSLTSchema schema, String namespace, String name, String nameAlias, XSLTElementDef[] elements, XSLTAttributeDef[] attributes, XSLTElementProcessor contentHandler, Class classObject, boolean has_required) {
        this.m_type = 1;
        this.m_has_required = false;
        this.m_required = false;
        this.m_isOrdered = false;
        this.m_order = -1;
        this.m_lastOrder = -1;
        this.m_multiAllowed = true;
        this.m_has_required = has_required;
        build(namespace, name, nameAlias, elements, attributes, contentHandler, classObject);
        if (namespace == null) {
            return;
        }
        if (namespace.equals(Constants.S_XSLNAMESPACEURL) || namespace.equals("http://xml.apache.org/xalan") || namespace.equals(Constants.S_BUILTIN_OLD_EXTENSIONS_URL)) {
            schema.addAvailableElement(new QName(namespace, name));
            if (nameAlias != null) {
                schema.addAvailableElement(new QName(namespace, nameAlias));
            }
        }
    }

    XSLTElementDef(XSLTSchema schema, String namespace, String name, String nameAlias, XSLTElementDef[] elements, XSLTAttributeDef[] attributes, XSLTElementProcessor contentHandler, Class classObject, boolean has_required, boolean required) {
        this(schema, namespace, name, nameAlias, elements, attributes, contentHandler, classObject, has_required);
        this.m_required = required;
    }

    XSLTElementDef(XSLTSchema schema, String namespace, String name, String nameAlias, XSLTElementDef[] elements, XSLTAttributeDef[] attributes, XSLTElementProcessor contentHandler, Class classObject, boolean has_required, boolean required, int order, boolean multiAllowed) {
        this(schema, namespace, name, nameAlias, elements, attributes, contentHandler, classObject, has_required, required);
        this.m_order = order;
        this.m_multiAllowed = multiAllowed;
    }

    XSLTElementDef(XSLTSchema schema, String namespace, String name, String nameAlias, XSLTElementDef[] elements, XSLTAttributeDef[] attributes, XSLTElementProcessor contentHandler, Class classObject, boolean has_required, boolean required, boolean has_order, int order, boolean multiAllowed) {
        this(schema, namespace, name, nameAlias, elements, attributes, contentHandler, classObject, has_required, required);
        this.m_order = order;
        this.m_multiAllowed = multiAllowed;
        this.m_isOrdered = has_order;
    }

    XSLTElementDef(XSLTSchema schema, String namespace, String name, String nameAlias, XSLTElementDef[] elements, XSLTAttributeDef[] attributes, XSLTElementProcessor contentHandler, Class classObject, boolean has_order, int order, boolean multiAllowed) {
        this(schema, namespace, name, nameAlias, elements, attributes, contentHandler, classObject, order, multiAllowed);
        this.m_isOrdered = has_order;
    }

    XSLTElementDef(XSLTSchema schema, String namespace, String name, String nameAlias, XSLTElementDef[] elements, XSLTAttributeDef[] attributes, XSLTElementProcessor contentHandler, Class classObject, int order, boolean multiAllowed) {
        this(schema, namespace, name, nameAlias, elements, attributes, contentHandler, classObject);
        this.m_order = order;
        this.m_multiAllowed = multiAllowed;
    }

    XSLTElementDef(Class classObject, XSLTElementProcessor contentHandler, int type) {
        this.m_type = 1;
        this.m_has_required = false;
        this.m_required = false;
        this.m_isOrdered = false;
        this.m_order = -1;
        this.m_lastOrder = -1;
        this.m_multiAllowed = true;
        this.m_classObject = classObject;
        this.m_type = type;
        setElementProcessor(contentHandler);
    }

    void build(String namespace, String name, String nameAlias, XSLTElementDef[] elements, XSLTAttributeDef[] attributes, XSLTElementProcessor contentHandler, Class classObject) {
        this.m_namespace = namespace;
        this.m_name = name;
        this.m_nameAlias = nameAlias;
        this.m_elements = elements;
        this.m_attributes = attributes;
        setElementProcessor(contentHandler);
        this.m_classObject = classObject;
        if (hasRequired() && this.m_elements != null) {
            for (XSLTElementDef def : this.m_elements) {
                if (def != null && def.getRequired()) {
                    if (this.m_requiredFound == null) {
                        this.m_requiredFound = new Hashtable();
                    }
                    this.m_requiredFound.put(def.getName(), "xsl:" + def.getName());
                }
            }
        }
    }

    private static boolean equalsMayBeNull(Object obj1, Object obj2) {
        if (obj2 != obj1) {
            return (obj1 == null || obj2 == null) ? false : obj2.equals(obj1);
        } else {
            return true;
        }
    }

    private static boolean equalsMayBeNullOrZeroLen(String s1, String s2) {
        int len1 = s1 == null ? 0 : s1.length();
        if (len1 != (s2 == null ? 0 : s2.length())) {
            return false;
        }
        if (len1 == 0) {
            return true;
        }
        return s1.equals(s2);
    }

    int getType() {
        return this.m_type;
    }

    void setType(int t) {
        this.m_type = t;
    }

    String getNamespace() {
        return this.m_namespace;
    }

    String getName() {
        return this.m_name;
    }

    String getNameAlias() {
        return this.m_nameAlias;
    }

    public XSLTElementDef[] getElements() {
        return this.m_elements;
    }

    void setElements(XSLTElementDef[] defs) {
        this.m_elements = defs;
    }

    private boolean QNameEquals(String uri, String localName) {
        if (!equalsMayBeNullOrZeroLen(this.m_namespace, uri)) {
            return false;
        }
        if (equalsMayBeNullOrZeroLen(this.m_name, localName)) {
            return true;
        }
        return equalsMayBeNullOrZeroLen(this.m_nameAlias, localName);
    }

    /* JADX WARNING: Removed duplicated region for block: B:24:0x0064  */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x0060  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    XSLTElementProcessor getProcessorFor(String uri, String localName) {
        XSLTElementProcessor elemDef = null;
        if (this.m_elements == null) {
            return null;
        }
        int lastOrder;
        int order = -1;
        int multiAllowed = true;
        for (XSLTElementDef def : this.m_elements) {
            if (def.m_name.equals("*")) {
                if (!equalsMayBeNullOrZeroLen(uri, Constants.S_XSLNAMESPACEURL)) {
                    elemDef = def.m_elementProcessor;
                    order = def.getOrder();
                    multiAllowed = def.getMultiAllowed();
                }
            } else if (def.QNameEquals(uri, localName)) {
                if (def.getRequired()) {
                    setRequiredFound(def.getName(), true);
                }
                order = def.getOrder();
                multiAllowed = def.getMultiAllowed();
                elemDef = def.m_elementProcessor;
                if (elemDef != null && isOrdered()) {
                    lastOrder = getLastOrder();
                    if (order <= lastOrder) {
                        setLastOrder(order);
                    } else if (order == lastOrder && (multiAllowed ^ 1) != 0) {
                        return null;
                    } else {
                        if (order >= lastOrder || order <= 0) {
                            return elemDef;
                        }
                        return null;
                    }
                }
                return elemDef;
            }
        }
        lastOrder = getLastOrder();
        if (order <= lastOrder) {
        }
        return elemDef;
    }

    XSLTElementProcessor getProcessorForUnknown(String uri, String localName) {
        if (this.m_elements == null) {
            return null;
        }
        for (XSLTElementDef def : this.m_elements) {
            if (def.m_name.equals("unknown") && uri.length() > 0) {
                return def.m_elementProcessor;
            }
        }
        return null;
    }

    XSLTAttributeDef[] getAttributes() {
        return this.m_attributes;
    }

    XSLTAttributeDef getAttributeDef(String uri, String localName) {
        XSLTAttributeDef defaultDef = null;
        for (XSLTAttributeDef attrDef : getAttributes()) {
            String uriDef = attrDef.getNamespace();
            String nameDef = attrDef.getName();
            if (nameDef.equals("*") && (equalsMayBeNullOrZeroLen(uri, uriDef) || (uriDef != null && uriDef.equals("*") && uri != null && uri.length() > 0))) {
                return attrDef;
            }
            if (nameDef.equals("*") && uriDef == null) {
                defaultDef = attrDef;
            } else if (equalsMayBeNullOrZeroLen(uri, uriDef) && localName.equals(nameDef)) {
                return attrDef;
            }
        }
        if (defaultDef != null || uri.length() <= 0 || (equalsMayBeNullOrZeroLen(uri, Constants.S_XSLNAMESPACEURL) ^ 1) == 0) {
            return defaultDef;
        }
        return XSLTAttributeDef.m_foreignAttr;
    }

    public XSLTElementProcessor getElementProcessor() {
        return this.m_elementProcessor;
    }

    public void setElementProcessor(XSLTElementProcessor handler) {
        if (handler != null) {
            this.m_elementProcessor = handler;
            this.m_elementProcessor.setElemDef(this);
        }
    }

    Class getClassObject() {
        return this.m_classObject;
    }

    boolean hasRequired() {
        return this.m_has_required;
    }

    boolean getRequired() {
        return this.m_required;
    }

    void setRequiredFound(String elem, boolean found) {
        if (this.m_requiredFound.get(elem) != null) {
            this.m_requiredFound.remove(elem);
        }
    }

    boolean getRequiredFound() {
        if (this.m_requiredFound == null) {
            return true;
        }
        return this.m_requiredFound.isEmpty();
    }

    String getRequiredElem() {
        if (this.m_requiredFound == null) {
            return null;
        }
        Enumeration elems = this.m_requiredFound.elements();
        String s = "";
        boolean first = true;
        while (elems.hasMoreElements()) {
            if (first) {
                first = false;
            } else {
                s = s + ", ";
            }
            s = s + ((String) elems.nextElement());
        }
        return s;
    }

    boolean isOrdered() {
        return this.m_isOrdered;
    }

    int getOrder() {
        return this.m_order;
    }

    int getLastOrder() {
        return this.m_lastOrder;
    }

    void setLastOrder(int order) {
        this.m_lastOrder = order;
    }

    boolean getMultiAllowed() {
        return this.m_multiAllowed;
    }
}
