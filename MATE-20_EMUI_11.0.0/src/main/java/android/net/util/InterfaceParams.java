package android.net.util;

import android.net.MacAddress;
import android.text.TextUtils;
import com.android.internal.util.Preconditions;
import java.net.NetworkInterface;
import java.net.SocketException;

public class InterfaceParams {
    private static final int ETHER_MTU = 1500;
    private static final int IPV6_MIN_MTU = 1280;
    public final int defaultMtu;
    public final int index;
    public final MacAddress macAddr;
    public final String name;

    public static InterfaceParams getByName(String name2) {
        NetworkInterface netif = getNetworkInterfaceByName(name2);
        if (netif == null) {
            return null;
        }
        try {
            return new InterfaceParams(name2, netif.getIndex(), getMacAddress(netif), netif.getMTU());
        } catch (IllegalArgumentException | SocketException e) {
            return null;
        }
    }

    public InterfaceParams(String name2, int index2, MacAddress macAddr2) {
        this(name2, index2, macAddr2, 1500);
    }

    public InterfaceParams(String name2, int index2, MacAddress macAddr2, int defaultMtu2) {
        boolean z = true;
        Preconditions.checkArgument(!TextUtils.isEmpty(name2), "impossible interface name");
        Preconditions.checkArgument(index2 <= 0 ? false : z, "invalid interface index");
        this.name = name2;
        this.index = index2;
        this.macAddr = macAddr2 != null ? macAddr2 : MacAddress.fromBytes(new byte[]{2, 0, 0, 0, 0, 0});
        this.defaultMtu = defaultMtu2 > 1280 ? defaultMtu2 : 1280;
    }

    public String toString() {
        return String.format("%s/%d/%s/%d", this.name, Integer.valueOf(this.index), this.macAddr, Integer.valueOf(this.defaultMtu));
    }

    private static NetworkInterface getNetworkInterfaceByName(String name2) {
        try {
            return NetworkInterface.getByName(name2);
        } catch (NullPointerException | SocketException e) {
            return null;
        }
    }

    private static MacAddress getMacAddress(NetworkInterface netif) {
        try {
            return MacAddress.fromBytes(netif.getHardwareAddress());
        } catch (IllegalArgumentException | NullPointerException | SocketException e) {
            return null;
        }
    }
}
