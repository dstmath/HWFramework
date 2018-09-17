package com.android.framework.protobuf.nano;

import java.io.IOException;

public abstract class ExtendableMessageNano<M extends ExtendableMessageNano<M>> extends MessageNano {
    protected FieldArray unknownFieldData;

    protected int computeSerializedSize() {
        int size = 0;
        if (this.unknownFieldData != null) {
            for (int i = 0; i < this.unknownFieldData.size(); i++) {
                size += this.unknownFieldData.dataAt(i).computeSerializedSize();
            }
        }
        return size;
    }

    public void writeTo(CodedOutputByteBufferNano output) throws IOException {
        if (this.unknownFieldData != null) {
            for (int i = 0; i < this.unknownFieldData.size(); i++) {
                this.unknownFieldData.dataAt(i).writeTo(output);
            }
        }
    }

    public final boolean hasExtension(Extension<M, ?> extension) {
        boolean z = false;
        if (this.unknownFieldData == null) {
            return false;
        }
        if (this.unknownFieldData.get(WireFormatNano.getTagFieldNumber(extension.tag)) != null) {
            z = true;
        }
        return z;
    }

    public final <T> T getExtension(Extension<M, T> extension) {
        T t = null;
        if (this.unknownFieldData == null) {
            return null;
        }
        FieldData field = this.unknownFieldData.get(WireFormatNano.getTagFieldNumber(extension.tag));
        if (field != null) {
            t = field.getValue(extension);
        }
        return t;
    }

    public final <T> M setExtension(Extension<M, T> extension, T value) {
        int fieldNumber = WireFormatNano.getTagFieldNumber(extension.tag);
        if (value != null) {
            FieldData field = null;
            if (this.unknownFieldData == null) {
                this.unknownFieldData = new FieldArray();
            } else {
                field = this.unknownFieldData.get(fieldNumber);
            }
            if (field == null) {
                this.unknownFieldData.put(fieldNumber, new FieldData(extension, value));
            } else {
                field.setValue(extension, value);
            }
        } else if (this.unknownFieldData != null) {
            this.unknownFieldData.remove(fieldNumber);
            if (this.unknownFieldData.isEmpty()) {
                this.unknownFieldData = null;
            }
        }
        return this;
    }

    protected final boolean storeUnknownField(CodedInputByteBufferNano input, int tag) throws IOException {
        int startPos = input.getPosition();
        if (!input.skipField(tag)) {
            return false;
        }
        int fieldNumber = WireFormatNano.getTagFieldNumber(tag);
        UnknownFieldData unknownField = new UnknownFieldData(tag, input.getData(startPos, input.getPosition() - startPos));
        FieldData field = null;
        if (this.unknownFieldData == null) {
            this.unknownFieldData = new FieldArray();
        } else {
            field = this.unknownFieldData.get(fieldNumber);
        }
        if (field == null) {
            field = new FieldData();
            this.unknownFieldData.put(fieldNumber, field);
        }
        field.addUnknownField(unknownField);
        return true;
    }

    public M clone() throws CloneNotSupportedException {
        ExtendableMessageNano cloned = (ExtendableMessageNano) super.clone();
        InternalNano.cloneUnknownFieldData(this, cloned);
        return cloned;
    }
}
