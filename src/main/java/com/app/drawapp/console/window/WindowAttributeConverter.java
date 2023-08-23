package com.app.drawapp.console.window;

import com.app.drawapp.console.AttributeConverter;
import com.app.drawapp.render.Canvas;
import com.app.drawapp.render.Colour;

import static com.app.drawapp.render.Colour.*;
import static com.app.drawapp.render.Colour.WHITE;
import static java.lang.Math.max;
import static java.lang.Math.min;

public class WindowAttributeConverter extends AttributeConverter {


    private final Colour[] colours = {BLACK, RED, GREEN, YELLOW, BLUE, MAGENTA, CYAN, WHITE};
    private final Integer threshold8bit=64;
    private final Short[] background = {Kernel32Native.BACKGROUND_RED, Kernel32Native.BACKGROUND_GREEN, Kernel32Native.BACKGROUND_BLUE, Kernel32Native.BACKGROUND_INTENSITY};
    private final Short[] foreground = {Kernel32Native.FOREGROUND_RED, Kernel32Native.FOREGROUND_GREEN, Kernel32Native.FOREGROUND_BLUE, Kernel32Native.FOREGROUND_INTENSITY};

    public WindowAttributeConverter(){ }

    public Short colourToNative(Colour c, Short[] rgbi){
        Short result = 0;
        Short redComp = c.getRed();
        Short greenComp = c.getGreen();
        Short blueComp = c.getBlue();
        Short minVal = (short)min(min(redComp, greenComp), blueComp);

        Integer rRelToMin = max(redComp - minVal, max(minVal, threshold8bit)) - threshold8bit;
        Integer gRelToMin = max(greenComp - minVal, max(minVal, threshold8bit)) - threshold8bit;
        Integer bRelToMin = max(blueComp - minVal, max(minVal, threshold8bit)) - threshold8bit;

        if (rRelToMin > 0) result = (short)(result | rgbi[0]);
        if (gRelToMin > 0) result = (short)(result | rgbi[1]);
        if (bRelToMin > 0) result = (short)(result | rgbi[2]);

        return result;
    }

    Colour nativeToColour(Short value, Short[] rgbi) {
        Integer[] shifts = {Integer.numberOfTrailingZeros(rgbi[0]),Integer.numberOfTrailingZeros(rgbi[1]),
                Integer.numberOfTrailingZeros(rgbi[2])};
        Integer index = ((rgbi[0] & value)>>>shifts[0]) | ((rgbi[1] & value)>>>shifts[1]-1) | ((rgbi[2] & value)>>>shifts[2]-2);
        return colours[index];
    }

    public Colour nativeToBgColour(Short value) {
        return nativeToColour(value, background);
    }

    @Override
    public Long convert(Canvas.AttributeGroup attribute) {
        Long result = 0L;
        result |= attribute.getText().charAt(0);
        if (attribute.getBgColour().isPresent()) {
            Colour bgColour = attribute.getBgColour().get();
            Colour fgColour = bgColour == WHITE ? BLACK : WHITE;
            Long val = ((-1L>>>-16)&(colourToNative(bgColour, background)|colourToNative(fgColour, foreground)));
            result |= val << 16;
        } else {
            result |= ((-1L>>>-16)&colourToNative(WHITE, foreground)) << 16;
        }
        return result;
    }

}
