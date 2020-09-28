package com.android.internal.alsa;

import android.provider.Downloads;
import android.provider.SettingsStringUtil;
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
    private static LineTokenizer mTokenizer = new LineTokenizer(" :[]-");
    private final ArrayList<AlsaDeviceRecord> mDeviceRecords = new ArrayList<>();
    private boolean mHasCaptureDevices = false;
    private boolean mHasMIDIDevices = false;
    private boolean mHasPlaybackDevices = false;
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
                if (tokenIndex != 0) {
                    if (tokenIndex == 1) {
                        this.mCardNum = Integer.parseInt(token);
                        if (line.charAt(delimOffset) != '-') {
                            tokenIndex++;
                        }
                    } else if (tokenIndex == 2) {
                        this.mDeviceNum = Integer.parseInt(token);
                    } else if (tokenIndex != 3) {
                        if (tokenIndex != 4) {
                            if (tokenIndex == 5) {
                                try {
                                    if (token.equals("capture")) {
                                        this.mDeviceDir = 0;
                                        AlsaDevicesParser.this.mHasCaptureDevices = true;
                                    } else if (token.equals("playback")) {
                                        this.mDeviceDir = 1;
                                        AlsaDevicesParser.this.mHasPlaybackDevices = true;
                                    }
                                } catch (NumberFormatException e) {
                                    Slog.e(AlsaDevicesParser.TAG, "Failed to parse token " + tokenIndex + " of " + AlsaDevicesParser.kDevicesFilePath + " token: " + token);
                                    return false;
                                }
                            }
                        } else if (token.equals("audio")) {
                            this.mDeviceType = 0;
                        } else if (token.equals("midi")) {
                            this.mDeviceType = 2;
                            AlsaDevicesParser.this.mHasMIDIDevices = true;
                        }
                    } else if (!token.equals("digital")) {
                        if (token.equals(Downloads.Impl.COLUMN_CONTROL)) {
                            this.mDeviceType = 1;
                        } else {
                            token.equals("raw");
                        }
                    }
                }
                tokenIndex++;
            }
        }

        public String textFormat() {
            StringBuilder sb = new StringBuilder();
            sb.append("[" + this.mCardNum + SettingsStringUtil.DELIMITER + this.mDeviceNum + "]");
            int i = this.mDeviceType;
            if (i == 0) {
                sb.append(" Audio");
            } else if (i == 1) {
                sb.append(" Control");
            } else if (i != 2) {
                sb.append(" N/A");
            } else {
                sb.append(" MIDI");
            }
            int i2 = this.mDeviceDir;
            if (i2 == 0) {
                sb.append(" Capture");
            } else if (i2 != 1) {
                sb.append(" N/A");
            } else {
                sb.append(" Playback");
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
                String line = bufferedReader.readLine();
                if (line == null) {
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
