package com.app.drawapp.console;

import com.app.drawapp.DrawContext;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class InputReader {

    private BufferedReader reader;
    private InputStreamReader is;
    private Boolean pipedInput;
    private DrawContext drawContext;

    public InputReader(DrawContext dc){
        is = new InputStreamReader(System.in);
        reader = new BufferedReader(is);
        drawContext = dc;
        try {
            pipedInput = System.in.available() > 0;
        } catch (IOException ioe){
            ioe.printStackTrace();
        }
    }

    public String readLine(){
        String input;
        try {
            input = reader.readLine();
            if (pipedInput && input != null) {
                drawContext.println(input);
            }
            return input;
        } catch (IOException ioe){
            throw new RuntimeException(ioe);
        }
    }

}
