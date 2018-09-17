package java.net;

import java.security.AccessController;
import sun.security.action.GetPropertyAction;

class DefaultDatagramSocketImplFactory {
    static Class<?> prefixImplClass;

    DefaultDatagramSocketImplFactory() {
    }

    static {
        prefixImplClass = null;
        try {
            String prefix = (String) AccessController.doPrivileged(new GetPropertyAction("impl.prefix", null));
            if (prefix != null) {
                prefixImplClass = Class.forName("java.net." + prefix + "DatagramSocketImpl");
            }
        } catch (Exception e) {
            System.err.println("Can't find class: java.net." + null + "DatagramSocketImpl: check impl.prefix property");
        }
    }

    static DatagramSocketImpl createDatagramSocketImpl(boolean isMulticast) throws SocketException {
        if (prefixImplClass == null) {
            return new PlainDatagramSocketImpl();
        }
        try {
            return (DatagramSocketImpl) prefixImplClass.newInstance();
        } catch (Exception e) {
            throw new SocketException("can't instantiate DatagramSocketImpl");
        }
    }
}
