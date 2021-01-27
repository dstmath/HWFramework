package android.preference;

import android.media.audiopolicy.AudioProductStrategy;
import java.util.function.Function;

/* renamed from: android.preference.-$$Lambda$SeekBarVolumizer$ezNr2aLs33rVlzIsAVW8OXqqpIs  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$SeekBarVolumizer$ezNr2aLs33rVlzIsAVW8OXqqpIs implements Function {
    public static final /* synthetic */ $$Lambda$SeekBarVolumizer$ezNr2aLs33rVlzIsAVW8OXqqpIs INSTANCE = new $$Lambda$SeekBarVolumizer$ezNr2aLs33rVlzIsAVW8OXqqpIs();

    private /* synthetic */ $$Lambda$SeekBarVolumizer$ezNr2aLs33rVlzIsAVW8OXqqpIs() {
    }

    @Override // java.util.function.Function
    public final Object apply(Object obj) {
        return Integer.valueOf(((AudioProductStrategy) obj).getVolumeGroupIdForAudioAttributes(AudioProductStrategy.sDefaultAttributes));
    }
}
