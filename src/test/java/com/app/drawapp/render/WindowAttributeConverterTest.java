package com.app.drawapp.render;

import com.app.drawapp.DrawContext;
import com.app.drawapp.console.window.Kernel32Native;
import com.app.drawapp.console.window.WindowAttributeConverter;
import com.app.drawapp.render.Canvas;
import com.app.drawapp.render.Colour;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class WindowAttributeConverterTest {

    private DrawContext dc;
    private Canvas c;
    private WindowAttributeConverter attributeConverter;

    @BeforeEach
    public void setup(){
        dc = new DrawContext(System.out);
        c = new Canvas(5, 3, dc);
        attributeConverter = new WindowAttributeConverter();
    }

    @Test
    public void testConvertAttribute(){
        Canvas.AttributeGroup attributeGroup = c.new AttributeGroup("X", Colour.GREEN);
        Long value = attributeConverter.convert(attributeGroup);

        Long asciiChar = (-1L>>>-8)&(attributeGroup.getText().charAt(0));
        Long attribute = ((-1L>>>-16)&(Kernel32Native.BACKGROUND_GREEN|Kernel32Native.FOREGROUND_RED|Kernel32Native.FOREGROUND_GREEN|Kernel32Native.FOREGROUND_BLUE)) << 16;
        Long attributeVal = asciiChar | attribute;
        assertEquals(true, Objects.equals(attributeVal, value));
    }


    public String format(Long val){
        String binaryString = String.format("%64s", Long.toBinaryString(Long.reverse(val))).replace(' ', '0');
        StringBuilder sb = new StringBuilder();
        for (Integer i=0; i<8; i++){
            if (sb.length()>0){
                sb.append("\n");
            }
            sb.append(binaryString, i*8, i*8+8);
        }
        return sb.toString();
    }

}
