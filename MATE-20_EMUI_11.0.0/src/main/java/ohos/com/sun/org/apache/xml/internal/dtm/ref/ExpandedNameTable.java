package ohos.com.sun.org.apache.xml.internal.dtm.ref;

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
    private static ExtendedType[] m_defaultExtendedTypes = new ExtendedType[14];
    private static int m_initialCapacity = 203;
    private static int m_initialSize = 128;
    private static float m_loadFactor = 0.75f;
    ExtendedType hashET = new ExtendedType(-1, "", "");
    private int m_capacity = m_initialCapacity;
    private ExtendedType[] m_extendedTypes;
    private int m_nextType;
    private HashEntry[] m_table;
    private int m_threshold;

    public int getExpandedTypeID(int i) {
        return i;
    }

    static {
        for (int i = 0; i < 14; i++) {
            m_defaultExtendedTypes[i] = new ExtendedType(i, "", "");
        }
    }

    public ExpandedNameTable() {
        int i = this.m_capacity;
        this.m_threshold = (int) (((float) i) * m_loadFactor);
        this.m_table = new HashEntry[i];
        initExtendedTypes();
    }

    private void initExtendedTypes() {
        this.m_extendedTypes = new ExtendedType[m_initialSize];
        for (int i = 0; i < 14; i++) {
            ExtendedType[] extendedTypeArr = this.m_extendedTypes;
            ExtendedType[] extendedTypeArr2 = m_defaultExtendedTypes;
            extendedTypeArr[i] = extendedTypeArr2[i];
            this.m_table[i] = new HashEntry(extendedTypeArr2[i], i, i, null);
        }
        this.m_nextType = 14;
    }

    public int getExpandedTypeID(String str, String str2, int i) {
        return getExpandedTypeID(str, str2, i, false);
    }

    public int getExpandedTypeID(String str, String str2, int i, boolean z) {
        if (str == null) {
            str = "";
        }
        if (str2 == null) {
            str2 = "";
        }
        int hashCode = str.hashCode() + i + str2.hashCode();
        this.hashET.redefine(i, str, str2, hashCode);
        int i2 = hashCode % this.m_capacity;
        if (i2 < 0) {
            i2 = -i2;
        }
        for (HashEntry hashEntry = this.m_table[i2]; hashEntry != null; hashEntry = hashEntry.next) {
            if (hashEntry.hash == hashCode && hashEntry.key.equals(this.hashET)) {
                return hashEntry.value;
            }
        }
        if (z) {
            return -1;
        }
        if (this.m_nextType > this.m_threshold) {
            rehash();
            i2 = hashCode % this.m_capacity;
            if (i2 < 0) {
                i2 = -i2;
            }
        }
        ExtendedType extendedType = new ExtendedType(i, str, str2, hashCode);
        ExtendedType[] extendedTypeArr = this.m_extendedTypes;
        if (extendedTypeArr.length == this.m_nextType) {
            ExtendedType[] extendedTypeArr2 = new ExtendedType[(extendedTypeArr.length * 2)];
            System.arraycopy(extendedTypeArr, 0, extendedTypeArr2, 0, extendedTypeArr.length);
            this.m_extendedTypes = extendedTypeArr2;
        }
        ExtendedType[] extendedTypeArr3 = this.m_extendedTypes;
        int i3 = this.m_nextType;
        extendedTypeArr3[i3] = extendedType;
        this.m_table[i2] = new HashEntry(extendedType, i3, hashCode, this.m_table[i2]);
        int i4 = this.m_nextType;
        this.m_nextType = i4 + 1;
        return i4;
    }

    private void rehash() {
        int i = this.m_capacity;
        HashEntry[] hashEntryArr = this.m_table;
        int i2 = (i * 2) + 1;
        this.m_capacity = i2;
        this.m_threshold = (int) (((float) i2) * m_loadFactor);
        this.m_table = new HashEntry[i2];
        for (int i3 = i - 1; i3 >= 0; i3--) {
            HashEntry hashEntry = hashEntryArr[i3];
            while (hashEntry != null) {
                HashEntry hashEntry2 = hashEntry.next;
                int i4 = hashEntry.hash % i2;
                if (i4 < 0) {
                    i4 = -i4;
                }
                HashEntry[] hashEntryArr2 = this.m_table;
                hashEntry.next = hashEntryArr2[i4];
                hashEntryArr2[i4] = hashEntry;
                hashEntry = hashEntry2;
            }
        }
    }

    public String getLocalName(int i) {
        return this.m_extendedTypes[i].getLocalName();
    }

    public final int getLocalNameID(int i) {
        if (this.m_extendedTypes[i].getLocalName().equals("")) {
            return 0;
        }
        return i;
    }

    public String getNamespace(int i) {
        String namespace = this.m_extendedTypes[i].getNamespace();
        if (namespace.equals("")) {
            return null;
        }
        return namespace;
    }

    public final int getNamespaceID(int i) {
        if (this.m_extendedTypes[i].getNamespace().equals("")) {
            return 0;
        }
        return i;
    }

    public final short getType(int i) {
        return (short) this.m_extendedTypes[i].getNodeType();
    }

    public int getSize() {
        return this.m_nextType;
    }

    public ExtendedType[] getExtendedTypes() {
        return this.m_extendedTypes;
    }

    /* access modifiers changed from: private */
    public static final class HashEntry {
        int hash;
        ExtendedType key;
        HashEntry next;
        int value;

        protected HashEntry(ExtendedType extendedType, int i, int i2, HashEntry hashEntry) {
            this.key = extendedType;
            this.value = i;
            this.hash = i2;
            this.next = hashEntry;
        }
    }
}
