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
        public int orientation;

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
            this.cachedSize = -1;
            return this;
        }

        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            if (this.orientation != 0) {
                output.writeInt32(1, this.orientation);
            }
            if (this.insetLeft != 0) {
                output.writeInt32(2, this.insetLeft);
            }
            if (this.insetTop != 0) {
                output.writeInt32(3, this.insetTop);
            }
            if (this.insetRight != 0) {
                output.writeInt32(4, this.insetRight);
            }
            if (this.insetBottom != 0) {
                output.writeInt32(5, this.insetBottom);
            }
            super.writeTo(output);
        }

        protected int computeSerializedSize() {
            int size = super.computeSerializedSize();
            if (this.orientation != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(1, this.orientation);
            }
            if (this.insetLeft != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(2, this.insetLeft);
            }
            if (this.insetTop != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(3, this.insetTop);
            }
            if (this.insetRight != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(4, this.insetRight);
            }
            if (this.insetBottom != 0) {
                return size + CodedOutputByteBufferNano.computeInt32Size(5, this.insetBottom);
            }
            return size;
        }

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
                    default:
                        if (WireFormatNano.parseUnknownField(input, tag)) {
                            break;
                        }
                        return this;
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
