package ohos.media.photokit.adapter;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import java.io.FileDescriptor;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import ohos.app.Context;
import ohos.media.image.PixelMap;
import ohos.media.image.common.PixelFormat;
import ohos.media.image.inner.ImageDoubleFwConverter;
import ohos.media.photokit.common.PixelMapConfigs;
import ohos.utils.net.Uri;

public class AVMetadataHelperAdapter {
    private final MediaMetadataRetriever retriever = new MediaMetadataRetriever();

    public boolean setSource(String str) {
        this.retriever.setDataSource(str);
        return true;
    }

    public boolean setSource(FileDescriptor fileDescriptor) {
        this.retriever.setDataSource(fileDescriptor);
        return true;
    }

    public boolean setSource(FileDescriptor fileDescriptor, long j, long j2) {
        this.retriever.setDataSource(fileDescriptor, j, j2);
        return true;
    }

    public boolean setSource(String str, Map<String, String> map) {
        this.retriever.setDataSource(str, map);
        return true;
    }

    public boolean setSource(Context context, Uri uri) {
        this.retriever.setDataSource((android.content.Context) context.getHostContext(), android.net.Uri.parse(uri.toString()));
        return true;
    }

    public PixelMap fetchVideoScaledPixelMapByTime(long j, int i, int i2, int i3) {
        return ImageDoubleFwConverter.createShellPixelMap(this.retriever.getScaledFrameAtTime(j, i, i2, i3));
    }

    public PixelMap fetchVideoPixelMapByTime(long j, int i) {
        return ImageDoubleFwConverter.createShellPixelMap(this.retriever.getFrameAtTime(j, i));
    }

    public PixelMap fetchVideoPixelMapByTime(long j) {
        return ImageDoubleFwConverter.createShellPixelMap(this.retriever.getFrameAtTime(j));
    }

    public PixelMap fetchVideoPixelMapByTime() {
        return ImageDoubleFwConverter.createShellPixelMap(this.retriever.getFrameAtTime());
    }

    public String resolveMetadata(int i) {
        return this.retriever.extractMetadata(i);
    }

    public byte[] resolveImage() {
        return this.retriever.getEmbeddedPicture();
    }

    public PixelMap fetchVideoPixelMapByIndex(int i, PixelMapConfigs pixelMapConfigs) {
        return fetchVideoPixelMapsByIndex(i, 1, pixelMapConfigs).get(0);
    }

    public PixelMap fetchVideoPixelMapByIndex(int i) {
        return fetchVideoPixelMapsByIndex(i, 1).get(0);
    }

    public List<PixelMap> fetchVideoPixelMapsByIndex(int i, int i2, PixelMapConfigs pixelMapConfigs) {
        return getFramesAtIndexInternal(i, i2, pixelMapConfigs);
    }

    public List<PixelMap> fetchVideoPixelMapsByIndex(int i, int i2) {
        return getFramesAtIndexInternal(i, i2, null);
    }

    private List<PixelMap> getFramesAtIndexInternal(int i, int i2, PixelMapConfigs pixelMapConfigs) {
        return (List) this.retriever.getFramesAtIndex(i, i2, convertPixelMapConfigsToBitmapParams(pixelMapConfigs)).stream().map($$Lambda$UgcuZ9GI5zEleF3oROMthyyZ2U.INSTANCE).collect(Collectors.toList());
    }

    private MediaMetadataRetriever.BitmapParams convertPixelMapConfigsToBitmapParams(PixelMapConfigs pixelMapConfigs) {
        MediaMetadataRetriever.BitmapParams bitmapParams = new MediaMetadataRetriever.BitmapParams();
        if (pixelMapConfigs == null) {
            return bitmapParams;
        }
        int i = AnonymousClass1.$SwitchMap$ohos$media$image$common$PixelFormat[pixelMapConfigs.getPreferredConfig().ordinal()];
        if (i == 1) {
            bitmapParams.setPreferredConfig(Bitmap.Config.ARGB_8888);
        } else if (i == 2) {
            bitmapParams.setPreferredConfig(Bitmap.Config.RGB_565);
        }
        return bitmapParams;
    }

    /* access modifiers changed from: package-private */
    /* renamed from: ohos.media.photokit.adapter.AVMetadataHelperAdapter$1  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$ohos$media$image$common$PixelFormat = new int[PixelFormat.values().length];

        static {
            try {
                $SwitchMap$ohos$media$image$common$PixelFormat[PixelFormat.ARGB_8888.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$ohos$media$image$common$PixelFormat[PixelFormat.RGB_565.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
        }
    }

    public PixelMap fetchImagePixelMapByIndex(int i, PixelMapConfigs pixelMapConfigs) {
        return getImageAtIndexInternal(i, pixelMapConfigs);
    }

    public PixelMap fetchImagePixelMapByIndex(int i) {
        return getImageAtIndexInternal(i, null);
    }

    public PixelMap fetchImagePrimaryPixelMap(PixelMapConfigs pixelMapConfigs) {
        return getImageAtIndexInternal(-1, pixelMapConfigs);
    }

    public PixelMap fetchImagePrimaryPixelMap() {
        return getImageAtIndexInternal(-1, null);
    }

    private PixelMap getImageAtIndexInternal(int i, PixelMapConfigs pixelMapConfigs) {
        return ImageDoubleFwConverter.createShellPixelMap(this.retriever.getImageAtIndex(i, convertPixelMapConfigsToBitmapParams(pixelMapConfigs)));
    }

    public void release() {
        this.retriever.close();
    }
}
