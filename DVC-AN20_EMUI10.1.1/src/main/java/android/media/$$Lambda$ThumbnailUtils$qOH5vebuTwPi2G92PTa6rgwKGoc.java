package android.media;

import java.io.File;
import java.util.function.ToIntFunction;

/* renamed from: android.media.-$$Lambda$ThumbnailUtils$qOH5vebuTwPi2G92PTa6rgwKGoc  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$ThumbnailUtils$qOH5vebuTwPi2G92PTa6rgwKGoc implements ToIntFunction {
    public static final /* synthetic */ $$Lambda$ThumbnailUtils$qOH5vebuTwPi2G92PTa6rgwKGoc INSTANCE = new $$Lambda$ThumbnailUtils$qOH5vebuTwPi2G92PTa6rgwKGoc();

    private /* synthetic */ $$Lambda$ThumbnailUtils$qOH5vebuTwPi2G92PTa6rgwKGoc() {
    }

    @Override // java.util.function.ToIntFunction
    public final int applyAsInt(Object obj) {
        return ThumbnailUtils.lambda$createAudioThumbnail$1((File) obj);
    }
}
