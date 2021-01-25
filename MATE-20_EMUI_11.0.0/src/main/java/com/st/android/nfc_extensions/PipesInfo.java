package com.st.android.nfc_extensions;

import android.util.Log;
import java.util.ArrayList;
import java.util.List;

public class PipesInfo {
    private static final boolean DBG = true;
    private List<ItemInfo> info = new ArrayList();
    private int nbPipes;
    String tag = "NfcPipesInfo";

    public class ItemInfo {
        public String destHost;
        public String pipeId;
        public String pipeState;
        public String sourceGate;
        public String sourceHost;

        public ItemInfo() {
        }
    }

    public PipesInfo(int nb) {
        String str = this.tag;
        Log.i(str, "Contructor - nbPipes = " + nb);
        this.nbPipes = nb;
    }

    public String getPipeData(int idx) {
        return "    Gate: 0x" + this.info.get(idx).sourceGate + "\n      pipe Id: 0x" + this.info.get(idx).pipeId + " - " + this.info.get(idx).pipeState + "\n      Source Host: " + this.info.get(idx).sourceHost + " - Dest Host: " + this.info.get(idx).destHost;
    }

    public int getNbPipes() {
        return this.nbPipes;
    }

    public ItemInfo getPipeInfo(int pipe_idx) {
        Log.i(this.tag, "getPipeInfo()");
        return this.info.get(pipe_idx);
    }

    public void setPipeInfo(int pipe_id, byte[] data) {
        Log.i(this.tag, "setPipeInfo() - for pipe " + pipe_id);
        ItemInfo item = new ItemInfo();
        item.pipeId = String.format("%02X", Integer.valueOf(pipe_id & 255));
        byte b = data[0];
        if (b == -122) {
            item.pipeState = "Pipe Created/Opened/RF active";
        } else if (b == 2) {
            item.pipeState = "Pipe Created";
        } else if (b != 6) {
            item.pipeState = "Unknwon pipe state";
        } else {
            item.pipeState = "Pipe Created/Opened";
        }
        Log.i(this.tag, "setPipeInfo() - pipe state is " + item.pipeState);
        byte b2 = data[1];
        if (b2 == -64) {
            item.sourceHost = "eSE";
        } else if (b2 == 0) {
            item.destHost = "NFCC";
        } else if (b2 == 1) {
            item.sourceHost = "Device Host";
        } else if (b2 != 2) {
            item.sourceHost = "Unknwon Host";
        } else {
            item.sourceHost = "UICC";
        }
        Log.i(this.tag, "setPipeInfo() - source host is " + item.sourceHost);
        item.sourceGate = String.format("%02X", Integer.valueOf(data[2] & 255));
        String gateName = "";
        byte b3 = data[2];
        if (b3 == -16) {
            gateName = " - APDU";
        } else if (b3 == 65) {
            gateName = " - Connectivity";
        } else if (b3 == 4) {
            gateName = " - Loopback mgmt";
        } else if (b3 != 5) {
            switch (b3) {
                case 17:
                    gateName = " - Reader B";
                    break;
                case 18:
                    gateName = " - Reader 15693";
                    break;
                case 19:
                    gateName = " - Reader A";
                    break;
                case 20:
                    gateName = " - Reader F";
                    break;
                case 21:
                    gateName = " - Reader A'";
                    break;
                default:
                    switch (b3) {
                        case 33:
                            gateName = " - Card B";
                            break;
                        case 34:
                            gateName = " - Card B'";
                            break;
                        case 35:
                            gateName = " - Card A";
                            break;
                        case 36:
                            gateName = " - Card F";
                            break;
                    }
            }
        } else {
            gateName = " - Identity mgmt";
        }
        item.sourceGate += gateName;
        Log.i(this.tag, "setPipeInfo() - source gate is " + item.sourceGate);
        byte b4 = data[3];
        if (b4 == -64) {
            item.destHost = "eSE";
        } else if (b4 == 0) {
            item.destHost = "NFCC";
        } else if (b4 == 1) {
            item.destHost = "Device Host";
        } else if (b4 != 2) {
            item.destHost = "Unknwon Host";
        } else {
            item.destHost = "UICC";
        }
        Log.i(this.tag, "setPipeInfo() - destination host is " + item.destHost);
        this.info.add(item);
    }
}
