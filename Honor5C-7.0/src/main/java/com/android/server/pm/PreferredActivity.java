package com.android.server.pm;

import android.content.ComponentName;
import android.content.IntentFilter;
import com.android.internal.util.XmlUtils;
import com.android.server.pm.PreferredComponent.Callbacks;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

class PreferredActivity extends IntentFilter implements Callbacks {
    private static final boolean DEBUG_FILTERS = false;
    private static final String TAG = "PreferredActivity";
    final PreferredComponent mPref;

    PreferredActivity(IntentFilter filter, int match, ComponentName[] set, ComponentName activity, boolean always) {
        super(filter);
        this.mPref = new PreferredComponent(this, match, set, activity, always);
    }

    PreferredActivity(XmlPullParser parser) throws XmlPullParserException, IOException {
        this.mPref = new PreferredComponent(this, parser);
    }

    public void writeToXml(XmlSerializer serializer, boolean full) throws IOException {
        this.mPref.writeToXml(serializer, full);
        serializer.startTag(null, "filter");
        super.writeToXml(serializer);
        serializer.endTag(null, "filter");
    }

    public boolean onReadTag(String tagName, XmlPullParser parser) throws XmlPullParserException, IOException {
        if (tagName.equals("filter")) {
            readFromXml(parser);
        } else {
            PackageManagerService.reportSettingsProblem(5, "Unknown element under <preferred-activities>: " + parser.getName());
            XmlUtils.skipCurrentTag(parser);
        }
        return true;
    }

    public String toString() {
        return "PreferredActivity{0x" + Integer.toHexString(System.identityHashCode(this)) + " " + this.mPref.mComponent.flattenToShortString() + "}";
    }
}
