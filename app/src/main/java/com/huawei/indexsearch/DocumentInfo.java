package com.huawei.indexsearch;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.PtmLog;
import java.util.ArrayList;
import java.util.Arrays;

public class DocumentInfo implements Parcelable {
    public static final Creator<DocumentInfo> CREATOR = null;
    private String[] mDocFields;
    private int[] mFieldIndexStatus;
    private int[] mFieldStoreStatus;
    private ArrayList<String> mFieldValueList;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.indexsearch.DocumentInfo.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.indexsearch.DocumentInfo.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.indexsearch.DocumentInfo.<clinit>():void");
    }

    public DocumentInfo(String[] docFields, ArrayList<String> list, int[] fieldStoreStatus, int[] fieldIndexStatus) {
        this.mDocFields = (String[]) docFields.clone();
        this.mFieldValueList = list;
        this.mFieldStoreStatus = (int[]) fieldStoreStatus.clone();
        this.mFieldIndexStatus = (int[]) fieldIndexStatus.clone();
    }

    private DocumentInfo(Parcel in) {
        this.mDocFields = in.createStringArray();
        this.mFieldValueList = in.createStringArrayList();
        this.mFieldStoreStatus = in.createIntArray();
        this.mFieldIndexStatus = in.createIntArray();
    }

    public ArrayList<String> getList() {
        return this.mFieldValueList;
    }

    public String[] getDocFieldList() {
        return (String[]) this.mDocFields.clone();
    }

    public int[] getFieldStoreList() {
        return (int[]) this.mFieldStoreStatus.clone();
    }

    public int[] getFieldIndexList() {
        return (int[]) this.mFieldIndexStatus.clone();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(this.mDocFields);
        dest.writeStringList(this.mFieldValueList);
        dest.writeIntArray(this.mFieldStoreStatus);
        dest.writeIntArray(this.mFieldIndexStatus);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(Arrays.toString(this.mDocFields)).append("\n");
        for (String str : this.mFieldValueList) {
            sb.append(str).append(PtmLog.PAIRE_DELIMETER);
        }
        sb.append("\n");
        sb.append(Arrays.toString(this.mFieldStoreStatus)).append("\n");
        sb.append(Arrays.toString(this.mFieldIndexStatus));
        return sb.toString();
    }

    public void readFromParcel(Parcel in) {
        this.mDocFields = in.createStringArray();
        this.mFieldValueList = in.createStringArrayList();
        this.mFieldStoreStatus = in.createIntArray();
        this.mFieldIndexStatus = in.createIntArray();
    }
}
