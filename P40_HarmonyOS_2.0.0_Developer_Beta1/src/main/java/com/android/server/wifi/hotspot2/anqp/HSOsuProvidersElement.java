package com.android.server.wifi.hotspot2.anqp;

import android.net.wifi.WifiSsid;
import com.android.internal.annotations.VisibleForTesting;
import com.android.server.wifi.hotspot2.anqp.Constants;
import java.net.ProtocolException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class HSOsuProvidersElement extends ANQPElement {
    @VisibleForTesting
    public static final int MAXIMUM_OSU_SSID_LENGTH = 32;
    private final WifiSsid mOsuSsid;
    private final List<OsuProviderInfo> mProviders;

    @VisibleForTesting
    public HSOsuProvidersElement(WifiSsid osuSsid, List<OsuProviderInfo> providers) {
        super(Constants.ANQPElementType.HSOSUProviders);
        this.mOsuSsid = osuSsid;
        this.mProviders = providers;
    }

    public static HSOsuProvidersElement parse(ByteBuffer payload) throws ProtocolException {
        int ssidLength = payload.get() & 255;
        if (ssidLength <= 32) {
            byte[] ssidBytes = new byte[ssidLength];
            payload.get(ssidBytes);
            List<OsuProviderInfo> providers = new ArrayList<>();
            for (int numProviders = payload.get() & 255; numProviders > 0; numProviders--) {
                providers.add(OsuProviderInfo.parse(payload));
            }
            return new HSOsuProvidersElement(WifiSsid.createFromByteArray(ssidBytes), providers);
        }
        throw new ProtocolException("Invalid SSID length: " + ssidLength);
    }

    public WifiSsid getOsuSsid() {
        return this.mOsuSsid;
    }

    public List<OsuProviderInfo> getProviders() {
        return Collections.unmodifiableList(this.mProviders);
    }

    public boolean equals(Object thatObject) {
        if (this == thatObject) {
            return true;
        }
        if (!(thatObject instanceof HSOsuProvidersElement)) {
            return false;
        }
        HSOsuProvidersElement that = (HSOsuProvidersElement) thatObject;
        WifiSsid wifiSsid = this.mOsuSsid;
        if (wifiSsid != null ? wifiSsid.equals(that.mOsuSsid) : that.mOsuSsid == null) {
            List<OsuProviderInfo> list = this.mProviders;
            if (list == null) {
                if (that.mProviders == null) {
                    return true;
                }
            } else if (list.equals(that.mProviders)) {
                return true;
            }
        }
        return false;
    }

    public int hashCode() {
        return Objects.hash(this.mOsuSsid, this.mProviders);
    }

    public String toString() {
        return "OSUProviders{mOsuSsid=" + this.mOsuSsid + ", mProviders=" + this.mProviders + "}";
    }
}
