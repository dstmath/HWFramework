package javax.xml.transform;

public abstract class TransformerFactory {
    public abstract Source getAssociatedStylesheet(Source source, String str, String str2, String str3) throws TransformerConfigurationException;

    public abstract Object getAttribute(String str);

    public abstract ErrorListener getErrorListener();

    public abstract boolean getFeature(String str);

    public abstract URIResolver getURIResolver();

    public abstract Templates newTemplates(Source source) throws TransformerConfigurationException;

    public abstract Transformer newTransformer() throws TransformerConfigurationException;

    public abstract Transformer newTransformer(Source source) throws TransformerConfigurationException;

    public abstract void setAttribute(String str, Object obj);

    public abstract void setErrorListener(ErrorListener errorListener);

    public abstract void setFeature(String str, boolean z) throws TransformerConfigurationException;

    public abstract void setURIResolver(URIResolver uRIResolver);

    protected TransformerFactory() {
    }

    public static TransformerFactory newInstance() throws TransformerFactoryConfigurationError {
        try {
            return (TransformerFactory) Class.forName("org.apache.xalan.processor.TransformerFactoryImpl").newInstance();
        } catch (Exception e) {
            throw new NoClassDefFoundError("org.apache.xalan.processor.TransformerFactoryImpl");
        }
    }

    public static TransformerFactory newInstance(String factoryClassName, ClassLoader classLoader) throws TransformerFactoryConfigurationError {
        Class<?> type;
        if (factoryClassName != null) {
            if (classLoader == null) {
                classLoader = Thread.currentThread().getContextClassLoader();
            }
            if (classLoader != null) {
                try {
                    type = classLoader.loadClass(factoryClassName);
                } catch (ClassNotFoundException e) {
                    throw new TransformerFactoryConfigurationError((Exception) e);
                } catch (InstantiationException e2) {
                    throw new TransformerFactoryConfigurationError((Exception) e2);
                } catch (IllegalAccessException e3) {
                    throw new TransformerFactoryConfigurationError((Exception) e3);
                }
            } else {
                type = Class.forName(factoryClassName);
            }
            return (TransformerFactory) type.newInstance();
        }
        throw new TransformerFactoryConfigurationError("factoryClassName == null");
    }
}
