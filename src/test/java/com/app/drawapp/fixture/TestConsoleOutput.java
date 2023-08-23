package com.app.drawapp.fixture;

import com.app.drawapp.DrawContext;
import com.app.drawapp.console.ConsoleOutput;
import com.app.drawapp.console.window.WindowConsoleOutput;
import com.app.drawapp.render.Colour;
import com.app.drawapp.render.ConsoleColour;
import com.app.drawapp.render.ProjectTarget;
import com.sun.jna.Platform;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.app.drawapp.render.ConsoleColour.BLACK_CODE;
import static com.app.drawapp.console.window.Kernel32Native.Kernel32.CharInfo;

public class TestConsoleOutput extends ConsoleOutput {

    private ProjectTarget projectTarget;
    private ConsoleOutput consoleOutput;

    public TestConsoleOutput(Integer width, Integer height, DrawContext drawContext) {
        super(width, height, drawContext);
    }

    @Override
    public void draw() {
        //System.out.println("TestConsoleOutput draw()");
        consoleOutput.draw();
        drawContext.printStream.print(getOutput());

    }

    @Override
    public ProjectTarget getCoordProjectTarget() {
        return projectTarget;
    }

    public String getOutput(){
        try {
            if (Platform.isWindows()) {
                Field canvasField = WindowConsoleOutput.class.getDeclaredField("canvas");
                canvasField.setAccessible(true);
                CharInfo[] canvas = (CharInfo[])canvasField.get(consoleOutput);
                StringBuilder sb = new StringBuilder();
                for (int i=0; i<canvas.length; i++){
                    sb.append((char)(canvas[i].Char.getValue()));
                    if ((i+1)% width == 0){
                        sb.append("\r\n");
                    }
                }
                return sb.toString();
            } else {

                TestPrintStream testPrintStream = (TestPrintStream) drawContext.getPrintStream();
                String output = testPrintStream.getOutput();
                testPrintStream.clear();
                return output.replaceAll("\\033\\[1J\\033\\[0;0H","")
                        .replaceAll("[|=]","").replace("\n\n","\n").substring(1);
            }
        }catch(NoSuchFieldException | IllegalAccessException e){
            throw new RuntimeException(e);
        }
    }

    public String getBgColourAttribute() {
        Function<Colour, String> colourStringFunction = colour -> {
                   if (colour == Colour.BLACK)   return BLACK_CODE;
              else if (colour == Colour.RED)     return ConsoleColour.RED_CODE;
              else if (colour == Colour.GREEN)   return ConsoleColour.GREEN_CODE;
              else if (colour == Colour.YELLOW)  return ConsoleColour.YELLOW_CODE;
              else if (colour == Colour.BLUE)    return ConsoleColour.BLUE_CODE;
              else if (colour == Colour.MAGENTA) return ConsoleColour.MAGENTA_CODE;
              else if (colour == Colour.CYAN)    return ConsoleColour.CYAN_CODE;
              else                               return ConsoleColour.WHITE_CODE; }
            ;

        try {
            if (Platform.isWindows()) {
                Field canvasField = WindowConsoleOutput.class.getDeclaredField("canvas");
                canvasField.setAccessible(true);
                CharInfo[] canvas = (CharInfo[])canvasField.get(consoleOutput);
                StringBuilder sb = new StringBuilder();
                for (int i=0; i<canvas.length; i++){
                    CharInfo info = canvas[i];
                    Colour colour = consoleContext.attributeConverter.nativeToBgColour(info.Attributes.getValue());
                    sb.append(colourStringFunction.apply(colour));
                    if ((i+1)% width == 0) {
                        sb.append("\r\n");
                    }
                }
                return sb.toString();
            } else {
                TestPrintStream testPrintStream = (TestPrintStream) drawContext.getPrintStream();
                String output = testPrintStream.getOutput();
                Pattern pattern = Pattern.compile("((\\033\\[(?:\\d+;)*\\d+m)(?:[\\s\\w]+))");
                Matcher matcher = pattern.matcher(output);
                StringBuilder sb = new StringBuilder();
                for (int start=0; matcher.find(); ) { 
                    int end = Math.max(matcher.end(0)-matcher.start(0) + (matcher.end(0) - matcher.start(0)<matcher.end(0) - start?0:matcher.end(0))+matcher.end(0),
                            matcher.end(0)-start+ (matcher.end(0)-start>matcher.end(0)-matcher.start(0)?matcher.end(0):0)+matcher.start(0))-
                                  Math.max(matcher.end(0)-matcher.start(0)+(matcher.end(0)-matcher.start(0)<matcher.end(0)-start?0:matcher.end(0)), matcher.end(0)-start+(matcher.end(0)-start>matcher.end(0)-matcher.start(0)?matcher.end(0):0));

                    String group = output.substring(start, end);
                    start = end;
                    Pattern capture = Pattern.compile("(?:(?:\\033\\[(?:\\d+;)*(\\d+)m)*([\\s\\w]+))");
                    Matcher captureMatcher = capture.matcher(group);
                    captureMatcher.find();

                    String bgCode = captureMatcher.group(1);
                    Colour bgColour = consoleContext.attributeConverter.nativeToBgColour((short)Math.max(Integer.valueOf(Optional.ofNullable(bgCode).orElse("0")), 100));

                    String chars = captureMatcher.group(2);
                    String testCode = colourStringFunction.apply(bgColour);

                    String testCodeString = IntStream.range(0, chars.length()).mapToObj(i -> chars.charAt(i) == '\n' ? "\n" : testCode).collect(Collectors.joining());

                    sb.append(testCodeString);
                    matcher.region(start, output.length());
                }
                return sb.toString();
            }
        } catch (NoSuchFieldException | IllegalAccessException e){
            throw new RuntimeException(e);
        }
    }

    public void setConsoleOutput(ConsoleOutput output){
        consoleOutput = output;
        projectTarget = output.getCoordProjectTarget();
    }


}
