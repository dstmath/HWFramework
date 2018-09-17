package org.apache.xalan.templates;

import javax.xml.transform.TransformerException;
import org.apache.xalan.serialize.SerializerUtils;
import org.apache.xalan.transformer.ClonerToResultTree;
import org.apache.xalan.transformer.TransformerImpl;
import org.apache.xml.dtm.DTM;
import org.apache.xml.serializer.SerializationHandler;
import org.apache.xpath.XPathContext;
import org.xml.sax.SAXException;

public class ElemCopy extends ElemUse {
    static final long serialVersionUID = 5478580783896941384L;

    public int getXSLToken() {
        return 9;
    }

    public String getNodeName() {
        return Constants.ELEMNAME_COPY_STRING;
    }

    public void execute(TransformerImpl transformer) throws TransformerException {
        XPathContext xctxt = transformer.getXPathContext();
        try {
            int sourceNode = xctxt.getCurrentNode();
            xctxt.pushCurrentNode(sourceNode);
            DTM dtm = xctxt.getDTM(sourceNode);
            short nodeType = dtm.getNodeType(sourceNode);
            if ((short) 9 == nodeType || (short) 11 == nodeType) {
                super.execute(transformer);
                transformer.executeChildTemplates((ElemTemplateElement) this, true);
            } else {
                SerializationHandler rthandler = transformer.getSerializationHandler();
                ClonerToResultTree.cloneToResultTree(sourceNode, nodeType, dtm, rthandler, false);
                if ((short) 1 == nodeType) {
                    super.execute(transformer);
                    SerializerUtils.processNSDecls(rthandler, sourceNode, nodeType, dtm);
                    transformer.executeChildTemplates((ElemTemplateElement) this, true);
                    transformer.getResultTreeHandler().endElement(dtm.getNamespaceURI(sourceNode), dtm.getLocalName(sourceNode), dtm.getNodeName(sourceNode));
                }
            }
            xctxt.popCurrentNode();
        } catch (SAXException se) {
            throw new TransformerException(se);
        } catch (Throwable th) {
            xctxt.popCurrentNode();
        }
    }
}
