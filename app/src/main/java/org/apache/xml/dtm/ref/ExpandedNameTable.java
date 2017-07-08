package org.apache.xml.dtm.ref;

public class ExpandedNameTable {
    public static final int ATTRIBUTE = 2;
    public static final int CDATA_SECTION = 4;
    public static final int COMMENT = 8;
    public static final int DOCUMENT = 9;
    public static final int DOCUMENT_FRAGMENT = 11;
    public static final int DOCUMENT_TYPE = 10;
    public static final int ELEMENT = 1;
    public static final int ENTITY = 6;
    public static final int ENTITY_REFERENCE = 5;
    public static final int NAMESPACE = 13;
    public static final int NOTATION = 12;
    public static final int PROCESSING_INSTRUCTION = 7;
    public static final int TEXT = 3;
    private static ExtendedType[] m_defaultExtendedTypes;
    private static int m_initialCapacity;
    private static int m_initialSize;
    private static float m_loadFactor;
    ExtendedType hashET;
    private int m_capacity;
    private ExtendedType[] m_extendedTypes;
    private int m_nextType;
    private HashEntry[] m_table;
    private int m_threshold;

    private static final class HashEntry {
        int hash;
        ExtendedType key;
        HashEntry next;
        int value;

        protected HashEntry(ExtendedType key, int value, int hash, HashEntry next) {
            this.key = key;
            this.value = value;
            this.hash = hash;
            this.next = next;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: org.apache.xml.dtm.ref.ExpandedNameTable.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: org.apache.xml.dtm.ref.ExpandedNameTable.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.xml.dtm.ref.ExpandedNameTable.<clinit>():void");
    }

    public ExpandedNameTable() {
        this.hashET = new ExtendedType(-1, SerializerConstants.EMPTYSTRING, SerializerConstants.EMPTYSTRING);
        this.m_capacity = m_initialCapacity;
        this.m_threshold = (int) (((float) this.m_capacity) * m_loadFactor);
        this.m_table = new HashEntry[this.m_capacity];
        initExtendedTypes();
    }

    private void initExtendedTypes() {
        this.m_extendedTypes = new ExtendedType[m_initialSize];
        for (int i = 0; i < 14; i += ELEMENT) {
            this.m_extendedTypes[i] = m_defaultExtendedTypes[i];
            this.m_table[i] = new HashEntry(m_defaultExtendedTypes[i], i, i, null);
        }
        this.m_nextType = 14;
    }

    public int getExpandedTypeID(String namespace, String localName, int type) {
        return getExpandedTypeID(namespace, localName, type, false);
    }

    public int getExpandedTypeID(String namespace, String localName, int type, boolean searchOnly) {
        if (namespace == null) {
            namespace = SerializerConstants.EMPTYSTRING;
        }
        if (localName == null) {
            localName = SerializerConstants.EMPTYSTRING;
        }
        int hash = (namespace.hashCode() + type) + localName.hashCode();
        this.hashET.redefine(type, namespace, localName, hash);
        int index = hash % this.m_capacity;
        if (index < 0) {
            index = -index;
        }
        HashEntry e = this.m_table[index];
        while (e != null) {
            if (e.hash == hash && e.key.equals(this.hashET)) {
                return e.value;
            }
            e = e.next;
        }
        if (searchOnly) {
            return -1;
        }
        if (this.m_nextType > this.m_threshold) {
            rehash();
            index = hash % this.m_capacity;
            if (index < 0) {
                index = -index;
            }
        }
        ExtendedType newET = new ExtendedType(type, namespace, localName, hash);
        if (this.m_extendedTypes.length == this.m_nextType) {
            ExtendedType[] newArray = new ExtendedType[(this.m_extendedTypes.length * ATTRIBUTE)];
            System.arraycopy(this.m_extendedTypes, 0, newArray, 0, this.m_extendedTypes.length);
            this.m_extendedTypes = newArray;
        }
        this.m_extendedTypes[this.m_nextType] = newET;
        this.m_table[index] = new HashEntry(newET, this.m_nextType, hash, this.m_table[index]);
        int i = this.m_nextType;
        this.m_nextType = i + ELEMENT;
        return i;
    }

    private void rehash() {
        int oldCapacity = this.m_capacity;
        HashEntry[] oldTable = this.m_table;
        int newCapacity = (oldCapacity * ATTRIBUTE) + ELEMENT;
        this.m_capacity = newCapacity;
        this.m_threshold = (int) (((float) newCapacity) * m_loadFactor);
        this.m_table = new HashEntry[newCapacity];
        for (int i = oldCapacity - 1; i >= 0; i--) {
            HashEntry old = oldTable[i];
            while (old != null) {
                HashEntry e = old;
                old = old.next;
                int newIndex = e.hash % newCapacity;
                if (newIndex < 0) {
                    newIndex = -newIndex;
                }
                e.next = this.m_table[newIndex];
                this.m_table[newIndex] = e;
            }
        }
    }

    public int getExpandedTypeID(int type) {
        return type;
    }

    public String getLocalName(int ExpandedNameID) {
        return this.m_extendedTypes[ExpandedNameID].getLocalName();
    }

    public final int getLocalNameID(int ExpandedNameID) {
        if (this.m_extendedTypes[ExpandedNameID].getLocalName().equals(SerializerConstants.EMPTYSTRING)) {
            return 0;
        }
        return ExpandedNameID;
    }

    public String getNamespace(int ExpandedNameID) {
        String namespace = this.m_extendedTypes[ExpandedNameID].getNamespace();
        return namespace.equals(SerializerConstants.EMPTYSTRING) ? null : namespace;
    }

    public final int getNamespaceID(int ExpandedNameID) {
        if (this.m_extendedTypes[ExpandedNameID].getNamespace().equals(SerializerConstants.EMPTYSTRING)) {
            return 0;
        }
        return ExpandedNameID;
    }

    public final short getType(int ExpandedNameID) {
        return (short) this.m_extendedTypes[ExpandedNameID].getNodeType();
    }

    public int getSize() {
        return this.m_nextType;
    }

    public ExtendedType[] getExtendedTypes() {
        return this.m_extendedTypes;
    }
}
