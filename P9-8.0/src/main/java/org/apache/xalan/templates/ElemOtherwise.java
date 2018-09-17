package org.apache.xalan.templates;

public class ElemOtherwise extends ElemTemplateElement {
    static final long serialVersionUID = 1863944560970181395L;

    public int getXSLToken() {
        return 39;
    }

    public String getNodeName() {
        return Constants.ELEMNAME_OTHERWISE_STRING;
    }
}
