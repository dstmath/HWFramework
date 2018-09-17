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
        int i = 0;
        StringBuilder s = new StringBuilder();
        s.append("mHandle: ");
        s.append(this.mHandle.toString());
        s.append(" mSources: {");
        for (AudioPortConfig source : this.mSources) {
            s.append(source.toString());
            s.append(", ");
        }
        s.append("} mSinks: {");
        AudioPortConfig[] audioPortConfigArr = this.mSinks;
        int length = audioPortConfigArr.length;
        while (i < length) {
            s.append(audioPortConfigArr[i].toString());
            s.append(", ");
            i++;
        }
        s.append("}");
        return s.toString();
    }
}
