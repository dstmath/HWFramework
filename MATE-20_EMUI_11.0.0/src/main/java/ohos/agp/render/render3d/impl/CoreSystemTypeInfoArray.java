package ohos.agp.render.render3d.impl;

/* access modifiers changed from: package-private */
public class CoreSystemTypeInfoArray {
    private transient long agpCptr;
    transient boolean isAgpCmemOwn;

    CoreSystemTypeInfoArray(long j, boolean z) {
        this.isAgpCmemOwn = z;
        this.agpCptr = j;
    }

    static long getCptr(CoreSystemTypeInfoArray coreSystemTypeInfoArray) {
        if (coreSystemTypeInfoArray == null) {
            return 0;
        }
        return coreSystemTypeInfoArray.agpCptr;
    }

    static long getCptrAndSetMemOwn(CoreSystemTypeInfoArray coreSystemTypeInfoArray, boolean z) {
        if (coreSystemTypeInfoArray != null) {
            coreSystemTypeInfoArray.isAgpCmemOwn = z;
        }
        return getCptr(coreSystemTypeInfoArray);
    }
}
