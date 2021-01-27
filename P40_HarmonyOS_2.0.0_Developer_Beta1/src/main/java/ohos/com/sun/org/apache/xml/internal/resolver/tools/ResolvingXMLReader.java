package ohos.com.sun.org.apache.xml.internal.resolver.tools;

import ohos.com.sun.org.apache.xml.internal.resolver.CatalogManager;
import ohos.javax.xml.parsers.SAXParserFactory;
import ohos.jdk.xml.internal.JdkXmlUtils;

public class ResolvingXMLReader extends ResolvingXMLFilter {
    public static boolean namespaceAware = true;
    public static boolean validating = false;

    public ResolvingXMLReader() {
        SAXParserFactory sAXFactory = JdkXmlUtils.getSAXFactory(this.catalogManager.overrideDefaultParser());
        sAXFactory.setValidating(validating);
        try {
            setParent(sAXFactory.newSAXParser().getXMLReader());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ResolvingXMLReader(CatalogManager catalogManager) {
        super(catalogManager);
        SAXParserFactory sAXFactory = JdkXmlUtils.getSAXFactory(this.catalogManager.overrideDefaultParser());
        sAXFactory.setValidating(validating);
        try {
            setParent(sAXFactory.newSAXParser().getXMLReader());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
