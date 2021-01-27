package javax.obex;

import java.io.IOException;

public class ObexSession {
    private static final String TAG = "ObexSession";
    private static final boolean V = false;
    protected Authenticator mAuthenticator;
    protected byte[] mChallengeDigest;

    public boolean handleAuthChall(HeaderSet header) throws IOException {
        boolean isFullAccess;
        boolean isFullAccess2;
        byte[] password;
        if (this.mAuthenticator == null) {
            return false;
        }
        byte[] challenge = ObexHelper.getTagValue((byte) 0, header.mAuthChall);
        byte[] option = ObexHelper.getTagValue((byte) 1, header.mAuthChall);
        byte[] description = ObexHelper.getTagValue((byte) 2, header.mAuthChall);
        String realm = null;
        if (description != null) {
            byte[] realmString = new byte[(description.length - 1)];
            System.arraycopy(description, 1, realmString, 0, realmString.length);
            int i = description[0] & 255;
            if (i == 0 || i == 1) {
                try {
                    realm = new String(realmString, "ISO8859_1");
                } catch (Exception e) {
                    throw new IOException("Unsupported Encoding Scheme");
                }
            } else if (i == 255) {
                realm = ObexHelper.convertToUnicode(realmString, false);
            } else {
                throw new IOException("Unsupported Encoding Scheme");
            }
        }
        boolean isUserIDRequired = false;
        if (option != null) {
            if ((option[0] & 1) != 0) {
                isUserIDRequired = true;
            }
            if ((option[0] & 2) != 0) {
                isFullAccess = false;
                isFullAccess2 = isUserIDRequired;
            } else {
                isFullAccess = true;
                isFullAccess2 = isUserIDRequired;
            }
        } else {
            isFullAccess = true;
            isFullAccess2 = false;
        }
        header.mAuthChall = null;
        try {
            PasswordAuthentication result = this.mAuthenticator.onAuthenticationChallenge(realm, isFullAccess2, isFullAccess);
            if (result == null || (password = result.getPassword()) == null) {
                return false;
            }
            byte[] userName = result.getUserName();
            if (userName != null) {
                header.mAuthResp = new byte[(userName.length + 38)];
                header.mAuthResp[36] = 1;
                header.mAuthResp[37] = (byte) userName.length;
                System.arraycopy(userName, 0, header.mAuthResp, 38, userName.length);
            } else {
                header.mAuthResp = new byte[36];
            }
            byte[] digest = new byte[(challenge.length + password.length + 1)];
            System.arraycopy(challenge, 0, digest, 0, challenge.length);
            digest[challenge.length] = 58;
            System.arraycopy(password, 0, digest, challenge.length + 1, password.length);
            header.mAuthResp[0] = 0;
            header.mAuthResp[1] = 16;
            System.arraycopy(ObexHelper.computeMd5Hash(digest), 0, header.mAuthResp, 2, 16);
            header.mAuthResp[18] = 2;
            header.mAuthResp[19] = 16;
            System.arraycopy(challenge, 0, header.mAuthResp, 20, 16);
            return true;
        } catch (Exception e2) {
            return false;
        }
    }

    public boolean handleAuthResp(byte[] authResp) {
        byte[] correctPassword;
        Authenticator authenticator = this.mAuthenticator;
        if (authenticator == null || (correctPassword = authenticator.onAuthenticationResponse(ObexHelper.getTagValue((byte) 1, authResp))) == null) {
            return false;
        }
        byte[] temp = new byte[(correctPassword.length + 16)];
        System.arraycopy(this.mChallengeDigest, 0, temp, 0, 16);
        System.arraycopy(correctPassword, 0, temp, 16, correctPassword.length);
        byte[] correctResponse = ObexHelper.computeMd5Hash(temp);
        byte[] actualResponse = ObexHelper.getTagValue((byte) 0, authResp);
        for (int i = 0; i < 16; i++) {
            if (correctResponse[i] != actualResponse[i]) {
                return false;
            }
        }
        return true;
    }
}
