package android.nfc.cardemulation;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public final class NxpAidGroup extends AidGroup implements Parcelable {
    public static final Parcelable.Creator<NxpAidGroup> CREATOR = new Parcelable.Creator<NxpAidGroup>() {
        public NxpAidGroup createFromParcel(Parcel source) {
            String category = source.readString();
            int listSize = source.readInt();
            ArrayList<String> aidList = new ArrayList<>();
            if (listSize > 0) {
                source.readStringList(aidList);
            }
            String description = source.readString();
            if (aidList.size() == 0) {
                return new NxpAidGroup(category, description);
            }
            return new NxpAidGroup(aidList, category, description);
        }

        public NxpAidGroup[] newArray(int size) {
            return new NxpAidGroup[size];
        }
    };
    static final String TAG = "NxpAidGroup";
    protected ArrayList<ApduPatternGroup> mStaticApduPatternGroups;

    public class ApduPattern {
        private String description;
        private String mask;
        private String reference_data;

        public ApduPattern(String reference_data2, String mask2, String description2) {
            this.reference_data = reference_data2;
            this.mask = mask2;
            this.description = description2;
        }

        public String getreferenceData() {
            return this.reference_data;
        }

        public String getMask() {
            return this.mask;
        }
    }

    public static class ApduPatternGroup implements Parcelable {
        public static final Parcelable.Creator<ApduPatternGroup> CREATOR = new Parcelable.Creator<ApduPatternGroup>() {
            public ApduPatternGroup createFromParcel(Parcel source) {
                String description = source.readString();
                int readInt = source.readInt();
                new ArrayList();
                return new ApduPatternGroup(description);
            }

            public ApduPatternGroup[] newArray(int size) {
                return new ApduPatternGroup[size];
            }
        };
        public static final int MAX_NUM_APDU = 5;
        public static final String TAG = "ApduPatternGroup";
        protected List<ApduPattern> apduList = new ArrayList(5);
        protected String description;

        public ApduPatternGroup(String description2) {
            this.description = description2;
        }

        public void addApduPattern(ApduPattern apduPattern) {
            if (!containsApduPattern(apduPattern)) {
                this.apduList.add(apduPattern);
            }
        }

        private boolean containsApduPattern(ApduPattern apduPattern) {
            for (ApduPattern apdu : this.apduList) {
                if (apdu.getreferenceData().equalsIgnoreCase(apduPattern.getreferenceData())) {
                    return true;
                }
            }
            return false;
        }

        public List<ApduPattern> getApduPattern() {
            return this.apduList;
        }

        public String getDescription() {
            return this.description;
        }

        public String toString() {
            StringBuilder out = new StringBuilder("APDU Pattern List");
            for (ApduPattern apdu : this.apduList) {
                out.append("apdu_data" + apdu.getreferenceData());
                out.append("apdu mask" + apdu.getMask());
            }
            return out.toString();
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.description);
            dest.writeInt(this.apduList.size());
            this.apduList.size();
        }

        public int describeContents() {
            return 0;
        }
    }

    public NxpAidGroup(List<String> aids, String category, String description) {
        super(aids, category);
        this.mStaticApduPatternGroups = null;
        this.description = description;
        this.mStaticApduPatternGroups = new ArrayList<>();
    }

    public NxpAidGroup(List<String> aids, String category) {
        super(aids, category);
        this.mStaticApduPatternGroups = null;
    }

    public NxpAidGroup(String category, String description) {
        super(category, description);
        this.mStaticApduPatternGroups = null;
    }

    public NxpAidGroup(AidGroup aid) {
        this(aid.getAids(), aid.getCategory(), getDescription(aid));
    }

    static String getDescription(AidGroup aid) {
        for (Field f : aid.getClass().getDeclaredFields()) {
            f.setAccessible(true);
        }
        return aid.description;
    }

    public AidGroup createAidGroup() {
        if (getAids() == null || getAids().isEmpty()) {
            Log.d(TAG, "Empty aid group creation");
            return new AidGroup(getCategory(), getDescription());
        }
        Log.d(TAG, "Non Empty aid group creation");
        return new AidGroup(getAids(), getCategory());
    }

    public void addApduGroup(ApduPatternGroup apdu) {
        this.mStaticApduPatternGroups.add(apdu);
    }

    public ArrayList<ApduPattern> getApduPatternList() {
        ArrayList<ApduPattern> apdulist = new ArrayList<>();
        if (this.mStaticApduPatternGroups == null) {
            return null;
        }
        try {
            Iterator<ApduPatternGroup> it = this.mStaticApduPatternGroups.iterator();
            while (it.hasNext()) {
                ApduPatternGroup group = it.next();
                if (group != null) {
                    for (ApduPattern apduPattern : group.getApduPattern()) {
                        if (apduPattern != null) {
                            apdulist.add(apduPattern);
                        }
                    }
                }
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return apdulist;
    }

    public String getDescription() {
        return this.description;
    }

    public void writeToParcel(Parcel dest, int flags) {
        NxpAidGroup.super.writeToParcel(dest, flags);
        if (this.description != null) {
            dest.writeString(this.description);
        } else {
            dest.writeString(null);
        }
    }

    public static NxpAidGroup createFromXml(XmlPullParser parser) throws XmlPullParserException, IOException {
        String category = null;
        String description = null;
        ArrayList<String> aids = new ArrayList<>();
        NxpAidGroup group = null;
        boolean inGroup = false;
        int eventType = parser.getEventType();
        int minDepth = parser.getDepth();
        while (true) {
            if (eventType == 1 || parser.getDepth() < minDepth) {
                break;
            }
            String tagName = parser.getName();
            if (eventType == 2) {
                if (tagName.equals("aid")) {
                    if (inGroup) {
                        String aid = parser.getAttributeValue(null, "value");
                        if (aid != null) {
                            aids.add(aid.toUpperCase());
                        }
                    } else {
                        Log.d(TAG, "Ignoring <aid> tag while not in group");
                    }
                } else if (tagName.equals("aid-group")) {
                    category = parser.getAttributeValue(null, "category");
                    description = parser.getAttributeValue(null, "description");
                    if (category == null) {
                        Log.e(TAG, "<aid-group> tag without valid category");
                        return null;
                    }
                    inGroup = true;
                } else {
                    Log.d(TAG, "Ignoring unexpected tag: " + tagName);
                }
            } else if (eventType == 3 && tagName.equals("aid-group") && inGroup) {
                group = aids.size() > 0 ? new NxpAidGroup(aids, category, description) : new NxpAidGroup(category, description);
            }
            eventType = parser.next();
        }
        return group;
    }

    public void writeAsXml(XmlSerializer out) throws IOException {
        out.startTag(null, "aid-group");
        out.attribute(null, "category", this.category);
        if (this.description != null) {
            out.attribute(null, "description", this.description);
        }
        for (String aid : this.aids) {
            out.startTag(null, "aid");
            out.attribute(null, "value", aid);
            out.endTag(null, "aid");
        }
        out.endTag(null, "aid-group");
    }
}
