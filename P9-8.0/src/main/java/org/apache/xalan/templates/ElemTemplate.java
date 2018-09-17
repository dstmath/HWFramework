package org.apache.xalan.templates;

import java.util.Vector;
import javax.xml.transform.SourceLocator;
import javax.xml.transform.TransformerException;
import org.apache.xalan.transformer.TransformerImpl;
import org.apache.xml.utils.QName;
import org.apache.xpath.XPath;
import org.apache.xpath.XPathContext;

public class ElemTemplate extends ElemTemplateElement {
    static final long serialVersionUID = -5283056789965384058L;
    private int[] m_argsQNameIDs;
    public int m_frameSize;
    int m_inArgsSize;
    private XPath m_matchPattern = null;
    private QName m_mode;
    private QName m_name = null;
    private double m_priority = Double.NEGATIVE_INFINITY;
    private String m_publicId;
    private Stylesheet m_stylesheet;
    private String m_systemId;

    public String getPublicId() {
        return this.m_publicId;
    }

    public String getSystemId() {
        return this.m_systemId;
    }

    public void setLocaterInfo(SourceLocator locator) {
        this.m_publicId = locator.getPublicId();
        this.m_systemId = locator.getSystemId();
        super.setLocaterInfo(locator);
    }

    public StylesheetComposed getStylesheetComposed() {
        return this.m_stylesheet.getStylesheetComposed();
    }

    public Stylesheet getStylesheet() {
        return this.m_stylesheet;
    }

    public void setStylesheet(Stylesheet sheet) {
        this.m_stylesheet = sheet;
    }

    public StylesheetRoot getStylesheetRoot() {
        return this.m_stylesheet.getStylesheetRoot();
    }

    public void setMatch(XPath v) {
        this.m_matchPattern = v;
    }

    public XPath getMatch() {
        return this.m_matchPattern;
    }

    public void setName(QName v) {
        this.m_name = v;
    }

    public QName getName() {
        return this.m_name;
    }

    public void setMode(QName v) {
        this.m_mode = v;
    }

    public QName getMode() {
        return this.m_mode;
    }

    public void setPriority(double v) {
        this.m_priority = v;
    }

    public double getPriority() {
        return this.m_priority;
    }

    public int getXSLToken() {
        return 19;
    }

    public String getNodeName() {
        return Constants.ELEMNAME_TEMPLATE_STRING;
    }

    public void compose(StylesheetRoot sroot) throws TransformerException {
        super.compose(sroot);
        ComposeState cstate = sroot.getComposeState();
        Vector vnames = cstate.getVariableNames();
        if (this.m_matchPattern != null) {
            this.m_matchPattern.fixupVariables(vnames, sroot.getComposeState().getGlobalsSize());
        }
        cstate.resetStackFrameSize();
        this.m_inArgsSize = 0;
    }

    public void endCompose(StylesheetRoot sroot) throws TransformerException {
        ComposeState cstate = sroot.getComposeState();
        super.endCompose(sroot);
        this.m_frameSize = cstate.getFrameSize();
        cstate.resetStackFrameSize();
    }

    public void execute(TransformerImpl transformer) throws TransformerException {
        XPathContext xctxt = transformer.getXPathContext();
        xctxt.pushRTFContext();
        transformer.executeChildTemplates((ElemTemplateElement) this, true);
        xctxt.popRTFContext();
    }

    public void recompose(StylesheetRoot root) {
        root.recomposeTemplates(this);
    }
}
