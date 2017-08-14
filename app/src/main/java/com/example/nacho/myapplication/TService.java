package com.example.nacho.myapplication;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TService extends Service {
    private static final String ACTION_IN = "android.intent.action.PHONE_STATE";
    private static final String ACTION_OUT = "android.intent.action.NEW_OUTGOING_CALL";

    private CallBr br_call;
    MediaRecorder recorder;
    File audiofile;


    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onDestroy() {
        Log.d("service", "onDestroy()");
        unregisterReceiver(br_call);
        super.onDestroy();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.w("ACTIasdasdasdasdasON", "ASDASDASDASDASDASDASD" );
        final IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_OUT);
        filter.addAction(ACTION_IN);
        this.br_call = new CallBr(this);
        this.registerReceiver(this.br_call, filter);

        return START_NOT_STICKY;
    }

    public class CallBr extends BroadcastReceiver {
        private static final String TAG = "CallBr";

        private Bundle mBundle;
        private boolean recordStarted;
        private boolean isOutgoingCall;
        private TService mService;


        public CallBr(@NonNull TService service) {
            mService = service;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "ASDASDASDASDASDASDASD" );

            if (intent.getAction().equals(ACTION_OUT) || isOutgoingCall) {
                isOutgoingCall = true;
                Log.w(TAG, "ACTION: " + intent.getAction());
                Log.w(TAG, "STATE: " + (intent.getExtras() != null ? intent.getExtras().get("state") : "null"));
                if ((mBundle = intent.getExtras()) != null && mBundle.get("state") != null) {
                    if (!recordStarted && String.valueOf(mBundle.get("state")).equals("OFFHOOK")) {
                        Toast.makeText(context, "Grabando llamada", Toast.LENGTH_LONG).show();
                        File sampleDir = new File(Environment.getExternalStorageDirectory(), "/APP_RECORDER");
                        if (!sampleDir.exists()) {
                            sampleDir.mkdirs();
                        }

                        String file_name = "Record " + new SimpleDateFormat("dd-MM-yyyy hh-mm-ss").format(new Date());
                        try {
                            audiofile = File.createTempFile(file_name, ".mp3", sampleDir);//.amr
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
                            Thread.sleep(1000);
                        } catch (IOException | InterruptedException e) {
                            e.printStackTrace();
                        }
                        recorder.start();
                        recordStarted = true;

                    } else if (String.valueOf(mBundle.get("state")).equals("IDLE")) {
                        if (recordStarted) {
                            // audioManager.setMode(AudioManager.MODE_NORMAL);
                            recorder.stop();
                            Toast.makeText(context, "Llamada grabada", Toast.LENGTH_LONG).show();
                            recordStarted = false;
                            isOutgoingCall = false;
                            mService.stopSelf();
                        }
                    }
                }
            }
        }
    }

}