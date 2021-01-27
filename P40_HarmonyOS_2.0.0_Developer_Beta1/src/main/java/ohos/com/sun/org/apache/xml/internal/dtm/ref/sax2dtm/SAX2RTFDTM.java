package ohos.com.sun.org.apache.xml.internal.dtm.ref.sax2dtm;

import java.util.Vector;
import ohos.com.sun.org.apache.xml.internal.dtm.DTMManager;
import ohos.com.sun.org.apache.xml.internal.dtm.DTMWSFilter;
import ohos.com.sun.org.apache.xml.internal.utils.IntStack;
import ohos.com.sun.org.apache.xml.internal.utils.IntVector;
import ohos.com.sun.org.apache.xml.internal.utils.StringVector;
import ohos.com.sun.org.apache.xml.internal.utils.XMLStringFactory;
import ohos.javax.xml.transform.Source;
import ohos.org.xml.sax.SAXException;

public class SAX2RTFDTM extends SAX2DTM {
    private static final boolean DEBUG = false;
    private int m_currentDocumentNode = -1;
    int m_emptyCharsCount;
    int m_emptyDataCount;
    int m_emptyDataQNCount;
    int m_emptyNSDeclSetCount;
    int m_emptyNSDeclSetElemsCount;
    int m_emptyNodeCount;
    IntStack mark_char_size = new IntStack();
    IntStack mark_data_size = new IntStack();
    IntStack mark_doq_size = new IntStack();
    IntStack mark_nsdeclelem_size = new IntStack();
    IntStack mark_nsdeclset_size = new IntStack();
    IntStack mark_size = new IntStack();

    public SAX2RTFDTM(DTMManager dTMManager, Source source, int i, DTMWSFilter dTMWSFilter, XMLStringFactory xMLStringFactory, boolean z) {
        super(dTMManager, source, i, dTMWSFilter, xMLStringFactory, z);
        int i2;
        int i3 = 0;
        this.m_useSourceLocationProperty = false;
        IntVector intVector = null;
        this.m_sourceSystemId = this.m_useSourceLocationProperty ? new StringVector() : null;
        this.m_sourceLine = this.m_useSourceLocationProperty ? new IntVector() : null;
        this.m_sourceColumn = this.m_useSourceLocationProperty ? new IntVector() : intVector;
        this.m_emptyNodeCount = this.m_size;
        if (this.m_namespaceDeclSets == null) {
            i2 = 0;
        } else {
            i2 = this.m_namespaceDeclSets.size();
        }
        this.m_emptyNSDeclSetCount = i2;
        this.m_emptyNSDeclSetElemsCount = this.m_namespaceDeclSetElements != null ? this.m_namespaceDeclSetElements.size() : i3;
        this.m_emptyDataCount = this.m_data.size();
        this.m_emptyCharsCount = this.m_chars.size();
        this.m_emptyDataQNCount = this.m_dataOrQName.size();
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMDefaultBase, ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public int getDocument() {
        return makeNodeHandle(this.m_currentDocumentNode);
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMDefaultBase, ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public int getDocumentRoot(int i) {
        int makeNodeIdentity = makeNodeIdentity(i);
        while (makeNodeIdentity != -1) {
            if (_type(makeNodeIdentity) == 9) {
                return makeNodeHandle(makeNodeIdentity);
            }
            makeNodeIdentity = _parent(makeNodeIdentity);
        }
        return -1;
    }

    /* access modifiers changed from: protected */
    public int _documentRoot(int i) {
        if (i == -1) {
            return -1;
        }
        int _parent = _parent(i);
        while (true) {
            i = _parent;
            if (i == -1) {
                return i;
            }
            _parent = _parent(i);
        }
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.sax2dtm.SAX2DTM
    public void startDocument() throws SAXException {
        this.m_endDocumentOccured = false;
        this.m_prefixMappings = new Vector();
        this.m_contextIndexes = new IntStack();
        this.m_parents = new IntStack();
        this.m_currentDocumentNode = this.m_size;
        super.startDocument();
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.sax2dtm.SAX2DTM
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
        int i;
        if (this.m_indexing || this.m_elemIndexes != null) {
            throw new NullPointerException("Coding error; Don't try to mark/rewind an indexed DTM");
        }
        this.mark_size.push(this.m_size);
        IntStack intStack = this.mark_nsdeclset_size;
        int i2 = 0;
        if (this.m_namespaceDeclSets == null) {
            i = 0;
        } else {
            i = this.m_namespaceDeclSets.size();
        }
        intStack.push(i);
        IntStack intStack2 = this.mark_nsdeclelem_size;
        if (this.m_namespaceDeclSetElements != null) {
            i2 = this.m_namespaceDeclSetElements.size();
        }
        intStack2.push(i2);
        this.mark_data_size.push(this.m_data.size());
        this.mark_char_size.push(this.m_chars.size());
        this.mark_doq_size.push(this.m_dataOrQName.size());
    }

    public boolean popRewindMark() {
        boolean empty = this.mark_size.empty();
        this.m_size = empty ? this.m_emptyNodeCount : this.mark_size.pop();
        this.m_exptype.setSize(this.m_size);
        this.m_firstch.setSize(this.m_size);
        this.m_nextsib.setSize(this.m_size);
        this.m_prevsib.setSize(this.m_size);
        this.m_parent.setSize(this.m_size);
        this.m_elemIndexes = null;
        int pop = empty ? this.m_emptyNSDeclSetCount : this.mark_nsdeclset_size.pop();
        if (this.m_namespaceDeclSets != null) {
            this.m_namespaceDeclSets.setSize(pop);
        }
        int pop2 = empty ? this.m_emptyNSDeclSetElemsCount : this.mark_nsdeclelem_size.pop();
        if (this.m_namespaceDeclSetElements != null) {
            this.m_namespaceDeclSetElements.setSize(pop2);
        }
        this.m_data.setSize(empty ? this.m_emptyDataCount : this.mark_data_size.pop());
        this.m_chars.setLength(empty ? this.m_emptyCharsCount : this.mark_char_size.pop());
        this.m_dataOrQName.setSize(empty ? this.m_emptyDataQNCount : this.mark_doq_size.pop());
        return this.m_size == 0;
    }

    public boolean isTreeIncomplete() {
        return !this.m_endDocumentOccured;
    }
}
