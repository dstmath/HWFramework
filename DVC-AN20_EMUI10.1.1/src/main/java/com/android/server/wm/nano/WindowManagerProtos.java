package com.android.server.wm.nano;

import com.android.framework.protobuf.nano.CodedInputByteBufferNano;
import com.android.framework.protobuf.nano.CodedOutputByteBufferNano;
import com.android.framework.protobuf.nano.InternalNano;
import com.android.framework.protobuf.nano.InvalidProtocolBufferNanoException;
import com.android.framework.protobuf.nano.MessageNano;
import com.android.framework.protobuf.nano.WireFormatNano;
import java.io.IOException;

public interface WindowManagerProtos {

    public static final class TaskSnapshotProto extends MessageNano {
        private static volatile TaskSnapshotProto[] _emptyArray;
        public int insetBottom;
        public int insetLeft;
        public int insetRight;
        public int insetTop;
        public boolean isRealSnapshot;
        public boolean isTranslucent;
        public int orientation;
        public float scale;
        public int systemUiVisibility;
        public String topActivityComponent;
        public int windowingMode;

        public static TaskSnapshotProto[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new TaskSnapshotProto[0];
                    }
                }
            }
            return _emptyArray;
        }

        public TaskSnapshotProto() {
            clear();
        }

        public TaskSnapshotProto clear() {
            this.orientation = 0;
            this.insetLeft = 0;
            this.insetTop = 0;
            this.insetRight = 0;
            this.insetBottom = 0;
            this.isRealSnapshot = false;
            this.windowingMode = 0;
            this.systemUiVisibility = 0;
            this.isTranslucent = false;
            this.topActivityComponent = "";
            this.scale = 0.0f;
            this.cachedSize = -1;
            return this;
        }

        @Override // com.android.framework.protobuf.nano.MessageNano
        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            int i = this.orientation;
            if (i != 0) {
                output.writeInt32(1, i);
            }
            int i2 = this.insetLeft;
            if (i2 != 0) {
                output.writeInt32(2, i2);
            }
            int i3 = this.insetTop;
            if (i3 != 0) {
                output.writeInt32(3, i3);
            }
            int i4 = this.insetRight;
            if (i4 != 0) {
                output.writeInt32(4, i4);
            }
            int i5 = this.insetBottom;
            if (i5 != 0) {
                output.writeInt32(5, i5);
            }
            boolean z = this.isRealSnapshot;
            if (z) {
                output.writeBool(6, z);
            }
            int i6 = this.windowingMode;
            if (i6 != 0) {
                output.writeInt32(7, i6);
            }
            int i7 = this.systemUiVisibility;
            if (i7 != 0) {
                output.writeInt32(8, i7);
            }
            boolean z2 = this.isTranslucent;
            if (z2) {
                output.writeBool(9, z2);
            }
            if (!this.topActivityComponent.equals("")) {
                output.writeString(10, this.topActivityComponent);
            }
            if (Float.floatToIntBits(this.scale) != Float.floatToIntBits(0.0f)) {
                output.writeFloat(11, this.scale);
            }
            super.writeTo(output);
        }

        /* access modifiers changed from: protected */
        @Override // com.android.framework.protobuf.nano.MessageNano
        public int computeSerializedSize() {
            int size = super.computeSerializedSize();
            int i = this.orientation;
            if (i != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(1, i);
            }
            int i2 = this.insetLeft;
            if (i2 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(2, i2);
            }
            int i3 = this.insetTop;
            if (i3 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(3, i3);
            }
            int i4 = this.insetRight;
            if (i4 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(4, i4);
            }
            int i5 = this.insetBottom;
            if (i5 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(5, i5);
            }
            boolean z = this.isRealSnapshot;
            if (z) {
                size += CodedOutputByteBufferNano.computeBoolSize(6, z);
            }
            int i6 = this.windowingMode;
            if (i6 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(7, i6);
            }
            int i7 = this.systemUiVisibility;
            if (i7 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(8, i7);
            }
            boolean z2 = this.isTranslucent;
            if (z2) {
                size += CodedOutputByteBufferNano.computeBoolSize(9, z2);
            }
            if (!this.topActivityComponent.equals("")) {
                size += CodedOutputByteBufferNano.computeStringSize(10, this.topActivityComponent);
            }
            if (Float.floatToIntBits(this.scale) != Float.floatToIntBits(0.0f)) {
                return size + CodedOutputByteBufferNano.computeFloatSize(11, this.scale);
            }
            return size;
        }

        @Override // com.android.framework.protobuf.nano.MessageNano
        public TaskSnapshotProto mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                switch (tag) {
                    case 0:
                        return this;
                    case 8:
                        this.orientation = input.readInt32();
                        break;
                    case 16:
                        this.insetLeft = input.readInt32();
                        break;
                    case 24:
                        this.insetTop = input.readInt32();
                        break;
                    case 32:
                        this.insetRight = input.readInt32();
                        break;
                    case 40:
                        this.insetBottom = input.readInt32();
                        break;
                    case 48:
                        this.isRealSnapshot = input.readBool();
                        break;
                    case 56:
                        this.windowingMode = input.readInt32();
                        break;
                    case 64:
                        this.systemUiVisibility = input.readInt32();
                        break;
                    case 72:
                        this.isTranslucent = input.readBool();
                        break;
                    case 82:
                        this.topActivityComponent = input.readString();
                        break;
                    case 93:
                        this.scale = input.readFloat();
                        break;
                    default:
                        if (WireFormatNano.parseUnknownField(input, tag)) {
                            break;
                        } else {
                            return this;
                        }
                }
            }
        }

        public static TaskSnapshotProto parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (TaskSnapshotProto) MessageNano.mergeFrom(new TaskSnapshotProto(), data);
        }

        public static TaskSnapshotProto parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new TaskSnapshotProto().mergeFrom(input);
        }
    }
}
