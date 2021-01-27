package com.android.server.wifi.hotspot2;

import android.util.Log;
import android.util.Pair;
import com.android.server.wifi.WifiNative;
import com.android.server.wifi.hotspot2.anqp.ANQPElement;
import com.android.server.wifi.hotspot2.anqp.Constants;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PasspointEventHandler {
    private final Callbacks mCallbacks;
    private final WifiNative mSupplicantHook;

    public interface Callbacks {
        void onANQPResponse(long j, Map<Constants.ANQPElementType, ANQPElement> map);

        void onIconResponse(long j, String str, byte[] bArr);

        void onWnmFrameReceived(WnmData wnmData);
    }

    public PasspointEventHandler(WifiNative supplicantHook, Callbacks callbacks) {
        this.mSupplicantHook = supplicantHook;
        this.mCallbacks = callbacks;
    }

    public boolean requestANQP(long bssid, List<Constants.ANQPElementType> elements) {
        Pair<Set<Integer>, Set<Integer>> querySets = buildAnqpIdSet(elements);
        if (bssid == 0 || querySets == null) {
            return false;
        }
        WifiNative wifiNative = this.mSupplicantHook;
        if (!wifiNative.requestAnqp(wifiNative.getClientInterfaceName(), Utils.macToString(bssid), (Set) querySets.first, (Set) querySets.second)) {
            String hs2LogTag = Utils.hs2LogTag(getClass());
            Log.d(hs2LogTag, "ANQP failed on " + Utils.macToString(bssid));
            return false;
        }
        String hs2LogTag2 = Utils.hs2LogTag(getClass());
        Log.d(hs2LogTag2, "ANQP initiated on " + Utils.macToString(bssid));
        return true;
    }

    public boolean requestIcon(long bssid, String fileName) {
        if (bssid == 0 || fileName == null) {
            return false;
        }
        WifiNative wifiNative = this.mSupplicantHook;
        return wifiNative.requestIcon(wifiNative.getClientInterfaceName(), Utils.macToString(bssid), fileName);
    }

    public void notifyANQPDone(AnqpEvent anqpEvent) {
        if (anqpEvent != null) {
            this.mCallbacks.onANQPResponse(anqpEvent.getBssid(), anqpEvent.getElements());
        }
    }

    public void notifyIconDone(IconEvent iconEvent) {
        if (iconEvent != null) {
            this.mCallbacks.onIconResponse(iconEvent.getBSSID(), iconEvent.getFileName(), iconEvent.getData());
        }
    }

    public void notifyWnmFrameReceived(WnmData data) {
        this.mCallbacks.onWnmFrameReceived(data);
    }

    private static Pair<Set<Integer>, Set<Integer>> buildAnqpIdSet(List<Constants.ANQPElementType> querySet) {
        Set<Integer> anqpIds = new HashSet<>();
        Set<Integer> hs20Subtypes = new HashSet<>();
        for (Constants.ANQPElementType elementType : querySet) {
            Integer id = Constants.getANQPElementID(elementType);
            if (id != null) {
                anqpIds.add(id);
            } else {
                hs20Subtypes.add(Constants.getHS20ElementID(elementType));
            }
        }
        return Pair.create(anqpIds, hs20Subtypes);
    }
}
