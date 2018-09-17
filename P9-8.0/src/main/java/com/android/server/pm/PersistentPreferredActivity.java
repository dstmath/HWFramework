package com.android.server.pm;

import android.content.ComponentName;
import android.content.IntentFilter;
import com.android.internal.util.XmlUtils;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

class PersistentPreferredActivity extends IntentFilter {
    private static final String ATTR_FILTER = "filter";
    private static final String ATTR_NAME = "name";
    private static final boolean DEBUG_FILTERS = false;
    private static final String TAG = "PersistentPreferredActivity";
    final ComponentName mComponent;

    PersistentPreferredActivity(IntentFilter filter, ComponentName activity) {
        super(filter);
        this.mComponent = activity;
    }

    PersistentPreferredActivity(XmlPullParser parser) throws XmlPullParserException, IOException {
        String shortComponent = parser.getAttributeValue(null, ATTR_NAME);
        this.mComponent = ComponentName.unflattenFromString(shortComponent);
        if (this.mComponent == null) {
            PackageManagerService.reportSettingsProblem(5, "Error in package manager settings: Bad activity name " + shortComponent + " at " + parser.getPositionDescription());
        }
        int outerDepth = parser.getDepth();
        String tagName = parser.getName();
        while (true) {
            int type = parser.next();
            if (type == 1 || (type == 3 && parser.getDepth() <= outerDepth)) {
                break;
            }
            tagName = parser.getName();
            if (!(type == 3 || type == 4 || type != 2)) {
                if (tagName.equals(ATTR_FILTER)) {
                    break;
                }
                PackageManagerService.reportSettingsProblem(5, "Unknown element: " + tagName + " at " + parser.getPositionDescription());
                XmlUtils.skipCurrentTag(parser);
            }
        }
        if (tagName.equals(ATTR_FILTER)) {
            readFromXml(parser);
            return;
        }
        PackageManagerService.reportSettingsProblem(5, "Missing element filter at " + parser.getPositionDescription());
        XmlUtils.skipCurrentTag(parser);
    }

    public void writeToXml(XmlSerializer serializer) throws IOException {
        serializer.attribute(null, ATTR_NAME, this.mComponent.flattenToShortString());
        serializer.startTag(null, ATTR_FILTER);
        super.writeToXml(serializer);
        serializer.endTag(null, ATTR_FILTER);
    }

    public String toString() {
        return "PersistentPreferredActivity{0x" + Integer.toHexString(System.identityHashCode(this)) + " " + this.mComponent.flattenToShortString() + "}";
    }
}
