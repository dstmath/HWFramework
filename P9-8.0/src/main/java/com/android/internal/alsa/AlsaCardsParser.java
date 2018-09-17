package com.android.internal.alsa;

import android.util.LogException;
import android.util.Slog;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class AlsaCardsParser {
    protected static final boolean DEBUG = false;
    private static final String TAG = "AlsaCardsParser";
    private static final String kCardsFilePath = "/proc/asound/cards";
    private static LineTokenizer mTokenizer = new LineTokenizer(" :[]");
    private ArrayList<AlsaCardRecord> mCardRecords = new ArrayList();

    public class AlsaCardRecord {
        private static final String TAG = "AlsaCardRecord";
        private static final String kUsbCardKeyStr = "at usb-";
        public String mCardDescription = LogException.NO_VALUE;
        public String mCardName = LogException.NO_VALUE;
        public int mCardNum = -1;
        public String mField1 = LogException.NO_VALUE;
        public boolean mIsUsb = false;

        public boolean parse(String line, int lineIndex) {
            boolean z = false;
            int tokenIndex;
            if (lineIndex == 0) {
                tokenIndex = AlsaCardsParser.mTokenizer.nextToken(line, 0);
                int delimIndex = AlsaCardsParser.mTokenizer.nextDelimiter(line, tokenIndex);
                try {
                    this.mCardNum = Integer.parseInt(line.substring(tokenIndex, delimIndex));
                    tokenIndex = AlsaCardsParser.mTokenizer.nextToken(line, delimIndex);
                    delimIndex = AlsaCardsParser.mTokenizer.nextDelimiter(line, tokenIndex);
                    this.mField1 = line.substring(tokenIndex, delimIndex);
                    this.mCardName = line.substring(AlsaCardsParser.mTokenizer.nextToken(line, delimIndex));
                } catch (NumberFormatException e) {
                    Slog.e(TAG, "Failed to parse line " + lineIndex + " of " + AlsaCardsParser.kCardsFilePath + ": " + line.substring(tokenIndex, delimIndex));
                    return false;
                }
            } else if (lineIndex == 1) {
                tokenIndex = AlsaCardsParser.mTokenizer.nextToken(line, 0);
                if (tokenIndex != -1) {
                    int keyIndex = line.indexOf(kUsbCardKeyStr);
                    if (keyIndex != -1) {
                        z = true;
                    }
                    this.mIsUsb = z;
                    if (this.mIsUsb) {
                        this.mCardDescription = line.substring(tokenIndex, keyIndex - 1);
                    }
                }
            }
            return true;
        }

        public String textFormat() {
            return this.mCardName + " : " + this.mCardDescription;
        }

        public void log(int listIndex) {
            Slog.d(TAG, LogException.NO_VALUE + listIndex + " [" + this.mCardNum + " " + this.mCardName + " : " + this.mCardDescription + " usb:" + this.mIsUsb);
        }
    }

    public void scan() {
        this.mCardRecords = new ArrayList();
        try {
            FileReader reader = new FileReader(new File(kCardsFilePath));
            BufferedReader bufferedReader = new BufferedReader(reader);
            String str = LogException.NO_VALUE;
            while (true) {
                str = bufferedReader.readLine();
                if (str == null) {
                    break;
                }
                AlsaCardRecord cardRecord = new AlsaCardRecord();
                cardRecord.parse(str, 0);
                str = bufferedReader.readLine();
                if (str == null) {
                    break;
                }
                cardRecord.parse(str, 1);
                this.mCardRecords.add(cardRecord);
            }
            reader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e2) {
            e2.printStackTrace();
        }
    }

    public ArrayList<AlsaCardRecord> getScanRecords() {
        return this.mCardRecords;
    }

    public AlsaCardRecord getCardRecordAt(int index) {
        return (AlsaCardRecord) this.mCardRecords.get(index);
    }

    public AlsaCardRecord getCardRecordFor(int cardNum) {
        for (AlsaCardRecord rec : this.mCardRecords) {
            if (rec.mCardNum == cardNum) {
                return rec;
            }
        }
        return null;
    }

    public int getNumCardRecords() {
        return this.mCardRecords.size();
    }

    public boolean isCardUsb(int cardNum) {
        for (AlsaCardRecord rec : this.mCardRecords) {
            if (rec.mCardNum == cardNum) {
                return rec.mIsUsb;
            }
        }
        return false;
    }

    public int getDefaultUsbCard() {
        ArrayList<AlsaCardRecord> prevRecs = this.mCardRecords;
        scan();
        for (AlsaCardRecord rec : getNewCardRecords(prevRecs)) {
            if (rec.mIsUsb) {
                return rec.mCardNum;
            }
        }
        for (AlsaCardRecord rec2 : prevRecs) {
            if (rec2.mIsUsb) {
                return rec2.mCardNum;
            }
        }
        return -1;
    }

    public int getDefaultCard() {
        int card = getDefaultUsbCard();
        if (card >= 0 || getNumCardRecords() <= 0) {
            return card;
        }
        return getCardRecordAt(getNumCardRecords() - 1).mCardNum;
    }

    public static boolean hasCardNumber(ArrayList<AlsaCardRecord> recs, int cardNum) {
        for (AlsaCardRecord cardRec : recs) {
            if (cardRec.mCardNum == cardNum) {
                return true;
            }
        }
        return false;
    }

    public ArrayList<AlsaCardRecord> getNewCardRecords(ArrayList<AlsaCardRecord> prevScanRecs) {
        ArrayList<AlsaCardRecord> newRecs = new ArrayList();
        for (AlsaCardRecord rec : this.mCardRecords) {
            if (!hasCardNumber(prevScanRecs, rec.mCardNum)) {
                newRecs.add(rec);
            }
        }
        return newRecs;
    }

    public void Log(String heading) {
    }

    public static void LogDevices(String caption, ArrayList<AlsaCardRecord> deviceList) {
        Slog.d(TAG, caption + " ----------------");
        int listIndex = 0;
        for (AlsaCardRecord device : deviceList) {
            int listIndex2 = listIndex + 1;
            device.log(listIndex);
            listIndex = listIndex2;
        }
        Slog.d(TAG, "----------------");
    }
}
