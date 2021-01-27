package ohos.com.sun.org.apache.xml.internal.serializer;

import java.util.Properties;
import ohos.com.sun.org.apache.xalan.internal.templates.Constants;
import ohos.com.sun.org.apache.xalan.internal.utils.ObjectFactory;
import ohos.com.sun.org.apache.xml.internal.serializer.utils.MsgKey;
import ohos.com.sun.org.apache.xml.internal.serializer.utils.Utils;
import ohos.com.sun.org.apache.xml.internal.serializer.utils.WrappedRuntimeException;
import ohos.org.xml.sax.ContentHandler;

public final class SerializerFactory {
    private SerializerFactory() {
    }

    public static Serializer getSerializer(Properties properties) {
        try {
            String property = properties.getProperty(Constants.ATTRNAME_OUTPUT_METHOD);
            if (property != null) {
                String property2 = properties.getProperty(OutputPropertiesFactory.S_KEY_CONTENT_HANDLER);
                if (property2 == null && (property2 = OutputPropertiesFactory.getDefaultMethodProperties(property).getProperty(OutputPropertiesFactory.S_KEY_CONTENT_HANDLER)) == null) {
                    throw new IllegalArgumentException(Utils.messages.createMessage(MsgKey.ER_FACTORY_PROPERTY_MISSING, new Object[]{OutputPropertiesFactory.S_KEY_CONTENT_HANDLER}));
                }
                Class<?> findProviderClass = ObjectFactory.findProviderClass(property2, true);
                Object newInstance = findProviderClass.newInstance();
                if (newInstance instanceof SerializationHandler) {
                    Serializer serializer = (Serializer) findProviderClass.newInstance();
                    serializer.setOutputFormat(properties);
                    return serializer;
                } else if (newInstance instanceof ContentHandler) {
                    SerializationHandler serializationHandler = (SerializationHandler) ObjectFactory.findProviderClass(SerializerConstants.DEFAULT_SAX_SERIALIZER, true).newInstance();
                    serializationHandler.setContentHandler((ContentHandler) newInstance);
                    serializationHandler.setOutputFormat(properties);
                    return serializationHandler;
                } else {
                    throw new Exception(Utils.messages.createMessage("ER_SERIALIZER_NOT_CONTENTHANDLER", new Object[]{property2}));
                }
            } else {
                throw new IllegalArgumentException(Utils.messages.createMessage(MsgKey.ER_FACTORY_PROPERTY_MISSING, new Object[]{Constants.ATTRNAME_OUTPUT_METHOD}));
            }
        } catch (Exception e) {
            throw new WrappedRuntimeException(e);
        }
    }
}
