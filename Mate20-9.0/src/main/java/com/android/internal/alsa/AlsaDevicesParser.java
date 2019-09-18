package com.android.internal.alsa;

import android.util.Slog;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

public class AlsaDevicesParser {
    protected static final boolean DEBUG = false;
    public static final int SCANSTATUS_EMPTY = 2;
    public static final int SCANSTATUS_FAIL = 1;
    public static final int SCANSTATUS_NOTSCANNED = -1;
    public static final int SCANSTATUS_SUCCESS = 0;
    private static final String TAG = "AlsaDevicesParser";
    private static final String kDevicesFilePath = "/proc/asound/devices";
    private static final int kEndIndex_CardNum = 8;
    private static final int kEndIndex_DeviceNum = 11;
    private static final int kIndex_CardDeviceField = 5;
    private static final int kStartIndex_CardNum = 6;
    private static final int kStartIndex_DeviceNum = 9;
    private static final int kStartIndex_Type = 14;
    /* access modifiers changed from: private */
    public static LineTokenizer mTokenizer = new LineTokenizer(" :[]-");
    private final ArrayList<AlsaDeviceRecord> mDeviceRecords = new ArrayList<>();
    /* access modifiers changed from: private */
    public boolean mHasCaptureDevices = false;
    /* access modifiers changed from: private */
    public boolean mHasMIDIDevices = false;
    /* access modifiers changed from: private */
    public boolean mHasPlaybackDevices = false;
    private int mScanStatus = -1;

    public class AlsaDeviceRecord {
        public static final int kDeviceDir_Capture = 0;
        public static final int kDeviceDir_Playback = 1;
        public static final int kDeviceDir_Unknown = -1;
        public static final int kDeviceType_Audio = 0;
        public static final int kDeviceType_Control = 1;
        public static final int kDeviceType_MIDI = 2;
        public static final int kDeviceType_Unknown = -1;
        int mCardNum = -1;
        int mDeviceDir = -1;
        int mDeviceNum = -1;
        int mDeviceType = -1;

        public AlsaDeviceRecord() {
        }

        public boolean parse(String line) {
            String str = line;
            int delimOffset = 0;
            int tokenOffset = 0;
            while (true) {
                int tokenIndex = tokenOffset;
                int tokenOffset2 = AlsaDevicesParser.mTokenizer.nextToken(str, delimOffset);
                if (tokenOffset2 == -1) {
                    return true;
                }
                delimOffset = AlsaDevicesParser.mTokenizer.nextDelimiter(str, tokenOffset2);
                if (delimOffset == -1) {
                    delimOffset = line.length();
                }
                String token = str.substring(tokenOffset2, delimOffset);
                switch (tokenIndex) {
                    case 1:
                        this.mCardNum = Integer.parseInt(token);
                        if (str.charAt(delimOffset) == '-') {
                            break;
                        } else {
                            tokenIndex++;
                            break;
                        }
                    case 2:
                        this.mDeviceNum = Integer.parseInt(token);
                        break;
                    case 3:
                        if (!token.equals("digital")) {
                            if (!token.equals("control")) {
                                boolean equals = token.equals("raw");
                                break;
                            } else {
                                this.mDeviceType = 1;
                                break;
                            }
                        } else {
                            break;
                        }
                    case 4:
                        if (!token.equals("audio")) {
                            if (!token.equals("midi")) {
                                break;
                            } else {
                                this.mDeviceType = 2;
                                boolean unused = AlsaDevicesParser.this.mHasMIDIDevices = true;
                                break;
                            }
                        } else {
                            this.mDeviceType = 0;
                            break;
                        }
                    case 5:
                        try {
                            if (!token.equals("capture")) {
                                if (!token.equals("playback")) {
                                    break;
                                } else {
                                    this.mDeviceDir = 1;
                                    boolean unused2 = AlsaDevicesParser.this.mHasPlaybackDevices = true;
                                    break;
                                }
                            } else {
                                this.mDeviceDir = 0;
                                boolean unused3 = AlsaDevicesParser.this.mHasCaptureDevices = true;
                                break;
                            }
                        } catch (NumberFormatException e) {
                            Slog.e(AlsaDevicesParser.TAG, "Failed to parse token " + tokenIndex + " of " + AlsaDevicesParser.kDevicesFilePath + " token: " + token);
                            return false;
                        }
                }
                tokenOffset = tokenIndex + 1;
            }
        }

        public String textFormat() {
            StringBuilder sb = new StringBuilder();
            sb.append("[" + this.mCardNum + ":" + this.mDeviceNum + "]");
            switch (this.mDeviceType) {
                case 0:
                    sb.append(" Audio");
                    break;
                case 1:
                    sb.append(" Control");
                    break;
                case 2:
                    sb.append(" MIDI");
                    break;
                default:
                    sb.append(" N/A");
                    break;
            }
            switch (this.mDeviceDir) {
                case 0:
                    sb.append(" Capture");
                    break;
                case 1:
                    sb.append(" Playback");
                    break;
                default:
                    sb.append(" N/A");
                    break;
            }
            return sb.toString();
        }
    }

    public int getDefaultDeviceNum(int card) {
        return 0;
    }

    public boolean hasPlaybackDevices(int card) {
        Iterator<AlsaDeviceRecord> it = this.mDeviceRecords.iterator();
        while (it.hasNext()) {
            AlsaDeviceRecord deviceRecord = it.next();
            if (deviceRecord.mCardNum == card && deviceRecord.mDeviceType == 0 && deviceRecord.mDeviceDir == 1) {
                return true;
            }
        }
        return false;
    }

    public boolean hasCaptureDevices(int card) {
        Iterator<AlsaDeviceRecord> it = this.mDeviceRecords.iterator();
        while (it.hasNext()) {
            AlsaDeviceRecord deviceRecord = it.next();
            if (deviceRecord.mCardNum == card && deviceRecord.mDeviceType == 0 && deviceRecord.mDeviceDir == 0) {
                return true;
            }
        }
        return false;
    }

    public boolean hasMIDIDevices(int card) {
        Iterator<AlsaDeviceRecord> it = this.mDeviceRecords.iterator();
        while (it.hasNext()) {
            AlsaDeviceRecord deviceRecord = it.next();
            if (deviceRecord.mCardNum == card && deviceRecord.mDeviceType == 2) {
                return true;
            }
        }
        return false;
    }

    private boolean isLineDeviceRecord(String line) {
        return line.charAt(5) == '[';
    }

    public int scan() {
        this.mDeviceRecords.clear();
        try {
            FileReader reader = new FileReader(new File(kDevicesFilePath));
            BufferedReader bufferedReader = new BufferedReader(reader);
            while (true) {
                String readLine = bufferedReader.readLine();
                String line = readLine;
                if (readLine == null) {
                    break;
                } else if (isLineDeviceRecord(line)) {
                    AlsaDeviceRecord deviceRecord = new AlsaDeviceRecord();
                    deviceRecord.parse(line);
                    Slog.i(TAG, deviceRecord.textFormat());
                    this.mDeviceRecords.add(deviceRecord);
                }
            }
            reader.close();
            if (this.mDeviceRecords.size() > 0) {
                this.mScanStatus = 0;
            } else {
                this.mScanStatus = 2;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            this.mScanStatus = 1;
        } catch (IOException e2) {
            e2.printStackTrace();
            this.mScanStatus = 1;
        }
        return this.mScanStatus;
    }

    public int getScanStatus() {
        return this.mScanStatus;
    }

    private void Log(String heading) {
    }
}
