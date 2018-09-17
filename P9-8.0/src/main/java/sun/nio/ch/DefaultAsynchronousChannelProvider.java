package sun.nio.ch;

import java.nio.channels.spi.AsynchronousChannelProvider;

public class DefaultAsynchronousChannelProvider {
    private DefaultAsynchronousChannelProvider() {
    }

    /* JADX WARNING: Removed duplicated region for block: B:8:0x0012 A:{Splitter: B:2:0x0004, ExcHandler: java.lang.IllegalAccessException (r2_0 'x' java.lang.Object)} */
    /* JADX WARNING: Missing block: B:8:0x0012, code:
            r2 = move-exception;
     */
    /* JADX WARNING: Missing block: B:10:0x0018, code:
            throw new java.lang.AssertionError(r2);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static AsynchronousChannelProvider createProvider(String cn) {
        try {
            try {
                return (AsynchronousChannelProvider) Class.forName(cn).newInstance();
            } catch (Object x) {
            }
        } catch (Object x2) {
            throw new AssertionError(x2);
        }
    }

    public static AsynchronousChannelProvider create() {
        return createProvider("sun.nio.ch.LinuxAsynchronousChannelProvider");
    }
}
