package com.android.commands.monkey;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

public class MonkeySourceRandomScript implements MonkeyEventSource {
    private MonkeySourceScript mCurrentSource;
    private Random mRandom;
    private boolean mRandomizeScript;
    private int mScriptCount;
    private ArrayList<MonkeySourceScript> mScriptSources;
    private MonkeySourceScript mSetupSource;
    private int mVerbose;

    public MonkeySourceRandomScript(String setupFileName, ArrayList<String> scriptFileNames, long throttle, boolean randomizeThrottle, Random random, long profileWaitTime, long deviceSleepTime, boolean randomizeScript) {
        this.mVerbose = 0;
        this.mSetupSource = null;
        this.mScriptSources = new ArrayList<>();
        this.mCurrentSource = null;
        this.mRandomizeScript = false;
        this.mScriptCount = 0;
        if (setupFileName != null) {
            MonkeySourceScript monkeySourceScript = new MonkeySourceScript(random, setupFileName, throttle, randomizeThrottle, profileWaitTime, deviceSleepTime);
            this.mSetupSource = monkeySourceScript;
            this.mCurrentSource = this.mSetupSource;
        }
        Iterator<String> it = scriptFileNames.iterator();
        while (it.hasNext()) {
            ArrayList<MonkeySourceScript> arrayList = this.mScriptSources;
            MonkeySourceScript monkeySourceScript2 = new MonkeySourceScript(random, it.next(), throttle, randomizeThrottle, profileWaitTime, deviceSleepTime);
            arrayList.add(monkeySourceScript2);
        }
        this.mRandom = random;
        this.mRandomizeScript = randomizeScript;
    }

    public MonkeySourceRandomScript(ArrayList<String> scriptFileNames, long throttle, boolean randomizeThrottle, Random random, long profileWaitTime, long deviceSleepTime, boolean randomizeScript) {
        this(null, scriptFileNames, throttle, randomizeThrottle, random, profileWaitTime, deviceSleepTime, randomizeScript);
    }

    public MonkeyEvent getNextEvent() {
        if (this.mCurrentSource == null) {
            int numSources = this.mScriptSources.size();
            if (numSources == 1) {
                this.mCurrentSource = this.mScriptSources.get(0);
            } else if (numSources > 1) {
                if (this.mRandomizeScript) {
                    this.mCurrentSource = this.mScriptSources.get(this.mRandom.nextInt(numSources));
                } else {
                    this.mCurrentSource = this.mScriptSources.get(this.mScriptCount % numSources);
                    this.mScriptCount++;
                }
            }
        }
        if (this.mCurrentSource == null) {
            return null;
        }
        MonkeyEvent nextEvent = this.mCurrentSource.getNextEvent();
        if (nextEvent == null) {
            this.mCurrentSource = null;
        }
        return nextEvent;
    }

    public void setVerbose(int verbose) {
        this.mVerbose = verbose;
        if (this.mSetupSource != null) {
            this.mSetupSource.setVerbose(verbose);
        }
        Iterator<MonkeySourceScript> it = this.mScriptSources.iterator();
        while (it.hasNext()) {
            it.next().setVerbose(verbose);
        }
    }

    public boolean validate() {
        if (this.mSetupSource != null && !this.mSetupSource.validate()) {
            return false;
        }
        Iterator<MonkeySourceScript> it = this.mScriptSources.iterator();
        while (it.hasNext()) {
            if (!it.next().validate()) {
                return false;
            }
        }
        return true;
    }
}
