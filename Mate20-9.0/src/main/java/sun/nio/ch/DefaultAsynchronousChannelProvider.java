package sun.nio.ch;

import java.nio.channels.spi.AsynchronousChannelProvider;

public class DefaultAsynchronousChannelProvider {
    private DefaultAsynchronousChannelProvider() {
    }

    private static AsynchronousChannelProvider createProvider(String cn) {
        try {
            try {
                return (AsynchronousChannelProvider) Class.forName(cn).newInstance();
            } catch (IllegalAccessException | InstantiationException x) {
                throw new AssertionError((Object) x);
            }
        } catch (ClassNotFoundException x2) {
            throw new AssertionError((Object) x2);
        }
    }

    public static AsynchronousChannelProvider create() {
        return createProvider("sun.nio.ch.LinuxAsynchronousChannelProvider");
    }
}
