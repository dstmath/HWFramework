package com.huawei.wallet.sdk.business.buscard.base.traffic;

import android.content.Context;
import android.nfc.tech.IsoDep;
import android.os.PowerManager;
import android.util.Log;
import com.huawei.wallet.sdk.business.buscard.base.appletcardinfo.ConfigData;
import com.huawei.wallet.sdk.business.buscard.base.model.ApduCommandInfo;
import com.huawei.wallet.sdk.business.buscard.base.model.CardInfo;
import com.huawei.wallet.sdk.business.buscard.base.model.TransactionRecord;
import com.huawei.wallet.sdk.business.buscard.base.operation.Operation;
import com.huawei.wallet.sdk.business.buscard.base.traffic.datacheck.DataChecker;
import com.huawei.wallet.sdk.business.buscard.base.traffic.readers.CardBalanceInfoReader;
import com.huawei.wallet.sdk.business.buscard.base.traffic.readers.CardC8FileInfoReader;
import com.huawei.wallet.sdk.business.buscard.base.traffic.readers.CardDateInfoReader;
import com.huawei.wallet.sdk.business.buscard.base.traffic.readers.CardNumInfoReader;
import com.huawei.wallet.sdk.business.buscard.base.traffic.readers.CardRecordInfoReader;
import com.huawei.wallet.sdk.business.buscard.base.traffic.readers.CardStationStatusReader;
import com.huawei.wallet.sdk.business.buscard.base.traffic.readers.CardStatusInfoReader;
import com.huawei.wallet.sdk.business.buscard.base.traffic.readers.FMCardInfoReader;
import com.huawei.wallet.sdk.business.buscard.base.traffic.readers.InfoReader;
import com.huawei.wallet.sdk.business.buscard.base.traffic.readers.RideTimesInfoReader;
import com.huawei.wallet.sdk.business.buscard.base.util.AppletCardException;
import com.huawei.wallet.sdk.business.idcard.walletbase.util.LogX;
import com.huawei.wallet.sdk.common.apdu.IAPDUService;
import com.huawei.wallet.sdk.common.apdu.model.ApduCommand;
import com.huawei.wallet.sdk.common.apdu.model.ChannelID;
import com.huawei.wallet.sdk.common.utils.StringUtil;
import com.huawei.wallet.sdk.common.utils.TimeUtil;
import java.util.ArrayList;
import java.util.List;

public class TrafficCardInfoReader {
    private static final String TAG = "CardInfoRead";
    private static final Object WAKE_LOCK_SYNC = new Object();
    private static final int WAKE_LOCK_TIMEOUT = 180000;
    private static PowerManager.WakeLock wakeLock;
    private IAPDUService apduService;
    private ChannelID channelID;
    private ConfigData configData;
    private DataChecker dataChecker;
    private Context mContext;

    public TrafficCardInfoReader(Context context, IAPDUService iapduService, ConfigData configData2) {
        this.mContext = context;
        this.apduService = iapduService;
        this.configData = configData2;
    }

    public void setDataChecker(DataChecker dataChecker2) {
        this.dataChecker = dataChecker2;
    }

    public CardInfo readCardInfo(String aid, String productId, int dataType) throws AppletCardException {
        CardInfo cardInfo = new CardInfo();
        this.channelID = new ChannelID();
        LogX.i("readCardInfo ChannelType: 1");
        this.channelID.setChannelType(1);
        this.channelID.setAid(aid);
        checkCardStatus(productId);
        readByDataType(productId, dataType, cardInfo);
        closeChannel();
        return cardInfo;
    }

    public CardInfo readCardInfo(String productId, int dataType, IsoDep isoDep) throws AppletCardException {
        CardInfo cardInfo = new CardInfo();
        this.channelID = new ChannelID();
        this.channelID.setIsodep(isoDep);
        readByDataType(productId, dataType, cardInfo);
        return cardInfo;
    }

    private void readByDataType(String productId, int dataType, CardInfo cardInfo) throws AppletCardException {
        if ((dataType & 1) == 1) {
            readCardNum(productId, cardInfo);
        }
        if ((dataType & 4) == 4) {
            readCardDate(productId, cardInfo);
        }
        if ((dataType & 2) == 2) {
            readBalance(productId, cardInfo);
        }
        if ((dataType & 8) == 8) {
            readC8FileStatus(productId, cardInfo);
        }
        if ((dataType & 16) == 16) {
            readStationStatus(productId, cardInfo);
        }
        if ((dataType & 32) == 32) {
            readRideTimes(productId, cardInfo);
        }
        if ((dataType & 64) == 64) {
            readLogicNum(productId, cardInfo);
        }
    }

    private void readRideTimes(String productId, CardInfo cardInfo) throws AppletCardException {
        LogX.i("TrafficCardInfoReader readRideTimes, begin.");
        RideTimesInfoReader rideTimesInfoReader = new RideTimesInfoReader(this.apduService);
        rideTimesInfoReader.setChannelID(this.channelID);
        String[] rideTimesInfo = (String[]) readCardInfoImpl(productId, 10, rideTimesInfoReader);
        this.channelID = rideTimesInfoReader.getChannelID();
        String month = rideTimesInfo[0];
        String rideTimes = rideTimesInfo[1];
        String formatTime = TimeUtil.getFormatTime("yyMM");
        LogX.i("TrafficCardInfoReader readRideTimes, formatTime:" + formatTime + ", ride month:" + month);
        if (formatTime != null && !formatTime.equals(month)) {
            rideTimes = "00";
        }
        cardInfo.setRideMonth(month);
        cardInfo.setRideTimes(Integer.parseInt(rideTimes, 16));
    }

    public String readFMCardInfo(String aid, String productId) throws AppletCardException {
        this.channelID = new ChannelID();
        this.channelID.setAid(aid);
        checkCardStatus(productId);
        FMCardInfoReader fMCardInfoReader = new FMCardInfoReader(this.apduService);
        fMCardInfoReader.setChannelID(this.channelID);
        String fmCardInfo = (String) readCardInfoImpl(productId, 13, fMCardInfoReader);
        closeChannel();
        return fmCardInfo;
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r4v4, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v5, resolved type: java.util.List} */
    /* JADX WARNING: Multi-variable type inference failed */
    public List<TransactionRecord> readTransactionRecords(String aid, String productId) throws AppletCardException {
        this.channelID = new ChannelID();
        this.channelID.setAid(aid);
        checkCardStatus(productId);
        CardRecordInfoReader cardRecordInfoReader = new CardRecordInfoReader(this.apduService);
        cardRecordInfoReader.setChannelID(this.channelID);
        Log.i("consume_records", "begin read records ... productId = " + productId + " aid = " + aid);
        List<TransactionRecord> records = (List) readCardInfoImpl(productId, 4, cardRecordInfoReader);
        this.channelID = cardRecordInfoReader.getChannelID();
        List<TransactionRecord> consume_records = null;
        if (productId != null && productId.equals("p_xian_01")) {
            List<TransactionRecord> recordsnew = new ArrayList<>();
            if (records != null) {
                for (TransactionRecord tr : records) {
                    if (!recordsnew.contains(tr)) {
                        recordsnew.add(tr);
                    }
                }
                records.clear();
                records.addAll(recordsnew);
            }
            consume_records = readCardInfoImpl(productId, 11, cardRecordInfoReader);
        }
        closeChannel();
        if (consume_records != null) {
            if (records == null) {
                records = new ArrayList<>();
            }
            for (TransactionRecord tr2 : consume_records) {
                if (!records.contains(tr2)) {
                    records.add(tr2);
                }
            }
        }
        return records;
    }

    private void readCardNum(String productId, CardInfo info) throws AppletCardException {
        CardNumInfoReader cardNumInfoReader = new CardNumInfoReader(this.apduService);
        cardNumInfoReader.setChannelID(this.channelID);
        this.channelID = cardNumInfoReader.getChannelID();
        info.setCardNum((String) readCardInfoImpl(productId, 1, cardNumInfoReader));
    }

    private void readLogicNum(String productId, CardInfo info) throws AppletCardException {
        CardNumInfoReader cardNumInfoReader = new CardNumInfoReader(this.apduService);
        cardNumInfoReader.setChannelID(this.channelID);
        this.channelID = cardNumInfoReader.getChannelID();
        info.setLogicCardNum((String) readCardInfoImpl(productId, 12, cardNumInfoReader));
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r4v6, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v3, resolved type: java.lang.String[]} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r7v3, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v5, resolved type: java.lang.String[]} */
    /* JADX WARNING: Multi-variable type inference failed */
    private void readCardDate(String productId, CardInfo info) throws AppletCardException {
        String cardNumRapdu = null;
        List<Operation> operations = null;
        String[] dates = null;
        if (this.configData.isSameApduNumAndDate(productId)) {
            List<ApduCommand> apdus = this.configData.getApudList(productId, 1);
            ApduCommand command = apdus.get(apdus.size() - 1);
            cardNumRapdu = command != null ? command.getRapdu() : null;
            List<ApduCommand> apdus2 = this.configData.getApudList(productId, 2);
            ApduCommand command2 = apdus2.get(apdus2.size() - 1);
            if (command2 instanceof ApduCommandInfo) {
                operations = ((ApduCommandInfo) command2).getOperations();
            }
        }
        CardDateInfoReader cardDateInfoReader = new CardDateInfoReader(this.apduService);
        if (!StringUtil.isEmpty(cardNumRapdu, true) && operations != null) {
            dates = cardDateInfoReader.readInfoFromData(cardNumRapdu, operations);
        }
        if (dates == null) {
            cardDateInfoReader.setChannelID(this.channelID);
            dates = readCardInfoImpl(productId, 2, cardDateInfoReader);
            this.channelID = cardDateInfoReader.getChannelID();
        }
        info.setEnableDate(dates[0]);
        info.setExpireDate(dates[1]);
        if (this.dataChecker != null) {
            this.dataChecker.checkDate(info);
        }
    }

    private void readBalance(String productId, CardInfo info) throws AppletCardException {
        acquireWakeLock(this.mContext);
        CardBalanceInfoReader cardBalanceInfoReader = new CardBalanceInfoReader(this.apduService);
        cardBalanceInfoReader.setChannelID(this.channelID);
        Integer[] balance = (Integer[]) readCardInfoImpl(productId, 3, cardBalanceInfoReader);
        this.channelID = cardBalanceInfoReader.getChannelID();
        info.setOverdraftAmount(balance[0].intValue());
        info.setAmount(balance[1].intValue());
        if (this.dataChecker != null) {
            this.dataChecker.checkAmount(info);
        }
        releaseWakeLock();
    }

    private static void acquireWakeLock(Context mContext2) {
        synchronized (WAKE_LOCK_SYNC) {
            if (wakeLock == null) {
                LogX.i("TrafficCardInfoReader acquireWakeLock, wakeLock is null ,wake lock now.");
                wakeLock = ((PowerManager) mContext2.getSystemService("power")).newWakeLock(1, "beginWakeLock");
                wakeLock.setReferenceCounted(true);
            } else {
                LogX.i("TrafficCardInfoReader acquireWakeLock, wakeLock not null.");
            }
            wakeLock.acquire(180000);
            LogX.i("TrafficCardInfoReader acquireWakeLock, lock has been wake. WAKE_LOCK_TIMEOUT= 180000");
        }
    }

    private static void releaseWakeLock() {
        synchronized (WAKE_LOCK_SYNC) {
            if (wakeLock != null) {
                if (wakeLock.isHeld()) {
                    wakeLock.release();
                    LogX.i("TrafficCardInfoReader releaseWakeLock, wakeLock release. WAKE_LOCK_TIMEOUT= 180000");
                } else {
                    LogX.i("TrafficCardInfoReader releaseWakeLock, wakeLock not held. ");
                }
                wakeLock = null;
            } else {
                LogX.i("TrafficCardInfoReader releaseWakeLock, wakeLock is null. ");
            }
        }
    }

    private void readC8FileStatus(String productId, CardInfo info) throws AppletCardException {
        LogX.i("TrafficCardInfoReader readC8FileStatus, begin to read c8 file status.");
        acquireWakeLock(this.mContext);
        CardC8FileInfoReader cardC8FileInfoReader = new CardC8FileInfoReader(this.apduService);
        cardC8FileInfoReader.setChannelID(this.channelID);
        info.setC8FileStatus((Integer) readCardInfoImpl(productId, 6, cardC8FileInfoReader));
        releaseWakeLock();
    }

    private void readStationStatus(String productId, CardInfo info) throws AppletCardException {
        LogX.i("TrafficCardInfoReader readStationStatus, begin to read station status.");
        acquireWakeLock(this.mContext);
        CardStationStatusReader cardStationStatusReader = new CardStationStatusReader(this.apduService);
        cardStationStatusReader.setChannelID(this.channelID);
        info.setInOutStationStatus((Integer) readCardInfoImpl(productId, 7, cardStationStatusReader));
        releaseWakeLock();
    }

    private boolean checkCardStatus(String productId) throws AppletCardException {
        List<ApduCommand> apdus = this.configData.getApudList(productId, 0);
        if (apdus == null || apdus.isEmpty()) {
            return true;
        }
        CardStatusInfoReader cardStatusInfoReader = new CardStatusInfoReader(this.apduService);
        cardStatusInfoReader.setChannelID(this.channelID);
        this.channelID = cardStatusInfoReader.getChannelID();
        return ((Boolean) readCardInfoImpl(productId, 0, cardStatusInfoReader)).booleanValue();
    }

    private <T> T readCardInfoImpl(String productId, int type, InfoReader<T> infoReader) throws AppletCardException {
        infoReader.setCommandList(this.configData.getApudList(productId, type));
        try {
            return infoReader.readInfo();
        } catch (AppletCardException e) {
            LogX.i("TrafficCardInfoReader readCardInfoImpl, exception: " + e.getMessage());
            this.apduService.closeChannel(infoReader.getChannelID());
            throw new AppletCardException(e.getErrCode(), e.getMessage());
        }
    }

    public void closeChannel() {
        this.apduService.closeChannel(this.channelID);
        this.channelID = null;
    }
}
