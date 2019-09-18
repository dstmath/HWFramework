package javax.crypto.spec;

import java.security.spec.KeySpec;

public class PBEKeySpec implements KeySpec {
    private int iterationCount = 0;
    private int keyLength = 0;
    private char[] password;
    private byte[] salt = null;

    public PBEKeySpec(char[] password2) {
        if (password2 == null || password2.length == 0) {
            this.password = new char[0];
        } else {
            this.password = (char[]) password2.clone();
        }
    }

    public PBEKeySpec(char[] password2, byte[] salt2, int iterationCount2, int keyLength2) {
        if (password2 == null || password2.length == 0) {
            this.password = new char[0];
        } else {
            this.password = (char[]) password2.clone();
        }
        if (salt2 == null) {
            throw new NullPointerException("the salt parameter must be non-null");
        } else if (salt2.length != 0) {
            this.salt = (byte[]) salt2.clone();
            if (iterationCount2 <= 0) {
                throw new IllegalArgumentException("invalid iterationCount value");
            } else if (keyLength2 > 0) {
                this.iterationCount = iterationCount2;
                this.keyLength = keyLength2;
            } else {
                throw new IllegalArgumentException("invalid keyLength value");
            }
        } else {
            throw new IllegalArgumentException("the salt parameter must not be empty");
        }
    }

    public PBEKeySpec(char[] password2, byte[] salt2, int iterationCount2) {
        if (password2 == null || password2.length == 0) {
            this.password = new char[0];
        } else {
            this.password = (char[]) password2.clone();
        }
        if (salt2 == null) {
            throw new NullPointerException("the salt parameter must be non-null");
        } else if (salt2.length != 0) {
            this.salt = (byte[]) salt2.clone();
            if (iterationCount2 > 0) {
                this.iterationCount = iterationCount2;
                return;
            }
            throw new IllegalArgumentException("invalid iterationCount value");
        } else {
            throw new IllegalArgumentException("the salt parameter must not be empty");
        }
    }

    public final void clearPassword() {
        if (this.password != null) {
            for (int i = 0; i < this.password.length; i++) {
                this.password[i] = ' ';
            }
            this.password = null;
        }
    }

    public final char[] getPassword() {
        if (this.password != null) {
            return (char[]) this.password.clone();
        }
        throw new IllegalStateException("password has been cleared");
    }

    public final byte[] getSalt() {
        if (this.salt != null) {
            return (byte[]) this.salt.clone();
        }
        return null;
    }

    public final int getIterationCount() {
        return this.iterationCount;
    }

    public final int getKeyLength() {
        return this.keyLength;
    }
}
