package ohos.media.sessioncore.adapter;

import android.media.VolumeProvider;
import java.lang.reflect.InvocationTargetException;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;

public class AVVolumeControlAdapter {
    private static final Logger LOGGER = LoggerFactory.getMediaLogger(AVVolumeControlAdapter.class);
    private final Object volumeControlObj;
    private final VolumeProvider volumeProvider;

    public AVVolumeControlAdapter(Object obj, int i, int i2, int i3) {
        this.volumeControlObj = obj;
        this.volumeProvider = new VolumeProvider(i, i2, i3) {
            /* class ohos.media.sessioncore.adapter.AVVolumeControlAdapter.AnonymousClass1 */

            @Override // android.media.VolumeProvider
            public void onSetVolumeTo(int i) {
                AVVolumeControlAdapter.this.adapterSetVolumeTo(i);
            }

            @Override // android.media.VolumeProvider
            public void onAdjustVolume(int i) {
                AVVolumeControlAdapter.this.adapterAdjustVolume(i);
            }
        };
    }

    public final int getAVPlaybackVolumeType() {
        return this.volumeProvider.getVolumeControl();
    }

    public final int getAVPlaybackMaxVolume() {
        return this.volumeProvider.getMaxVolume();
    }

    public final int getAVPlaybackCurrentVolume() {
        return this.volumeProvider.getCurrentVolume();
    }

    public final void setAVPlaybackCurrentVolume(int i) {
        this.volumeProvider.setCurrentVolume(i);
    }

    public Object getHostVolumeControl() {
        return this.volumeProvider;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void adapterAdjustVolume(int i) {
        try {
            if (this.volumeControlObj == null) {
                LOGGER.error("get method onAdjustAVPlaybackVolume failed, volume obj is null", new Object[0]);
            } else {
                this.volumeControlObj.getClass().getMethod("onAdjustAVPlaybackVolume", Integer.TYPE).invoke(this.volumeControlObj, Integer.valueOf(i));
            }
        } catch (NoSuchMethodException unused) {
            LOGGER.error("get method onAdjustAVPlaybackVolume failed", new Object[0]);
        } catch (IllegalAccessException unused2) {
            LOGGER.error("cannot call method onAdjustAVPlaybackVolume", new Object[0]);
        } catch (InvocationTargetException unused3) {
            LOGGER.error("callmethod onAdjustAVPlaybackVolume failed", new Object[0]);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void adapterSetVolumeTo(int i) {
        try {
            if (this.volumeControlObj == null) {
                LOGGER.error("adapterSetVolumeTo failed, volume obj is null", new Object[0]);
            } else {
                this.volumeControlObj.getClass().getMethod("onSetAVPlaybackVolumeTo", Integer.TYPE).invoke(this.volumeControlObj, Integer.valueOf(i));
            }
        } catch (NoSuchMethodException unused) {
            LOGGER.error("get method onSetAVPlaybackVolumeTo failed", new Object[0]);
        } catch (IllegalAccessException unused2) {
            LOGGER.error("cannot call method onSetAVPlaybackVolumeTo", new Object[0]);
        } catch (InvocationTargetException unused3) {
            LOGGER.error("call method onSetAVPlaybackVolumeTo failed", new Object[0]);
        }
    }
}
