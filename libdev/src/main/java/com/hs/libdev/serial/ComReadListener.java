package com.hs.libdev.serial;

public interface ComReadListener {
    abstract public void readHex(byte[] bytes, int size);
    abstract public void readASCII(String str, int size);
}
