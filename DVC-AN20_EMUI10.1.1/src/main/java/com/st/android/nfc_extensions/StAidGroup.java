package com.st.android.nfc_extensions;

import android.nfc.cardemulation.AidGroup;
import android.nfc.cardemulation.CardEmulation;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public final class StAidGroup implements Parcelable {
    public static final Parcelable.Creator<StAidGroup> CREATOR = new Parcelable.Creator<StAidGroup>() {
        /* class com.st.android.nfc_extensions.StAidGroup.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public StAidGroup createFromParcel(Parcel source) {
            String category = source.readString();
            int listSize = source.readInt();
            ArrayList<String> aidList = new ArrayList<>();
            if (listSize > 0) {
                source.readStringList(aidList);
            }
            return new StAidGroup(aidList, category, source.readString());
        }

        @Override // android.os.Parcelable.Creator
        public StAidGroup[] newArray(int size) {
            return new StAidGroup[size];
        }
    };
    static final boolean DBG = (!"user".equals(Build.TYPE) && !"userdebug".equals(Build.TYPE));
    static final String TAG = "APINfc_StAidGroup";
    final List<String> aids;
    final String category;
    final String description;

    public StAidGroup(List<String> aids2, String category2, String description2) {
        if (DBG) {
            Log.d(TAG, "constructor - category: " + category2 + ", description: " + description2);
        }
        if (aids2 == null || aids2.size() == 0) {
            this.aids = new ArrayList();
        } else if (aids2.size() <= 256) {
            for (String aid : aids2) {
                if (!CardEmulation.isValidAid(aid)) {
                    throw new IllegalArgumentException("AID " + aid + " is not a valid AID.");
                }
            }
            this.aids = new ArrayList(aids2.size());
            for (String aid2 : aids2) {
                this.aids.add(aid2.toUpperCase());
            }
        } else {
            throw new IllegalArgumentException("Too many AIDs in AID group.");
        }
        if (isValidCategory(category2)) {
            this.category = category2;
        } else {
            this.category = "other";
        }
        this.description = description2;
    }

    public StAidGroup(String category2, String description2) {
        if (DBG) {
            Log.d(TAG, "constructor - category: " + category2 + ", description: " + description2);
        }
        this.aids = new ArrayList();
        this.category = category2;
        this.description = description2;
    }

    public StAidGroup(AidGroup aid, String description2) {
        if (DBG) {
            Log.d(TAG, "constructor");
        }
        this.aids = aid.getAids();
        this.category = aid.getCategory();
        this.description = description2;
    }

    public String getCategory() {
        return this.category;
    }

    public List<String> getAids() {
        return this.aids;
    }

    public AidGroup getAidGroup() {
        return new AidGroup(this.aids, this.category);
    }

    public String getDescription() {
        if (DBG) {
            Log.d(TAG, "getDescription() - description: " + this.description);
        }
        return this.description;
    }

    public String toString() {
        StringBuilder out = new StringBuilder("Category: " + this.category + ", AIDs:");
        for (String aid : this.aids) {
            out.append(aid);
            out.append(", ");
        }
        return out.toString();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.category);
        dest.writeInt(this.aids.size());
        if (this.aids.size() > 0) {
            dest.writeStringList(this.aids);
        }
        dest.writeString(this.description);
    }

    public static StAidGroup createFromXml(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (DBG) {
            Log.d(TAG, "createFromXml()");
        }
        String category2 = null;
        String description2 = null;
        ArrayList<String> aids2 = new ArrayList<>();
        StAidGroup group = null;
        boolean inGroup = false;
        int eventType = parser.getEventType();
        int minDepth = parser.getDepth();
        while (true) {
            if (eventType == 1 || parser.getDepth() < minDepth) {
                break;
            }
            String tagName = parser.getName();
            if (eventType != 2) {
                if (eventType == 3 && tagName.equals("aid-group") && inGroup && aids2.size() > 0) {
                    group = new StAidGroup(aids2, category2, description2);
                    break;
                }
            } else if (tagName.equals("aid")) {
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
                description2 = parser.getAttributeValue(null, "description");
                inGroup = true;
            } else {
                Log.d(TAG, "Ignoring unexpected tag: " + tagName);
            }
            eventType = parser.next();
        }
        if (DBG) {
            Log.d(TAG, "createFromXml() - category: " + category2 + ", description: " + description2);
        }
        return group;
    }

    public void writeAsXml(XmlSerializer out) throws IOException {
        if (DBG) {
            Log.d(TAG, "writeAsXml()");
        }
        out.startTag(null, "aid-group");
        out.attribute(null, "category", this.category);
        String str = this.description;
        if (str != null) {
            out.attribute(null, "description", str);
        }
        for (String aid : this.aids) {
            out.startTag(null, "aid");
            out.attribute(null, "value", aid);
            out.endTag(null, "aid");
        }
        out.endTag(null, "aid-group");
    }

    static boolean isValidCategory(String category2) {
        return "payment".equals(category2) || "other".equals(category2);
    }
}
