package com.app.drawapp.console.mac;

import com.app.drawapp.console.AttributeConverter;
import com.app.drawapp.render.Canvas;
import com.app.drawapp.render.Colour;

import static com.app.drawapp.render.Colour.BLACK;
import static com.app.drawapp.render.Colour.RED;
import static com.app.drawapp.render.Colour.GREEN;
import static com.app.drawapp.render.Colour.YELLOW;
import static com.app.drawapp.render.Colour.BLUE;
import static com.app.drawapp.render.Colour.MAGENTA;
import static com.app.drawapp.render.Colour.CYAN;
import static com.app.drawapp.render.Colour.WHITE;
import static java.lang.Math.max;
import static java.lang.Math.min;

public class MacAttributeConverter extends AttributeConverter {


    private final Colour[] colours = {BLACK, RED, GREEN, YELLOW, BLUE, MAGENTA, CYAN, WHITE};
    private final Integer foregroundStartVal;
    private final Integer backgroundStartVal;
    private final Integer threshold8bit=64;

    public MacAttributeConverter(){
        foregroundStartVal = 30;
        backgroundStartVal = 100;
    }

    Integer colorCode(Colour c, Integer startVal){
        Short result = 0;
        Short redComp = c.getRed();
        Short greenComp = c.getGreen();
        Short blueComp = c.getBlue();
        Short minVal = (short)min(min(redComp, greenComp), blueComp);

        Integer rRelToMin = max(redComp - minVal, max(minVal, threshold8bit)) - threshold8bit;
        Integer gRelToMin = max(greenComp - minVal, max(minVal, threshold8bit)) - threshold8bit;
        Integer bRelToMin = max(blueComp - minVal, max(minVal, threshold8bit)) - threshold8bit;

        if (rRelToMin > 0) result = (short)(result | (1<<0));
        if (gRelToMin > 0) result = (short)(result | (1<<1));
        if (bRelToMin > 0) result = (short)(result | (1<<2));

        return startVal + result;
    }

    @Override
    public Long convert(Canvas.AttributeGroup attribute) {
        Long result = 0L;
        result |= attribute.getText().charAt(0);

        if (attribute.getBgColour().isPresent()) {
            Colour bgColour = attribute.getBgColour().get();
            Colour fgColour = bgColour == WHITE ? BLACK : WHITE;
            Long fgAttributes = (-1L^((-1L^(0L))&(-1L^((-1L>>>-8)&colorCode(fgColour, foregroundStartVal)))));
            Long bgAttributes = (-1L^((-1L^(0L))&(-1L^((-1L>>>-8)&colorCode(bgColour, backgroundStartVal)))))<<8;
            result |= (bgAttributes | fgAttributes)<<16;
        } else {
            Colour bgColour = BLACK;
            Colour fgColour = WHITE;
            Long fgAttributes = (-1L^((-1L^(0L))&(-1L^((-1L>>>-8)&colorCode(fgColour, foregroundStartVal)))));
            Long bgAttributes = (-1L^((-1L^(0L))&(-1L^((-1L>>>-8)&colorCode(bgColour, backgroundStartVal)))))<<8;
            result |= (bgAttributes | fgAttributes)<<16;
        }

        return result;
    }

    @Override
    public Colour nativeToBgColour(Short value) {
        return colours[value-backgroundStartVal];
    }
}
