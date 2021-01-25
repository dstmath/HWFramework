package ohos.com.sun.org.apache.xpath.internal.functions;

import java.io.BufferedInputStream;
import java.util.Properties;
import ohos.com.sun.org.apache.xalan.internal.utils.ObjectFactory;
import ohos.com.sun.org.apache.xalan.internal.utils.SecuritySupport;
import ohos.com.sun.org.apache.xml.internal.utils.WrappedRuntimeException;
import ohos.com.sun.org.apache.xpath.internal.XPathContext;
import ohos.com.sun.org.apache.xpath.internal.objects.XObject;
import ohos.com.sun.org.apache.xpath.internal.objects.XString;
import ohos.javax.xml.transform.TransformerException;

public class FuncSystemProperty extends FunctionOneArg {
    static final String XSLT_PROPERTIES = "com/sun/org/apache/xalan/internal/res/XSLTInfo.properties";
    static final long serialVersionUID = 3694874980992204867L;

    @Override // ohos.com.sun.org.apache.xpath.internal.functions.Function, ohos.com.sun.org.apache.xpath.internal.Expression
    public XObject execute(XPathContext xPathContext) throws TransformerException {
        String str;
        String str2;
        String str3 = this.m_arg0.execute(xPathContext).str();
        int indexOf = str3.indexOf(58);
        Properties properties = new Properties();
        loadPropertyFile(XSLT_PROPERTIES, properties);
        String str4 = "";
        if (indexOf > 0) {
            if (indexOf >= 0) {
                str4 = str3.substring(0, indexOf);
            }
            String namespaceForPrefix = xPathContext.getNamespaceContext().getNamespaceForPrefix(str4);
            if (indexOf < 0) {
                str2 = str3;
            } else {
                str2 = str3.substring(indexOf + 1);
            }
            if (namespaceForPrefix.startsWith("http://www.w3.org/XSL/Transform") || namespaceForPrefix.equals("http://www.w3.org/1999/XSL/Transform")) {
                str = properties.getProperty(str2);
                if (str == null) {
                    warn(xPathContext, "WG_PROPERTY_NOT_SUPPORTED", new Object[]{str3});
                    return XString.EMPTYSTRING;
                }
            } else {
                warn(xPathContext, "WG_DONT_DO_ANYTHING_WITH_NS", new Object[]{namespaceForPrefix, str3});
                try {
                    str = SecuritySupport.getSystemProperty(str2);
                    if (str == null) {
                        return XString.EMPTYSTRING;
                    }
                } catch (SecurityException unused) {
                    warn(xPathContext, "WG_SECURITY_EXCEPTION", new Object[]{str3});
                    return XString.EMPTYSTRING;
                }
            }
        } else {
            try {
                str = SecuritySupport.getSystemProperty(str3);
                if (str == null) {
                    return XString.EMPTYSTRING;
                }
                str2 = str4;
            } catch (SecurityException unused2) {
                warn(xPathContext, "WG_SECURITY_EXCEPTION", new Object[]{str3});
                return XString.EMPTYSTRING;
            }
        }
        if (!str2.equals("version") || str.length() <= 0) {
            return new XString(str);
        }
        try {
            return new XString("1.0");
        } catch (Exception unused3) {
            return new XString(str);
        }
    }

    public void loadPropertyFile(String str, Properties properties) {
        try {
            BufferedInputStream bufferedInputStream = new BufferedInputStream(SecuritySupport.getResourceAsStream(ObjectFactory.findClassLoader(), str));
            properties.load(bufferedInputStream);
            bufferedInputStream.close();
        } catch (Exception e) {
            throw new WrappedRuntimeException(e);
        }
    }
}
