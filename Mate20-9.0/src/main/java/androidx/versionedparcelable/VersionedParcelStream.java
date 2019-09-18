package androidx.versionedparcelable;

import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcelable;
import android.support.annotation.RestrictTo;
import android.support.v4.internal.view.SupportMenu;
import android.util.SparseArray;
import androidx.versionedparcelable.VersionedParcel;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Set;

@RestrictTo({RestrictTo.Scope.LIBRARY})
class VersionedParcelStream extends VersionedParcel {
    private static final int TYPE_BOOLEAN = 5;
    private static final int TYPE_BOOLEAN_ARRAY = 6;
    private static final int TYPE_DOUBLE = 7;
    private static final int TYPE_DOUBLE_ARRAY = 8;
    private static final int TYPE_FLOAT = 13;
    private static final int TYPE_FLOAT_ARRAY = 14;
    private static final int TYPE_INT = 9;
    private static final int TYPE_INT_ARRAY = 10;
    private static final int TYPE_LONG = 11;
    private static final int TYPE_LONG_ARRAY = 12;
    private static final int TYPE_NULL = 0;
    private static final int TYPE_STRING = 3;
    private static final int TYPE_STRING_ARRAY = 4;
    private static final int TYPE_SUB_BUNDLE = 1;
    private static final int TYPE_SUB_PERSISTABLE_BUNDLE = 2;
    private static final Charset UTF_16 = Charset.forName("UTF-16");
    private final SparseArray<InputBuffer> mCachedFields = new SparseArray<>();
    private DataInputStream mCurrentInput;
    private DataOutputStream mCurrentOutput;
    private FieldBuffer mFieldBuffer;
    private boolean mIgnoreParcelables;
    private final DataInputStream mMasterInput;
    private final DataOutputStream mMasterOutput;

    private static class FieldBuffer {
        /* access modifiers changed from: private */
        public final DataOutputStream mDataStream = new DataOutputStream(this.mOutput);
        private final int mFieldId;
        /* access modifiers changed from: private */
        public final ByteArrayOutputStream mOutput = new ByteArrayOutputStream();
        private final DataOutputStream mTarget;

        FieldBuffer(int fieldId, DataOutputStream target) {
            this.mFieldId = fieldId;
            this.mTarget = target;
        }

        /* access modifiers changed from: package-private */
        public void flushField() throws IOException {
            this.mDataStream.flush();
            int size = this.mOutput.size();
            this.mTarget.writeInt((this.mFieldId << 16) | (size >= 65535 ? 65535 : size));
            if (size >= 65535) {
                this.mTarget.writeInt(size);
            }
            this.mOutput.writeTo(this.mTarget);
        }
    }

    private static class InputBuffer {
        /* access modifiers changed from: private */
        public final int mFieldId;
        /* access modifiers changed from: private */
        public final DataInputStream mInputStream;
        private final int mSize;

        InputBuffer(int fieldId, int size, DataInputStream inputStream) throws IOException {
            this.mSize = size;
            this.mFieldId = fieldId;
            byte[] data = new byte[this.mSize];
            inputStream.readFully(data);
            this.mInputStream = new DataInputStream(new ByteArrayInputStream(data));
        }
    }

    public VersionedParcelStream(InputStream input, OutputStream output) {
        DataOutputStream dataOutputStream = null;
        this.mMasterInput = input != null ? new DataInputStream(input) : null;
        this.mMasterOutput = output != null ? new DataOutputStream(output) : dataOutputStream;
        this.mCurrentInput = this.mMasterInput;
        this.mCurrentOutput = this.mMasterOutput;
    }

    public boolean isStream() {
        return true;
    }

    public void setSerializationFlags(boolean allowSerialization, boolean ignoreParcelables) {
        if (allowSerialization) {
            this.mIgnoreParcelables = ignoreParcelables;
            return;
        }
        throw new RuntimeException("Serialization of this object is not allowed");
    }

    public void closeField() {
        if (this.mFieldBuffer != null) {
            try {
                if (this.mFieldBuffer.mOutput.size() != 0) {
                    this.mFieldBuffer.flushField();
                }
                this.mFieldBuffer = null;
            } catch (IOException e) {
                throw new VersionedParcel.ParcelException(e);
            }
        }
    }

    /* access modifiers changed from: protected */
    public VersionedParcel createSubParcel() {
        return new VersionedParcelStream(this.mCurrentInput, this.mCurrentOutput);
    }

    public boolean readField(int fieldId) {
        InputBuffer buffer = this.mCachedFields.get(fieldId);
        if (buffer != null) {
            this.mCachedFields.remove(fieldId);
            this.mCurrentInput = buffer.mInputStream;
            return true;
        }
        while (true) {
            try {
                int fieldInfo = this.mMasterInput.readInt();
                int size = fieldInfo & SupportMenu.USER_MASK;
                if (size == 65535) {
                    size = this.mMasterInput.readInt();
                }
                InputBuffer buffer2 = new InputBuffer(65535 & (fieldInfo >> 16), size, this.mMasterInput);
                if (buffer2.mFieldId == fieldId) {
                    this.mCurrentInput = buffer2.mInputStream;
                    return true;
                }
                this.mCachedFields.put(buffer2.mFieldId, buffer2);
            } catch (IOException e) {
                return false;
            }
        }
    }

    public void setOutputField(int fieldId) {
        closeField();
        this.mFieldBuffer = new FieldBuffer(fieldId, this.mMasterOutput);
        this.mCurrentOutput = this.mFieldBuffer.mDataStream;
    }

    public void writeByteArray(byte[] b) {
        if (b != null) {
            try {
                this.mCurrentOutput.writeInt(b.length);
                this.mCurrentOutput.write(b);
            } catch (IOException e) {
                throw new VersionedParcel.ParcelException(e);
            }
        } else {
            this.mCurrentOutput.writeInt(-1);
        }
    }

    public void writeByteArray(byte[] b, int offset, int len) {
        if (b != null) {
            try {
                this.mCurrentOutput.writeInt(len);
                this.mCurrentOutput.write(b, offset, len);
            } catch (IOException e) {
                throw new VersionedParcel.ParcelException(e);
            }
        } else {
            this.mCurrentOutput.writeInt(-1);
        }
    }

    public void writeInt(int val) {
        try {
            this.mCurrentOutput.writeInt(val);
        } catch (IOException e) {
            throw new VersionedParcel.ParcelException(e);
        }
    }

    public void writeLong(long val) {
        try {
            this.mCurrentOutput.writeLong(val);
        } catch (IOException e) {
            throw new VersionedParcel.ParcelException(e);
        }
    }

    public void writeFloat(float val) {
        try {
            this.mCurrentOutput.writeFloat(val);
        } catch (IOException e) {
            throw new VersionedParcel.ParcelException(e);
        }
    }

    public void writeDouble(double val) {
        try {
            this.mCurrentOutput.writeDouble(val);
        } catch (IOException e) {
            throw new VersionedParcel.ParcelException(e);
        }
    }

    public void writeString(String val) {
        if (val != null) {
            try {
                byte[] bytes = val.getBytes(UTF_16);
                this.mCurrentOutput.writeInt(bytes.length);
                this.mCurrentOutput.write(bytes);
            } catch (IOException e) {
                throw new VersionedParcel.ParcelException(e);
            }
        } else {
            this.mCurrentOutput.writeInt(-1);
        }
    }

    public void writeBoolean(boolean val) {
        try {
            this.mCurrentOutput.writeBoolean(val);
        } catch (IOException e) {
            throw new VersionedParcel.ParcelException(e);
        }
    }

    public void writeStrongBinder(IBinder val) {
        if (!this.mIgnoreParcelables) {
            throw new RuntimeException("Binders cannot be written to an OutputStream");
        }
    }

    public void writeParcelable(Parcelable p) {
        if (!this.mIgnoreParcelables) {
            throw new RuntimeException("Parcelables cannot be written to an OutputStream");
        }
    }

    public void writeStrongInterface(IInterface val) {
        if (!this.mIgnoreParcelables) {
            throw new RuntimeException("Binders cannot be written to an OutputStream");
        }
    }

    public IBinder readStrongBinder() {
        return null;
    }

    public <T extends Parcelable> T readParcelable() {
        return null;
    }

    public int readInt() {
        try {
            return this.mCurrentInput.readInt();
        } catch (IOException e) {
            throw new VersionedParcel.ParcelException(e);
        }
    }

    public long readLong() {
        try {
            return this.mCurrentInput.readLong();
        } catch (IOException e) {
            throw new VersionedParcel.ParcelException(e);
        }
    }

    public float readFloat() {
        try {
            return this.mCurrentInput.readFloat();
        } catch (IOException e) {
            throw new VersionedParcel.ParcelException(e);
        }
    }

    public double readDouble() {
        try {
            return this.mCurrentInput.readDouble();
        } catch (IOException e) {
            throw new VersionedParcel.ParcelException(e);
        }
    }

    public String readString() {
        try {
            int len = this.mCurrentInput.readInt();
            if (len <= 0) {
                return null;
            }
            byte[] bytes = new byte[len];
            this.mCurrentInput.readFully(bytes);
            return new String(bytes, UTF_16);
        } catch (IOException e) {
            throw new VersionedParcel.ParcelException(e);
        }
    }

    public byte[] readByteArray() {
        try {
            int len = this.mCurrentInput.readInt();
            if (len <= 0) {
                return null;
            }
            byte[] bytes = new byte[len];
            this.mCurrentInput.readFully(bytes);
            return bytes;
        } catch (IOException e) {
            throw new VersionedParcel.ParcelException(e);
        }
    }

    public boolean readBoolean() {
        try {
            return this.mCurrentInput.readBoolean();
        } catch (IOException e) {
            throw new VersionedParcel.ParcelException(e);
        }
    }

    public void writeBundle(Bundle val) {
        if (val != null) {
            try {
                Set<String> keys = val.keySet();
                this.mCurrentOutput.writeInt(keys.size());
                for (String key : keys) {
                    writeString(key);
                    writeObject(val.get(key));
                }
            } catch (IOException e) {
                throw new VersionedParcel.ParcelException(e);
            }
        } else {
            this.mCurrentOutput.writeInt(-1);
        }
    }

    public Bundle readBundle() {
        int size = readInt();
        if (size < 0) {
            return null;
        }
        Bundle b = new Bundle();
        for (int i = 0; i < size; i++) {
            readObject(readInt(), readString(), b);
        }
        return b;
    }

    private void writeObject(Object o) {
        if (o == null) {
            writeInt(0);
        } else if (o instanceof Bundle) {
            writeInt(1);
            writeBundle((Bundle) o);
        } else if (o instanceof String) {
            writeInt(3);
            writeString((String) o);
        } else if (o instanceof String[]) {
            writeInt(4);
            writeArray((String[]) o);
        } else if (o instanceof Boolean) {
            writeInt(5);
            writeBoolean(((Boolean) o).booleanValue());
        } else if (o instanceof boolean[]) {
            writeInt(6);
            writeBooleanArray((boolean[]) o);
        } else if (o instanceof Double) {
            writeInt(7);
            writeDouble(((Double) o).doubleValue());
        } else if (o instanceof double[]) {
            writeInt(8);
            writeDoubleArray((double[]) o);
        } else if (o instanceof Integer) {
            writeInt(9);
            writeInt(((Integer) o).intValue());
        } else if (o instanceof int[]) {
            writeInt(10);
            writeIntArray((int[]) o);
        } else if (o instanceof Long) {
            writeInt(11);
            writeLong(((Long) o).longValue());
        } else if (o instanceof long[]) {
            writeInt(12);
            writeLongArray((long[]) o);
        } else if (o instanceof Float) {
            writeInt(13);
            writeFloat(((Float) o).floatValue());
        } else if (o instanceof float[]) {
            writeInt(14);
            writeFloatArray((float[]) o);
        } else {
            throw new IllegalArgumentException("Unsupported type " + o.getClass());
        }
    }

    private void readObject(int type, String key, Bundle b) {
        switch (type) {
            case 0:
                b.putParcelable(key, null);
                return;
            case 1:
                b.putBundle(key, readBundle());
                return;
            case 2:
                b.putBundle(key, readBundle());
                return;
            case 3:
                b.putString(key, readString());
                return;
            case 4:
                b.putStringArray(key, (String[]) readArray(new String[0]));
                return;
            case 5:
                b.putBoolean(key, readBoolean());
                return;
            case 6:
                b.putBooleanArray(key, readBooleanArray());
                return;
            case 7:
                b.putDouble(key, readDouble());
                return;
            case 8:
                b.putDoubleArray(key, readDoubleArray());
                return;
            case 9:
                b.putInt(key, readInt());
                return;
            case 10:
                b.putIntArray(key, readIntArray());
                return;
            case 11:
                b.putLong(key, readLong());
                return;
            case 12:
                b.putLongArray(key, readLongArray());
                return;
            case 13:
                b.putFloat(key, readFloat());
                return;
            case 14:
                b.putFloatArray(key, readFloatArray());
                return;
            default:
                throw new RuntimeException("Unknown type " + type);
        }
    }
}
