package com.android.server.usage;

import android.app.usage.ConfigurationStats;
import android.app.usage.TimeSparseArray;
import android.app.usage.UsageEvents.Event;
import android.app.usage.UsageStats;
import android.content.res.Configuration;
import com.android.internal.util.XmlUtils;
import java.io.IOException;
import java.net.ProtocolException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

final class UsageStatsXmlV1 {
    private static final String ACTIVE_ATTR = "active";
    private static final String CLASS_ATTR = "class";
    private static final String CONFIGURATIONS_TAG = "configurations";
    private static final String CONFIG_TAG = "config";
    private static final String COUNT_ATTR = "count";
    private static final String END_TIME_ATTR = "endTime";
    private static final String EVENT_LOG_TAG = "event-log";
    private static final String EVENT_TAG = "event";
    private static final String LAND_TIME_ACTIVE_ATTR = "landTimeActive";
    private static final String LAST_EVENT_ATTR = "lastEvent";
    private static final String LAST_LAND_TIME_ACTIVE_ATTR = "lastLandTimeActive";
    private static final String LAST_TIME_ACTIVE_ATTR = "lastTimeActive";
    private static final String LAUNCH_COUNT_ATTR = "launchCount";
    private static final String PACKAGES_TAG = "packages";
    private static final String PACKAGE_ATTR = "package";
    private static final String PACKAGE_TAG = "package";
    private static final String TIME_ATTR = "time";
    private static final String TOTAL_TIME_ACTIVE_ATTR = "timeActive";
    private static final String TYPE_ATTR = "type";

    private static void loadUsageStats(XmlPullParser parser, IntervalStats statsOut) throws IOException {
        String pkg = parser.getAttributeValue(null, PACKAGE_TAG);
        if (pkg == null) {
            throw new ProtocolException("no package attribute present");
        }
        UsageStats stats = statsOut.getOrCreateUsageStats(pkg);
        stats.mLastTimeUsed = statsOut.beginTime + XmlUtils.readLongAttribute(parser, LAST_TIME_ACTIVE_ATTR);
        stats.mTotalTimeInForeground = XmlUtils.readLongAttribute(parser, TOTAL_TIME_ACTIVE_ATTR);
        stats.mLastEvent = XmlUtils.readIntAttribute(parser, LAST_EVENT_ATTR);
        stats.mLaunchCount = XmlUtils.readIntAttribute(parser, LAUNCH_COUNT_ATTR);
        stats.mLandTimeInForeground = XmlUtils.readLongAttribute(parser, LAND_TIME_ACTIVE_ATTR, 0);
        stats.mLastLandTimeUsed = statsOut.beginTime + XmlUtils.readLongAttribute(parser, LAST_LAND_TIME_ACTIVE_ATTR, 0);
    }

    private static void loadConfigStats(XmlPullParser parser, IntervalStats statsOut) throws XmlPullParserException, IOException {
        Configuration config = new Configuration();
        Configuration.readXmlAttrs(parser, config);
        ConfigurationStats configStats = statsOut.getOrCreateConfigurationStats(config);
        configStats.mLastTimeActive = statsOut.beginTime + XmlUtils.readLongAttribute(parser, LAST_TIME_ACTIVE_ATTR);
        configStats.mTotalTimeActive = XmlUtils.readLongAttribute(parser, TOTAL_TIME_ACTIVE_ATTR);
        configStats.mActivationCount = XmlUtils.readIntAttribute(parser, COUNT_ATTR);
        if (XmlUtils.readBooleanAttribute(parser, ACTIVE_ATTR)) {
            statsOut.activeConfiguration = configStats.mConfiguration;
        }
    }

    private static void loadEvent(XmlPullParser parser, IntervalStats statsOut) throws XmlPullParserException, IOException {
        String packageName = XmlUtils.readStringAttribute(parser, PACKAGE_TAG);
        if (packageName == null) {
            throw new ProtocolException("no package attribute present");
        }
        Event event = statsOut.buildEvent(packageName, XmlUtils.readStringAttribute(parser, CLASS_ATTR));
        event.mTimeStamp = statsOut.beginTime + XmlUtils.readLongAttribute(parser, TIME_ATTR);
        event.mEventType = XmlUtils.readIntAttribute(parser, TYPE_ATTR);
        if (event.mEventType == 5) {
            event.mConfiguration = new Configuration();
            Configuration.readXmlAttrs(parser, event.mConfiguration);
        }
        if (statsOut.events == null) {
            statsOut.events = new TimeSparseArray();
        }
        statsOut.events.put(event.mTimeStamp, event);
    }

    private static void writeUsageStats(XmlSerializer xml, IntervalStats stats, UsageStats usageStats) throws IOException {
        xml.startTag(null, PACKAGE_TAG);
        XmlUtils.writeLongAttribute(xml, LAST_TIME_ACTIVE_ATTR, usageStats.mLastTimeUsed - stats.beginTime);
        XmlUtils.writeStringAttribute(xml, PACKAGE_TAG, usageStats.mPackageName);
        XmlUtils.writeLongAttribute(xml, TOTAL_TIME_ACTIVE_ATTR, usageStats.mTotalTimeInForeground);
        XmlUtils.writeIntAttribute(xml, LAST_EVENT_ATTR, usageStats.mLastEvent);
        XmlUtils.writeIntAttribute(xml, LAUNCH_COUNT_ATTR, usageStats.mLaunchCount);
        XmlUtils.writeLongAttribute(xml, LAND_TIME_ACTIVE_ATTR, usageStats.mLandTimeInForeground);
        XmlUtils.writeLongAttribute(xml, LAST_LAND_TIME_ACTIVE_ATTR, usageStats.mLastLandTimeUsed - stats.beginTime);
        xml.endTag(null, PACKAGE_TAG);
    }

    private static void writeConfigStats(XmlSerializer xml, IntervalStats stats, ConfigurationStats configStats, boolean isActive) throws IOException {
        xml.startTag(null, CONFIG_TAG);
        XmlUtils.writeLongAttribute(xml, LAST_TIME_ACTIVE_ATTR, configStats.mLastTimeActive - stats.beginTime);
        XmlUtils.writeLongAttribute(xml, TOTAL_TIME_ACTIVE_ATTR, configStats.mTotalTimeActive);
        XmlUtils.writeIntAttribute(xml, COUNT_ATTR, configStats.mActivationCount);
        if (isActive) {
            XmlUtils.writeBooleanAttribute(xml, ACTIVE_ATTR, true);
        }
        Configuration.writeXmlAttrs(xml, configStats.mConfiguration);
        xml.endTag(null, CONFIG_TAG);
    }

    private static void writeEvent(XmlSerializer xml, IntervalStats stats, Event event) throws IOException {
        xml.startTag(null, EVENT_TAG);
        XmlUtils.writeLongAttribute(xml, TIME_ATTR, event.mTimeStamp - stats.beginTime);
        XmlUtils.writeStringAttribute(xml, PACKAGE_TAG, event.mPackage);
        if (event.mClass != null) {
            XmlUtils.writeStringAttribute(xml, CLASS_ATTR, event.mClass);
        }
        XmlUtils.writeIntAttribute(xml, TYPE_ATTR, event.mEventType);
        if (event.mEventType == 5 && event.mConfiguration != null) {
            Configuration.writeXmlAttrs(xml, event.mConfiguration);
        }
        xml.endTag(null, EVENT_TAG);
    }

    public static void read(XmlPullParser parser, IntervalStats statsOut) throws XmlPullParserException, IOException {
        statsOut.packageStats.clear();
        statsOut.configurations.clear();
        statsOut.activeConfiguration = null;
        if (statsOut.events != null) {
            statsOut.events.clear();
        }
        statsOut.endTime = statsOut.beginTime + XmlUtils.readLongAttribute(parser, END_TIME_ATTR);
        int outerDepth = parser.getDepth();
        while (true) {
            int eventCode = parser.next();
            if (eventCode == 1) {
                return;
            }
            if (eventCode == 3 && parser.getDepth() <= outerDepth) {
                return;
            }
            if (eventCode == 2) {
                String tag = parser.getName();
                if (tag.equals(PACKAGE_TAG)) {
                    loadUsageStats(parser, statsOut);
                } else if (tag.equals(CONFIG_TAG)) {
                    loadConfigStats(parser, statsOut);
                } else if (tag.equals(EVENT_TAG)) {
                    loadEvent(parser, statsOut);
                }
            }
        }
    }

    public static void write(XmlSerializer xml, IntervalStats stats) throws IOException {
        int i;
        XmlUtils.writeLongAttribute(xml, END_TIME_ATTR, stats.endTime - stats.beginTime);
        xml.startTag(null, PACKAGES_TAG);
        int statsCount = stats.packageStats.size();
        for (i = 0; i < statsCount; i++) {
            writeUsageStats(xml, stats, (UsageStats) stats.packageStats.valueAt(i));
        }
        xml.endTag(null, PACKAGES_TAG);
        xml.startTag(null, CONFIGURATIONS_TAG);
        int configCount = stats.configurations.size();
        for (i = 0; i < configCount; i++) {
            writeConfigStats(xml, stats, (ConfigurationStats) stats.configurations.valueAt(i), stats.activeConfiguration.equals((Configuration) stats.configurations.keyAt(i)));
        }
        xml.endTag(null, CONFIGURATIONS_TAG);
        xml.startTag(null, EVENT_LOG_TAG);
        int eventCount = stats.events != null ? stats.events.size() : 0;
        for (i = 0; i < eventCount; i++) {
            writeEvent(xml, stats, (Event) stats.events.valueAt(i));
        }
        xml.endTag(null, EVENT_LOG_TAG);
    }

    private UsageStatsXmlV1() {
    }
}
