package tmsdk.bg.module.network;

import android.content.Context;
import java.util.ArrayList;
import java.util.Iterator;
import tmsdk.common.TMSDKContext;
import tmsdk.common.TMServiceFactory;
import tmsdk.common.creator.ManagerCreatorC;
import tmsdk.common.module.network.TrafficEntity;
import tmsdk.common.utils.f;
import tmsdkobf.cz;
import tmsdkobf.kk;
import tmsdkobf.lf;
import tmsdkobf.py;
import tmsdkobf.qj;
import tmsdkobf.qk;

/* compiled from: Unknown */
final class a {
    private Context mContext;
    private lf xj;
    private k xk;

    public a() {
        this.mContext = TMSDKContext.getApplicaionContext();
        this.xj = kk.a(this.mContext, "traffic_xml");
        this.xk = new k();
        ((qk) ManagerCreatorC.getManager(qk.class)).c(new qj() {
            final /* synthetic */ a xl;

            {
                this.xl = r1;
            }

            private void bY(String str) {
                this.xl.clearTrafficInfo(new String[]{str});
            }

            public void bQ(String str) {
                bY(str);
            }

            public void bR(String str) {
            }

            public void bS(String str) {
            }
        });
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private TrafficEntity a(TrafficEntity trafficEntity, String str, String str2) {
        int bX = bX(str);
        if (trafficEntity == null && bX != -1) {
            trafficEntity = new TrafficEntity();
            trafficEntity.mPkg = str;
            int i = 1;
        } else {
            Object obj = null;
        }
        if (trafficEntity != null) {
            if (str2 == null) {
                str2 = this.xj.getString("last_connection_type", null);
            }
            long uidTxBytes = this.xk.getUidTxBytes(bX);
            long uidRxBytes = this.xk.getUidRxBytes(bX);
            if (obj != null) {
                trafficEntity.mLastDownValue = uidRxBytes;
                trafficEntity.mLastUpValue = uidTxBytes;
            }
            if (uidTxBytes == -1) {
                if ((trafficEntity.mMobileUpValue > 0 ? 1 : null) == null) {
                }
                uidTxBytes = 0;
            }
            if (uidRxBytes == -1) {
                if ((trafficEntity.mMobileDownValue > 0 ? 1 : null) == null) {
                }
                uidRxBytes = 0;
            }
            long j = uidTxBytes - trafficEntity.mLastUpValue;
            long j2 = uidRxBytes - trafficEntity.mLastDownValue;
            long j3 = ((j > 0 ? 1 : (j == 0 ? 0 : -1)) >= 0 ? 1 : null) == null ? uidTxBytes : j;
            if ((j2 >= 0 ? 1 : null) == null) {
                j2 = uidTxBytes;
            }
            if (uidTxBytes == -1 && uidRxBytes == -1) {
                trafficEntity.mMobileDownValue = 0;
                trafficEntity.mMobileUpValue = 0;
                trafficEntity.mWIFIDownValue = 0;
                trafficEntity.mWIFIUpValue = 0;
            } else if (uidTxBytes != -1 || uidRxBytes == -1) {
                if (uidTxBytes != -1 && uidRxBytes == -1) {
                    trafficEntity.mMobileDownValue = 0;
                    trafficEntity.mWIFIDownValue = 0;
                    if (str2.equals("mobile")) {
                        j2 = trafficEntity.mMobileUpValue;
                        trafficEntity.mMobileUpValue = j2 + j3;
                    } else if (str2.equals("wifi")) {
                        j2 = trafficEntity.mWIFIUpValue;
                    }
                } else if (str2.equals("mobile")) {
                    trafficEntity.mMobileDownValue = j2 + trafficEntity.mMobileDownValue;
                    j2 = trafficEntity.mMobileUpValue;
                    trafficEntity.mMobileUpValue = j2 + j3;
                } else if (str2.equals("wifi")) {
                    trafficEntity.mWIFIDownValue = j2 + trafficEntity.mWIFIDownValue;
                    j2 = trafficEntity.mWIFIUpValue;
                }
                trafficEntity.mWIFIUpValue = j2 + j3;
            } else {
                trafficEntity.mMobileUpValue = 0;
                trafficEntity.mWIFIUpValue = 0;
                if (str2.equals("mobile")) {
                    trafficEntity.mMobileDownValue = j2 + trafficEntity.mMobileDownValue;
                } else if (str2.equals("wifi")) {
                    trafficEntity.mWIFIDownValue = j2 + trafficEntity.mWIFIDownValue;
                }
            }
            trafficEntity.mLastUpValue = uidTxBytes;
            trafficEntity.mLastDownValue = uidRxBytes;
            this.xj.m(str, trafficEntity.toString());
        }
        return trafficEntity;
    }

    private int bX(String str) {
        py b = TMServiceFactory.getSystemInfoService().b(str, 1);
        return b == null ? -1 : b.getUid();
    }

    public void clearTrafficInfo(String[] strArr) {
        for (String str : strArr) {
            int bX = bX(str);
            TrafficEntity trafficEntity = getTrafficEntity(str);
            if (bX == -1 || trafficEntity == null) {
                this.xj.m(str, "EMPTY");
            } else {
                trafficEntity.mLastUpValue = this.xk.getUidTxBytes(bX);
                trafficEntity.mLastDownValue = this.xk.getUidRxBytes(bX);
                trafficEntity.mMobileDownValue = 0;
                trafficEntity.mMobileUpValue = 0;
                trafficEntity.mWIFIDownValue = 0;
                trafficEntity.mWIFIUpValue = 0;
                this.xj.m(str, trafficEntity.toString());
            }
        }
    }

    public long getMobileRxBytes(String str) {
        TrafficEntity trafficEntity = getTrafficEntity(str);
        return trafficEntity == null ? -1 : trafficEntity.mMobileDownValue;
    }

    public long getMobileTxBytes(String str) {
        TrafficEntity trafficEntity = getTrafficEntity(str);
        return trafficEntity == null ? -1 : trafficEntity.mMobileUpValue;
    }

    public TrafficEntity getTrafficEntity(String str) {
        int bX = bX(str);
        String string = this.xj.getString(str, null);
        return (bX == -1 || string == null || "EMPTY".equals(string)) ? null : TrafficEntity.fromString(string);
    }

    public long getWIFIRxBytes(String str) {
        TrafficEntity trafficEntity = getTrafficEntity(str);
        return trafficEntity == null ? -1 : trafficEntity.mWIFIDownValue;
    }

    public long getWIFITxBytes(String str) {
        TrafficEntity trafficEntity = getTrafficEntity(str);
        return trafficEntity == null ? -1 : trafficEntity.mWIFIUpValue;
    }

    public boolean isSupportTrafficState() {
        return this.xk.isSupportTrafficState();
    }

    public ArrayList<TrafficEntity> refreshTrafficInfo(String[] strArr, boolean z) {
        int i = 0;
        ArrayList<TrafficEntity> arrayList = new ArrayList();
        cz iw = f.iw();
        String str = iw != cz.gD ? iw != cz.gB ? "mobile" : "none" : "wifi";
        String string = this.xj.getString("last_connection_type", null);
        if (string == null) {
            string = str;
        } else if (str.equals(string) && !z) {
            return arrayList;
        }
        while (i < strArr.length) {
            TrafficEntity a = a(getTrafficEntity(strArr[i]), strArr[i], string);
            if (a != null) {
                arrayList.add(a);
            }
            i++;
        }
        this.xj.m("last_connection_type", str);
        return arrayList;
    }

    public void refreshTrafficInfo(ArrayList<TrafficEntity> arrayList) {
        if (arrayList != null && !arrayList.isEmpty()) {
            cz iw = f.iw();
            String str = iw != cz.gD ? iw != cz.gB ? "mobile" : "none" : "wifi";
            String str2 = str;
            str = this.xj.getString("last_connection_type", null);
            String str3 = str != null ? str : str2;
            Iterator it = arrayList.iterator();
            while (it.hasNext()) {
                TrafficEntity trafficEntity = (TrafficEntity) it.next();
                a(trafficEntity, trafficEntity.mPkg, str3);
            }
            this.xj.m("last_connection_type", str2);
        }
    }

    public void refreshTrafficInfo(TrafficEntity trafficEntity) {
        if (trafficEntity != null) {
            ArrayList arrayList = new ArrayList();
            arrayList.add(trafficEntity);
            refreshTrafficInfo(arrayList);
        }
    }
}
