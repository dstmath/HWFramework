package android.security;

import android.content.Context;
import java.security.KeyStore.ProtectionParameter;

@Deprecated
public final class KeyStoreParameter implements ProtectionParameter {
    private final int mFlags;

    @Deprecated
    public static final class Builder {
        private int mFlags;

        public Builder(Context context) {
            if (context == null) {
                throw new NullPointerException("context == null");
            }
        }

        public Builder setEncryptionRequired(boolean required) {
            if (required) {
                this.mFlags |= 1;
            } else {
                this.mFlags &= -2;
            }
            return this;
        }

        public KeyStoreParameter build() {
            return new KeyStoreParameter(this.mFlags, null);
        }
    }

    /* synthetic */ KeyStoreParameter(int flags, KeyStoreParameter -this1) {
        this(flags);
    }

    private KeyStoreParameter(int flags) {
        this.mFlags = flags;
    }

    public int getFlags() {
        return this.mFlags;
    }

    public boolean isEncryptionRequired() {
        return (this.mFlags & 1) != 0;
    }
}
