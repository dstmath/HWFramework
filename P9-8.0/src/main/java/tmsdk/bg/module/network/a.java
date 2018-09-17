package tmsdk.bg.module.network;

import android.content.Context;
import java.util.ArrayList;
import java.util.Iterator;
import tmsdk.common.TMSDKContext;
import tmsdk.common.TMServiceFactory;
import tmsdk.common.creator.ManagerCreatorC;
import tmsdk.common.module.network.TrafficEntity;
import tmsdk.common.utils.i;
import tmsdkobf.eb;
import tmsdkobf.jd;
import tmsdkobf.jx;
import tmsdkobf.ov;
import tmsdkobf.pg;
import tmsdkobf.ph;

final class a {
    private Context mContext = TMSDKContext.getApplicaionContext();
    private jx un = jd.b(this.mContext, "traffic_xml");
    private k uo = new k();

    public a() {
        ((ph) ManagerCreatorC.getManager(ph.class)).c(new pg() {
            private void ba(String str) {
                a.this.clearTrafficInfo(new String[]{str});
            }

            public void aQ(String str) {
                ba(str);
            }

            public void aR(String str) {
            }

            public void aS(String str) {
            }
        });
    }

    /* JADX WARNING: Missing block: B:19:0x0050, code:
            if ((r17.mWIFIUpValue <= 0 ? 1 : null) == null) goto L_0x0052;
     */
    /* JADX WARNING: Missing block: B:30:0x0072, code:
            if ((r17.mWIFIDownValue <= 0 ? 1 : null) == null) goto L_0x0074;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private TrafficEntity a(TrafficEntity trafficEntity, String str, String str2) {
        int aZ = aZ(str);
        Object obj = null;
        if (trafficEntity == null && aZ != -1) {
            trafficEntity = new TrafficEntity();
            trafficEntity.mPkg = str;
            obj = 1;
        }
        if (trafficEntity != null) {
            if (str2 == null) {
                str2 = this.un.getString("last_connection_type", null);
            }
            long uidTxBytes = this.uo.getUidTxBytes(aZ);
            long uidRxBytes = this.uo.getUidRxBytes(aZ);
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
            if ((j >= 0 ? 1 : null) == null) {
                j = uidTxBytes;
            }
            if ((j2 >= 0 ? 1 : null) == null) {
                j2 = uidTxBytes;
            }
            if (uidTxBytes == -1 && uidRxBytes == -1) {
                trafficEntity.mMobileDownValue = 0;
                trafficEntity.mMobileUpValue = 0;
                trafficEntity.mWIFIDownValue = 0;
                trafficEntity.mWIFIUpValue = 0;
            } else if (uidTxBytes != -1 || uidRxBytes == -1) {
                long j3;
                if (uidTxBytes == -1 || uidRxBytes != -1) {
                    if (str2.equals("mobile")) {
                        trafficEntity.mMobileDownValue += j2;
                        j3 = trafficEntity.mMobileUpValue;
                        trafficEntity.mMobileUpValue = j3 + j;
                    } else {
                        if (str2.equals("wifi")) {
                            trafficEntity.mWIFIDownValue += j2;
                            j3 = trafficEntity.mWIFIUpValue;
                        }
                    }
                } else {
                    trafficEntity.mMobileDownValue = 0;
                    trafficEntity.mWIFIDownValue = 0;
                    if (str2.equals("mobile")) {
                        j3 = trafficEntity.mMobileUpValue;
                        trafficEntity.mMobileUpValue = j3 + j;
                    } else {
                        if (str2.equals("wifi")) {
                            j3 = trafficEntity.mWIFIUpValue;
                        }
                    }
                }
                trafficEntity.mWIFIUpValue = j3 + j;
            } else {
                trafficEntity.mMobileUpValue = 0;
                trafficEntity.mWIFIUpValue = 0;
                if (str2.equals("mobile")) {
                    trafficEntity.mMobileDownValue += j2;
                } else {
                    if (str2.equals("wifi")) {
                        trafficEntity.mWIFIDownValue += j2;
                    }
                }
            }
            trafficEntity.mLastUpValue = uidTxBytes;
            trafficEntity.mLastDownValue = uidRxBytes;
            this.un.putString(str, trafficEntity.toString());
        }
        return trafficEntity;
    }

    private int aZ(String str) {
        ov a = TMServiceFactory.getSystemInfoService().a(str, 1);
        return a == null ? -1 : a.getUid();
    }

    public void clearTrafficInfo(String[] -l_2_R) {
        for (String str : -l_2_R) {
            int aZ = aZ(str);
            TrafficEntity trafficEntity = getTrafficEntity(str);
            if (aZ == -1 || trafficEntity == null) {
                this.un.putString(str, "EMPTY");
            } else {
                trafficEntity.mLastUpValue = this.uo.getUidTxBytes(aZ);
                trafficEntity.mLastDownValue = this.uo.getUidRxBytes(aZ);
                trafficEntity.mMobileDownValue = 0;
                trafficEntity.mMobileUpValue = 0;
                trafficEntity.mWIFIDownValue = 0;
                trafficEntity.mWIFIUpValue = 0;
                this.un.putString(str, trafficEntity.toString());
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
        int aZ = aZ(str);
        String string = this.un.getString(str, null);
        return (aZ == -1 || string == null || "EMPTY".equals(string)) ? null : TrafficEntity.fromString(string);
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
        return this.uo.isSupportTrafficState();
    }

    public ArrayList<TrafficEntity> refreshTrafficInfo(String[] strArr, boolean z) {
        ArrayList<TrafficEntity> arrayList = new ArrayList();
        eb iG = i.iG();
        String str = iG != eb.iJ ? iG != eb.iH ? "mobile" : "none" : "wifi";
        String string = this.un.getString("last_connection_type", null);
        if (string != null) {
            String str2 = str;
            if (str.equals(string) && !z) {
                return arrayList;
            }
        }
        string = str;
        for (int i = 0; i < strArr.length; i++) {
            TrafficEntity a = a(getTrafficEntity(strArr[i]), strArr[i], string);
            if (a != null) {
                arrayList.add(a);
            }
        }
        this.un.putString("last_connection_type", str);
        return arrayList;
    }

    public void refreshTrafficInfo(ArrayList<TrafficEntity> arrayList) {
        if (arrayList != null && !arrayList.isEmpty()) {
            eb iG = i.iG();
            String str = iG != eb.iJ ? iG != eb.iH ? "mobile" : "none" : "wifi";
            String string = this.un.getString("last_connection_type", null);
            if (string == null) {
                string = str;
            }
            Iterator it = arrayList.iterator();
            while (it.hasNext()) {
                TrafficEntity trafficEntity = (TrafficEntity) it.next();
                a(trafficEntity, trafficEntity.mPkg, string);
            }
            this.un.putString("last_connection_type", str);
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
