package com.a24gdemo;

import android.os.Bundle;
import android.os.Message;
import android.serialport.DeviceControl;
import android.serialport.SerialPort;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.speedata.libutils.DataConversionUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import static android.serialport.SerialPort.SERIAL_TTYMT2;
import static android.serialport.SerialPort.SERIAL_TTYMT0;
import static com.a24gdemo.R.id.btn_clear;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    DeviceControl deviceControl;
    SerialPort serialPort;
    EditText edSend;
    TextView tvReciver;
    Button btnSend, btclear;
    private int fd;
    readTherd readThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        edSend = (EditText) findViewById(R.id.edit_send);
        btnSend = (Button) findViewById(R.id.btn_send);
        tvReciver = (TextView) findViewById(R.id.tv_reciver);
        btclear = (Button) findViewById(btn_clear);
        btclear.setOnClickListener(this);
        btnSend.setOnClickListener(this);
        findViewById(R.id.btn_open).setOnClickListener(this);
        findViewById(R.id.btn_close).setOnClickListener(this);
        tvReciver.setText("接收内容：\n");
        try {
//            deviceControl = new DeviceControl(DeviceControl.PowerType.MAIN, 64);
            deviceControl = new DeviceControl(DeviceControl.PowerType.MAIN, 94);
            serialPort = new SerialPort();
//            serialPort.OpenSerial(SERIAL_TTYMT0, 115200);
            serialPort.OpenSerial(SERIAL_TTYMT2, 115200);
            fd = serialPort.getFd();
            readThread = new readTherd();
            readThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_send) {
            serialPort.WriteSerialByte(serialPort.getFd(), DataConversionUtils.hexStringToByteArray(edSend.getHint().toString()));
        } else if (view.getId() == R.id.btn_clear) {
            tvReciver.setText("接收内容：\n");
        } else if (view.getId() == R.id.btn_open) {
            try {
                deviceControl.PowerOnDevice();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (view.getId() == R.id.btn_close) {
            try {
                deviceControl.PowerOffDevice();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    android.os.Handler handler = new android.os.Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    byte[] temp = (byte[]) msg.obj;
                    String string = DataConversionUtils.byteArrayToString(temp);

                    tvReciver.append(string + "\n");
                    break;
            }

        }
    };

    class readTherd extends Thread {
        @Override
        public void run() {
            super.run();
            while (!isInterrupted()) {
                try {
                    byte[] data = serialPort.ReadSerial(serialPort.getFd(), 1024);
                    Log.d("zzc","readTherd"+ Arrays.toString(data));
                    if (data != null) {
                        handler.sendMessage(handler.obtainMessage(0, data));
                    }
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            deviceControl.PowerOffDevice();
            serialPort.CloseSerial(fd);
            if (readThread != null) {
                readThread.interrupt();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
