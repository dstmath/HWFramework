package android.net.wifi.p2p;

import android.annotation.UnsupportedAppUsage;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.LruCache;
import java.util.Collection;
import java.util.Map;

public class WifiP2pGroupList implements Parcelable {
    public static final Parcelable.Creator<WifiP2pGroupList> CREATOR = new Parcelable.Creator<WifiP2pGroupList>() {
        /* class android.net.wifi.p2p.WifiP2pGroupList.AnonymousClass2 */

        @Override // android.os.Parcelable.Creator
        public WifiP2pGroupList createFromParcel(Parcel in) {
            WifiP2pGroupList grpList = new WifiP2pGroupList();
            int deviceCount = in.readInt();
            for (int i = 0; i < deviceCount; i++) {
                grpList.add((WifiP2pGroup) in.readParcelable(null));
            }
            return grpList;
        }

        @Override // android.os.Parcelable.Creator
        public WifiP2pGroupList[] newArray(int size) {
            return new WifiP2pGroupList[size];
        }
    };
    private static final int CREDENTIAL_MAX_NUM = 32;
    private boolean isClearCalled;
    @UnsupportedAppUsage
    private final LruCache<Integer, WifiP2pGroup> mGroups;
    private final GroupDeleteListener mListener;

    public interface GroupDeleteListener {
        void onDeleteGroup(int i);
    }

    public WifiP2pGroupList() {
        this(null, null);
    }

    @UnsupportedAppUsage
    public WifiP2pGroupList(WifiP2pGroupList source, GroupDeleteListener listener) {
        this.isClearCalled = false;
        this.mListener = listener;
        this.mGroups = new LruCache<Integer, WifiP2pGroup>(32) {
            /* class android.net.wifi.p2p.WifiP2pGroupList.AnonymousClass1 */

            /* access modifiers changed from: protected */
            public void entryRemoved(boolean evicted, Integer netId, WifiP2pGroup oldValue, WifiP2pGroup newValue) {
                if (WifiP2pGroupList.this.mListener != null && !WifiP2pGroupList.this.isClearCalled) {
                    WifiP2pGroupList.this.mListener.onDeleteGroup(oldValue.getNetworkId());
                }
            }
        };
        if (source != null) {
            for (Map.Entry<Integer, WifiP2pGroup> item : source.mGroups.snapshot().entrySet()) {
                this.mGroups.put(item.getKey(), item.getValue());
            }
        }
    }

    @UnsupportedAppUsage
    public Collection<WifiP2pGroup> getGroupList() {
        return this.mGroups.snapshot().values();
    }

    public void add(WifiP2pGroup group) {
        this.mGroups.put(Integer.valueOf(group.getNetworkId()), group);
    }

    public void remove(int netId) {
        this.mGroups.remove(Integer.valueOf(netId));
    }

    /* access modifiers changed from: package-private */
    public void remove(String deviceAddress) {
        remove(getNetworkId(deviceAddress));
    }

    public boolean clear() {
        if (this.mGroups.size() == 0) {
            return false;
        }
        this.isClearCalled = true;
        this.mGroups.evictAll();
        this.isClearCalled = false;
        return true;
    }

    public int getNetworkId(String deviceAddress) {
        if (deviceAddress == null) {
            return -1;
        }
        for (WifiP2pGroup grp : this.mGroups.snapshot().values()) {
            if (deviceAddress.equalsIgnoreCase(grp.getOwner().deviceAddress)) {
                this.mGroups.get(Integer.valueOf(grp.getNetworkId()));
                return grp.getNetworkId();
            }
        }
        return -1;
    }

    public int getNetworkId(String deviceAddress, String ssid) {
        if (deviceAddress == null || ssid == null) {
            return -1;
        }
        for (WifiP2pGroup grp : this.mGroups.snapshot().values()) {
            if (deviceAddress.equalsIgnoreCase(grp.getOwner().deviceAddress) && ssid.equals(grp.getNetworkName())) {
                this.mGroups.get(Integer.valueOf(grp.getNetworkId()));
                return grp.getNetworkId();
            }
        }
        return -1;
    }

    public String getOwnerAddr(int netId) {
        WifiP2pGroup grp = this.mGroups.get(Integer.valueOf(netId));
        if (grp != null) {
            return grp.getOwner().deviceAddress;
        }
        return null;
    }

    public boolean contains(int netId) {
        for (WifiP2pGroup grp : this.mGroups.snapshot().values()) {
            if (netId == grp.getNetworkId()) {
                return true;
            }
        }
        return false;
    }

    public String toString() {
        StringBuffer sbuf = new StringBuffer();
        for (WifiP2pGroup grp : this.mGroups.snapshot().values()) {
            sbuf.append(grp);
            sbuf.append("\n");
        }
        return sbuf.toString();
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        Collection<WifiP2pGroup> groups = this.mGroups.snapshot().values();
        dest.writeInt(groups.size());
        for (WifiP2pGroup group : groups) {
            dest.writeParcelable(group, flags);
        }
    }
}
