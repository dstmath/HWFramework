package android.nfc.cardemulation;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.provider.MediaStore.Video.VideoColumns;
import android.provider.Settings.NameValueTable;
import android.util.Log;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public final class AidGroup implements Parcelable {
    public static final Creator<AidGroup> CREATOR = null;
    public static final int MAX_NUM_AIDS = 256;
    static final String TAG = "AidGroup";
    final List<String> aids;
    final String category;
    final String description;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.nfc.cardemulation.AidGroup.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.nfc.cardemulation.AidGroup.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.nfc.cardemulation.AidGroup.<clinit>():void");
    }

    public AidGroup(List<String> aids, String category, String description) {
        if (aids == null || aids.size() == 0) {
            throw new IllegalArgumentException("No AIDS in AID group.");
        } else if (aids.size() > MAX_NUM_AIDS) {
            throw new IllegalArgumentException("Too many AIDs in AID group.");
        } else {
            for (String aid : aids) {
                if (!CardEmulation.isValidAid(aid)) {
                    throw new IllegalArgumentException("AID " + aid + " is not a valid AID.");
                }
            }
            if (isValidCategory(category)) {
                this.category = category;
            } else {
                this.category = CardEmulation.CATEGORY_OTHER;
            }
            this.aids = new ArrayList(aids.size());
            for (String aid2 : aids) {
                this.aids.add(aid2.toUpperCase(Locale.US));
            }
            this.description = description;
        }
    }

    public AidGroup(List<String> aids, String category) {
        if (aids == null || aids.size() == 0) {
            throw new IllegalArgumentException("No AIDS in AID group.");
        } else if (aids.size() > MAX_NUM_AIDS) {
            throw new IllegalArgumentException("Too many AIDs in AID group.");
        } else {
            for (String aid : aids) {
                if (!CardEmulation.isValidAid(aid)) {
                    throw new IllegalArgumentException("AID " + aid + " is not a valid AID.");
                }
            }
            if (isValidCategory(category)) {
                this.category = category;
            } else {
                this.category = CardEmulation.CATEGORY_OTHER;
            }
            this.aids = new ArrayList(aids.size());
            for (String aid2 : aids) {
                this.aids.add(aid2.toUpperCase());
            }
            this.description = null;
        }
    }

    public AidGroup(String category, String description) {
        this.aids = new ArrayList();
        this.category = category;
        this.description = description;
    }

    public String getCategory() {
        return this.category;
    }

    public String getDescription() {
        return this.description;
    }

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

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.category);
        dest.writeInt(this.aids.size());
        if (this.aids.size() > 0) {
            dest.writeStringList(this.aids);
        }
        if (this.description != null) {
            dest.writeString(this.description);
        } else {
            dest.writeString(null);
        }
    }

    public static AidGroup createFromXml(XmlPullParser parser) throws XmlPullParserException, IOException {
        String category = null;
        String description = null;
        ArrayList<String> aids = new ArrayList();
        AidGroup aidGroup = null;
        boolean inGroup = false;
        int eventType = parser.getEventType();
        int minDepth = parser.getDepth();
        while (eventType != 1 && parser.getDepth() >= minDepth) {
            String tagName = parser.getName();
            if (eventType != 2) {
                if (eventType == 3 && tagName.equals("aid-group") && inGroup && aids.size() > 0) {
                    aidGroup = new AidGroup(aids, category, description);
                    break;
                }
            } else if (tagName.equals("aid")) {
                if (inGroup) {
                    String aid = parser.getAttributeValue(null, NameValueTable.VALUE);
                    if (aid != null) {
                        aids.add(aid.toUpperCase());
                    }
                } else {
                    Log.d(TAG, "Ignoring <aid> tag while not in group");
                }
            } else if (tagName.equals("aid-group")) {
                category = parser.getAttributeValue(null, VideoColumns.CATEGORY);
                description = parser.getAttributeValue(null, VideoColumns.DESCRIPTION);
                if (category == null) {
                    Log.e(TAG, "<aid-group> tag without valid category");
                    return null;
                }
                inGroup = true;
            } else {
                Log.d(TAG, "Ignoring unexpected tag: " + tagName);
            }
            eventType = parser.next();
        }
        return aidGroup;
    }

    public void writeAsXml(XmlSerializer out) throws IOException {
        out.startTag(null, "aid-group");
        out.attribute(null, VideoColumns.CATEGORY, this.category);
        if (this.description != null) {
            out.attribute(null, VideoColumns.DESCRIPTION, this.description);
        }
        for (String aid : this.aids) {
            out.startTag(null, "aid");
            out.attribute(null, NameValueTable.VALUE, aid);
            out.endTag(null, "aid");
        }
        out.endTag(null, "aid-group");
    }

    static boolean isValidCategory(String category) {
        if (CardEmulation.CATEGORY_PAYMENT.equals(category)) {
            return true;
        }
        return CardEmulation.CATEGORY_OTHER.equals(category);
    }
}
