package ohos.media.sessioncore.adapter;

import android.content.Context;
import android.content.pm.ParceledListSlice;
import android.media.MediaDescription;
import android.media.browse.MediaBrowser;
import android.media.session.MediaSession;
import android.media.session.MediaSessionManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.service.media.MediaBrowserService;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import ohos.app.GeneralReceiver;
import ohos.media.common.AVDescription;
import ohos.media.common.adapter.AVCallerUserInfoAdapter;
import ohos.media.common.adapter.AVDescriptionAdapter;
import ohos.media.common.sessioncore.AVBrowserRoot;
import ohos.media.common.sessioncore.AVCallerUserInfo;
import ohos.media.common.sessioncore.AVElement;
import ohos.media.common.sessioncore.AVToken;
import ohos.media.common.utils.AVUtils;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;
import ohos.utils.PacMap;

public class AVConverter {
    private static final Logger LOGGER = LoggerFactory.getMediaLogger(AVConverter.class);

    public static ResultReceiver convert2ResultReceiver(final GeneralReceiver generalReceiver, Handler handler) {
        return new ResultReceiver(handler) {
            /* class ohos.media.sessioncore.adapter.AVConverter.AnonymousClass1 */

            /* access modifiers changed from: protected */
            @Override // android.os.ResultReceiver
            public void onReceiveResult(int i, Bundle bundle) {
                reflectOnReceive(i, AVUtils.convert2PacMap(bundle));
            }

            private void reflectOnReceive(int i, PacMap pacMap) {
                Method method = null;
                try {
                    method = generalReceiver.getClass().getDeclaredMethod("onReceive", Integer.TYPE, PacMap.class);
                    method.setAccessible(true);
                    method.invoke(generalReceiver, Integer.valueOf(i), pacMap);
                } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                    AVConverter.LOGGER.warn("reflectOnReceive failed, e: %{public}s", e);
                    if (method == null) {
                        return;
                    }
                } catch (Throwable th) {
                    if (method != null) {
                        method.setAccessible(false);
                    }
                    throw th;
                }
                method.setAccessible(false);
            }
        };
    }

    public static Context convertContext(ohos.app.Context context) {
        Object hostContext = context.getHostContext();
        if (!(hostContext instanceof Context)) {
            return null;
        }
        return (Context) hostContext;
    }

    public static AVToken convertToken(MediaSession.Token token) {
        if (token == null) {
            return null;
        }
        return new AVToken(token);
    }

    public static MediaSession.Token convertToken(AVToken aVToken) {
        Object hostAVToken = aVToken.getHostAVToken();
        if (hostAVToken instanceof MediaSession.Token) {
            return (MediaSession.Token) hostAVToken;
        }
        LOGGER.warn("convertToken instance error, return null", new Object[0]);
        return null;
    }

    public static List<AVElement> convert2MediaItemList(ParceledListSlice<?> parceledListSlice) {
        ArrayList arrayList = new ArrayList();
        for (Object obj : parceledListSlice.getList()) {
            if (obj instanceof MediaBrowser.MediaItem) {
                arrayList.add(convertItem((MediaBrowser.MediaItem) obj));
            }
        }
        return arrayList;
    }

    private static AVElement convertItem(MediaBrowser.MediaItem mediaItem) {
        return new AVElement(convertDescription(mediaItem.getDescription()), mediaItem.getFlags());
    }

    private static AVDescription convertDescription(MediaDescription mediaDescription) {
        return AVDescriptionAdapter.getAVDescription(mediaDescription);
    }

    public static MediaBrowserService.BrowserRoot convertBrowserRoot(AVBrowserRoot aVBrowserRoot) {
        return new MediaBrowserService.BrowserRoot(aVBrowserRoot.getRootMediaId(), AVUtils.convert2Bundle(aVBrowserRoot.getOptions()));
    }

    public static AVCallerUserInfo convertRemoteUserInfo(MediaSessionManager.RemoteUserInfo remoteUserInfo) {
        return AVCallerUserInfoAdapter.getAVCallerUserInfo(remoteUserInfo);
    }
}
