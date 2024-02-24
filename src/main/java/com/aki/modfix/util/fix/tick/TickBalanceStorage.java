package com.aki.modfix.util.fix.tick;

public class TickBalanceStorage {
    private long Time = 0L;
    private int StopTickCycle = 0;

    public TickBalanceStorage() {
    }

    public void setTime(long time) {
        Time = time;
    }

    public long getTime() {
        return Time;
    }

    public void setStopTickCycle(int stopTickCycle) {
        StopTickCycle = stopTickCycle;
    }

    public int getStopTickCycle() {
        return StopTickCycle;
    }
}
