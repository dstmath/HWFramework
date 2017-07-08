package com.android.server.location.gnsschrlog;

import java.util.ArrayList;
import java.util.List;

public class CSegEVENT_GEOLOCATION_DATA_COLLECT_EVENT extends ChrLogBaseEventModel {
    public List<CSubCurrentCell> cCurrentCellList;
    public List<CSubNeighborCell> cNeighborCellList;
    public List<CSubWifiApInfo> cWifiApInfoList;
    public ENCEventId enEventId;
    public LogLong lBootTime;
    public LogLong lwifi_scaned_time;
    public LogString strAccuracy;
    public LogString strBearing;
    public LogString strLatitude;
    public LogString strLongitude;
    public LogString strSpeed;
    public LogString strlocation_time;
    public LogDate tmTimeStamp;
    public LogByte ucCardIndex;
    public LogByte ucErrorCode;
    public LogByte ucType;
    public LogShort usLen;

    public void setCSubCurrentCellList(CSubCurrentCell pCurrentCell) {
        if (pCurrentCell != null) {
            this.cCurrentCellList.add(pCurrentCell);
            this.lengthMap.put("cCurrentCellList", Integer.valueOf((((ChrLogBaseModel) this.cCurrentCellList.get(0)).getTotalBytes() * this.cCurrentCellList.size()) + 2));
            this.fieldMap.put("cCurrentCellList", this.cCurrentCellList);
            this.usLen.setValue(getTotalLen());
        }
    }

    public void setCSubNeighborCellList(CSubNeighborCell pNeighborCell) {
        if (pNeighborCell != null) {
            this.cNeighborCellList.add(pNeighborCell);
            this.lengthMap.put("cNeighborCellList", Integer.valueOf((((ChrLogBaseModel) this.cNeighborCellList.get(0)).getTotalBytes() * this.cNeighborCellList.size()) + 2));
            this.fieldMap.put("cNeighborCellList", this.cNeighborCellList);
            this.usLen.setValue(getTotalLen());
        }
    }

    public void setCSubWifiApInfoList(CSubWifiApInfo pWifiApInfo) {
        if (pWifiApInfo != null) {
            this.cWifiApInfoList.add(pWifiApInfo);
            this.lengthMap.put("cWifiApInfoList", Integer.valueOf((((ChrLogBaseModel) this.cWifiApInfoList.get(0)).getTotalBytes() * this.cWifiApInfoList.size()) + 2));
            this.fieldMap.put("cWifiApInfoList", this.cWifiApInfoList);
            this.usLen.setValue(getTotalLen());
        }
    }

    public CSegEVENT_GEOLOCATION_DATA_COLLECT_EVENT() {
        this.enEventId = new ENCEventId();
        this.usLen = new LogShort();
        this.tmTimeStamp = new LogDate(6);
        this.ucCardIndex = new LogByte();
        this.ucErrorCode = new LogByte();
        this.ucType = new LogByte();
        this.strLatitude = new LogString(12);
        this.strLongitude = new LogString(12);
        this.strAccuracy = new LogString(12);
        this.strBearing = new LogString(12);
        this.strlocation_time = new LogString(12);
        this.strSpeed = new LogString(12);
        this.cCurrentCellList = new ArrayList(8);
        this.cNeighborCellList = new ArrayList(8);
        this.lwifi_scaned_time = new LogLong();
        this.cWifiApInfoList = new ArrayList(8);
        this.lBootTime = new LogLong();
        this.lengthMap.put("enEventId", Integer.valueOf(1));
        this.fieldMap.put("enEventId", this.enEventId);
        this.lengthMap.put("usLen", Integer.valueOf(2));
        this.fieldMap.put("usLen", this.usLen);
        this.lengthMap.put("tmTimeStamp", Integer.valueOf(6));
        this.fieldMap.put("tmTimeStamp", this.tmTimeStamp);
        this.lengthMap.put("ucCardIndex", Integer.valueOf(1));
        this.fieldMap.put("ucCardIndex", this.ucCardIndex);
        this.lengthMap.put("ucErrorCode", Integer.valueOf(1));
        this.fieldMap.put("ucErrorCode", this.ucErrorCode);
        this.lengthMap.put("ucType", Integer.valueOf(1));
        this.fieldMap.put("ucType", this.ucType);
        this.lengthMap.put("strLatitude", Integer.valueOf(12));
        this.fieldMap.put("strLatitude", this.strLatitude);
        this.lengthMap.put("strLongitude", Integer.valueOf(12));
        this.fieldMap.put("strLongitude", this.strLongitude);
        this.lengthMap.put("strAccuracy", Integer.valueOf(12));
        this.fieldMap.put("strAccuracy", this.strAccuracy);
        this.lengthMap.put("strBearing", Integer.valueOf(12));
        this.fieldMap.put("strBearing", this.strBearing);
        this.lengthMap.put("strlocation_time", Integer.valueOf(12));
        this.fieldMap.put("strlocation_time", this.strlocation_time);
        this.lengthMap.put("strSpeed", Integer.valueOf(12));
        this.fieldMap.put("strSpeed", this.strSpeed);
        this.lengthMap.put("cCurrentCellList", Integer.valueOf(2));
        this.fieldMap.put("cCurrentCellList", this.cCurrentCellList);
        this.lengthMap.put("cNeighborCellList", Integer.valueOf(2));
        this.fieldMap.put("cNeighborCellList", this.cNeighborCellList);
        this.lengthMap.put("lwifi_scaned_time", Integer.valueOf(8));
        this.fieldMap.put("lwifi_scaned_time", this.lwifi_scaned_time);
        this.lengthMap.put("cWifiApInfoList", Integer.valueOf(2));
        this.fieldMap.put("cWifiApInfoList", this.cWifiApInfoList);
        this.lengthMap.put("lBootTime", Integer.valueOf(8));
        this.fieldMap.put("lBootTime", this.lBootTime);
        this.enEventId.setValue("GEOLOCATION_DATA_COLLECT_EVENT");
        this.usLen.setValue(getTotalLen());
    }
}
