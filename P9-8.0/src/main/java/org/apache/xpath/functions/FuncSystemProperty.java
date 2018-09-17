package org.apache.xpath.functions;

import java.io.BufferedInputStream;
import java.util.Properties;
import javax.xml.transform.TransformerException;
import org.apache.xml.utils.Constants;
import org.apache.xml.utils.WrappedRuntimeException;
import org.apache.xpath.XPathContext;
import org.apache.xpath.objects.XObject;
import org.apache.xpath.objects.XString;
import org.apache.xpath.res.XPATHErrorResources;

public class FuncSystemProperty extends FunctionOneArg {
    static final String XSLT_PROPERTIES = "org/apache/xalan/res/XSLTInfo.properties";
    static final long serialVersionUID = 3694874980992204867L;

    public XObject execute(XPathContext xctxt) throws TransformerException {
        String fullName = this.m_arg0.execute(xctxt).str();
        int indexOfNSSep = fullName.indexOf(58);
        String result = null;
        String propName = "";
        Properties xsltInfo = new Properties();
        loadPropertyFile("org/apache/xalan/res/XSLTInfo.properties", xsltInfo);
        if (indexOfNSSep > 0) {
            String namespace = xctxt.getNamespaceContext().getNamespaceForPrefix(indexOfNSSep >= 0 ? fullName.substring(0, indexOfNSSep) : "");
            if (indexOfNSSep < 0) {
                propName = fullName;
            } else {
                propName = fullName.substring(indexOfNSSep + 1);
            }
            if (namespace.startsWith("http://www.w3.org/XSL/Transform") || namespace.equals(Constants.S_XSLNAMESPACEURL)) {
                result = xsltInfo.getProperty(propName);
                if (result == null) {
                    warn(xctxt, XPATHErrorResources.WG_PROPERTY_NOT_SUPPORTED, new Object[]{fullName});
                    return XString.EMPTYSTRING;
                }
            }
            warn(xctxt, XPATHErrorResources.WG_DONT_DO_ANYTHING_WITH_NS, new Object[]{namespace, fullName});
            try {
                if (xctxt.isSecureProcessing()) {
                    warn(xctxt, XPATHErrorResources.WG_SECURITY_EXCEPTION, new Object[]{fullName});
                } else {
                    result = System.getProperty(propName);
                }
                if (result == null) {
                    return XString.EMPTYSTRING;
                }
            } catch (SecurityException e) {
                warn(xctxt, XPATHErrorResources.WG_SECURITY_EXCEPTION, new Object[]{fullName});
                return XString.EMPTYSTRING;
            }
        }
        try {
            if (xctxt.isSecureProcessing()) {
                warn(xctxt, XPATHErrorResources.WG_SECURITY_EXCEPTION, new Object[]{fullName});
            } else {
                result = System.getProperty(fullName);
            }
            if (result == null) {
                return XString.EMPTYSTRING;
            }
        } catch (SecurityException e2) {
            warn(xctxt, XPATHErrorResources.WG_SECURITY_EXCEPTION, new Object[]{fullName});
            return XString.EMPTYSTRING;
        }
        if (!propName.equals("version") || result.length() <= 0) {
            return new XString(result);
        }
        try {
            return new XString(SerializerConstants.XMLVERSION10);
        } catch (Exception e3) {
            return new XString(result);
        }
    }

    public void loadPropertyFile(String file, Properties target) {
        try {
            BufferedInputStream bis = new BufferedInputStream(SecuritySupport.getInstance().getResourceAsStream(ObjectFactory.findClassLoader(), file));
            target.load(bis);
            bis.close();
        } catch (Exception ex) {
            throw new WrappedRuntimeException(ex);
        }
    }
}
