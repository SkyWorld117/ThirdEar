package com.example.thirdear;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Switch;
import android.widget.SeekBar;
import android.widget.CompoundButton;
import android.media.AudioManager;
import android.content.Context;
import android.media.AudioTrack;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Switch OnOff = findViewById(R.id.OnOff);
        SeekBar Amplitude = findViewById(R.id.Amplitude);
        TextView DisplayText = findViewById(R.id.DisplayText);
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        Amplitude.setMax(15);
        int max_Level = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        Amplitude.setProgress((int)(15*(double)audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)/max_Level));
        Amplitude.setEnabled(false);

        //int minSize = AudioRecord.getMinBufferSize(8000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
            Runtime.getRuntime().exit(0);
        }

        class RecordThread extends Thread{
            static final int frequency = 44100;
            static final int channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_MONO;
            static final int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
            @Override
            public void run() {
                int recBufSize = AudioRecord.getMinBufferSize(frequency, channelConfiguration, audioEncoding)*2;
                int plyBufSize = AudioTrack.getMinBufferSize(frequency, channelConfiguration, audioEncoding)*2;
                //AudioRecord audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, frequency, channelConfiguration, audioEncoding, recBufSize);
                AudioRecord audioRecord = new AudioRecord(MediaRecorder.AudioSource.VOICE_RECOGNITION, frequency, channelConfiguration, audioEncoding, recBufSize);
                AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, frequency, channelConfiguration, audioEncoding, plyBufSize, AudioTrack.MODE_STREAM);
                byte[] recBuf = new byte[recBufSize];
                audioRecord.startRecording();
                audioTrack.play();
                while (true) {
                    if (OnOff.isChecked()) {
                        int readLen = audioRecord.read(recBuf, 0, recBufSize);
                        audioTrack.write(recBuf, 0, readLen);
                        Amplitude.setProgress((int)(15*(double)audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)/max_Level));
                    }
                }
            }
        }
        RecordThread rec = new RecordThread();
        rec.start();

        OnOff.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {
                    DisplayText.setText("On");
                    Amplitude.setEnabled(true);
                } else {
                    DisplayText.setText("Off");
                    Amplitude.setEnabled(false);
                }
            }
        });

        Amplitude.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                DisplayText.setText(Integer.toString(Amplitude.getProgress()));
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, (int)((double)Amplitude.getProgress()/15*max_Level), 0);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }
}

// http://www.360doc.com/content/16/0128/19/9200790_531296568.shtml