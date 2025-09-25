package com.example.freqapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class MainActivity extends AppCompatActivity {
    private static final int SAMPLE_RATE = 44100;
    private TextView freqText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        freqText = findViewById(R.id.freqText);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 123);
        } else {
            startAudioCapture();
        }
    }

    private void startAudioCapture() {
        new Thread(() -> {
            int bufferSize = AudioRecord.getMinBufferSize(
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT);

            AudioRecord recorder = new AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    bufferSize);

            short[] audioBuffer = new short[bufferSize];
            recorder.startRecording();

            while (true) {
                int read = recorder.read(audioBuffer, 0, bufferSize);
                if (read > 0) {
                    double freq = calculateFrequency(audioBuffer, read);
                    runOnUiThread(() -> freqText.setText(String.format("Frequency: %.2f Hz", freq)));
                }
            }
        }).start();
    }

    private double calculateFrequency(short[] buffer, int readSize) {
        int numCrossings = 0;
        for (int i = 1; i < readSize; i++) {
            if ((buffer[i - 1] > 0 && buffer[i] <= 0) || (buffer[i - 1] < 0 && buffer[i] >= 0)) {
                numCrossings++;
            }
        }
        return (SAMPLE_RATE / (double) readSize) * (numCrossings / 2.0);
    }
}
