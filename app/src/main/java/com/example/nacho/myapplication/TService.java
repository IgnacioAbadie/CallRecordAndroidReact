package com.example.nacho.myapplication;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;

public class TService extends Service {
    MediaRecorder recorder;
    File audiofile;
    String name, phonenumber;
    String audio_format;
    public String Audio_Type;
    int audioSource;
    Context context;
    private Handler handler;
    Timer timer;
    Boolean offHook = false, ringing = false;
    Toast toast;
    Boolean isOffHook = false;
    private boolean recordstarted = false;
    AudioManager audioManager;

    private static final String ACTION_IN = "android.intent.action.PHONE_STATE";
    private static final String ACTION_OUT = "android.intent.action.NEW_OUTGOING_CALL";
    private CallBr br_call;


    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onDestroy() {
        Log.d("service", "destroy");

        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        final IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_OUT);
        filter.addAction(ACTION_IN);
        this.br_call = new CallBr();
        this.registerReceiver(this.br_call, filter);

        return START_NOT_STICKY;
    }

    public class CallBr extends BroadcastReceiver {
        Bundle bundle;
        public boolean recordstarted = false;
        public boolean isOutgoingCall;

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ACTION_OUT) || isOutgoingCall) {
                isOutgoingCall = true;
                Log.w("TSetvice", "ACTION: " + intent.getAction());
                Log.w("TSetvice", "STATE: " + (intent.getExtras() != null ? intent.getExtras().get("state") : "null"));
                if ((bundle = intent.getExtras()) != null && bundle.get("state") != null) {
                    if (recordstarted == false && String.valueOf(bundle.get("state")).equals("OFFHOOK")) {
                        Toast.makeText(context, "Grabando llamada", Toast.LENGTH_LONG).show();
                        File sampleDir = new File(Environment.getExternalStorageDirectory(), "/APP_RECORDER");
                        if (!sampleDir.exists()) {
                            sampleDir.mkdirs();
                        }
                        String file_name = "Record " + new SimpleDateFormat("dd-MM-yyyy hh-mm-ss").format(new Date());
                        try {
                            audiofile = File.createTempFile(file_name, ".amr", sampleDir);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        recorder = new MediaRecorder();
                        recorder.setAudioSource(MediaRecorder.AudioSource.VOICE_CALL);
                        recorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
                        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                        recorder.setOutputFile(audiofile.getAbsolutePath());
                        try {
                            recorder.prepare();
                        } catch (IllegalStateException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                        audioManager.setMode(AudioManager.MODE_IN_CALL);
                        audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL), 0);
                        recorder.start();
                        recordstarted = true;

                    } else if (String.valueOf(bundle.get("state")).equals("IDLE")) {
                        if (recordstarted) {
                            audioManager.setMode(AudioManager.MODE_NORMAL);
                            recorder.stop();
                            Toast.makeText(context, "Llamada grabada", Toast.LENGTH_LONG).show();
                            recordstarted = false;
                            isOutgoingCall = false;
                        }
                    }
                }
            }
        }
    }

}