package jcifs.smb;

public abstract class NtlmAuthenticator {
    private static NtlmAuthenticator auth;
    private SmbAuthException sae;
    private String url;

    private void reset() {
        this.url = null;
        this.sae = null;
    }

    public static synchronized void setDefault(NtlmAuthenticator a) {
        synchronized (NtlmAuthenticator.class) {
            if (auth == null) {
                auth = a;
            }
        }
    }

    protected final String getRequestingURL() {
        return this.url;
    }

    protected final SmbAuthException getRequestingException() {
        return this.sae;
    }

    public static NtlmPasswordAuthentication requestNtlmPasswordAuthentication(String url, SmbAuthException sae) {
        if (auth == null) {
            return null;
        }
        NtlmPasswordAuthentication ntlmPasswordAuthentication;
        synchronized (auth) {
            auth.url = url;
            auth.sae = sae;
            ntlmPasswordAuthentication = auth.getNtlmPasswordAuthentication();
        }
        return ntlmPasswordAuthentication;
    }

    protected NtlmPasswordAuthentication getNtlmPasswordAuthentication() {
        return null;
    }
}
