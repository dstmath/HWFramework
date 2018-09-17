package org.apache.xml.dtm.ref.sax2dtm;

import java.util.Vector;
import javax.xml.transform.Source;
import org.apache.xml.dtm.DTMManager;
import org.apache.xml.dtm.DTMWSFilter;
import org.apache.xml.utils.IntStack;
import org.apache.xml.utils.IntVector;
import org.apache.xml.utils.StringVector;
import org.apache.xml.utils.XMLStringFactory;
import org.xml.sax.SAXException;

public class SAX2RTFDTM extends SAX2DTM {
    private static final boolean DEBUG = false;
    private int m_currentDocumentNode;
    int m_emptyCharsCount;
    int m_emptyDataCount;
    int m_emptyDataQNCount;
    int m_emptyNSDeclSetCount;
    int m_emptyNSDeclSetElemsCount;
    int m_emptyNodeCount;
    IntStack mark_char_size;
    IntStack mark_data_size;
    IntStack mark_doq_size;
    IntStack mark_nsdeclelem_size;
    IntStack mark_nsdeclset_size;
    IntStack mark_size;

    public SAX2RTFDTM(DTMManager mgr, Source source, int dtmIdentity, DTMWSFilter whiteSpaceFilter, XMLStringFactory xstringfactory, boolean doIndexing) {
        StringVector stringVector;
        IntVector intVector;
        int i;
        int i2 = 0;
        IntVector intVector2 = null;
        super(mgr, source, dtmIdentity, whiteSpaceFilter, xstringfactory, doIndexing);
        this.m_currentDocumentNode = -1;
        this.mark_size = new IntStack();
        this.mark_data_size = new IntStack();
        this.mark_char_size = new IntStack();
        this.mark_doq_size = new IntStack();
        this.mark_nsdeclset_size = new IntStack();
        this.mark_nsdeclelem_size = new IntStack();
        this.m_useSourceLocationProperty = false;
        if (this.m_useSourceLocationProperty) {
            stringVector = new StringVector();
        } else {
            stringVector = null;
        }
        this.m_sourceSystemId = stringVector;
        if (this.m_useSourceLocationProperty) {
            intVector = new IntVector();
        } else {
            intVector = null;
        }
        this.m_sourceLine = intVector;
        if (this.m_useSourceLocationProperty) {
            intVector2 = new IntVector();
        }
        this.m_sourceColumn = intVector2;
        this.m_emptyNodeCount = this.m_size;
        if (this.m_namespaceDeclSets == null) {
            i = 0;
        } else {
            i = this.m_namespaceDeclSets.size();
        }
        this.m_emptyNSDeclSetCount = i;
        if (this.m_namespaceDeclSetElements != null) {
            i2 = this.m_namespaceDeclSetElements.size();
        }
        this.m_emptyNSDeclSetElemsCount = i2;
        this.m_emptyDataCount = this.m_data.size();
        this.m_emptyCharsCount = this.m_chars.size();
        this.m_emptyDataQNCount = this.m_dataOrQName.size();
    }

    public int getDocument() {
        return makeNodeHandle(this.m_currentDocumentNode);
    }

    public int getDocumentRoot(int nodeHandle) {
        int id = makeNodeIdentity(nodeHandle);
        while (id != -1) {
            if (_type(id) == (short) 9) {
                return makeNodeHandle(id);
            }
            id = _parent(id);
        }
        return -1;
    }

    protected int _documentRoot(int nodeIdentifier) {
        if (nodeIdentifier == -1) {
            return -1;
        }
        int parent = _parent(nodeIdentifier);
        while (parent != -1) {
            nodeIdentifier = parent;
            parent = _parent(parent);
        }
        return nodeIdentifier;
    }

    public void startDocument() throws SAXException {
        this.m_endDocumentOccured = false;
        this.m_prefixMappings = new Vector();
        this.m_contextIndexes = new IntStack();
        this.m_parents = new IntStack();
        this.m_currentDocumentNode = this.m_size;
        super.startDocument();
    }

    public void endDocument() throws SAXException {
        charactersFlush();
        this.m_nextsib.setElementAt(-1, this.m_currentDocumentNode);
        if (this.m_firstch.elementAt(this.m_currentDocumentNode) == -2) {
            this.m_firstch.setElementAt(-1, this.m_currentDocumentNode);
        }
        if (-1 != this.m_previous) {
            this.m_nextsib.setElementAt(-1, this.m_previous);
        }
        this.m_parents = null;
        this.m_prefixMappings = null;
        this.m_contextIndexes = null;
        this.m_currentDocumentNode = -1;
        this.m_endDocumentOccured = true;
    }

    public void pushRewindMark() {
        int i = 0;
        if (this.m_indexing || this.m_elemIndexes != null) {
            throw new NullPointerException("Coding error; Don't try to mark/rewind an indexed DTM");
        }
        int i2;
        this.mark_size.push(this.m_size);
        IntStack intStack = this.mark_nsdeclset_size;
        if (this.m_namespaceDeclSets == null) {
            i2 = 0;
        } else {
            i2 = this.m_namespaceDeclSets.size();
        }
        intStack.push(i2);
        IntStack intStack2 = this.mark_nsdeclelem_size;
        if (this.m_namespaceDeclSetElements != null) {
            i = this.m_namespaceDeclSetElements.size();
        }
        intStack2.push(i);
        this.mark_data_size.push(this.m_data.size());
        this.mark_char_size.push(this.m_chars.size());
        this.mark_doq_size.push(this.m_dataOrQName.size());
    }

    public boolean popRewindMark() {
        boolean top = this.mark_size.empty();
        this.m_size = top ? this.m_emptyNodeCount : this.mark_size.pop();
        this.m_exptype.setSize(this.m_size);
        this.m_firstch.setSize(this.m_size);
        this.m_nextsib.setSize(this.m_size);
        this.m_prevsib.setSize(this.m_size);
        this.m_parent.setSize(this.m_size);
        this.m_elemIndexes = null;
        int ds = top ? this.m_emptyNSDeclSetCount : this.mark_nsdeclset_size.pop();
        if (this.m_namespaceDeclSets != null) {
            this.m_namespaceDeclSets.setSize(ds);
        }
        int ds1 = top ? this.m_emptyNSDeclSetElemsCount : this.mark_nsdeclelem_size.pop();
        if (this.m_namespaceDeclSetElements != null) {
            this.m_namespaceDeclSetElements.setSize(ds1);
        }
        this.m_data.setSize(top ? this.m_emptyDataCount : this.mark_data_size.pop());
        this.m_chars.setLength(top ? this.m_emptyCharsCount : this.mark_char_size.pop());
        this.m_dataOrQName.setSize(top ? this.m_emptyDataQNCount : this.mark_doq_size.pop());
        if (this.m_size == 0) {
            return true;
        }
        return false;
    }

    public boolean isTreeIncomplete() {
        return this.m_endDocumentOccured ^ 1;
    }
}
