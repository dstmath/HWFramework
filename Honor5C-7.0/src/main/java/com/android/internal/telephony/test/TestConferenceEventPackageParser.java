package com.android.internal.telephony.test;

import android.os.Bundle;
import android.provider.Telephony.Carriers;
import android.util.Log;
import android.util.Xml;
import com.android.ims.ImsConferenceState;
import com.android.internal.telephony.TelephonyEventLog;
import com.android.internal.util.XmlUtils;
import java.io.IOException;
import java.io.InputStream;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class TestConferenceEventPackageParser {
    private static final String LOG_TAG = "TestConferenceEventPackageParser";
    private static final String PARTICIPANT_TAG = "participant";
    private InputStream mInputStream;

    public TestConferenceEventPackageParser(InputStream inputStream) {
        this.mInputStream = inputStream;
    }

    public ImsConferenceState parse() {
        ImsConferenceState conferenceState = new ImsConferenceState();
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(this.mInputStream, null);
            parser.nextTag();
            int outerDepth = parser.getDepth();
            while (XmlUtils.nextElementWithin(parser, outerDepth)) {
                if (parser.getName().equals(PARTICIPANT_TAG)) {
                    Log.v(LOG_TAG, "Found participant.");
                    Bundle participant = parseParticipant(parser);
                    conferenceState.mParticipants.put(participant.getString("endpoint"), participant);
                }
            }
            return conferenceState;
        } catch (Exception e) {
            Log.e(LOG_TAG, "Failed to read test conference event package from XML file", e);
            return null;
        } finally {
            try {
                this.mInputStream.close();
            } catch (IOException e2) {
                Log.e(LOG_TAG, "Failed to close test conference event package InputStream", e2);
                return null;
            }
        }
    }

    private Bundle parseParticipant(XmlPullParser parser) throws IOException, XmlPullParserException {
        Bundle bundle = new Bundle();
        String user = "";
        String displayText = "";
        String endpoint = "";
        String status = "";
        int outerDepth = parser.getDepth();
        while (XmlUtils.nextElementWithin(parser, outerDepth)) {
            if (parser.getName().equals(Carriers.USER)) {
                parser.next();
                user = parser.getText();
            } else if (parser.getName().equals("display-text")) {
                parser.next();
                displayText = parser.getText();
            } else if (parser.getName().equals("endpoint")) {
                parser.next();
                endpoint = parser.getText();
            } else if (parser.getName().equals(TelephonyEventLog.DATA_KEY_DATA_CALL_STATUS)) {
                parser.next();
                status = parser.getText();
            }
        }
        Log.v(LOG_TAG, "User: " + user);
        Log.v(LOG_TAG, "DisplayText: " + displayText);
        Log.v(LOG_TAG, "Endpoint: " + endpoint);
        Log.v(LOG_TAG, "Status: " + status);
        bundle.putString(Carriers.USER, user);
        bundle.putString("display-text", displayText);
        bundle.putString("endpoint", endpoint);
        bundle.putString(TelephonyEventLog.DATA_KEY_DATA_CALL_STATUS, status);
        return bundle;
    }
}
