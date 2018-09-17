package org.apache.xalan.templates;

import java.util.List;
import java.util.Vector;
import javax.xml.transform.TransformerException;
import org.apache.xalan.res.XSLMessages;
import org.apache.xalan.res.XSLTErrorResources;
import org.apache.xalan.transformer.TransformerImpl;
import org.apache.xml.utils.QName;

public class ElemUse extends ElemTemplateElement {
    static final long serialVersionUID = 5830057200289299736L;
    private QName[] m_attributeSetsNames = null;

    public void setUseAttributeSets(Vector v) {
        int n = v.size();
        this.m_attributeSetsNames = new QName[n];
        for (int i = 0; i < n; i++) {
            this.m_attributeSetsNames[i] = (QName) v.elementAt(i);
        }
    }

    public void setUseAttributeSets(QName[] v) {
        this.m_attributeSetsNames = v;
    }

    public QName[] getUseAttributeSets() {
        return this.m_attributeSetsNames;
    }

    public void applyAttrSets(TransformerImpl transformer, StylesheetRoot stylesheet) throws TransformerException {
        applyAttrSets(transformer, stylesheet, this.m_attributeSetsNames);
    }

    private void applyAttrSets(TransformerImpl transformer, StylesheetRoot stylesheet, QName[] attributeSetsNames) throws TransformerException {
        if (attributeSetsNames != null) {
            int nNames = attributeSetsNames.length;
            int i = 0;
            while (i < nNames) {
                List attrSets = stylesheet.getAttributeSetComposed(attributeSetsNames[i]);
                if (attrSets != null) {
                    for (int k = attrSets.size() - 1; k >= 0; k--) {
                        ((ElemAttributeSet) attrSets.get(k)).execute(transformer);
                    }
                    i++;
                } else {
                    throw new TransformerException(XSLMessages.createMessage(XSLTErrorResources.ER_NO_ATTRIB_SET, new Object[]{qname}), this);
                }
            }
        }
    }

    public void execute(TransformerImpl transformer) throws TransformerException {
        if (this.m_attributeSetsNames != null) {
            applyAttrSets(transformer, getStylesheetRoot(), this.m_attributeSetsNames);
        }
    }
}
