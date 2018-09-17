package android.app;

import android.app.InstantAppResolverService.InstantAppResolutionCallback;
import android.content.pm.EphemeralResolveInfo;
import android.content.pm.InstantAppResolveInfo;
import android.os.Build;
import android.os.Looper;
import android.util.Log;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Deprecated
public abstract class EphemeralResolverService extends InstantAppResolverService {
    private static final boolean DEBUG_EPHEMERAL = Build.IS_DEBUGGABLE;
    private static final String TAG = "PackageManager";

    @Deprecated
    public abstract List<EphemeralResolveInfo> onEphemeralResolveInfoList(int[] iArr, int i);

    public List<EphemeralResolveInfo> onGetEphemeralResolveInfo(int[] digestPrefix) {
        return onEphemeralResolveInfoList(digestPrefix, -4096);
    }

    public EphemeralResolveInfo onGetEphemeralIntentFilter(String hostName) {
        throw new IllegalStateException("Must define");
    }

    public Looper getLooper() {
        return super.getLooper();
    }

    void _onGetInstantAppResolveInfo(int[] digestPrefix, String token, InstantAppResolutionCallback callback) {
        if (DEBUG_EPHEMERAL) {
            Log.d(TAG, "Legacy resolver; getInstantAppResolveInfo; prefix: " + Arrays.toString(digestPrefix));
        }
        List<EphemeralResolveInfo> response = onGetEphemeralResolveInfo(digestPrefix);
        int responseSize = response == null ? 0 : response.size();
        List<InstantAppResolveInfo> resultList = new ArrayList(responseSize);
        for (int i = 0; i < responseSize; i++) {
            resultList.add(((EphemeralResolveInfo) response.get(i)).getInstantAppResolveInfo());
        }
        callback.onInstantAppResolveInfo(resultList);
    }

    void _onGetInstantAppIntentFilter(int[] digestPrefix, String token, String hostName, InstantAppResolutionCallback callback) {
        if (DEBUG_EPHEMERAL) {
            Log.d(TAG, "Legacy resolver; getInstantAppIntentFilter; prefix: " + Arrays.toString(digestPrefix));
        }
        EphemeralResolveInfo response = onGetEphemeralIntentFilter(hostName);
        List<InstantAppResolveInfo> resultList = new ArrayList(1);
        resultList.add(response.getInstantAppResolveInfo());
        callback.onInstantAppResolveInfo(resultList);
    }
}
