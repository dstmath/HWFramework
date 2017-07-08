package android.media;

import android.app.Instrumentation;
import android.bluetooth.BluetoothAssignedNumbers;
import android.content.pm.PackageManager;
import android.hardware.SensorManager;
import android.location.LocationRequest;
import android.net.ProxyInfo;
import android.net.wifi.WifiEnterpriseConfig;
import android.nfc.tech.Ndef;
import android.provider.BrowserContract.Bookmarks;
import android.provider.CalendarContract.Instances;
import android.provider.MediaStore.Video.Thumbnails;
import android.rms.HwSysResource;
import android.speech.tts.Voice;
import android.util.Log;
import java.util.Vector;

/* compiled from: WebVttRenderer */
class WebVttParser {
    private static final String TAG = "WebVttParser";
    private String mBuffer;
    private TextTrackCue mCue;
    private Vector<String> mCueTexts;
    private WebVttCueListener mListener;
    private final Phase mParseCueId;
    private final Phase mParseCueText;
    private final Phase mParseCueTime;
    private final Phase mParseHeader;
    private final Phase mParseStart;
    private Phase mPhase;
    private final Phase mSkipRest;

    /* compiled from: WebVttRenderer */
    interface Phase {
        void parse(String str);
    }

    /* compiled from: WebVttRenderer */
    /* renamed from: android.media.WebVttParser.3 */
    class AnonymousClass3 implements Phase {
        static final /* synthetic */ boolean -assertionsDisabled = false;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.media.WebVttParser.3.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.media.WebVttParser.3.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.WebVttParser.3.<clinit>():void");
        }

        AnonymousClass3() {
        }

        TextTrackRegion parseRegion(String s) {
            TextTrackRegion region = new TextTrackRegion();
            for (String setting : s.split(" +")) {
                int equalAt = setting.indexOf(61);
                if (equalAt > 0 && equalAt != setting.length() - 1) {
                    String name = setting.substring(0, equalAt);
                    String value = setting.substring(equalAt + 1);
                    if (name.equals(Instrumentation.REPORT_KEY_IDENTIFIER)) {
                        region.mId = value;
                    } else if (name.equals(Thumbnails.WIDTH)) {
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
                                if (-assertionsDisabled) {
                                    continue;
                                } else {
                                    if ((region.mLines >= 0 ? 1 : null) == null) {
                                        throw new AssertionError();
                                    }
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
            }
            return region;
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
    }

    /* compiled from: WebVttRenderer */
    /* renamed from: android.media.WebVttParser.4 */
    class AnonymousClass4 implements Phase {
        static final /* synthetic */ boolean -assertionsDisabled = false;
        final /* synthetic */ WebVttParser this$0;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.media.WebVttParser.4.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.media.WebVttParser.4.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.WebVttParser.4.<clinit>():void");
        }

        AnonymousClass4(WebVttParser this$0) {
            this.this$0 = this$0;
        }

        public void parse(String line) {
            Object obj = null;
            if (line.length() != 0) {
                if (!-assertionsDisabled) {
                    if (this.this$0.mCue == null) {
                        obj = 1;
                    }
                    if (obj == null) {
                        throw new AssertionError();
                    }
                }
                if (line.equals("NOTE") || line.startsWith("NOTE ")) {
                    this.this$0.mPhase = this.this$0.mParseCueText;
                }
                this.this$0.mCue = new TextTrackCue();
                this.this$0.mCueTexts.clear();
                this.this$0.mPhase = this.this$0.mParseCueTime;
                if (line.contains("-->")) {
                    this.this$0.mPhase.parse(line);
                } else {
                    this.this$0.mCue.mId = line;
                }
            }
        }
    }

    /* compiled from: WebVttRenderer */
    /* renamed from: android.media.WebVttParser.5 */
    class AnonymousClass5 implements Phase {
        static final /* synthetic */ boolean -assertionsDisabled = false;
        final /* synthetic */ WebVttParser this$0;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.media.WebVttParser.5.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.media.WebVttParser.5.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.WebVttParser.5.<clinit>():void");
        }

        AnonymousClass5(WebVttParser this$0) {
            this.this$0 = this$0;
        }

        public void parse(String line) {
            int arrowAt = line.indexOf("-->");
            if (arrowAt < 0) {
                this.this$0.mCue = null;
                this.this$0.mPhase = this.this$0.mParseCueId;
                return;
            }
            String start = line.substring(0, arrowAt).trim();
            String rest = line.substring(arrowAt + 3).replaceFirst("^\\s+", ProxyInfo.LOCAL_EXCL_LIST).replaceFirst("\\s+", WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
            int spaceAt = rest.indexOf(32);
            String end = spaceAt > 0 ? rest.substring(0, spaceAt) : rest;
            rest = spaceAt > 0 ? rest.substring(spaceAt + 1) : ProxyInfo.LOCAL_EXCL_LIST;
            this.this$0.mCue.mStartTimeMs = WebVttParser.parseTimestampMs(start);
            this.this$0.mCue.mEndTimeMs = WebVttParser.parseTimestampMs(end);
            for (String setting : rest.split(" +")) {
                int colonAt = setting.indexOf(58);
                if (colonAt > 0 && colonAt != setting.length() - 1) {
                    String name = setting.substring(0, colonAt);
                    String value = setting.substring(colonAt + 1);
                    if (name.equals(TtmlUtils.TAG_REGION)) {
                        this.this$0.mCue.mRegionId = value;
                    } else if (name.equals("vertical")) {
                        if (value.equals("rl")) {
                            this.this$0.mCue.mWritingDirection = HwSysResource.MAINSERVICES;
                        } else if (value.equals("lr")) {
                            this.this$0.mCue.mWritingDirection = Ndef.TYPE_ICODE_SLI;
                        } else {
                            this.this$0.log_warning("cue setting", name, "has invalid value", value);
                        }
                    } else if (name.equals(AudioSystem.DEVICE_OUT_LINE_NAME)) {
                        try {
                            if (!-assertionsDisabled) {
                                Object obj;
                                if (value.indexOf(32) < 0) {
                                    obj = 1;
                                } else {
                                    obj = null;
                                }
                                if (obj == null) {
                                    throw new AssertionError();
                                }
                            }
                            if (value.endsWith("%")) {
                                this.this$0.mCue.mSnapToLines = false;
                                this.this$0.mCue.mLinePosition = Integer.valueOf(WebVttParser.parseIntPercentage(value));
                            } else if (value.matches(".*[^0-9].*")) {
                                this.this$0.log_warning("cue setting", name, "contains an invalid character", value);
                            } else {
                                this.this$0.mCue.mSnapToLines = true;
                                this.this$0.mCue.mLinePosition = Integer.valueOf(Integer.parseInt(value));
                            }
                        } catch (NumberFormatException e) {
                            this.this$0.log_warning("cue setting", name, "is not numeric or percentage", value);
                        }
                    } else if (name.equals(Bookmarks.POSITION)) {
                        try {
                            this.this$0.mCue.mTextPosition = WebVttParser.parseIntPercentage(value);
                        } catch (NumberFormatException e2) {
                            this.this$0.log_warning("cue setting", name, "is not numeric or percentage", value);
                        }
                    } else if (name.equals("size")) {
                        try {
                            this.this$0.mCue.mSize = WebVttParser.parseIntPercentage(value);
                        } catch (NumberFormatException e3) {
                            this.this$0.log_warning("cue setting", name, "is not numeric or percentage", value);
                        }
                    } else if (name.equals("align")) {
                        if (value.equals("start")) {
                            this.this$0.mCue.mAlignment = LocationRequest.POWER_LOW;
                        } else if (value.equals("middle")) {
                            this.this$0.mCue.mAlignment = Voice.QUALITY_LOW;
                        } else if (value.equals(Instances.END)) {
                            this.this$0.mCue.mAlignment = BluetoothAssignedNumbers.MC10;
                        } else if (value.equals("left")) {
                            this.this$0.mCue.mAlignment = LocationRequest.POWER_HIGH;
                        } else if (value.equals("right")) {
                            this.this$0.mCue.mAlignment = AudioFormat.CHANNEL_OUT_QUAD;
                        } else {
                            this.this$0.log_warning("cue setting", name, "has invalid value", value);
                        }
                    }
                }
            }
            if (this.this$0.mCue.mLinePosition == null && this.this$0.mCue.mSize == 100) {
                if (this.this$0.mCue.mWritingDirection != 100) {
                }
                this.this$0.mPhase = this.this$0.mParseCueText;
            }
            this.this$0.mCue.mRegionId = ProxyInfo.LOCAL_EXCL_LIST;
            this.this$0.mPhase = this.this$0.mParseCueText;
        }
    }

    /* compiled from: WebVttRenderer */
    /* renamed from: android.media.WebVttParser.6 */
    class AnonymousClass6 implements Phase {
        final /* synthetic */ WebVttParser this$0;

        AnonymousClass6(WebVttParser this$0) {
            this.this$0 = this$0;
        }

        public void parse(String line) {
            if (line.length() == 0) {
                this.this$0.yieldCue();
                this.this$0.mPhase = this.this$0.mParseCueId;
                return;
            }
            if (this.this$0.mCue != null) {
                this.this$0.mCueTexts.add(line);
            }
        }
    }

    WebVttParser(WebVttCueListener listener) {
        this.mSkipRest = new Phase() {
            public void parse(String line) {
            }
        };
        this.mParseStart = new Phase() {
            public void parse(String line) {
                if (line.startsWith("\ufeff")) {
                    line = line.substring(1);
                }
                if (line.equals("WEBVTT") || line.startsWith("WEBVTT ") || line.startsWith("WEBVTT\t")) {
                    WebVttParser.this.mPhase = WebVttParser.this.mParseHeader;
                    return;
                }
                WebVttParser.this.log_warning("Not a WEBVTT header", line);
                WebVttParser.this.mPhase = WebVttParser.this.mSkipRest;
            }
        };
        this.mParseHeader = new AnonymousClass3();
        this.mParseCueId = new AnonymousClass4(this);
        this.mParseCueTime = new AnonymousClass5(this);
        this.mParseCueText = new AnonymousClass6(this);
        this.mPhase = this.mParseStart;
        this.mBuffer = ProxyInfo.LOCAL_EXCL_LIST;
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
                if (value >= 0.0f && value <= SensorManager.LIGHT_CLOUDY) {
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
        return String.format("%d:%02d:%02d.%03d", new Object[]{Long.valueOf(timeMs / PackageManager.MAXIMUM_VERIFICATION_TIMEOUT), Long.valueOf((timeMs / 60000) % 60), Long.valueOf((timeMs / 1000) % 60), Long.valueOf(timeMs % 1000)});
    }

    public void parse(String s) {
        boolean trailingCR = false;
        this.mBuffer = (this.mBuffer + s.replace("\u0000", "\ufffd")).replace("\r\n", "\n");
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
