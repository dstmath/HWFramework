package ohos.com.sun.org.apache.xml.internal.serialize;

import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import ohos.com.sun.org.apache.xerces.internal.utils.ObjectFactory;
import ohos.com.sun.org.apache.xerces.internal.utils.SecuritySupport;

public abstract class SerializerFactory {
    public static final String FactoriesProperty = "com.sun.org.apache.xml.internal.serialize.factories";
    private static final Map<String, SerializerFactory> _factories = Collections.synchronizedMap(new HashMap());

    /* access modifiers changed from: protected */
    public abstract String getSupportedMethod();

    public abstract Serializer makeSerializer(OutputStream outputStream, OutputFormat outputFormat) throws UnsupportedEncodingException;

    public abstract Serializer makeSerializer(Writer writer, OutputFormat outputFormat);

    public abstract Serializer makeSerializer(OutputFormat outputFormat);

    static {
        registerSerializerFactory(new SerializerFactoryImpl("xml"));
        registerSerializerFactory(new SerializerFactoryImpl("html"));
        registerSerializerFactory(new SerializerFactoryImpl("xhtml"));
        registerSerializerFactory(new SerializerFactoryImpl("text"));
        String systemProperty = SecuritySupport.getSystemProperty(FactoriesProperty);
        if (systemProperty != null) {
            StringTokenizer stringTokenizer = new StringTokenizer(systemProperty, " ;,:");
            while (stringTokenizer.hasMoreTokens()) {
                try {
                    SerializerFactory serializerFactory = (SerializerFactory) ObjectFactory.newInstance(stringTokenizer.nextToken(), true);
                    if (_factories.containsKey(serializerFactory.getSupportedMethod())) {
                        _factories.put(serializerFactory.getSupportedMethod(), serializerFactory);
                    }
                } catch (Exception unused) {
                }
            }
        }
    }

    public static void registerSerializerFactory(SerializerFactory serializerFactory) {
        synchronized (_factories) {
            _factories.put(serializerFactory.getSupportedMethod(), serializerFactory);
        }
    }

    public static SerializerFactory getSerializerFactory(String str) {
        return _factories.get(str);
    }
}
