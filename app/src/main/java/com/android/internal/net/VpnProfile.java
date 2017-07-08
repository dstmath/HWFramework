package com.android.internal.net;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.text.TextUtils;
import android.util.Log;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

public class VpnProfile implements Cloneable, Parcelable {
    public static final Creator<VpnProfile> CREATOR = null;
    private static final String TAG = "VpnProfile";
    public static final int TYPE_IPSEC_HYBRID_RSA = 6;
    public static final int TYPE_IPSEC_XAUTH_PSK = 4;
    public static final int TYPE_IPSEC_XAUTH_RSA = 5;
    public static final int TYPE_L2TP = 1;
    public static final int TYPE_L2TP_IPSEC_PSK = 2;
    public static final int TYPE_L2TP_IPSEC_RSA = 3;
    public static final int TYPE_MAX = 6;
    public static final int TYPE_PPTP = 0;
    public String dnsServers;
    public String ipsecCaCert;
    public String ipsecIdentifier;
    public String ipsecSecret;
    public String ipsecServerCert;
    public String ipsecUserCert;
    public final String key;
    public String l2tpSecret;
    public boolean mppe;
    public String name;
    public String password;
    public String routes;
    public boolean saveLogin;
    public String searchDomains;
    public String server;
    public int type;
    public String username;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.net.VpnProfile.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.net.VpnProfile.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.net.VpnProfile.<clinit>():void");
    }

    public VpnProfile(String key) {
        this.name = "";
        this.type = 0;
        this.server = "";
        this.username = "";
        this.password = "";
        this.dnsServers = "";
        this.searchDomains = "";
        this.routes = "";
        this.mppe = true;
        this.l2tpSecret = "";
        this.ipsecIdentifier = "";
        this.ipsecSecret = "";
        this.ipsecUserCert = "";
        this.ipsecCaCert = "";
        this.ipsecServerCert = "";
        this.saveLogin = false;
        this.key = key;
    }

    public VpnProfile(Parcel in) {
        boolean z;
        boolean z2 = true;
        this.name = "";
        this.type = 0;
        this.server = "";
        this.username = "";
        this.password = "";
        this.dnsServers = "";
        this.searchDomains = "";
        this.routes = "";
        this.mppe = true;
        this.l2tpSecret = "";
        this.ipsecIdentifier = "";
        this.ipsecSecret = "";
        this.ipsecUserCert = "";
        this.ipsecCaCert = "";
        this.ipsecServerCert = "";
        this.saveLogin = false;
        this.key = in.readString();
        this.name = in.readString();
        this.type = in.readInt();
        this.server = in.readString();
        this.username = in.readString();
        this.password = in.readString();
        this.dnsServers = in.readString();
        this.searchDomains = in.readString();
        this.routes = in.readString();
        if (in.readInt() != 0) {
            z = true;
        } else {
            z = false;
        }
        this.mppe = z;
        this.l2tpSecret = in.readString();
        this.ipsecIdentifier = in.readString();
        this.ipsecSecret = in.readString();
        this.ipsecUserCert = in.readString();
        this.ipsecCaCert = in.readString();
        this.ipsecServerCert = in.readString();
        if (in.readInt() == 0) {
            z2 = false;
        }
        this.saveLogin = z2;
    }

    public void writeToParcel(Parcel out, int flags) {
        int i;
        int i2 = TYPE_L2TP;
        out.writeString(this.key);
        out.writeString(this.name);
        out.writeInt(this.type);
        out.writeString(this.server);
        out.writeString(this.username);
        out.writeString(this.password);
        out.writeString(this.dnsServers);
        out.writeString(this.searchDomains);
        out.writeString(this.routes);
        if (this.mppe) {
            i = TYPE_L2TP;
        } else {
            i = 0;
        }
        out.writeInt(i);
        out.writeString(this.l2tpSecret);
        out.writeString(this.ipsecIdentifier);
        out.writeString(this.ipsecSecret);
        out.writeString(this.ipsecUserCert);
        out.writeString(this.ipsecCaCert);
        out.writeString(this.ipsecServerCert);
        if (!this.saveLogin) {
            i2 = 0;
        }
        out.writeInt(i2);
    }

    public static VpnProfile decode(String key, byte[] value) {
        boolean z = true;
        if (key == null) {
            return null;
        }
        try {
            String[] values = new String(value, StandardCharsets.UTF_8).split("\u0000", -1);
            if (values.length < 14 || values.length > 15) {
                return null;
            }
            VpnProfile profile = new VpnProfile(key);
            profile.name = values[0];
            profile.type = Integer.parseInt(values[TYPE_L2TP]);
            if (profile.type < 0 || profile.type > TYPE_MAX) {
                return null;
            }
            profile.server = values[TYPE_L2TP_IPSEC_PSK];
            profile.username = values[TYPE_L2TP_IPSEC_RSA];
            profile.password = values[TYPE_IPSEC_XAUTH_PSK];
            profile.dnsServers = values[TYPE_IPSEC_XAUTH_RSA];
            profile.searchDomains = values[TYPE_MAX];
            profile.routes = values[7];
            profile.mppe = Boolean.valueOf(values[8]).booleanValue();
            profile.l2tpSecret = values[9];
            profile.ipsecIdentifier = values[10];
            profile.ipsecSecret = values[11];
            profile.ipsecUserCert = values[12];
            profile.ipsecCaCert = values[13];
            profile.ipsecServerCert = values.length > 14 ? values[14] : "";
            if (profile.username.isEmpty() && profile.password.isEmpty()) {
                z = false;
            }
            profile.saveLogin = z;
            return profile;
        } catch (Exception e) {
            return null;
        }
    }

    public byte[] encode() {
        StringBuilder builder = new StringBuilder(this.name);
        builder.append('\u0000').append(this.type);
        builder.append('\u0000').append(this.server);
        builder.append('\u0000').append(this.saveLogin ? this.username : "");
        builder.append('\u0000').append(this.saveLogin ? this.password : "");
        builder.append('\u0000').append(this.dnsServers);
        builder.append('\u0000').append(this.searchDomains);
        builder.append('\u0000').append(this.routes);
        builder.append('\u0000').append(this.mppe);
        builder.append('\u0000').append(this.l2tpSecret);
        builder.append('\u0000').append(this.ipsecIdentifier);
        builder.append('\u0000').append(this.ipsecSecret);
        builder.append('\u0000').append(this.ipsecUserCert);
        builder.append('\u0000').append(this.ipsecCaCert);
        builder.append('\u0000').append(this.ipsecServerCert);
        return builder.toString().getBytes(StandardCharsets.UTF_8);
    }

    public boolean isValidLockdownProfile() {
        try {
            InetAddress.parseNumericAddress(this.server);
            String[] split = this.dnsServers.split(" +");
            int length = split.length;
            for (int i = 0; i < length; i += TYPE_L2TP) {
                String dnsServer = split[i];
                InetAddress.parseNumericAddress(this.dnsServers);
            }
            if (!TextUtils.isEmpty(this.dnsServers)) {
                return true;
            }
            Log.w(TAG, "DNS required");
            return false;
        } catch (IllegalArgumentException e) {
            Log.w(TAG, "Invalid address", e);
            return false;
        }
    }

    public int describeContents() {
        return 0;
    }
}
