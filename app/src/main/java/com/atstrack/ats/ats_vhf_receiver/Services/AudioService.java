package com.atstrack.ats.ats_vhf_receiver.Services;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioTrack;
import android.util.Log;

import com.atstrack.ats.ats_vhf_receiver.Utils.ValueCodes;

public class AudioService {
    public static final int[] frequencies = {800, 951, 1131, 1345, 1600, 1903, 2263, 2691, 3200, 3806, 4525};

    public static void emitAudioPulse(int frequency, float rssi) {
        int durationMs = 30; // 30ms
        int sampleRate = 44100; // Standard quality
        float volume = calculateVolume(rssi);

        int frameCount = (int) ((durationMs / 1000.0) * sampleRate);
        short[] buffer = new short[frameCount];

        int fadeOutFrames = (int) (5.0 / 1000.0 * sampleRate);
        int fadeStart = frameCount - fadeOutFrames;

        // Generate the wave
        for (int i = 0; i < frameCount; i++) {
            double phase = 2.0 * Math.PI * i * frequency / sampleRate;
            double sample = Math.sin(phase) * volume * 32767;

            if (i > fadeStart) {
                float ramp = (float) (frameCount - i) / fadeOutFrames;
                sample *= ramp;
            }

            buffer[i] = (short) sample;
        }

        // Configure audio attributes
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        // Configure audio format
        AudioFormat audioFormat = new AudioFormat.Builder()
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setSampleRate(sampleRate)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build();

        try {
            // Create a modern audio track
            AudioTrack audioTrack = new AudioTrack.Builder()
                    .setAudioAttributes(audioAttributes)
                    .setAudioFormat(audioFormat)
                    .setBufferSizeInBytes(buffer.length * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build();

            // Play and clean
            audioTrack.write(buffer, 0, buffer.length);
            audioTrack.play();

            // Listener to free up memory
            audioTrack.setNotificationMarkerPosition(frameCount);
            audioTrack.setPlaybackPositionUpdateListener(new AudioTrack.OnPlaybackPositionUpdateListener() {
                @Override
                public void onMarkerReached(AudioTrack track) {
                    track.release();
                }

                @Override
                public void onPeriodicNotification(AudioTrack track) {
                }
            });
        } catch (Exception ex) {
            Log.e("Audio Service", "Error: " + ex.getLocalizedMessage());
        }
    }

    public static float calculateVolume(float rssi) {
        float amplitude;
        if (rssi <= ValueCodes.MIN_RSSI) {
            amplitude = 0.001f;
        } else if (rssi >= ValueCodes.MAX_RSSI) {
            amplitude = 1.0f;
        } else {
            float exponent = (rssi - ValueCodes.MAX_RSSI) / 6.0f;
            amplitude = (float) Math.pow(2.0, exponent);
        }

        float estimatedMVPP = amplitude * 2800.0f;
        Log.d("AudioTest", String.format("RSSI: %.1f | Amp: %.4f | mVpp: %.1f", rssi, amplitude, estimatedMVPP));
        return Math.max(0.001f, amplitude);
    }
}