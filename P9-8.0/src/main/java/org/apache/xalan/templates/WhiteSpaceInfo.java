package org.apache.xalan.templates;

import org.apache.xpath.XPath;

public class WhiteSpaceInfo extends ElemTemplate {
    static final long serialVersionUID = 6389208261999943836L;
    private boolean m_shouldStripSpace;

    public boolean getShouldStripSpace() {
        return this.m_shouldStripSpace;
    }

    public WhiteSpaceInfo(Stylesheet thisSheet) {
        setStylesheet(thisSheet);
    }

    public WhiteSpaceInfo(XPath matchPattern, boolean shouldStripSpace, Stylesheet thisSheet) {
        this.m_shouldStripSpace = shouldStripSpace;
        setMatch(matchPattern);
        setStylesheet(thisSheet);
    }

    public void recompose(StylesheetRoot root) {
        root.recomposeWhiteSpaceInfo(this);
    }
}
