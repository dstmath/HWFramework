package org.apache.xml.serializer;

import java.util.Hashtable;
import java.util.Properties;
import org.apache.xalan.templates.Constants;
import org.apache.xml.serializer.utils.MsgKey;
import org.apache.xml.serializer.utils.Utils;
import org.apache.xml.serializer.utils.WrappedRuntimeException;
import org.xml.sax.ContentHandler;

public final class SerializerFactory {
    private static Hashtable m_formats;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: org.apache.xml.serializer.SerializerFactory.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: org.apache.xml.serializer.SerializerFactory.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.xml.serializer.SerializerFactory.<clinit>():void");
    }

    private SerializerFactory() {
    }

    public static Serializer getSerializer(Properties format) {
        try {
            String method = format.getProperty(Constants.ATTRNAME_OUTPUT_METHOD);
            if (method == null) {
                throw new IllegalArgumentException(Utils.messages.createMessage(MsgKey.ER_FACTORY_PROPERTY_MISSING, new Object[]{Constants.ATTRNAME_OUTPUT_METHOD}));
            }
            String className = format.getProperty(OutputPropertiesFactory.S_KEY_CONTENT_HANDLER);
            if (className == null) {
                className = OutputPropertiesFactory.getDefaultMethodProperties(method).getProperty(OutputPropertiesFactory.S_KEY_CONTENT_HANDLER);
                if (className == null) {
                    throw new IllegalArgumentException(Utils.messages.createMessage(MsgKey.ER_FACTORY_PROPERTY_MISSING, new Object[]{OutputPropertiesFactory.S_KEY_CONTENT_HANDLER}));
                }
            }
            ClassLoader loader = ObjectFactory.findClassLoader();
            Class cls = ObjectFactory.findProviderClass(className, loader, true);
            Object obj = cls.newInstance();
            if (obj instanceof SerializationHandler) {
                Serializer ser = (Serializer) cls.newInstance();
                ser.setOutputFormat(format);
                return ser;
            } else if (obj instanceof ContentHandler) {
                SerializationHandler sh = (SerializationHandler) ObjectFactory.findProviderClass(SerializerConstants.DEFAULT_SAX_SERIALIZER, loader, true).newInstance();
                sh.setContentHandler((ContentHandler) obj);
                sh.setOutputFormat(format);
                return sh;
            } else {
                throw new Exception(Utils.messages.createMessage(MsgKey.ER_SERIALIZER_NOT_CONTENTHANDLER, new Object[]{className}));
            }
        } catch (Exception e) {
            throw new WrappedRuntimeException(e);
        }
    }
}
