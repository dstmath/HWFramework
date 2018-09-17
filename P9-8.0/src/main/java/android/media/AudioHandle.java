package android.media;

class AudioHandle {
    private final int mId;

    AudioHandle(int id) {
        this.mId = id;
    }

    int id() {
        return this.mId;
    }

    public boolean equals(Object o) {
        boolean z = false;
        if (o == null || ((o instanceof AudioHandle) ^ 1) != 0) {
            return false;
        }
        if (this.mId == ((AudioHandle) o).id()) {
            z = true;
        }
        return z;
    }

    public int hashCode() {
        return this.mId;
    }

    public String toString() {
        return Integer.toString(this.mId);
    }
}
