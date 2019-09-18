package android.media;

import android.util.Log;
import java.util.Vector;

/* compiled from: WebVttRenderer */
class WebVttParser {
    private static final String TAG = "WebVttParser";
    private String mBuffer = "";
    /* access modifiers changed from: private */
    public TextTrackCue mCue;
    /* access modifiers changed from: private */
    public Vector<String> mCueTexts;
    /* access modifiers changed from: private */
    public WebVttCueListener mListener;
    /* access modifiers changed from: private */
    public final Phase mParseCueId = new Phase() {
        static final /* synthetic */ boolean $assertionsDisabled = false;

        static {
            Class<WebVttParser> cls = WebVttParser.class;
        }

        public void parse(String line) {
            if (line.length() != 0) {
                if (line.equals("NOTE") || line.startsWith("NOTE ")) {
                    Phase unused = WebVttParser.this.mPhase = WebVttParser.this.mParseCueText;
                }
                TextTrackCue unused2 = WebVttParser.this.mCue = new TextTrackCue();
                WebVttParser.this.mCueTexts.clear();
                Phase unused3 = WebVttParser.this.mPhase = WebVttParser.this.mParseCueTime;
                if (line.contains("-->")) {
                    WebVttParser.this.mPhase.parse(line);
                } else {
                    WebVttParser.this.mCue.mId = line;
                }
            }
        }
    };
    /* access modifiers changed from: private */
    public final Phase mParseCueText = new Phase() {
        public void parse(String line) {
            if (line.length() == 0) {
                WebVttParser.this.yieldCue();
                Phase unused = WebVttParser.this.mPhase = WebVttParser.this.mParseCueId;
                return;
            }
            if (WebVttParser.this.mCue != null) {
                WebVttParser.this.mCueTexts.add(line);
            }
        }
    };
    /* access modifiers changed from: private */
    public final Phase mParseCueTime = new Phase() {
        static final /* synthetic */ boolean $assertionsDisabled = false;

        static {
            Class<WebVttParser> cls = WebVttParser.class;
        }

        public void parse(String line) {
            String str = line;
            int arrowAt = str.indexOf("-->");
            if (arrowAt < 0) {
                TextTrackCue unused = WebVttParser.this.mCue = null;
                Phase unused2 = WebVttParser.this.mPhase = WebVttParser.this.mParseCueId;
                return;
            }
            int i = 0;
            String start = str.substring(0, arrowAt).trim();
            String rest = str.substring(arrowAt + 3).replaceFirst("^\\s+", "").replaceFirst("\\s+", " ");
            int spaceAt = rest.indexOf(32);
            String end = spaceAt > 0 ? rest.substring(0, spaceAt) : rest;
            String rest2 = spaceAt > 0 ? rest.substring(spaceAt + 1) : "";
            WebVttParser.this.mCue.mStartTimeMs = WebVttParser.parseTimestampMs(start);
            WebVttParser.this.mCue.mEndTimeMs = WebVttParser.parseTimestampMs(end);
            String[] split = rest2.split(" +");
            int length = split.length;
            int i2 = 0;
            while (i2 < length) {
                String setting = split[i2];
                int colonAt = setting.indexOf(58);
                if (colonAt > 0 && colonAt != setting.length() - 1) {
                    String name = setting.substring(i, colonAt);
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
                            if (value.endsWith("%")) {
                                WebVttParser.this.mCue.mSnapToLines = false;
                                WebVttParser.this.mCue.mLinePosition = Integer.valueOf(WebVttParser.parseIntPercentage(value));
                            } else if (value.matches(".*[^0-9].*")) {
                                WebVttParser.this.log_warning("cue setting", name, "contains an invalid character", value);
                            } else {
                                WebVttParser.this.mCue.mSnapToLines = true;
                                WebVttParser.this.mCue.mLinePosition = Integer.valueOf(Integer.parseInt(value));
                            }
                        } catch (NumberFormatException e) {
                            NumberFormatException numberFormatException = e;
                            WebVttParser.this.log_warning("cue setting", name, "is not numeric or percentage", value);
                        }
                    } else if (name.equals("position")) {
                        try {
                            WebVttParser.this.mCue.mTextPosition = WebVttParser.parseIntPercentage(value);
                        } catch (NumberFormatException e2) {
                            NumberFormatException numberFormatException2 = e2;
                            WebVttParser.this.log_warning("cue setting", name, "is not numeric or percentage", value);
                        }
                    } else if (name.equals("size")) {
                        try {
                            WebVttParser.this.mCue.mSize = WebVttParser.parseIntPercentage(value);
                        } catch (NumberFormatException e3) {
                            NumberFormatException numberFormatException3 = e3;
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
                i2++;
                String str2 = line;
                i = 0;
            }
            if (!(WebVttParser.this.mCue.mLinePosition == null && WebVttParser.this.mCue.mSize == 100 && WebVttParser.this.mCue.mWritingDirection == 100)) {
                WebVttParser.this.mCue.mRegionId = "";
            }
            Phase unused3 = WebVttParser.this.mPhase = WebVttParser.this.mParseCueText;
        }
    };
    /* access modifiers changed from: private */
    public final Phase mParseHeader = new Phase() {
        static final /* synthetic */ boolean $assertionsDisabled = false;

        static {
            Class<WebVttParser> cls = WebVttParser.class;
        }

        /*  JADX ERROR: NullPointerException in pass: CodeShrinkVisitor
            java.lang.NullPointerException
            */
        android.media.TextTrackRegion parseRegion(java.lang.String r22) {
            /*
                r21 = this;
                r1 = r21
                android.media.TextTrackRegion r0 = new android.media.TextTrackRegion
                r0.<init>()
                r2 = r0
                java.lang.String r0 = " +"
                r3 = r22
                java.lang.String[] r4 = r3.split(r0)
                int r5 = r4.length
                r6 = 0
                r7 = r6
            L_0x0013:
                if (r7 >= r5) goto L_0x0162
                r8 = r4[r7]
                r0 = 61
                int r9 = r8.indexOf(r0)
                if (r9 <= 0) goto L_0x0158
                int r0 = r8.length()
                int r0 = r0 + -1
                if (r9 != r0) goto L_0x0029
                goto L_0x0158
            L_0x0029:
                java.lang.String r15 = r8.substring(r6, r9)
                int r0 = r9 + 1
                java.lang.String r14 = r8.substring(r0)
                java.lang.String r0 = "id"
                boolean r0 = r15.equals(r0)
                if (r0 == 0) goto L_0x003f
                r2.mId = r14
                goto L_0x0158
            L_0x003f:
                java.lang.String r0 = "width"
                boolean r0 = r15.equals(r0)
                if (r0 == 0) goto L_0x006c
                float r0 = android.media.WebVttParser.parseFloatPercentage(r14)     // Catch:{ NumberFormatException -> 0x0050 }
                r2.mWidth = r0     // Catch:{ NumberFormatException -> 0x0050 }
                goto L_0x0158
            L_0x0050:
                r0 = move-exception
                android.media.WebVttParser r10 = android.media.WebVttParser.this
                java.lang.String r11 = "region setting"
                java.lang.String r13 = "has invalid value"
                java.lang.String r16 = r0.getMessage()
                r12 = r15
                r17 = r14
                r14 = r16
                r6 = r15
                r15 = r17
                r10.log_warning(r11, r12, r13, r14, r15)
            L_0x0068:
                r18 = 0
                goto L_0x015a
            L_0x006c:
                r17 = r14
                r6 = r15
                java.lang.String r0 = "lines"
                boolean r0 = r6.equals(r0)
                if (r0 == 0) goto L_0x00a0
                java.lang.String r0 = ".*[^0-9].*"
                r15 = r17
                boolean r0 = r15.matches(r0)
                if (r0 == 0) goto L_0x008d
                android.media.WebVttParser r0 = android.media.WebVttParser.this
                java.lang.String r10 = "lines"
                java.lang.String r11 = "contains an invalid character"
                r0.log_warning(r10, r6, r11, r15)
                goto L_0x0068
            L_0x008d:
                int r0 = java.lang.Integer.parseInt(r15)     // Catch:{ NumberFormatException -> 0x0094 }
                r2.mLines = r0     // Catch:{ NumberFormatException -> 0x0094 }
                goto L_0x009f
            L_0x0094:
                r0 = move-exception
                android.media.WebVttParser r10 = android.media.WebVttParser.this
                java.lang.String r11 = "region setting"
                java.lang.String r12 = "is not numeric"
                r10.log_warning(r11, r6, r12, r15)
            L_0x009f:
                goto L_0x0068
            L_0x00a0:
                r15 = r17
                java.lang.String r0 = "regionanchor"
                boolean r0 = r6.equals(r0)
                if (r0 != 0) goto L_0x00d7
                java.lang.String r0 = "viewportanchor"
                boolean r0 = r6.equals(r0)
                if (r0 == 0) goto L_0x00b5
                goto L_0x00d7
            L_0x00b5:
                java.lang.String r0 = "scroll"
                boolean r0 = r6.equals(r0)
                if (r0 == 0) goto L_0x0068
                java.lang.String r0 = "up"
                boolean r0 = r15.equals(r0)
                if (r0 == 0) goto L_0x00cc
                r0 = 301(0x12d, float:4.22E-43)
                r2.mScrollValue = r0
                goto L_0x0068
            L_0x00cc:
                android.media.WebVttParser r0 = android.media.WebVttParser.this
                java.lang.String r10 = "region setting"
                java.lang.String r11 = "has invalid value"
                r0.log_warning(r10, r6, r11, r15)
                goto L_0x0068
            L_0x00d7:
                java.lang.String r0 = ","
                int r14 = r15.indexOf(r0)
                if (r14 >= 0) goto L_0x00eb
                android.media.WebVttParser r0 = android.media.WebVttParser.this
                java.lang.String r10 = "region setting"
                java.lang.String r11 = "contains no comma"
                r0.log_warning(r10, r6, r11, r15)
                goto L_0x0068
            L_0x00eb:
                r10 = 0
                java.lang.String r13 = r15.substring(r10, r14)
                int r0 = r14 + 1
                java.lang.String r0 = r15.substring(r0)
                r12 = r0
                float r0 = android.media.WebVttParser.parseFloatPercentage(r13)     // Catch:{ NumberFormatException -> 0x013b }
                r11 = r0
                float r0 = android.media.WebVttParser.parseFloatPercentage(r12)     // Catch:{ NumberFormatException -> 0x0118 }
                r10 = 0
                char r3 = r6.charAt(r10)
                r10 = 114(0x72, float:1.6E-43)
                if (r3 != r10) goto L_0x0112
                r2.mAnchorPointX = r11
                r2.mAnchorPointY = r0
                goto L_0x0116
            L_0x0112:
                r2.mViewportAnchorPointX = r11
                r2.mViewportAnchorPointY = r0
            L_0x0116:
                goto L_0x0068
            L_0x0118:
                r0 = move-exception
                r3 = r0
                android.media.WebVttParser r10 = android.media.WebVttParser.this
                java.lang.String r3 = "region setting"
                java.lang.String r16 = "has invalid y component"
                java.lang.String r17 = r0.getMessage()
                r18 = 0
                r19 = r11
                r11 = r3
                r3 = r12
                r12 = r6
                r20 = r13
                r13 = r16
                r16 = r14
                r14 = r17
                r17 = r15
                r15 = r3
                r10.log_warning(r11, r12, r13, r14, r15)
                goto L_0x015a
            L_0x013b:
                r0 = move-exception
                r3 = r12
                r20 = r13
                r16 = r14
                r17 = r15
                r18 = 0
                r10 = r0
                android.media.WebVttParser r10 = android.media.WebVttParser.this
                java.lang.String r11 = "region setting"
                java.lang.String r13 = "has invalid x component"
                java.lang.String r14 = r0.getMessage()
                r12 = r6
                r15 = r20
                r10.log_warning(r11, r12, r13, r14, r15)
                goto L_0x015a
            L_0x0158:
                r18 = r6
            L_0x015a:
                int r7 = r7 + 1
                r6 = r18
                r3 = r22
                goto L_0x0013
            L_0x0162:
                return r2
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.WebVttParser.AnonymousClass3.parseRegion(java.lang.String):android.media.TextTrackRegion");
        }

        public void parse(String line) {
            if (line.length() == 0) {
                Phase unused = WebVttParser.this.mPhase = WebVttParser.this.mParseCueId;
            } else if (line.contains("-->")) {
                Phase unused2 = WebVttParser.this.mPhase = WebVttParser.this.mParseCueTime;
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
            if (line.equals("WEBVTT") || line.startsWith("WEBVTT ") || line.startsWith("WEBVTT\t")) {
                Phase unused = WebVttParser.this.mPhase = WebVttParser.this.mParseHeader;
                return;
            }
            WebVttParser.this.log_warning("Not a WEBVTT header", line);
            Phase unused2 = WebVttParser.this.mPhase = WebVttParser.this.mSkipRest;
        }
    };
    /* access modifiers changed from: private */
    public Phase mPhase = this.mParseStart;
    /* access modifiers changed from: private */
    public final Phase mSkipRest = new Phase() {
        public void parse(String line) {
        }
    };

    /* compiled from: WebVttRenderer */
    interface Phase {
        void parse(String str);
    }

    WebVttParser(WebVttCueListener listener) {
        this.mListener = listener;
        this.mCueTexts = new Vector<>();
    }

    public static float parseFloatPercentage(String s) throws NumberFormatException {
        if (s.endsWith("%")) {
            String s2 = s.substring(0, s.length() - 1);
            if (!s2.matches(".*[^0-9.].*")) {
                try {
                    float value = Float.parseFloat(s2);
                    if (value >= 0.0f && value <= 100.0f) {
                        return value;
                    }
                    throw new NumberFormatException("is out of range");
                } catch (NumberFormatException e) {
                    throw new NumberFormatException("is not a number");
                }
            } else {
                throw new NumberFormatException("contains an invalid character");
            }
        } else {
            throw new NumberFormatException("does not end in %");
        }
    }

    public static int parseIntPercentage(String s) throws NumberFormatException {
        if (s.endsWith("%")) {
            String s2 = s.substring(0, s.length() - 1);
            if (!s2.matches(".*[^0-9].*")) {
                try {
                    int value = Integer.parseInt(s2);
                    if (value >= 0 && value <= 100) {
                        return value;
                    }
                    throw new NumberFormatException("is out of range");
                } catch (NumberFormatException e) {
                    throw new NumberFormatException("is not a number");
                }
            } else {
                throw new NumberFormatException("contains an invalid character");
            }
        } else {
            throw new NumberFormatException("does not end in %");
        }
    }

    public static long parseTimestampMs(String s) throws NumberFormatException {
        if (s.matches("(\\d+:)?[0-5]\\d:[0-5]\\d\\.\\d{3}")) {
            String[] parts = s.split("\\.", 2);
            long value = 0;
            for (String group : parts[0].split(":")) {
                value = (60 * value) + Long.parseLong(group);
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
        this.mBuffer = "";
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

    /* access modifiers changed from: private */
    public void log_warning(String nameType, String name, String message, String subMessage, String value) {
        String name2 = getClass().getName();
        Log.w(name2, nameType + " '" + name + "' " + message + " ('" + value + "' " + subMessage + ")");
    }

    /* access modifiers changed from: private */
    public void log_warning(String nameType, String name, String message, String value) {
        String name2 = getClass().getName();
        Log.w(name2, nameType + " '" + name + "' " + message + " ('" + value + "')");
    }

    /* access modifiers changed from: private */
    public void log_warning(String message, String value) {
        String name = getClass().getName();
        Log.w(name, message + " ('" + value + "')");
    }
}
