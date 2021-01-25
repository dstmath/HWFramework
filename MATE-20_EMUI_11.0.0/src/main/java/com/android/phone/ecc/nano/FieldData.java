package com.android.phone.ecc.nano;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/* access modifiers changed from: package-private */
public class FieldData implements Cloneable {
    private Extension<?, ?> cachedExtension;
    private List<UnknownFieldData> unknownFieldData;
    private Object value;

    /* JADX DEBUG: Multi-variable search result rejected for r1v0, resolved type: com.android.phone.ecc.nano.Extension<?, T> */
    /* JADX WARN: Multi-variable type inference failed */
    <T> FieldData(Extension<?, T> extension, T newValue) {
        this.cachedExtension = extension;
        this.value = newValue;
    }

    FieldData() {
        this.unknownFieldData = new ArrayList();
    }

    /* access modifiers changed from: package-private */
    public void addUnknownField(UnknownFieldData unknownField) {
        this.unknownFieldData.add(unknownField);
    }

    /* access modifiers changed from: package-private */
    public UnknownFieldData getUnknownField(int index) {
        List<UnknownFieldData> list = this.unknownFieldData;
        if (list != null && index < list.size()) {
            return this.unknownFieldData.get(index);
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public int getUnknownFieldSize() {
        List<UnknownFieldData> list = this.unknownFieldData;
        if (list == null) {
            return 0;
        }
        return list.size();
    }

    /* JADX DEBUG: Multi-variable search result rejected for r3v0, resolved type: com.android.phone.ecc.nano.Extension<?, T> */
    /* JADX WARN: Multi-variable type inference failed */
    /* access modifiers changed from: package-private */
    public <T> T getValue(Extension<?, T> extension) {
        if (this.value == null) {
            this.cachedExtension = extension;
            this.value = extension.getValueFrom(this.unknownFieldData);
            this.unknownFieldData = null;
        } else if (this.cachedExtension != extension) {
            throw new IllegalStateException("Tried to getExtension with a differernt Extension.");
        }
        return (T) this.value;
    }

    /* JADX DEBUG: Multi-variable search result rejected for r2v0, resolved type: com.android.phone.ecc.nano.Extension<?, T> */
    /* JADX WARN: Multi-variable type inference failed */
    /* access modifiers changed from: package-private */
    public <T> void setValue(Extension<?, T> extension, T newValue) {
        this.cachedExtension = extension;
        this.value = newValue;
        this.unknownFieldData = null;
    }

    /* access modifiers changed from: package-private */
    public int computeSerializedSize() {
        int size = 0;
        Object obj = this.value;
        if (obj != null) {
            return this.cachedExtension.computeSerializedSize(obj);
        }
        for (UnknownFieldData unknownField : this.unknownFieldData) {
            size += unknownField.computeSerializedSize();
        }
        return size;
    }

    /* access modifiers changed from: package-private */
    public void writeTo(CodedOutputByteBufferNano output) throws IOException {
        Object obj = this.value;
        if (obj != null) {
            this.cachedExtension.writeTo(obj, output);
            return;
        }
        for (UnknownFieldData unknownField : this.unknownFieldData) {
            unknownField.writeTo(output);
        }
    }

    @Override // java.lang.Object
    public boolean equals(Object o) {
        List<UnknownFieldData> list;
        if (o == this) {
            return true;
        }
        if (!(o instanceof FieldData)) {
            return false;
        }
        FieldData other = (FieldData) o;
        if (this.value == null || other.value == null) {
            List<UnknownFieldData> list2 = this.unknownFieldData;
            if (list2 != null && (list = other.unknownFieldData) != null) {
                return list2.equals(list);
            }
            try {
                return Arrays.equals(toByteArray(), other.toByteArray());
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        } else {
            Extension<?, ?> extension = this.cachedExtension;
            if (extension != other.cachedExtension) {
                return false;
            }
            if (!extension.clazz.isArray()) {
                return this.value.equals(other.value);
            }
            Object obj = this.value;
            if (obj instanceof byte[]) {
                return Arrays.equals((byte[]) obj, (byte[]) other.value);
            }
            if (obj instanceof int[]) {
                return Arrays.equals((int[]) obj, (int[]) other.value);
            }
            if (obj instanceof long[]) {
                return Arrays.equals((long[]) obj, (long[]) other.value);
            }
            if (obj instanceof float[]) {
                return Arrays.equals((float[]) obj, (float[]) other.value);
            }
            if (obj instanceof double[]) {
                return Arrays.equals((double[]) obj, (double[]) other.value);
            }
            if (obj instanceof boolean[]) {
                return Arrays.equals((boolean[]) obj, (boolean[]) other.value);
            }
            return Arrays.deepEquals((Object[]) obj, (Object[]) other.value);
        }
    }

    @Override // java.lang.Object
    public int hashCode() {
        try {
            return (17 * 31) + Arrays.hashCode(toByteArray());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private byte[] toByteArray() throws IOException {
        byte[] result = new byte[computeSerializedSize()];
        writeTo(CodedOutputByteBufferNano.newInstance(result));
        return result;
    }

    @Override // java.lang.Object
    public final FieldData clone() {
        FieldData clone = new FieldData();
        try {
            clone.cachedExtension = this.cachedExtension;
            if (this.unknownFieldData == null) {
                clone.unknownFieldData = null;
            } else {
                clone.unknownFieldData.addAll(this.unknownFieldData);
            }
            if (this.value != null) {
                if (this.value instanceof MessageNano) {
                    clone.value = ((MessageNano) this.value).clone();
                } else if (this.value instanceof byte[]) {
                    clone.value = ((byte[]) this.value).clone();
                } else {
                    int i = 0;
                    if (this.value instanceof byte[][]) {
                        byte[][] valueArray = (byte[][]) this.value;
                        byte[][] cloneArray = new byte[valueArray.length][];
                        clone.value = cloneArray;
                        while (i < valueArray.length) {
                            cloneArray[i] = (byte[]) valueArray[i].clone();
                            i++;
                        }
                    } else if (this.value instanceof boolean[]) {
                        clone.value = ((boolean[]) this.value).clone();
                    } else if (this.value instanceof int[]) {
                        clone.value = ((int[]) this.value).clone();
                    } else if (this.value instanceof long[]) {
                        clone.value = ((long[]) this.value).clone();
                    } else if (this.value instanceof float[]) {
                        clone.value = ((float[]) this.value).clone();
                    } else if (this.value instanceof double[]) {
                        clone.value = ((double[]) this.value).clone();
                    } else if (this.value instanceof MessageNano[]) {
                        MessageNano[] valueArray2 = (MessageNano[]) this.value;
                        MessageNano[] cloneArray2 = new MessageNano[valueArray2.length];
                        clone.value = cloneArray2;
                        while (i < valueArray2.length) {
                            cloneArray2[i] = valueArray2[i].clone();
                            i++;
                        }
                    }
                }
            }
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }
}
