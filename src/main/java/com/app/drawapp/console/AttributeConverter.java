package com.app.drawapp.console;

import com.app.drawapp.render.Canvas;
import com.app.drawapp.render.Colour;

import static java.lang.Math.max;
import static java.lang.Math.min;

public abstract class AttributeConverter {

    public abstract Long convert(Canvas.AttributeGroup attribute);

    public abstract Colour nativeToBgColour(Short value);


}
