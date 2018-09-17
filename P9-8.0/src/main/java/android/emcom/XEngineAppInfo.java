package android.emcom;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.ArrayList;
import java.util.List;

public class XEngineAppInfo implements Parcelable {
    public static final Creator<XEngineAppInfo> CREATOR = new Creator<XEngineAppInfo>() {
        public XEngineAppInfo createFromParcel(Parcel in) {
            return new XEngineAppInfo(in);
        }

        public XEngineAppInfo[] newArray(int size) {
            return new XEngineAppInfo[size];
        }
    };
    private List<EventInfo> mEventList = new ArrayList();
    private int mGrade;
    private HiViewParam mHiViewParam;
    private String mPackageName;
    private int mainCardPsStatus;

    public static class EventInfo {
        private int mAccelerateGrade;
        private String mContainer;
        private String mKeyword;
        private int mMaxChildCount;
        private String mRootView;
        private int mTreeDepth;
        private int mainCardPsStatus;

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
            buffer.append("{rootView=").append(this.mRootView).append(", ").append("container=").append(this.mContainer).append(",").append("keyword=").append(this.mKeyword).append(",").append("maxDepth=").append(this.mTreeDepth).append(", ").append("maxChildCount=").append(this.mMaxChildCount).append(", ").append("grade=").append(this.mAccelerateGrade).append(", ").append("mainCardPsStatus=").append(this.mainCardPsStatus).append("}");
            return buffer.toString();
        }
    }

    public static class HiViewParam {
        private int mMultiFlow;
        private int mMultiPath;
        private String mPackageName;

        public HiViewParam(String packName, int mf, int mp) {
            this.mMultiFlow = mf;
            this.mPackageName = packName;
            this.mMultiPath = mp;
        }

        public int getMultiFlow() {
            return this.mMultiFlow;
        }

        public int getMultiPath() {
            return this.mMultiPath;
        }

        public String getPackageName() {
            return this.mPackageName;
        }

        public String toString() {
            StringBuffer buffer = new StringBuffer();
            buffer.append("{MultiFlow=").append(this.mMultiFlow).append(", ").append("MultiPath=").append(this.mMultiPath).append(", ").append("}");
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
        for (int i = 0; i < count; i++) {
            EventInfo eventInfo = new EventInfo();
            eventInfo.mRootView = p.readString();
            eventInfo.mContainer = p.readString();
            eventInfo.mKeyword = p.readString();
            eventInfo.mTreeDepth = p.readInt();
            eventInfo.mMaxChildCount = p.readInt();
            eventInfo.mAccelerateGrade = p.readInt();
            eventInfo.mainCardPsStatus = p.readInt();
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
        buffer.append("packageName=").append(this.mPackageName).append(", ").append("grade=").append(this.mGrade).append(", ").append("eventList=").append(this.mEventList);
        return buffer.toString();
    }
}
