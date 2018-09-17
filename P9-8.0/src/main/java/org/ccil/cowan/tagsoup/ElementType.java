package org.ccil.cowan.tagsoup;

public class ElementType {
    private AttributesImpl theAtts = new AttributesImpl();
    private int theFlags;
    private String theLocalName;
    private int theMemberOf;
    private int theModel;
    private String theName;
    private String theNamespace;
    private ElementType theParent;
    private Schema theSchema;

    public ElementType(String name, int model, int memberOf, int flags, Schema schema) {
        this.theName = name;
        this.theModel = model;
        this.theMemberOf = memberOf;
        this.theFlags = flags;
        this.theSchema = schema;
        this.theNamespace = namespace(name, false);
        this.theLocalName = localName(name);
    }

    public String namespace(String name, boolean attribute) {
        int colon = name.indexOf(58);
        if (colon == -1) {
            return attribute ? "" : this.theSchema.getURI();
        }
        String prefix = name.substring(0, colon);
        if (prefix.equals("xml")) {
            return "http://www.w3.org/XML/1998/namespace";
        }
        return ("urn:x-prefix:" + prefix).intern();
    }

    public String localName(String name) {
        int colon = name.indexOf(58);
        if (colon == -1) {
            return name;
        }
        return name.substring(colon + 1).intern();
    }

    public String name() {
        return this.theName;
    }

    public String namespace() {
        return this.theNamespace;
    }

    public String localName() {
        return this.theLocalName;
    }

    public int model() {
        return this.theModel;
    }

    public int memberOf() {
        return this.theMemberOf;
    }

    public int flags() {
        return this.theFlags;
    }

    public AttributesImpl atts() {
        return this.theAtts;
    }

    public ElementType parent() {
        return this.theParent;
    }

    public Schema schema() {
        return this.theSchema;
    }

    public boolean canContain(ElementType other) {
        return (this.theModel & other.theMemberOf) != 0;
    }

    public void setAttribute(AttributesImpl atts, String name, String type, String value) {
        if (!name.equals("xmlns") && !name.startsWith("xmlns:")) {
            String namespace = namespace(name, true);
            String localName = localName(name);
            int i = atts.getIndex(name);
            if (i == -1) {
                name = name.intern();
                if (type == null) {
                    type = "CDATA";
                }
                if (!type.equals("CDATA")) {
                    value = normalize(value);
                }
                atts.addAttribute(namespace, localName, name, type, value);
            } else {
                if (type == null) {
                    type = atts.getType(i);
                }
                if (!type.equals("CDATA")) {
                    value = normalize(value);
                }
                atts.setAttribute(i, namespace, localName, name, type, value);
            }
        }
    }

    public static String normalize(String value) {
        if (value == null) {
            return value;
        }
        value = value.trim();
        if (value.indexOf("  ") == -1) {
            return value;
        }
        boolean space = false;
        int len = value.length();
        StringBuffer b = new StringBuffer(len);
        for (int i = 0; i < len; i++) {
            char v = value.charAt(i);
            if (v == ' ') {
                if (!space) {
                    b.append(v);
                }
                space = true;
            } else {
                b.append(v);
                space = false;
            }
        }
        return b.toString();
    }

    public void setAttribute(String name, String type, String value) {
        setAttribute(this.theAtts, name, type, value);
    }

    public void setModel(int model) {
        this.theModel = model;
    }

    public void setMemberOf(int memberOf) {
        this.theMemberOf = memberOf;
    }

    public void setFlags(int flags) {
        this.theFlags = flags;
    }

    public void setParent(ElementType parent) {
        this.theParent = parent;
    }
}
