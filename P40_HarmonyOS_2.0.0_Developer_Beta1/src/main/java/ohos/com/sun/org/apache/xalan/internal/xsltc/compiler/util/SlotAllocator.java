package ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util;

import ohos.com.sun.org.apache.bcel.internal.generic.LocalVariableGen;
import ohos.com.sun.org.apache.bcel.internal.generic.Type;

/* access modifiers changed from: package-private */
public final class SlotAllocator {
    private int _firstAvailableSlot;
    private int _free = 0;
    private int _size = 8;
    private int[] _slotsTaken = new int[this._size];

    SlotAllocator() {
    }

    public void initialize(LocalVariableGen[] localVariableGenArr) {
        int length = localVariableGenArr.length;
        int i = 0;
        for (int i2 = 0; i2 < length; i2++) {
            i = Math.max(i, localVariableGenArr[i2].getIndex() + localVariableGenArr[i2].getType().getSize());
        }
        this._firstAvailableSlot = i;
    }

    public int allocateSlot(Type type) {
        int size = type.getSize();
        int i = this._free;
        int i2 = this._firstAvailableSlot;
        int i3 = i + size;
        int i4 = this._size;
        if (i3 > i4) {
            int i5 = i4 * 2;
            this._size = i5;
            int[] iArr = new int[i5];
            for (int i6 = 0; i6 < i; i6++) {
                iArr[i6] = this._slotsTaken[i6];
            }
            this._slotsTaken = iArr;
        }
        int i7 = i2;
        int i8 = 0;
        while (true) {
            if (i8 >= i) {
                break;
            }
            int i9 = i7 + size;
            int[] iArr2 = this._slotsTaken;
            if (i9 <= iArr2[i8]) {
                for (int i10 = i - 1; i10 >= i8; i10--) {
                    int[] iArr3 = this._slotsTaken;
                    iArr3[i10 + size] = iArr3[i10];
                }
            } else {
                i7 = iArr2[i8] + 1;
                i8++;
            }
        }
        for (int i11 = 0; i11 < size; i11++) {
            this._slotsTaken[i8 + i11] = i7 + i11;
        }
        this._free += size;
        return i7;
    }

    public void releaseSlot(LocalVariableGen localVariableGen) {
        int size = localVariableGen.getType().getSize();
        int index = localVariableGen.getIndex();
        int i = this._free;
        int i2 = 0;
        while (i2 < i) {
            if (this._slotsTaken[i2] == index) {
                for (int i3 = i2 + size; i3 < i; i3++) {
                    int[] iArr = this._slotsTaken;
                    iArr[i2] = iArr[i3];
                    i2++;
                }
                this._free -= size;
                return;
            }
            i2++;
        }
        throw new Error(new ErrorMsg(ErrorMsg.INTERNAL_ERR, "Variable slot allocation error(size=" + size + ", slot=" + index + ", limit=" + i + ")").toString());
    }
}
