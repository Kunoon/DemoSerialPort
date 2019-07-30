package com.hs.demoserialport;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.*;
import com.hs.libdev.serial.ComDev;
import com.hs.libdev.serial.ComDevRead;
import com.hs.libdev.serial.ComReadListener;

import java.util.LinkedHashMap;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,
        RadioGroup.OnCheckedChangeListener {
    private ComDev comDev = null;
    private LinkedHashMap<String, String> decives = null;
    private String[] devicesName = null;
    private String[] baudrates = null;
    private String comPath = null;
    private int baudrate = 0;
    private boolean devIsOpen = false;
    private Spinner spinnerSerialPorts = null, spinnerSerialBaudrate = null;
    private Button btnComOpenOrClose = null, btnSendData = null;
    private TextView tvReceiveAir = null;
    private TextView tvReceiveBDCnum = null, tvSendDBCnum = null,tvReceiveBDNum = null, tvSendBDNum = null;
    private CheckBox checkBoxRecevieEnter, checkBoxRecevieAutoSend, checkBoxSendAgain;
    private RadioGroup radioGroupRecevie, radioGroupSend;
    private EditText etSendDAir = null, etSendGapTimeMS = null;

    private int rxCnum = 0, txCnum = 0, rxNum = 0, txNum = 0;

    private int gapTimeMs = 0;
    private SendAutoThread sendAutoThread = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        comDev = new ComDev(comReadListener);
        decives = comDev.getDevices();
        devicesName = new String[decives.size()];
        int index = 0;
        for (String key : decives.keySet()) {
            devicesName[index++] = key;
        }
        ArrayAdapter<String> adapter=new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, devicesName);
        spinnerSerialPorts.setAdapter(adapter);
        baudrates = getResources().getStringArray(R.array.baudrate);
    }

    @Override
    protected void onResume() {
        super.onResume();
        gapTimeMs = Integer.valueOf(etSendGapTimeMS.getText().toString().trim());
        sendAutoThread = new SendAutoThread();
        sendAutoThread.startT();
    }

    @Override
    protected void onStop() {
        super.onStop();
        sendAutoThread.stopT();
    }

    private void initView() {
        spinnerSerialPorts = findViewById(R.id.spinner_serialport_set_portnum);
        spinnerSerialBaudrate = findViewById(R.id.spinner_serialport_set_baudrate);
        btnComOpenOrClose = findViewById(R.id.btn_serialport_open_or_close);
        tvReceiveBDCnum = findViewById(R.id.tv_serialport_receive_data_byte_cnum);
        tvSendDBCnum = findViewById(R.id.tv_serialport_send_data_byte_cnum);
        tvReceiveAir = findViewById(R.id.tv_serialport_receive_data_air);
        tvReceiveAir.setMovementMethod(new ScrollingMovementMethod());
        tvReceiveBDNum = findViewById(R.id.tv_serialport_receive_data_byte_num);
        tvSendBDNum = findViewById(R.id.tv_serialport_send_data_byte_num);
        etSendDAir = findViewById(R.id.et_serialport_send_data_air);
        btnSendData = findViewById(R.id.btn_serialport_send_data);
        radioGroupRecevie = findViewById(R.id.radiogroup_set_serialport_receive_type);
        radioGroupSend = findViewById(R.id.radiogroup_set_serialport_send_type);
        radioGroupRecevie.setOnCheckedChangeListener(this);
        radioGroupSend.setOnCheckedChangeListener(this);
        checkBoxRecevieEnter = findViewById(R.id.chbox_serialport_set_receive_enter);
        checkBoxRecevieAutoSend = findViewById(R.id.chbox_serialport_set_receive_auto_send);
        checkBoxSendAgain = findViewById(R.id.chbox_serialport_set_send_again);
        etSendGapTimeMS = findViewById(R.id.et_serialport_set_send_gap_time);

        etSendGapTimeMS.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                gapTimeMs = Integer.valueOf(etSendGapTimeMS.getText().toString().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        spinnerSerialPorts.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                comPath = decives.get(devicesName[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        spinnerSerialBaudrate.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                baudrate = Integer.valueOf(baudrates[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        btnComOpenOrClose.setOnClickListener(this);
        findViewById(R.id.btn_serialport_count_data_byte_num_restart).setOnClickListener(this);
        findViewById(R.id.btn_serialport_clean_receive_data).setOnClickListener(this);
        findViewById(R.id.btn_serialport_clean_send_data).setOnClickListener(this);
        btnSendData.setOnClickListener(this);
    }

    public ComReadListener comReadListener = new ComReadListener() {
        @Override
        public void readHex(byte[] bytes, int size) {
            StringBuffer tmpStr = new StringBuffer("");
            int length = bytes.length;
            for (int i = 0; i < length; i++) {
                if (i == (length - 1)) {
                    tmpStr.append(toHexString(bytes[i]));
                } else {
                    tmpStr.append(toHexString(bytes[i]) + " ");
                }
            }
            setReceiveView(tmpStr.toString(), size);
        }

        @Override
        public void readASCII(String str, int size) {
            setReceiveView(str, size);
        }
    };

    private void setReceiveView(String showStr, int size) {
        String enterStr = null;
        if (checkBoxRecevieEnter.isChecked()) {
            enterStr = "\n";
        } else {
            enterStr = "";
        }
        tvReceiveAir.append(showStr + enterStr);
        rxNum = size;
        rxCnum += size;
        tvReceiveBDNum.setText(String.format("RX：本次接收%s字节", rxNum));
        tvReceiveBDCnum.setText(rxCnum+"");
        if (checkBoxRecevieAutoSend.isChecked()) {
            sendDataToCom();
        }
    }

    private void sendDataToCom() {
        if (devIsOpen) {
            byte[] sendByte = strToHex(etSendDAir.getText().toString());
            if (sendByte != null && sendByte.length > 0) {
                txNum = comDev.write(sendByte);
                txCnum += txNum;
                tvSendBDNum.setText(String.format("TX：本次发送%d字节", txNum));
                tvSendDBCnum.setText(txCnum+"");
            }
        } else {
            Toast.makeText(getApplicationContext(), "请确认已开启串口", Toast.LENGTH_SHORT).show();
        }
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
        return hex.toUpperCase();
//        return "0X" + hex.toUpperCase();
    }

    private byte[] strToHex(String str) {
        if (sendHex) {
            try{
                String[] strArray = str.split(" ");
                int bSize = strArray.length;
                byte[] bytes = new byte[bSize];
                for (int i = 0; i < bSize; i++) {
                    byte b = (byte) Integer.parseInt(strArray[i].replaceAll("^0[x|X]", ""), 16);
                    bytes[i] = b;
                }
                return bytes;
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        } else {
            return str.getBytes();
        }
        return null;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_serialport_open_or_close:
                if (devIsOpen) {
                    comDev.close();
                    devIsOpen = false;
                    btnComOpenOrClose.setText(" 打 开 串 口 ");
                } else {
                    devIsOpen = comDev.open(comPath, baudrate);
                    if (devIsOpen) {
                        btnComOpenOrClose.setText(" 关 闭 串 口 ");
                    }
                }
                break;
            case R.id.btn_serialport_count_data_byte_num_restart:
                rxCnum = 0;
                rxNum = 0;
                txCnum = 0;
                txNum = 0;
                tvSendBDNum.setText(String.format("TX：本次发送%d字节", rxNum));
                tvSendDBCnum.setText(rxCnum+"");
                tvReceiveBDNum.setText(String.format("RX：本次接收%d字节", rxNum));
                tvReceiveBDCnum.setText(rxCnum+"");
                break;
            case R.id.btn_serialport_clean_receive_data:
                tvReceiveAir.setText("");
                break;
            case R.id.btn_serialport_clean_send_data:
                etSendDAir.setText("");
                break;
            case R.id.btn_serialport_send_data:
                sendDataToCom();
                break;
        }
    }

    private boolean sendHex = true;
    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (group.getId()) {
            case R.id.radiogroup_set_serialport_receive_type:
                if (checkedId == R.id.radio_set_serialport_receive_type_ascii) {
                    comDev.setReadType(ComDevRead.RESULT_TYPE_ASCII);
                } else if (checkedId == R.id.radio_set_serialport_receive_type_hex) {
                    comDev.setReadType(ComDevRead.RESULT_TYPE_HEX);
                }
                break;
            case R.id.radiogroup_set_serialport_send_type:
                if (checkedId == R.id.radio_set_serialport_send_type_ascii) {
                    sendHex = false;
                } else if (checkedId == R.id.radio_set_serialport_send_type_hex) {
                    sendHex = true;
                }
                break;
        }
    }

    private class SendAutoThread extends Thread {
        private boolean isRun = true;

        public void startT() {
            isRun = true;
            this.start();
        }

        public void stopT() {
            isRun = false;
            try {
                this.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        @Override
        public void run() {
            super.run();
            while (isRun) {
                if (checkBoxSendAgain.isChecked()) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            sendDataToCom();
                        }
                    });
                    try {
                        sleep(gapTimeMs);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
