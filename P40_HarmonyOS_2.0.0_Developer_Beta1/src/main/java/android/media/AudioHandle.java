package android.media;

import android.annotation.UnsupportedAppUsage;

/* access modifiers changed from: package-private */
public class AudioHandle {
    @UnsupportedAppUsage
    private final int mId;

    @UnsupportedAppUsage
    AudioHandle(int id) {
        this.mId = id;
    }

    /* access modifiers changed from: package-private */
    public int id() {
        return this.mId;
    }

    public boolean equals(Object o) {
        if (o == null || !(o instanceof AudioHandle) || this.mId != ((AudioHandle) o).id()) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        return this.mId;
    }

    public String toString() {
        return Integer.toString(this.mId);
    }
}
