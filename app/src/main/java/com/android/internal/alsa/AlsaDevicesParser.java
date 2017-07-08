package com.android.internal.alsa;

import android.util.Slog;
import huawei.cust.HwCfgFilePolicy;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class AlsaDevicesParser {
    protected static final boolean DEBUG = false;
    private static final String TAG = "AlsaDevicesParser";
    private static final String kDevicesFilePath = "/proc/asound/devices";
    private static final int kEndIndex_CardNum = 8;
    private static final int kEndIndex_DeviceNum = 11;
    private static final int kIndex_CardDeviceField = 5;
    private static final int kStartIndex_CardNum = 6;
    private static final int kStartIndex_DeviceNum = 9;
    private static final int kStartIndex_Type = 14;
    private static LineTokenizer mTokenizer;
    private ArrayList<AlsaDeviceRecord> mDeviceRecords;
    private boolean mHasCaptureDevices;
    private boolean mHasMIDIDevices;
    private boolean mHasPlaybackDevices;

    public class AlsaDeviceRecord {
        public static final int kDeviceDir_Capture = 0;
        public static final int kDeviceDir_Playback = 1;
        public static final int kDeviceDir_Unknown = -1;
        public static final int kDeviceType_Audio = 0;
        public static final int kDeviceType_Control = 1;
        public static final int kDeviceType_MIDI = 2;
        public static final int kDeviceType_Unknown = -1;
        int mCardNum;
        int mDeviceDir;
        int mDeviceNum;
        int mDeviceType;

        public AlsaDeviceRecord() {
            this.mCardNum = kDeviceType_Unknown;
            this.mDeviceNum = kDeviceType_Unknown;
            this.mDeviceType = kDeviceType_Unknown;
            this.mDeviceDir = kDeviceType_Unknown;
        }

        public boolean parse(String line) {
            int delimOffset = kDeviceType_Audio;
            int tokenIndex = kDeviceType_Audio;
            while (true) {
                int tokenOffset = AlsaDevicesParser.mTokenizer.nextToken(line, delimOffset);
                if (tokenOffset == kDeviceType_Unknown) {
                    return true;
                }
                delimOffset = AlsaDevicesParser.mTokenizer.nextDelimiter(line, tokenOffset);
                if (delimOffset == kDeviceType_Unknown) {
                    delimOffset = line.length();
                }
                String token = line.substring(tokenOffset, delimOffset);
                switch (tokenIndex) {
                    case kDeviceType_Control /*1*/:
                        this.mCardNum = Integer.parseInt(token);
                        if (line.charAt(delimOffset) == '-') {
                            break;
                        }
                        tokenIndex += kDeviceType_Control;
                        break;
                    case kDeviceType_MIDI /*2*/:
                        this.mDeviceNum = Integer.parseInt(token);
                        break;
                    case HwCfgFilePolicy.BASE /*3*/:
                        try {
                            if (!token.equals("digital")) {
                                if (!token.equals("control")) {
                                    if (!token.equals("raw")) {
                                        break;
                                    }
                                    break;
                                }
                                this.mDeviceType = kDeviceType_Control;
                                break;
                            }
                            break;
                        } catch (NumberFormatException e) {
                            Slog.e(AlsaDevicesParser.TAG, "Failed to parse token " + tokenIndex + " of " + AlsaDevicesParser.kDevicesFilePath + " token: " + token);
                            return AlsaDevicesParser.DEBUG;
                        }
                    case HwCfgFilePolicy.CUST /*4*/:
                        if (!token.equals("audio")) {
                            if (!token.equals("midi")) {
                                break;
                            }
                            this.mDeviceType = kDeviceType_MIDI;
                            AlsaDevicesParser.this.mHasMIDIDevices = true;
                            break;
                        }
                        this.mDeviceType = kDeviceType_Audio;
                        break;
                    case AlsaDevicesParser.kIndex_CardDeviceField /*5*/:
                        if (!token.equals("capture")) {
                            if (!token.equals("playback")) {
                                break;
                            }
                            this.mDeviceDir = kDeviceType_Control;
                            AlsaDevicesParser.this.mHasPlaybackDevices = true;
                            break;
                        }
                        this.mDeviceDir = kDeviceType_Audio;
                        AlsaDevicesParser.this.mHasCaptureDevices = true;
                        break;
                    default:
                        break;
                }
                tokenIndex += kDeviceType_Control;
            }
        }

        public String textFormat() {
            StringBuilder sb = new StringBuilder();
            sb.append("[").append(this.mCardNum).append(":").append(this.mDeviceNum).append("]");
            switch (this.mDeviceType) {
                case kDeviceType_Unknown /*-1*/:
                    sb.append(" N/A");
                    break;
                case kDeviceType_Audio /*0*/:
                    sb.append(" Audio");
                    break;
                case kDeviceType_Control /*1*/:
                    sb.append(" Control");
                    break;
                case kDeviceType_MIDI /*2*/:
                    sb.append(" MIDI");
                    break;
            }
            switch (this.mDeviceDir) {
                case kDeviceType_Unknown /*-1*/:
                    sb.append(" N/A");
                    break;
                case kDeviceType_Audio /*0*/:
                    sb.append(" Capture");
                    break;
                case kDeviceType_Control /*1*/:
                    sb.append(" Playback");
                    break;
            }
            return sb.toString();
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.alsa.AlsaDevicesParser.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.alsa.AlsaDevicesParser.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.alsa.AlsaDevicesParser.<clinit>():void");
    }

    public AlsaDevicesParser() {
        this.mHasCaptureDevices = DEBUG;
        this.mHasPlaybackDevices = DEBUG;
        this.mHasMIDIDevices = DEBUG;
        this.mDeviceRecords = new ArrayList();
    }

    public int getDefaultDeviceNum(int card) {
        return 0;
    }

    public boolean hasPlaybackDevices() {
        return this.mHasPlaybackDevices;
    }

    public boolean hasPlaybackDevices(int card) {
        for (AlsaDeviceRecord deviceRecord : this.mDeviceRecords) {
            if (deviceRecord.mCardNum == card && deviceRecord.mDeviceType == 0 && deviceRecord.mDeviceDir == 1) {
                return true;
            }
        }
        return DEBUG;
    }

    public boolean hasCaptureDevices() {
        return this.mHasCaptureDevices;
    }

    public boolean hasCaptureDevices(int card) {
        for (AlsaDeviceRecord deviceRecord : this.mDeviceRecords) {
            if (deviceRecord.mCardNum == card && deviceRecord.mDeviceType == 0 && deviceRecord.mDeviceDir == 0) {
                return true;
            }
        }
        return DEBUG;
    }

    public boolean hasMIDIDevices() {
        return this.mHasMIDIDevices;
    }

    public boolean hasMIDIDevices(int card) {
        for (AlsaDeviceRecord deviceRecord : this.mDeviceRecords) {
            if (deviceRecord.mCardNum == card && deviceRecord.mDeviceType == 2) {
                return true;
            }
        }
        return DEBUG;
    }

    private boolean isLineDeviceRecord(String line) {
        return line.charAt(kIndex_CardDeviceField) == '[' ? true : DEBUG;
    }

    public void scan() {
        this.mDeviceRecords.clear();
        try {
            FileReader reader = new FileReader(new File(kDevicesFilePath));
            BufferedReader bufferedReader = new BufferedReader(reader);
            String str = "";
            while (true) {
                str = bufferedReader.readLine();
                if (str == null) {
                    reader.close();
                    return;
                } else if (isLineDeviceRecord(str)) {
                    AlsaDeviceRecord deviceRecord = new AlsaDeviceRecord();
                    deviceRecord.parse(str);
                    this.mDeviceRecords.add(deviceRecord);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e2) {
            e2.printStackTrace();
        }
    }

    public void Log(String heading) {
    }
}
