package ohos.com.sun.org.apache.bcel.internal.classfile;

import java.io.Serializable;

public abstract class AccessFlags implements Serializable {
    protected int access_flags;

    public AccessFlags() {
    }

    public AccessFlags(int i) {
        this.access_flags = i;
    }

    public final int getAccessFlags() {
        return this.access_flags;
    }

    public final int getModifiers() {
        return this.access_flags;
    }

    public final void setAccessFlags(int i) {
        this.access_flags = i;
    }

    public final void setModifiers(int i) {
        setAccessFlags(i);
    }

    private final void setFlag(int i, boolean z) {
        int i2 = this.access_flags;
        if ((i2 & i) != 0) {
            if (!z) {
                this.access_flags = i ^ i2;
            }
        } else if (z) {
            this.access_flags = i | i2;
        }
    }

    public final void isPublic(boolean z) {
        setFlag(1, z);
    }

    public final boolean isPublic() {
        return (this.access_flags & 1) != 0;
    }

    public final void isPrivate(boolean z) {
        setFlag(2, z);
    }

    public final boolean isPrivate() {
        return (this.access_flags & 2) != 0;
    }

    public final void isProtected(boolean z) {
        setFlag(4, z);
    }

    public final boolean isProtected() {
        return (this.access_flags & 4) != 0;
    }

    public final void isStatic(boolean z) {
        setFlag(8, z);
    }

    public final boolean isStatic() {
        return (this.access_flags & 8) != 0;
    }

    public final void isFinal(boolean z) {
        setFlag(16, z);
    }

    public final boolean isFinal() {
        return (this.access_flags & 16) != 0;
    }

    public final void isSynchronized(boolean z) {
        setFlag(32, z);
    }

    public final boolean isSynchronized() {
        return (this.access_flags & 32) != 0;
    }

    public final void isVolatile(boolean z) {
        setFlag(64, z);
    }

    public final boolean isVolatile() {
        return (this.access_flags & 64) != 0;
    }

    public final void isTransient(boolean z) {
        setFlag(128, z);
    }

    public final boolean isTransient() {
        return (this.access_flags & 128) != 0;
    }

    public final void isNative(boolean z) {
        setFlag(256, z);
    }

    public final boolean isNative() {
        return (this.access_flags & 256) != 0;
    }

    public final void isInterface(boolean z) {
        setFlag(512, z);
    }

    public final boolean isInterface() {
        return (this.access_flags & 512) != 0;
    }

    public final void isAbstract(boolean z) {
        setFlag(1024, z);
    }

    public final boolean isAbstract() {
        return (this.access_flags & 1024) != 0;
    }

    public final void isStrictfp(boolean z) {
        setFlag(2048, z);
    }

    public final boolean isStrictfp() {
        return (this.access_flags & 2048) != 0;
    }
}
