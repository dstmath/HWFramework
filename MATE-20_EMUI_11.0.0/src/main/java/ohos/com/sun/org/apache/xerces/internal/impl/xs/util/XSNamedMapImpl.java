package ohos.com.sun.org.apache.xerces.internal.impl.xs.util;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import ohos.com.sun.org.apache.xerces.internal.util.SymbolHash;
import ohos.com.sun.org.apache.xerces.internal.xs.XSNamedMap;
import ohos.com.sun.org.apache.xerces.internal.xs.XSObject;
import ohos.javax.xml.namespace.QName;

public class XSNamedMapImpl extends AbstractMap implements XSNamedMap {
    public static final XSNamedMapImpl EMPTY_MAP = new XSNamedMapImpl(new XSObject[0], 0);
    XSObject[] fArray = null;
    private Set fEntrySet = null;
    int fLength = -1;
    final SymbolHash[] fMaps;
    final int fNSNum;
    final String[] fNamespaces;

    public XSNamedMapImpl(String str, SymbolHash symbolHash) {
        this.fNamespaces = new String[]{str};
        this.fMaps = new SymbolHash[]{symbolHash};
        this.fNSNum = 1;
    }

    public XSNamedMapImpl(String[] strArr, SymbolHash[] symbolHashArr, int i) {
        this.fNamespaces = strArr;
        this.fMaps = symbolHashArr;
        this.fNSNum = i;
    }

    public XSNamedMapImpl(XSObject[] xSObjectArr, int i) {
        if (i == 0) {
            this.fNamespaces = null;
            this.fMaps = null;
            this.fNSNum = 0;
            this.fArray = xSObjectArr;
            this.fLength = 0;
            return;
        }
        this.fNamespaces = new String[]{xSObjectArr[0].getNamespace()};
        this.fMaps = null;
        this.fNSNum = 1;
        this.fArray = xSObjectArr;
        this.fLength = i;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSNamedMap
    public synchronized int getLength() {
        if (this.fLength == -1) {
            this.fLength = 0;
            for (int i = 0; i < this.fNSNum; i++) {
                this.fLength += this.fMaps[i].getLength();
            }
        }
        return this.fLength;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSNamedMap
    public XSObject itemByName(String str, String str2) {
        for (int i = 0; i < this.fNSNum; i++) {
            if (isEqual(str, this.fNamespaces[i])) {
                SymbolHash[] symbolHashArr = this.fMaps;
                if (symbolHashArr != null) {
                    return (XSObject) symbolHashArr[i].get(str2);
                }
                for (int i2 = 0; i2 < this.fLength; i2++) {
                    XSObject xSObject = this.fArray[i2];
                    if (xSObject.getName().equals(str2)) {
                        return xSObject;
                    }
                }
                return null;
            }
        }
        return null;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSNamedMap
    public synchronized XSObject item(int i) {
        if (this.fArray == null) {
            getLength();
            this.fArray = new XSObject[this.fLength];
            int i2 = 0;
            for (int i3 = 0; i3 < this.fNSNum; i3++) {
                i2 += this.fMaps[i3].getValues(this.fArray, i2);
            }
        }
        if (i >= 0) {
            if (i < this.fLength) {
                return this.fArray[i];
            }
        }
        return null;
    }

    static boolean isEqual(String str, String str2) {
        if (str != null) {
            return str.equals(str2);
        }
        return str2 == null;
    }

    @Override // java.util.AbstractMap, java.util.Map
    public boolean containsKey(Object obj) {
        return get(obj) != null;
    }

    @Override // java.util.AbstractMap, java.util.Map
    public Object get(Object obj) {
        if (!(obj instanceof QName)) {
            return null;
        }
        QName qName = (QName) obj;
        String namespaceURI = qName.getNamespaceURI();
        if ("".equals(namespaceURI)) {
            namespaceURI = null;
        }
        return itemByName(namespaceURI, qName.getLocalPart());
    }

    @Override // java.util.AbstractMap, java.util.Map
    public int size() {
        return getLength();
    }

    @Override // java.util.AbstractMap, java.util.Map
    public synchronized Set entrySet() {
        if (this.fEntrySet == null) {
            final int length = getLength();
            final XSNamedMapEntry[] xSNamedMapEntryArr = new XSNamedMapEntry[length];
            for (int i = 0; i < length; i++) {
                XSObject item = item(i);
                xSNamedMapEntryArr[i] = new XSNamedMapEntry(new QName(item.getNamespace(), item.getName()), item);
            }
            this.fEntrySet = new AbstractSet() {
                /* class ohos.com.sun.org.apache.xerces.internal.impl.xs.util.XSNamedMapImpl.AnonymousClass1 */

                @Override // java.util.AbstractCollection, java.util.Collection, java.util.Set, java.lang.Iterable
                public Iterator iterator() {
                    return new Iterator() {
                        /* class ohos.com.sun.org.apache.xerces.internal.impl.xs.util.XSNamedMapImpl.AnonymousClass1.AnonymousClass1 */
                        private int index = 0;

                        @Override // java.util.Iterator
                        public boolean hasNext() {
                            return this.index < length;
                        }

                        @Override // java.util.Iterator
                        public Object next() {
                            if (this.index < length) {
                                XSNamedMapEntry[] xSNamedMapEntryArr = xSNamedMapEntryArr;
                                int i = this.index;
                                this.index = i + 1;
                                return xSNamedMapEntryArr[i];
                            }
                            throw new NoSuchElementException();
                        }

                        @Override // java.util.Iterator
                        public void remove() {
                            throw new UnsupportedOperationException();
                        }
                    };
                }

                @Override // java.util.AbstractCollection, java.util.Collection, java.util.Set
                public int size() {
                    return length;
                }
            };
        }
        return this.fEntrySet;
    }

    private static final class XSNamedMapEntry implements Map.Entry {
        private final QName key;
        private final XSObject value;

        public XSNamedMapEntry(QName qName, XSObject xSObject) {
            this.key = qName;
            this.value = xSObject;
        }

        @Override // java.util.Map.Entry
        public Object getKey() {
            return this.key;
        }

        @Override // java.util.Map.Entry
        public Object getValue() {
            return this.value;
        }

        @Override // java.util.Map.Entry
        public Object setValue(Object obj) {
            throw new UnsupportedOperationException();
        }

        @Override // java.util.Map.Entry, java.lang.Object
        public boolean equals(Object obj) {
            if (!(obj instanceof Map.Entry)) {
                return false;
            }
            Map.Entry entry = (Map.Entry) obj;
            Object key2 = entry.getKey();
            Object value2 = entry.getValue();
            QName qName = this.key;
            if (qName == null) {
                if (key2 != null) {
                    return false;
                }
            } else if (!qName.equals(key2)) {
                return false;
            }
            XSObject xSObject = this.value;
            if (xSObject == null) {
                if (value2 != null) {
                    return false;
                }
            } else if (!xSObject.equals(value2)) {
                return false;
            }
            return true;
        }

        @Override // java.util.Map.Entry, java.lang.Object
        public int hashCode() {
            QName qName = this.key;
            int i = 0;
            int hashCode = qName == null ? 0 : qName.hashCode();
            XSObject xSObject = this.value;
            if (xSObject != null) {
                i = xSObject.hashCode();
            }
            return hashCode ^ i;
        }

        @Override // java.lang.Object
        public String toString() {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append(String.valueOf(this.key));
            stringBuffer.append('=');
            stringBuffer.append(String.valueOf(this.value));
            return stringBuffer.toString();
        }
    }
}
