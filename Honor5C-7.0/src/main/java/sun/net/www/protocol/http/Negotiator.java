package sun.net.www.protocol.http;

import java.io.IOException;

public abstract class Negotiator {
    public abstract byte[] firstToken() throws IOException;

    public abstract byte[] nextToken(byte[] bArr) throws IOException;

    static Negotiator getNegotiator(HttpCallerInfo hci) {
        try {
            try {
                return (Negotiator) Class.forName("sun.net.www.protocol.http.spnego.NegotiatorImpl", true, null).getConstructor(HttpCallerInfo.class).newInstance(hci);
            } catch (ReflectiveOperationException roe) {
                finest(roe);
                Throwable t = roe.getCause();
                if (t != null && (t instanceof Exception)) {
                    finest((Exception) t);
                }
                return null;
            }
        } catch (ClassNotFoundException cnfe) {
            finest(cnfe);
            return null;
        } catch (Object roe2) {
            throw new AssertionError(roe2);
        }
    }

    private static void finest(Exception e) {
        HttpURLConnection.getHttpLogger().finest("NegotiateAuthentication: " + e);
    }
}
