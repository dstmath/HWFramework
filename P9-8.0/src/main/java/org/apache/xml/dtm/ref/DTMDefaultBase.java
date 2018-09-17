package org.apache.xml.dtm.ref;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Vector;
import javax.xml.transform.Source;
import org.apache.xml.dtm.DTM;
import org.apache.xml.dtm.DTMAxisTraverser;
import org.apache.xml.dtm.DTMException;
import org.apache.xml.dtm.DTMFilter;
import org.apache.xml.dtm.DTMManager;
import org.apache.xml.dtm.DTMWSFilter;
import org.apache.xml.res.XMLErrorResources;
import org.apache.xml.res.XMLMessages;
import org.apache.xml.utils.BoolStack;
import org.apache.xml.utils.SuballocatedIntVector;
import org.apache.xml.utils.XMLString;
import org.apache.xml.utils.XMLStringFactory;
import org.w3c.dom.Node;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

public abstract class DTMDefaultBase implements DTM {
    public static final int DEFAULT_BLOCKSIZE = 512;
    public static final int DEFAULT_NUMBLOCKS = 32;
    public static final int DEFAULT_NUMBLOCKS_SMALL = 4;
    static final boolean JJK_DEBUG = false;
    protected static final int NOTPROCESSED = -2;
    public static final int ROOTNODE = 0;
    protected String m_documentBaseURI;
    protected SuballocatedIntVector m_dtmIdent;
    protected int[][][] m_elemIndexes;
    protected ExpandedNameTable m_expandedNameTable;
    protected SuballocatedIntVector m_exptype;
    protected SuballocatedIntVector m_firstch;
    protected boolean m_indexing;
    public DTMManager m_mgr;
    protected DTMManagerDefault m_mgrDefault;
    protected SuballocatedIntVector m_namespaceDeclSetElements;
    protected Vector m_namespaceDeclSets;
    private Vector m_namespaceLists;
    protected SuballocatedIntVector m_nextsib;
    protected SuballocatedIntVector m_parent;
    protected SuballocatedIntVector m_prevsib;
    protected boolean m_shouldStripWS;
    protected BoolStack m_shouldStripWhitespaceStack;
    protected int m_size;
    protected DTMAxisTraverser[] m_traversers;
    protected DTMWSFilter m_wsfilter;
    protected XMLStringFactory m_xstrf;

    public abstract void dispatchCharactersEvents(int i, ContentHandler contentHandler, boolean z) throws SAXException;

    public abstract void dispatchToEvents(int i, ContentHandler contentHandler) throws SAXException;

    public abstract int getAttributeNode(int i, String str, String str2);

    public abstract String getDocumentTypeDeclarationPublicIdentifier();

    public abstract String getDocumentTypeDeclarationSystemIdentifier();

    public abstract int getElementById(String str);

    public abstract String getLocalName(int i);

    public abstract String getNamespaceURI(int i);

    protected abstract int getNextNodeIdentity(int i);

    public abstract String getNodeName(int i);

    public abstract String getNodeValue(int i);

    protected abstract int getNumberOfNodes();

    public abstract String getPrefix(int i);

    public abstract XMLString getStringValue(int i);

    public abstract String getUnparsedEntityURI(String str);

    public abstract boolean isAttributeSpecified(int i);

    protected abstract boolean nextNode();

    public DTMDefaultBase(DTMManager mgr, Source source, int dtmIdentity, DTMWSFilter whiteSpaceFilter, XMLStringFactory xstringfactory, boolean doIndexing) {
        this(mgr, source, dtmIdentity, whiteSpaceFilter, xstringfactory, doIndexing, 512, true, false);
    }

    public DTMDefaultBase(DTMManager mgr, Source source, int dtmIdentity, DTMWSFilter whiteSpaceFilter, XMLStringFactory xstringfactory, boolean doIndexing, int blocksize, boolean usePrevsib, boolean newNameTable) {
        int numblocks;
        String str = null;
        this.m_size = 0;
        this.m_namespaceDeclSets = null;
        this.m_namespaceDeclSetElements = null;
        this.m_mgrDefault = null;
        this.m_shouldStripWS = false;
        this.m_namespaceLists = null;
        if (blocksize <= 64) {
            numblocks = 4;
            this.m_dtmIdent = new SuballocatedIntVector(4, 1);
        } else {
            numblocks = 32;
            this.m_dtmIdent = new SuballocatedIntVector(32);
        }
        this.m_exptype = new SuballocatedIntVector(blocksize, numblocks);
        this.m_firstch = new SuballocatedIntVector(blocksize, numblocks);
        this.m_nextsib = new SuballocatedIntVector(blocksize, numblocks);
        this.m_parent = new SuballocatedIntVector(blocksize, numblocks);
        if (usePrevsib) {
            this.m_prevsib = new SuballocatedIntVector(blocksize, numblocks);
        }
        this.m_mgr = mgr;
        if (mgr instanceof DTMManagerDefault) {
            this.m_mgrDefault = (DTMManagerDefault) mgr;
        }
        if (source != null) {
            str = source.getSystemId();
        }
        this.m_documentBaseURI = str;
        this.m_dtmIdent.setElementAt(dtmIdentity, 0);
        this.m_wsfilter = whiteSpaceFilter;
        this.m_xstrf = xstringfactory;
        this.m_indexing = doIndexing;
        if (doIndexing) {
            this.m_expandedNameTable = new ExpandedNameTable();
        } else {
            this.m_expandedNameTable = this.m_mgrDefault.getExpandedNameTable(this);
        }
        if (whiteSpaceFilter != null) {
            this.m_shouldStripWhitespaceStack = new BoolStack();
            pushShouldStripWhitespace(false);
        }
    }

    protected void ensureSizeOfIndex(int namespaceID, int LocalNameID) {
        if (this.m_elemIndexes == null) {
            this.m_elemIndexes = new int[(namespaceID + 20)][][];
        } else if (this.m_elemIndexes.length <= namespaceID) {
            int[][][] indexes = this.m_elemIndexes;
            this.m_elemIndexes = new int[(namespaceID + 20)][][];
            System.arraycopy(indexes, 0, this.m_elemIndexes, 0, indexes.length);
        }
        int[][] localNameIndex = this.m_elemIndexes[namespaceID];
        if (localNameIndex == null) {
            localNameIndex = new int[(LocalNameID + 100)][];
            this.m_elemIndexes[namespaceID] = localNameIndex;
        } else if (localNameIndex.length <= LocalNameID) {
            int[][] indexes2 = localNameIndex;
            localNameIndex = new int[(LocalNameID + 100)][];
            System.arraycopy(indexes2, 0, localNameIndex, 0, indexes2.length);
            this.m_elemIndexes[namespaceID] = localNameIndex;
        }
        int[] elemHandles = localNameIndex[LocalNameID];
        if (elemHandles == null) {
            elemHandles = new int[128];
            localNameIndex[LocalNameID] = elemHandles;
            elemHandles[0] = 1;
        } else if (elemHandles.length <= elemHandles[0] + 1) {
            int[] indexes3 = elemHandles;
            elemHandles = new int[(elemHandles[0] + 1024)];
            System.arraycopy(indexes3, 0, elemHandles, 0, indexes3.length);
            localNameIndex[LocalNameID] = elemHandles;
        }
    }

    protected void indexNode(int expandedTypeID, int identity) {
        ExpandedNameTable ent = this.m_expandedNameTable;
        if ((short) 1 == ent.getType(expandedTypeID)) {
            int namespaceID = ent.getNamespaceID(expandedTypeID);
            int localNameID = ent.getLocalNameID(expandedTypeID);
            ensureSizeOfIndex(namespaceID, localNameID);
            int[] index = this.m_elemIndexes[namespaceID][localNameID];
            index[index[0]] = identity;
            index[0] = index[0] + 1;
        }
    }

    protected int findGTE(int[] list, int start, int len, int value) {
        int low = start;
        int high = start + (len - 1);
        int end = high;
        while (low <= high) {
            int mid = (low + high) / 2;
            int c = list[mid];
            if (c > value) {
                high = mid - 1;
            } else if (c >= value) {
                return mid;
            } else {
                low = mid + 1;
            }
        }
        if (low > end || list[low] <= value) {
            low = -1;
        }
        return low;
    }

    int findElementFromIndex(int nsIndex, int lnIndex, int firstPotential) {
        int[][][] indexes = this.m_elemIndexes;
        if (indexes != null && nsIndex < indexes.length) {
            int[][] lnIndexs = indexes[nsIndex];
            if (lnIndexs != null && lnIndex < lnIndexs.length) {
                int[] elems = lnIndexs[lnIndex];
                if (elems != null) {
                    int pos = findGTE(elems, 1, elems[0], firstPotential);
                    if (pos > -1) {
                        return elems[pos];
                    }
                }
            }
        }
        return -2;
    }

    protected short _type(int identity) {
        int info = _exptype(identity);
        if (-1 != info) {
            return this.m_expandedNameTable.getType(info);
        }
        return (short) -1;
    }

    protected int _exptype(int identity) {
        if (identity == -1) {
            return -1;
        }
        while (identity >= this.m_size) {
            if (!nextNode() && identity >= this.m_size) {
                return -1;
            }
        }
        return this.m_exptype.elementAt(identity);
    }

    protected int _level(int identity) {
        while (identity >= this.m_size) {
            if (!nextNode() && identity >= this.m_size) {
                return -1;
            }
        }
        int i = 0;
        while (true) {
            identity = _parent(identity);
            if (-1 == identity) {
                return i;
            }
            i++;
        }
    }

    protected int _firstch(int identity) {
        int info = identity >= this.m_size ? -2 : this.m_firstch.elementAt(identity);
        while (info == -2) {
            boolean isMore = nextNode();
            if (identity >= this.m_size && (isMore ^ 1) != 0) {
                return -1;
            }
            info = this.m_firstch.elementAt(identity);
            if (info == -2 && (isMore ^ 1) != 0) {
                return -1;
            }
        }
        return info;
    }

    protected int _nextsib(int identity) {
        int info = identity >= this.m_size ? -2 : this.m_nextsib.elementAt(identity);
        while (info == -2) {
            boolean isMore = nextNode();
            if (identity >= this.m_size && (isMore ^ 1) != 0) {
                return -1;
            }
            info = this.m_nextsib.elementAt(identity);
            if (info == -2 && (isMore ^ 1) != 0) {
                return -1;
            }
        }
        return info;
    }

    protected int _prevsib(int identity) {
        if (identity < this.m_size) {
            return this.m_prevsib.elementAt(identity);
        }
        do {
            boolean isMore = nextNode();
            if (identity >= this.m_size && (isMore ^ 1) != 0) {
                return -1;
            }
        } while (identity >= this.m_size);
        return this.m_prevsib.elementAt(identity);
    }

    protected int _parent(int identity) {
        if (identity < this.m_size) {
            return this.m_parent.elementAt(identity);
        }
        do {
            boolean isMore = nextNode();
            if (identity >= this.m_size && (isMore ^ 1) != 0) {
                return -1;
            }
        } while (identity >= this.m_size);
        return this.m_parent.elementAt(identity);
    }

    public void dumpDTM(OutputStream os) {
        if (os == null) {
            try {
                File f = new File("DTMDump" + hashCode() + ".txt");
                System.err.println("Dumping... " + f.getAbsolutePath());
                os = new FileOutputStream(f);
            } catch (IOException ioe) {
                ioe.printStackTrace(System.err);
                throw new RuntimeException(ioe.getMessage());
            }
        }
        PrintStream ps = new PrintStream(os);
        do {
        } while (nextNode());
        int nRecords = this.m_size;
        ps.println("Total nodes: " + nRecords);
        for (int index = 0; index < nRecords; index++) {
            String typestring;
            int i = makeNodeHandle(index);
            ps.println("=========== index=" + index + " handle=" + i + " ===========");
            ps.println("NodeName: " + getNodeName(i));
            ps.println("NodeNameX: " + getNodeNameX(i));
            ps.println("LocalName: " + getLocalName(i));
            ps.println("NamespaceURI: " + getNamespaceURI(i));
            ps.println("Prefix: " + getPrefix(i));
            ps.println("Expanded Type ID: " + Integer.toHexString(_exptype(index)));
            switch (_type(index)) {
                case -1:
                    typestring = "NULL";
                    break;
                case 1:
                    typestring = "ELEMENT_NODE";
                    break;
                case 2:
                    typestring = "ATTRIBUTE_NODE";
                    break;
                case 3:
                    typestring = "TEXT_NODE";
                    break;
                case 4:
                    typestring = "CDATA_SECTION_NODE";
                    break;
                case 5:
                    typestring = "ENTITY_REFERENCE_NODE";
                    break;
                case 6:
                    typestring = "ENTITY_NODE";
                    break;
                case 7:
                    typestring = "PROCESSING_INSTRUCTION_NODE";
                    break;
                case 8:
                    typestring = "COMMENT_NODE";
                    break;
                case 9:
                    typestring = "DOCUMENT_NODE";
                    break;
                case 10:
                    typestring = "DOCUMENT_NODE";
                    break;
                case 11:
                    typestring = "DOCUMENT_FRAGMENT_NODE";
                    break;
                case 12:
                    typestring = "NOTATION_NODE";
                    break;
                case 13:
                    typestring = "NAMESPACE_NODE";
                    break;
                default:
                    typestring = "Unknown!";
                    break;
            }
            ps.println("Type: " + typestring);
            int firstChild = _firstch(index);
            if (-1 == firstChild) {
                ps.println("First child: DTM.NULL");
            } else if (-2 == firstChild) {
                ps.println("First child: NOTPROCESSED");
            } else {
                ps.println("First child: " + firstChild);
            }
            if (this.m_prevsib != null) {
                int prevSibling = _prevsib(index);
                if (-1 == prevSibling) {
                    ps.println("Prev sibling: DTM.NULL");
                } else if (-2 == prevSibling) {
                    ps.println("Prev sibling: NOTPROCESSED");
                } else {
                    ps.println("Prev sibling: " + prevSibling);
                }
            }
            int nextSibling = _nextsib(index);
            if (-1 == nextSibling) {
                ps.println("Next sibling: DTM.NULL");
            } else if (-2 == nextSibling) {
                ps.println("Next sibling: NOTPROCESSED");
            } else {
                ps.println("Next sibling: " + nextSibling);
            }
            int parent = _parent(index);
            if (-1 == parent) {
                ps.println("Parent: DTM.NULL");
            } else if (-2 == parent) {
                ps.println("Parent: NOTPROCESSED");
            } else {
                ps.println("Parent: " + parent);
            }
            ps.println("Level: " + _level(index));
            ps.println("Node Value: " + getNodeValue(i));
            ps.println("String Value: " + getStringValue(i));
        }
    }

    public String dumpNode(int nodeHandle) {
        if (nodeHandle == -1) {
            return "[null]";
        }
        String typestring;
        switch (getNodeType(nodeHandle)) {
            case (short) -1:
                typestring = "null";
                break;
            case (short) 1:
                typestring = "ELEMENT";
                break;
            case (short) 2:
                typestring = "ATTR";
                break;
            case (short) 3:
                typestring = "TEXT";
                break;
            case (short) 4:
                typestring = "CDATA";
                break;
            case (short) 5:
                typestring = "ENT_REF";
                break;
            case (short) 6:
                typestring = "ENTITY";
                break;
            case (short) 7:
                typestring = "PI";
                break;
            case (short) 8:
                typestring = "COMMENT";
                break;
            case (short) 9:
                typestring = "DOC";
                break;
            case (short) 10:
                typestring = "DOC_TYPE";
                break;
            case (short) 11:
                typestring = "DOC_FRAG";
                break;
            case (short) 12:
                typestring = "NOTATION";
                break;
            case (short) 13:
                typestring = "NAMESPACE";
                break;
            default:
                typestring = "Unknown!";
                break;
        }
        StringBuffer sb = new StringBuffer();
        sb.append("[" + nodeHandle + ": " + typestring + "(0x" + Integer.toHexString(getExpandedTypeID(nodeHandle)) + ") " + getNodeNameX(nodeHandle) + " {" + getNamespaceURI(nodeHandle) + "}" + "=\"" + getNodeValue(nodeHandle) + "\"]");
        return sb.toString();
    }

    public void setFeature(String featureId, boolean state) {
    }

    public boolean hasChildNodes(int nodeHandle) {
        return _firstch(makeNodeIdentity(nodeHandle)) != -1;
    }

    public final int makeNodeHandle(int nodeIdentity) {
        if (-1 == nodeIdentity) {
            return -1;
        }
        return this.m_dtmIdent.elementAt(nodeIdentity >>> 16) + (DTMManager.IDENT_NODE_DEFAULT & nodeIdentity);
    }

    public final int makeNodeIdentity(int nodeHandle) {
        int i = -1;
        if (-1 == nodeHandle) {
            return -1;
        }
        if (this.m_mgrDefault != null) {
            int whichDTMindex = nodeHandle >>> 16;
            if (this.m_mgrDefault.m_dtms[whichDTMindex] != this) {
                return -1;
            }
            return this.m_mgrDefault.m_dtm_offsets[whichDTMindex] | (nodeHandle & DTMManager.IDENT_NODE_DEFAULT);
        }
        int whichDTMid = this.m_dtmIdent.indexOf(DTMManager.IDENT_DTM_DEFAULT & nodeHandle);
        if (whichDTMid != -1) {
            i = (whichDTMid << 16) + (nodeHandle & DTMManager.IDENT_NODE_DEFAULT);
        }
        return i;
    }

    public int getFirstChild(int nodeHandle) {
        return makeNodeHandle(_firstch(makeNodeIdentity(nodeHandle)));
    }

    public int getTypedFirstChild(int nodeHandle, int nodeType) {
        int firstChild;
        if (nodeType < 14) {
            firstChild = _firstch(makeNodeIdentity(nodeHandle));
            while (firstChild != -1) {
                int eType = _exptype(firstChild);
                if (eType == nodeType || (eType >= 14 && this.m_expandedNameTable.getType(eType) == nodeType)) {
                    return makeNodeHandle(firstChild);
                }
                firstChild = _nextsib(firstChild);
            }
        } else {
            firstChild = _firstch(makeNodeIdentity(nodeHandle));
            while (firstChild != -1) {
                if (_exptype(firstChild) == nodeType) {
                    return makeNodeHandle(firstChild);
                }
                firstChild = _nextsib(firstChild);
            }
        }
        return -1;
    }

    public int getLastChild(int nodeHandle) {
        int child = _firstch(makeNodeIdentity(nodeHandle));
        int lastChild = -1;
        while (child != -1) {
            lastChild = child;
            child = _nextsib(child);
        }
        return makeNodeHandle(lastChild);
    }

    public int getFirstAttribute(int nodeHandle) {
        return makeNodeHandle(getFirstAttributeIdentity(makeNodeIdentity(nodeHandle)));
    }

    protected int getFirstAttributeIdentity(int identity) {
        if (1 == _type(identity)) {
            int type;
            do {
                identity = getNextNodeIdentity(identity);
                if (-1 == identity) {
                    break;
                }
                type = _type(identity);
                if (type == 2) {
                    return identity;
                }
            } while (13 == type);
        }
        return -1;
    }

    protected int getTypedAttribute(int nodeHandle, int attType) {
        if (1 == getNodeType(nodeHandle)) {
            int identity = makeNodeIdentity(nodeHandle);
            while (true) {
                identity = getNextNodeIdentity(identity);
                if (-1 == identity) {
                    break;
                }
                int type = _type(identity);
                if (type == 2) {
                    if (_exptype(identity) == attType) {
                        return makeNodeHandle(identity);
                    }
                } else if (13 != type) {
                    break;
                }
            }
        }
        return -1;
    }

    public int getNextSibling(int nodeHandle) {
        if (nodeHandle == -1) {
            return -1;
        }
        return makeNodeHandle(_nextsib(makeNodeIdentity(nodeHandle)));
    }

    public int getTypedNextSibling(int nodeHandle, int nodeType) {
        int i = -1;
        if (nodeHandle == -1) {
            return -1;
        }
        int node = makeNodeIdentity(nodeHandle);
        int eType;
        do {
            node = _nextsib(node);
            if (node == -1) {
                break;
            }
            eType = _exptype(node);
            if (eType == nodeType) {
                break;
            }
        } while (this.m_expandedNameTable.getType(eType) != nodeType);
        if (node != -1) {
            i = makeNodeHandle(node);
        }
        return i;
    }

    public int getPreviousSibling(int nodeHandle) {
        if (nodeHandle == -1) {
            return -1;
        }
        if (this.m_prevsib != null) {
            return makeNodeHandle(_prevsib(makeNodeIdentity(nodeHandle)));
        }
        int nodeID = makeNodeIdentity(nodeHandle);
        int node = _firstch(_parent(nodeID));
        int result = -1;
        while (node != nodeID) {
            result = node;
            node = _nextsib(node);
        }
        return makeNodeHandle(result);
    }

    public int getNextAttribute(int nodeHandle) {
        int nodeID = makeNodeIdentity(nodeHandle);
        if (_type(nodeID) == (short) 2) {
            return makeNodeHandle(getNextAttributeIdentity(nodeID));
        }
        return -1;
    }

    protected int getNextAttributeIdentity(int identity) {
        int type;
        do {
            identity = getNextNodeIdentity(identity);
            if (-1 == identity) {
                break;
            }
            type = _type(identity);
            if (type == 2) {
                return identity;
            }
        } while (type == 13);
        return -1;
    }

    protected void declareNamespaceInContext(int elementNodeIndex, int namespaceNodeIndex) {
        int i;
        SuballocatedIntVector nsList = null;
        if (this.m_namespaceDeclSets == null) {
            this.m_namespaceDeclSetElements = new SuballocatedIntVector(32);
            this.m_namespaceDeclSetElements.addElement(elementNodeIndex);
            this.m_namespaceDeclSets = new Vector();
            nsList = new SuballocatedIntVector(32);
            this.m_namespaceDeclSets.addElement(nsList);
        } else {
            int last = this.m_namespaceDeclSetElements.size() - 1;
            if (last >= 0 && elementNodeIndex == this.m_namespaceDeclSetElements.elementAt(last)) {
                nsList = (SuballocatedIntVector) this.m_namespaceDeclSets.elementAt(last);
            }
        }
        if (nsList == null) {
            this.m_namespaceDeclSetElements.addElement(elementNodeIndex);
            SuballocatedIntVector inherited = findNamespaceContext(_parent(elementNodeIndex));
            if (inherited != null) {
                int isize = inherited.size();
                nsList = new SuballocatedIntVector(Math.max(Math.min(isize + 16, DTMFilter.SHOW_NOTATION), 32));
                for (i = 0; i < isize; i++) {
                    nsList.addElement(inherited.elementAt(i));
                }
            } else {
                nsList = new SuballocatedIntVector(32);
            }
            this.m_namespaceDeclSets.addElement(nsList);
        }
        int newEType = _exptype(namespaceNodeIndex);
        for (i = nsList.size() - 1; i >= 0; i--) {
            if (newEType == getExpandedTypeID(nsList.elementAt(i))) {
                nsList.setElementAt(makeNodeHandle(namespaceNodeIndex), i);
                return;
            }
        }
        nsList.addElement(makeNodeHandle(namespaceNodeIndex));
    }

    protected SuballocatedIntVector findNamespaceContext(int elementNodeIndex) {
        if (this.m_namespaceDeclSetElements != null) {
            int wouldBeAt = findInSortedSuballocatedIntVector(this.m_namespaceDeclSetElements, elementNodeIndex);
            if (wouldBeAt < 0) {
                if (wouldBeAt != -1) {
                    wouldBeAt = (-1 - wouldBeAt) - 1;
                    int candidate = this.m_namespaceDeclSetElements.elementAt(wouldBeAt);
                    int ancestor = _parent(elementNodeIndex);
                    if (wouldBeAt == 0 && candidate < ancestor) {
                        int uppermostNSCandidateID;
                        int rootHandle = getDocumentRoot(makeNodeHandle(elementNodeIndex));
                        int rootID = makeNodeIdentity(rootHandle);
                        if (getNodeType(rootHandle) == (short) 9) {
                            int ch = _firstch(rootID);
                            uppermostNSCandidateID = ch != -1 ? ch : rootID;
                        } else {
                            uppermostNSCandidateID = rootID;
                        }
                        if (candidate == uppermostNSCandidateID) {
                            return (SuballocatedIntVector) this.m_namespaceDeclSets.elementAt(wouldBeAt);
                        }
                    }
                    while (wouldBeAt >= 0 && ancestor > 0) {
                        if (candidate != ancestor) {
                            if (candidate >= ancestor) {
                                if (wouldBeAt <= 0) {
                                    break;
                                }
                                wouldBeAt--;
                                candidate = this.m_namespaceDeclSetElements.elementAt(wouldBeAt);
                            } else {
                                while (true) {
                                    ancestor = _parent(ancestor);
                                    if (candidate >= ancestor) {
                                        break;
                                    }
                                }
                            }
                        } else {
                            return (SuballocatedIntVector) this.m_namespaceDeclSets.elementAt(wouldBeAt);
                        }
                    }
                }
                return null;
            }
            return (SuballocatedIntVector) this.m_namespaceDeclSets.elementAt(wouldBeAt);
        }
        return null;
    }

    protected int findInSortedSuballocatedIntVector(SuballocatedIntVector vector, int lookfor) {
        int i = 0;
        if (vector != null) {
            int first = 0;
            int last = vector.size() - 1;
            while (first <= last) {
                i = (first + last) / 2;
                int test = lookfor - vector.elementAt(i);
                if (test == 0) {
                    return i;
                }
                if (test < 0) {
                    last = i - 1;
                } else {
                    first = i + 1;
                }
            }
            if (first > i) {
                i = first;
            }
        }
        return -1 - i;
    }

    public int getFirstNamespaceNode(int nodeHandle, boolean inScope) {
        int identity;
        if (inScope) {
            identity = makeNodeIdentity(nodeHandle);
            if (_type(identity) != (short) 1) {
                return -1;
            }
            SuballocatedIntVector nsContext = findNamespaceContext(identity);
            if (nsContext == null || nsContext.size() < 1) {
                return -1;
            }
            return nsContext.elementAt(0);
        }
        identity = makeNodeIdentity(nodeHandle);
        if (_type(identity) != (short) 1) {
            return -1;
        }
        int type;
        do {
            identity = getNextNodeIdentity(identity);
            if (-1 == identity) {
                break;
            }
            type = _type(identity);
            if (type == 13) {
                return makeNodeHandle(identity);
            }
        } while (2 == type);
        return -1;
    }

    public int getNextNamespaceNode(int baseHandle, int nodeHandle, boolean inScope) {
        if (inScope) {
            SuballocatedIntVector nsContext = findNamespaceContext(makeNodeIdentity(baseHandle));
            if (nsContext == null) {
                return -1;
            }
            int i = nsContext.indexOf(nodeHandle) + 1;
            if (i <= 0 || i == nsContext.size()) {
                return -1;
            }
            return nsContext.elementAt(i);
        }
        int identity = makeNodeIdentity(nodeHandle);
        int type;
        do {
            identity = getNextNodeIdentity(identity);
            if (-1 == identity) {
                break;
            }
            type = _type(identity);
            if (type == 13) {
                return makeNodeHandle(identity);
            }
        } while (type == 2);
        return -1;
    }

    public int getParent(int nodeHandle) {
        int identity = makeNodeIdentity(nodeHandle);
        if (identity > 0) {
            return makeNodeHandle(_parent(identity));
        }
        return -1;
    }

    public int getDocument() {
        return this.m_dtmIdent.elementAt(0);
    }

    public int getOwnerDocument(int nodeHandle) {
        if ((short) 9 == getNodeType(nodeHandle)) {
            return -1;
        }
        return getDocumentRoot(nodeHandle);
    }

    public int getDocumentRoot(int nodeHandle) {
        return getManager().getDTM(nodeHandle).getDocument();
    }

    public int getStringValueChunkCount(int nodeHandle) {
        error(XMLMessages.createXMLMessage(XMLErrorResources.ER_METHOD_NOT_SUPPORTED, null));
        return 0;
    }

    public char[] getStringValueChunk(int nodeHandle, int chunkIndex, int[] startAndLen) {
        error(XMLMessages.createXMLMessage(XMLErrorResources.ER_METHOD_NOT_SUPPORTED, null));
        return null;
    }

    public int getExpandedTypeID(int nodeHandle) {
        int id = makeNodeIdentity(nodeHandle);
        if (id == -1) {
            return -1;
        }
        return _exptype(id);
    }

    public int getExpandedTypeID(String namespace, String localName, int type) {
        return this.m_expandedNameTable.getExpandedTypeID(namespace, localName, type);
    }

    public String getLocalNameFromExpandedNameID(int expandedNameID) {
        return this.m_expandedNameTable.getLocalName(expandedNameID);
    }

    public String getNamespaceFromExpandedNameID(int expandedNameID) {
        return this.m_expandedNameTable.getNamespace(expandedNameID);
    }

    public int getNamespaceType(int nodeHandle) {
        return this.m_expandedNameTable.getNamespaceID(_exptype(makeNodeIdentity(nodeHandle)));
    }

    public String getNodeNameX(int nodeHandle) {
        error(XMLMessages.createXMLMessage(XMLErrorResources.ER_METHOD_NOT_SUPPORTED, null));
        return null;
    }

    public short getNodeType(int nodeHandle) {
        if (nodeHandle == -1) {
            return (short) -1;
        }
        return this.m_expandedNameTable.getType(_exptype(makeNodeIdentity(nodeHandle)));
    }

    public short getLevel(int nodeHandle) {
        return (short) (_level(makeNodeIdentity(nodeHandle)) + 1);
    }

    public int getNodeIdent(int nodeHandle) {
        return makeNodeIdentity(nodeHandle);
    }

    public int getNodeHandle(int nodeId) {
        return makeNodeHandle(nodeId);
    }

    public boolean isSupported(String feature, String version) {
        return false;
    }

    public String getDocumentBaseURI() {
        return this.m_documentBaseURI;
    }

    public void setDocumentBaseURI(String baseURI) {
        this.m_documentBaseURI = baseURI;
    }

    public String getDocumentSystemIdentifier(int nodeHandle) {
        return this.m_documentBaseURI;
    }

    public String getDocumentEncoding(int nodeHandle) {
        return "UTF-8";
    }

    public String getDocumentStandalone(int nodeHandle) {
        return null;
    }

    public String getDocumentVersion(int documentHandle) {
        return null;
    }

    public boolean getDocumentAllDeclarationsProcessed() {
        return true;
    }

    public boolean supportsPreStripping() {
        return true;
    }

    public boolean isNodeAfter(int nodeHandle1, int nodeHandle2) {
        int index1 = makeNodeIdentity(nodeHandle1);
        int index2 = makeNodeIdentity(nodeHandle2);
        if (index1 == -1 || index2 == -1 || index1 > index2) {
            return false;
        }
        return true;
    }

    public boolean isCharacterElementContentWhitespace(int nodeHandle) {
        return false;
    }

    public boolean isDocumentAllDeclarationsProcessed(int documentHandle) {
        return true;
    }

    public Node getNode(int nodeHandle) {
        return new DTMNodeProxy(this, nodeHandle);
    }

    public void appendChild(int newChild, boolean clone, boolean cloneDepth) {
        error(XMLMessages.createXMLMessage(XMLErrorResources.ER_METHOD_NOT_SUPPORTED, null));
    }

    public void appendTextChild(String str) {
        error(XMLMessages.createXMLMessage(XMLErrorResources.ER_METHOD_NOT_SUPPORTED, null));
    }

    protected void error(String msg) {
        throw new DTMException(msg);
    }

    protected boolean getShouldStripWhitespace() {
        return this.m_shouldStripWS;
    }

    protected void pushShouldStripWhitespace(boolean shouldStrip) {
        this.m_shouldStripWS = shouldStrip;
        if (this.m_shouldStripWhitespaceStack != null) {
            this.m_shouldStripWhitespaceStack.push(shouldStrip);
        }
    }

    protected void popShouldStripWhitespace() {
        if (this.m_shouldStripWhitespaceStack != null) {
            this.m_shouldStripWS = this.m_shouldStripWhitespaceStack.popAndTop();
        }
    }

    protected void setShouldStripWhitespace(boolean shouldStrip) {
        this.m_shouldStripWS = shouldStrip;
        if (this.m_shouldStripWhitespaceStack != null) {
            this.m_shouldStripWhitespaceStack.setTop(shouldStrip);
        }
    }

    public void documentRegistration() {
    }

    public void documentRelease() {
    }

    public void migrateTo(DTMManager mgr) {
        this.m_mgr = mgr;
        if (mgr instanceof DTMManagerDefault) {
            this.m_mgrDefault = (DTMManagerDefault) mgr;
        }
    }

    public DTMManager getManager() {
        return this.m_mgr;
    }

    public SuballocatedIntVector getDTMIDs() {
        if (this.m_mgr == null) {
            return null;
        }
        return this.m_dtmIdent;
    }
}
