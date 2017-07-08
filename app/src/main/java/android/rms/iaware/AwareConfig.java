package android.rms.iaware;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AwareConfig implements Parcelable {
    public static final Creator<AwareConfig> CREATOR = null;
    private List<Item> mItems;

    public static class Item implements Parcelable {
        public static final Creator<Item> CREATOR = null;
        private String mItemName;
        private Map<String, String> mItemProperties;
        private List<SubItem> mSubItemList;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.rms.iaware.AwareConfig.Item.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.rms.iaware.AwareConfig.Item.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.rms.iaware.AwareConfig.Item.<clinit>():void");
        }

        public Item(Parcel source) {
            this.mSubItemList = new ArrayList();
            source.readList(this.mSubItemList, null);
            this.mItemProperties = new HashMap();
            source.readMap(this.mItemProperties, null);
        }

        public String getName() {
            return this.mItemName;
        }

        public void setName(String name) {
            this.mItemName = name;
        }

        public Map<String, String> getProperties() {
            return this.mItemProperties;
        }

        public void setProperties(Map<String, String> properties) {
            this.mItemProperties = properties;
        }

        public List<SubItem> getSubItemList() {
            return this.mSubItemList;
        }

        public void setSubItemList(List<SubItem> subItemList) {
            this.mSubItemList = subItemList;
        }

        public String toString() {
            return "Item [name=" + this.mItemName + ", properties=" + this.mItemProperties + ", subItemList=" + this.mSubItemList + "]";
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeList(this.mSubItemList);
            dest.writeMap(this.mItemProperties);
        }
    }

    public static class SubItem implements Parcelable {
        public static final Creator<SubItem> CREATOR = null;
        private String mName;
        private Map<String, String> mProperties;
        private String mValue;

        /* renamed from: android.rms.iaware.AwareConfig.SubItem.1 */
        static class AnonymousClass1 implements Creator<SubItem> {
            AnonymousClass1() {
            }

            public /* bridge */ /* synthetic */ Object[] m98newArray(int size) {
                return newArray(size);
            }

            public SubItem[] newArray(int size) {
                return new SubItem[size];
            }

            public /* bridge */ /* synthetic */ Object m97createFromParcel(Parcel source) {
                return createFromParcel(source);
            }

            public SubItem createFromParcel(Parcel source) {
                return new SubItem(source);
            }
        }

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.rms.iaware.AwareConfig.SubItem.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.rms.iaware.AwareConfig.SubItem.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.rms.iaware.AwareConfig.SubItem.<clinit>():void");
        }

        public SubItem(String name, String value, Map<String, String> properties) {
            this.mName = name;
            this.mValue = value;
            this.mProperties = properties;
        }

        public SubItem(Parcel source) {
            this.mName = source.readString();
            this.mValue = source.readString();
            this.mProperties = new HashMap();
            source.readMap(this.mProperties, null);
        }

        public String getName() {
            return this.mName;
        }

        public void setName(String name) {
            this.mName = name;
        }

        public String getValue() {
            return this.mValue;
        }

        public void setValue(String value) {
            this.mValue = value;
        }

        public Map<String, String> getProperties() {
            return this.mProperties;
        }

        public void setProperties(Map<String, String> properties) {
            this.mProperties = properties;
        }

        public String toString() {
            return "SubItem [name=" + this.mName + ", value=" + this.mValue + ", properties=" + this.mProperties + "]";
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.mName);
            dest.writeString(this.mValue);
            dest.writeMap(this.mProperties);
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.rms.iaware.AwareConfig.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.rms.iaware.AwareConfig.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.rms.iaware.AwareConfig.<clinit>():void");
    }

    public AwareConfig(List<Item> items) {
        this.mItems = items;
    }

    public AwareConfig(Parcel source) {
        this.mItems = new ArrayList();
        source.readList(this.mItems, null);
    }

    public List<Item> getConfigList() {
        return this.mItems;
    }

    public void setConfigList(List<Item> items) {
        this.mItems = items;
    }

    public String toString() {
        return "AwareConfigList [mItems=" + this.mItems + "]";
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeList(this.mItems);
    }
}
