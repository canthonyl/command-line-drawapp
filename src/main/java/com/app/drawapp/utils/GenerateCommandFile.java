package com.app.drawapp.utils;

import java.io.*;

public class GenerateCommandFile {

    public static void main(String[] args) {
        try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(args[0])))){

            Integer canvasWidth = 50;
            Integer canvasHeight = 30;
            writer.println("C " + canvasWidth + " " + canvasHeight);

            Integer x = 0;
            Integer y = 0;
            Integer width = 45;
            Integer height = 35;
            for (int i = 0; i < 100; i++) {
                x = (x + 2 * i) % canvasWidth;
                y = (y + 2 * i) % canvasHeight;
                Integer endX = Math.min((x + width - 1), canvasWidth - 1);
                Integer endY = Math.min((y + height - 1), canvasHeight - 1);
                writer.println("R " + x + " " + y + " " + endX + " " + endY);
            }

        } catch (IOException ioe){
            ioe.printStackTrace();
        }
    }
}
