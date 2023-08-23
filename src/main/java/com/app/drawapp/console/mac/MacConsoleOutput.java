package com.app.drawapp.console.mac;

import com.app.drawapp.DrawContext;
import com.app.drawapp.console.ConsoleOutput;
import com.app.drawapp.render.Canvas;
import com.app.drawapp.render.Colour;
import com.app.drawapp.render.ConsoleColour;
import com.app.drawapp.render.ProjectTarget;

import java.io.PrintStream;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class MacConsoleOutput extends ConsoleOutput implements ProjectTarget<Canvas.AttributeGroup, Canvas.AttributeGroup> {

    public static final String[] borderElements = { "=", "=", "=", "=", "=", "|"};
    public static final String upperLeft = borderElements[0];
    public static final String upperRight = borderElements[1];
    public static final String lowerLeft = borderElements[2];
    public static final String lowerRight = borderElements[3];
    public static final String horizontalBorder = borderElements[4];
    public static final String verticalBorder = borderElements[5];
    public static final String lineBreak = System.lineSeparator();


    private final Element[][] elements;
    private final String border;
    private final String topBorder;
    private final String bottomBorder;
    private final PrintStream printStream;
    private Function<Canvas.AttributeGroup, Canvas.AttributeGroup> converter;

    public MacConsoleOutput(Integer width, Integer height, DrawContext context) {
        super(width, height, context);
        printStream = context.getPrintStream();
        border = repeat(horizontalBorder, width);
        topBorder = upperLeft+border+upperRight;
        bottomBorder = lowerLeft+border+lowerRight;

        elements = new Element[this.height][this.width];
        for (Integer y = 0; y< this.height; y++) {
            elements[y] = IntStream.range(0, this.width).mapToObj(i -> new Element(0, this.width -1)).toArray(Element[]::new);
        }
    }

    private String repeat(String text, Integer numTimes){
        return Stream.generate(() -> text).limit(numTimes).collect(Collectors.joining());
    }

    private void updateStartEnd(Integer y, Integer x1, Integer x2){
        for (int i=x1; i<=x2; i++){
            Element element = elements[y][i];
            element.startIndex = x1;
            element.endIndex = x2;
        }
    }

    @Override
    public void setConverter(Function<Canvas.AttributeGroup, Canvas.AttributeGroup> attributeGroupConverter) {
        converter = attributeGroupConverter;
    }

    @Override
    public void updateRange(Integer x1, Integer y1, Integer x2, Integer y2, Canvas.AttributeGroup group) {
        Canvas.AttributeGroup attributeGroup = converter.apply(group);
        for (int y=y1; y<=y2; y++){
            if (x1 > 0)  updateStartEnd(y, elements[y][x1-1].startIndex, x1-1);
            if (x2 < width -1) updateStartEnd(y, x2+1, elements[y][x2+1].endIndex);

            for (int x=x1; x<=x2; x++){
                Element element = elements[y][x];
                element.startIndex = x1;
                element.endIndex = x2;
                element.attribute = attributeGroup;
            }
        }
    }


    private String bgColourEscapeCode(Colour colour) {
        if (colour == Colour.BLACK)
            return ConsoleColour.BLACK_BACKGROUND_BRIGHT;
        else if (colour == Colour.RED)
            return ConsoleColour.RED_BACKGROUND_BRIGHT;
        else if (colour == Colour.GREEN)
            return ConsoleColour.GREEN_BACKGROUND_BRIGHT;
        else if (colour == Colour.YELLOW)
            return ConsoleColour.YELLOW_BACKGROUND_BRIGHT;
        else if (colour == Colour.BLUE)
            return ConsoleColour.BLUE_BACKGROUND_BRIGHT;
        else if (colour == Colour.MAGENTA)
            return ConsoleColour.MAGENTA_BACKGROUND_BRIGHT;
        else if (colour == Colour.CYAN)
            return ConsoleColour.CYAN_BACKGROUND_BRIGHT;
        else
            return ConsoleColour.WHITE_BACKGROUND_BRIGHT;
    }

    @Override
    public void draw(){

        drawContext.print(ConsoleColour.CLEAR_TO_CURSOR);
        drawContext.print(ConsoleColour.MOVE_CURSOR);
        printStream.println(topBorder);

        for (int y = 0; y< height; y++){
            Optional<Colour> prevColour = Optional.empty();

            StringBuilder line = new StringBuilder();
            line.append(verticalBorder);
            for (int x=0 ; x< width; ) {
                Element element = elements[y][x];
                Canvas.AttributeGroup attribute = element.attribute;
                if (!prevColour.equals(attribute.getBgColour())) {

                    line.append(attribute.getBgColour().map(this::bgColourEscapeCode).orElse(ConsoleColour.RESET));

                    prevColour = attribute.getBgColour();
                }
                line.append(repeat(attribute.getText(), element.endIndex - element.startIndex + 1));
                x = element.endIndex+1;
            }

            if (prevColour.isPresent()) {
                line.append(ConsoleColour.RESET);
            }
            line.append(verticalBorder).append(lineBreak);

            printStream.print(line);
        }

        printStream.println(bottomBorder);
    }

    @Override
    public ProjectTarget getCoordProjectTarget() {
        setConverter(Function.identity());
        return this;
    }

    private class Element {
        private Integer startIndex;
        private Integer endIndex;
        private Canvas.AttributeGroup attribute;

        private Element(Integer start, Integer end) {
            startIndex = start;
            endIndex = end;
        }
    }

}
