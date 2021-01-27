package ohos.aafwk.ability;

public class MemoryInfo {
    private int arkPrivateDirty;
    private int arkPss;
    private int arkSharedDirty;
    private int nativePrivateDirty;
    private int nativePss;
    private int nativeSharedDirty;
    private int otherPrivateDirty;
    private int otherPss;
    private int otherSharedDirty;

    public void setArkPss(int i) {
        this.arkPss = i;
    }

    public void setArkPrivateDirty(int i) {
        this.arkPrivateDirty = i;
    }

    public void setArkSharedDirty(int i) {
        this.arkSharedDirty = i;
    }

    public void setNativePrivateDirty(int i) {
        this.nativePrivateDirty = i;
    }

    public void setNativePss(int i) {
        this.nativePss = i;
    }

    public void setNativeSharedDirty(int i) {
        this.nativeSharedDirty = i;
    }

    public void setOtherPrivateDirty(int i) {
        this.otherPrivateDirty = i;
    }

    public void setOtherPss(int i) {
        this.otherPss = i;
    }

    public void setOtherSharedDirty(int i) {
        this.otherSharedDirty = i;
    }

    public int getArkPss() {
        return this.arkPss;
    }

    public int getArkPrivateDirty() {
        return this.arkPrivateDirty;
    }

    public int getArkSharedDirty() {
        return this.arkSharedDirty;
    }

    public int getNativePrivateDirty() {
        return this.nativePrivateDirty;
    }

    public int getNativePss() {
        return this.nativePss;
    }

    public int getNativeSharedDirty() {
        return this.nativeSharedDirty;
    }

    public int getOtherPrivateDirty() {
        return this.otherPrivateDirty;
    }

    public int getOtherPss() {
        return this.otherPss;
    }

    public int getOtherSharedDirty() {
        return this.otherSharedDirty;
    }

    public String toString() {
        return "arkPss = " + this.arkPss + "; arkPrivateDirty = " + this.arkPrivateDirty + "; arkSharedDirty = " + this.arkSharedDirty + "; nativePrivateDirty = " + this.nativePrivateDirty + "; nativePss = " + this.nativePss + "; nativeSharedDirty = " + this.nativeSharedDirty + "; otherPrivateDirty = " + this.otherPrivateDirty + "; otherPss = " + this.otherPss + "; otherSharedDirty = " + this.otherSharedDirty + "; ";
    }
}
