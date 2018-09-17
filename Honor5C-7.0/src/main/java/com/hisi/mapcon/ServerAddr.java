package com.hisi.mapcon;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.ArrayList;

public class ServerAddr implements Parcelable {
    public static final Creator<ServerAddr> CREATOR = null;
    ArrayList<ServerAddrPair> mAddress;
    int mSize;

    public static class ServerAddrPair {
        String address;
        int type;

        public ServerAddrPair(int type, String addr) {
            this.type = type;
            this.address = addr;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.hisi.mapcon.ServerAddr.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.hisi.mapcon.ServerAddr.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.hisi.mapcon.ServerAddr.<clinit>():void");
    }

    public ServerAddr() {
        this.mAddress = new ArrayList();
        this.mSize = 0;
    }

    public ServerAddr(Parcel in) {
        this.mSize = in.readInt();
        this.mAddress = new ArrayList();
        for (int index = 0; index < this.mSize; index++) {
            this.mAddress.add(new ServerAddrPair(in.readInt(), in.readString()));
        }
    }

    public void add(int type, String addr) {
        this.mAddress.add(new ServerAddrPair(type, addr));
        this.mSize++;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int arg1) {
        int size = this.mAddress.size();
        out.writeInt(this.mSize);
        for (int index = 0; index < size; index++) {
            ServerAddrPair pair = (ServerAddrPair) this.mAddress.get(index);
            out.writeInt(pair.type);
            out.writeString(pair.address);
        }
    }

    public String toString() {
        StringBuffer retString = new StringBuffer();
        retString.append("serverAddr:size is:");
        retString.append(this.mAddress.size());
        for (int index = 0; index < this.mAddress.size(); index++) {
            ServerAddrPair pair = (ServerAddrPair) this.mAddress.get(index);
            retString.append("index:" + index + " type:" + pair.type + " addr:" + pair.address);
        }
        return retString.toString();
    }
}
