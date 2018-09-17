package android.net.wifi.p2p;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.LruCache;
import java.util.Collection;
import java.util.Map.Entry;

public class WifiP2pGroupList implements Parcelable {
    public static final Creator<WifiP2pGroupList> CREATOR = null;
    private static final int CREDENTIAL_MAX_NUM = 32;
    private boolean isClearCalled;
    private final LruCache<Integer, WifiP2pGroup> mGroups;
    private final GroupDeleteListener mListener;

    /* renamed from: android.net.wifi.p2p.WifiP2pGroupList.2 */
    class AnonymousClass2 extends LruCache<Integer, WifiP2pGroup> {
        AnonymousClass2(int $anonymous0) {
            super($anonymous0);
        }

        protected void entryRemoved(boolean evicted, Integer netId, WifiP2pGroup oldValue, WifiP2pGroup newValue) {
            if (WifiP2pGroupList.this.mListener != null && !WifiP2pGroupList.this.isClearCalled) {
                WifiP2pGroupList.this.mListener.onDeleteGroup(oldValue.getNetworkId());
            }
        }
    }

    public interface GroupDeleteListener {
        void onDeleteGroup(int i);
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.net.wifi.p2p.WifiP2pGroupList.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.net.wifi.p2p.WifiP2pGroupList.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.net.wifi.p2p.WifiP2pGroupList.<clinit>():void");
    }

    public WifiP2pGroupList() {
        this(null, null);
    }

    public WifiP2pGroupList(WifiP2pGroupList source, GroupDeleteListener listener) {
        this.isClearCalled = false;
        this.mListener = listener;
        this.mGroups = new AnonymousClass2(CREDENTIAL_MAX_NUM);
        if (source != null) {
            for (Entry<Integer, WifiP2pGroup> item : source.mGroups.snapshot().entrySet()) {
                this.mGroups.put((Integer) item.getKey(), (WifiP2pGroup) item.getValue());
            }
        }
    }

    public Collection<WifiP2pGroup> getGroupList() {
        return this.mGroups.snapshot().values();
    }

    public void add(WifiP2pGroup group) {
        this.mGroups.put(Integer.valueOf(group.getNetworkId()), group);
    }

    public void remove(int netId) {
        this.mGroups.remove(Integer.valueOf(netId));
    }

    void remove(String deviceAddress) {
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
        WifiP2pGroup grp = (WifiP2pGroup) this.mGroups.get(Integer.valueOf(netId));
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
            sbuf.append(grp).append("\n");
        }
        return sbuf.toString();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        Collection<WifiP2pGroup> groups = this.mGroups.snapshot().values();
        dest.writeInt(groups.size());
        for (WifiP2pGroup group : groups) {
            dest.writeParcelable(group, flags);
        }
    }
}
