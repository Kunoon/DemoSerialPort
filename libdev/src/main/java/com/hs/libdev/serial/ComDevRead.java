package com.hs.libdev.serial;

import android.os.Handler;
import com.serial.SerialPort;

import java.io.UnsupportedEncodingException;

public class ComDevRead extends Thread {
    public static final int RESULT_TYPE_HEX = 0;
    public static final int RESULT_TYPE_ASCII = 1;
    private Handler handler = null;
    private int resultType = 0;
    private ComDevReadBase devReadBase = null;

    public ComDevRead(SerialPort com, Handler handler, int resultType) {
        devReadBase = new ComDevReadBase(com);
        this.handler = handler;
        this.resultType = resultType;
    }

    public void setResultType(int resultType) {
        this.resultType = resultType;
    }

    @Override
    public void run() {
        super.run();
        while (!isInterrupted()) {
            int length = devReadBase.getRecBytes().length;
            if (length > 0) {
                if (handler != null) {
                    switch (resultType) {
                        case RESULT_TYPE_ASCII:
                            handler.obtainMessage(1, RESULT_TYPE_ASCII, length, readTxt(devReadBase.getRecBytes(), "UTF-8")).sendToTarget();
                            break;
                        case RESULT_TYPE_HEX:
                        default:
                            handler.obtainMessage(1, RESULT_TYPE_HEX, length, devReadBase.getRecBytes()).sendToTarget();
                            break;
                    }
                }
                devReadBase.finishRead();
            }
        }
        devReadBase.stopRead();
        devReadBase = null;
    }

    public void startT() {
        devReadBase.startRead();
        this.start();
    }

    public void stopT() {
        this.interrupt();
    }

    /**
     * 将串口读取的数据转换为字符串
     * @param buffer
     * @return
     */
    private String readTxt(byte[] buffer, String charsetName) {
        String str = null;
        try {
            str = new String(buffer, charsetName);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return str;
    }
}
