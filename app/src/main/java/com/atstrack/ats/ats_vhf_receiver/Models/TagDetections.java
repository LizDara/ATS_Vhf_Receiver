package com.atstrack.ats.ats_vhf_receiver.Models;

import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;

import com.atstrack.ats.ats_vhf_receiver.Interfaces.OnTimeTickListener;

import java.util.ArrayList;

public class TagDetections {
    public String code;
    public ArrayList<Detection> detections;
    public int frequencyTone;
    private final Handler timerHandler = new Handler(Looper.getMainLooper());
    private Runnable timerRunnable;
    public int timeSince = 0;
    public TextView time_since_textView;
    public OnTimeTickListener timeTickListener;

    public TagDetections(String code, int frequencyTone) {
        this.code = code;
        this.frequencyTone = frequencyTone;
        this.detections = new ArrayList<>();
        startTimer();
    }

    public Detection getLastDetection() {
        return detections.get(detections.size() - 1);
    }

    private void startTimer() {
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                int currentTimeSince = (int) (System.currentTimeMillis() - detections.get(detections.size() - 1).timestamp) / 1000;
                boolean updateTimeSince = currentTimeSince > timeSince;
                timeSince = currentTimeSince;
                if (timeTickListener != null)
                    timeTickListener.onTick(code, timeSince, updateTimeSince);
                timerHandler.postDelayed(this, 100);
            }
        };
        timerHandler.post(timerRunnable);
    }

    public void stopTimer() {
        timerHandler.removeCallbacks(timerRunnable);
    }
}

