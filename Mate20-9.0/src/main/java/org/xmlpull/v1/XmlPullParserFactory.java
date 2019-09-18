package org.xmlpull.v1;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class XmlPullParserFactory {
    public static final String PROPERTY_NAME = "org.xmlpull.v1.XmlPullParserFactory";
    protected String classNamesLocation = null;
    protected HashMap<String, Boolean> features = new HashMap<>();
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
        Boolean value = this.features.get(name);
        if (value != null) {
            return value.booleanValue();
        }
        return false;
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
        for (Map.Entry<String, Boolean> entry : this.features.entrySet()) {
            if (entry.getValue().booleanValue()) {
                pp.setFeature(entry.getKey(), entry.getValue().booleanValue());
            }
        }
        return pp;
    }

    private XmlPullParser getParserInstance() throws XmlPullParserException {
        ArrayList<Exception> exceptions = null;
        if (this.parserClasses != null && !this.parserClasses.isEmpty()) {
            exceptions = new ArrayList<>();
            Iterator it = this.parserClasses.iterator();
            while (it.hasNext()) {
                Object o = it.next();
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
        ArrayList<Exception> exceptions = null;
        if (this.serializerClasses != null && !this.serializerClasses.isEmpty()) {
            exceptions = new ArrayList<>();
            Iterator it = this.serializerClasses.iterator();
            while (it.hasNext()) {
                Object o = it.next();
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
        Iterator<Exception> it = exceptions.iterator();
        while (it.hasNext()) {
            exception.addSuppressed(it.next());
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
