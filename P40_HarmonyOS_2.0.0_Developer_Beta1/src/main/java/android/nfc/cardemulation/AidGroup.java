package android.nfc.cardemulation;

import android.annotation.UnsupportedAppUsage;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class AidGroup implements Parcelable {
    @UnsupportedAppUsage
    public static final Parcelable.Creator<AidGroup> CREATOR = new Parcelable.Creator<AidGroup>() {
        /* class android.nfc.cardemulation.AidGroup.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public AidGroup createFromParcel(Parcel source) {
            String category = source.readString();
            int listSize = source.readInt();
            ArrayList<String> aidList = new ArrayList<>();
            if (listSize > 0) {
                source.readStringList(aidList);
            }
            return new AidGroup(aidList, category);
        }

        @Override // android.os.Parcelable.Creator
        public AidGroup[] newArray(int size) {
            return new AidGroup[size];
        }
    };
    public static final int MAX_NUM_AIDS = 256;
    static final String TAG = "AidGroup";
    @UnsupportedAppUsage
    protected List<String> aids;
    @UnsupportedAppUsage
    protected String category;
    @UnsupportedAppUsage
    protected String description;

    public AidGroup(List<String> aids2, String category2) {
        if (aids2 == null || aids2.size() == 0) {
            throw new IllegalArgumentException("No AIDS in AID group.");
        } else if (aids2.size() <= 256) {
            for (String aid : aids2) {
                if (!CardEmulation.isValidAid(aid)) {
                    throw new IllegalArgumentException("AID " + aid + " is not a valid AID.");
                }
            }
            if (isValidCategory(category2)) {
                this.category = category2;
            } else {
                this.category = "other";
            }
            this.aids = new ArrayList(aids2.size());
            for (String aid2 : aids2) {
                this.aids.add(aid2.toUpperCase());
            }
            this.description = null;
        } else {
            throw new IllegalArgumentException("Too many AIDs in AID group.");
        }
    }

    @UnsupportedAppUsage
    AidGroup(String category2, String description2) {
        this.aids = new ArrayList();
        this.category = category2;
        this.description = description2;
    }

    @UnsupportedAppUsage
    public String getCategory() {
        return this.category;
    }

    @UnsupportedAppUsage
    public List<String> getAids() {
        return this.aids;
    }

    public String toString() {
        StringBuilder out = new StringBuilder("Category: " + this.category + ", AIDs:");
        for (String aid : this.aids) {
            out.append(aid);
            out.append(", ");
        }
        return out.toString();
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.category);
        dest.writeInt(this.aids.size());
        if (this.aids.size() > 0) {
            dest.writeStringList(this.aids);
        }
    }

    @UnsupportedAppUsage
    public static AidGroup createFromXml(XmlPullParser parser) throws XmlPullParserException, IOException {
        String category2 = null;
        ArrayList<String> aids2 = new ArrayList<>();
        boolean inGroup = false;
        int eventType = parser.getEventType();
        int minDepth = parser.getDepth();
        while (eventType != 1 && parser.getDepth() >= minDepth) {
            String tagName = parser.getName();
            if (eventType == 2) {
                if (tagName.equals("aid")) {
                    if (inGroup) {
                        String aid = parser.getAttributeValue(null, "value");
                        if (aid != null) {
                            aids2.add(aid.toUpperCase());
                        }
                    } else {
                        Log.d(TAG, "Ignoring <aid> tag while not in group");
                    }
                } else if (tagName.equals("aid-group")) {
                    category2 = parser.getAttributeValue(null, "category");
                    if (category2 == null) {
                        Log.e(TAG, "<aid-group> tag without valid category");
                        return null;
                    }
                    inGroup = true;
                } else {
                    Log.d(TAG, "Ignoring unexpected tag: " + tagName);
                }
            } else if (eventType == 3 && tagName.equals("aid-group") && inGroup && aids2.size() > 0) {
                return new AidGroup(aids2, category2);
            }
            eventType = parser.next();
        }
        return null;
    }

    @UnsupportedAppUsage
    public void writeAsXml(XmlSerializer out) throws IOException {
        out.startTag(null, "aid-group");
        out.attribute(null, "category", this.category);
        for (String aid : this.aids) {
            out.startTag(null, "aid");
            out.attribute(null, "value", aid);
            out.endTag(null, "aid");
        }
        out.endTag(null, "aid-group");
    }

    static boolean isValidCategory(String category2) {
        return CardEmulation.CATEGORY_PAYMENT.equals(category2) || "other".equals(category2);
    }
}
