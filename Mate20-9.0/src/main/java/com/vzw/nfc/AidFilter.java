package com.vzw.nfc;

import android.nfc.INfcAdapter;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import com.nxp.nfc.INfcVzw;
import com.nxp.nfc.INxpNfcAdapter;
import com.vzw.nfc.dos.AidMaskDo;
import com.vzw.nfc.dos.AidRangeDo;
import com.vzw.nfc.dos.ClfFilterDo;
import com.vzw.nfc.dos.ClfFilterDoList;
import com.vzw.nfc.dos.DoParserException;
import com.vzw.nfc.dos.FilterConditionTagDo;
import com.vzw.nfc.dos.FilterEntryDo;
import java.util.ArrayList;
import java.util.Iterator;

public final class AidFilter {
    private static INfcVzw sVzwNfcAdapter;
    public final int DEFAULT_ROUTE_LOCATION = 2;

    public boolean setFilterList(byte[] filterList) {
        boolean status = true;
        ClfFilterDoList allClfFilterDo = new ClfFilterDoList(filterList, 0, filterList.length);
        try {
            allClfFilterDo.translate();
            ArrayList<RouteEntry> entries = new ArrayList<>();
            prepareRouteInfo(allClfFilterDo, entries);
            if (getServiceInterface() != null) {
                try {
                    status = getServiceInterface().setVzwAidList((RouteEntry[]) entries.toArray(new RouteEntry[entries.size()]));
                } catch (RemoteException e) {
                    status = false;
                }
            }
            return status;
        } catch (DoParserException e2) {
            e2.printStackTrace();
            return false;
        }
    }

    public boolean enableFilterCondition(byte filterConditionTag) {
        if (getServiceInterface() == null || -15 != filterConditionTag) {
            return true;
        }
        try {
            getServiceInterface().setScreenOffCondition(true);
            return true;
        } catch (RemoteException e) {
            return false;
        }
    }

    public boolean disableFilterCondition(byte filterConditionTag) {
        if (getServiceInterface() == null || -15 != filterConditionTag) {
            return true;
        }
        try {
            getServiceInterface().setScreenOffCondition(false);
            return true;
        } catch (RemoteException e) {
            return false;
        }
    }

    private void prepareRouteInfo(ClfFilterDoList allClfFilterDo, ArrayList<RouteEntry> entries) {
        Iterator<ClfFilterDo> it = allClfFilterDo.getClfFilterDos().iterator();
        while (it.hasNext()) {
            ClfFilterDo clfDoFilter = it.next();
            byte[] aid = getAid(clfDoFilter.getFilterEntryDo().getAidRangeDo(), clfDoFilter.getFilterEntryDo().getAidMaskDo());
            int powerState = getPowerState(clfDoFilter.getFilterEntryDo());
            Log.d("AidFilter", "prepareRouteInfo powerState" + powerState);
            entries.add(new RouteEntry(aid, powerState, 2, clfDoFilter.getFilterEntryDo().getVzwArDo().isVzwAllowed()));
        }
    }

    private byte[] getAid(AidRangeDo aid_range, AidMaskDo aid_mask) {
        byte[] barr_aid_mask = aid_mask.getAidMask();
        int count = 0;
        while (count < barr_aid_mask.length && barr_aid_mask[count] == -1) {
            count++;
        }
        if (count == 0) {
            return null;
        }
        byte[] aid = new byte[count];
        System.arraycopy(aid_range.getAidRange(), 0, aid, 0, count);
        return aid;
    }

    private int getPowerState(FilterEntryDo filter_entry_do) {
        int powerState = 0;
        int routeInfo = filter_entry_do.getRoutingModeDo().getRoutingInfo();
        FilterConditionTagDo conditionTagDo = filter_entry_do.getFilterConditionTagDo();
        if (conditionTagDo != null && conditionTagDo.getFilterConditionTag() == -15) {
            powerState = 1;
        } else if (routeInfo != 0) {
            powerState = ((routeInfo & 1) << 2) | (routeInfo & 2) | ((routeInfo & 4) >> 2) | 128;
        }
        Log.d("AidFilter", "getPowerState" + powerState);
        return powerState;
    }

    private static INfcVzw getServiceInterface() {
        if (sVzwNfcAdapter != null) {
            return sVzwNfcAdapter;
        }
        IBinder b = ServiceManager.getService("nfc");
        if (b == null) {
            return null;
        }
        try {
            IBinder b2 = INfcAdapter.Stub.asInterface(b).getNfcAdapterVendorInterface("nxp");
            if (b2 == null) {
                return null;
            }
            sVzwNfcAdapter = INxpNfcAdapter.Stub.asInterface(b2).getNfcVzwInterface();
            return sVzwNfcAdapter;
        } catch (Exception e) {
            throw new UnsupportedOperationException();
        }
    }
}
