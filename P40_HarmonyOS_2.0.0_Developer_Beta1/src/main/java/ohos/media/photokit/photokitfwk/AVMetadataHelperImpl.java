package ohos.media.photokit.photokitfwk;

import java.io.FileDescriptor;
import java.util.List;
import java.util.Map;
import ohos.app.Context;
import ohos.media.image.PixelMap;
import ohos.media.photokit.adapter.AVMetadataHelperAdapter;
import ohos.media.photokit.common.PixelMapConfigs;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;
import ohos.utils.net.Uri;

public class AVMetadataHelperImpl {
    private static final Logger LOGGER = LoggerFactory.getMediaLogger(AVMetadataHelperImpl.class);
    private final AVMetadataHelperAdapter metadataHelperAdapter = new AVMetadataHelperAdapter();

    public boolean setSource(String str) {
        return this.metadataHelperAdapter.setSource(str);
    }

    public boolean setSource(FileDescriptor fileDescriptor) {
        return this.metadataHelperAdapter.setSource(fileDescriptor);
    }

    public boolean setSource(FileDescriptor fileDescriptor, long j, long j2) {
        return this.metadataHelperAdapter.setSource(fileDescriptor, j, j2);
    }

    public boolean setSource(String str, Map<String, String> map) {
        return this.metadataHelperAdapter.setSource(str, map);
    }

    public boolean setSource(Context context, Uri uri) {
        return this.metadataHelperAdapter.setSource(context, uri);
    }

    public PixelMap fetchVideoScaledPixelMapByTime(long j, int i, int i2, int i3) {
        return this.metadataHelperAdapter.fetchVideoScaledPixelMapByTime(j, i, i2, i3);
    }

    public PixelMap fetchVideoPixelMapByTime(long j, int i) {
        return this.metadataHelperAdapter.fetchVideoPixelMapByTime(j, i);
    }

    public PixelMap fetchVideoPixelMapByTime(long j) {
        return this.metadataHelperAdapter.fetchVideoPixelMapByTime(j);
    }

    public PixelMap fetchVideoPixelMapByTime() {
        return this.metadataHelperAdapter.fetchVideoPixelMapByTime();
    }

    public String resolveMetadata(int i) {
        return this.metadataHelperAdapter.resolveMetadata(i);
    }

    public byte[] resolveImage() {
        return this.metadataHelperAdapter.resolveImage();
    }

    public PixelMap fetchVideoPixelMapByIndex(int i, PixelMapConfigs pixelMapConfigs) {
        return this.metadataHelperAdapter.fetchVideoPixelMapByIndex(i, pixelMapConfigs);
    }

    public PixelMap fetchVideoPixelMapByIndex(int i) {
        return this.metadataHelperAdapter.fetchVideoPixelMapByIndex(i);
    }

    public List<PixelMap> fetchVideoPixelMapsByIndex(int i, int i2, PixelMapConfigs pixelMapConfigs) {
        return this.metadataHelperAdapter.fetchVideoPixelMapsByIndex(i, i2, pixelMapConfigs);
    }

    public List<PixelMap> fetchVideoPixelMapsByIndex(int i, int i2) {
        return this.metadataHelperAdapter.fetchVideoPixelMapsByIndex(i, i2);
    }

    public PixelMap fetchImagePixelMapByIndex(int i, PixelMapConfigs pixelMapConfigs) {
        return this.metadataHelperAdapter.fetchImagePixelMapByIndex(i, pixelMapConfigs);
    }

    public PixelMap fetchImagePixelMapByIndex(int i) {
        return this.metadataHelperAdapter.fetchImagePixelMapByIndex(i);
    }

    public PixelMap fetchImagePrimaryPixelMap(PixelMapConfigs pixelMapConfigs) {
        return this.metadataHelperAdapter.fetchImagePrimaryPixelMap(pixelMapConfigs);
    }

    public PixelMap fetchImagePrimaryPixelMap() {
        return this.metadataHelperAdapter.fetchImagePrimaryPixelMap();
    }

    public void release() {
        this.metadataHelperAdapter.release();
    }
}
