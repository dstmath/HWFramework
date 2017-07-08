package com.tencent.tmsecurelite.commom;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;

public final class DataEntity extends JSONObject implements Parcelable {
    public static final Creator<DataEntity> CREATOR = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.tencent.tmsecurelite.commom.DataEntity.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.tencent.tmsecurelite.commom.DataEntity.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.tencent.tmsecurelite.commom.DataEntity.<clinit>():void");
    }

    public DataEntity(Parcel parcel) throws JSONException {
        super(parcel.readString());
    }

    public static ArrayList<DataEntity> readFromParcel(Parcel parcel) {
        ArrayList<DataEntity> arrayList = new ArrayList();
        try {
            int readInt = parcel.readInt();
            arrayList.ensureCapacity(readInt);
            for (int i = 0; i < readInt; i++) {
                arrayList.add(i, new DataEntity(parcel));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return arrayList;
    }

    public static void writeToParcel(List<DataEntity> list, Parcel parcel) {
        parcel.writeInt(list.size());
        for (DataEntity writeToParcel : list) {
            writeToParcel.writeToParcel(parcel, 0);
        }
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(toString());
    }
}
