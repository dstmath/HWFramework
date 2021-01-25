package ohos.com.sun.org.apache.xerces.internal.impl.xs;

import java.lang.reflect.Array;
import java.util.AbstractList;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Vector;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.util.StringListImpl;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.util.XSNamedMap4Types;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.util.XSNamedMapImpl;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.util.XSObjectListImpl;
import ohos.com.sun.org.apache.xerces.internal.util.SymbolHash;
import ohos.com.sun.org.apache.xerces.internal.util.XMLSymbols;
import ohos.com.sun.org.apache.xerces.internal.xs.StringList;
import ohos.com.sun.org.apache.xerces.internal.xs.XSAttributeDeclaration;
import ohos.com.sun.org.apache.xerces.internal.xs.XSAttributeGroupDefinition;
import ohos.com.sun.org.apache.xerces.internal.xs.XSElementDeclaration;
import ohos.com.sun.org.apache.xerces.internal.xs.XSModel;
import ohos.com.sun.org.apache.xerces.internal.xs.XSModelGroupDefinition;
import ohos.com.sun.org.apache.xerces.internal.xs.XSNamedMap;
import ohos.com.sun.org.apache.xerces.internal.xs.XSNamespaceItem;
import ohos.com.sun.org.apache.xerces.internal.xs.XSNamespaceItemList;
import ohos.com.sun.org.apache.xerces.internal.xs.XSNotationDeclaration;
import ohos.com.sun.org.apache.xerces.internal.xs.XSObject;
import ohos.com.sun.org.apache.xerces.internal.xs.XSObjectList;
import ohos.com.sun.org.apache.xerces.internal.xs.XSTypeDefinition;

public final class XSModelImpl extends AbstractList implements XSModel, XSNamespaceItemList {
    private static final boolean[] GLOBAL_COMP = {false, true, true, true, false, true, true, false, false, false, false, true, false, false, false, true, true};
    private static final short MAX_COMP_IDX = 16;
    private XSObjectList fAnnotations;
    private final XSNamedMap[] fGlobalComponents;
    private final int fGrammarCount;
    private final SchemaGrammar[] fGrammarList;
    private final SymbolHash fGrammarMap;
    private final boolean fHasIDC;
    private final XSNamedMap[][] fNSComponents;
    private final String[] fNamespaces;
    private final StringList fNamespacesList;
    private final SymbolHash fSubGroupMap;

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSModel
    public XSNamespaceItemList getNamespaceItems() {
        return this;
    }

    public XSModelImpl(SchemaGrammar[] schemaGrammarArr) {
        this(schemaGrammarArr, 1);
    }

    public XSModelImpl(SchemaGrammar[] schemaGrammarArr, short s) {
        int i;
        this.fAnnotations = null;
        int length = schemaGrammarArr.length;
        int i2 = length + 1;
        int max = Math.max(i2, 5);
        String[] strArr = new String[max];
        SchemaGrammar[] schemaGrammarArr2 = new SchemaGrammar[max];
        boolean z = false;
        for (int i3 = 0; i3 < length; i3++) {
            SchemaGrammar schemaGrammar = schemaGrammarArr[i3];
            String targetNamespace = schemaGrammar.getTargetNamespace();
            strArr[i3] = targetNamespace;
            schemaGrammarArr2[i3] = schemaGrammar;
            if (targetNamespace == SchemaSymbols.URI_SCHEMAFORSCHEMA) {
                z = true;
            }
        }
        if (!z) {
            strArr[length] = SchemaSymbols.URI_SCHEMAFORSCHEMA;
            schemaGrammarArr2[length] = SchemaGrammar.getS4SGrammar(s);
            length = i2;
        }
        for (int i4 = 0; i4 < length; i4++) {
            Vector importedGrammars = schemaGrammarArr2[i4].getImportedGrammars();
            if (importedGrammars == null) {
                i = -1;
            } else {
                i = importedGrammars.size() - 1;
            }
            while (i >= 0) {
                SchemaGrammar schemaGrammar2 = (SchemaGrammar) importedGrammars.elementAt(i);
                int i5 = 0;
                while (i5 < length && schemaGrammar2 != schemaGrammarArr2[i5]) {
                    i5++;
                }
                if (i5 == length) {
                    if (length == schemaGrammarArr2.length) {
                        int i6 = length * 2;
                        String[] strArr2 = new String[i6];
                        System.arraycopy(strArr, 0, strArr2, 0, length);
                        SchemaGrammar[] schemaGrammarArr3 = new SchemaGrammar[i6];
                        System.arraycopy(schemaGrammarArr2, 0, schemaGrammarArr3, 0, length);
                        schemaGrammarArr2 = schemaGrammarArr3;
                        strArr = strArr2;
                    }
                    strArr[length] = schemaGrammar2.getTargetNamespace();
                    schemaGrammarArr2[length] = schemaGrammar2;
                    length++;
                }
                i--;
            }
        }
        this.fNamespaces = strArr;
        this.fGrammarList = schemaGrammarArr2;
        this.fGrammarMap = new SymbolHash(length * 2);
        boolean z2 = false;
        for (int i7 = 0; i7 < length; i7++) {
            this.fGrammarMap.put(null2EmptyString(this.fNamespaces[i7]), this.fGrammarList[i7]);
            if (this.fGrammarList[i7].hasIDConstraints()) {
                z2 = true;
            }
        }
        this.fHasIDC = z2;
        this.fGrammarCount = length;
        this.fGlobalComponents = new XSNamedMap[17];
        this.fNSComponents = (XSNamedMap[][]) Array.newInstance(XSNamedMap.class, length, 17);
        this.fNamespacesList = new StringListImpl(this.fNamespaces, this.fGrammarCount);
        this.fSubGroupMap = buildSubGroups();
    }

    private SymbolHash buildSubGroups_Org() {
        SubstitutionGroupHandler substitutionGroupHandler = new SubstitutionGroupHandler(null);
        for (int i = 0; i < this.fGrammarCount; i++) {
            substitutionGroupHandler.addSubstitutionGroup(this.fGrammarList[i].getSubstitutionGroups());
        }
        XSNamedMap components = getComponents(2);
        int length = components.getLength();
        SymbolHash symbolHash = new SymbolHash(length * 2);
        for (int i2 = 0; i2 < length; i2++) {
            XSElementDecl xSElementDecl = (XSElementDecl) components.item(i2);
            XSElementDecl[] substitutionGroup = substitutionGroupHandler.getSubstitutionGroup(xSElementDecl);
            symbolHash.put(xSElementDecl, substitutionGroup.length > 0 ? new XSObjectListImpl(substitutionGroup, substitutionGroup.length) : XSObjectListImpl.EMPTY_LIST);
        }
        return symbolHash;
    }

    private SymbolHash buildSubGroups() {
        SubstitutionGroupHandler substitutionGroupHandler = new SubstitutionGroupHandler(null);
        for (int i = 0; i < this.fGrammarCount; i++) {
            substitutionGroupHandler.addSubstitutionGroup(this.fGrammarList[i].getSubstitutionGroups());
        }
        XSObjectListImpl globalElements = getGlobalElements();
        int length = globalElements.getLength();
        SymbolHash symbolHash = new SymbolHash(length * 2);
        for (int i2 = 0; i2 < length; i2++) {
            XSElementDecl xSElementDecl = (XSElementDecl) globalElements.item(i2);
            XSElementDecl[] substitutionGroup = substitutionGroupHandler.getSubstitutionGroup(xSElementDecl);
            symbolHash.put(xSElementDecl, substitutionGroup.length > 0 ? new XSObjectListImpl(substitutionGroup, substitutionGroup.length) : XSObjectListImpl.EMPTY_LIST);
        }
        return symbolHash;
    }

    private XSObjectListImpl getGlobalElements() {
        SymbolHash[] symbolHashArr = new SymbolHash[this.fGrammarCount];
        int i = 0;
        for (int i2 = 0; i2 < this.fGrammarCount; i2++) {
            symbolHashArr[i2] = this.fGrammarList[i2].fAllGlobalElemDecls;
            i += symbolHashArr[i2].getLength();
        }
        if (i == 0) {
            return XSObjectListImpl.EMPTY_LIST;
        }
        XSObject[] xSObjectArr = new XSObject[i];
        int i3 = 0;
        for (int i4 = 0; i4 < this.fGrammarCount; i4++) {
            symbolHashArr[i4].getValues(xSObjectArr, i3);
            i3 += symbolHashArr[i4].getLength();
        }
        return new XSObjectListImpl(xSObjectArr, i);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSModel
    public StringList getNamespaces() {
        return this.fNamespacesList;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSModel
    public synchronized XSNamedMap getComponents(short s) {
        if (s > 0 && s <= 16) {
            if (GLOBAL_COMP[s]) {
                SymbolHash[] symbolHashArr = new SymbolHash[this.fGrammarCount];
                if (this.fGlobalComponents[s] == null) {
                    for (int i = 0; i < this.fGrammarCount; i++) {
                        if (s == 1) {
                            symbolHashArr[i] = this.fGrammarList[i].fGlobalAttrDecls;
                        } else if (s != 2) {
                            if (s != 3) {
                                if (s == 5) {
                                    symbolHashArr[i] = this.fGrammarList[i].fGlobalAttrGrpDecls;
                                } else if (s == 6) {
                                    symbolHashArr[i] = this.fGrammarList[i].fGlobalGroupDecls;
                                } else if (s == 11) {
                                    symbolHashArr[i] = this.fGrammarList[i].fGlobalNotationDecls;
                                } else if (!(s == 15 || s == 16)) {
                                }
                            }
                            symbolHashArr[i] = this.fGrammarList[i].fGlobalTypeDecls;
                        } else {
                            symbolHashArr[i] = this.fGrammarList[i].fGlobalElemDecls;
                        }
                    }
                    if (s != 15) {
                        if (s != 16) {
                            this.fGlobalComponents[s] = new XSNamedMapImpl(this.fNamespaces, symbolHashArr, this.fGrammarCount);
                        }
                    }
                    this.fGlobalComponents[s] = new XSNamedMap4Types(this.fNamespaces, symbolHashArr, this.fGrammarCount, s);
                }
                return this.fGlobalComponents[s];
            }
        }
        return XSNamedMapImpl.EMPTY_MAP;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:42:0x005a, code lost:
        if (r6 != 16) goto L_0x0086;
     */
    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSModel
    public synchronized XSNamedMap getComponentsByNamespace(short s, String str) {
        if (s > 0 && s <= 16) {
            if (GLOBAL_COMP[s]) {
                int i = 0;
                if (str != null) {
                    while (true) {
                        if (i >= this.fGrammarCount) {
                            break;
                        } else if (str.equals(this.fNamespaces[i])) {
                            break;
                        } else {
                            i++;
                        }
                    }
                } else {
                    while (true) {
                        if (i >= this.fGrammarCount) {
                            break;
                        } else if (this.fNamespaces[i] == null) {
                            break;
                        } else {
                            i++;
                        }
                    }
                }
                if (i == this.fGrammarCount) {
                    return XSNamedMapImpl.EMPTY_MAP;
                }
                if (this.fNSComponents[i][s] == null) {
                    SymbolHash symbolHash = null;
                    if (s == 1) {
                        symbolHash = this.fGrammarList[i].fGlobalAttrDecls;
                    } else if (s != 2) {
                        if (s != 3) {
                            if (s == 5) {
                                symbolHash = this.fGrammarList[i].fGlobalAttrGrpDecls;
                            } else if (s == 6) {
                                symbolHash = this.fGrammarList[i].fGlobalGroupDecls;
                            } else if (s == 11) {
                                symbolHash = this.fGrammarList[i].fGlobalNotationDecls;
                            } else if (s != 15) {
                            }
                        }
                        symbolHash = this.fGrammarList[i].fGlobalTypeDecls;
                    } else {
                        symbolHash = this.fGrammarList[i].fGlobalElemDecls;
                    }
                    if (s == 15 || s == 16) {
                        this.fNSComponents[i][s] = new XSNamedMap4Types(str, symbolHash, s);
                    } else {
                        this.fNSComponents[i][s] = new XSNamedMapImpl(str, symbolHash);
                    }
                }
                return this.fNSComponents[i][s];
            }
        }
        return XSNamedMapImpl.EMPTY_MAP;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSModel
    public XSTypeDefinition getTypeDefinition(String str, String str2) {
        SchemaGrammar schemaGrammar = (SchemaGrammar) this.fGrammarMap.get(null2EmptyString(str2));
        if (schemaGrammar == null) {
            return null;
        }
        return (XSTypeDefinition) schemaGrammar.fGlobalTypeDecls.get(str);
    }

    public XSTypeDefinition getTypeDefinition(String str, String str2, String str3) {
        SchemaGrammar schemaGrammar = (SchemaGrammar) this.fGrammarMap.get(null2EmptyString(str2));
        if (schemaGrammar == null) {
            return null;
        }
        return schemaGrammar.getGlobalTypeDecl(str, str3);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSModel
    public XSAttributeDeclaration getAttributeDeclaration(String str, String str2) {
        SchemaGrammar schemaGrammar = (SchemaGrammar) this.fGrammarMap.get(null2EmptyString(str2));
        if (schemaGrammar == null) {
            return null;
        }
        return (XSAttributeDeclaration) schemaGrammar.fGlobalAttrDecls.get(str);
    }

    public XSAttributeDeclaration getAttributeDeclaration(String str, String str2, String str3) {
        SchemaGrammar schemaGrammar = (SchemaGrammar) this.fGrammarMap.get(null2EmptyString(str2));
        if (schemaGrammar == null) {
            return null;
        }
        return schemaGrammar.getGlobalAttributeDecl(str, str3);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSModel
    public XSElementDeclaration getElementDeclaration(String str, String str2) {
        SchemaGrammar schemaGrammar = (SchemaGrammar) this.fGrammarMap.get(null2EmptyString(str2));
        if (schemaGrammar == null) {
            return null;
        }
        return (XSElementDeclaration) schemaGrammar.fGlobalElemDecls.get(str);
    }

    public XSElementDeclaration getElementDeclaration(String str, String str2, String str3) {
        SchemaGrammar schemaGrammar = (SchemaGrammar) this.fGrammarMap.get(null2EmptyString(str2));
        if (schemaGrammar == null) {
            return null;
        }
        return schemaGrammar.getGlobalElementDecl(str, str3);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSModel
    public XSAttributeGroupDefinition getAttributeGroup(String str, String str2) {
        SchemaGrammar schemaGrammar = (SchemaGrammar) this.fGrammarMap.get(null2EmptyString(str2));
        if (schemaGrammar == null) {
            return null;
        }
        return (XSAttributeGroupDefinition) schemaGrammar.fGlobalAttrGrpDecls.get(str);
    }

    public XSAttributeGroupDefinition getAttributeGroup(String str, String str2, String str3) {
        SchemaGrammar schemaGrammar = (SchemaGrammar) this.fGrammarMap.get(null2EmptyString(str2));
        if (schemaGrammar == null) {
            return null;
        }
        return schemaGrammar.getGlobalAttributeGroupDecl(str, str3);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSModel
    public XSModelGroupDefinition getModelGroupDefinition(String str, String str2) {
        SchemaGrammar schemaGrammar = (SchemaGrammar) this.fGrammarMap.get(null2EmptyString(str2));
        if (schemaGrammar == null) {
            return null;
        }
        return (XSModelGroupDefinition) schemaGrammar.fGlobalGroupDecls.get(str);
    }

    public XSModelGroupDefinition getModelGroupDefinition(String str, String str2, String str3) {
        SchemaGrammar schemaGrammar = (SchemaGrammar) this.fGrammarMap.get(null2EmptyString(str2));
        if (schemaGrammar == null) {
            return null;
        }
        return schemaGrammar.getGlobalGroupDecl(str, str3);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSModel
    public XSNotationDeclaration getNotationDeclaration(String str, String str2) {
        SchemaGrammar schemaGrammar = (SchemaGrammar) this.fGrammarMap.get(null2EmptyString(str2));
        if (schemaGrammar == null) {
            return null;
        }
        return (XSNotationDeclaration) schemaGrammar.fGlobalNotationDecls.get(str);
    }

    public XSNotationDeclaration getNotationDeclaration(String str, String str2, String str3) {
        SchemaGrammar schemaGrammar = (SchemaGrammar) this.fGrammarMap.get(null2EmptyString(str2));
        if (schemaGrammar == null) {
            return null;
        }
        return schemaGrammar.getGlobalNotationDecl(str, str3);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSModel
    public synchronized XSObjectList getAnnotations() {
        if (this.fAnnotations != null) {
            return this.fAnnotations;
        }
        int i = 0;
        for (int i2 = 0; i2 < this.fGrammarCount; i2++) {
            i += this.fGrammarList[i2].fNumAnnotations;
        }
        if (i == 0) {
            this.fAnnotations = XSObjectListImpl.EMPTY_LIST;
            return this.fAnnotations;
        }
        XSAnnotationImpl[] xSAnnotationImplArr = new XSAnnotationImpl[i];
        int i3 = 0;
        for (int i4 = 0; i4 < this.fGrammarCount; i4++) {
            SchemaGrammar schemaGrammar = this.fGrammarList[i4];
            if (schemaGrammar.fNumAnnotations > 0) {
                System.arraycopy(schemaGrammar.fAnnotations, 0, xSAnnotationImplArr, i3, schemaGrammar.fNumAnnotations);
                i3 += schemaGrammar.fNumAnnotations;
            }
        }
        this.fAnnotations = new XSObjectListImpl(xSAnnotationImplArr, xSAnnotationImplArr.length);
        return this.fAnnotations;
    }

    private static final String null2EmptyString(String str) {
        return str == null ? XMLSymbols.EMPTY_STRING : str;
    }

    public boolean hasIDConstraints() {
        return this.fHasIDC;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSModel
    public XSObjectList getSubstitutionGroup(XSElementDeclaration xSElementDeclaration) {
        return (XSObjectList) this.fSubGroupMap.get(xSElementDeclaration);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSNamespaceItemList
    public int getLength() {
        return this.fGrammarCount;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSNamespaceItemList
    public XSNamespaceItem item(int i) {
        if (i < 0 || i >= this.fGrammarCount) {
            return null;
        }
        return this.fGrammarList[i];
    }

    @Override // java.util.AbstractList, java.util.List
    public Object get(int i) {
        if (i >= 0 && i < this.fGrammarCount) {
            return this.fGrammarList[i];
        }
        throw new IndexOutOfBoundsException("Index: " + i);
    }

    @Override // java.util.AbstractCollection, java.util.List, java.util.Collection
    public int size() {
        return getLength();
    }

    @Override // java.util.AbstractList, java.util.AbstractCollection, java.util.List, java.util.Collection, java.lang.Iterable
    public Iterator iterator() {
        return listIterator0(0);
    }

    @Override // java.util.AbstractList, java.util.List
    public ListIterator listIterator() {
        return listIterator0(0);
    }

    @Override // java.util.AbstractList, java.util.List
    public ListIterator listIterator(int i) {
        if (i >= 0 && i < this.fGrammarCount) {
            return listIterator0(i);
        }
        throw new IndexOutOfBoundsException("Index: " + i);
    }

    private ListIterator listIterator0(int i) {
        return new XSNamespaceItemListIterator(i);
    }

    @Override // java.util.AbstractCollection, java.util.List, java.util.Collection
    public Object[] toArray() {
        Object[] objArr = new Object[this.fGrammarCount];
        toArray0(objArr);
        return objArr;
    }

    @Override // java.util.AbstractCollection, java.util.List, java.util.Collection
    public Object[] toArray(Object[] objArr) {
        if (objArr.length < this.fGrammarCount) {
            objArr = (Object[]) Array.newInstance(objArr.getClass().getComponentType(), this.fGrammarCount);
        }
        toArray0(objArr);
        int length = objArr.length;
        int i = this.fGrammarCount;
        if (length > i) {
            objArr[i] = null;
        }
        return objArr;
    }

    private void toArray0(Object[] objArr) {
        int i = this.fGrammarCount;
        if (i > 0) {
            System.arraycopy(this.fGrammarList, 0, objArr, 0, i);
        }
    }

    /* access modifiers changed from: private */
    public final class XSNamespaceItemListIterator implements ListIterator {
        private int index;

        public XSNamespaceItemListIterator(int i) {
            this.index = i;
        }

        @Override // java.util.ListIterator, java.util.Iterator
        public boolean hasNext() {
            return this.index < XSModelImpl.this.fGrammarCount;
        }

        @Override // java.util.ListIterator, java.util.Iterator
        public Object next() {
            if (this.index < XSModelImpl.this.fGrammarCount) {
                SchemaGrammar[] schemaGrammarArr = XSModelImpl.this.fGrammarList;
                int i = this.index;
                this.index = i + 1;
                return schemaGrammarArr[i];
            }
            throw new NoSuchElementException();
        }

        @Override // java.util.ListIterator
        public boolean hasPrevious() {
            return this.index > 0;
        }

        @Override // java.util.ListIterator
        public Object previous() {
            if (this.index > 0) {
                SchemaGrammar[] schemaGrammarArr = XSModelImpl.this.fGrammarList;
                int i = this.index - 1;
                this.index = i;
                return schemaGrammarArr[i];
            }
            throw new NoSuchElementException();
        }

        @Override // java.util.ListIterator
        public int nextIndex() {
            return this.index;
        }

        @Override // java.util.ListIterator
        public int previousIndex() {
            return this.index - 1;
        }

        @Override // java.util.ListIterator, java.util.Iterator
        public void remove() {
            throw new UnsupportedOperationException();
        }

        @Override // java.util.ListIterator
        public void set(Object obj) {
            throw new UnsupportedOperationException();
        }

        @Override // java.util.ListIterator
        public void add(Object obj) {
            throw new UnsupportedOperationException();
        }
    }
}
