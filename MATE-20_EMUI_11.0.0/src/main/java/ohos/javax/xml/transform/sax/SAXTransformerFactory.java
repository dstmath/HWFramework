package ohos.javax.xml.transform.sax;

import ohos.javax.xml.transform.Source;
import ohos.javax.xml.transform.Templates;
import ohos.javax.xml.transform.TransformerConfigurationException;
import ohos.javax.xml.transform.TransformerFactory;
import ohos.org.xml.sax.XMLFilter;

public abstract class SAXTransformerFactory extends TransformerFactory {
    public static final String FEATURE = "http://ohos.javax.xml.transform.sax.SAXTransformerFactory/feature";
    public static final String FEATURE_XMLFILTER = "http://ohos.javax.xml.transform.sax.SAXTransformerFactory/feature/xmlfilter";

    public abstract TemplatesHandler newTemplatesHandler() throws TransformerConfigurationException;

    public abstract TransformerHandler newTransformerHandler() throws TransformerConfigurationException;

    public abstract TransformerHandler newTransformerHandler(Source source) throws TransformerConfigurationException;

    public abstract TransformerHandler newTransformerHandler(Templates templates) throws TransformerConfigurationException;

    public abstract XMLFilter newXMLFilter(Source source) throws TransformerConfigurationException;

    public abstract XMLFilter newXMLFilter(Templates templates) throws TransformerConfigurationException;

    protected SAXTransformerFactory() {
    }
}
