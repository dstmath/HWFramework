package com.huawei.android.hardware.qcomfmradio;

import android.media.BuildConfig;
import android.support.annotation.NonNull;
import android.util.Log;
import com.huawei.android.hardware.qcomfmradio.SpurFileFormatConst;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/* access modifiers changed from: package-private */
public class SpurFileParser implements SpurFileParserInterface {
    private static final String TAG = "SPUR";

    SpurFileParser() {
    }

    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:146:0x0332 A[ExcHandler: IOException (e java.io.IOException), Splitter:B:5:0x001d] */
    private boolean parse(BufferedReader reader, SpurTable t) {
        Spur spur;
        byte RotationValue;
        int RotationValue2;
        byte NoOfSpursToTrack;
        byte NoOfSpursToTrack2;
        boolean z = false;
        if (t == null || reader == null) {
            return false;
        }
        SpurFileFormatConst.LineType lastLine = SpurFileFormatConst.LineType.EMPTY_LINE;
        byte noOfSpursFreq = 0;
        byte freqCnt = 0;
        byte RotationValue3 = 0;
        int LsbOfIntegrationLength = 0;
        int RotationValue4 = 0;
        int SpurFreq = 0;
        int entryFound = 0;
        while (reader.ready()) {
            try {
                String line = reader.readLine();
                if (line == null) {
                    return true;
                }
                String line2 = removeSpaces(line);
                if (lineIsComment(line2)) {
                    z = false;
                } else if (entryFound == 2 && freqCnt <= noOfSpursFreq) {
                    try {
                        if (lastLine == SpurFileFormatConst.LineType.EMPTY_LINE) {
                            try {
                                if (lineIsOfType(line2, SpurFileFormatConst.SPUR_FREQ)) {
                                    SpurFreq = Integer.parseInt(line2.substring(line2.indexOf(61) + 1));
                                    lastLine = SpurFileFormatConst.LineType.SPUR_FR_LINE;
                                    freqCnt = (byte) (freqCnt + 1);
                                    RotationValue4 = RotationValue4;
                                    z = false;
                                }
                            } catch (NumberFormatException e) {
                                z = false;
                                Log.d(TAG, "NumberFormatException");
                                return z;
                            } catch (IOException e2) {
                                Log.d(TAG, "IOException");
                                return false;
                            }
                        }
                        if (lastLine != SpurFileFormatConst.LineType.SPUR_FR_LINE) {
                            return false;
                        }
                        if (!lineIsOfType(line2, SpurFileFormatConst.SPUR_NO_OF)) {
                            return false;
                        }
                        byte NoOfSpursToTrack3 = Byte.parseByte(line2.substring(line2.indexOf(61) + 1));
                        try {
                            spur = new Spur();
                            spur.setSpurFreq(SpurFreq);
                            spur.setNoOfSpursToTrack(NoOfSpursToTrack3);
                            String line3 = line2;
                            int i = 0;
                            RotationValue = RotationValue3;
                            RotationValue2 = LsbOfIntegrationLength;
                            while (i < 3) {
                                try {
                                    SpurDetails spurDetails = new SpurDetails();
                                    String line4 = line3;
                                    int j = 0;
                                    byte LsbOfIntegrationLength2 = RotationValue;
                                    int RotationValue5 = RotationValue2;
                                    while (true) {
                                        try {
                                            if (j >= SpurFileFormatConst.SPUR_DETAILS_FOR_EACH_FREQ_CNT) {
                                                NoOfSpursToTrack = NoOfSpursToTrack3;
                                                break;
                                            } else if (!reader.ready()) {
                                                NoOfSpursToTrack = NoOfSpursToTrack3;
                                                break;
                                            } else {
                                                String line5 = reader.readLine();
                                                if (line5 != null) {
                                                    try {
                                                        line4 = removeSpaces(line5);
                                                    } catch (NumberFormatException e3) {
                                                        z = false;
                                                        Log.d(TAG, "NumberFormatException");
                                                        return z;
                                                    } catch (IOException e4) {
                                                        Log.d(TAG, "IOException");
                                                        return false;
                                                    }
                                                } else {
                                                    line4 = line5;
                                                }
                                                if (line4 == null) {
                                                    NoOfSpursToTrack2 = NoOfSpursToTrack3;
                                                } else {
                                                    StringBuilder sb = new StringBuilder();
                                                    NoOfSpursToTrack2 = NoOfSpursToTrack3;
                                                    try {
                                                        sb.append(SpurFileFormatConst.SPUR_ROTATION_VALUE);
                                                        sb.append(i);
                                                        if (lineIsOfType(line4, sb.toString())) {
                                                            int RotationValue6 = Integer.parseInt(line4.substring(line4.indexOf(61) + 1));
                                                            try {
                                                                spurDetails.setRotationValue(RotationValue6);
                                                                RotationValue5 = RotationValue6;
                                                            } catch (NumberFormatException e5) {
                                                                z = false;
                                                                Log.d(TAG, "NumberFormatException");
                                                                return z;
                                                            } catch (IOException e6) {
                                                                Log.d(TAG, "IOException");
                                                                return false;
                                                            }
                                                        } else {
                                                            if (lineIsOfType(line4, SpurFileFormatConst.SPUR_LSB_LENGTH + i)) {
                                                                byte LsbOfIntegrationLength3 = Byte.parseByte(line4.substring(line4.indexOf(61) + 1));
                                                                try {
                                                                    spurDetails.setLsbOfIntegrationLength(LsbOfIntegrationLength3);
                                                                    LsbOfIntegrationLength2 = LsbOfIntegrationLength3;
                                                                } catch (NumberFormatException e7) {
                                                                    z = false;
                                                                    Log.d(TAG, "NumberFormatException");
                                                                    return z;
                                                                } catch (IOException e8) {
                                                                    Log.d(TAG, "IOException");
                                                                    return false;
                                                                }
                                                            } else {
                                                                if (lineIsOfType(line4, SpurFileFormatConst.SPUR_FILTER_COEFF + i)) {
                                                                    spurDetails.setFilterCoefficeint(Byte.parseByte(line4.substring(line4.indexOf(61) + 1)));
                                                                } else {
                                                                    if (lineIsOfType(line4, SpurFileFormatConst.SPUR_IS_ENABLE + i)) {
                                                                        spurDetails.setIsEnableSpur(Byte.parseByte(line4.substring(line4.indexOf(61) + 1)));
                                                                    } else {
                                                                        if (lineIsOfType(line4, SpurFileFormatConst.SPUR_LEVEL + i)) {
                                                                            spurDetails.setSpurLevel(Byte.parseByte(line4.substring(line4.indexOf(61) + 1)));
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    } catch (NumberFormatException e9) {
                                                        z = false;
                                                        Log.d(TAG, "NumberFormatException");
                                                        return z;
                                                    } catch (IOException e10) {
                                                        Log.d(TAG, "IOException");
                                                        return false;
                                                    }
                                                }
                                                j++;
                                                SpurFreq = SpurFreq;
                                                NoOfSpursToTrack3 = NoOfSpursToTrack2;
                                            }
                                        } catch (NumberFormatException e11) {
                                            z = false;
                                            Log.d(TAG, "NumberFormatException");
                                            return z;
                                        } catch (IOException e12) {
                                            Log.d(TAG, "IOException");
                                            return false;
                                        }
                                    }
                                    spur.addSpurDetails(spurDetails);
                                    i++;
                                    line3 = line4;
                                    RotationValue2 = RotationValue5;
                                    lastLine = lastLine;
                                    RotationValue = LsbOfIntegrationLength2;
                                    SpurFreq = SpurFreq;
                                    NoOfSpursToTrack3 = NoOfSpursToTrack;
                                } catch (NumberFormatException e13) {
                                    z = false;
                                    Log.d(TAG, "NumberFormatException");
                                    return z;
                                } catch (IOException e14) {
                                    Log.d(TAG, "IOException");
                                    return false;
                                }
                            }
                        } catch (NumberFormatException e15) {
                            z = false;
                            Log.d(TAG, "NumberFormatException");
                            return z;
                        } catch (IOException e16) {
                            Log.d(TAG, "IOException");
                            return false;
                        }
                        try {
                            t.InsertSpur(spur);
                            lastLine = SpurFileFormatConst.LineType.EMPTY_LINE;
                            LsbOfIntegrationLength = RotationValue2;
                            RotationValue3 = RotationValue;
                            SpurFreq = SpurFreq;
                            RotationValue4 = NoOfSpursToTrack3;
                            z = false;
                        } catch (NumberFormatException e17) {
                            z = false;
                            Log.d(TAG, "NumberFormatException");
                            return z;
                        } catch (IOException e18) {
                            Log.d(TAG, "IOException");
                            return false;
                        }
                    } catch (NumberFormatException e19) {
                        z = false;
                        Log.d(TAG, "NumberFormatException");
                        return z;
                    } catch (IOException e20) {
                        Log.d(TAG, "IOException");
                        return false;
                    }
                } else if (entryFound == 1) {
                    try {
                        if (!lineIsOfType(line2, SpurFileFormatConst.SPUR_NUM_ENTRY)) {
                            return false;
                        }
                        noOfSpursFreq = Byte.parseByte(line2.substring(line2.indexOf(61) + 1));
                        t.SetspurNoOfFreq(noOfSpursFreq);
                        entryFound++;
                        RotationValue4 = RotationValue4;
                        lastLine = lastLine;
                        SpurFreq = SpurFreq;
                        z = false;
                    } catch (NumberFormatException e21) {
                        z = false;
                        Log.d(TAG, "NumberFormatException");
                        return z;
                    } catch (IOException e22) {
                        Log.d(TAG, "IOException");
                        return false;
                    }
                } else if (!lineIsOfType(line2, SpurFileFormatConst.SPUR_MODE)) {
                    return false;
                } else {
                    t.SetMode(Byte.parseByte(line2.substring(line2.indexOf(61) + 1)));
                    entryFound++;
                    RotationValue4 = RotationValue4;
                    lastLine = lastLine;
                    SpurFreq = SpurFreq;
                    z = false;
                }
            } catch (NumberFormatException e23) {
                z = false;
                Log.d(TAG, "NumberFormatException");
                return z;
            } catch (IOException e24) {
            }
        }
        return true;
    }

    @NonNull
    private String removeSpaces(String s) {
        return SpurFileFormatConst.SPACE_PATTERN.matcher(s).replaceAll(BuildConfig.FLAVOR);
    }

    private boolean lineIsOfType(String line, String lineType) {
        try {
            int indexEqual = line.indexOf(61);
            if (indexEqual < 0 || indexEqual >= line.length() || !line.startsWith(lineType)) {
                return false;
            }
            Integer.parseInt(line.substring(indexEqual + 1));
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean lineIsComment(String s) {
        if (s == null || BuildConfig.FLAVOR.equals(s) || " ".equals(s) || s.length() == 0 || s.charAt(0) == '#') {
            return true;
        }
        return false;
    }

    @Override // com.huawei.android.hardware.qcomfmradio.SpurFileParserInterface
    public SpurTable GetSpurTable(String fileName) {
        BufferedReader reader = null;
        SpurTable t = new SpurTable();
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
            parse(reader, t);
            try {
                reader.close();
            } catch (IOException e) {
                Log.d(TAG, "GetSpurTable close reader fail");
            }
        } catch (IOException e2) {
            Log.d(TAG, "GetSpurTable fail");
            if (reader != null) {
                reader.close();
            }
        } catch (Throwable th) {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e3) {
                    Log.d(TAG, "GetSpurTable close reader fail");
                }
            }
            throw th;
        }
        return t;
    }
}
