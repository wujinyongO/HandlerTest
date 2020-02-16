package com.example.handlertest;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private final static String TAG = "MainActivity";

    private TextView mTextView;
    private Button mStart, mEnd;
    private boolean mIsExitTiming = true;

    private MyThread mThread1;
    private Handler mHandler1;

    private MyThread2 mThread2;
    private Handler mHandler2;

    private HandlerThread mHandlerThread;
    private Handler mHandler3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.i(TAG, "START MainActivity");

        findView();

        // --------------------------------------------------------------------------------
        mHandler1 = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                Log.i(TAG, "current thread:"+Thread.currentThread());
                mTextView.setText(String.valueOf(msg.arg1));
            }
        };
        // --------------------------------------------------------------------------------

        // --------------------------------------------------------------------------------
        mThread2 = new MyThread2();
        mThread2.start();

        // 必须要加，不然崩溃
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        mHandler2 = new Handler(mThread2.mLooper) {
            @Override
            public void handleMessage(Message msg) {
                Log.i(TAG, "Main thread; current thread:"+Thread.currentThread());

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (mThread2.mHandler != null) {
                    mThread2.mHandler.sendEmptyMessage(2);
                }
            }
        };
        mThread2.mHandler.sendEmptyMessage(1);
        // --------------------------------------------------------------------------------

        // --------------------------------------------------------------------------------
        mHandlerThread = new HandlerThread("mHandlerThread");
        mHandlerThread.start();
        mHandler3 = new Handler(mHandlerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                Log.i(TAG, "mHandlerThread thread; current thread:"+Thread.currentThread());
            }
        };
        mHandler3.sendEmptyMessage(3);
        // --------------------------------------------------------------------------------

        mStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mIsExitTiming) {
                    mIsExitTiming = false;
                    mThread1 = new MyThread();
                    mThread1.start();
                }
            }
        });

        mEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mIsExitTiming = true;
            }
        });
    }

    private void findView() {
        mTextView = findViewById(R.id.text);
        mStart = findViewById(R.id.start_timing);
        mEnd = findViewById(R.id.end_timing);
    }

    /**
     * 验证子线程更新UI
     */
    private class MyThread extends Thread {
        @Override
        public void run() {


            int timeCount = 0;
            while (!mIsExitTiming) {
                timeCount++;
                final int finalTimeCount = timeCount;

//                在子线程更新UI

//                1.runOnUiThread()
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        mTextView.setText(String.valueOf(finalTimeCount));
//                    }
//                });

//                2.用handler post 一个 Runnable
//                mHandler.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        mTextView.setText(String.valueOf(finalTimeCount));
//                    }
//                });

//                3.用handler发送一个message
                Message msg = mHandler1.obtainMessage();
                msg.arg1 = timeCount;
                mHandler1.sendMessage(msg);
//                Log.i(TAG, "msg:"+msg.arg1);
                Log.i(TAG, "MyThread1 thread; current thread:"+Thread.currentThread());

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 验证UI线程发消息给子线程
     */
    class MyThread2 extends Thread {
        Looper mLooper;
        Handler mHandler;

        @Override
        public void run() {
            Looper.prepare();
            mLooper = Looper.myLooper();
            mHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    Log.i(TAG, "MyThread2 thread; current thread:"+Thread.currentThread());
                }
            };
            Looper.loop();

            // 没有执行
            while(true) {
                Log.i(TAG, "MyThread2 thread; current thread:"+Thread.currentThread());
                mHandler2.sendEmptyMessage(2);

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
