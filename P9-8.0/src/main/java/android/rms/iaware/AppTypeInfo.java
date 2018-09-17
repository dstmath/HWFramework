package android.rms.iaware;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class AppTypeInfo implements Parcelable {
    public static final int APP_ATTRIBUTE_LOGIN = 2;
    public static final int APP_ATTRIBUTE_OVERSEA = Integer.MIN_VALUE;
    public static final int APP_ATTRIBUTE_PAY = 1;
    public static final int APP_ATTRIBUTE_UNKNOWN = -1;
    public static final int APP_TYPE_ALARM = 5;
    public static final int APP_TYPE_APP_MARKET = 31;
    public static final int APP_TYPE_ASSISTANT = 32;
    public static final int APP_TYPE_BANK = 16;
    public static final int APP_TYPE_BIKE = 23;
    public static final int APP_TYPE_BROWSER = 18;
    public static final int APP_TYPE_BUS = 24;
    public static final int APP_TYPE_BUSINESS = 11;
    public static final int APP_TYPE_CALLCAB = 22;
    public static final int APP_TYPE_CAMERA = 17;
    public static final int APP_TYPE_DOWN_UP_LOAD = 35;
    public static final int APP_TYPE_EDUCATION = 33;
    public static final int APP_TYPE_EMAIL = 1;
    public static final int APP_TYPE_FORUM = 20;
    public static final int APP_TYPE_GALLERY = 29;
    public static final int APP_TYPE_GAME = 9;
    public static final int APP_TYPE_IM = 0;
    public static final int APP_TYPE_IME = 19;
    public static final int APP_TYPE_LAUNCHER = 28;
    public static final int APP_TYPE_LEXICON = 10;
    public static final int APP_TYPE_LIVE = 21;
    public static final int APP_TYPE_MUSIC = 7;
    public static final int APP_TYPE_NAVI = 3;
    public static final int APP_TYPE_NEEDCHECK = -2;
    public static final int APP_TYPE_NEWS = 26;
    public static final int APP_TYPE_OFFICE = 12;
    public static final int APP_TYPE_OTHERS = 255;
    public static final int APP_TYPE_PAY = 34;
    public static final int APP_TYPE_READING = 6;
    public static final int APP_TYPE_SHOPPING = 14;
    public static final int APP_TYPE_SMALL_VIDEO = 36;
    public static final int APP_TYPE_SMS = 27;
    public static final int APP_TYPE_SPORT = 2;
    public static final int APP_TYPE_STOCKS = 4;
    public static final int APP_TYPE_THEME = 13;
    public static final int APP_TYPE_THIRD_GUARD = 25;
    public static final int APP_TYPE_TOOL = 15;
    public static final int APP_TYPE_UNKNOWN = -1;
    public static final int APP_TYPE_VIDEO = 8;
    public static final int APP_TYPE_VOIP = 30;
    public static final Creator<AppTypeInfo> CREATOR = new Creator<AppTypeInfo>() {
        public AppTypeInfo createFromParcel(Parcel source) {
            return new AppTypeInfo(source);
        }

        public AppTypeInfo[] newArray(int size) {
            return new AppTypeInfo[size];
        }
    };
    public static final int PG_APP_TYPE_ALARM = 310;
    public static final int PG_APP_TYPE_APP_MARKET = 320;
    public static final int PG_APP_TYPE_BROWSER = 306;
    public static final int PG_APP_TYPE_CAMERA = 324;
    public static final int PG_APP_TYPE_EBOOK = 307;
    public static final int PG_APP_TYPE_EDUCATION = 322;
    public static final int PG_APP_TYPE_EMAIL = 303;
    public static final int PG_APP_TYPE_GALLERY = 316;
    public static final int PG_APP_TYPE_GAME = 305;
    public static final int PG_APP_TYPE_IM = 311;
    public static final int PG_APP_TYPE_INPUTMETHOD = 304;
    public static final int PG_APP_TYPE_LAUNCHER = 301;
    public static final int PG_APP_TYPE_LIFE_TOOL = 321;
    public static final int PG_APP_TYPE_LOCATION_PROVIDER = 314;
    public static final int PG_APP_TYPE_MONEY = 323;
    public static final int PG_APP_TYPE_MUSIC = 312;
    public static final int PG_APP_TYPE_NAVIGATION = 313;
    public static final int PG_APP_TYPE_NEWS_CLIENT = 318;
    public static final int PG_APP_TYPE_OFFICE = 315;
    public static final int PG_APP_TYPE_PEDOMETER = 325;
    public static final int PG_APP_TYPE_SCRLOCK = 309;
    public static final int PG_APP_TYPE_SHOP = 319;
    public static final int PG_APP_TYPE_SIP = 317;
    public static final int PG_APP_TYPE_SMS = 302;
    public static final int PG_APP_TYPE_VIDEO = 308;
    public static final int PG_TYPE_BASE = 300;
    private int attribute;
    private String pkgName;
    private int type;

    public AppTypeInfo(Parcel source) {
        this.type = source.readInt();
        this.attribute = source.readInt();
        this.pkgName = source.readString();
    }

    public int getType() {
        return this.type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getAttribute() {
        return this.attribute;
    }

    public void setAttribute(int attribute) {
        this.attribute = attribute;
    }

    public String getPkgName() {
        return this.pkgName;
    }

    public void setPkgName(String pkgName) {
        this.pkgName = pkgName;
    }

    public String toString() {
        return "AppTypeInfo [type=" + this.type + ", attribute=" + this.attribute + "]";
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.type);
        dest.writeInt(this.attribute);
        dest.writeString(this.pkgName);
    }
}
