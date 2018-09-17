package com.android.commands.monkey;

import java.util.ArrayList;
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
        this.mScriptSources = new ArrayList();
        this.mCurrentSource = null;
        this.mRandomizeScript = false;
        this.mScriptCount = 0;
        if (setupFileName != null) {
            this.mSetupSource = new MonkeySourceScript(random, setupFileName, throttle, randomizeThrottle, profileWaitTime, deviceSleepTime);
            this.mCurrentSource = this.mSetupSource;
        }
        for (String fileName : scriptFileNames) {
            this.mScriptSources.add(new MonkeySourceScript(random, fileName, throttle, randomizeThrottle, profileWaitTime, deviceSleepTime));
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
                this.mCurrentSource = (MonkeySourceScript) this.mScriptSources.get(0);
            } else if (numSources > 1) {
                if (this.mRandomizeScript) {
                    this.mCurrentSource = (MonkeySourceScript) this.mScriptSources.get(this.mRandom.nextInt(numSources));
                } else {
                    this.mCurrentSource = (MonkeySourceScript) this.mScriptSources.get(this.mScriptCount % numSources);
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
        for (MonkeySourceScript source : this.mScriptSources) {
            source.setVerbose(verbose);
        }
    }

    public boolean validate() {
        if (this.mSetupSource != null && (this.mSetupSource.validate() ^ 1) != 0) {
            return false;
        }
        for (MonkeySourceScript source : this.mScriptSources) {
            if (!source.validate()) {
                return false;
            }
        }
        return true;
    }
}
