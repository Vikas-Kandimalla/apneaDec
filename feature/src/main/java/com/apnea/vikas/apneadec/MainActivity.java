package com.apnea.vikas.apneadec;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

import static android.os.SystemClock.sleep;

public class MainActivity extends AppCompatActivity {

    public static CyclicBarrier myBarrier = new CyclicBarrier(2);
    private recordThread rec = new recordThread();
    private playThread p;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try {
             p = new playThread();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }




        final TextView t = (TextView) findViewById(R.id.hello);

        Button sRec = (Button) findViewById(R.id.sRec);
        sRec.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {



                t.setText( "State : Recording ");
                rec.startRecording();


                try {
                    p.startPlaying();
                } catch (IOException e) {
                    e.printStackTrace();
                }





            }

        });

        Button btn = (Button) findViewById(R.id.stopRec);
        btn.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick(View view){
                t.setText( "State : Stopped ");



                    rec.stopRecording();
                    p.stopPlaying();
                    myBarrier.reset();


            }

        });
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    //  public native String stringFromJNI();
    @Override
    protected void onStop() {

            super.onStop();
            rec.stopRecording();
            myBarrier.reset();

    }






}
