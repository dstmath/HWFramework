package com.android.internal.alsa;

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
    private static LineTokenizer mTokenizer;
    private ArrayList<AlsaCardRecord> mCardRecords;

    public class AlsaCardRecord {
        private static final String TAG = "AlsaCardRecord";
        private static final String kUsbCardKeyStr = "at usb-";
        public String mCardDescription;
        public String mCardName;
        public int mCardNum;
        public String mField1;
        public boolean mIsUsb;

        public AlsaCardRecord() {
            this.mCardNum = -1;
            this.mField1 = "";
            this.mCardName = "";
            this.mCardDescription = "";
            this.mIsUsb = AlsaCardsParser.DEBUG;
        }

        public boolean parse(String line, int lineIndex) {
            boolean z = AlsaCardsParser.DEBUG;
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
                    return AlsaCardsParser.DEBUG;
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
            Slog.d(TAG, "" + listIndex + " [" + this.mCardNum + " " + this.mCardName + " : " + this.mCardDescription + " usb:" + this.mIsUsb);
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.alsa.AlsaCardsParser.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.alsa.AlsaCardsParser.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.alsa.AlsaCardsParser.<clinit>():void");
    }

    public AlsaCardsParser() {
        this.mCardRecords = new ArrayList();
    }

    public void scan() {
        this.mCardRecords = new ArrayList();
        try {
            FileReader reader = new FileReader(new File(kCardsFilePath));
            BufferedReader bufferedReader = new BufferedReader(reader);
            String str = "";
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
        return DEBUG;
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
        return DEBUG;
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
