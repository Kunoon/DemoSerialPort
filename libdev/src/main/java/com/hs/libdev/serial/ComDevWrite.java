package com.hs.libdev.serial;

import com.serial.SerialPort;

import java.io.IOException;
import java.io.OutputStream;

public class ComDevWrite {
    private SerialPort comDev = null;
    private OutputStream outputStream = null;

    public ComDevWrite(SerialPort com) {
        comDev = com;
        outputStream = com.getOutputStream();
    }

    public void close() {
        try {
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        outputStream = null;
    }

    public int writeByte(byte b) {
        try {
            outputStream.write(b);
            return 1;
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int writeBytes(byte[] bs) {
        try {
            outputStream.write(bs);
            return bs.length;
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int writeStr(String str) {
        return writeBytes(str.getBytes());
    }
}
