package javax.crypto.spec;

import java.security.spec.KeySpec;

public class PBEKeySpec implements KeySpec {
    private int iterationCount = 0;
    private int keyLength = 0;
    private char[] password;
    private byte[] salt = null;

    public PBEKeySpec(char[] password) {
        if (password == null || password.length == 0) {
            this.password = new char[0];
        } else {
            this.password = (char[]) password.clone();
        }
    }

    public PBEKeySpec(char[] password, byte[] salt, int iterationCount, int keyLength) {
        if (password == null || password.length == 0) {
            this.password = new char[0];
        } else {
            this.password = (char[]) password.clone();
        }
        if (salt == null) {
            throw new NullPointerException("the salt parameter must be non-null");
        } else if (salt.length == 0) {
            throw new IllegalArgumentException("the salt parameter must not be empty");
        } else {
            this.salt = (byte[]) salt.clone();
            if (iterationCount <= 0) {
                throw new IllegalArgumentException("invalid iterationCount value");
            } else if (keyLength <= 0) {
                throw new IllegalArgumentException("invalid keyLength value");
            } else {
                this.iterationCount = iterationCount;
                this.keyLength = keyLength;
            }
        }
    }

    public PBEKeySpec(char[] password, byte[] salt, int iterationCount) {
        if (password == null || password.length == 0) {
            this.password = new char[0];
        } else {
            this.password = (char[]) password.clone();
        }
        if (salt == null) {
            throw new NullPointerException("the salt parameter must be non-null");
        } else if (salt.length == 0) {
            throw new IllegalArgumentException("the salt parameter must not be empty");
        } else {
            this.salt = (byte[]) salt.clone();
            if (iterationCount <= 0) {
                throw new IllegalArgumentException("invalid iterationCount value");
            }
            this.iterationCount = iterationCount;
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
