package android.rms.iaware;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AwareConfig implements Parcelable {
    public static final Parcelable.Creator<AwareConfig> CREATOR = new Parcelable.Creator<AwareConfig>() {
        /* class android.rms.iaware.AwareConfig.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public AwareConfig[] newArray(int size) {
            return new AwareConfig[size];
        }

        @Override // android.os.Parcelable.Creator
        public AwareConfig createFromParcel(Parcel source) {
            return new AwareConfig(source);
        }
    };
    private List<Item> mItems;

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

    @Override // java.lang.Object
    public String toString() {
        return "AwareConfigList [mItems=" + this.mItems + "]";
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeList(this.mItems);
    }

    public static class Item implements Parcelable {
        public static final Parcelable.Creator<Item> CREATOR = new Parcelable.Creator<Item>() {
            /* class android.rms.iaware.AwareConfig.Item.AnonymousClass1 */

            @Override // android.os.Parcelable.Creator
            public Item[] newArray(int size) {
                return new Item[size];
            }

            @Override // android.os.Parcelable.Creator
            public Item createFromParcel(Parcel source) {
                return new Item(source);
            }
        };
        private String mItemName;
        private Map<String, String> mItemProperties;
        private List<SubItem> mSubItemList;

        public Item() {
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

        @Override // java.lang.Object
        public String toString() {
            return "Item [name=" + this.mItemName + ", properties=" + this.mItemProperties + ", subItemList=" + this.mSubItemList + "]";
        }

        @Override // android.os.Parcelable
        public int describeContents() {
            return 0;
        }

        public boolean isEqualTo(Item anotherItem) {
            if (this == anotherItem) {
                return true;
            }
            if (anotherItem == null) {
                return false;
            }
            String name = this.mItemName;
            String anotherItemPropStr = "null";
            if (name == null) {
                name = anotherItemPropStr;
            }
            if (name.compareTo(anotherItem.getName() != null ? anotherItem.getName() : anotherItemPropStr) != 0) {
                return false;
            }
            Map<String, String> map = this.mItemProperties;
            String itemPropStr = map != null ? map.toString() : anotherItemPropStr;
            Map<String, String> anotherItemProp = anotherItem.getProperties();
            if (anotherItemProp != null) {
                anotherItemPropStr = anotherItemProp.toString();
            }
            if (itemPropStr.compareTo(anotherItemPropStr) != 0) {
                return false;
            }
            return true;
        }

        @Override // android.os.Parcelable
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeList(this.mSubItemList);
            dest.writeMap(this.mItemProperties);
        }
    }

    public static class SubItem implements Parcelable {
        public static final Parcelable.Creator<SubItem> CREATOR = new Parcelable.Creator<SubItem>() {
            /* class android.rms.iaware.AwareConfig.SubItem.AnonymousClass1 */

            @Override // android.os.Parcelable.Creator
            public SubItem[] newArray(int size) {
                return new SubItem[size];
            }

            @Override // android.os.Parcelable.Creator
            public SubItem createFromParcel(Parcel source) {
                return new SubItem(source);
            }
        };
        private String mName;
        private Map<String, String> mProperties;
        private String mValue;

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

        @Override // java.lang.Object
        public String toString() {
            return "SubItem [name=" + this.mName + ", value=" + this.mValue + ", properties=" + this.mProperties + "]";
        }

        @Override // android.os.Parcelable
        public int describeContents() {
            return 0;
        }

        @Override // android.os.Parcelable
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.mName);
            dest.writeString(this.mValue);
            dest.writeMap(this.mProperties);
        }
    }
}
