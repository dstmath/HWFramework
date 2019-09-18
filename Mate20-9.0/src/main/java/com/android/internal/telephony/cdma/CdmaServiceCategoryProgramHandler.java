package com.android.internal.telephony.cdma;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.telephony.PhoneNumberUtils;
import android.telephony.SubscriptionManager;
import android.telephony.cdma.CdmaSmsCbProgramData;
import android.telephony.cdma.CdmaSmsCbProgramResults;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.WakeLockStateMachine;
import com.android.internal.telephony.cdma.sms.BearerData;
import com.android.internal.telephony.cdma.sms.CdmaSmsAddress;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public final class CdmaServiceCategoryProgramHandler extends WakeLockStateMachine {
    final CommandsInterface mCi;
    private final BroadcastReceiver mScpResultsReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            sendScpResults();
            CdmaServiceCategoryProgramHandler.this.log("mScpResultsReceiver finished");
            CdmaServiceCategoryProgramHandler.this.sendMessage(2);
        }

        private void sendScpResults() {
            int resultCode = getResultCode();
            if (resultCode == -1 || resultCode == 1) {
                Bundle extras = getResultExtras(false);
                if (extras == null) {
                    CdmaServiceCategoryProgramHandler.this.loge("SCP results error: missing extras");
                    return;
                }
                String sender = extras.getString("sender");
                if (sender == null) {
                    CdmaServiceCategoryProgramHandler.this.loge("SCP results error: missing sender extra.");
                    return;
                }
                ArrayList<CdmaSmsCbProgramResults> results = extras.getParcelableArrayList("results");
                if (results == null) {
                    CdmaServiceCategoryProgramHandler.this.loge("SCP results error: missing results extra.");
                    return;
                }
                BearerData bData = new BearerData();
                bData.messageType = 2;
                bData.messageId = SmsMessage.getNextMessageId();
                bData.serviceCategoryProgramResults = results;
                byte[] encodedBearerData = BearerData.encode(bData);
                ByteArrayOutputStream baos = new ByteArrayOutputStream(100);
                DataOutputStream dos = new DataOutputStream(baos);
                try {
                    dos.writeInt(4102);
                    dos.writeInt(0);
                    dos.writeInt(0);
                    CdmaSmsAddress destAddr = CdmaSmsAddress.parse(PhoneNumberUtils.cdmaCheckAndProcessPlusCodeForSms(sender));
                    dos.write(destAddr.digitMode);
                    dos.write(destAddr.numberMode);
                    dos.write(destAddr.ton);
                    dos.write(destAddr.numberPlan);
                    dos.write(destAddr.numberOfDigits);
                    dos.write(destAddr.origBytes, 0, destAddr.origBytes.length);
                    dos.write(0);
                    dos.write(0);
                    dos.write(0);
                    dos.write(encodedBearerData.length);
                    dos.write(encodedBearerData, 0, encodedBearerData.length);
                    CdmaServiceCategoryProgramHandler.this.mCi.sendCdmaSms(baos.toByteArray(), null);
                    try {
                        dos.close();
                    } catch (IOException e) {
                    }
                } catch (IOException e2) {
                    CdmaServiceCategoryProgramHandler.this.loge("exception creating SCP results PDU", e2);
                    dos.close();
                } catch (Throwable th) {
                    try {
                        dos.close();
                    } catch (IOException e3) {
                    }
                    throw th;
                }
                return;
            }
            CdmaServiceCategoryProgramHandler cdmaServiceCategoryProgramHandler = CdmaServiceCategoryProgramHandler.this;
            cdmaServiceCategoryProgramHandler.loge("SCP results error: result code = " + resultCode);
        }
    };

    CdmaServiceCategoryProgramHandler(Context context, CommandsInterface commandsInterface) {
        super("CdmaServiceCategoryProgramHandler", context, null);
        this.mContext = context;
        this.mCi = commandsInterface;
    }

    static CdmaServiceCategoryProgramHandler makeScpHandler(Context context, CommandsInterface commandsInterface) {
        CdmaServiceCategoryProgramHandler handler = new CdmaServiceCategoryProgramHandler(context, commandsInterface);
        handler.start();
        return handler;
    }

    /* access modifiers changed from: protected */
    public boolean handleSmsMessage(Message message) {
        if (message.obj instanceof SmsMessage) {
            return handleServiceCategoryProgramData((SmsMessage) message.obj);
        }
        loge("handleMessage got object of type: " + message.obj.getClass().getName());
        return false;
    }

    private boolean handleServiceCategoryProgramData(SmsMessage sms) {
        ArrayList<CdmaSmsCbProgramData> programDataList = sms.getSmsCbProgramData();
        if (programDataList == null) {
            loge("handleServiceCategoryProgramData: program data list is null!");
            return false;
        }
        Intent intent = new Intent("android.provider.Telephony.SMS_SERVICE_CATEGORY_PROGRAM_DATA_RECEIVED");
        intent.setPackage(this.mContext.getResources().getString(17039785));
        intent.putExtra("sender", sms.getOriginatingAddress());
        intent.putParcelableArrayListExtra("program_data", programDataList);
        SubscriptionManager.putPhoneIdAndSubIdExtra(intent, this.mPhone.getPhoneId());
        this.mContext.sendOrderedBroadcast(intent, "android.permission.RECEIVE_SMS", 16, this.mScpResultsReceiver, getHandler(), -1, null, null);
        return true;
    }
}
