package com.android.internal.alsa;

import android.provider.Downloads.Impl;
import android.provider.SettingsStringUtil;
import android.rms.iaware.AwareConstant.Database.AwareTables;
import android.util.LogException;
import android.util.Slog;
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
    private static LineTokenizer mTokenizer = new LineTokenizer(" :[]-");
    private final ArrayList<AlsaDeviceRecord> mDeviceRecords = new ArrayList();
    private boolean mHasCaptureDevices = false;
    private boolean mHasMIDIDevices = false;
    private boolean mHasPlaybackDevices = false;

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

        public boolean parse(String line) {
            int delimOffset = 0;
            int tokenIndex = 0;
            while (true) {
                int tokenOffset = AlsaDevicesParser.mTokenizer.nextToken(line, delimOffset);
                if (tokenOffset == -1) {
                    return true;
                }
                delimOffset = AlsaDevicesParser.mTokenizer.nextDelimiter(line, tokenOffset);
                if (delimOffset == -1) {
                    delimOffset = line.length();
                }
                String token = line.substring(tokenOffset, delimOffset);
                switch (tokenIndex) {
                    case 1:
                        try {
                            this.mCardNum = Integer.parseInt(token);
                            if (line.charAt(delimOffset) == '-') {
                                break;
                            }
                            tokenIndex++;
                            break;
                        } catch (NumberFormatException e) {
                            Slog.e(AlsaDevicesParser.TAG, "Failed to parse token " + tokenIndex + " of " + AlsaDevicesParser.kDevicesFilePath + " token: " + token);
                            return false;
                        }
                    case 2:
                        this.mDeviceNum = Integer.parseInt(token);
                        break;
                    case 3:
                        if (!token.equals("digital")) {
                            if (!token.equals(Impl.COLUMN_CONTROL)) {
                                boolean equals = token.equals("raw");
                                break;
                            }
                            this.mDeviceType = 1;
                            break;
                        }
                        break;
                    case 4:
                        if (!token.equals("audio")) {
                            if (!token.equals("midi")) {
                                break;
                            }
                            this.mDeviceType = 2;
                            AlsaDevicesParser.this.mHasMIDIDevices = true;
                            break;
                        }
                        this.mDeviceType = 0;
                        break;
                    case 5:
                        if (!token.equals(AwareTables.CAPTURE_TABLE)) {
                            if (!token.equals("playback")) {
                                break;
                            }
                            this.mDeviceDir = 1;
                            AlsaDevicesParser.this.mHasPlaybackDevices = true;
                            break;
                        }
                        this.mDeviceDir = 0;
                        AlsaDevicesParser.this.mHasCaptureDevices = true;
                        break;
                    default:
                        break;
                }
                tokenIndex++;
            }
        }

        public String textFormat() {
            StringBuilder sb = new StringBuilder();
            sb.append("[").append(this.mCardNum).append(SettingsStringUtil.DELIMITER).append(this.mDeviceNum).append("]");
            switch (this.mDeviceType) {
                case -1:
                    sb.append(" N/A");
                    break;
                case 0:
                    sb.append(" Audio");
                    break;
                case 1:
                    sb.append(" Control");
                    break;
                case 2:
                    sb.append(" MIDI");
                    break;
            }
            switch (this.mDeviceDir) {
                case -1:
                    sb.append(" N/A");
                    break;
                case 0:
                    sb.append(" Capture");
                    break;
                case 1:
                    sb.append(" Playback");
                    break;
            }
            return sb.toString();
        }
    }

    public int getDefaultDeviceNum(int card) {
        return 0;
    }

    public boolean hasPlaybackDevices(int card) {
        for (AlsaDeviceRecord deviceRecord : this.mDeviceRecords) {
            if (deviceRecord.mCardNum == card && deviceRecord.mDeviceType == 0 && deviceRecord.mDeviceDir == 1) {
                return true;
            }
        }
        return false;
    }

    public boolean hasCaptureDevices(int card) {
        for (AlsaDeviceRecord deviceRecord : this.mDeviceRecords) {
            if (deviceRecord.mCardNum == card && deviceRecord.mDeviceType == 0 && deviceRecord.mDeviceDir == 0) {
                return true;
            }
        }
        return false;
    }

    public boolean hasMIDIDevices(int card) {
        for (AlsaDeviceRecord deviceRecord : this.mDeviceRecords) {
            if (deviceRecord.mCardNum == card && deviceRecord.mDeviceType == 2) {
                return true;
            }
        }
        return false;
    }

    private boolean isLineDeviceRecord(String line) {
        return line.charAt(5) == '[';
    }

    public void scan() {
        this.mDeviceRecords.clear();
        try {
            FileReader reader = new FileReader(new File(kDevicesFilePath));
            BufferedReader bufferedReader = new BufferedReader(reader);
            String str = LogException.NO_VALUE;
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
