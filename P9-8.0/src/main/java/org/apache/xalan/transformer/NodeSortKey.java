package org.apache.xalan.transformer;

import java.text.Collator;
import java.util.Locale;
import javax.xml.transform.TransformerException;
import org.apache.xalan.res.XSLTErrorResources;
import org.apache.xml.utils.PrefixResolver;
import org.apache.xpath.XPath;

class NodeSortKey {
    boolean m_caseOrderUpper;
    Collator m_col;
    boolean m_descending;
    Locale m_locale;
    PrefixResolver m_namespaceContext;
    TransformerImpl m_processor;
    XPath m_selectPat;
    boolean m_treatAsNumbers;

    NodeSortKey(TransformerImpl transformer, XPath selectPat, boolean treatAsNumbers, boolean descending, String langValue, boolean caseOrderUpper, PrefixResolver namespaceContext) throws TransformerException {
        this.m_processor = transformer;
        this.m_namespaceContext = namespaceContext;
        this.m_selectPat = selectPat;
        this.m_treatAsNumbers = treatAsNumbers;
        this.m_descending = descending;
        this.m_caseOrderUpper = caseOrderUpper;
        if (langValue == null || this.m_treatAsNumbers) {
            this.m_locale = Locale.getDefault();
        } else {
            this.m_locale = new Locale(langValue.toLowerCase(), Locale.getDefault().getCountry());
            if (this.m_locale == null) {
                this.m_locale = Locale.getDefault();
            }
        }
        this.m_col = Collator.getInstance(this.m_locale);
        if (this.m_col == null) {
            this.m_processor.getMsgMgr().warn(null, XSLTErrorResources.WG_CANNOT_FIND_COLLATOR, new Object[]{langValue});
            this.m_col = Collator.getInstance();
        }
    }
}
