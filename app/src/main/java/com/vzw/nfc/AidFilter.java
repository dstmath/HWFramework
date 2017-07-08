package com.vzw.nfc;

import android.nfc.INfcAdapter.Stub;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import com.huawei.pgmng.log.LogPower;
import com.nxp.nfc.INfcVzw;
import com.vzw.nfc.dos.AidMaskDo;
import com.vzw.nfc.dos.AidRangeDo;
import com.vzw.nfc.dos.ClfFilterDo;
import com.vzw.nfc.dos.ClfFilterDoList;
import com.vzw.nfc.dos.DoParserException;
import com.vzw.nfc.dos.FilterConditionTagDo;
import com.vzw.nfc.dos.FilterEntryDo;
import java.util.ArrayList;

public final class AidFilter {
    public static final int DEFAULT_ROUTE_LOCATION = 2;
    private static INfcVzw mVzwNfcAdapter;

    public boolean setFilterList(byte[] filterList) {
        boolean status = true;
        ClfFilterDoList all_CLF_FILTER_DO = new ClfFilterDoList(filterList, 0, filterList.length);
        try {
            all_CLF_FILTER_DO.translate();
            ArrayList<RouteEntry> entries = new ArrayList();
            prepareRouteInfo(all_CLF_FILTER_DO, entries);
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

    private void prepareRouteInfo(ClfFilterDoList all_CLF_FILTER_DO, ArrayList<RouteEntry> entries) {
        for (ClfFilterDo clf_FILTER_DO : all_CLF_FILTER_DO.getClfFilterDos()) {
            byte[] aid = getAid(clf_FILTER_DO.getFilterEntryDo().getAidRangeDo(), clf_FILTER_DO.getFilterEntryDo().getAidMaskDo());
            int powerState = getPowerState(clf_FILTER_DO.getFilterEntryDo());
            Log.d("AidFilter", "prepareRouteInfo powerState" + powerState);
            entries.add(new RouteEntry(aid, powerState, DEFAULT_ROUTE_LOCATION, clf_FILTER_DO.getFilterEntryDo().getVzwArDo().isVzwAllowed()));
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
            powerState = ((((routeInfo & 1) << DEFAULT_ROUTE_LOCATION) | (routeInfo & DEFAULT_ROUTE_LOCATION)) | ((routeInfo & 4) >> DEFAULT_ROUTE_LOCATION)) | LogPower.START_CHG_ROTATION;
        }
        Log.d("AidFilter", "getPowerState" + powerState);
        return powerState;
    }

    private static INfcVzw getServiceInterface() {
        if (mVzwNfcAdapter != null) {
            return mVzwNfcAdapter;
        }
        IBinder b = ServiceManager.getService("nfc");
        if (b == null) {
            return null;
        }
        try {
            mVzwNfcAdapter = Stub.asInterface(b).getNxpNfcAdapterInterface().getNfcVzwInterface();
            return mVzwNfcAdapter;
        } catch (Exception e) {
            throw new UnsupportedOperationException();
        }
    }
}
