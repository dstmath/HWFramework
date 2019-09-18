package android.security.keystore.recovery;

import android.annotation.SystemApi;
import android.os.Parcel;
import android.os.Parcelable;
import com.android.internal.util.Preconditions;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@SystemApi
public final class KeyDerivationParams implements Parcelable {
    public static final int ALGORITHM_SCRYPT = 2;
    public static final int ALGORITHM_SHA256 = 1;
    public static final Parcelable.Creator<KeyDerivationParams> CREATOR = new Parcelable.Creator<KeyDerivationParams>() {
        public KeyDerivationParams createFromParcel(Parcel in) {
            return new KeyDerivationParams(in);
        }

        public KeyDerivationParams[] newArray(int length) {
            return new KeyDerivationParams[length];
        }
    };
    private final int mAlgorithm;
    private final int mMemoryDifficulty;
    private final byte[] mSalt;

    @Retention(RetentionPolicy.SOURCE)
    public @interface KeyDerivationAlgorithm {
    }

    public static KeyDerivationParams createSha256Params(byte[] salt) {
        return new KeyDerivationParams(1, salt);
    }

    public static KeyDerivationParams createScryptParams(byte[] salt, int memoryDifficulty) {
        return new KeyDerivationParams(2, salt, memoryDifficulty);
    }

    private KeyDerivationParams(int algorithm, byte[] salt) {
        this(algorithm, salt, -1);
    }

    private KeyDerivationParams(int algorithm, byte[] salt, int memoryDifficulty) {
        this.mAlgorithm = algorithm;
        this.mSalt = (byte[]) Preconditions.checkNotNull(salt);
        this.mMemoryDifficulty = memoryDifficulty;
    }

    public int getAlgorithm() {
        return this.mAlgorithm;
    }

    public byte[] getSalt() {
        return this.mSalt;
    }

    public int getMemoryDifficulty() {
        return this.mMemoryDifficulty;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.mAlgorithm);
        out.writeByteArray(this.mSalt);
        out.writeInt(this.mMemoryDifficulty);
    }

    protected KeyDerivationParams(Parcel in) {
        this.mAlgorithm = in.readInt();
        this.mSalt = in.createByteArray();
        this.mMemoryDifficulty = in.readInt();
    }

    public int describeContents() {
        return 0;
    }
}
