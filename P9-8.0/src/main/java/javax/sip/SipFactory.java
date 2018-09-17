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
    private static SipFactory sSipFactory = null;
    private Map<String, SipStack> mNameSipStackMap = new HashMap();

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
