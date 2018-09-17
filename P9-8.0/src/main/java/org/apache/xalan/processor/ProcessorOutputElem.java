package org.apache.xalan.processor;

import java.util.Vector;
import javax.xml.transform.TransformerException;
import org.apache.xalan.templates.Constants;
import org.apache.xalan.templates.OutputProperties;
import org.apache.xml.serializer.OutputPropertiesFactory;
import org.apache.xml.utils.QName;
import org.apache.xml.utils.SystemIDResolver;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

class ProcessorOutputElem extends XSLTElementProcessor {
    static final long serialVersionUID = 3513742319582547590L;
    private OutputProperties m_outputProperties;

    ProcessorOutputElem() {
    }

    public void setCdataSectionElements(Vector newValue) {
        this.m_outputProperties.setQNameProperties(Constants.ATTRNAME_OUTPUT_CDATA_SECTION_ELEMENTS, newValue);
    }

    public void setDoctypePublic(String newValue) {
        this.m_outputProperties.setProperty(Constants.ATTRNAME_OUTPUT_DOCTYPE_PUBLIC, newValue);
    }

    public void setDoctypeSystem(String newValue) {
        this.m_outputProperties.setProperty(Constants.ATTRNAME_OUTPUT_DOCTYPE_SYSTEM, newValue);
    }

    public void setEncoding(String newValue) {
        this.m_outputProperties.setProperty("encoding", newValue);
    }

    public void setIndent(boolean newValue) {
        this.m_outputProperties.setBooleanProperty("indent", newValue);
    }

    public void setMediaType(String newValue) {
        this.m_outputProperties.setProperty(Constants.ATTRNAME_OUTPUT_MEDIATYPE, newValue);
    }

    public void setMethod(QName newValue) {
        this.m_outputProperties.setQNameProperty(Constants.ATTRNAME_OUTPUT_METHOD, newValue);
    }

    public void setOmitXmlDeclaration(boolean newValue) {
        this.m_outputProperties.setBooleanProperty("omit-xml-declaration", newValue);
    }

    public void setStandalone(boolean newValue) {
        this.m_outputProperties.setBooleanProperty(Constants.ATTRNAME_OUTPUT_STANDALONE, newValue);
    }

    public void setVersion(String newValue) {
        this.m_outputProperties.setProperty("version", newValue);
    }

    public void setForeignAttr(String attrUri, String attrLocalName, String attrRawName, String attrValue) {
        this.m_outputProperties.setProperty(new QName(attrUri, attrLocalName), attrValue);
    }

    public void addLiteralResultAttribute(String attrUri, String attrLocalName, String attrRawName, String attrValue) {
        this.m_outputProperties.setProperty(new QName(attrUri, attrLocalName), attrValue);
    }

    public void startElement(StylesheetHandler handler, String uri, String localName, String rawName, Attributes attributes) throws SAXException {
        this.m_outputProperties = new OutputProperties();
        this.m_outputProperties.setDOMBackPointer(handler.getOriginatingNode());
        this.m_outputProperties.setLocaterInfo(handler.getLocator());
        this.m_outputProperties.setUid(handler.nextUid());
        setPropertiesFromAttributes(handler, rawName, attributes, this);
        String entitiesFileName = (String) this.m_outputProperties.getProperties().get(OutputPropertiesFactory.S_KEY_ENTITIES);
        if (entitiesFileName != null) {
            try {
                this.m_outputProperties.getProperties().put(OutputPropertiesFactory.S_KEY_ENTITIES, SystemIDResolver.getAbsoluteURI(entitiesFileName, handler.getBaseIdentifier()));
            } catch (TransformerException te) {
                handler.error(te.getMessage(), te);
            }
        }
        handler.getStylesheet().setOutput(this.m_outputProperties);
        handler.getElemTemplateElement().appendChild(this.m_outputProperties);
        this.m_outputProperties = null;
    }
}
