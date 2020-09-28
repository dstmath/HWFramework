package com.android.phone.ecc.nano;

import com.android.phone.ecc.nano.ExtendableMessageNano;
import java.io.IOException;

public abstract class ExtendableMessageNano<M extends ExtendableMessageNano<M>> extends MessageNano {
    protected FieldArray unknownFieldData;

    /* access modifiers changed from: protected */
    @Override // com.android.phone.ecc.nano.MessageNano
    public int computeSerializedSize() {
        int size = 0;
        if (this.unknownFieldData != null) {
            for (int i = 0; i < this.unknownFieldData.size(); i++) {
                size += this.unknownFieldData.dataAt(i).computeSerializedSize();
            }
        }
        return size;
    }

    @Override // com.android.phone.ecc.nano.MessageNano
    public void writeTo(CodedOutputByteBufferNano output) throws IOException {
        if (this.unknownFieldData != null) {
            for (int i = 0; i < this.unknownFieldData.size(); i++) {
                this.unknownFieldData.dataAt(i).writeTo(output);
            }
        }
    }

    public final boolean hasExtension(Extension<M, ?> extension) {
        FieldArray fieldArray = this.unknownFieldData;
        if (fieldArray == null || fieldArray.get(WireFormatNano.getTagFieldNumber(extension.tag)) == null) {
            return false;
        }
        return true;
    }

    public final <T> T getExtension(Extension<M, T> extension) {
        FieldData field;
        FieldArray fieldArray = this.unknownFieldData;
        if (fieldArray == null || (field = fieldArray.get(WireFormatNano.getTagFieldNumber(extension.tag))) == null) {
            return null;
        }
        return (T) field.getValue(extension);
    }

    public final <T> M setExtension(Extension<M, T> extension, T value) {
        int fieldNumber = WireFormatNano.getTagFieldNumber(extension.tag);
        if (value == null) {
            FieldArray fieldArray = this.unknownFieldData;
            if (fieldArray != null) {
                fieldArray.remove(fieldNumber);
                if (this.unknownFieldData.isEmpty()) {
                    this.unknownFieldData = null;
                }
            }
        } else {
            FieldData field = null;
            FieldArray fieldArray2 = this.unknownFieldData;
            if (fieldArray2 == null) {
                this.unknownFieldData = new FieldArray();
            } else {
                field = fieldArray2.get(fieldNumber);
            }
            if (field == null) {
                this.unknownFieldData.put(fieldNumber, new FieldData(extension, value));
            } else {
                field.setValue(extension, value);
            }
        }
        return this;
    }

    /* access modifiers changed from: protected */
    public final boolean storeUnknownField(CodedInputByteBufferNano input, int tag) throws IOException {
        int startPos = input.getPosition();
        if (!input.skipField(tag)) {
            return false;
        }
        int fieldNumber = WireFormatNano.getTagFieldNumber(tag);
        UnknownFieldData unknownField = new UnknownFieldData(tag, input.getData(startPos, input.getPosition() - startPos));
        FieldData field = null;
        FieldArray fieldArray = this.unknownFieldData;
        if (fieldArray == null) {
            this.unknownFieldData = new FieldArray();
        } else {
            field = fieldArray.get(fieldNumber);
        }
        if (field == null) {
            field = new FieldData();
            this.unknownFieldData.put(fieldNumber, field);
        }
        field.addUnknownField(unknownField);
        return true;
    }

    @Override // com.android.phone.ecc.nano.MessageNano, com.android.phone.ecc.nano.MessageNano
    public M clone() throws CloneNotSupportedException {
        M cloned = (M) ((ExtendableMessageNano) super.clone());
        InternalNano.cloneUnknownFieldData(this, cloned);
        return cloned;
    }
}
