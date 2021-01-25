package com.android.server.wifi.hotspot2.anqp;

import android.net.Uri;
import android.text.TextUtils;
import com.android.internal.annotations.VisibleForTesting;
import com.android.server.wifi.ByteBufferReader;
import java.net.ProtocolException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

public class OsuProviderInfo {
    private static final int MAXIMUM_I18N_STRING_LENGTH = 252;
    @VisibleForTesting
    public static final int MINIMUM_LENGTH = 9;
    private final Map<String, String> mFriendlyNames = new HashMap();
    private final List<IconInfo> mIconInfoList;
    private final List<Integer> mMethodList;
    private final String mNetworkAccessIdentifier;
    private final Uri mServerUri;
    private final List<I18Name> mServiceDescriptions;

    @VisibleForTesting
    public OsuProviderInfo(List<I18Name> friendlyNames, Uri serverUri, List<Integer> methodList, List<IconInfo> iconInfoList, String nai, List<I18Name> serviceDescriptions) {
        if (friendlyNames != null) {
            friendlyNames.forEach(new Consumer() {
                /* class com.android.server.wifi.hotspot2.anqp.$$Lambda$OsuProviderInfo$CikQnZalVd3RFGF5pHDiwtVeVkg */

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    OsuProviderInfo.this.lambda$new$0$OsuProviderInfo((I18Name) obj);
                }
            });
        }
        this.mServerUri = serverUri;
        this.mMethodList = methodList;
        this.mIconInfoList = iconInfoList;
        this.mNetworkAccessIdentifier = nai;
        this.mServiceDescriptions = serviceDescriptions;
    }

    public /* synthetic */ void lambda$new$0$OsuProviderInfo(I18Name e) {
        this.mFriendlyNames.put(e.getLocale().getLanguage(), e.getText());
    }

    public static OsuProviderInfo parse(ByteBuffer payload) throws ProtocolException {
        int length = ((int) ByteBufferReader.readInteger(payload, ByteOrder.LITTLE_ENDIAN, 2)) & Constants.SHORT_MASK;
        if (length >= 9) {
            List<I18Name> friendlyNameList = parseI18Names(getSubBuffer(payload, ((int) ByteBufferReader.readInteger(payload, ByteOrder.LITTLE_ENDIAN, 2)) & Constants.SHORT_MASK));
            Uri serverUri = Uri.parse(ByteBufferReader.readStringWithByteLength(payload, StandardCharsets.UTF_8));
            List<Integer> methodList = new ArrayList<>();
            for (int methodListLength = payload.get() & 255; methodListLength > 0; methodListLength--) {
                methodList.add(Integer.valueOf(payload.get() & 255));
            }
            ByteBuffer iconBuffer = getSubBuffer(payload, ((int) ByteBufferReader.readInteger(payload, ByteOrder.LITTLE_ENDIAN, 2)) & Constants.SHORT_MASK);
            List<IconInfo> iconInfoList = new ArrayList<>();
            while (iconBuffer.hasRemaining()) {
                iconInfoList.add(IconInfo.parse(iconBuffer));
            }
            return new OsuProviderInfo(friendlyNameList, serverUri, methodList, iconInfoList, ByteBufferReader.readStringWithByteLength(payload, StandardCharsets.UTF_8), parseI18Names(getSubBuffer(payload, ((int) ByteBufferReader.readInteger(payload, ByteOrder.LITTLE_ENDIAN, 2)) & Constants.SHORT_MASK)));
        }
        throw new ProtocolException("Invalid length value: " + length);
    }

    public Map<String, String> getFriendlyNames() {
        return this.mFriendlyNames;
    }

    public Uri getServerUri() {
        return this.mServerUri;
    }

    public List<Integer> getMethodList() {
        return Collections.unmodifiableList(this.mMethodList);
    }

    public List<IconInfo> getIconInfoList() {
        return Collections.unmodifiableList(this.mIconInfoList);
    }

    public String getNetworkAccessIdentifier() {
        return this.mNetworkAccessIdentifier;
    }

    public List<I18Name> getServiceDescriptions() {
        return Collections.unmodifiableList(this.mServiceDescriptions);
    }

    public String getFriendlyName() {
        Map<String, String> map = this.mFriendlyNames;
        if (map == null || map.isEmpty()) {
            return null;
        }
        String friendlyName = this.mFriendlyNames.get(Locale.getDefault().getLanguage());
        if (friendlyName != null) {
            return friendlyName;
        }
        String friendlyName2 = this.mFriendlyNames.get("en");
        if (friendlyName2 != null) {
            return friendlyName2;
        }
        Map<String, String> map2 = this.mFriendlyNames;
        return map2.get(map2.keySet().stream().findFirst().get());
    }

    public String getServiceDescription() {
        return getI18String(this.mServiceDescriptions);
    }

    public boolean equals(Object thatObject) {
        if (this == thatObject) {
            return true;
        }
        if (!(thatObject instanceof OsuProviderInfo)) {
            return false;
        }
        OsuProviderInfo that = (OsuProviderInfo) thatObject;
        Map<String, String> map = this.mFriendlyNames;
        if (map != null ? map.equals(that.mFriendlyNames) : that.mFriendlyNames == null) {
            Uri uri = this.mServerUri;
            if (uri != null ? uri.equals(that.mServerUri) : that.mServerUri == null) {
                List<Integer> list = this.mMethodList;
                if (list != null ? list.equals(that.mMethodList) : that.mMethodList == null) {
                    List<IconInfo> list2 = this.mIconInfoList;
                    if (list2 != null ? list2.equals(that.mIconInfoList) : that.mIconInfoList == null) {
                        if (TextUtils.equals(this.mNetworkAccessIdentifier, that.mNetworkAccessIdentifier)) {
                            List<I18Name> list3 = this.mServiceDescriptions;
                            if (list3 == null) {
                                if (that.mServiceDescriptions == null) {
                                    return true;
                                }
                            } else if (list3.equals(that.mServiceDescriptions)) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    public int hashCode() {
        return Objects.hash(this.mFriendlyNames, this.mServerUri, this.mMethodList, this.mIconInfoList, this.mNetworkAccessIdentifier, this.mServiceDescriptions);
    }

    public String toString() {
        return "OsuProviderInfo{mFriendlyNames=" + this.mFriendlyNames + ", mServerUri=" + this.mServerUri + ", mMethodList=" + this.mMethodList + ", mIconInfoList=" + this.mIconInfoList + ", mNetworkAccessIdentifier=" + this.mNetworkAccessIdentifier + ", mServiceDescriptions=" + this.mServiceDescriptions + "}";
    }

    private static List<I18Name> parseI18Names(ByteBuffer payload) throws ProtocolException {
        List<I18Name> results = new ArrayList<>();
        while (payload.hasRemaining()) {
            I18Name name = I18Name.parse(payload);
            int textBytes = name.getText().getBytes(StandardCharsets.UTF_8).length;
            if (textBytes <= 252) {
                results.add(name);
            } else {
                throw new ProtocolException("I18Name string exceeds the maximum allowed " + textBytes);
            }
        }
        return results;
    }

    private static ByteBuffer getSubBuffer(ByteBuffer payload, int length) {
        if (payload.remaining() >= length) {
            ByteBuffer subBuffer = payload.slice();
            subBuffer.limit(length);
            payload.position(payload.position() + length);
            return subBuffer;
        }
        throw new BufferUnderflowException();
    }

    private static String getI18String(List<I18Name> i18Strings) {
        for (I18Name name : i18Strings) {
            if (name.getLanguage().equals(Locale.getDefault().getLanguage())) {
                return name.getText();
            }
        }
        if (i18Strings.size() > 0) {
            return i18Strings.get(0).getText();
        }
        return null;
    }
}
