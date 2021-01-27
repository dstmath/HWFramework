package ohos.com.sun.org.apache.xalan.internal.xsltc.dom;

import java.util.HashMap;
import java.util.Map;
import ohos.com.sun.org.apache.xalan.internal.xsltc.DOM;
import ohos.com.sun.org.apache.xalan.internal.xsltc.DOMEnhancedForDTM;
import ohos.com.sun.org.apache.xalan.internal.xsltc.StripFilter;
import ohos.com.sun.org.apache.xalan.internal.xsltc.runtime.AbstractTranslet;
import ohos.com.sun.org.apache.xml.internal.dtm.DTM;
import ohos.com.sun.org.apache.xml.internal.dtm.DTMWSFilter;

public class DOMWSFilter implements DTMWSFilter {
    private DTM m_currentDTM;
    private short[] m_currentMapping;
    private StripFilter m_filter;
    private Map<DTM, short[]> m_mappings = new HashMap();
    private AbstractTranslet m_translet;

    public DOMWSFilter(AbstractTranslet abstractTranslet) {
        this.m_translet = abstractTranslet;
        if (abstractTranslet instanceof StripFilter) {
            this.m_filter = (StripFilter) abstractTranslet;
        }
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMWSFilter
    public short getShouldStripSpace(int i, DTM dtm) {
        short[] sArr;
        if (this.m_filter == null || !(dtm instanceof DOM)) {
            return 1;
        }
        DOM dom = (DOM) dtm;
        if (!(dtm instanceof DOMEnhancedForDTM)) {
            return 3;
        }
        DOMEnhancedForDTM dOMEnhancedForDTM = (DOMEnhancedForDTM) dtm;
        if (dtm == this.m_currentDTM) {
            sArr = this.m_currentMapping;
        } else {
            sArr = this.m_mappings.get(dtm);
            if (sArr == null) {
                sArr = dOMEnhancedForDTM.getMapping(this.m_translet.getNamesArray(), this.m_translet.getUrisArray(), this.m_translet.getTypesArray());
                this.m_mappings.put(dtm, sArr);
                this.m_currentDTM = dtm;
                this.m_currentMapping = sArr;
            }
        }
        int expandedTypeID = dOMEnhancedForDTM.getExpandedTypeID(i);
        if (this.m_filter.stripSpace(dom, i, (expandedTypeID < 0 || expandedTypeID >= sArr.length) ? -1 : sArr[expandedTypeID])) {
            return 2;
        }
        return 1;
    }
}
