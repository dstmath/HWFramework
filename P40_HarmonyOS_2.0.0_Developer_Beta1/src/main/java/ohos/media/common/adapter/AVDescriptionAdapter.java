package ohos.media.common.adapter;

import android.media.MediaDescription;
import android.net.Uri;
import ohos.media.common.AVDescription;
import ohos.media.image.inner.ImageDoubleFwConverter;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;
import ohos.utils.adapter.PacMapUtils;

public class AVDescriptionAdapter {
    private static final Logger LOGGER = LoggerFactory.getImageLogger(AVDescriptionAdapter.class);

    public static MediaDescription getMediaDescription(AVDescription aVDescription) {
        Uri uri;
        Uri uri2 = null;
        if (aVDescription == null) {
            LOGGER.error("getMediaDescription failed, avDescription is null", new Object[0]);
            return null;
        }
        MediaDescription.Builder iconBitmap = new MediaDescription.Builder().setMediaId(aVDescription.getMediaId()).setTitle(aVDescription.getTitle()).setSubtitle(aVDescription.getSubTitle()).setDescription(aVDescription.getDescription()).setIconBitmap(ImageDoubleFwConverter.createShadowBitmap(aVDescription.getIcon()));
        if (aVDescription.getIconUri() == null) {
            uri = null;
        } else {
            uri = Uri.parse(aVDescription.getIconUri().toString());
        }
        MediaDescription.Builder iconUri = iconBitmap.setIconUri(uri);
        if (aVDescription.getMediaUri() != null) {
            uri2 = Uri.parse(aVDescription.getMediaUri().toString());
        }
        return iconUri.setMediaUri(uri2).setExtras(PacMapUtils.convertIntoBundle(aVDescription.getExtras())).build();
    }

    public static AVDescription getAVDescription(MediaDescription mediaDescription) {
        ohos.utils.net.Uri uri = null;
        if (mediaDescription == null) {
            LOGGER.error("getAVDescription failed, mediaDescription is null", new Object[0]);
            return null;
        }
        AVDescription.Builder iconUri = new AVDescription.Builder().setMediaId(mediaDescription.getMediaId()).setTitle(mediaDescription.getTitle()).setSubTitle(mediaDescription.getSubtitle()).setDescription(mediaDescription.getDescription()).setIcon(ImageDoubleFwConverter.createShellPixelMap(mediaDescription.getIconBitmap())).setIconUri(mediaDescription.getIconUri() == null ? null : ohos.utils.net.Uri.parse(mediaDescription.getIconUri().toString()));
        if (mediaDescription.getMediaUri() != null) {
            uri = ohos.utils.net.Uri.parse(mediaDescription.getMediaUri().toString());
        }
        return iconUri.setIMediaUri(uri).setExtras(PacMapUtils.convertFromBundle(mediaDescription.getExtras())).build();
    }
}
