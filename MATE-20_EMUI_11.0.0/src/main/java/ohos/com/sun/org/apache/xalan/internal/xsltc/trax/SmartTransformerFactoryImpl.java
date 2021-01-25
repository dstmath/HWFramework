package ohos.com.sun.org.apache.xalan.internal.xsltc.trax;

import ohos.com.sun.org.apache.xalan.internal.utils.ObjectFactory;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ErrorMsg;
import ohos.com.sun.org.apache.xerces.internal.impl.Constants;
import ohos.javax.xml.transform.ErrorListener;
import ohos.javax.xml.transform.Source;
import ohos.javax.xml.transform.Templates;
import ohos.javax.xml.transform.Transformer;
import ohos.javax.xml.transform.TransformerConfigurationException;
import ohos.javax.xml.transform.TransformerException;
import ohos.javax.xml.transform.URIResolver;
import ohos.javax.xml.transform.sax.SAXTransformerFactory;
import ohos.javax.xml.transform.sax.TemplatesHandler;
import ohos.javax.xml.transform.sax.TransformerHandler;
import ohos.org.xml.sax.XMLFilter;

public class SmartTransformerFactoryImpl extends SAXTransformerFactory {
    private static final String CLASS_NAME = "SmartTransformerFactoryImpl";
    private SAXTransformerFactory _currFactory = null;
    private ErrorListener _errorlistener = null;
    private URIResolver _uriresolver = null;
    private SAXTransformerFactory _xalanFactory = null;
    private SAXTransformerFactory _xsltcFactory = null;
    private boolean featureSecureProcessing = false;

    private void createXSLTCTransformerFactory() {
        this._xsltcFactory = new TransformerFactoryImpl();
        this._currFactory = this._xsltcFactory;
    }

    private void createXalanTransformerFactory() {
        try {
            this._xalanFactory = (SAXTransformerFactory) ObjectFactory.findProviderClass("com.sun.org.apache.xalan.internal.processor.TransformerFactoryImpl", true).newInstance();
        } catch (ClassNotFoundException unused) {
            System.err.println("ohos.com.sun.org.apache.xalan.internal.xsltc.trax.SmartTransformerFactoryImpl could not create an com.sun.org.apache.xalan.internal.processor.TransformerFactoryImpl.");
        } catch (InstantiationException unused2) {
            System.err.println("ohos.com.sun.org.apache.xalan.internal.xsltc.trax.SmartTransformerFactoryImpl could not create an com.sun.org.apache.xalan.internal.processor.TransformerFactoryImpl.");
        } catch (IllegalAccessException unused3) {
            System.err.println("ohos.com.sun.org.apache.xalan.internal.xsltc.trax.SmartTransformerFactoryImpl could not create an com.sun.org.apache.xalan.internal.processor.TransformerFactoryImpl.");
        }
        this._currFactory = this._xalanFactory;
    }

    public void setErrorListener(ErrorListener errorListener) throws IllegalArgumentException {
        this._errorlistener = errorListener;
    }

    public ErrorListener getErrorListener() {
        return this._errorlistener;
    }

    public Object getAttribute(String str) throws IllegalArgumentException {
        if (str.equals(TransformerFactoryImpl.TRANSLET_NAME) || str.equals("debug")) {
            if (this._xsltcFactory == null) {
                createXSLTCTransformerFactory();
            }
            return this._xsltcFactory.getAttribute(str);
        }
        if (this._xalanFactory == null) {
            createXalanTransformerFactory();
        }
        return this._xalanFactory.getAttribute(str);
    }

    public void setAttribute(String str, Object obj) throws IllegalArgumentException {
        if (str.equals(TransformerFactoryImpl.TRANSLET_NAME) || str.equals("debug")) {
            if (this._xsltcFactory == null) {
                createXSLTCTransformerFactory();
            }
            this._xsltcFactory.setAttribute(str, obj);
            return;
        }
        if (this._xalanFactory == null) {
            createXalanTransformerFactory();
        }
        this._xalanFactory.setAttribute(str, obj);
    }

    public void setFeature(String str, boolean z) throws TransformerConfigurationException {
        if (str == null) {
            throw new NullPointerException(new ErrorMsg(ErrorMsg.JAXP_SET_FEATURE_NULL_NAME).toString());
        } else if (str.equals(Constants.FEATURE_SECURE_PROCESSING)) {
            this.featureSecureProcessing = z;
        } else {
            throw new TransformerConfigurationException(new ErrorMsg(ErrorMsg.JAXP_UNSUPPORTED_FEATURE, str).toString());
        }
    }

    public boolean getFeature(String str) {
        String[] strArr = {"http://ohos.javax.xml.transform.dom.DOMSource/feature", "http://ohos.javax.xml.transform.dom.DOMResult/feature", "http://ohos.javax.xml.transform.sax.SAXSource/feature", "http://ohos.javax.xml.transform.sax.SAXResult/feature", "http://ohos.javax.xml.transform.stream.StreamSource/feature", "http://ohos.javax.xml.transform.stream.StreamResult/feature"};
        if (str != null) {
            for (String str2 : strArr) {
                if (str.equals(str2)) {
                    return true;
                }
            }
            if (str.equals(Constants.FEATURE_SECURE_PROCESSING)) {
                return this.featureSecureProcessing;
            }
            return false;
        }
        throw new NullPointerException(new ErrorMsg(ErrorMsg.JAXP_GET_FEATURE_NULL_NAME).toString());
    }

    public URIResolver getURIResolver() {
        return this._uriresolver;
    }

    public void setURIResolver(URIResolver uRIResolver) {
        this._uriresolver = uRIResolver;
    }

    public Source getAssociatedStylesheet(Source source, String str, String str2, String str3) throws TransformerConfigurationException {
        if (this._currFactory == null) {
            createXSLTCTransformerFactory();
        }
        return this._currFactory.getAssociatedStylesheet(source, str, str2, str3);
    }

    public Transformer newTransformer() throws TransformerConfigurationException {
        if (this._xalanFactory == null) {
            createXalanTransformerFactory();
        }
        ErrorListener errorListener = this._errorlistener;
        if (errorListener != null) {
            this._xalanFactory.setErrorListener(errorListener);
        }
        URIResolver uRIResolver = this._uriresolver;
        if (uRIResolver != null) {
            this._xalanFactory.setURIResolver(uRIResolver);
        }
        this._currFactory = this._xalanFactory;
        return this._currFactory.newTransformer();
    }

    public Transformer newTransformer(Source source) throws TransformerConfigurationException {
        if (this._xalanFactory == null) {
            createXalanTransformerFactory();
        }
        ErrorListener errorListener = this._errorlistener;
        if (errorListener != null) {
            this._xalanFactory.setErrorListener(errorListener);
        }
        URIResolver uRIResolver = this._uriresolver;
        if (uRIResolver != null) {
            this._xalanFactory.setURIResolver(uRIResolver);
        }
        this._currFactory = this._xalanFactory;
        return this._currFactory.newTransformer(source);
    }

    public Templates newTemplates(Source source) throws TransformerConfigurationException {
        if (this._xsltcFactory == null) {
            createXSLTCTransformerFactory();
        }
        ErrorListener errorListener = this._errorlistener;
        if (errorListener != null) {
            this._xsltcFactory.setErrorListener(errorListener);
        }
        URIResolver uRIResolver = this._uriresolver;
        if (uRIResolver != null) {
            this._xsltcFactory.setURIResolver(uRIResolver);
        }
        this._currFactory = this._xsltcFactory;
        return this._currFactory.newTemplates(source);
    }

    public TemplatesHandler newTemplatesHandler() throws TransformerConfigurationException {
        if (this._xsltcFactory == null) {
            createXSLTCTransformerFactory();
        }
        ErrorListener errorListener = this._errorlistener;
        if (errorListener != null) {
            this._xsltcFactory.setErrorListener(errorListener);
        }
        URIResolver uRIResolver = this._uriresolver;
        if (uRIResolver != null) {
            this._xsltcFactory.setURIResolver(uRIResolver);
        }
        return this._xsltcFactory.newTemplatesHandler();
    }

    public TransformerHandler newTransformerHandler() throws TransformerConfigurationException {
        if (this._xalanFactory == null) {
            createXalanTransformerFactory();
        }
        ErrorListener errorListener = this._errorlistener;
        if (errorListener != null) {
            this._xalanFactory.setErrorListener(errorListener);
        }
        URIResolver uRIResolver = this._uriresolver;
        if (uRIResolver != null) {
            this._xalanFactory.setURIResolver(uRIResolver);
        }
        return this._xalanFactory.newTransformerHandler();
    }

    public TransformerHandler newTransformerHandler(Source source) throws TransformerConfigurationException {
        if (this._xalanFactory == null) {
            createXalanTransformerFactory();
        }
        ErrorListener errorListener = this._errorlistener;
        if (errorListener != null) {
            this._xalanFactory.setErrorListener(errorListener);
        }
        URIResolver uRIResolver = this._uriresolver;
        if (uRIResolver != null) {
            this._xalanFactory.setURIResolver(uRIResolver);
        }
        return this._xalanFactory.newTransformerHandler(source);
    }

    public TransformerHandler newTransformerHandler(Templates templates) throws TransformerConfigurationException {
        if (this._xsltcFactory == null) {
            createXSLTCTransformerFactory();
        }
        ErrorListener errorListener = this._errorlistener;
        if (errorListener != null) {
            this._xsltcFactory.setErrorListener(errorListener);
        }
        URIResolver uRIResolver = this._uriresolver;
        if (uRIResolver != null) {
            this._xsltcFactory.setURIResolver(uRIResolver);
        }
        return this._xsltcFactory.newTransformerHandler(templates);
    }

    public XMLFilter newXMLFilter(Source source) throws TransformerConfigurationException {
        if (this._xsltcFactory == null) {
            createXSLTCTransformerFactory();
        }
        ErrorListener errorListener = this._errorlistener;
        if (errorListener != null) {
            this._xsltcFactory.setErrorListener(errorListener);
        }
        URIResolver uRIResolver = this._uriresolver;
        if (uRIResolver != null) {
            this._xsltcFactory.setURIResolver(uRIResolver);
        }
        Templates newTemplates = this._xsltcFactory.newTemplates(source);
        if (newTemplates == null) {
            return null;
        }
        return newXMLFilter(newTemplates);
    }

    public XMLFilter newXMLFilter(Templates templates) throws TransformerConfigurationException {
        try {
            return new TrAXFilter(templates);
        } catch (TransformerConfigurationException e) {
            if (this._xsltcFactory == null) {
                createXSLTCTransformerFactory();
            }
            ErrorListener errorListener = this._xsltcFactory.getErrorListener();
            if (errorListener != null) {
                try {
                    errorListener.fatalError(e);
                    return null;
                } catch (TransformerException e2) {
                    new TransformerConfigurationException(e2);
                    throw e;
                }
            }
            throw e;
        }
    }
}
