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
        KeyTable kt;
        boolean foundDoc = false;
        if (this.m_key_tables == null) {
            this.m_key_tables = new Vector(4);
        } else {
            int nKeyTables = this.m_key_tables.size();
            for (int i = 0; i < nKeyTables; i++) {
                kt = (KeyTable) this.m_key_tables.elementAt(i);
                if (kt.getKeyTableName().equals(name) && doc == kt.getDocKey()) {
                    nl = kt.getNodeSetDTMByKey(name, ref);
                    if (nl != null) {
                        foundDoc = true;
                        break;
                    }
                }
            }
        }
        if (nl != null || (foundDoc ^ 1) == 0) {
            return nl;
        }
        kt = new KeyTable(doc, nscontext, name, template.getStylesheetRoot().getKeysComposed(), xctxt);
        this.m_key_tables.addElement(kt);
        if (doc == kt.getDocKey()) {
            return kt.getNodeSetDTMByKey(name, ref);
        }
        return nl;
    }
}
