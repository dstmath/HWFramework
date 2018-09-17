package org.apache.xml.dtm;

import javax.xml.transform.Source;
import org.apache.xml.res.XMLErrorResources;
import org.apache.xml.res.XMLMessages;
import org.apache.xml.utils.PrefixResolver;
import org.apache.xml.utils.XMLStringFactory;
import org.w3c.dom.Node;

public abstract class DTMManager {
    public static final int IDENT_DTM_DEFAULT = -65536;
    public static final int IDENT_DTM_NODE_BITS = 16;
    public static final int IDENT_MAX_DTMS = 65536;
    public static final int IDENT_NODE_DEFAULT = 65535;
    private static boolean debug = false;
    private static String defaultClassName = null;
    private static final String defaultPropName = "org.apache.xml.dtm.DTMManager";
    public boolean m_incremental;
    public boolean m_source_location;
    protected XMLStringFactory m_xsf;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: org.apache.xml.dtm.DTMManager.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: org.apache.xml.dtm.DTMManager.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: org.apache.xml.dtm.DTMManager.<clinit>():void");
    }

    public abstract DTMIterator createDTMIterator(int i);

    public abstract DTMIterator createDTMIterator(int i, DTMFilter dTMFilter, boolean z);

    public abstract DTMIterator createDTMIterator(Object obj, int i);

    public abstract DTMIterator createDTMIterator(String str, PrefixResolver prefixResolver);

    public abstract DTM createDocumentFragment();

    public abstract DTM getDTM(int i);

    public abstract DTM getDTM(Source source, boolean z, DTMWSFilter dTMWSFilter, boolean z2, boolean z3);

    public abstract int getDTMHandleFromNode(Node node);

    public abstract int getDTMIdentity(DTM dtm);

    public abstract boolean release(DTM dtm, boolean z);

    protected DTMManager() {
        this.m_xsf = null;
        this.m_incremental = false;
        this.m_source_location = false;
    }

    public XMLStringFactory getXMLStringFactory() {
        return this.m_xsf;
    }

    public void setXMLStringFactory(XMLStringFactory xsf) {
        this.m_xsf = xsf;
    }

    public static DTMManager newInstance(XMLStringFactory xsf) throws DTMConfigurationException {
        try {
            DTMManager factoryImpl = (DTMManager) ObjectFactory.createObject(defaultPropName, defaultClassName);
            if (factoryImpl == null) {
                throw new DTMConfigurationException(XMLMessages.createXMLMessage(XMLErrorResources.ER_NO_DEFAULT_IMPL, null));
            }
            factoryImpl.setXMLStringFactory(xsf);
            return factoryImpl;
        } catch (ConfigurationError e) {
            throw new DTMConfigurationException(XMLMessages.createXMLMessage(XMLErrorResources.ER_NO_DEFAULT_IMPL, null), e.getException());
        }
    }

    public boolean getIncremental() {
        return this.m_incremental;
    }

    public void setIncremental(boolean incremental) {
        this.m_incremental = incremental;
    }

    public boolean getSource_location() {
        return this.m_source_location;
    }

    public void setSource_location(boolean sourceLocation) {
        this.m_source_location = sourceLocation;
    }

    public int getDTMIdentityMask() {
        return IDENT_DTM_DEFAULT;
    }

    public int getNodeIdentityMask() {
        return IDENT_NODE_DEFAULT;
    }
}
