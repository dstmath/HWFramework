package android.hardware.location;

import android.annotation.SystemApi;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

@SystemApi
public final class NanoAppBinary implements Parcelable {
    public static final Parcelable.Creator<NanoAppBinary> CREATOR = new Parcelable.Creator<NanoAppBinary>() {
        /* class android.hardware.location.NanoAppBinary.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public NanoAppBinary createFromParcel(Parcel in) {
            return new NanoAppBinary(in);
        }

        @Override // android.os.Parcelable.Creator
        public NanoAppBinary[] newArray(int size) {
            return new NanoAppBinary[size];
        }
    };
    private static final int EXPECTED_HEADER_VERSION = 1;
    private static final int EXPECTED_MAGIC_VALUE = 1330528590;
    private static final ByteOrder HEADER_ORDER = ByteOrder.LITTLE_ENDIAN;
    private static final int HEADER_SIZE_BYTES = 40;
    private static final int NANOAPP_ENCRYPTED_FLAG_BIT = 2;
    private static final int NANOAPP_SIGNED_FLAG_BIT = 1;
    private static final String TAG = "NanoAppBinary";
    private int mFlags;
    private boolean mHasValidHeader;
    private int mHeaderVersion;
    private long mHwHubType;
    private int mMagic;
    private byte[] mNanoAppBinary;
    private long mNanoAppId;
    private int mNanoAppVersion;
    private byte mTargetChreApiMajorVersion;
    private byte mTargetChreApiMinorVersion;

    public NanoAppBinary(byte[] appBinary) {
        this.mHasValidHeader = false;
        this.mNanoAppBinary = appBinary;
        parseBinaryHeader();
    }

    private void parseBinaryHeader() {
        ByteBuffer buf = ByteBuffer.wrap(this.mNanoAppBinary).order(HEADER_ORDER);
        this.mHasValidHeader = false;
        try {
            this.mHeaderVersion = buf.getInt();
            if (this.mHeaderVersion != 1) {
                Log.e(TAG, "Unexpected header version " + this.mHeaderVersion + " while parsing header (expected " + 1 + ")");
                return;
            }
            this.mMagic = buf.getInt();
            this.mNanoAppId = buf.getLong();
            this.mNanoAppVersion = buf.getInt();
            this.mFlags = buf.getInt();
            this.mHwHubType = buf.getLong();
            this.mTargetChreApiMajorVersion = buf.get();
            this.mTargetChreApiMinorVersion = buf.get();
            if (this.mMagic != EXPECTED_MAGIC_VALUE) {
                Log.e(TAG, "Unexpected magic value " + String.format("0x%08X", Integer.valueOf(this.mMagic)) + "while parsing header (expected " + String.format("0x%08X", Integer.valueOf((int) EXPECTED_MAGIC_VALUE)) + ")");
                return;
            }
            this.mHasValidHeader = true;
        } catch (BufferUnderflowException e) {
            Log.e(TAG, "Not enough contents in nanoapp header");
        }
    }

    public byte[] getBinary() {
        return this.mNanoAppBinary;
    }

    public byte[] getBinaryNoHeader() {
        byte[] bArr = this.mNanoAppBinary;
        if (bArr.length >= 40) {
            return Arrays.copyOfRange(bArr, 40, bArr.length);
        }
        throw new IndexOutOfBoundsException("NanoAppBinary binary byte size (" + this.mNanoAppBinary.length + ") is less than header size (" + 40 + ")");
    }

    public boolean hasValidHeader() {
        return this.mHasValidHeader;
    }

    public int getHeaderVersion() {
        return this.mHeaderVersion;
    }

    public long getNanoAppId() {
        return this.mNanoAppId;
    }

    public int getNanoAppVersion() {
        return this.mNanoAppVersion;
    }

    public long getHwHubType() {
        return this.mHwHubType;
    }

    public byte getTargetChreApiMajorVersion() {
        return this.mTargetChreApiMajorVersion;
    }

    public byte getTargetChreApiMinorVersion() {
        return this.mTargetChreApiMinorVersion;
    }

    public int getFlags() {
        return this.mFlags;
    }

    public boolean isSigned() {
        return (this.mFlags & 1) != 0;
    }

    public boolean isEncrypted() {
        return (this.mFlags & 2) != 0;
    }

    private NanoAppBinary(Parcel in) {
        this.mHasValidHeader = false;
        this.mNanoAppBinary = new byte[in.readInt()];
        in.readByteArray(this.mNanoAppBinary);
        parseBinaryHeader();
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.mNanoAppBinary.length);
        out.writeByteArray(this.mNanoAppBinary);
    }
}
