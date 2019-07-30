package com.hs.libdev.serial;

import android.os.Handler;
import android.os.Message;
import com.serial.SerialPort;
import com.serial.SerialPortFinder;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;

public class ComDev {
    private ComReadListener comReadListener = null;
    private SerialPort comDev = null;
    private ComDevWrite devWrite = null;
    private ComDevRead devRead = null;

    public ComDev(ComReadListener comReadListener) {
        this.comReadListener = comReadListener;
    }
    /**
     * 获取串口设备
     * LinkedHashMap<String, String>的Key为设备名，Value为设备路径
     * @return
     */
    public LinkedHashMap<String, String> getDevices() {
        LinkedHashMap<String, String> devices = new LinkedHashMap<>();
        SerialPortFinder sFinder = new SerialPortFinder();
        String[] devicesArray = sFinder.getAllDevicesPath();
        for (String device : devicesArray) {
            String[] tmp = device.split("/");
            String key = tmp[tmp.length-1];
            devices.put(key, device);
        }
        return devices;
    }

    /**
     * 打开串口
     * @param devPath
     * @param baudrate
     * @return
     */
    public boolean open(String devPath, int baudrate) {
        try {
            comDev = new SerialPort(new File(devPath),baudrate,0);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        }
        if (comDev != null) {
            devWrite = new ComDevWrite(comDev);
            devRead = new ComDevRead(comDev, readHandler, ComDevRead.RESULT_TYPE_HEX);
            devRead.startT();
            return true;
        }
        return false;
    }

    /**
     * 关闭串口
     * @return
     */
    public boolean close() {
        devWrite.close();
        devRead.stopT();
        comDev.close();
        comDev = null;
        return true;
    }

    public void setReadType(int readType) {
        devRead.setResultType(readType);
    }

    private Handler readHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                if (comReadListener != null) {
                    switch (msg.arg1) {
                        case ComDevRead.RESULT_TYPE_HEX:
                            comReadListener.readHex((byte[]) msg.obj, msg.arg2);
                            break;
                        case ComDevRead.RESULT_TYPE_ASCII:
                            comReadListener.readASCII(msg.obj.toString(), msg.arg2);
                            break;
                    }
                }
            }
        }
    };

    /**
     * 发送字节
     * @param b
     * @return
     */
    public int write(byte b) {
        if (devWrite != null) {
            return devWrite.writeByte(b);
        }
        return -1;
    }

    /**
     * 发送字节数组
     * @param bs
     * @return
     */
    public int write(byte[] bs) {
        if (devWrite != null) {
            return devWrite.writeBytes(bs);
        }
        return -1;
    }

    /**
     * 发送字符串
     * @param str
     * @return
     */
    public int write(String str) {
        if (devWrite != null) {
            return devWrite.writeStr(str);
        }
        return -1;
    }
}
