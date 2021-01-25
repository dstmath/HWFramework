package ohos.agp.render.render3d.impl;

/* access modifiers changed from: package-private */
public class CoreComponentManagerTypeInfoArray {
    private transient long agpCptr;
    transient boolean isAgpCmemOwn;

    CoreComponentManagerTypeInfoArray(long j, boolean z) {
        this.isAgpCmemOwn = z;
        this.agpCptr = j;
    }

    static long getCptr(CoreComponentManagerTypeInfoArray coreComponentManagerTypeInfoArray) {
        if (coreComponentManagerTypeInfoArray == null) {
            return 0;
        }
        return coreComponentManagerTypeInfoArray.agpCptr;
    }

    static long getCptrAndSetMemOwn(CoreComponentManagerTypeInfoArray coreComponentManagerTypeInfoArray, boolean z) {
        if (coreComponentManagerTypeInfoArray != null) {
            coreComponentManagerTypeInfoArray.isAgpCmemOwn = z;
        }
        return getCptr(coreComponentManagerTypeInfoArray);
    }
}
