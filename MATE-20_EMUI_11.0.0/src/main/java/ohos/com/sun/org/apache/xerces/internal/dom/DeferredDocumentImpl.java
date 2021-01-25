package ohos.com.sun.org.apache.xerces.internal.dom;

import java.util.ArrayList;
import java.util.HashMap;
import ohos.org.w3c.dom.DOMImplementation;
import ohos.org.w3c.dom.Element;

public class DeferredDocumentImpl extends DocumentImpl implements DeferredNode {
    protected static final int CHUNK_MASK = 255;
    protected static final int CHUNK_SHIFT = 8;
    protected static final int CHUNK_SIZE = 256;
    private static final boolean DEBUG_IDS = false;
    private static final boolean DEBUG_PRINT_REF_COUNTS = false;
    private static final boolean DEBUG_PRINT_TABLES = false;
    protected static final int INITIAL_CHUNK_COUNT = 32;
    private static final int[] INIT_ARRAY = new int[257];
    static final long serialVersionUID = 5186323580749626857L;
    private final transient StringBuilder fBufferStr;
    protected transient int fIdCount;
    protected transient int[] fIdElement;
    protected transient String[] fIdName;
    protected boolean fNamespacesEnabled;
    protected transient int fNodeCount;
    protected transient int[][] fNodeExtra;
    protected transient int[][] fNodeLastChild;
    protected transient Object[][] fNodeName;
    protected transient int[][] fNodeParent;
    protected transient int[][] fNodePrevSib;
    protected transient int[][] fNodeType;
    protected transient Object[][] fNodeURI;
    protected transient Object[][] fNodeValue;
    private final transient ArrayList fStrChunks;

    private static void print(int[] iArr, int i, int i2, int i3, int i4) {
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.dom.DeferredNode
    public int getNodeIndex() {
        return 0;
    }

    public void print() {
    }

    public DeferredDocumentImpl() {
        this(false);
    }

    public DeferredDocumentImpl(boolean z) {
        this(z, false);
    }

    public DeferredDocumentImpl(boolean z, boolean z2) {
        super(z2);
        this.fNodeCount = 0;
        this.fNamespacesEnabled = false;
        this.fBufferStr = new StringBuilder();
        this.fStrChunks = new ArrayList();
        needsSyncData(true);
        needsSyncChildren(true);
        this.fNamespacesEnabled = z;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.dom.DocumentImpl, ohos.com.sun.org.apache.xerces.internal.dom.CoreDocumentImpl
    public DOMImplementation getImplementation() {
        return DeferredDOMImplementationImpl.getDOMImplementation();
    }

    /* access modifiers changed from: package-private */
    public boolean getNamespacesEnabled() {
        return this.fNamespacesEnabled;
    }

    /* access modifiers changed from: package-private */
    public void setNamespacesEnabled(boolean z) {
        this.fNamespacesEnabled = z;
    }

    public int createDeferredDocument() {
        return createNode(9);
    }

    public int createDeferredDocumentType(String str, String str2, String str3) {
        int createNode = createNode(10);
        int i = createNode >> 8;
        int i2 = createNode & 255;
        setChunkValue(this.fNodeName, str, i, i2);
        setChunkValue(this.fNodeValue, str2, i, i2);
        setChunkValue(this.fNodeURI, str3, i, i2);
        return createNode;
    }

    public void setInternalSubset(int i, String str) {
        int createNode = createNode(10);
        setChunkIndex(this.fNodeExtra, createNode, i >> 8, i & 255);
        setChunkValue(this.fNodeValue, str, createNode >> 8, createNode & 255);
    }

    public int createDeferredNotation(String str, String str2, String str3, String str4) {
        int createNode = createNode(12);
        int i = createNode >> 8;
        int i2 = createNode & 255;
        int createNode2 = createNode(12);
        setChunkValue(this.fNodeName, str, i, i2);
        setChunkValue(this.fNodeValue, str2, i, i2);
        setChunkValue(this.fNodeURI, str3, i, i2);
        setChunkIndex(this.fNodeExtra, createNode2, i, i2);
        setChunkValue(this.fNodeName, str4, createNode2 >> 8, createNode2 & 255);
        return createNode;
    }

    public int createDeferredEntity(String str, String str2, String str3, String str4, String str5) {
        int createNode = createNode(6);
        int i = createNode >> 8;
        int i2 = createNode & 255;
        int createNode2 = createNode(6);
        int i3 = createNode2 >> 8;
        int i4 = createNode2 & 255;
        setChunkValue(this.fNodeName, str, i, i2);
        setChunkValue(this.fNodeValue, str2, i, i2);
        setChunkValue(this.fNodeURI, str3, i, i2);
        setChunkIndex(this.fNodeExtra, createNode2, i, i2);
        setChunkValue(this.fNodeName, str4, i3, i4);
        setChunkValue(this.fNodeValue, null, i3, i4);
        setChunkValue(this.fNodeURI, null, i3, i4);
        int createNode3 = createNode(6);
        setChunkIndex(this.fNodeExtra, createNode3, i3, i4);
        setChunkValue(this.fNodeName, str5, createNode3 >> 8, createNode3 & 255);
        return createNode;
    }

    public String getDeferredEntityBaseURI(int i) {
        if (i != -1) {
            return getNodeName(getNodeExtra(getNodeExtra(i, false), false), false);
        }
        return null;
    }

    public void setEntityInfo(int i, String str, String str2) {
        int nodeExtra = getNodeExtra(i, false);
        if (nodeExtra != -1) {
            int i2 = nodeExtra >> 8;
            int i3 = nodeExtra & 255;
            setChunkValue(this.fNodeValue, str, i2, i3);
            setChunkValue(this.fNodeURI, str2, i2, i3);
        }
    }

    public void setTypeInfo(int i, Object obj) {
        setChunkValue(this.fNodeValue, obj, i >> 8, i & 255);
    }

    public void setInputEncoding(int i, String str) {
        int nodeExtra = getNodeExtra(getNodeExtra(i, false), false);
        setChunkValue(this.fNodeValue, str, nodeExtra >> 8, nodeExtra & 255);
    }

    public int createDeferredEntityReference(String str, String str2) {
        int createNode = createNode(5);
        int i = createNode >> 8;
        int i2 = createNode & 255;
        setChunkValue(this.fNodeName, str, i, i2);
        setChunkValue(this.fNodeValue, str2, i, i2);
        return createNode;
    }

    public int createDeferredElement(String str, String str2, Object obj) {
        int createNode = createNode(1);
        int i = createNode >> 8;
        int i2 = createNode & 255;
        setChunkValue(this.fNodeName, str2, i, i2);
        setChunkValue(this.fNodeURI, str, i, i2);
        setChunkValue(this.fNodeValue, obj, i, i2);
        return createNode;
    }

    public int createDeferredElement(String str) {
        return createDeferredElement(null, str);
    }

    public int createDeferredElement(String str, String str2) {
        int createNode = createNode(1);
        int i = createNode >> 8;
        int i2 = createNode & 255;
        setChunkValue(this.fNodeName, str2, i, i2);
        setChunkValue(this.fNodeURI, str, i, i2);
        return createNode;
    }

    public int setDeferredAttribute(int i, String str, String str2, String str3, boolean z, boolean z2, Object obj) {
        int createDeferredAttribute = createDeferredAttribute(str, str2, str3, z);
        int i2 = createDeferredAttribute >> 8;
        int i3 = createDeferredAttribute & 255;
        setChunkIndex(this.fNodeParent, i, i2, i3);
        int i4 = i >> 8;
        int i5 = i & 255;
        int chunkIndex = getChunkIndex(this.fNodeExtra, i4, i5);
        if (chunkIndex != 0) {
            setChunkIndex(this.fNodePrevSib, chunkIndex, i2, i3);
        }
        setChunkIndex(this.fNodeExtra, createDeferredAttribute, i4, i5);
        int chunkIndex2 = getChunkIndex(this.fNodeExtra, i2, i3);
        if (z2) {
            setChunkIndex(this.fNodeExtra, chunkIndex2 | 512, i2, i3);
            putIdentifier(getChunkValue(this.fNodeValue, i2, i3), i);
        }
        if (obj != null) {
            int createNode = createNode(20);
            setChunkIndex(this.fNodeLastChild, createNode, i2, i3);
            setChunkValue(this.fNodeValue, obj, createNode >> 8, createNode & 255);
        }
        return createDeferredAttribute;
    }

    public int setDeferredAttribute(int i, String str, String str2, String str3, boolean z) {
        int createDeferredAttribute = createDeferredAttribute(str, str2, str3, z);
        int i2 = createDeferredAttribute >> 8;
        int i3 = createDeferredAttribute & 255;
        setChunkIndex(this.fNodeParent, i, i2, i3);
        int i4 = i >> 8;
        int i5 = i & 255;
        int chunkIndex = getChunkIndex(this.fNodeExtra, i4, i5);
        if (chunkIndex != 0) {
            setChunkIndex(this.fNodePrevSib, chunkIndex, i2, i3);
        }
        setChunkIndex(this.fNodeExtra, createDeferredAttribute, i4, i5);
        return createDeferredAttribute;
    }

    public int createDeferredAttribute(String str, String str2, boolean z) {
        return createDeferredAttribute(str, null, str2, z);
    }

    public int createDeferredAttribute(String str, String str2, String str3, boolean z) {
        int createNode = createNode(2);
        int i = createNode >> 8;
        int i2 = createNode & 255;
        setChunkValue(this.fNodeName, str, i, i2);
        setChunkValue(this.fNodeURI, str2, i, i2);
        setChunkValue(this.fNodeValue, str3, i, i2);
        setChunkIndex(this.fNodeExtra, z ? 32 : 0, i, i2);
        return createNode;
    }

    public int createDeferredElementDefinition(String str) {
        int createNode = createNode(21);
        setChunkValue(this.fNodeName, str, createNode >> 8, createNode & 255);
        return createNode;
    }

    public int createDeferredTextNode(String str, boolean z) {
        int createNode = createNode(3);
        int i = createNode >> 8;
        int i2 = createNode & 255;
        setChunkValue(this.fNodeValue, str, i, i2);
        setChunkIndex(this.fNodeExtra, z ? 1 : 0, i, i2);
        return createNode;
    }

    public int createDeferredCDATASection(String str) {
        int createNode = createNode(4);
        setChunkValue(this.fNodeValue, str, createNode >> 8, createNode & 255);
        return createNode;
    }

    public int createDeferredProcessingInstruction(String str, String str2) {
        int createNode = createNode(7);
        int i = createNode >> 8;
        int i2 = createNode & 255;
        setChunkValue(this.fNodeName, str, i, i2);
        setChunkValue(this.fNodeValue, str2, i, i2);
        return createNode;
    }

    public int createDeferredComment(String str) {
        int createNode = createNode(8);
        setChunkValue(this.fNodeValue, str, createNode >> 8, createNode & 255);
        return createNode;
    }

    public int cloneNode(int i, boolean z) {
        int i2 = i >> 8;
        int i3 = i & 255;
        int i4 = this.fNodeType[i2][i3];
        int createNode = createNode((short) i4);
        int i5 = createNode >> 8;
        int i6 = createNode & 255;
        Object[][] objArr = this.fNodeName;
        setChunkValue(objArr, objArr[i2][i3], i5, i6);
        Object[][] objArr2 = this.fNodeValue;
        setChunkValue(objArr2, objArr2[i2][i3], i5, i6);
        Object[][] objArr3 = this.fNodeURI;
        setChunkValue(objArr3, objArr3[i2][i3], i5, i6);
        int i7 = this.fNodeExtra[i2][i3];
        if (i7 != -1) {
            if (!(i4 == 2 || i4 == 3)) {
                i7 = cloneNode(i7, false);
            }
            setChunkIndex(this.fNodeExtra, i7, i5, i6);
        }
        if (z) {
            int lastChild = getLastChild(i, false);
            int i8 = -1;
            while (lastChild != -1) {
                int cloneNode = cloneNode(lastChild, z);
                insertBefore(createNode, cloneNode, i8);
                lastChild = getRealPrevSibling(lastChild, false);
                i8 = cloneNode;
            }
        }
        return createNode;
    }

    public void appendChild(int i, int i2) {
        int i3 = i >> 8;
        int i4 = i & 255;
        int i5 = i2 >> 8;
        int i6 = i2 & 255;
        setChunkIndex(this.fNodeParent, i, i5, i6);
        setChunkIndex(this.fNodePrevSib, getChunkIndex(this.fNodeLastChild, i3, i4), i5, i6);
        setChunkIndex(this.fNodeLastChild, i2, i3, i4);
    }

    public int setAttributeNode(int i, int i2) {
        int i3 = i >> 8;
        int i4 = i & 255;
        int i5 = i2 >> 8;
        int i6 = i2 & 255;
        String chunkValue = getChunkValue(this.fNodeName, i5, i6);
        int chunkIndex = getChunkIndex(this.fNodeExtra, i3, i4);
        int i7 = -1;
        int i8 = -1;
        int i9 = -1;
        while (chunkIndex != -1) {
            i8 = chunkIndex >> 8;
            i9 = chunkIndex & 255;
            if (getChunkValue(this.fNodeName, i8, i9).equals(chunkValue)) {
                break;
            }
            i7 = chunkIndex;
            chunkIndex = getChunkIndex(this.fNodePrevSib, i8, i9);
        }
        if (chunkIndex != -1) {
            int chunkIndex2 = getChunkIndex(this.fNodePrevSib, i8, i9);
            if (i7 == -1) {
                setChunkIndex(this.fNodeExtra, chunkIndex2, i3, i4);
            } else {
                setChunkIndex(this.fNodePrevSib, chunkIndex2, i7 >> 8, i7 & 255);
            }
            clearChunkIndex(this.fNodeType, i8, i9);
            clearChunkValue(this.fNodeName, i8, i9);
            clearChunkValue(this.fNodeValue, i8, i9);
            clearChunkIndex(this.fNodeParent, i8, i9);
            clearChunkIndex(this.fNodePrevSib, i8, i9);
            int clearChunkIndex = clearChunkIndex(this.fNodeLastChild, i8, i9);
            int i10 = clearChunkIndex >> 8;
            int i11 = clearChunkIndex & 255;
            clearChunkIndex(this.fNodeType, i10, i11);
            clearChunkValue(this.fNodeValue, i10, i11);
            clearChunkIndex(this.fNodeParent, i10, i11);
            clearChunkIndex(this.fNodeLastChild, i10, i11);
        }
        int chunkIndex3 = getChunkIndex(this.fNodeExtra, i3, i4);
        setChunkIndex(this.fNodeExtra, i2, i3, i4);
        setChunkIndex(this.fNodePrevSib, chunkIndex3, i5, i6);
        return chunkIndex;
    }

    public void setIdAttributeNode(int i, int i2) {
        int i3 = i2 >> 8;
        int i4 = i2 & 255;
        setChunkIndex(this.fNodeExtra, getChunkIndex(this.fNodeExtra, i3, i4) | 512, i3, i4);
        putIdentifier(getChunkValue(this.fNodeValue, i3, i4), i);
    }

    public void setIdAttribute(int i) {
        int i2 = i >> 8;
        int i3 = i & 255;
        setChunkIndex(this.fNodeExtra, getChunkIndex(this.fNodeExtra, i2, i3) | 512, i2, i3);
    }

    public int insertBefore(int i, int i2, int i3) {
        if (i3 == -1) {
            appendChild(i, i2);
            return i2;
        }
        int i4 = i3 >> 8;
        int i5 = i3 & 255;
        int chunkIndex = getChunkIndex(this.fNodePrevSib, i4, i5);
        setChunkIndex(this.fNodePrevSib, i2, i4, i5);
        setChunkIndex(this.fNodePrevSib, chunkIndex, i2 >> 8, i2 & 255);
        return i2;
    }

    public void setAsLastChild(int i, int i2) {
        setChunkIndex(this.fNodeLastChild, i2, i >> 8, i & 255);
    }

    public int getParentNode(int i) {
        return getParentNode(i, false);
    }

    public int getParentNode(int i, boolean z) {
        if (i == -1) {
            return -1;
        }
        int i2 = i >> 8;
        int i3 = i & 255;
        if (z) {
            return clearChunkIndex(this.fNodeParent, i2, i3);
        }
        return getChunkIndex(this.fNodeParent, i2, i3);
    }

    public int getLastChild(int i) {
        return getLastChild(i, true);
    }

    public int getLastChild(int i, boolean z) {
        if (i == -1) {
            return -1;
        }
        int i2 = i >> 8;
        int i3 = i & 255;
        if (z) {
            return clearChunkIndex(this.fNodeLastChild, i2, i3);
        }
        return getChunkIndex(this.fNodeLastChild, i2, i3);
    }

    public int getPrevSibling(int i) {
        return getPrevSibling(i, true);
    }

    public int getPrevSibling(int i, boolean z) {
        if (i == -1) {
            return -1;
        }
        int i2 = i >> 8;
        int i3 = i & 255;
        if (getChunkIndex(this.fNodeType, i2, i3) != 3) {
            return getChunkIndex(this.fNodePrevSib, i2, i3);
        }
        while (true) {
            int chunkIndex = getChunkIndex(this.fNodePrevSib, i2, i3);
            if (chunkIndex == -1) {
                return chunkIndex;
            }
            i2 = chunkIndex >> 8;
            int i4 = chunkIndex & 255;
            if (getChunkIndex(this.fNodeType, i2, i4) != 3) {
                return chunkIndex;
            }
            i3 = i4;
        }
    }

    public int getRealPrevSibling(int i) {
        return getRealPrevSibling(i, true);
    }

    public int getRealPrevSibling(int i, boolean z) {
        if (i == -1) {
            return -1;
        }
        int i2 = i >> 8;
        int i3 = i & 255;
        if (z) {
            return clearChunkIndex(this.fNodePrevSib, i2, i3);
        }
        return getChunkIndex(this.fNodePrevSib, i2, i3);
    }

    public int lookupElementDefinition(String str) {
        if (this.fNodeCount > 1) {
            int chunkIndex = getChunkIndex(this.fNodeLastChild, 0, 0);
            while (true) {
                if (chunkIndex == -1) {
                    chunkIndex = -1;
                    break;
                }
                int i = chunkIndex >> 8;
                int i2 = chunkIndex & 255;
                if (getChunkIndex(this.fNodeType, i, i2) == 10) {
                    break;
                }
                chunkIndex = getChunkIndex(this.fNodePrevSib, i, i2);
            }
            if (chunkIndex == -1) {
                return -1;
            }
            int chunkIndex2 = getChunkIndex(this.fNodeLastChild, chunkIndex >> 8, chunkIndex & 255);
            while (chunkIndex2 != -1) {
                int i3 = chunkIndex2 >> 8;
                int i4 = chunkIndex2 & 255;
                if (getChunkIndex(this.fNodeType, i3, i4) == 21 && getChunkValue(this.fNodeName, i3, i4) == str) {
                    return chunkIndex2;
                }
                chunkIndex2 = getChunkIndex(this.fNodePrevSib, i3, i4);
            }
        }
        return -1;
    }

    public DeferredNode getNodeObject(int i) {
        DeferredNode deferredNode;
        if (i == -1) {
            return null;
        }
        int i2 = i >> 8;
        int i3 = i & 255;
        int chunkIndex = getChunkIndex(this.fNodeType, i2, i3);
        if (!(chunkIndex == 3 || chunkIndex == 4)) {
            clearChunkIndex(this.fNodeType, i2, i3);
        }
        if (chunkIndex == 12) {
            return new DeferredNotationImpl(this, i);
        }
        if (chunkIndex == 21) {
            return new DeferredElementDefinitionImpl(this, i);
        }
        switch (chunkIndex) {
            case 1:
                if (this.fNamespacesEnabled) {
                    deferredNode = new DeferredElementNSImpl(this, i);
                } else {
                    deferredNode = new DeferredElementImpl(this, i);
                }
                int[] iArr = this.fIdElement;
                if (iArr != null) {
                    int binarySearch = binarySearch(iArr, 0, this.fIdCount - 1, i);
                    while (binarySearch != -1) {
                        String str = this.fIdName[binarySearch];
                        if (str != null) {
                            putIdentifier0(str, (Element) deferredNode);
                            this.fIdName[binarySearch] = null;
                        }
                        binarySearch++;
                        if (binarySearch >= this.fIdCount || this.fIdElement[binarySearch] != i) {
                            binarySearch = -1;
                        }
                    }
                }
                return deferredNode;
            case 2:
                if (this.fNamespacesEnabled) {
                    return new DeferredAttrNSImpl(this, i);
                }
                return new DeferredAttrImpl(this, i);
            case 3:
                return new DeferredTextImpl(this, i);
            case 4:
                return new DeferredCDATASectionImpl(this, i);
            case 5:
                return new DeferredEntityReferenceImpl(this, i);
            case 6:
                return new DeferredEntityImpl(this, i);
            case 7:
                return new DeferredProcessingInstructionImpl(this, i);
            case 8:
                return new DeferredCommentImpl(this, i);
            case 9:
                return this;
            case 10:
                DeferredDocumentTypeImpl deferredDocumentTypeImpl = new DeferredDocumentTypeImpl(this, i);
                this.docType = deferredDocumentTypeImpl;
                return deferredDocumentTypeImpl;
            default:
                throw new IllegalArgumentException("type: " + chunkIndex);
        }
    }

    public String getNodeName(int i) {
        return getNodeName(i, true);
    }

    public String getNodeName(int i, boolean z) {
        if (i == -1) {
            return null;
        }
        int i2 = i >> 8;
        int i3 = i & 255;
        if (z) {
            return clearChunkValue(this.fNodeName, i2, i3);
        }
        return getChunkValue(this.fNodeName, i2, i3);
    }

    public String getNodeValueString(int i) {
        return getNodeValueString(i, true);
    }

    public String getNodeValueString(int i, boolean z) {
        String str;
        int lastChild;
        if (i == -1) {
            return null;
        }
        int i2 = i >> 8;
        int i3 = i & 255;
        if (z) {
            str = clearChunkValue(this.fNodeValue, i2, i3);
        } else {
            str = getChunkValue(this.fNodeValue, i2, i3);
        }
        if (str == null) {
            return null;
        }
        int chunkIndex = getChunkIndex(this.fNodeType, i2, i3);
        if (chunkIndex == 3) {
            int realPrevSibling = getRealPrevSibling(i);
            if (realPrevSibling != -1 && getNodeType(realPrevSibling, false) == 3) {
                this.fStrChunks.add(str);
                do {
                    int i4 = realPrevSibling >> 8;
                    int i5 = realPrevSibling & 255;
                    this.fStrChunks.add(getChunkValue(this.fNodeValue, i4, i5));
                    realPrevSibling = getChunkIndex(this.fNodePrevSib, i4, i5);
                    if (realPrevSibling == -1) {
                        break;
                    }
                } while (getNodeType(realPrevSibling, false) == 3);
                for (int size = this.fStrChunks.size() - 1; size >= 0; size--) {
                    this.fBufferStr.append((String) this.fStrChunks.get(size));
                }
                String sb = this.fBufferStr.toString();
                this.fStrChunks.clear();
                this.fBufferStr.setLength(0);
                return sb;
            }
        } else if (chunkIndex == 4 && (lastChild = getLastChild(i, false)) != -1) {
            this.fBufferStr.append(str);
            while (lastChild != -1) {
                int i6 = lastChild >> 8;
                int i7 = lastChild & 255;
                this.fStrChunks.add(getChunkValue(this.fNodeValue, i6, i7));
                lastChild = getChunkIndex(this.fNodePrevSib, i6, i7);
            }
            for (int size2 = this.fStrChunks.size() - 1; size2 >= 0; size2--) {
                this.fBufferStr.append((String) this.fStrChunks.get(size2));
            }
            String sb2 = this.fBufferStr.toString();
            this.fStrChunks.clear();
            this.fBufferStr.setLength(0);
            return sb2;
        }
        return str;
    }

    public String getNodeValue(int i) {
        return getNodeValue(i, true);
    }

    public Object getTypeInfo(int i) {
        if (i == -1) {
            return null;
        }
        int i2 = i >> 8;
        int i3 = i & 255;
        Object[][] objArr = this.fNodeValue;
        Object obj = objArr[i2] != null ? objArr[i2][i3] : null;
        if (obj != null) {
            Object[][] objArr2 = this.fNodeValue;
            objArr2[i2][i3] = null;
            RefCount refCount = (RefCount) objArr2[i2][256];
            refCount.fCount--;
            if (refCount.fCount == 0) {
                this.fNodeValue[i2] = null;
            }
        }
        return obj;
    }

    public String getNodeValue(int i, boolean z) {
        if (i == -1) {
            return null;
        }
        int i2 = i >> 8;
        int i3 = i & 255;
        if (z) {
            return clearChunkValue(this.fNodeValue, i2, i3);
        }
        return getChunkValue(this.fNodeValue, i2, i3);
    }

    public int getNodeExtra(int i) {
        return getNodeExtra(i, true);
    }

    public int getNodeExtra(int i, boolean z) {
        if (i == -1) {
            return -1;
        }
        int i2 = i >> 8;
        int i3 = i & 255;
        if (z) {
            return clearChunkIndex(this.fNodeExtra, i2, i3);
        }
        return getChunkIndex(this.fNodeExtra, i2, i3);
    }

    public short getNodeType(int i) {
        return getNodeType(i, true);
    }

    public short getNodeType(int i, boolean z) {
        int i2;
        if (i == -1) {
            return -1;
        }
        int i3 = i >> 8;
        int i4 = i & 255;
        if (z) {
            i2 = clearChunkIndex(this.fNodeType, i3, i4);
        } else {
            i2 = getChunkIndex(this.fNodeType, i3, i4);
        }
        return (short) i2;
    }

    public String getAttribute(int i, String str) {
        if (!(i == -1 || str == null)) {
            int chunkIndex = getChunkIndex(this.fNodeExtra, i >> 8, i & 255);
            while (chunkIndex != -1) {
                int i2 = chunkIndex >> 8;
                int i3 = chunkIndex & 255;
                if (getChunkValue(this.fNodeName, i2, i3) == str) {
                    return getChunkValue(this.fNodeValue, i2, i3);
                }
                chunkIndex = getChunkIndex(this.fNodePrevSib, i2, i3);
            }
        }
        return null;
    }

    public String getNodeURI(int i) {
        return getNodeURI(i, true);
    }

    public String getNodeURI(int i, boolean z) {
        if (i == -1) {
            return null;
        }
        int i2 = i >> 8;
        int i3 = i & 255;
        if (z) {
            return clearChunkValue(this.fNodeURI, i2, i3);
        }
        return getChunkValue(this.fNodeURI, i2, i3);
    }

    public void putIdentifier(String str, int i) {
        if (this.fIdName == null) {
            this.fIdName = new String[64];
            this.fIdElement = new int[64];
        }
        int i2 = this.fIdCount;
        String[] strArr = this.fIdName;
        if (i2 == strArr.length) {
            String[] strArr2 = new String[(i2 * 2)];
            System.arraycopy(strArr, 0, strArr2, 0, i2);
            this.fIdName = strArr2;
            int[] iArr = new int[strArr2.length];
            System.arraycopy(this.fIdElement, 0, iArr, 0, this.fIdCount);
            this.fIdElement = iArr;
        }
        String[] strArr3 = this.fIdName;
        int i3 = this.fIdCount;
        strArr3[i3] = str;
        this.fIdElement[i3] = i;
        this.fIdCount = i3 + 1;
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xerces.internal.dom.NodeImpl
    public void synchronizeData() {
        int i = 0;
        needsSyncData(false);
        if (this.fIdElement != null) {
            IntVector intVector = new IntVector();
            while (i < this.fIdCount) {
                int i2 = this.fIdElement[i];
                String str = this.fIdName[i];
                if (str != null) {
                    intVector.removeAllElements();
                    int i3 = i2;
                    do {
                        intVector.addElement(i3);
                        i3 = getChunkIndex(this.fNodeParent, i3 >> 8, i3 & 255);
                    } while (i3 != -1);
                    DeferredNode deferredNode = this;
                    for (int size = intVector.size() - 2; size >= 0; size--) {
                        int elementAt = intVector.elementAt(size);
                        DeferredNode lastChild = deferredNode.getLastChild();
                        while (true) {
                            if (lastChild == null) {
                                break;
                            }
                            if ((lastChild instanceof DeferredNode) && lastChild.getNodeIndex() == elementAt) {
                                deferredNode = lastChild;
                                break;
                            }
                            lastChild = lastChild.getPreviousSibling();
                        }
                    }
                    Element element = (Element) deferredNode;
                    putIdentifier0(str, element);
                    this.fIdName[i] = null;
                    while (true) {
                        int i4 = i + 1;
                        if (i4 >= this.fIdCount || this.fIdElement[i4] != i2) {
                            break;
                        }
                        String str2 = this.fIdName[i4];
                        if (str2 != null) {
                            putIdentifier0(str2, element);
                        }
                        i = i4;
                    }
                }
                i++;
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xerces.internal.dom.ParentNode
    public void synchronizeChildren() {
        if (needsSyncData()) {
            synchronizeData();
            if (!needsSyncChildren()) {
                return;
            }
        }
        boolean z = this.mutationEvents;
        this.mutationEvents = false;
        needsSyncChildren(false);
        getNodeType(0);
        int lastChild = getLastChild(0);
        ChildNode childNode = null;
        ChildNode childNode2 = null;
        while (lastChild != -1) {
            ChildNode childNode3 = (ChildNode) getNodeObject(lastChild);
            if (childNode2 == null) {
                childNode2 = childNode3;
            } else {
                childNode.previousSibling = childNode3;
            }
            childNode3.ownerNode = this;
            childNode3.isOwned(true);
            childNode3.nextSibling = childNode;
            short nodeType = childNode3.getNodeType();
            if (nodeType == 1) {
                this.docElement = (ElementImpl) childNode3;
            } else if (nodeType == 10) {
                this.docType = (DocumentTypeImpl) childNode3;
            }
            lastChild = getPrevSibling(lastChild);
            childNode = childNode3;
        }
        if (childNode != null) {
            this.firstChild = childNode;
            childNode.isFirstChild(true);
            lastChild(childNode2);
        }
        this.mutationEvents = z;
    }

    /* access modifiers changed from: protected */
    public final void synchronizeChildren(AttrImpl attrImpl, int i) {
        boolean mutationEvents = getMutationEvents();
        setMutationEvents(false);
        attrImpl.needsSyncChildren(false);
        int lastChild = getLastChild(i);
        if (getPrevSibling(lastChild) == -1) {
            attrImpl.value = getNodeValueString(i);
            attrImpl.hasStringValue(true);
        } else {
            ChildNode childNode = null;
            ChildNode childNode2 = null;
            while (lastChild != -1) {
                ChildNode childNode3 = (ChildNode) getNodeObject(lastChild);
                if (childNode == null) {
                    childNode = childNode3;
                } else {
                    childNode2.previousSibling = childNode3;
                }
                childNode3.ownerNode = attrImpl;
                childNode3.isOwned(true);
                childNode3.nextSibling = childNode2;
                lastChild = getPrevSibling(lastChild);
                childNode2 = childNode3;
            }
            if (childNode != null) {
                attrImpl.value = childNode2;
                childNode2.isFirstChild(true);
                attrImpl.lastChild(childNode);
            }
            attrImpl.hasStringValue(false);
        }
        setMutationEvents(mutationEvents);
    }

    /* access modifiers changed from: protected */
    public final void synchronizeChildren(ParentNode parentNode, int i) {
        boolean mutationEvents = getMutationEvents();
        setMutationEvents(false);
        parentNode.needsSyncChildren(false);
        int lastChild = getLastChild(i);
        ChildNode childNode = null;
        ChildNode childNode2 = null;
        while (lastChild != -1) {
            ChildNode childNode3 = (ChildNode) getNodeObject(lastChild);
            if (childNode == null) {
                childNode = childNode3;
            } else {
                childNode2.previousSibling = childNode3;
            }
            childNode3.ownerNode = parentNode;
            childNode3.isOwned(true);
            childNode3.nextSibling = childNode2;
            lastChild = getPrevSibling(lastChild);
            childNode2 = childNode3;
        }
        if (childNode != null) {
            parentNode.firstChild = childNode2;
            childNode2.isFirstChild(true);
            parentNode.lastChild(childNode);
        }
        setMutationEvents(mutationEvents);
    }

    /* access modifiers changed from: protected */
    public void ensureCapacity(int i) {
        int[][] iArr = this.fNodeType;
        if (iArr == null) {
            this.fNodeType = new int[32][];
            this.fNodeName = new Object[32][];
            this.fNodeValue = new Object[32][];
            this.fNodeParent = new int[32][];
            this.fNodeLastChild = new int[32][];
            this.fNodePrevSib = new int[32][];
            this.fNodeURI = new Object[32][];
            this.fNodeExtra = new int[32][];
        } else if (iArr.length <= i) {
            int i2 = i * 2;
            int[][] iArr2 = new int[i2][];
            System.arraycopy(iArr, 0, iArr2, 0, i);
            this.fNodeType = iArr2;
            Object[][] objArr = new Object[i2][];
            System.arraycopy(this.fNodeName, 0, objArr, 0, i);
            this.fNodeName = objArr;
            Object[][] objArr2 = new Object[i2][];
            System.arraycopy(this.fNodeValue, 0, objArr2, 0, i);
            this.fNodeValue = objArr2;
            int[][] iArr3 = new int[i2][];
            System.arraycopy(this.fNodeParent, 0, iArr3, 0, i);
            this.fNodeParent = iArr3;
            int[][] iArr4 = new int[i2][];
            System.arraycopy(this.fNodeLastChild, 0, iArr4, 0, i);
            this.fNodeLastChild = iArr4;
            int[][] iArr5 = new int[i2][];
            System.arraycopy(this.fNodePrevSib, 0, iArr5, 0, i);
            this.fNodePrevSib = iArr5;
            Object[][] objArr3 = new Object[i2][];
            System.arraycopy(this.fNodeURI, 0, objArr3, 0, i);
            this.fNodeURI = objArr3;
            int[][] iArr6 = new int[i2][];
            System.arraycopy(this.fNodeExtra, 0, iArr6, 0, i);
            this.fNodeExtra = iArr6;
        } else if (iArr[i] != null) {
            return;
        }
        createChunk(this.fNodeType, i);
        createChunk(this.fNodeName, i);
        createChunk(this.fNodeValue, i);
        createChunk(this.fNodeParent, i);
        createChunk(this.fNodeLastChild, i);
        createChunk(this.fNodePrevSib, i);
        createChunk(this.fNodeURI, i);
        createChunk(this.fNodeExtra, i);
    }

    /* access modifiers changed from: protected */
    public int createNode(short s) {
        int i = this.fNodeCount;
        int i2 = i >> 8;
        ensureCapacity(i2);
        setChunkIndex(this.fNodeType, s, i2, i & 255);
        int i3 = this.fNodeCount;
        this.fNodeCount = i3 + 1;
        return i3;
    }

    protected static int binarySearch(int[] iArr, int i, int i2, int i3) {
        while (i <= i2) {
            int i4 = (i + i2) >>> 1;
            int i5 = iArr[i4];
            if (i5 == i3) {
                while (i4 > 0 && iArr[i4 - 1] == i3) {
                    i4--;
                }
                return i4;
            } else if (i5 > i3) {
                i2 = i4 - 1;
            } else {
                i = i4 + 1;
            }
        }
        return -1;
    }

    static {
        for (int i = 0; i < 256; i++) {
            INIT_ARRAY[i] = -1;
        }
    }

    private final void createChunk(int[][] iArr, int i) {
        iArr[i] = new int[257];
        System.arraycopy(INIT_ARRAY, 0, iArr[i], 0, 256);
    }

    /* access modifiers changed from: package-private */
    public static final class RefCount {
        int fCount;

        RefCount() {
        }
    }

    private final void createChunk(Object[][] objArr, int i) {
        objArr[i] = new Object[257];
        objArr[i][256] = new RefCount();
    }

    private final int setChunkIndex(int[][] iArr, int i, int i2, int i3) {
        if (i == -1) {
            return clearChunkIndex(iArr, i2, i3);
        }
        int[] iArr2 = iArr[i2];
        if (iArr2 == null) {
            createChunk(iArr, i2);
            iArr2 = iArr[i2];
        }
        int i4 = iArr2[i3];
        if (i4 == -1) {
            iArr2[256] = iArr2[256] + 1;
        }
        iArr2[i3] = i;
        return i4;
    }

    private final String setChunkValue(Object[][] objArr, Object obj, int i, int i2) {
        if (obj == null) {
            return clearChunkValue(objArr, i, i2);
        }
        Object[] objArr2 = objArr[i];
        if (objArr2 == null) {
            createChunk(objArr, i);
            objArr2 = objArr[i];
        }
        String str = (String) objArr2[i2];
        if (str == null) {
            ((RefCount) objArr2[256]).fCount++;
        }
        objArr2[i2] = obj;
        return str;
    }

    private final int getChunkIndex(int[][] iArr, int i, int i2) {
        if (iArr[i] != null) {
            return iArr[i][i2];
        }
        return -1;
    }

    private final String getChunkValue(Object[][] objArr, int i, int i2) {
        if (objArr[i] != null) {
            return (String) objArr[i][i2];
        }
        return null;
    }

    private final String getNodeValue(int i, int i2) {
        Object obj = this.fNodeValue[i][i2];
        if (obj == null) {
            return null;
        }
        if (obj instanceof String) {
            return (String) obj;
        }
        return obj.toString();
    }

    private final int clearChunkIndex(int[][] iArr, int i, int i2) {
        int i3 = iArr[i] != null ? iArr[i][i2] : -1;
        if (i3 != -1) {
            int[] iArr2 = iArr[i];
            iArr2[256] = iArr2[256] - 1;
            iArr[i][i2] = -1;
            if (iArr[i][256] == 0) {
                iArr[i] = null;
            }
        }
        return i3;
    }

    private final String clearChunkValue(Object[][] objArr, int i, int i2) {
        String str = objArr[i] != null ? (String) objArr[i][i2] : null;
        if (str != null) {
            objArr[i][i2] = null;
            RefCount refCount = (RefCount) objArr[i][256];
            refCount.fCount--;
            if (refCount.fCount == 0) {
                objArr[i] = null;
            }
        }
        return str;
    }

    private final void putIdentifier0(String str, Element element) {
        if (this.identifiers == null) {
            this.identifiers = new HashMap();
        }
        this.identifiers.put(str, element);
    }

    /* access modifiers changed from: package-private */
    public static final class IntVector {
        private int[] data;
        private int size;

        IntVector() {
        }

        public int size() {
            return this.size;
        }

        public int elementAt(int i) {
            return this.data[i];
        }

        public void addElement(int i) {
            ensureCapacity(this.size + 1);
            int[] iArr = this.data;
            int i2 = this.size;
            this.size = i2 + 1;
            iArr[i2] = i;
        }

        public void removeAllElements() {
            this.size = 0;
        }

        private void ensureCapacity(int i) {
            int[] iArr = this.data;
            if (iArr == null) {
                this.data = new int[(i + 15)];
            } else if (i > iArr.length) {
                int[] iArr2 = new int[(i + 15)];
                System.arraycopy(iArr, 0, iArr2, 0, iArr.length);
                this.data = iArr2;
            }
        }
    }
}
