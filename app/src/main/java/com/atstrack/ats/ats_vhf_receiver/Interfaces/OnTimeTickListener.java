package com.atstrack.ats.ats_vhf_receiver.Interfaces;

public interface OnTimeTickListener {
    void onTick(String code, int currentTimeSince, boolean updateTimeSince);
}
