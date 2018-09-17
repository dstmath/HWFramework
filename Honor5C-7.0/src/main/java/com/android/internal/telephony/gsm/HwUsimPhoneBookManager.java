package com.android.internal.telephony.gsm;

import android.os.AsyncResult;
import android.os.Message;
import android.telephony.Rlog;
import android.util.SparseArray;
import com.android.internal.telephony.gsm.UsimPhoneBookManager.File;
import com.android.internal.telephony.gsm.UsimPhoneBookManager.PbrRecord;
import com.android.internal.telephony.uicc.AdnRecord;
import com.android.internal.telephony.uicc.AdnRecordCache;
import com.android.internal.telephony.uicc.IccFileHandler;
import java.util.ArrayList;

public class HwUsimPhoneBookManager extends UsimPhoneBookManager {
    private static final boolean DBG = true;
    protected static final int EVENT_GET_SIZE_DONE = 101;
    private static final String LOG_TAG = "HwUsimPhoneBookManager";
    private static final int USIM_EFADN_TAG = 192;
    private static UsimPhoneBookManagerUtils usimPhoneBookManagerUtils;
    private int[] recordSize;
    private int[] temRecordSize;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.gsm.HwUsimPhoneBookManager.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.gsm.HwUsimPhoneBookManager.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.gsm.HwUsimPhoneBookManager.<clinit>():void");
    }

    public HwUsimPhoneBookManager(IccFileHandler fh) {
        super(fh, null);
        this.recordSize = new int[3];
        this.temRecordSize = new int[3];
        usimPhoneBookManagerUtils.setFh(this, fh);
        usimPhoneBookManagerUtils.setPhoneBookRecords(this, new ArrayList());
        usimPhoneBookManagerUtils.setPbrRecords(this, null);
        usimPhoneBookManagerUtils.setIsPbrPresent(this, DBG);
    }

    public HwUsimPhoneBookManager(IccFileHandler fh, AdnRecordCache cache) {
        super(fh, cache);
        this.recordSize = new int[3];
        this.temRecordSize = new int[3];
    }

    public ArrayList<AdnRecord> getPhonebookRecords() {
        if (usimPhoneBookManagerUtils.getPhoneBookRecords(this).isEmpty()) {
            return null;
        }
        return usimPhoneBookManagerUtils.getPhoneBookRecords(this);
    }

    public void setIccFileHandler(IccFileHandler fh) {
        usimPhoneBookManagerUtils.setFh(this, fh);
    }

    public int[] getAdnRecordsSizeFromEF() {
        synchronized (usimPhoneBookManagerUtils.getLockObject(this)) {
            if (usimPhoneBookManagerUtils.getIsPbrPresent(this)) {
                if (usimPhoneBookManagerUtils.getPbrRecords(this) == null) {
                    usimPhoneBookManagerUtils.readPbrFileAndWait(this);
                }
                if (usimPhoneBookManagerUtils.getPbrRecords(this) == null) {
                    return null;
                }
                int numRecs = usimPhoneBookManagerUtils.getPbrRecords(this).size();
                this.temRecordSize[0] = 0;
                this.temRecordSize[1] = 0;
                this.temRecordSize[2] = 0;
                for (int i = 0; i < numRecs; i++) {
                    this.recordSize[0] = 0;
                    this.recordSize[1] = 0;
                    this.recordSize[2] = 0;
                    getAdnRecordsSizeAndWait(i);
                    Rlog.d(LOG_TAG, "getAdnRecordsSizeFromEF: recordSize[2]=" + this.recordSize[2]);
                    if (this.recordSize[0] != 0) {
                        this.temRecordSize[0] = this.recordSize[0];
                    }
                    if (this.recordSize[1] != 0) {
                        this.temRecordSize[1] = this.recordSize[1];
                    }
                    this.temRecordSize[2] = this.recordSize[2] + this.temRecordSize[2];
                }
                Rlog.d(LOG_TAG, "getAdnRecordsSizeFromEF: temRecordSize[2]=" + this.temRecordSize[2]);
                int[] iArr = this.temRecordSize;
                return iArr;
            }
            return null;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void getAdnRecordsSizeAndWait(int recNum) {
        if (usimPhoneBookManagerUtils.getPbrRecords(this) != null) {
            SparseArray<File> files = ((PbrRecord) usimPhoneBookManagerUtils.getPbrRecords(this).get(recNum)).mFileIds;
            if (files != null && files.size() != 0 && files.get(USIM_EFADN_TAG) != null) {
                int efid = ((File) files.get(USIM_EFADN_TAG)).getEfid();
                Rlog.d(LOG_TAG, "getAdnRecordsSize: efid=" + efid);
                usimPhoneBookManagerUtils.getFh(this).getEFLinearRecordSize(efid, obtainMessage(EVENT_GET_SIZE_DONE));
                boolean isWait = DBG;
                while (isWait) {
                    try {
                        usimPhoneBookManagerUtils.getLockObject(this).wait();
                        isWait = false;
                    } catch (InterruptedException e) {
                        Rlog.e(LOG_TAG, "Interrupted Exception in getAdnRecordsSizeAndWait");
                    }
                }
            }
        }
    }

    public int getPbrFileSize() {
        int size = 0;
        if (usimPhoneBookManagerUtils.getPbrRecords(this) != null) {
            size = usimPhoneBookManagerUtils.getPbrRecords(this).size();
        }
        log("getPbrFileSize:" + size);
        return size;
    }

    public int getEFidInPBR(int recNum, int tag) {
        int efid = 0;
        if (usimPhoneBookManagerUtils.getPbrRecords(this) == null) {
            return 0;
        }
        SparseArray<File> files = ((PbrRecord) usimPhoneBookManagerUtils.getPbrRecords(this).get(recNum)).mFileIds;
        if (files == null || files.size() == 0) {
            return 0;
        }
        if (files.get(tag) != null) {
            efid = ((File) files.get(tag)).getEfid();
        }
        log("getEFidInPBR, efid = " + efid + ", recNum = " + recNum + ", tag = " + tag);
        return efid;
    }

    public void handleMessage(Message msg) {
        switch (msg.what) {
            case EVENT_GET_SIZE_DONE /*101*/:
                AsyncResult ar = msg.obj;
                synchronized (usimPhoneBookManagerUtils.getLockObject(this)) {
                    if (ar.exception == null) {
                        this.recordSize = (int[]) ar.result;
                        log("GET_RECORD_SIZE Size " + this.recordSize[0] + " total " + this.recordSize[1] + " #record " + this.recordSize[2]);
                    }
                    usimPhoneBookManagerUtils.getLockObject(this).notify();
                    break;
                }
            default:
                super.handleMessage(msg);
        }
    }

    private void log(String msg) {
        Rlog.d(LOG_TAG, msg);
    }
}
