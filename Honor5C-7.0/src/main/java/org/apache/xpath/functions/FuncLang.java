package org.apache.xpath.functions;

import javax.xml.transform.TransformerException;
import org.apache.xml.dtm.DTM;
import org.apache.xpath.XPathContext;
import org.apache.xpath.compiler.Keywords;
import org.apache.xpath.jaxp.JAXPPrefixResolver;
import org.apache.xpath.objects.XBoolean;
import org.apache.xpath.objects.XObject;

public class FuncLang extends FunctionOneArg {
    static final long serialVersionUID = -7868705139354872185L;

    public XObject execute(XPathContext xctxt) throws TransformerException {
        String lang = this.m_arg0.execute(xctxt).str();
        int parent = xctxt.getCurrentNode();
        boolean isLang = false;
        DTM dtm = xctxt.getDTM(parent);
        while (-1 != parent) {
            if ((short) 1 == dtm.getNodeType(parent)) {
                int langAttr = dtm.getAttributeNode(parent, JAXPPrefixResolver.S_XMLNAMESPACEURI, Keywords.FUNC_LANG_STRING);
                if (-1 != langAttr) {
                    String langVal = dtm.getNodeValue(langAttr);
                    if (langVal.toLowerCase().startsWith(lang.toLowerCase())) {
                        int valLen = lang.length();
                        if (langVal.length() == valLen || langVal.charAt(valLen) == '-') {
                            isLang = true;
                        }
                    }
                    return isLang ? XBoolean.S_TRUE : XBoolean.S_FALSE;
                }
            }
            parent = dtm.getParent(parent);
        }
        if (isLang) {
        }
    }
}
