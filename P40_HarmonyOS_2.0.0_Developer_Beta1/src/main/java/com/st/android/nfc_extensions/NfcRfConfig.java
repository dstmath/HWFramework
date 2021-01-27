package com.st.android.nfc_extensions;

import android.util.Log;
import java.util.HashSet;
import java.util.Set;

public class NfcRfConfig {
    boolean enabled;
    String tag = "NfcRfConfig";
    Set<String> tech;

    public NfcRfConfig() {
        Log.i(this.tag, "Contructor");
        this.enabled = true;
        this.tech = new HashSet();
    }

    public void setEnabled(boolean state) {
        Log.i(this.tag, "setEnabled()");
        this.enabled = state;
    }

    public boolean getEnabled() {
        Log.i(this.tag, "getEnabled()");
        return this.enabled;
    }

    public void setTech(String techno) {
        String str = this.tag;
        Log.i(str, "setTech(" + techno + ")");
        Set<String> set = this.tech;
        if (set == null || techno == null) {
            Log.i(this.tag, "setTech() - tech field or techno input is null!!!!");
        } else {
            set.add(techno);
        }
    }

    public void removeTech(String techno) {
        String str = this.tag;
        Log.i(str, "removeTech(" + techno + ")");
        this.tech.remove(techno);
    }

    public Set<String> getTechSet() {
        Log.i(this.tag, "getTechSet()");
        return this.tech;
    }
}
