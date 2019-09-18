package com.huawei.security.hccm.param;

import android.support.annotation.NonNull;
import android.util.Log;
import com.huawei.security.hccm.CredentialException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Arrays;

public final class CredentialSpec {
    private static final String TAG = "CredentialSpec";
    public static final int TYPE_ALIAS = 1;
    public static final int TYPE_CHALLENGE = 3;
    public static final int TYPE_UNKNOWN = 0;
    public static final int TYPE_USERNAME = 2;
    private String mAlias;
    private byte[] mChallenge;
    private String mKeyStoretype;
    private byte[] mPassword;
    private int mType;
    private byte[] mUsername;

    public static final class Builder {
        private static final String TAG = "CredentialSpec.Builder";
        private String mAlias;
        private byte[] mAttestationChallenge;
        private String mKeyStoreType;
        private byte[] mPassword;
        private byte[] mUsername;

        public Builder(@NonNull byte[] challenge) {
            this.mUsername = null;
            this.mPassword = null;
            this.mAlias = null;
            this.mKeyStoreType = null;
            this.mAttestationChallenge = null;
            this.mAttestationChallenge = Arrays.copyOf(challenge, challenge.length);
        }

        public Builder(@NonNull String username, @NonNull String password) {
            this(username.getBytes(Charset.forName("UTF-8")), password.getBytes(Charset.forName("UTF-8")));
        }

        public Builder(@NonNull byte[] username, @NonNull byte[] password) {
            this.mUsername = null;
            this.mPassword = null;
            this.mAlias = null;
            this.mKeyStoreType = null;
            this.mAttestationChallenge = null;
            this.mUsername = Arrays.copyOf(username, username.length);
            this.mPassword = Arrays.copyOf(password, password.length);
        }

        public Builder(@NonNull Certificate user, @NonNull PrivateKey key) throws CredentialException {
            this.mUsername = null;
            this.mPassword = null;
            this.mAlias = null;
            this.mKeyStoreType = null;
            this.mAttestationChallenge = null;
            try {
                KeyStore ks = KeyStore.getInstance("AndroidKeyStore");
                ks.load(null);
                this.mAlias = ks.getCertificateAlias(user);
            } catch (IOException | KeyStoreException | NoSuchAlgorithmException | CertificateException e) {
                throw new CredentialException(e.getMessage(), e);
            }
        }

        public Builder(@NonNull Certificate[] chain, @NonNull PrivateKey key) throws CredentialException {
            this(chain[0], key);
        }

        public Builder(@NonNull int type, @NonNull String alias, @NonNull String keyStoreType) throws CredentialException {
            this.mUsername = null;
            this.mPassword = null;
            this.mAlias = null;
            this.mKeyStoreType = null;
            this.mAttestationChallenge = null;
            if (type == 1) {
                try {
                    KeyStore ks = KeyStore.getInstance(keyStoreType);
                    ks.load(null);
                    ks.getCertificateChain(alias);
                    ks.getKey(alias, null);
                    this.mAlias = alias;
                } catch (IOException | KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException | CertificateException e) {
                    throw new CredentialException(e.getMessage(), e);
                }
            }
        }

        public Builder setKeyStoreType(@NonNull String keyStoreType) {
            if (keyStoreType == null) {
                Log.e(TAG, "the keystore type is null");
                return null;
            }
            this.mKeyStoreType = keyStoreType;
            return this;
        }

        public CredentialSpec build() throws CredentialException {
            if (this.mAlias != null) {
                return new CredentialSpec(this.mAlias, this.mKeyStoreType);
            }
            if (this.mUsername != null && this.mPassword != null) {
                return new CredentialSpec(this.mUsername, this.mPassword);
            }
            if (this.mAttestationChallenge != null) {
                return new CredentialSpec(this.mAttestationChallenge);
            }
            throw new CredentialException("No credentials defined");
        }
    }

    private CredentialSpec(@NonNull String alias, @NonNull String keyStoreType) {
        this.mType = 0;
        this.mAlias = null;
        this.mKeyStoretype = null;
        this.mUsername = new byte[0];
        this.mPassword = new byte[0];
        this.mChallenge = new byte[0];
        this.mType = 1;
        this.mAlias = alias;
        this.mKeyStoretype = keyStoreType;
    }

    private CredentialSpec(@NonNull byte[] username, @NonNull byte[] password) {
        this.mType = 0;
        this.mAlias = null;
        this.mKeyStoretype = null;
        this.mUsername = new byte[0];
        this.mPassword = new byte[0];
        this.mChallenge = new byte[0];
        this.mType = 2;
        this.mUsername = Arrays.copyOf(username, username.length);
        this.mPassword = Arrays.copyOf(password, password.length);
    }

    private CredentialSpec(@NonNull byte[] challenge) {
        this.mType = 0;
        this.mAlias = null;
        this.mKeyStoretype = null;
        this.mUsername = new byte[0];
        this.mPassword = new byte[0];
        this.mChallenge = new byte[0];
        this.mType = 3;
        this.mChallenge = challenge;
    }

    public byte[] getChallenge() {
        byte[] challenge = new byte[0];
        if (this.mType == 3) {
            return Arrays.copyOf(this.mChallenge, this.mChallenge.length);
        }
        return challenge;
    }

    public String getAlias() {
        if (this.mType == 1) {
            return this.mAlias;
        }
        return null;
    }

    public String getKeyStoretype() {
        if (this.mType == 1) {
            return this.mKeyStoretype;
        }
        return null;
    }

    public byte[] getUsername() {
        byte[] userName = new byte[0];
        if (this.mType == 2) {
            return Arrays.copyOf(this.mUsername, this.mUsername.length);
        }
        return userName;
    }

    public byte[] getPassword() {
        byte[] password = new byte[0];
        if (this.mType == 2) {
            return Arrays.copyOf(this.mPassword, this.mPassword.length);
        }
        return password;
    }

    public int getType() {
        return this.mType;
    }
}
