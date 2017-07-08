package com.android.server.location.gnsschrlog;

import java.util.ArrayList;
import java.util.List;

public class CSubMainCardCellInfo extends ChrLogBaseModel {
    public List<CSubCurrentCell> cCurrentCellList;
    public List<CSubNeighborCell> cNeighborCellList;
    public ENCSubEventId enSubEventId;

    public void setCSubCurrentCellList(CSubCurrentCell pCurrentCell) {
        if (pCurrentCell != null) {
            this.cCurrentCellList.add(pCurrentCell);
            this.lengthMap.put("cCurrentCellList", Integer.valueOf((((ChrLogBaseModel) this.cCurrentCellList.get(0)).getTotalBytes() * this.cCurrentCellList.size()) + 2));
            this.fieldMap.put("cCurrentCellList", this.cCurrentCellList);
        }
    }

    public void setCSubNeighborCellList(CSubNeighborCell pNeighborCell) {
        if (pNeighborCell != null) {
            this.cNeighborCellList.add(pNeighborCell);
            this.lengthMap.put("cNeighborCellList", Integer.valueOf((((ChrLogBaseModel) this.cNeighborCellList.get(0)).getTotalBytes() * this.cNeighborCellList.size()) + 2));
            this.fieldMap.put("cNeighborCellList", this.cNeighborCellList);
        }
    }

    public CSubMainCardCellInfo() {
        this.enSubEventId = new ENCSubEventId();
        this.cCurrentCellList = new ArrayList(8);
        this.cNeighborCellList = new ArrayList(8);
        this.lengthMap.put("enSubEventId", Integer.valueOf(2));
        this.fieldMap.put("enSubEventId", this.enSubEventId);
        this.lengthMap.put("cCurrentCellList", Integer.valueOf(2));
        this.fieldMap.put("cCurrentCellList", this.cCurrentCellList);
        this.lengthMap.put("cNeighborCellList", Integer.valueOf(2));
        this.fieldMap.put("cNeighborCellList", this.cNeighborCellList);
        this.enSubEventId.setValue("MainCardCellInfo");
    }
}
