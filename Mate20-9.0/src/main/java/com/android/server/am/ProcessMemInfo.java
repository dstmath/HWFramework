package com.android.server.am;

public class ProcessMemInfo {
    final String adjReason;
    final String adjType;
    long memtrack;
    final String name;
    final int oomAdj;
    final int pid;
    final int procState;
    long pss;

    public ProcessMemInfo(String _name, int _pid, int _oomAdj, int _procState, String _adjType, String _adjReason) {
        this.name = _name;
        this.pid = _pid;
        this.oomAdj = _oomAdj;
        this.procState = _procState;
        this.adjType = _adjType;
        this.adjReason = _adjReason;
    }
}
