package com.android.server.pm;

import android.content.ComponentName;
import android.content.IntentFilter;
import com.android.server.pm.PreferredComponent;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class PreferredActivityEx extends IntentFilter implements PreferredComponent.Callbacks {
    private PreferredActivity mActivity;

    public PreferredActivityEx() {
    }

    PreferredActivityEx(IntentFilter filter, int match, ComponentName[] set, ComponentName activity, boolean always) {
        this.mActivity = new PreferredActivity(filter, match, set, activity, always);
    }

    PreferredActivityEx(XmlPullParser parser) throws XmlPullParserException, IOException {
        this.mActivity = new PreferredActivity(parser);
    }

    public void writeToXml(XmlSerializer serializer, boolean full) throws IOException {
        this.mActivity.writeToXml(serializer, full);
    }

    public boolean onReadTag(String tagName, XmlPullParser parser) throws XmlPullParserException, IOException {
        return this.mActivity.onReadTag(tagName, parser);
    }

    @Override // java.lang.Object
    public String toString() {
        return this.mActivity.toString();
    }

    public PreferredActivity getPreferredActivity() {
        return this.mActivity;
    }

    public void setPreferredActivity(PreferredActivity mActivity2) {
        this.mActivity = mActivity2;
    }

    public PreferredComponentEx getPref() {
        PreferredComponentEx preferredComponentEx = new PreferredComponentEx();
        preferredComponentEx.setPreferredComponent(this.mActivity.mPref);
        return preferredComponentEx;
    }
}
