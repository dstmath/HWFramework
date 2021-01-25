package com.android.internal.telephony.uicc.euicc.apdu;

import android.os.Handler;
import android.os.Looper;
import android.telephony.IccOpenLogicalChannelResponse;
import android.telephony.Rlog;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.PhoneConfigurationManager;
import com.android.internal.telephony.uicc.IccIoResult;
import com.android.internal.telephony.uicc.euicc.async.AsyncResultCallback;
import com.android.internal.telephony.uicc.euicc.async.AsyncResultHelper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

public class ApduSender {
    private static final int INS_GET_MORE_RESPONSE = 192;
    private static final String LOG_TAG = "ApduSender";
    private static final int STATUS_NO_ERROR = 36864;
    private static final int SW1_MORE_RESPONSE = 97;
    private static final int SW1_NO_ERROR_1 = 145;
    private static final int SW1_NO_ERROR_2 = 146;
    private static final int WAIT_TIME_MS = 2000;
    private final String mAid;
    private final Object mChannelLock = new Object();
    private boolean mChannelOpened;
    private final CloseLogicalChannelInvocation mCloseChannel;
    private final OpenLogicalChannelInvocation mOpenChannel;
    private final boolean mSupportExtendedApdu;
    private final TransmitApduLogicalChannelInvocation mTransmitApdu;

    /* access modifiers changed from: private */
    public static void logv(String msg) {
        Rlog.v(LOG_TAG, msg);
    }

    private static void logd(String msg) {
        Rlog.d(LOG_TAG, msg);
    }

    public ApduSender(CommandsInterface ci, String aid, boolean supportExtendedApdu) {
        this.mAid = aid;
        this.mSupportExtendedApdu = supportExtendedApdu;
        this.mOpenChannel = new OpenLogicalChannelInvocation(ci);
        this.mCloseChannel = new CloseLogicalChannelInvocation(ci);
        this.mTransmitApdu = new TransmitApduLogicalChannelInvocation(ci);
    }

    public void send(final RequestProvider requestProvider, final ApduSenderResultCallback resultCallback, final Handler handler) {
        synchronized (this.mChannelLock) {
            if (this.mChannelOpened) {
                if (!Looper.getMainLooper().equals(Looper.myLooper())) {
                    logd("Logical channel has already been opened. Wait.");
                    try {
                        this.mChannelLock.wait(2000);
                    } catch (InterruptedException e) {
                    }
                    if (this.mChannelOpened) {
                        AsyncResultHelper.throwException(new ApduException("The logical channel is still in use."), resultCallback, handler);
                        return;
                    }
                } else {
                    AsyncResultHelper.throwException(new ApduException("The logical channel is in use."), resultCallback, handler);
                    return;
                }
            }
            this.mChannelOpened = true;
            this.mOpenChannel.invoke(this.mAid, new AsyncResultCallback<IccOpenLogicalChannelResponse>() {
                /* class com.android.internal.telephony.uicc.euicc.apdu.ApduSender.AnonymousClass1 */

                public void onResult(IccOpenLogicalChannelResponse openChannelResponse) {
                    Throwable requestException;
                    int channel = openChannelResponse.getChannel();
                    int status = openChannelResponse.getStatus();
                    if (channel == -1 || status != 1) {
                        synchronized (ApduSender.this.mChannelLock) {
                            ApduSender.this.mChannelOpened = false;
                            ApduSender.this.mChannelLock.notify();
                        }
                        resultCallback.onException(new ApduException("Failed to open logical channel opened for AID: " + ApduSender.this.mAid + ", with status: " + status));
                        return;
                    }
                    RequestBuilder builder = new RequestBuilder(channel, ApduSender.this.mSupportExtendedApdu);
                    try {
                        requestProvider.buildRequest(openChannelResponse.getSelectResponse(), builder);
                        requestException = null;
                    } catch (Throwable e) {
                        requestException = e;
                    }
                    if (builder.getCommands().isEmpty() || requestException != null) {
                        ApduSender.this.closeAndReturn(channel, null, requestException, resultCallback, handler);
                    } else {
                        ApduSender.this.sendCommand(builder.getCommands(), 0, resultCallback, handler);
                    }
                }
            }, handler);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendCommand(final List<ApduCommand> commands, final int index, final ApduSenderResultCallback resultCallback, final Handler handler) {
        final ApduCommand command = commands.get(index);
        this.mTransmitApdu.invoke(command, new AsyncResultCallback<IccIoResult>() {
            /* class com.android.internal.telephony.uicc.euicc.apdu.ApduSender.AnonymousClass2 */

            public void onResult(IccIoResult response) {
                ApduSender.this.getCompleteResponse(command.channel, response, null, new AsyncResultCallback<IccIoResult>() {
                    /* class com.android.internal.telephony.uicc.euicc.apdu.ApduSender.AnonymousClass2.AnonymousClass1 */

                    public void onResult(IccIoResult fullResponse) {
                        ApduSender.logv("Full APDU response: " + fullResponse);
                        int status = (fullResponse.sw1 << 8) | fullResponse.sw2;
                        if (status == ApduSender.STATUS_NO_ERROR || fullResponse.sw1 == 145 || fullResponse.sw1 == 146) {
                            if (index < commands.size() - 1 && resultCallback.shouldContinueOnIntermediateResult(fullResponse)) {
                                ApduSender.this.sendCommand(commands, index + 1, resultCallback, handler);
                            } else {
                                ApduSender.this.closeAndReturn(command.channel, fullResponse.payload, null, resultCallback, handler);
                            }
                        } else {
                            ApduSender.this.closeAndReturn(command.channel, null, new ApduException(status), resultCallback, handler);
                        }
                    }
                }, handler);
            }
        }, handler);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void getCompleteResponse(final int channel, IccIoResult lastResponse, ByteArrayOutputStream responseBuilder, final AsyncResultCallback<IccIoResult> resultCallback, final Handler handler) {
        final ByteArrayOutputStream resultBuilder = responseBuilder == null ? new ByteArrayOutputStream() : responseBuilder;
        if (lastResponse.payload != null) {
            try {
                resultBuilder.write(lastResponse.payload);
            } catch (IOException e) {
            }
        }
        if (lastResponse.sw1 != 97) {
            lastResponse.payload = resultBuilder.toByteArray();
            resultCallback.onResult(lastResponse);
            return;
        }
        this.mTransmitApdu.invoke(new ApduCommand(channel, 0, 192, 0, 0, lastResponse.sw2, PhoneConfigurationManager.SSSS), new AsyncResultCallback<IccIoResult>() {
            /* class com.android.internal.telephony.uicc.euicc.apdu.ApduSender.AnonymousClass3 */

            public void onResult(IccIoResult response) {
                ApduSender.this.getCompleteResponse(channel, response, resultBuilder, resultCallback, handler);
            }
        }, handler);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void closeAndReturn(int channel, final byte[] response, final Throwable exception, final ApduSenderResultCallback resultCallback, Handler handler) {
        this.mCloseChannel.invoke(Integer.valueOf(channel), new AsyncResultCallback<Boolean>() {
            /* class com.android.internal.telephony.uicc.euicc.apdu.ApduSender.AnonymousClass4 */

            public void onResult(Boolean aBoolean) {
                synchronized (ApduSender.this.mChannelLock) {
                    ApduSender.this.mChannelOpened = false;
                    ApduSender.this.mChannelLock.notify();
                }
                Throwable th = exception;
                if (th == null) {
                    resultCallback.onResult(response);
                } else {
                    resultCallback.onException(th);
                }
            }
        }, handler);
    }
}
