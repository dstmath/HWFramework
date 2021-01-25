package javax.obex;

public final class PasswordAuthentication {
    private final byte[] mPassword;
    private byte[] mUserName;

    public PasswordAuthentication(byte[] userName, byte[] password) {
        if (userName != null) {
            this.mUserName = new byte[userName.length];
            System.arraycopy(userName, 0, this.mUserName, 0, userName.length);
        }
        this.mPassword = new byte[password.length];
        System.arraycopy(password, 0, this.mPassword, 0, password.length);
    }

    public byte[] getUserName() {
        return this.mUserName;
    }

    public byte[] getPassword() {
        return this.mPassword;
    }
}
