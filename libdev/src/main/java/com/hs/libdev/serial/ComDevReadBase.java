package com.hs.libdev.serial;

import com.serial.SerialPort;

import java.io.IOException;
import java.io.InputStream;

public class ComDevReadBase extends Thread {
    private SerialPort serialPort = null;
    public InputStream inputStream = null;
    private boolean isRun = true;

    private boolean isRead = true;
    private byte[] recBytes = new byte[0];

    public ComDevReadBase(SerialPort com) {
        serialPort = com;
        inputStream = com.getInputStream();
        setName("Serial Read Read Thread");
    }

    public void startRead() {
        isRun = true;
        this.start();
    }

    public void stopRead() {
        isRun = false;
        try {
            this.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
            isRun = true;
        }
        try {
            inputStream.close();
            inputStream = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public byte[] getRecBytes() {
        return recBytes;
    }

    public void finishRead() {
        recBytes = new byte[0];
        isRead = true;
    }

    @Override
    public void run() {
        super.run();
        while (isRun) {
            readResult();
        }
    }

    /**
     * 读取串口返回数据
     */
    private void readResult() {
        if (serialPort != null) {
            byte[] bTmp = new byte[1024];
            if (isRead) {
                try {
                    int size = inputStream.read(bTmp);
                    if (size > 0) {
                        recBytes = readBytes(bTmp, size);
                        isRead = false;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /******************************************************************************************************************
     * 串口接收字节数组
     *****************************************************************************************************************/
    private byte[] readBytes(byte[] buffer, int size) {
        byte[] bRec=new byte[size];
        for (int i = 0; i < size; i++) {
            bRec[i]=buffer[i];
        }
        return bRec;
    }

    /**
     * 将指定byte数组以16进制的形式打印到控制台
      * @param b
     */
    private String toHexString( byte b) {
        String hex = Integer.toHexString(b & 0xFF);
        if (hex.length() == 1) {
            hex = '0' + hex;
        }
        return "0X" + hex.toUpperCase();
    }


//                    StringBuffer nByteStr = new StringBuffer("");
//                    for (int i = 0; i < size; i++) {
//                        if (i == (size -1)) {
//                            nByteStr.append(toHexString(tmpByte[i]));
//                        } else {
//                            nByteStr.append(toHexString(tmpByte[i]) + ", ");
//                        }
//                    }
}
