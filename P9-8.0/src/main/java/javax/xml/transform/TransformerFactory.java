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
        String className = "org.apache.xalan.processor.TransformerFactoryImpl";
        try {
            return (TransformerFactory) Class.forName(className).newInstance();
        } catch (Exception e) {
            throw new NoClassDefFoundError(className);
        }
    }

    public static TransformerFactory newInstance(String factoryClassName, ClassLoader classLoader) throws TransformerFactoryConfigurationError {
        if (factoryClassName == null) {
            throw new TransformerFactoryConfigurationError("factoryClassName == null");
        }
        Class<?> type;
        if (classLoader == null) {
            classLoader = Thread.currentThread().getContextClassLoader();
        }
        if (classLoader != null) {
            try {
                type = classLoader.loadClass(factoryClassName);
            } catch (Exception e) {
                throw new TransformerFactoryConfigurationError(e);
            } catch (Exception e2) {
                throw new TransformerFactoryConfigurationError(e2);
            } catch (Exception e3) {
                throw new TransformerFactoryConfigurationError(e3);
            }
        }
        type = Class.forName(factoryClassName);
        return (TransformerFactory) type.newInstance();
    }
}
