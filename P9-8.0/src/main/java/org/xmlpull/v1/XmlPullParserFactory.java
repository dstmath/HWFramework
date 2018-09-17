package org.xmlpull.v1;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

public class XmlPullParserFactory {
    public static final String PROPERTY_NAME = "org.xmlpull.v1.XmlPullParserFactory";
    protected String classNamesLocation = null;
    protected HashMap<String, Boolean> features = new HashMap();
    protected ArrayList parserClasses = new ArrayList();
    protected ArrayList serializerClasses = new ArrayList();

    protected XmlPullParserFactory() {
        try {
            this.parserClasses.add(Class.forName("org.kxml2.io.KXmlParser"));
            this.serializerClasses.add(Class.forName("org.kxml2.io.KXmlSerializer"));
        } catch (ClassNotFoundException e) {
            throw new AssertionError();
        }
    }

    public void setFeature(String name, boolean state) throws XmlPullParserException {
        this.features.put(name, Boolean.valueOf(state));
    }

    public boolean getFeature(String name) {
        Boolean value = (Boolean) this.features.get(name);
        return value != null ? value.booleanValue() : false;
    }

    public void setNamespaceAware(boolean awareness) {
        this.features.put(XmlPullParser.FEATURE_PROCESS_NAMESPACES, Boolean.valueOf(awareness));
    }

    public boolean isNamespaceAware() {
        return getFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES);
    }

    public void setValidating(boolean validating) {
        this.features.put(XmlPullParser.FEATURE_VALIDATION, Boolean.valueOf(validating));
    }

    public boolean isValidating() {
        return getFeature(XmlPullParser.FEATURE_VALIDATION);
    }

    public XmlPullParser newPullParser() throws XmlPullParserException {
        XmlPullParser pp = getParserInstance();
        for (Entry<String, Boolean> entry : this.features.entrySet()) {
            if (((Boolean) entry.getValue()).booleanValue()) {
                pp.setFeature((String) entry.getKey(), ((Boolean) entry.getValue()).booleanValue());
            }
        }
        return pp;
    }

    private XmlPullParser getParserInstance() throws XmlPullParserException {
        ArrayList exceptions = null;
        if (!(this.parserClasses == null || (this.parserClasses.isEmpty() ^ 1) == 0)) {
            exceptions = new ArrayList();
            for (Object o : this.parserClasses) {
                if (o != null) {
                    try {
                        return (XmlPullParser) ((Class) o).newInstance();
                    } catch (InstantiationException e) {
                        exceptions.add(e);
                    } catch (IllegalAccessException e2) {
                        exceptions.add(e2);
                    } catch (ClassCastException e3) {
                        exceptions.add(e3);
                    }
                }
            }
        }
        throw newInstantiationException("Invalid parser class list", exceptions);
    }

    private XmlSerializer getSerializerInstance() throws XmlPullParserException {
        ArrayList exceptions = null;
        if (!(this.serializerClasses == null || (this.serializerClasses.isEmpty() ^ 1) == 0)) {
            exceptions = new ArrayList();
            for (Object o : this.serializerClasses) {
                if (o != null) {
                    try {
                        return (XmlSerializer) ((Class) o).newInstance();
                    } catch (InstantiationException e) {
                        exceptions.add(e);
                    } catch (IllegalAccessException e2) {
                        exceptions.add(e2);
                    } catch (ClassCastException e3) {
                        exceptions.add(e3);
                    }
                }
            }
        }
        throw newInstantiationException("Invalid serializer class list", exceptions);
    }

    private static XmlPullParserException newInstantiationException(String message, ArrayList<Exception> exceptions) {
        if (exceptions == null || exceptions.isEmpty()) {
            return new XmlPullParserException(message);
        }
        XmlPullParserException exception = new XmlPullParserException(message);
        for (Exception ex : exceptions) {
            exception.addSuppressed(ex);
        }
        return exception;
    }

    public XmlSerializer newSerializer() throws XmlPullParserException {
        return getSerializerInstance();
    }

    public static XmlPullParserFactory newInstance() throws XmlPullParserException {
        return new XmlPullParserFactory();
    }

    public static XmlPullParserFactory newInstance(String unused, Class unused2) throws XmlPullParserException {
        return newInstance();
    }
}
