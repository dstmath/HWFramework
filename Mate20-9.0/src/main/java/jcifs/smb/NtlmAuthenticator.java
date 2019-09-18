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

    /* access modifiers changed from: protected */
    public final String getRequestingURL() {
        return this.url;
    }

    /* access modifiers changed from: protected */
    public final SmbAuthException getRequestingException() {
        return this.sae;
    }

    public static NtlmPasswordAuthentication requestNtlmPasswordAuthentication(String url2, SmbAuthException sae2) {
        NtlmPasswordAuthentication ntlmPasswordAuthentication;
        if (auth == null) {
            return null;
        }
        synchronized (auth) {
            auth.url = url2;
            auth.sae = sae2;
            ntlmPasswordAuthentication = auth.getNtlmPasswordAuthentication();
        }
        return ntlmPasswordAuthentication;
    }

    /* access modifiers changed from: protected */
    public NtlmPasswordAuthentication getNtlmPasswordAuthentication() {
        return null;
    }
}
