package javax.obex;

import java.io.IOException;

public class ObexSession {
    private static final String TAG = "ObexSession";
    private static final boolean V = false;
    protected Authenticator mAuthenticator;
    protected byte[] mChallengeDigest;

    public boolean handleAuthChall(HeaderSet header) throws IOException {
        if (this.mAuthenticator == null) {
            return false;
        }
        byte[] challenge = ObexHelper.getTagValue((byte) 0, header.mAuthChall);
        byte[] option = ObexHelper.getTagValue((byte) 1, header.mAuthChall);
        byte[] description = ObexHelper.getTagValue((byte) 2, header.mAuthChall);
        String str = null;
        if (description != null) {
            byte[] realmString = new byte[(description.length - 1)];
            System.arraycopy(description, 1, realmString, 0, realmString.length);
            switch (description[0] & ObexHelper.OBEX_OPCODE_ABORT) {
                case ObexHelper.OBEX_AUTH_REALM_CHARSET_ASCII /*0*/:
                case ObexHelper.OBEX_AUTH_REALM_CHARSET_ISO_8859_1 /*1*/:
                    try {
                        str = new String(realmString, "ISO8859_1");
                        break;
                    } catch (Exception e) {
                        throw new IOException("Unsupported Encoding Scheme");
                    }
                case ObexHelper.OBEX_OPCODE_ABORT /*255*/:
                    str = ObexHelper.convertToUnicode(realmString, false);
                    break;
                default:
                    throw new IOException("Unsupported Encoding Scheme");
            }
        }
        boolean isUserIDRequired = false;
        boolean isFullAccess = true;
        if (option != null) {
            if ((option[0] & 1) != 0) {
                isUserIDRequired = true;
            }
            if ((option[0] & 2) != 0) {
                isFullAccess = false;
            }
        }
        header.mAuthChall = null;
        try {
            PasswordAuthentication result = this.mAuthenticator.onAuthenticationChallenge(str, isUserIDRequired, isFullAccess);
            if (result == null) {
                return false;
            }
            byte[] password = result.getPassword();
            if (password == null) {
                return false;
            }
            byte[] userName = result.getUserName();
            if (userName != null) {
                header.mAuthResp = new byte[(userName.length + 38)];
                header.mAuthResp[36] = (byte) 1;
                header.mAuthResp[37] = (byte) userName.length;
                System.arraycopy(userName, 0, header.mAuthResp, 38, userName.length);
            } else {
                header.mAuthResp = new byte[36];
            }
            byte[] digest = new byte[((challenge.length + password.length) + 1)];
            System.arraycopy(challenge, 0, digest, 0, challenge.length);
            digest[challenge.length] = (byte) 58;
            System.arraycopy(password, 0, digest, challenge.length + 1, password.length);
            header.mAuthResp[0] = (byte) 0;
            header.mAuthResp[1] = (byte) 16;
            System.arraycopy(ObexHelper.computeMd5Hash(digest), 0, header.mAuthResp, 2, 16);
            header.mAuthResp[18] = (byte) 2;
            header.mAuthResp[19] = (byte) 16;
            System.arraycopy(challenge, 0, header.mAuthResp, 20, 16);
            return true;
        } catch (Exception e2) {
            return false;
        }
    }

    public boolean handleAuthResp(byte[] authResp) {
        if (this.mAuthenticator == null) {
            return false;
        }
        byte[] correctPassword = this.mAuthenticator.onAuthenticationResponse(ObexHelper.getTagValue((byte) 1, authResp));
        if (correctPassword == null) {
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
