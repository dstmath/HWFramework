package com.huawei.internal.telephony.smartnet;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Handler;
import com.android.internal.telephony.HwAESCryptoUtil;
import com.android.internal.telephony.fullnetwork.HwFullNetworkManager;
import com.huawei.android.telephony.RlogEx;
import java.util.ArrayList;
import java.util.List;

public class HwSmartNetDb {
    private static final Object LOCK = new Object();
    private static final String SELECTION_CONDITION = " = ? ";
    private static final String SELECTION_CONDITION_AND = " = ? AND ";
    private static final String SELECTION_CONDITION_GREATER = " > ? ";
    private static final String TAG = "HwSmartNetDb";
    private static HwSmartNetDb sInstance;
    private Context mContext;
    private final HwSmartNetDbHelper mHwSmartNetDbHelper;
    private Handler mStateHandler;

    private HwSmartNetDb(Context context, Handler stateHandler) {
        this.mContext = context;
        this.mStateHandler = stateHandler;
        this.mHwSmartNetDbHelper = new HwSmartNetDbHelper(context);
        logi("create HwSmartNetDb");
    }

    public static HwSmartNetDb make(Context context, Handler stateHandler) {
        HwSmartNetDb hwSmartNetDb;
        synchronized (LOCK) {
            sInstance = new HwSmartNetDb(context, stateHandler);
            hwSmartNetDb = sInstance;
        }
        return hwSmartNetDb;
    }

    public static HwSmartNetDb getInstance() {
        HwSmartNetDb hwSmartNetDb;
        synchronized (LOCK) {
            hwSmartNetDb = sInstance;
        }
        return hwSmartNetDb;
    }

    public boolean insertCellSensor(CellSensor cellSensor) {
        if (cellSensor == null) {
            return false;
        }
        ContentValues values = new ContentValues();
        values.put(HwSmartNetDbHelper.CELLSENSOR_SLOTID, Integer.valueOf(cellSensor.getSlotId()));
        String encryptIccid = null;
        try {
            encryptIccid = HwAESCryptoUtil.encrypt(HwFullNetworkManager.getInstance().getMasterPassword(), cellSensor.getIccid());
        } catch (Exception e) {
            loge("CellSensor encrypt error");
        }
        values.put(HwSmartNetDbHelper.CELLSENSOR_ICCID, encryptIccid);
        values.put(HwSmartNetDbHelper.CELLSENSOR_TIME, Long.valueOf(cellSensor.getSamplingTime()));
        values.put(HwSmartNetDbHelper.CELLSENSOR_MAIN_CELLID, Long.valueOf(cellSensor.getMainCellId()));
        values.put(HwSmartNetDbHelper.CELLSENSOR_MAIN_PCI, Integer.valueOf(cellSensor.getMainPci()));
        values.put(HwSmartNetDbHelper.CELLSENSOR_MAIN_TAC, Integer.valueOf(cellSensor.getMainTac()));
        values.put(HwSmartNetDbHelper.CELLSENSOR_NEIGHBORING_CELLID, Long.valueOf(cellSensor.getNeighboringCellId()));
        values.put(HwSmartNetDbHelper.CELLSENSOR_NEIGHBORING_PCI, Integer.valueOf(cellSensor.getNeighboringPci()));
        values.put(HwSmartNetDbHelper.CELLSENSOR_NEIGHBORING_TAC, Integer.valueOf(cellSensor.getNeighboringTac()));
        values.put(HwSmartNetDbHelper.CELLSENSOR_VOICE_REGSTATE, Integer.valueOf(cellSensor.getVoiceRegState()));
        values.put(HwSmartNetDbHelper.CELLSENSOR_DATA_REGSTATE, Integer.valueOf(cellSensor.getDataRegState()));
        values.put(HwSmartNetDbHelper.CELLSENSOR_VOICE_OPERATOR_NUMERIC, cellSensor.getVoiceOperatorNumeric());
        values.put(HwSmartNetDbHelper.CELLSENSOR_DATA_OPERATOR_NUMERIC, cellSensor.getDataOperatorNumeric());
        values.put(HwSmartNetDbHelper.CELLSENSOR_VOICE_RADIOTECH, Integer.valueOf(cellSensor.getVoiceRadioTech()));
        values.put(HwSmartNetDbHelper.CELLSENSOR_DATA_RADIOTECH, Integer.valueOf(cellSensor.getDataRadioTech()));
        values.put(HwSmartNetDbHelper.CELLSENSOR_NRSTATE, Integer.valueOf(cellSensor.getNrState()));
        values.put(HwSmartNetDbHelper.CELLSENSOR_NSASTATE, Integer.valueOf(cellSensor.getNsaState()));
        values.put(HwSmartNetDbHelper.CELLSENSOR_CDMA_LEVELHW, Integer.valueOf(cellSensor.getCdmaLevelHw()));
        values.put(HwSmartNetDbHelper.CELLSENSOR_GSM_LEVELHW, Integer.valueOf(cellSensor.getGsmLevelHw()));
        values.put(HwSmartNetDbHelper.CELLSENSOR_WCDMA_LEVELHW, Integer.valueOf(cellSensor.getWcdmaLevelHw()));
        values.put(HwSmartNetDbHelper.CELLSENSOR_LTE_LEVELHW, Integer.valueOf(cellSensor.getLteLevelHw()));
        values.put(HwSmartNetDbHelper.CELLSENSOR_NR_LEVELHW, Integer.valueOf(cellSensor.getNrLevelHw()));
        if (this.mHwSmartNetDbHelper.getWritableDatabase().insert(HwSmartNetDbHelper.TABLE_CELLSENSOR, null, values) > 0) {
            return true;
        }
        return false;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:17:0x017f, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0183, code lost:
        if (r1 != null) goto L_0x0185;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0185, code lost:
        if (r2 != null) goto L_0x0187;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:?, code lost:
        r1.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x018d, code lost:
        r1.close();
     */
    public List<CellSensor> queryCellSensor(int slotId) {
        SQLiteDatabase db = this.mHwSmartNetDbHelper.getWritableDatabase();
        String[] selectionArguments = {String.valueOf(slotId)};
        List<CellSensor> cellSensorList = new ArrayList<>();
        try {
            Cursor cursor = db.query(HwSmartNetDbHelper.TABLE_CELLSENSOR, null, "slotId=?", selectionArguments, null, null, "ORDER BY samplingTime DESC");
            while (cursor.moveToNext()) {
                CellSensor cellSensor = new CellSensor();
                cellSensor.setId(cursor.getInt(cursor.getColumnIndex(HwSmartNetDbHelper.CELLSENSOR_ID)));
                cellSensor.setSlotId(cursor.getInt(cursor.getColumnIndex(HwSmartNetDbHelper.CELLSENSOR_SLOTID)));
                String decryptIccid = null;
                try {
                    decryptIccid = HwAESCryptoUtil.decrypt(HwFullNetworkManager.getInstance().getMasterPassword(), cursor.getString(cursor.getColumnIndex(HwSmartNetDbHelper.CELLSENSOR_ICCID)));
                } catch (Exception e) {
                    RlogEx.d(TAG, "CellSensor encrypt error");
                }
                cellSensor.setIccid(decryptIccid);
                cellSensor.setSamplingTime(cursor.getLong(cursor.getColumnIndex(HwSmartNetDbHelper.CELLSENSOR_TIME)));
                cellSensor.setMainCellId(cursor.getLong(cursor.getColumnIndex(HwSmartNetDbHelper.CELLSENSOR_MAIN_CELLID)));
                cellSensor.setMainPci(cursor.getInt(cursor.getColumnIndex(HwSmartNetDbHelper.CELLSENSOR_MAIN_PCI)));
                cellSensor.setMainTac(cursor.getInt(cursor.getColumnIndex(HwSmartNetDbHelper.CELLSENSOR_MAIN_TAC)));
                cellSensor.setNeighboringCellId(cursor.getLong(cursor.getColumnIndex(HwSmartNetDbHelper.CELLSENSOR_NEIGHBORING_CELLID)));
                cellSensor.setNeighboringPci(cursor.getInt(cursor.getColumnIndex(HwSmartNetDbHelper.CELLSENSOR_NEIGHBORING_PCI)));
                cellSensor.setNeighboringTac(cursor.getInt(cursor.getColumnIndex(HwSmartNetDbHelper.CELLSENSOR_NEIGHBORING_TAC)));
                cellSensor.setVoiceRegState(cursor.getInt(cursor.getColumnIndex(HwSmartNetDbHelper.CELLSENSOR_VOICE_REGSTATE)));
                cellSensor.setDataRegState(cursor.getInt(cursor.getColumnIndex(HwSmartNetDbHelper.CELLSENSOR_DATA_REGSTATE)));
                cellSensor.setVoiceOperatorNumeric(cursor.getString(cursor.getColumnIndex(HwSmartNetDbHelper.CELLSENSOR_VOICE_OPERATOR_NUMERIC)));
                cellSensor.setDataOperatorNumeric(cursor.getString(cursor.getColumnIndex(HwSmartNetDbHelper.CELLSENSOR_DATA_OPERATOR_NUMERIC)));
                cellSensor.setVoiceRadioTech(cursor.getInt(cursor.getColumnIndex(HwSmartNetDbHelper.CELLSENSOR_VOICE_RADIOTECH)));
                cellSensor.setDataRadioTech(cursor.getInt(cursor.getColumnIndex(HwSmartNetDbHelper.CELLSENSOR_DATA_RADIOTECH)));
                cellSensor.setNrState(cursor.getInt(cursor.getColumnIndex(HwSmartNetDbHelper.CELLSENSOR_NRSTATE)));
                cellSensor.setNsaState(cursor.getInt(cursor.getColumnIndex(HwSmartNetDbHelper.CELLSENSOR_NSASTATE)));
                cellSensor.setCdmaLevelHw(cursor.getInt(cursor.getColumnIndex(HwSmartNetDbHelper.CELLSENSOR_CDMA_LEVELHW)));
                cellSensor.setGsmLevelHw(cursor.getInt(cursor.getColumnIndex(HwSmartNetDbHelper.CELLSENSOR_GSM_LEVELHW)));
                cellSensor.setWcdmaLevelHw(cursor.getInt(cursor.getColumnIndex(HwSmartNetDbHelper.CELLSENSOR_WCDMA_LEVELHW)));
                cellSensor.setLteLevelHw(cursor.getInt(cursor.getColumnIndex(HwSmartNetDbHelper.CELLSENSOR_LTE_LEVELHW)));
                cellSensor.setNrLevelHw(cursor.getInt(cursor.getColumnIndex(HwSmartNetDbHelper.CELLSENSOR_NR_LEVELHW)));
                cellSensorList.add(cellSensor);
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (SQLiteException e2) {
            loge("query CellSensor error");
        }
        return cellSensorList;
        throw th;
    }

    private ContentValues buildCellInfoContentValue(CellInfoPoint cellInfoPoint) {
        ContentValues values = new ContentValues();
        values.put(HwSmartNetDbHelper.ROUTE_ID, Integer.valueOf(cellInfoPoint.getRouteId()));
        values.put(HwSmartNetDbHelper.CELL_INFO_POINT_CELL_IDENTITY, Long.valueOf(cellInfoPoint.getCellIdentity()));
        values.put(HwSmartNetDbHelper.ICCID_HASH, cellInfoPoint.getIccIdHash());
        values.put(HwSmartNetDbHelper.CELL_INFO_POINT_BLACK_TYPE, Integer.valueOf(cellInfoPoint.getBlackType()));
        values.put(HwSmartNetDbHelper.CELL_INFO_POINT_NORMAL_COUNTS, Integer.valueOf(cellInfoPoint.getNormalCounts()));
        values.put(HwSmartNetDbHelper.CELL_INFO_POINT_EXCEPTION_COUNTS, Integer.valueOf(cellInfoPoint.getExceptionCounts()));
        values.put(HwSmartNetDbHelper.CELL_INFO_POINT_TOTAL_COUNTS, Integer.valueOf(cellInfoPoint.getTotalCounts()));
        values.put(HwSmartNetDbHelper.CELL_INFO_POINT_ARRIVE_TIME, Long.valueOf(cellInfoPoint.getArriveTime()));
        values.put(HwSmartNetDbHelper.CELL_INFO_POINT_CONVERTTOEXCEPTION_DURATION, Long.valueOf(cellInfoPoint.getConvertToExceptionDuration()));
        values.put(HwSmartNetDbHelper.CELL_INFO_POINT_EXCEPTION_PROBABILITY, Double.valueOf(cellInfoPoint.getExceptionProbability()));
        return values;
    }

    private ContentValues buildUpdateCellInfoContentValue(CellInfoPoint cellInfoPoint) {
        ContentValues values = new ContentValues();
        values.put(HwSmartNetDbHelper.CELL_INFO_POINT_BLACK_TYPE, Integer.valueOf(cellInfoPoint.getBlackType()));
        values.put(HwSmartNetDbHelper.CELL_INFO_POINT_NORMAL_COUNTS, Integer.valueOf(cellInfoPoint.getNormalCounts()));
        values.put(HwSmartNetDbHelper.CELL_INFO_POINT_EXCEPTION_COUNTS, Integer.valueOf(cellInfoPoint.getExceptionCounts()));
        values.put(HwSmartNetDbHelper.CELL_INFO_POINT_TOTAL_COUNTS, Integer.valueOf(cellInfoPoint.getTotalCounts()));
        values.put(HwSmartNetDbHelper.CELL_INFO_POINT_ARRIVE_TIME, Long.valueOf(cellInfoPoint.getArriveTime()));
        values.put(HwSmartNetDbHelper.CELL_INFO_POINT_CONVERTTOEXCEPTION_DURATION, Long.valueOf(cellInfoPoint.getConvertToExceptionDuration()));
        values.put(HwSmartNetDbHelper.CELL_INFO_POINT_EXCEPTION_PROBABILITY, Double.valueOf(cellInfoPoint.getExceptionProbability()));
        return values;
    }

    private ContentValues buildStudyIndexContentValue(StudyIndexTable studyIndexTable) {
        ContentValues values = new ContentValues();
        values.put(HwSmartNetDbHelper.ROUTE_ID, Integer.valueOf(studyIndexTable.getRouteId()));
        values.put(HwSmartNetDbHelper.ICCID_HASH, studyIndexTable.getIccidHash());
        values.put(HwSmartNetDbHelper.STUDY_INDEX_MAX_COUNT, Integer.valueOf(studyIndexTable.getStudyMaxCount()));
        return values;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0083, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0087, code lost:
        if (r1 != null) goto L_0x0089;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0089, code lost:
        if (r2 != null) goto L_0x008b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:?, code lost:
        r1.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0091, code lost:
        r1.close();
     */
    public void insertOrUpdateCellInfoPoint(CellInfoPoint cellInfoPoint) {
        if (cellInfoPoint != null) {
            SQLiteDatabase db = this.mHwSmartNetDbHelper.getWritableDatabase();
            String[] selectionArguments = {Integer.toString(cellInfoPoint.getRouteId()), Long.toString(cellInfoPoint.getCellIdentity()), cellInfoPoint.getIccIdHash()};
            try {
                Cursor cursor = db.query(HwSmartNetDbHelper.TABLE_CELLINFOPOINTS, null, "RouteId = ? AND CellIdentity = ? AND IccIdHash = ? ", selectionArguments, null, null, null);
                if (cursor.getCount() > 0) {
                    int result = db.update(HwSmartNetDbHelper.TABLE_CELLINFOPOINTS, buildUpdateCellInfoContentValue(cellInfoPoint), "RouteId = ? AND CellIdentity = ? AND IccIdHash = ? ", selectionArguments);
                    logi("update CellInfoPoint ret = " + result);
                } else {
                    long result2 = db.insert(HwSmartNetDbHelper.TABLE_CELLINFOPOINTS, null, buildCellInfoContentValue(cellInfoPoint));
                    logi("insert CellInfoPoint ret = " + result2);
                }
                if (cursor != null) {
                    cursor.close();
                    return;
                }
                return;
            } catch (SQLiteException e) {
                loge("insertOrUpdateCellInfoPoint insert error.");
                return;
            }
        } else {
            return;
        }
        throw th;
    }

    private void buildCellInfoByCursor(Cursor cursor, CellInfoPoint cellInfoPoint) {
        cellInfoPoint.setBlackType(cursor.getInt(cursor.getColumnIndex(HwSmartNetDbHelper.CELL_INFO_POINT_BLACK_TYPE)));
        cellInfoPoint.setNormalCounts(cursor.getInt(cursor.getColumnIndex(HwSmartNetDbHelper.CELL_INFO_POINT_NORMAL_COUNTS)));
        cellInfoPoint.setExceptionCounts(cursor.getInt(cursor.getColumnIndex(HwSmartNetDbHelper.CELL_INFO_POINT_EXCEPTION_COUNTS)));
        cellInfoPoint.setTotalCounts(cursor.getInt(cursor.getColumnIndex(HwSmartNetDbHelper.CELL_INFO_POINT_TOTAL_COUNTS)));
        cellInfoPoint.setArriveTime(cursor.getLong(cursor.getColumnIndex(HwSmartNetDbHelper.CELL_INFO_POINT_ARRIVE_TIME)));
        cellInfoPoint.setConvertToExceptionDuration(cursor.getLong(cursor.getColumnIndex(HwSmartNetDbHelper.CELL_INFO_POINT_CONVERTTOEXCEPTION_DURATION)));
        cellInfoPoint.setExceptionProbability(cursor.getDouble(cursor.getColumnIndex(HwSmartNetDbHelper.CELL_INFO_POINT_EXCEPTION_PROBABILITY)));
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0056, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x005a, code lost:
        if (r1 != null) goto L_0x005c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x005c, code lost:
        if (r2 != null) goto L_0x005e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:?, code lost:
        r1.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0064, code lost:
        r1.close();
     */
    public List<CellInfoPoint> queryMatchCellInfoPoints(int routeId, String iccIdHash) {
        SQLiteDatabase db = this.mHwSmartNetDbHelper.getReadableDatabase();
        String[] selectionArguments = {Integer.toString(routeId), iccIdHash, Double.toString(0.8d)};
        List<CellInfoPoint> cellInfoPoints = new ArrayList<>();
        try {
            Cursor cursor = db.query(HwSmartNetDbHelper.TABLE_CELLINFOPOINTS, null, "RouteId = ? AND IccIdHash = ? AND ExceptionProbability > ? ", selectionArguments, null, null, null);
            while (cursor.moveToNext()) {
                CellInfoPoint cellInfoPoint = new CellInfoPoint(routeId, cursor.getLong(cursor.getColumnIndex(HwSmartNetDbHelper.CELL_INFO_POINT_CELL_IDENTITY)), iccIdHash);
                buildCellInfoByCursor(cursor, cellInfoPoint);
                cellInfoPoints.add(cellInfoPoint);
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (SQLiteException e) {
            loge("queryCellInfoPoints error.");
        }
        return cellInfoPoints;
        throw th;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x004a, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x004e, code lost:
        if (r1 != null) goto L_0x0050;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0050, code lost:
        if (r2 != null) goto L_0x0052;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:?, code lost:
        r1.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0058, code lost:
        r1.close();
     */
    public List<CellInfoPoint> queryCellInfoPoints(int routeId, String iccIdHash) {
        SQLiteDatabase db = this.mHwSmartNetDbHelper.getReadableDatabase();
        String[] selectionArguments = {Integer.toString(routeId), iccIdHash};
        List<CellInfoPoint> cellInfoPoints = new ArrayList<>();
        try {
            Cursor cursor = db.query(HwSmartNetDbHelper.TABLE_CELLINFOPOINTS, null, "RouteId = ? AND IccIdHash = ? ", selectionArguments, null, null, null);
            while (cursor.moveToNext()) {
                CellInfoPoint cellInfoPoint = new CellInfoPoint(routeId, cursor.getLong(cursor.getColumnIndex(HwSmartNetDbHelper.CELL_INFO_POINT_CELL_IDENTITY)), iccIdHash);
                buildCellInfoByCursor(cursor, cellInfoPoint);
                cellInfoPoints.add(cellInfoPoint);
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (SQLiteException e) {
            loge("queryCellInfoPoints error");
        }
        return cellInfoPoints;
        throw th;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x004d, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0051, code lost:
        if (r1 != null) goto L_0x0053;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0053, code lost:
        if (r2 != null) goto L_0x0055;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:?, code lost:
        r1.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x005b, code lost:
        r1.close();
     */
    public List<StudyIndexTable> queryStudyIndex() {
        SQLiteDatabase db = this.mHwSmartNetDbHelper.getReadableDatabase();
        List<StudyIndexTable> studyIndexTables = new ArrayList<>();
        try {
            Cursor cursor = db.query(HwSmartNetDbHelper.TABLE_STUDYINDEX, null, null, null, null, null, null);
            while (cursor.moveToNext()) {
                studyIndexTables.add(new StudyIndexTable(cursor.getInt(cursor.getColumnIndex(HwSmartNetDbHelper.ROUTE_ID)), cursor.getString(cursor.getColumnIndex(HwSmartNetDbHelper.ICCID_HASH)), cursor.getInt(cursor.getColumnIndex(HwSmartNetDbHelper.STUDY_INDEX_MAX_COUNT))));
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (SQLiteException e) {
            loge("queryCellInfoPoints error.");
        }
        return studyIndexTables;
        throw th;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0086, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x008a, code lost:
        if (r1 != null) goto L_0x008c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x008c, code lost:
        if (r2 != null) goto L_0x008e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:?, code lost:
        r1.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0094, code lost:
        r1.close();
     */
    public void insertOrUpdateStudyIndex(StudyIndexTable studyIndexTable) {
        if (studyIndexTable != null) {
            SQLiteDatabase db = this.mHwSmartNetDbHelper.getWritableDatabase();
            String[] selectionArguments = {Integer.toString(studyIndexTable.getRouteId()), studyIndexTable.getIccidHash()};
            try {
                Cursor cursor = db.query(HwSmartNetDbHelper.TABLE_STUDYINDEX, null, "RouteId = ? AND IccIdHash = ? ", selectionArguments, null, null, null);
                if (cursor.getCount() > 0) {
                    ContentValues values = new ContentValues();
                    values.put(HwSmartNetDbHelper.STUDY_INDEX_MAX_COUNT, Integer.valueOf(studyIndexTable.getStudyMaxCount()));
                    int result = db.update(HwSmartNetDbHelper.TABLE_STUDYINDEX, values, "RouteId = ? AND IccIdHash = ? ", selectionArguments);
                    logd("update StudyIndex ret = " + result);
                } else {
                    long result2 = db.insert(HwSmartNetDbHelper.TABLE_STUDYINDEX, null, buildStudyIndexContentValue(studyIndexTable));
                    logd("insert StudyIndex ret = " + result2);
                }
                if (cursor != null) {
                    cursor.close();
                    return;
                }
                return;
            } catch (SQLiteException e) {
                loge("insertOrUpdateStudyIndex insert error.");
                return;
            }
        } else {
            return;
        }
        throw th;
    }

    private void logi(String msg) {
        RlogEx.i(TAG, msg);
    }

    private void loge(String msg) {
        RlogEx.e(TAG, msg);
    }

    private void logd(String msg) {
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x00a8, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x00ac, code lost:
        if (r1 != null) goto L_0x00ae;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x00ae, code lost:
        if (r2 != null) goto L_0x00b0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:?, code lost:
        r1.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x00b6, code lost:
        r1.close();
     */
    public List<CellSensor> queryCellSensorTest(int routeId, int slotId, String iccid) {
        SQLiteDatabase db = this.mHwSmartNetDbHelper.getReadableDatabase();
        String[] selectionArguments = {Integer.toString(slotId), iccid};
        List<CellSensor> cellSensors = new ArrayList<>();
        try {
            Cursor cursor = db.query("CellSensorTest", null, "slotId = ? AND iccid = ? ", selectionArguments, null, null, null);
            while (cursor.moveToNext()) {
                CellSensor cellSensor = new CellSensor();
                cellSensor.setRouteId(routeId);
                cellSensor.setSlotId(cursor.getInt(cursor.getColumnIndex(HwSmartNetDbHelper.CELLSENSOR_SLOTID)));
                cellSensor.setMainCellId(cursor.getLong(cursor.getColumnIndex(HwSmartNetDbHelper.CELLSENSOR_MAIN_CELLID)));
                cellSensor.setSamplingTime(cursor.getLong(cursor.getColumnIndex(HwSmartNetDbHelper.CELLSENSOR_TIME)));
                cellSensor.setVoiceRegState(cursor.getInt(cursor.getColumnIndex(HwSmartNetDbHelper.CELLSENSOR_VOICE_REGSTATE)));
                cellSensor.setDataRegState(cursor.getInt(cursor.getColumnIndex(HwSmartNetDbHelper.CELLSENSOR_DATA_REGSTATE)));
                cellSensor.setDataRadioTech(cursor.getInt(cursor.getColumnIndex(HwSmartNetDbHelper.CELLSENSOR_DATA_RADIOTECH)));
                cellSensor.setVoiceRadioTech(cursor.getInt(cursor.getColumnIndex(HwSmartNetDbHelper.CELLSENSOR_VOICE_RADIOTECH)));
                cellSensor.setIccid(cursor.getString(cursor.getColumnIndex(HwSmartNetDbHelper.CELLSENSOR_ICCID)));
                cellSensors.add(cellSensor);
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (SQLiteException e) {
            logi("queryCellInfoPoints error.");
        }
        return cellSensors;
        throw th;
    }
}
