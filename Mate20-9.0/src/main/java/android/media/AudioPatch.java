package android.media;

public class AudioPatch {
    private final AudioHandle mHandle;
    private final AudioPortConfig[] mSinks;
    private final AudioPortConfig[] mSources;

    AudioPatch(AudioHandle patchHandle, AudioPortConfig[] sources, AudioPortConfig[] sinks) {
        this.mHandle = patchHandle;
        this.mSources = sources;
        this.mSinks = sinks;
    }

    public AudioPortConfig[] sources() {
        return this.mSources;
    }

    public AudioPortConfig[] sinks() {
        return this.mSinks;
    }

    public int id() {
        return this.mHandle.id();
    }

    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append("mHandle: ");
        s.append(this.mHandle.toString());
        s.append(" mSources: {");
        for (AudioPortConfig source : this.mSources) {
            s.append(source.toString());
            s.append(", ");
        }
        s.append("} mSinks: {");
        for (AudioPortConfig sink : this.mSinks) {
            s.append(sink.toString());
            s.append(", ");
        }
        s.append("}");
        return s.toString();
    }
}
