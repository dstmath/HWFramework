package android.system;

import libcore.util.Objects;

public final class StructTimeval {
    public final long tv_sec;
    public final long tv_usec;

    private StructTimeval(long tv_sec2, long tv_usec2) {
        this.tv_sec = tv_sec2;
        this.tv_usec = tv_usec2;
    }

    public static StructTimeval fromMillis(long millis) {
        long tv_sec2 = millis / 1000;
        return new StructTimeval(tv_sec2, (millis - (tv_sec2 * 1000)) * 1000);
    }

    public long toMillis() {
        return (this.tv_sec * 1000) + (this.tv_usec / 1000);
    }

    public String toString() {
        return Objects.toString(this);
    }
}
