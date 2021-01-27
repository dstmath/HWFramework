package com.huawei.ace.plugin.video;

import com.huawei.ace.runtime.ALog;
import com.huawei.ace.runtime.IAceOnCallResourceMethod;
import com.huawei.ace.runtime.IAceOnResourceEvent;
import java.util.HashMap;
import java.util.Map;

public abstract class AceVideoBase {
    private static final String LOG_TAG = "AceVideoBase";
    private boolean autoPlay = false;
    private Map<String, IAceOnCallResourceMethod> callMethodMap;
    private final IAceOnResourceEvent callback;
    private final long id;
    private boolean isMute = false;

    /* renamed from: getPosition */
    public abstract String lambda$new$3$AceVideoBase(Map<String, String> map);

    public abstract void onActivityPause();

    public abstract void onActivityResume();

    /* renamed from: pause */
    public abstract String lambda$new$2$AceVideoBase(Map<String, String> map);

    public abstract void release();

    /* renamed from: seekTo */
    public abstract String lambda$new$4$AceVideoBase(Map<String, String> map);

    /* renamed from: setVolume */
    public abstract String lambda$new$5$AceVideoBase(Map<String, String> map);

    /* renamed from: start */
    public abstract String lambda$new$1$AceVideoBase(Map<String, String> map);

    public AceVideoBase(long j, IAceOnResourceEvent iAceOnResourceEvent) {
        this.id = j;
        this.callback = iAceOnResourceEvent;
        this.callMethodMap = new HashMap();
        $$Lambda$AceVideoBase$Eq8o_fHp7ChLMsHVCWQeYm1gHrc r7 = new IAceOnCallResourceMethod() {
            /* class com.huawei.ace.plugin.video.$$Lambda$AceVideoBase$Eq8o_fHp7ChLMsHVCWQeYm1gHrc */

            @Override // com.huawei.ace.runtime.IAceOnCallResourceMethod
            public final String onCall(Map map) {
                return AceVideoBase.this.lambda$new$0$AceVideoBase(map);
            }
        };
        Map<String, IAceOnCallResourceMethod> map = this.callMethodMap;
        map.put("video@" + j + "method=init?", r7);
        $$Lambda$AceVideoBase$zd2UcxBmTFf_qkuCvfSTUK9Tkb0 r72 = new IAceOnCallResourceMethod() {
            /* class com.huawei.ace.plugin.video.$$Lambda$AceVideoBase$zd2UcxBmTFf_qkuCvfSTUK9Tkb0 */

            @Override // com.huawei.ace.runtime.IAceOnCallResourceMethod
            public final String onCall(Map map) {
                return AceVideoBase.this.lambda$new$1$AceVideoBase(map);
            }
        };
        Map<String, IAceOnCallResourceMethod> map2 = this.callMethodMap;
        map2.put("video@" + j + "method=start?", r72);
        $$Lambda$AceVideoBase$AcTBwz_vhydVwBSwtzeFAP1Dh44 r73 = new IAceOnCallResourceMethod() {
            /* class com.huawei.ace.plugin.video.$$Lambda$AceVideoBase$AcTBwz_vhydVwBSwtzeFAP1Dh44 */

            @Override // com.huawei.ace.runtime.IAceOnCallResourceMethod
            public final String onCall(Map map) {
                return AceVideoBase.this.lambda$new$2$AceVideoBase(map);
            }
        };
        Map<String, IAceOnCallResourceMethod> map3 = this.callMethodMap;
        map3.put("video@" + j + "method=pause?", r73);
        $$Lambda$AceVideoBase$5AswTeGiaTVrjXXFH3mhBSsBg9Y r74 = new IAceOnCallResourceMethod() {
            /* class com.huawei.ace.plugin.video.$$Lambda$AceVideoBase$5AswTeGiaTVrjXXFH3mhBSsBg9Y */

            @Override // com.huawei.ace.runtime.IAceOnCallResourceMethod
            public final String onCall(Map map) {
                return AceVideoBase.this.lambda$new$3$AceVideoBase(map);
            }
        };
        Map<String, IAceOnCallResourceMethod> map4 = this.callMethodMap;
        map4.put("video@" + j + "method=getposition?", r74);
        $$Lambda$AceVideoBase$Rji6MWQ28RdVvXrL0WLsm9bZsg r75 = new IAceOnCallResourceMethod() {
            /* class com.huawei.ace.plugin.video.$$Lambda$AceVideoBase$Rji6MWQ28RdVvXrL0WLsm9bZsg */

            @Override // com.huawei.ace.runtime.IAceOnCallResourceMethod
            public final String onCall(Map map) {
                return AceVideoBase.this.lambda$new$4$AceVideoBase(map);
            }
        };
        Map<String, IAceOnCallResourceMethod> map5 = this.callMethodMap;
        map5.put("video@" + j + "method=seekto?", r75);
        $$Lambda$AceVideoBase$fKyw3v86Wivm9D_QK5Hbhz1gD48 r76 = new IAceOnCallResourceMethod() {
            /* class com.huawei.ace.plugin.video.$$Lambda$AceVideoBase$fKyw3v86Wivm9D_QK5Hbhz1gD48 */

            @Override // com.huawei.ace.runtime.IAceOnCallResourceMethod
            public final String onCall(Map map) {
                return AceVideoBase.this.lambda$new$5$AceVideoBase(map);
            }
        };
        Map<String, IAceOnCallResourceMethod> map6 = this.callMethodMap;
        map6.put("video@" + j + "method=setvolume?", r76);
    }

    public Map<String, IAceOnCallResourceMethod> getCallMethod() {
        return this.callMethodMap;
    }

    /* renamed from: initMediaPlayer */
    public String lambda$new$0$AceVideoBase(Map<String, String> map) {
        try {
            if (map.containsKey("mute") && Integer.parseInt(map.get("mute")) == 1) {
                this.isMute = true;
            }
            if (!map.containsKey("autoplay") || Integer.parseInt(map.get("autoplay")) != 1) {
                return "success";
            }
            this.autoPlay = true;
            return "success";
        } catch (NumberFormatException unused) {
            ALog.w(LOG_TAG, "NumberFormatException");
            return "fail";
        }
    }

    public long getId() {
        return this.id;
    }

    public boolean isAutoPlay() {
        return this.autoPlay;
    }

    public boolean isMute() {
        return this.isMute;
    }

    public void firePrepared(int i, int i2, int i3, boolean z) {
        String str = "width=" + i + "&height=" + i2 + "&duration=" + i3 + "&isplaying=" + (z ? 1 : 0);
        this.callback.onEvent("video@" + this.id + "event=prepared?", str);
    }

    public void fireError() {
        IAceOnResourceEvent iAceOnResourceEvent = this.callback;
        iAceOnResourceEvent.onEvent("video@" + this.id + "event=error?", "");
    }

    public void fireCompletion() {
        IAceOnResourceEvent iAceOnResourceEvent = this.callback;
        iAceOnResourceEvent.onEvent("video@" + this.id + "event=completion?", "");
    }

    public void fireSeekComplete(int i) {
        this.callback.onEvent("video@" + this.id + "event=seekcomplete?", "currentpos=" + i);
    }

    public void fireBufferingUpdate(int i) {
        this.callback.onEvent("video@" + this.id + "event=bufferingupdate?", "percent=" + i);
    }

    public void firePlayStatusChange(boolean z) {
        this.callback.onEvent("video@" + this.id + "event=onplaystatus?", "isplaying=" + (z ? 1 : 0));
    }
}
