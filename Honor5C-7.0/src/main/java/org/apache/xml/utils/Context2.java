package org.apache.xml.utils;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/* compiled from: NamespaceSupport2 */
final class Context2 {
    private static final Enumeration EMPTY_ENUMERATION = null;
    Hashtable attributeNameTable;
    private Context2 child;
    private Vector declarations;
    String defaultNS;
    Hashtable elementNameTable;
    private Context2 parent;
    Hashtable prefixTable;
    private boolean tablesDirty;
    Hashtable uriTable;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: org.apache.xml.utils.Context2.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: org.apache.xml.utils.Context2.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.xml.utils.Context2.<clinit>():void");
    }

    Context2(Context2 parent) {
        this.defaultNS = null;
        this.declarations = null;
        this.tablesDirty = false;
        this.parent = null;
        this.child = null;
        if (parent == null) {
            this.prefixTable = new Hashtable();
            this.uriTable = new Hashtable();
            this.elementNameTable = null;
            this.attributeNameTable = null;
            return;
        }
        setParent(parent);
    }

    Context2 getChild() {
        return this.child;
    }

    Context2 getParent() {
        return this.parent;
    }

    void setParent(Context2 parent) {
        this.parent = parent;
        parent.child = this;
        this.declarations = null;
        this.prefixTable = parent.prefixTable;
        this.uriTable = parent.uriTable;
        this.elementNameTable = parent.elementNameTable;
        this.attributeNameTable = parent.attributeNameTable;
        this.defaultNS = parent.defaultNS;
        this.tablesDirty = false;
    }

    void declarePrefix(String prefix, String uri) {
        if (!this.tablesDirty) {
            copyTables();
        }
        if (this.declarations == null) {
            this.declarations = new Vector();
        }
        prefix = prefix.intern();
        uri = uri.intern();
        if (!SerializerConstants.EMPTYSTRING.equals(prefix)) {
            this.prefixTable.put(prefix, uri);
            this.uriTable.put(uri, prefix);
        } else if (SerializerConstants.EMPTYSTRING.equals(uri)) {
            this.defaultNS = null;
        } else {
            this.defaultNS = uri;
        }
        this.declarations.addElement(prefix);
    }

    String[] processName(String qName, boolean isAttribute) {
        Hashtable table;
        if (isAttribute) {
            if (this.elementNameTable == null) {
                this.elementNameTable = new Hashtable();
            }
            table = this.elementNameTable;
        } else {
            if (this.attributeNameTable == null) {
                this.attributeNameTable = new Hashtable();
            }
            table = this.attributeNameTable;
        }
        String[] name = (String[]) table.get(qName);
        if (name != null) {
            return name;
        }
        name = new String[3];
        int index = qName.indexOf(58);
        if (index == -1) {
            if (isAttribute || this.defaultNS == null) {
                name[0] = SerializerConstants.EMPTYSTRING;
            } else {
                name[0] = this.defaultNS;
            }
            name[1] = qName.intern();
            name[2] = name[1];
        } else {
            String uri;
            String prefix = qName.substring(0, index);
            String local = qName.substring(index + 1);
            if (SerializerConstants.EMPTYSTRING.equals(prefix)) {
                uri = this.defaultNS;
            } else {
                uri = (String) this.prefixTable.get(prefix);
            }
            if (uri == null) {
                return null;
            }
            name[0] = uri;
            name[1] = local.intern();
            name[2] = qName.intern();
        }
        table.put(name[2], name);
        this.tablesDirty = true;
        return name;
    }

    String getURI(String prefix) {
        if (SerializerConstants.EMPTYSTRING.equals(prefix)) {
            return this.defaultNS;
        }
        if (this.prefixTable == null) {
            return null;
        }
        return (String) this.prefixTable.get(prefix);
    }

    String getPrefix(String uri) {
        if (this.uriTable == null) {
            return null;
        }
        return (String) this.uriTable.get(uri);
    }

    Enumeration getDeclaredPrefixes() {
        if (this.declarations == null) {
            return EMPTY_ENUMERATION;
        }
        return this.declarations.elements();
    }

    Enumeration getPrefixes() {
        if (this.prefixTable == null) {
            return EMPTY_ENUMERATION;
        }
        return this.prefixTable.keys();
    }

    private void copyTables() {
        this.prefixTable = (Hashtable) this.prefixTable.clone();
        this.uriTable = (Hashtable) this.uriTable.clone();
        if (this.elementNameTable != null) {
            this.elementNameTable = new Hashtable();
        }
        if (this.attributeNameTable != null) {
            this.attributeNameTable = new Hashtable();
        }
        this.tablesDirty = true;
    }
}
