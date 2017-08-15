package com.example.nacho.myapplication;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.content.ContentValues.TAG;

public class TService extends Service {
    private static final String ACTION_IN = "android.intent.action.PHONE_STATE";
    private static final String ACTION_OUT = "android.intent.action.NEW_OUTGOING_CALL";

    private CallBr br_call;
    MediaRecorder mRecorder;
    File mAudioFile;


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

        Log.w(TAG, "Service Started" );

        final IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_OUT);
        filter.addAction(ACTION_IN);
        filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
        this.br_call = new CallBr(this);
        this.registerReceiver(this.br_call, filter);

        return START_NOT_STICKY;
    }

    public class CallBr extends BroadcastReceiver {
        private static final String TAG = "CallBr";

        private String mRecordDirectory = "/APP_RECORDER";
        private String mFileExtension = ".mp3";
        private boolean recordStarted;
        private boolean isOutgoingCall;
        private TService mService;


        public CallBr(@NonNull TService service) {
            mService = service;
        }

        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals(ACTION_OUT) || isOutgoingCall) {

                isOutgoingCall = true;

                if (!recordStarted) {
                    startRecordCallAudio();

                } else if (intent.getExtras() != null && String.valueOf(intent.getExtras().get("state")).equals("IDLE")) {
                    if (recordStarted) {
                        stopRecordCallAudio();
                    }
                }
            }
        }

        private void startRecordCallAudio() {

            Log.d(TAG, "Prepared to Record");

            File sampleDir = new File(Environment.getExternalStorageDirectory(), mRecordDirectory);
            if (!sampleDir.exists()) {
                sampleDir.mkdirs();
            }

            String file_name = "Record " + new SimpleDateFormat("dd-MM-yyyy hh-mm-ss").format(new Date());

            try {
                mAudioFile = File.createTempFile(file_name, mFileExtension, sampleDir);
            } catch (IOException e) {
                e.printStackTrace();
            }

            mRecorder = new MediaRecorder();
            mRecorder.setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mRecorder.setOutputFile(mAudioFile.getAbsolutePath());

            try {
                mRecorder.prepare();
                Thread.sleep(1000);
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }

            try {
                mRecorder.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
            recordStarted = true;

            Log.d(TAG, "Record Started");
        }


        private void stopRecordCallAudio() {
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;
            recordStarted = false;
            isOutgoingCall = false;

            Log.d(TAG, "Record Stopped");

            mService.stopSelf();
        }
    }

}