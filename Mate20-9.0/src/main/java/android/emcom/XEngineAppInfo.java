package android.emcom;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

public class XEngineAppInfo implements Parcelable {
    public static final Parcelable.Creator<XEngineAppInfo> CREATOR = new Parcelable.Creator<XEngineAppInfo>() {
        public XEngineAppInfo createFromParcel(Parcel in) {
            return new XEngineAppInfo(in);
        }

        public XEngineAppInfo[] newArray(int size) {
            return new XEngineAppInfo[size];
        }
    };
    private static final int MAX_XENGINE_APP = 200;
    private static final String TAG = "XEngineAppInfo";
    private List<EventInfo> mEventList = new ArrayList();
    private int mGrade;
    private HiViewParam mHiViewParam;
    private String mPackageName;
    private int mainCardPsStatus;

    public static class EventInfo {
        /* access modifiers changed from: private */
        public int mAccelerateGrade;
        /* access modifiers changed from: private */
        public String mContainer;
        /* access modifiers changed from: private */
        public String mKeyword;
        /* access modifiers changed from: private */
        public int mMaxChildCount;
        /* access modifiers changed from: private */
        public String mRootView;
        /* access modifiers changed from: private */
        public int mTreeDepth;
        /* access modifiers changed from: private */
        public int mainCardPsStatus;

        public String getRootView() {
            return this.mRootView;
        }

        public void setRootView(String rootView) {
            this.mRootView = rootView;
        }

        public String getContainer() {
            return this.mContainer;
        }

        public void setContainer(String container) {
            this.mContainer = container;
        }

        public String getKeyword() {
            return this.mKeyword;
        }

        public void setKeyword(String keyword) {
            this.mKeyword = keyword;
        }

        public int getTreeDepth() {
            return this.mTreeDepth;
        }

        public void setTreeDepth(int treeDepth) {
            this.mTreeDepth = treeDepth;
        }

        public int getMaxChildCount() {
            return this.mMaxChildCount;
        }

        public void setMaxChildCount(int maxChildCount) {
            this.mMaxChildCount = maxChildCount;
        }

        public int getGrade() {
            return this.mAccelerateGrade;
        }

        public void setGrade(int grade) {
            this.mAccelerateGrade = grade;
        }

        public void setMainCardPsStatus(int psSta) {
            this.mainCardPsStatus = psSta;
        }

        public int getMainCardPsStatus() {
            return this.mainCardPsStatus;
        }

        public String toString() {
            StringBuffer buffer = new StringBuffer();
            buffer.append("{rootView=");
            buffer.append(this.mRootView);
            buffer.append(", ");
            buffer.append("container=");
            buffer.append(this.mContainer);
            buffer.append(",");
            buffer.append("keyword=");
            buffer.append(this.mKeyword);
            buffer.append(",");
            buffer.append("maxDepth=");
            buffer.append(this.mTreeDepth);
            buffer.append(", ");
            buffer.append("maxChildCount=");
            buffer.append(this.mMaxChildCount);
            buffer.append(", ");
            buffer.append("grade=");
            buffer.append(this.mAccelerateGrade);
            buffer.append(", ");
            buffer.append("mainCardPsStatus=");
            buffer.append(this.mainCardPsStatus);
            buffer.append("}");
            return buffer.toString();
        }
    }

    public static class HiViewParam {
        private int mMultiPath;
        private String mPackageName;

        public HiViewParam(String packName, int mp) {
            this.mPackageName = packName;
            this.mMultiPath = mp;
        }

        public int getMultiPath() {
            return this.mMultiPath;
        }

        public String getPackageName() {
            return this.mPackageName;
        }

        public String toString() {
            StringBuffer buffer = new StringBuffer();
            buffer.append("MultiPath=");
            buffer.append(this.mMultiPath);
            buffer.append(", ");
            buffer.append("}");
            return buffer.toString();
        }
    }

    public XEngineAppInfo(String packageName) {
        this.mPackageName = packageName;
    }

    public XEngineAppInfo(Parcel p) {
        this.mPackageName = p.readString();
        this.mGrade = p.readInt();
        int count = p.readInt();
        if (count > 200) {
            Log.e(TAG, "The count of xengine app is more than 200, do not read!");
            return;
        }
        for (int i = 0; i < count; i++) {
            EventInfo eventInfo = new EventInfo();
            String unused = eventInfo.mRootView = p.readString();
            String unused2 = eventInfo.mContainer = p.readString();
            String unused3 = eventInfo.mKeyword = p.readString();
            int unused4 = eventInfo.mTreeDepth = p.readInt();
            int unused5 = eventInfo.mMaxChildCount = p.readInt();
            int unused6 = eventInfo.mAccelerateGrade = p.readInt();
            int unused7 = eventInfo.mainCardPsStatus = p.readInt();
            this.mEventList.add(eventInfo);
        }
    }

    public int describeContents() {
        return 0;
    }

    public String getPackageName() {
        return this.mPackageName;
    }

    public void setPackageName(String packageName) {
        this.mPackageName = packageName;
    }

    public List<EventInfo> getEventList() {
        return this.mEventList;
    }

    public int getGrade() {
        return this.mGrade;
    }

    public void setGrade(int grade) {
        this.mGrade = grade;
    }

    public int getMainCardPsStatus() {
        return this.mainCardPsStatus;
    }

    public void setMainCardPsStatus(int psStatus) {
        this.mainCardPsStatus = psStatus;
    }

    public HiViewParam getHiViewParam() {
        return this.mHiViewParam;
    }

    public void setHiViewParam(HiViewParam hiParam) {
        this.mHiViewParam = hiParam;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mPackageName);
        dest.writeInt(this.mGrade);
        dest.writeInt(this.mEventList.size());
        for (EventInfo eventInfo : this.mEventList) {
            dest.writeString(eventInfo.mRootView);
            dest.writeString(eventInfo.mContainer);
            dest.writeString(eventInfo.mKeyword);
            dest.writeInt(eventInfo.mTreeDepth);
            dest.writeInt(eventInfo.mMaxChildCount);
            dest.writeInt(eventInfo.mAccelerateGrade);
            dest.writeInt(eventInfo.mainCardPsStatus);
        }
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("packageName=");
        buffer.append(this.mPackageName);
        buffer.append(", ");
        buffer.append("grade=");
        buffer.append(this.mGrade);
        buffer.append(", ");
        buffer.append("eventList=");
        buffer.append(this.mEventList);
        return buffer.toString();
    }
}
