package com.huawei.android.telephony;

import android.os.PersistableBundle;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import com.huawei.annotation.HwSystemApi;

public class SignalStrengthEx {
    @HwSystemApi
    public static final int INVALID = Integer.MAX_VALUE;
    @HwSystemApi
    public static final int MIN_NR_RSRP = -140;
    private static final int RADIO_TECH_GSM = 1;
    private static final int RADIO_TECH_LTE = 4;
    private static final int RADIO_TECH_LTECA = 7;
    private static final int RADIO_TECH_TDS = 3;
    private static final int RADIO_TECH_WCDMA = 2;
    public static final int SIGNAL_STRENGTH_EXCELLENT = 5;
    public static final int SIGNAL_STRENGTH_GOOD = 3;
    public static final int SIGNAL_STRENGTH_GREAT = 4;
    public static final int SIGNAL_STRENGTH_MODERATE = 2;
    public static final int SIGNAL_STRENGTH_NONE_OR_UNKNOWN = 0;
    public static final int SIGNAL_STRENGTH_POOR = 1;

    public static int getLteRsrp(SignalStrength signal) {
        if (signal != null) {
            return signal.getLteRsrp();
        }
        return INVALID;
    }

    public static int getLteRssnr(SignalStrength signal) {
        if (signal != null) {
            return signal.getLteRssnr();
        }
        return INVALID;
    }

    public static int getWcdmaRscp(SignalStrength signal) {
        if (signal != null) {
            return signal.getWcdmaRscp();
        }
        return INVALID;
    }

    public static int getWcdmaEcio(SignalStrength signal) {
        if (signal != null) {
            return signal.getWcdmaEcio();
        }
        return INVALID;
    }

    public static int getEvdoLevel(SignalStrength signal) {
        if (signal != null) {
            return signal.getEvdoLevel();
        }
        return 0;
    }

    public static int getCdmaLevel(SignalStrength signal) {
        if (signal != null) {
            return signal.getCdmaLevel();
        }
        return 0;
    }

    public static int getNrLevel(SignalStrength signal) {
        if (signal != null) {
            return signal.getNrLevel();
        }
        return 0;
    }

    public static int getNrAsuLevel(SignalStrength signal) {
        if (signal != null) {
            return signal.getNrAsuLevel();
        }
        return 0;
    }

    public static int getNrSignalStrength(SignalStrength signal) {
        if (signal != null) {
            return signal.getNrSignalStrength();
        }
        return INVALID;
    }

    public static int getNrRsrp(SignalStrength signal) {
        if (signal != null) {
            return signal.getNrRsrp();
        }
        return INVALID;
    }

    public static int getNrRssnr(SignalStrength signal) {
        if (signal != null) {
            return signal.getNrRssnr();
        }
        return INVALID;
    }

    public static int getNrCqi(SignalStrength signal) {
        if (signal != null) {
            return signal.getNrCqi();
        }
        return INVALID;
    }

    public static int getNrDbm(SignalStrength signal) {
        if (signal != null) {
            return signal.getNrDbm();
        }
        return INVALID;
    }

    @HwSystemApi
    public static int getLteRsrq(SignalStrength signal) {
        if (signal != null) {
            return signal.getLteRsrq();
        }
        return INVALID;
    }

    @HwSystemApi
    public static boolean isCdma(SignalStrength signal) {
        if (signal == null) {
            return false;
        }
        signal.isCdma();
        return false;
    }

    @HwSystemApi
    public static int getGsmDbm(SignalStrength signal) {
        if (signal != null) {
            return signal.getGsmDbm();
        }
        return INVALID;
    }

    @HwSystemApi
    public static int getLteSignalStrength(SignalStrength signal) {
        if (signal != null) {
            return signal.getLteSignalStrength();
        }
        return INVALID;
    }

    @HwSystemApi
    public static void setGsmSignalStrength(SignalStrength signal, int gsmSignalStrength) {
        if (signal != null) {
            signal.setGsmSignalStrength(gsmSignalStrength);
        }
    }

    @HwSystemApi
    public static void setWcdmaRscp(SignalStrength signal, int wcdmaRscp) {
        if (signal != null) {
            signal.setWcdmaRscp(wcdmaRscp);
        }
    }

    @HwSystemApi
    public static void setWcdmaEcio(SignalStrength signal, int wcdmaEcio) {
        if (signal != null) {
            signal.setWcdmaEcio(wcdmaEcio);
        }
    }

    @HwSystemApi
    public static void setCdmaDbm(SignalStrength signal, int cdmaDbm) {
        if (signal != null) {
            signal.setCdmaDbm(cdmaDbm);
        }
    }

    @HwSystemApi
    public static void setCdmaEcio(SignalStrength signal, int cdmaEcio) {
        if (signal != null) {
            signal.setCdmaEcio(cdmaEcio);
        }
    }

    @HwSystemApi
    public static void setEvdoDbm(SignalStrength signal, int evdoDbm) {
        if (signal != null) {
            signal.setEvdoDbm(evdoDbm);
        }
    }

    @HwSystemApi
    public static void setEvdoEcio(SignalStrength signal, int evdoEcio) {
        if (signal != null) {
            signal.setEvdoEcio(evdoEcio);
        }
    }

    @HwSystemApi
    public static void setEvdoSnr(SignalStrength signal, int evdoSnr) {
        if (signal != null) {
            signal.setEvdoSnr(evdoSnr);
        }
    }

    @HwSystemApi
    public static void setLteSignalStrength(SignalStrength signal, int lteSignalStrength) {
        if (signal != null) {
            signal.setLteSignalStrength(lteSignalStrength);
        }
    }

    @HwSystemApi
    public static void setLteRsrp(SignalStrength signal, int lteRsrp) {
        if (signal != null) {
            signal.setLteRsrp(lteRsrp);
        }
    }

    @HwSystemApi
    public static void setLteRsrq(SignalStrength signal, int lteRsrq) {
        if (signal != null) {
            signal.setLteRsrq(lteRsrq);
        }
    }

    @HwSystemApi
    public static void setLteRssnr(SignalStrength signal, int lteRssnr) {
        if (signal != null) {
            signal.setLteRssnr(lteRssnr);
        }
    }

    @HwSystemApi
    public static void setNrRsrp(SignalStrength signal, int nrRsrp) {
        if (signal != null) {
            signal.setNrRsrp(nrRsrp);
        }
    }

    @HwSystemApi
    public static void setNrRsrq(SignalStrength signal, int nrRsrq) {
        if (signal != null) {
            signal.setNrRsrq(nrRsrq);
        }
    }

    @HwSystemApi
    public static void setNrRssnr(SignalStrength signal, int nrRssnr) {
        if (signal != null) {
            signal.setNrRssnr(nrRssnr);
        }
    }

    @HwSystemApi
    public static int getPhoneId(SignalStrength signal) {
        if (signal != null) {
            return signal.getPhoneId();
        }
        return -1;
    }

    public static int getRatLevel(SignalStrength signal, int rat) {
        if (signal == null) {
            return 0;
        }
        if (rat == 1) {
            return signal.getGsmLevel();
        }
        if (rat == 2) {
            return signal.getWcdmaLevel();
        }
        if (rat == 3) {
            return signal.getTdScdmaLevel();
        }
        if (rat == 4 || rat == 7) {
            return signal.getLteLevel();
        }
        return 0;
    }

    @HwSystemApi
    public static int getPrimaryLevelHw(SignalStrength signal) {
        if (signal != null) {
            return signal.getPrimaryHw().getLevelHw();
        }
        return 0;
    }

    @HwSystemApi
    public static void clearNrSiganlStrength(SignalStrength signal) {
        if (signal != null) {
            signal.clearNrSiganlStrength();
        }
    }

    @HwSystemApi
    public static void updateLevel(SignalStrength signal, PersistableBundle cc, ServiceState ss) {
        if (signal != null) {
            signal.updateLevel(cc, ss);
        }
    }

    @HwSystemApi
    public static void setPhoneId(SignalStrength signal, int phoneId) {
        if (signal != null) {
            signal.setPhoneId(phoneId);
        }
    }

    @HwSystemApi
    public static int getHwNrRsrp(SignalStrength signal) {
        if (signal != null) {
            return signal.getHwNrRsrp();
        }
        return INVALID;
    }

    @HwSystemApi
    public static SignalStrength newSignalStrength(SignalStrength signalStrength) {
        return new SignalStrength(signalStrength);
    }

    @HwSystemApi
    public static int getDbm(SignalStrength signalStrength) {
        if (signalStrength != null) {
            return signalStrength.getDbm();
        }
        return INVALID;
    }

    @HwSystemApi
    public static void setCdma(SignalStrength signalStrength, boolean cdmaFlag) {
        if (signalStrength != null) {
            signalStrength.setCdma(cdmaFlag);
        }
    }
}
