package com.huawei.server.rme.hyperhold;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Trace;
import android.util.Slog;
import com.android.server.appactcontrol.AppActConstant;
import java.io.File;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class HyperHoldCfgUpdateReceiver extends BroadcastReceiver {
    private static final int CHANGE_TO_DISABLE = 0;
    private static final int CHANGE_TO_ENABLE = 1;
    private static final String HWOUC_XML = "/data/cota/para/HYPERHOLD/hyperhold_config.xml";
    private static final int NOT_CHANGE = -1;
    private static final String TAG = "HyperHold_HWOUC_Update";
    private static final String XML_TAG_SWAP_ENABLE = "swapEnable";

    @Override // android.content.BroadcastReceiver
    public void onReceive(Context context, Intent intent) {
        if (context != null && intent != null && intent.getAction() != null) {
            String action = intent.getAction();
            Slog.i(TAG, "action:" + action);
            Thread hwoucUpdate = new Thread(new Runnable() {
                /* class com.huawei.server.rme.hyperhold.HyperHoldCfgUpdateReceiver.AnonymousClass1 */

                @Override // java.lang.Runnable
                public void run() {
                    Trace.traceBegin(8, "HyperHoldUpdate");
                    HyperHoldCfgUpdateReceiver.checkSwapFromHWOUC();
                    Trace.traceEnd(8);
                }
            });
            hwoucUpdate.setName("HyperHoldUpdate");
            hwoucUpdate.start();
        }
    }

    public static void checkSwapFromHWOUC() {
        int checkSwapEnable = readHwoucSwapEnable();
        if (checkSwapEnable != -1) {
            boolean swapEnable = true;
            if (checkSwapEnable != 1) {
                swapEnable = false;
            }
            Slog.i(TAG, "hwouc update switches swapEnable to:" + swapEnable);
            Swap.getInstance().setSwapEnable(swapEnable);
            if (!swapEnable) {
                KernelInterface.getInstance().changeSwapOutSwitch(false);
                Slog.i(TAG, "close swap out switch");
            }
            ParaConfig.getInstance();
            ParaConfig.resetHasReadXml();
            Slog.i(TAG, "reset the hasReadXml");
            return;
        }
        Slog.i(TAG, "the swapEnable won't be changed by hwouc");
    }

    private static int readHwoucSwapEnable() {
        Node swapNode;
        try {
            Node swapNode2 = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File(HWOUC_XML)).getElementsByTagName(XML_TAG_SWAP_ENABLE).item(0);
            if (swapNode2 == null || (swapNode = swapNode2.getFirstChild()) == null) {
                return -1;
            }
            if (AppActConstant.VALUE_TRUE.equalsIgnoreCase(swapNode.getNodeValue())) {
                return 1;
            }
            return 0;
        } catch (IOException | IllegalArgumentException | ParserConfigurationException | SAXException e) {
            Slog.e(TAG, "faild to read from:/data/cota/para/HYPERHOLD/hyperhold_config.xml, " + e);
            return -1;
        }
    }
}
