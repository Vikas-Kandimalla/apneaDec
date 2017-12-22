package com.apnea.vikas.apneadec;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioTrack;
import android.os.Environment;
import android.os.Process;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.concurrent.BrokenBarrierException;


/**
 * Created by vikas on 20-12-2017.
 */

public class playThread {
    private Thread mThread;
    private File file;
    private FileInputStream is;
    private FileChannel fileChannel;
    private BufferedInputStream bis;
    private DataInputStream dis;
    private int buffSize = 5160;
    private final String TAG = "AudioTrack";
    private AudioTrack player;

    private boolean mShouldPlay;
    public playThread() throws FileNotFoundException {
        file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/ApneaApp/chirp.pcm");
        if(file.exists())
            Log.v(TAG,"File Found. " + file.length());


        is = new FileInputStream(file);
        fileChannel = is.getChannel();
        bis = new BufferedInputStream(is);
        dis = new DataInputStream(bis);
        buffSize = AudioTrack.getMinBufferSize(48000,AudioFormat.CHANNEL_OUT_MONO,AudioFormat.ENCODING_PCM_16BIT);
        player = new AudioTrack.Builder()
                .setAudioAttributes(new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_UNKNOWN)
                        .setUsage(AudioAttributes.USAGE_UNKNOWN)
                        .build())
                .setAudioFormat(new AudioFormat.Builder()
                        .setSampleRate(48000)
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build())
                .setBufferSizeInBytes(buffSize)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build();

        if(player.getState() != AudioTrack.STATE_INITIALIZED)
            Log.v(TAG,"Track State not initialized");
    }
    public void startPlaying() throws IOException {
        mShouldPlay = true;
        fileChannel.position(0);
        player.play();

        mThread = new Thread(new Runnable() {
            @Override
            public void run() {
                android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO);
                short[] buffer = new short[buffSize];

                try {
                    Log.v(TAG,"No of parties at the barrier : " + MainActivity.myBarrier.getNumberWaiting());
                    MainActivity.myBarrier.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (BrokenBarrierException e) {
                    e.printStackTrace();
                }
                try {
                            while(dis.available() > 0 && mShouldPlay) {
                                for (int i = 0; i < buffSize; i++)
                                        buffer[i] = dis.readShort();

                                player.write(buffer, 0, buffSize);
                            }

                        player.stop();
                        player.release();
                        dis.close();
                        bis.close();
                        is.close();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

            }
        });
        mThread.start();

    }

    public void stopPlaying() {
        mShouldPlay = false;
    }
}
