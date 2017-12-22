package com.apnea.vikas.apneadec;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.Process;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.BrokenBarrierException;

/**
 * Created by vikas on 18-12-2017.
 */

public class recordThread {

    private final String TAG = "AudioRecord";
    private final int SAMPLE_RATE = 48000;
    private final int ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    private final int CHANNEL_MASK = AudioFormat.CHANNEL_IN_MONO;
    private boolean mShouldContinue;
    private AudioRecord audioRecord;
    private Thread mThread;
    private final int  minBuffSize = 5160;


    public void startRecording() {


        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE, CHANNEL_MASK, ENCODING, minBuffSize);
        audioRecord.setNotificationMarkerPosition(minBuffSize);
        audioRecord.setPositionNotificationPeriod(minBuffSize);
        audioRecord.setRecordPositionUpdateListener(new AudioRecord.OnRecordPositionUpdateListener() {
            @Override
            public void onMarkerReached(AudioRecord recorder) {

                Log.v(TAG,"Marker is reached");
            }

            @Override
            public void onPeriodicNotification(AudioRecord recorder) {
                Log.v(TAG,"Periodic Notification is called");
            }
        });


        mThread = new Thread(new Runnable() {
            @Override
            public synchronized void run() {


                android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO);
                Log.v(TAG,"No of parties at the barrier : " + MainActivity.myBarrier.getNumberWaiting());
                try {
                    MainActivity.myBarrier.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (BrokenBarrierException e) {
                    e.printStackTrace();
                }
                audioRecord.startRecording();


                long shortsRead = 0;
                mShouldContinue = true;
                File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/ApneaApp/recordedData.pcm");
                try {
                    OutputStream os = new FileOutputStream(file);
                    BufferedOutputStream bos = new BufferedOutputStream(os);
                    DataOutputStream dos = new DataOutputStream(bos);
                    short[] buffer = new short[minBuffSize];
                    while (mShouldContinue) {


                        int recordsRead = audioRecord.read(buffer, 0, minBuffSize);
                        shortsRead += recordsRead;
                        for (int i=0 ; i < buffer.length; i++)
                                dos.writeShort(buffer[i]);
                        Log.v(TAG, "Recording.... " + shortsRead);
                    }
                    dos.flush();
                    bos.flush();
                    os.flush();
                    dos.close();
                    bos.close();
                    os.close();
                    audioRecord.stop();
                    audioRecord.release();
                    Log.v(TAG, "Recording stopped. Number of records read : " + shortsRead);
                }
                 catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        mThread.start();

    }

    public void stopRecording() {

        mShouldContinue = false;
    }




    /*public boolean recording() {
        return mThread != null;
    }

    public void startRecording() {
        if ( mThread != null )
            return;

        mShouldContinue = true;
        mThread = new Thread(new Runnable() {

            @Override
            public void run() {
                record();
            }
        });

        mThread.start();

    }
    public void stopRecording() {
        if (mThread == null)
            return;

      //  mThread.join();
        mShouldContinue = false;
        mThread = null;


    }

    private void record() {
        Log.v(TAG, "Initializing Recorder ");
        android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO);

        int bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_MASK, ENCODING);
        Log.v(TAG,"Audio  Recorder Buffer size : " + bufferSize);
        if (bufferSize == AudioRecord.ERROR || bufferSize == AudioRecord.ERROR_BAD_VALUE) {
            bufferSize = SAMPLE_RATE * 2;
        }

        short[] audioBuffer = new short[bufferSize / 2];
        try {
            File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/recordedData");
            OutputStream os = new FileOutputStream(file);
          //  BufferedOutputStream bos = new BufferedOutputStream(os);
            DataOutputStream dos = new DataOutputStream(os);




        AudioRecord audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE, CHANNEL_MASK, ENCODING, bufferSize);

        if (audioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
            Log.e("TAG", "Audio Recorder Cannot be Initialized");
            return;
        }
        audioRecord.startRecording();
        Log.v(TAG, "Start Recording ");

        long shortsRead = 0;
        long shortsWritten = 0;
        while (mShouldContinue) {
            int recordsRead = audioRecord.read(audioBuffer, 0, audioBuffer.length);
            shortsRead += recordsRead;
            int temp = audioRecord.getNotificationMarkerPosition();
            for (int i = 0; i < recordsRead; i++) {
                dos.writeShort(audioBuffer[i]);
            }


            shortsWritten += dos.size();
            Log.v(TAG, String.format("Recording... Samples written: %d", dos.size()));
        }
            dos.flush();
            audioRecord.stop();
            audioRecord.release();
            dos.close();
           // bos.flush();
           // bos.close();
            os.flush();
            os.close();
            Log.v(TAG, String.format("Recording stopped. Samples read: %d", shortsRead));
            Log.v(TAG, String.format("Recording stopped. Samples written: %d", shortsWritten));

        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }



*/


}
