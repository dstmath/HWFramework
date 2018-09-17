package org.apache.xalan.templates;

import java.io.Serializable;
import javax.xml.transform.TransformerException;
import org.apache.xml.utils.QName;
import org.apache.xpath.XPathContext;
import org.apache.xpath.patterns.StepPattern;

class TemplateSubPatternAssociation implements Serializable, Cloneable {
    static final long serialVersionUID = -8902606755229903350L;
    private TemplateSubPatternAssociation m_next = null;
    private String m_pattern;
    StepPattern m_stepPattern;
    private String m_targetString;
    private ElemTemplate m_template;
    private boolean m_wild;

    TemplateSubPatternAssociation(ElemTemplate template, StepPattern pattern, String pat) {
        this.m_pattern = pat;
        this.m_template = template;
        this.m_stepPattern = pattern;
        this.m_targetString = this.m_stepPattern.getTargetString();
        this.m_wild = this.m_targetString.equals("*");
    }

    public Object clone() throws CloneNotSupportedException {
        TemplateSubPatternAssociation tspa = (TemplateSubPatternAssociation) super.clone();
        tspa.m_next = null;
        return tspa;
    }

    public final String getTargetString() {
        return this.m_targetString;
    }

    public void setTargetString(String key) {
        this.m_targetString = key;
    }

    boolean matchMode(QName m1) {
        return matchModes(m1, this.m_template.getMode());
    }

    private boolean matchModes(QName m1, QName m2) {
        if (m1 == null && m2 == null) {
            return true;
        }
        return (m1 == null || m2 == null) ? false : m1.equals(m2);
    }

    public boolean matches(XPathContext xctxt, int targetNode, QName mode) throws TransformerException {
        if (Double.NEGATIVE_INFINITY != this.m_stepPattern.getMatchScore(xctxt, targetNode)) {
            return matchModes(mode, this.m_template.getMode());
        }
        return false;
    }

    public final boolean isWild() {
        return this.m_wild;
    }

    public final StepPattern getStepPattern() {
        return this.m_stepPattern;
    }

    public final String getPattern() {
        return this.m_pattern;
    }

    public int getDocOrderPos() {
        return this.m_template.getUid();
    }

    public final int getImportLevel() {
        return this.m_template.getStylesheetComposed().getImportCountComposed();
    }

    public final ElemTemplate getTemplate() {
        return this.m_template;
    }

    public final TemplateSubPatternAssociation getNext() {
        return this.m_next;
    }

    public void setNext(TemplateSubPatternAssociation mp) {
        this.m_next = mp;
    }
}
