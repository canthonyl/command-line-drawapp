package com.app.drawapp.render;

import com.app.drawapp.DrawContext;
import com.app.drawapp.console.mac.MacAttributeConverter;
import com.app.drawapp.render.Canvas;
import com.app.drawapp.render.Colour;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MacAttributeConverterTest {

    private DrawContext dc;
    private Canvas c;
    private MacAttributeConverter attributeConverter;

    @BeforeEach
    public void setup(){
        dc = new DrawContext(System.out);
        c = new Canvas(5, 3, dc);
        attributeConverter = new MacAttributeConverter();
    }

    @Test
    public void testConvertAttributeGroup(){
        Canvas.AttributeGroup attributeGroup = c.new AttributeGroup("X", Colour.GREEN);
        Long value = attributeConverter.convert(attributeGroup);

        Long asciiChar = (-1L>>>-8)&(attributeGroup.getText().charAt(0));
        Long fgAttribute = 37L<<16;
        Long bgAttribute = 102L<<24;
        Long attributeVal = asciiChar | fgAttribute | bgAttribute;
        assertEquals(true, Objects.equals(attributeVal, value));

    }

}
