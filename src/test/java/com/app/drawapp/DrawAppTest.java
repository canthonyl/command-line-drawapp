package com.app.drawapp;

import com.app.drawapp.fixture.TestConsoleOutput;
import com.app.drawapp.fixture.TestDrawContext;
import com.app.drawapp.fixture.TestPrintStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.time.Clock;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DrawAppTest {

    private Clock clock;
    private DrawApp drawApp;
    private TestPrintStream testOut;
    private TestDrawContext testDrawContext;
    private TestConsoleOutput testConsoleOutput;
    private String lineBreak;

    @BeforeEach
    public void setup(){
        clock = Clock.systemDefaultZone();
        testOut = new TestPrintStream(new ByteArrayOutputStream());
        testDrawContext= new TestDrawContext(testOut);
        drawApp = new DrawApp(testDrawContext);
        lineBreak = System.lineSeparator();
    }

    @Test
    public void blankCanvasDisplayedUponCreation(){
        drawApp.processCommand("C 10 2");
        testConsoleOutput = (TestConsoleOutput)testDrawContext.getCanvas().get().getConsoleOutput();
        String expected =
            "          "+lineBreak+
            "          "+lineBreak;

        String actual = charsOutput(testOut.getOutput());
        assertEquals(expected, actual);
        
    }

    @Test
    public void canvasAcrossBucketSizeToScreenWidth(){
        drawApp.processCommand("C 120 25");
        String expected = expectedBlankCanvas(120, 25);
        String actual = charsOutput(testOut.getOutput());
        assertEquals(expected, actual);
        
    }

    @Test
    public void drawHorizontalLine(){
        drawApp.processCommand("C 10 2");
        testOut.clear();

        drawApp.processCommand("L 1 0 5 0");
        String expected =
                " XXXXX    "+lineBreak+
                "          "+lineBreak;
        String actual = charsOutput(testOut.getOutput());
        assertEquals(expected, actual);
        
    }

    @Test
    public void drawVerticalLine(){
        drawApp.processCommand("C 10 3");
        testOut.clear();

        drawApp.processCommand("L 1 0 1 2");
        String expected =
                " X        "+lineBreak+
                " X        "+lineBreak+
                " X        "+lineBreak;
        String actual = charsOutput(testOut.getOutput());
        assertEquals(expected, actual);
        
    }


    @Test
    public void drawRectangle(){
        drawApp.processCommand("C 10 3");
        testOut.clear();

        drawApp.processCommand("R 1 0 3 2");
        String expected =
                " XXX      " + lineBreak +
                " X X      " + lineBreak +
                " XXX      " + lineBreak;
        String actual = charsOutput(testOut.getOutput());
        assertEquals(expected, actual);
        
    }

    @Test
    public void bucketFillRectangleBorder(){
        drawApp.processCommand("C 10 3");
        testConsoleOutput = (TestConsoleOutput)testDrawContext.getCanvas().get().getConsoleOutput();

        drawApp.processCommand("R 1 0 3 2");
        testOut.clear();

        drawApp.processCommand("B 1 0 y");

        String expectedChars =
                " XXX      "+lineBreak+
                " X X      "+lineBreak+
                " XXX      "+lineBreak;

        String expectedColour =
                "BYYYBBBBBB"+lineBreak+
                "BYBYBBBBBB"+lineBreak+
                "BYYYBBBBBB"+lineBreak;

        String actualChars = charsOutput(testOut.getOutput());
        String actualColour = testConsoleOutput.getBgColourAttribute();

        assertEquals(expectedChars, actualChars);;
        assertEquals(expectedColour, actualColour);
        
    }

    @Test
    public void bucketFillRectangleArea(){
        drawApp.processCommand("C 10 5");
        testConsoleOutput = (TestConsoleOutput)testDrawContext.getCanvas().get().getConsoleOutput();

        drawApp.processCommand("R 1 1 3 3");

        testOut.clear();

        drawApp.processCommand("B 2 2 y");
        String expectedChars =
                "          "+lineBreak+
                " XXX      "+lineBreak+
                " X X      "+lineBreak+
                " XXX      "+lineBreak+
                "          "+lineBreak;

        String expectedColour = 
                "BBBBBBBBBB"+lineBreak+
                "BBBBBBBBBB"+lineBreak+
                "BBYBBBBBBB"+lineBreak+
                "BBBBBBBBBB"+lineBreak+
                "BBBBBBBBBB"+lineBreak;

        String actualChars = charsOutput(testOut.getOutput());
        String actualColour = testConsoleOutput.getBgColourAttribute();

        assertEquals(expectedChars, actualChars);
        assertEquals(expectedColour, actualColour);
        
    }


    @Test
    public void bucketFillEntireCanvas(){
        drawApp.processCommand("C 10 2");
        testConsoleOutput = (TestConsoleOutput)testDrawContext.getCanvas().get().getConsoleOutput();
        testOut.clear();
        drawApp.processCommand("B 1 1 y");

        String expectedChars =
                "          "+lineBreak+
                "          "+lineBreak;

        String expectedColour =
                "YYYYYYYYYY"+lineBreak+
                "YYYYYYYYYY"+lineBreak;

        String actualChars = charsOutput(testOut.getOutput());
        String actualColour = testConsoleOutput.getBgColourAttribute();

        assertEquals(expectedChars, actualChars);
        assertEquals(expectedColour, actualColour);
        
    }

    @Test
    public void bucketFillOnlyPaintsClosedOffRegion(){
        drawApp.processCommand("C 10 2");
        testConsoleOutput = (TestConsoleOutput)testDrawContext.getCanvas().get().getConsoleOutput();
        drawApp.processCommand("L 5 0 5 1");
        String expectedChars =
                "     X    "+lineBreak+
                "     X    "+lineBreak;

        testOut.clear();
        drawApp.processCommand("B 1 1 y");

        String expectedColour =
                "YYYYYBBBBB"+lineBreak+
                "YYYYYBBBBB"+lineBreak;

        String actualChars = charsOutput(testOut.getOutput());
        String actualColour = testConsoleOutput.getBgColourAttribute();

        assertEquals(expectedChars, actualChars);
        assertEquals(expectedColour, actualColour);

        testOut.clear();
        drawApp.processCommand("B 7 0 g");

        String expectedChars2 =
                "     X    "+lineBreak+
                "     X    "+lineBreak;

        String expectedColour2 =
                "YYYYYBGGGG"+lineBreak+
                "YYYYYBGGGG"+lineBreak;


        String actualChars2 = charsOutput(testOut.getOutput());
        String actualColour2 = testConsoleOutput.getBgColourAttribute();
        assertEquals(expectedChars2, actualChars2);
        assertEquals(expectedColour2, actualColour2);
        
    }

    @Test
    public void bucketFillExistingColoredRegionWithNewColor(){
        drawApp.processCommand("C 10 2");
        testConsoleOutput = (TestConsoleOutput)testDrawContext.getCanvas().get().getConsoleOutput();
        drawApp.processCommand("L 5 0 5 1");

        testOut.clear();
        drawApp.processCommand("B 1 1 y");

        String expectedChars =
                "     X    "+lineBreak+
                "     X    "+lineBreak;

        String expectedColour =
                "YYYYYBBBBB"+lineBreak+
                "YYYYYBBBBB"+lineBreak;

        String actualChars = charsOutput(testOut.getOutput());
        String actualColour = testConsoleOutput.getBgColourAttribute();
        assertEquals(expectedChars, actualChars);
        assertEquals(expectedColour, actualColour);

        testOut.clear();
        drawApp.processCommand("B 0 0 g");
        String expectedChars2 =
                "     X    "+lineBreak+
                "     X    "+lineBreak;
        String expectedColour2 =
                "GGGGGBBBBB"+lineBreak+
                "GGGGGBBBBB"+lineBreak;

        String actualChars2 = charsOutput(testOut.getOutput());
        String actualColour2 = testConsoleOutput.getBgColourAttribute();
        assertEquals(expectedChars2, actualChars2);
        assertEquals(expectedColour2, actualColour2);
        
    }


    @Test
    public void bucketFillAtLineFillsLine(){
        drawApp.processCommand("C 10 3");
        testConsoleOutput = (TestConsoleOutput)testDrawContext.getCanvas().get().getConsoleOutput();

        drawApp.processCommand("L 3 0 3 2");
        testOut.clear();
        drawApp.processCommand("B 3 0 y");

        String expectedChars =
                "   X      "+lineBreak+
                "   X      "+lineBreak+
                "   X      "+lineBreak;

        String expectedColour =
                "BBBYBBBBBB"+lineBreak+
                "BBBYBBBBBB"+lineBreak+
                "BBBYBBBBBB"+lineBreak;

        String actualChars = charsOutput(testOut.getOutput());
        String actualColour = testConsoleOutput.getBgColourAttribute();

        assertEquals(expectedChars, actualChars);
        assertEquals(expectedColour, actualColour);
        
    }

    private String charsOutput(String output){
        return output.replaceAll("\\033\\[(?:\\d+;)*\\d+[A-Za-z]","");
    }

    private String expectedBlankCanvas(Integer width, Integer height){
        String canvasArea = new String(IntStream.range(0, width).map(c -> ' ').toArray(), 0, width);
        String canvasRegion = IntStream.range(0, height).mapToObj(l -> canvasArea).collect(Collectors.joining(lineBreak, "", lineBreak));
        String expected = canvasRegion;
        return expected;
    }


}
