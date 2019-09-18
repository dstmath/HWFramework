package org.apache.xalan.transformer;

import java.util.Vector;
import javax.xml.transform.TransformerException;
import org.apache.xalan.templates.ElemTemplateElement;
import org.apache.xml.utils.PrefixResolver;
import org.apache.xml.utils.QName;
import org.apache.xml.utils.XMLString;
import org.apache.xpath.XPathContext;
import org.apache.xpath.objects.XNodeSet;

public class KeyManager {
    private transient Vector m_key_tables = null;

    public XNodeSet getNodeSetDTMByKey(XPathContext xctxt, int doc, QName name, XMLString ref, PrefixResolver nscontext) throws TransformerException {
        XNodeSet nl = null;
        ElemTemplateElement template = (ElemTemplateElement) nscontext;
        if (template == null || template.getStylesheetRoot().getKeysComposed() == null) {
            return null;
        }
        boolean foundDoc = false;
        if (this.m_key_tables != null) {
            int nKeyTables = this.m_key_tables.size();
            int i = 0;
            while (true) {
                if (i >= nKeyTables) {
                    break;
                }
                KeyTable kt = (KeyTable) this.m_key_tables.elementAt(i);
                if (kt.getKeyTableName().equals(name) && doc == kt.getDocKey()) {
                    nl = kt.getNodeSetDTMByKey(name, ref);
                    if (nl != null) {
                        foundDoc = true;
                        break;
                    }
                }
                i++;
            }
        } else {
            this.m_key_tables = new Vector(4);
        }
        if (nl != null || foundDoc) {
            return nl;
        }
        KeyTable kt2 = new KeyTable(doc, nscontext, name, template.getStylesheetRoot().getKeysComposed(), xctxt);
        this.m_key_tables.addElement(kt2);
        if (doc == kt2.getDocKey()) {
            return kt2.getNodeSetDTMByKey(name, ref);
        }
        return nl;
    }
}
