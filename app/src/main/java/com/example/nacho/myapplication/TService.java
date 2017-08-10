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
import android.telephony.TelephonyManager;
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
//         final String terminate =(String)
//         intent.getExtras().get("terminate");//
//         intent.getStringExtra("terminate");
//         Log.d("TAG", "service started");
//
//         TelephonyManager telephony = (TelephonyManager)
//         getSystemService(Context.TELEPHONY_SERVICE); // TelephonyManager
//         // object
//         CustomPhoneStateListener customPhoneListener = new
//         CustomPhoneStateListener();
//         telephony.listen(customPhoneListener,
//         PhoneStateListener.LISTEN_CALL_STATE);
//         context = getApplicationContext();

        final IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_OUT);
        filter.addAction(ACTION_IN);
        this.br_call = new CallBr();
        this.registerReceiver(this.br_call, filter);

//         if(terminate != null) {
//         stopSelf();
//         }
        return START_NOT_STICKY;
    }

    public class CallBr extends BroadcastReceiver {
        Bundle bundle;
        String state;
        String inCall, outCall;
        public boolean wasRinging = false;
        public boolean recordstarted = false;

        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals(ACTION_OUT)) {
                if ((bundle = intent.getExtras()) != null) {
                    state = bundle.getString(TelephonyManager.EXTRA_STATE);
                    if (state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                        inCall = bundle.getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
                        wasRinging = true;
                        Toast.makeText(context, "IN : " + inCall, Toast.LENGTH_LONG).show();
                    } else if (state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                        if (wasRinging == true && !recordstarted) {

                            Toast.makeText(context, "RESPONDIO", Toast.LENGTH_LONG).show();
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
                            String path = Environment.getExternalStorageDirectory().getAbsolutePath();

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
                            audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
                            audioManager.setMode(AudioManager.MODE_IN_CALL);
                            audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL,audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL), 0);
                            recorder.start();
                            recordstarted = true;
                        }
                    } else if (state.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                        wasRinging = false;
                        Toast.makeText(context, "FINALIZADO", Toast.LENGTH_LONG).show();
                        if (recordstarted) {
                            audioManager.setMode(AudioManager.MODE_NORMAL);
                            recorder.stop();
                            recordstarted = false;
                        }
                    }
                }
            } else if (intent.getAction().equals(ACTION_IN)) {
                if ((bundle = intent.getExtras()) != null) {
                    outCall = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
                    Toast.makeText(context, "OUT : " + outCall, Toast.LENGTH_LONG).show();
                }
            }
        }
    }

}