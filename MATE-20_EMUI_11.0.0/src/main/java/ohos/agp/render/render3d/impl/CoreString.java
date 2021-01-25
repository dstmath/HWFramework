package ohos.agp.render.render3d.impl;

class CoreString {
    private transient long agpCptr;

    CoreString(long j, boolean z) {
        this.agpCptr = j;
    }

    CoreString() {
        this(0, false);
    }

    static long getCptr(CoreString coreString) {
        if (coreString == null) {
            return 0;
        }
        return coreString.agpCptr;
    }
}
