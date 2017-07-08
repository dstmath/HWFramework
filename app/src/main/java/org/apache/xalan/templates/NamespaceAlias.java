package org.apache.xalan.templates;

public class NamespaceAlias extends ElemTemplateElement {
    static final long serialVersionUID = 456173966637810718L;
    private String m_ResultNamespace;
    private String m_ResultPrefix;
    private String m_StylesheetNamespace;
    private String m_StylesheetPrefix;

    public NamespaceAlias(int docOrderNumber) {
        this.m_docOrderNumber = docOrderNumber;
    }

    public void setStylesheetPrefix(String v) {
        this.m_StylesheetPrefix = v;
    }

    public String getStylesheetPrefix() {
        return this.m_StylesheetPrefix;
    }

    public void setStylesheetNamespace(String v) {
        this.m_StylesheetNamespace = v;
    }

    public String getStylesheetNamespace() {
        return this.m_StylesheetNamespace;
    }

    public void setResultPrefix(String v) {
        this.m_ResultPrefix = v;
    }

    public String getResultPrefix() {
        return this.m_ResultPrefix;
    }

    public void setResultNamespace(String v) {
        this.m_ResultNamespace = v;
    }

    public String getResultNamespace() {
        return this.m_ResultNamespace;
    }

    public void recompose(StylesheetRoot root) {
        root.recomposeNamespaceAliases(this);
    }
}
