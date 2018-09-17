package android.media;

import android.app.Instrumentation;
import android.hardware.camera2.params.TonemapCurve;
import android.net.ProxyInfo;
import android.net.wifi.WifiEnterpriseConfig;
import android.util.Log;
import java.util.Vector;

/* compiled from: WebVttRenderer */
class WebVttParser {
    private static final String TAG = "WebVttParser";
    private String mBuffer = ProxyInfo.LOCAL_EXCL_LIST;
    private TextTrackCue mCue;
    private Vector<String> mCueTexts;
    private WebVttCueListener mListener;
    private final Phase mParseCueId = new Phase() {
        static final /* synthetic */ boolean -assertionsDisabled = (AnonymousClass4.class.desiredAssertionStatus() ^ 1);

        public void parse(String line) {
            if (line.length() != 0) {
                if (-assertionsDisabled || WebVttParser.this.mCue == null) {
                    if (line.equals("NOTE") || line.startsWith("NOTE ")) {
                        WebVttParser.this.mPhase = WebVttParser.this.mParseCueText;
                    }
                    WebVttParser.this.mCue = new TextTrackCue();
                    WebVttParser.this.mCueTexts.clear();
                    WebVttParser.this.mPhase = WebVttParser.this.mParseCueTime;
                    if (line.contains("-->")) {
                        WebVttParser.this.mPhase.parse(line);
                    } else {
                        WebVttParser.this.mCue.mId = line;
                    }
                    return;
                }
                throw new AssertionError();
            }
        }
    };
    private final Phase mParseCueText = new Phase() {
        public void parse(String line) {
            if (line.length() == 0) {
                WebVttParser.this.yieldCue();
                WebVttParser.this.mPhase = WebVttParser.this.mParseCueId;
                return;
            }
            if (WebVttParser.this.mCue != null) {
                WebVttParser.this.mCueTexts.add(line);
            }
        }
    };
    private final Phase mParseCueTime = new Phase() {
        static final /* synthetic */ boolean -assertionsDisabled = (AnonymousClass5.class.desiredAssertionStatus() ^ 1);

        public void parse(String line) {
            int arrowAt = line.indexOf("-->");
            if (arrowAt < 0) {
                WebVttParser.this.mCue = null;
                WebVttParser.this.mPhase = WebVttParser.this.mParseCueId;
                return;
            }
            String start = line.substring(0, arrowAt).trim();
            String rest = line.substring(arrowAt + 3).replaceFirst("^\\s+", ProxyInfo.LOCAL_EXCL_LIST).replaceFirst("\\s+", WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
            int spaceAt = rest.indexOf(32);
            String end = spaceAt > 0 ? rest.substring(0, spaceAt) : rest;
            rest = spaceAt > 0 ? rest.substring(spaceAt + 1) : ProxyInfo.LOCAL_EXCL_LIST;
            WebVttParser.this.mCue.mStartTimeMs = WebVttParser.parseTimestampMs(start);
            WebVttParser.this.mCue.mEndTimeMs = WebVttParser.parseTimestampMs(end);
            for (String setting : rest.split(" +")) {
                int colonAt = setting.indexOf(58);
                if (colonAt > 0 && colonAt != setting.length() - 1) {
                    String name = setting.substring(0, colonAt);
                    String value = setting.substring(colonAt + 1);
                    if (name.equals(TtmlUtils.TAG_REGION)) {
                        WebVttParser.this.mCue.mRegionId = value;
                    } else if (name.equals("vertical")) {
                        if (value.equals("rl")) {
                            WebVttParser.this.mCue.mWritingDirection = 101;
                        } else if (value.equals("lr")) {
                            WebVttParser.this.mCue.mWritingDirection = 102;
                        } else {
                            WebVttParser.this.log_warning("cue setting", name, "has invalid value", value);
                        }
                    } else if (name.equals("line")) {
                        try {
                            if (!-assertionsDisabled && value.indexOf(32) >= 0) {
                                throw new AssertionError();
                            } else if (value.endsWith("%")) {
                                WebVttParser.this.mCue.mSnapToLines = false;
                                WebVttParser.this.mCue.mLinePosition = Integer.valueOf(WebVttParser.parseIntPercentage(value));
                            } else if (value.matches(".*[^0-9].*")) {
                                WebVttParser.this.log_warning("cue setting", name, "contains an invalid character", value);
                            } else {
                                WebVttParser.this.mCue.mSnapToLines = true;
                                WebVttParser.this.mCue.mLinePosition = Integer.valueOf(Integer.parseInt(value));
                            }
                        } catch (NumberFormatException e) {
                            WebVttParser.this.log_warning("cue setting", name, "is not numeric or percentage", value);
                        }
                    } else if (name.equals("position")) {
                        try {
                            WebVttParser.this.mCue.mTextPosition = WebVttParser.parseIntPercentage(value);
                        } catch (NumberFormatException e2) {
                            WebVttParser.this.log_warning("cue setting", name, "is not numeric or percentage", value);
                        }
                    } else if (name.equals("size")) {
                        try {
                            WebVttParser.this.mCue.mSize = WebVttParser.parseIntPercentage(value);
                        } catch (NumberFormatException e3) {
                            WebVttParser.this.log_warning("cue setting", name, "is not numeric or percentage", value);
                        }
                    } else if (name.equals("align")) {
                        if (value.equals("start")) {
                            WebVttParser.this.mCue.mAlignment = 201;
                        } else if (value.equals("middle")) {
                            WebVttParser.this.mCue.mAlignment = 200;
                        } else if (value.equals(TtmlUtils.ATTR_END)) {
                            WebVttParser.this.mCue.mAlignment = 202;
                        } else if (value.equals("left")) {
                            WebVttParser.this.mCue.mAlignment = 203;
                        } else if (value.equals("right")) {
                            WebVttParser.this.mCue.mAlignment = 204;
                        } else {
                            WebVttParser.this.log_warning("cue setting", name, "has invalid value", value);
                        }
                    }
                }
            }
            if (!(WebVttParser.this.mCue.mLinePosition == null && WebVttParser.this.mCue.mSize == 100 && WebVttParser.this.mCue.mWritingDirection == 100)) {
                WebVttParser.this.mCue.mRegionId = ProxyInfo.LOCAL_EXCL_LIST;
            }
            WebVttParser.this.mPhase = WebVttParser.this.mParseCueText;
        }
    };
    private final Phase mParseHeader = new Phase() {
        static final /* synthetic */ boolean -assertionsDisabled = (AnonymousClass3.class.desiredAssertionStatus() ^ 1);

        TextTrackRegion parseRegion(String s) {
            TextTrackRegion region = new TextTrackRegion();
            String[] split = s.split(" +");
            int i = 0;
            int length = split.length;
            while (true) {
                int i2 = i;
                if (i2 >= length) {
                    return region;
                }
                String setting = split[i2];
                int equalAt = setting.indexOf(61);
                if (equalAt > 0 && equalAt != setting.length() - 1) {
                    String name = setting.substring(0, equalAt);
                    String value = setting.substring(equalAt + 1);
                    if (name.equals(Instrumentation.REPORT_KEY_IDENTIFIER)) {
                        region.mId = value;
                    } else if (name.equals(MediaFormat.KEY_WIDTH)) {
                        try {
                            region.mWidth = WebVttParser.parseFloatPercentage(value);
                        } catch (NumberFormatException e) {
                            WebVttParser.this.log_warning("region setting", name, "has invalid value", e.getMessage(), value);
                        }
                    } else if (name.equals("lines")) {
                        if (value.matches(".*[^0-9].*")) {
                            WebVttParser.this.log_warning("lines", name, "contains an invalid character", value);
                        } else {
                            try {
                                region.mLines = Integer.parseInt(value);
                                if (!-assertionsDisabled && region.mLines < 0) {
                                    throw new AssertionError();
                                }
                            } catch (NumberFormatException e2) {
                                WebVttParser.this.log_warning("region setting", name, "is not numeric", value);
                            }
                        }
                    } else if (name.equals("regionanchor") || name.equals("viewportanchor")) {
                        int commaAt = value.indexOf(",");
                        if (commaAt < 0) {
                            WebVttParser.this.log_warning("region setting", name, "contains no comma", value);
                        } else {
                            String anchorX = value.substring(0, commaAt);
                            String anchorY = value.substring(commaAt + 1);
                            try {
                                float x = WebVttParser.parseFloatPercentage(anchorX);
                                try {
                                    float y = WebVttParser.parseFloatPercentage(anchorY);
                                    if (name.charAt(0) == 'r') {
                                        region.mAnchorPointX = x;
                                        region.mAnchorPointY = y;
                                    } else {
                                        region.mViewportAnchorPointX = x;
                                        region.mViewportAnchorPointY = y;
                                    }
                                } catch (NumberFormatException e3) {
                                    WebVttParser.this.log_warning("region setting", name, "has invalid y component", e3.getMessage(), anchorY);
                                }
                            } catch (NumberFormatException e32) {
                                WebVttParser.this.log_warning("region setting", name, "has invalid x component", e32.getMessage(), anchorX);
                            }
                        }
                    } else if (name.equals("scroll")) {
                        if (value.equals("up")) {
                            region.mScrollValue = MediaFile.FILE_TYPE_CR2;
                        } else {
                            WebVttParser.this.log_warning("region setting", name, "has invalid value", value);
                        }
                    }
                }
                i = i2 + 1;
            }
        }

        public void parse(String line) {
            if (line.length() == 0) {
                WebVttParser.this.mPhase = WebVttParser.this.mParseCueId;
            } else if (line.contains("-->")) {
                WebVttParser.this.mPhase = WebVttParser.this.mParseCueTime;
                WebVttParser.this.mPhase.parse(line);
            } else {
                int colonAt = line.indexOf(58);
                if (colonAt <= 0 || colonAt >= line.length() - 1) {
                    WebVttParser.this.log_warning("meta data header has invalid format", line);
                }
                String name = line.substring(0, colonAt);
                String value = line.substring(colonAt + 1);
                if (name.equals("Region")) {
                    WebVttParser.this.mListener.onRegionParsed(parseRegion(value));
                }
            }
        }
    };
    private final Phase mParseStart = new Phase() {
        public void parse(String line) {
            if (line.startsWith("﻿")) {
                line = line.substring(1);
            }
            if (line.equals("WEBVTT") || (line.startsWith("WEBVTT ") ^ 1) == 0 || (line.startsWith("WEBVTT\t") ^ 1) == 0) {
                WebVttParser.this.mPhase = WebVttParser.this.mParseHeader;
                return;
            }
            WebVttParser.this.log_warning("Not a WEBVTT header", line);
            WebVttParser.this.mPhase = WebVttParser.this.mSkipRest;
        }
    };
    private Phase mPhase = this.mParseStart;
    private final Phase mSkipRest = new Phase() {
        public void parse(String line) {
        }
    };

    /* compiled from: WebVttRenderer */
    interface Phase {
        void parse(String str);
    }

    WebVttParser(WebVttCueListener listener) {
        this.mListener = listener;
        this.mCueTexts = new Vector();
    }

    public static float parseFloatPercentage(String s) throws NumberFormatException {
        if (s.endsWith("%")) {
            s = s.substring(0, s.length() - 1);
            if (s.matches(".*[^0-9.].*")) {
                throw new NumberFormatException("contains an invalid character");
            }
            try {
                float value = Float.parseFloat(s);
                if (value >= TonemapCurve.LEVEL_BLACK && value <= 100.0f) {
                    return value;
                }
                throw new NumberFormatException("is out of range");
            } catch (NumberFormatException e) {
                throw new NumberFormatException("is not a number");
            }
        }
        throw new NumberFormatException("does not end in %");
    }

    public static int parseIntPercentage(String s) throws NumberFormatException {
        if (s.endsWith("%")) {
            s = s.substring(0, s.length() - 1);
            if (s.matches(".*[^0-9].*")) {
                throw new NumberFormatException("contains an invalid character");
            }
            try {
                int value = Integer.parseInt(s);
                if (value >= 0 && value <= 100) {
                    return value;
                }
                throw new NumberFormatException("is out of range");
            } catch (NumberFormatException e) {
                throw new NumberFormatException("is not a number");
            }
        }
        throw new NumberFormatException("does not end in %");
    }

    public static long parseTimestampMs(String s) throws NumberFormatException {
        int i = 0;
        if (s.matches("(\\d+:)?[0-5]\\d:[0-5]\\d\\.\\d{3}")) {
            String[] parts = s.split("\\.", 2);
            long value = 0;
            String[] split = parts[0].split(":");
            while (i < split.length) {
                value = (60 * value) + Long.parseLong(split[i]);
                i++;
            }
            return (1000 * value) + Long.parseLong(parts[1]);
        }
        throw new NumberFormatException("has invalid format");
    }

    public static String timeToString(long timeMs) {
        return String.format("%d:%02d:%02d.%03d", new Object[]{Long.valueOf(timeMs / 3600000), Long.valueOf((timeMs / 60000) % 60), Long.valueOf((timeMs / 1000) % 60), Long.valueOf(timeMs % 1000)});
    }

    public void parse(String s) {
        boolean trailingCR = false;
        this.mBuffer = (this.mBuffer + s.replace("\u0000", "�")).replace("\r\n", "\n");
        if (this.mBuffer.endsWith("\r")) {
            trailingCR = true;
            this.mBuffer = this.mBuffer.substring(0, this.mBuffer.length() - 1);
        }
        String[] lines = this.mBuffer.split("[\r\n]");
        for (int i = 0; i < lines.length - 1; i++) {
            this.mPhase.parse(lines[i]);
        }
        this.mBuffer = lines[lines.length - 1];
        if (trailingCR) {
            this.mBuffer += "\r";
        }
    }

    public void eos() {
        if (this.mBuffer.endsWith("\r")) {
            this.mBuffer = this.mBuffer.substring(0, this.mBuffer.length() - 1);
        }
        this.mPhase.parse(this.mBuffer);
        this.mBuffer = ProxyInfo.LOCAL_EXCL_LIST;
        yieldCue();
        this.mPhase = this.mParseStart;
    }

    public void yieldCue() {
        if (this.mCue != null && this.mCueTexts.size() > 0) {
            this.mCue.mStrings = new String[this.mCueTexts.size()];
            this.mCueTexts.toArray(this.mCue.mStrings);
            this.mCueTexts.clear();
            this.mListener.onCueParsed(this.mCue);
        }
        this.mCue = null;
    }

    private void log_warning(String nameType, String name, String message, String subMessage, String value) {
        Log.w(getClass().getName(), nameType + " '" + name + "' " + message + " ('" + value + "' " + subMessage + ")");
    }

    private void log_warning(String nameType, String name, String message, String value) {
        Log.w(getClass().getName(), nameType + " '" + name + "' " + message + " ('" + value + "')");
    }

    private void log_warning(String message, String value) {
        Log.w(getClass().getName(), message + " ('" + value + "')");
    }
}
