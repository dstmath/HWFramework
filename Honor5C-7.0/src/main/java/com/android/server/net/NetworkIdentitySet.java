package com.android.server.net;

import android.net.NetworkIdentity;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashSet;

public class NetworkIdentitySet extends HashSet<NetworkIdentity> implements Comparable<NetworkIdentitySet> {
    private static final int VERSION_ADD_METERED = 4;
    private static final int VERSION_ADD_NETWORK_ID = 3;
    private static final int VERSION_ADD_ROAMING = 2;
    private static final int VERSION_INIT = 1;

    public NetworkIdentitySet(DataInputStream in) throws IOException {
        int version = in.readInt();
        int size = in.readInt();
        for (int i = 0; i < size; i += VERSION_INIT) {
            String readOptionalString;
            boolean readBoolean;
            if (version <= VERSION_INIT) {
                int readInt = in.readInt();
            }
            int type = in.readInt();
            int subType = in.readInt();
            String subscriberId = readOptionalString(in);
            if (version >= VERSION_ADD_NETWORK_ID) {
                readOptionalString = readOptionalString(in);
            } else {
                readOptionalString = null;
            }
            if (version >= VERSION_ADD_ROAMING) {
                readBoolean = in.readBoolean();
            } else {
                readBoolean = false;
            }
            boolean readBoolean2 = version >= VERSION_ADD_METERED ? in.readBoolean() : type == 0;
            add(new NetworkIdentity(type, subType, subscriberId, readOptionalString, readBoolean, readBoolean2));
        }
    }

    public void writeToStream(DataOutputStream out) throws IOException {
        out.writeInt(VERSION_ADD_METERED);
        out.writeInt(size());
        for (NetworkIdentity ident : this) {
            out.writeInt(ident.getType());
            out.writeInt(ident.getSubType());
            writeOptionalString(out, ident.getSubscriberId());
            writeOptionalString(out, ident.getNetworkId());
            out.writeBoolean(ident.getRoaming());
            out.writeBoolean(ident.getMetered());
        }
    }

    public boolean isAnyMemberRoaming() {
        if (isEmpty()) {
            return false;
        }
        for (NetworkIdentity ident : this) {
            if (ident.getRoaming()) {
                return true;
            }
        }
        return false;
    }

    private static void writeOptionalString(DataOutputStream out, String value) throws IOException {
        if (value != null) {
            out.writeByte(VERSION_INIT);
            out.writeUTF(value);
            return;
        }
        out.writeByte(0);
    }

    private static String readOptionalString(DataInputStream in) throws IOException {
        if (in.readByte() != null) {
            return in.readUTF();
        }
        return null;
    }

    public int compareTo(NetworkIdentitySet another) {
        if (isEmpty()) {
            return -1;
        }
        if (another.isEmpty()) {
            return VERSION_INIT;
        }
        return ((NetworkIdentity) iterator().next()).compareTo((NetworkIdentity) another.iterator().next());
    }
}
