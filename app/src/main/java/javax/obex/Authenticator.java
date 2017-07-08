package javax.obex;

public interface Authenticator {
    PasswordAuthentication onAuthenticationChallenge(String str, boolean z, boolean z2);

    byte[] onAuthenticationResponse(byte[] bArr);
}
