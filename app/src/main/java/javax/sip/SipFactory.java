package javax.sip;

import gov.nist.javax.sip.address.AddressFactoryImpl;
import gov.nist.javax.sip.header.HeaderFactoryImpl;
import gov.nist.javax.sip.message.MessageFactoryImpl;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import javax.sip.address.AddressFactory;
import javax.sip.header.HeaderFactory;
import javax.sip.message.MessageFactory;

public class SipFactory {
    private static final String IP_ADDRESS_PROP = "javax.sip.IP_ADDRESS";
    private static final String STACK_NAME_PROP = "javax.sip.STACK_NAME";
    private static SipFactory sSipFactory;
    private Map<String, SipStack> mNameSipStackMap;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: javax.sip.SipFactory.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: javax.sip.SipFactory.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: javax.sip.SipFactory.<clinit>():void");
    }

    public static synchronized SipFactory getInstance() {
        SipFactory sipFactory;
        synchronized (SipFactory.class) {
            if (sSipFactory == null) {
                sSipFactory = new SipFactory();
            }
            sipFactory = sSipFactory;
        }
        return sipFactory;
    }

    private SipFactory() {
        this.mNameSipStackMap = new HashMap();
    }

    public synchronized void resetFactory() {
        this.mNameSipStackMap.clear();
    }

    public synchronized SipStack createSipStack(Properties properties) throws PeerUnavailableException {
        SipStack sipStack;
        String name = properties.getProperty(IP_ADDRESS_PROP);
        if (name == null) {
            name = properties.getProperty(STACK_NAME_PROP);
            if (name == null) {
                throw new PeerUnavailableException("javax.sip.STACK_NAME property not found");
            }
        }
        sipStack = (SipStack) this.mNameSipStackMap.get(name);
        if (sipStack == null) {
            String implClassName = "gov.nist." + SipStack.class.getCanonicalName() + "Impl";
            try {
                sipStack = (SipStack) Class.forName(implClassName).asSubclass(SipStack.class).getConstructor(new Class[]{Properties.class}).newInstance(new Object[]{properties});
                this.mNameSipStackMap.put(name, sipStack);
            } catch (Exception e) {
                throw new PeerUnavailableException("Failed to initiate " + implClassName, e);
            }
        }
        return sipStack;
    }

    public AddressFactory createAddressFactory() throws PeerUnavailableException {
        try {
            return new AddressFactoryImpl();
        } catch (Exception e) {
            if (e instanceof PeerUnavailableException) {
                throw ((PeerUnavailableException) e);
            }
            throw new PeerUnavailableException("Failed to create AddressFactory", e);
        }
    }

    public HeaderFactory createHeaderFactory() throws PeerUnavailableException {
        try {
            return new HeaderFactoryImpl();
        } catch (Exception e) {
            if (e instanceof PeerUnavailableException) {
                throw ((PeerUnavailableException) e);
            }
            throw new PeerUnavailableException("Failed to create HeaderFactory", e);
        }
    }

    public MessageFactory createMessageFactory() throws PeerUnavailableException {
        try {
            return new MessageFactoryImpl();
        } catch (Exception e) {
            if (e instanceof PeerUnavailableException) {
                throw ((PeerUnavailableException) e);
            }
            throw new PeerUnavailableException("Failed to create MessageFactory", e);
        }
    }
}
