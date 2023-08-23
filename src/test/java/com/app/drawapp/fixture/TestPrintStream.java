package com.app.drawapp.fixture;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class TestPrintStream extends PrintStream {

    private Boolean outputToSysOut;
    private ByteArrayOutputStream os;

    public TestPrintStream(ByteArrayOutputStream out){
        super(out);
        os = out;
        outputToSysOut = false;
    }

    @Override
    public void println(String string){
        super.println(string);
        if (outputToSysOut) {
            System.out.println(string);
        }
    }

    @Override
    public void print(String string){
        super.print(string);
        if (outputToSysOut) {
            System.out.print(string);
        }
    }

    @Override
    public void print(char c) {
        super.print(c);
        if (outputToSysOut) {
            System.out.print(c);
        }
    }

    public void clear() {
        os.reset();
    }

    public String getOutput() {
        String output = out.toString();
        //os.reset();
        return output;
    }

}
