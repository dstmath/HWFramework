package ohos.com.sun.org.apache.xerces.internal.jaxp.validation;

import ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler;
import ohos.com.sun.org.apache.xerces.internal.xni.XNIException;
import ohos.javax.xml.transform.dom.DOMResult;
import ohos.org.w3c.dom.CDATASection;
import ohos.org.w3c.dom.Comment;
import ohos.org.w3c.dom.DocumentType;
import ohos.org.w3c.dom.ProcessingInstruction;
import ohos.org.w3c.dom.Text;

interface DOMDocumentHandler extends XMLDocumentHandler {
    void cdata(CDATASection cDATASection) throws XNIException;

    void characters(Text text) throws XNIException;

    void comment(Comment comment) throws XNIException;

    void doctypeDecl(DocumentType documentType) throws XNIException;

    void processingInstruction(ProcessingInstruction processingInstruction) throws XNIException;

    void setDOMResult(DOMResult dOMResult);

    void setIgnoringCharacters(boolean z);
}
