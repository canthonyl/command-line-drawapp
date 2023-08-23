package com.app.drawapp;

import com.app.drawapp.ui.commands.ArgumentEvaluator;
import com.app.drawapp.ui.commands.ArgumentValue;
import com.app.drawapp.ui.commands.Command;
import com.app.drawapp.ui.commands.CommandBuilder;
import com.app.drawapp.console.InputReader;
import com.app.drawapp.render.Canvas;
import com.app.drawapp.render.ConsoleColour;


import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DrawApp {

    private final List<String> commandHistory;
    private final InputReader inputReader;
    private final DrawContext drawContext;
    private final Map<String, Command> allCommands;

    Function<String, Optional<Integer>> intParser = s -> {
        try { return Optional.of(Integer.parseInt(s)) ; }
        catch (NumberFormatException nfe) { return Optional.empty(); }
    };

    Function<String, Optional<String>> colorParser = s -> ConsoleColour.supportedColors.contains(s.toUpperCase()) ? Optional.of(s) : Optional.empty();

    ArgumentEvaluator<Integer> length = new ArgumentEvaluator<>(
            "Length", intParser, (c,i) -> i > 0, "Length must be > 0");

    ArgumentEvaluator<Integer> coordinates = new ArgumentEvaluator<>(
            "Coordinate", intParser, (c,i) -> i >= 0, "Coordinates cannot be negative");

    ArgumentEvaluator<String> color = new ArgumentEvaluator<>(
            "Color", colorParser, (c, s) -> ConsoleColour.supportedColors.contains(s), "Valid color codes are "+ ConsoleColour.supportedColors);


    public DrawApp(DrawContext drawContext){
        this.inputReader = new InputReader(drawContext);
        this.allCommands = new LinkedHashMap<>();
        this.drawContext = drawContext;
        this.commandHistory = new LinkedList<>();
        registerCommand();
    }

    private void registerCommand(){
        Command createCanvasCommand = new CommandBuilder()
                .command("C")
                .argument("Width", length)
                .argument("Height", length)
                .action(this::createCanvas)
                .build();

        Command createLine = new CommandBuilder()
                .command("L")
                .argument("x1", coordinates)
                .argument("y1", coordinates)
                .argument("x2", coordinates)
                .argument("y2", coordinates)
                .validate(this::validateCanvasPresent)
                .validate(this::validateCoordPairsInRange)
                 .validate(this::validateCoordPairsRepresentStraightLine)
                .action(this::drawLine)
                .build();

        Command createRectangle = new CommandBuilder()
                .command("R")
                .argument("x1", coordinates)
                .argument("y1", coordinates)
                .argument("x2", coordinates)
                .argument("y2", coordinates)
                .validate(this::validateCanvasPresent)
                .validate(this::validateCoordPairsInRange)
                .validate(this::validateCoordPairsDistinct)
                .action(this::drawRectangle)
                .build();

        Command bucketFillCommand = new CommandBuilder()
                .command("B")
                .argument("x1", coordinates)
                .argument("y1", coordinates)
                .argument("Color", color)
                .validate(this::validateCanvasPresent)
                .validate(this::validateCoordInRange)
                .action(this::bucketFill)
                .build();

        Command quitCommand = new CommandBuilder()
                .command("Q")
                .action(dc -> dc.done = true)
                .build();

        allCommands.put("B", bucketFillCommand);
        allCommands.put("C", createCanvasCommand);
        allCommands.put("L", createLine);
        allCommands.put("R", createRectangle);
        allCommands.put("Q", quitCommand);
    }



    private Boolean validateCanvasPresent(DrawContext dc) {
        if (!dc.hasCanvas()) {
            dc.getPrintStream().println("Please create canvas first!");
        }
        return dc.hasCanvas();
    }


    private Boolean validateCoord(Canvas canvas, Integer x, Integer y) {
        Boolean coordsInRange = canvas.coordinatesInRange(x, y);
        if (!coordsInRange) {
            drawContext.getPrintStream().println("Coordinates not in range of canvas of width="+
                    canvas.getCanvasWidth()+" height="+canvas.getCanvasHeight());
        }
        return coordsInRange;
    }

    private Boolean validateCoordInRange(DrawContext dc, List<ArgumentValue> coord){
        Integer x = coord.get(0).asInt();
        Integer y = coord.get(1).asInt();
        if (dc.hasCanvas()) {
            Canvas canvas = dc.canvas.get();
            return validateCoord(canvas, x, y);
        }
        return Boolean.FALSE;
    }

    private Boolean validateCoordPairsInRange(DrawContext dc, List<ArgumentValue> coord){
        Integer x1 = coord.get(0).asInt();
        Integer y1 = coord.get(1).asInt();
        Integer x2 = coord.get(2).asInt();
        Integer y2 = coord.get(3).asInt();
        if (dc.hasCanvas()) {
            Canvas canvas = dc.canvas.get();
            return validateCoord(canvas, x1, y1) && validateCoord(canvas, x2, y2);
        }
        return Boolean.FALSE;
    }

    private boolean validateCoordPairsRepresentStraightLine(DrawContext drawContext, List<ArgumentValue> arguments) {
        Integer x1 = arguments.get(0).asInt();
        Integer y1 = arguments.get(1).asInt();
        Integer x2 = arguments.get(2).asInt();
        Integer y2 = arguments.get(3).asInt();
        Boolean straightLine = x1 == x2 || y1 == y2;
        if (!straightLine) {
            drawContext.getPrintStream().println("Currently only straight line is supported");
        }
        return straightLine;
    }

    private boolean validateCoordPairsDistinct(DrawContext drawContext, List<ArgumentValue> arguments) {
        Integer x1 = arguments.get(0).asInt();
        Integer y1 = arguments.get(1).asInt();
        Integer x2 = arguments.get(2).asInt();
        Integer y2 = arguments.get(3).asInt();
        Boolean coordDistinct = !(x1 == x2 && y1 == y2);
        if (!coordDistinct) {
            drawContext.getPrintStream().println("Coord pairs provided should be distinct");
        }
        return coordDistinct;
    }

    private void createCanvas(DrawContext dc, List<ArgumentValue> arguments){
        Integer width = arguments.get(0).asInt();
        Integer height = arguments.get(1).asInt();
        dc.canvas = Optional.of(new Canvas(width, height, dc));
        dc.canvas.get().drawToConsole();
    }

    private void drawLine(DrawContext dc, List<ArgumentValue> arguments){
        Integer x1 = arguments.get(0).asInt();
        Integer y1 = arguments.get(1).asInt();
        Integer x2 = arguments.get(2).asInt();
        Integer y2 = arguments.get(3).asInt();
        Canvas canvas = dc.getCanvas().get();
        if (x1 > x2 || y1 > y2) {
            canvas.addLine(x2, y2, x1, y1);
        } else {
            canvas.addLine(x1, y1, x2, y2);
        }
        canvas.drawToConsole();
    }

    private void drawRectangle(DrawContext dc, List<ArgumentValue> arguments){
        Integer x1 = arguments.get(0).asInt();
        Integer y1 = arguments.get(1).asInt();
        Integer x2 = arguments.get(2).asInt();
        Integer y2 = arguments.get(3).asInt();
        Canvas canvas = dc.getCanvas().get();
        if (x1 > x2 || y1 > y2) {
            canvas.addRectangle(x2, y2, x1, y1);
        } else {
            canvas.addRectangle(x1, y1, x2, y2);
        }
        canvas.drawToConsole();
    }

    private void bucketFill(DrawContext dc, List<ArgumentValue> arguments){
        Integer x1 = arguments.get(0).asInt();
        Integer y1 = arguments.get(1).asInt();
        String color = arguments.get(2).asString();
        Canvas canvas = dc.canvas.get();
        canvas.bucketFill(x1, y1, color);
        canvas.drawToConsole();
    }

    private void welcomeScreen(){
        drawContext.getConsoleContext().clearConsole();
        drawContext.print("Welcome!\n");
    }

     void processCommand(String input){
        String inputVal = Optional.ofNullable(input).orElse("Q");
        String[] args = inputVal.toUpperCase().split("\\s+");

        String command = args.length > 0 ? args[0] : "";

        if (command.length() == 0 || !allCommands.containsKey(command)) {
            if (command.length() > 0) {
                drawContext.println("Unknown command " + command);
            }
        } else {
            commandHistory.add(input);
            List<String> argumentList = Arrays.stream(args).skip(1)
                    .collect(Collectors.toList());

            allCommands.get(command).process(argumentList, drawContext);
        }
    }

    public void run(){
        welcomeScreen();
        do {
            drawContext.printStream.print("Please enter command: ");
            processCommand(inputReader.readLine());
        } while (!drawContext.isDone());
    }

    private static PrintStream createPrintStream(){
        return System.out;
    }


    public static void main(String[] args) {
        DrawApp app = new DrawApp(new DrawContext(createPrintStream()));
        try {

            if (args.length > 0) {
                String[] arg = args[0].split("=");
                List<String> commands;
                if (arg[0].equals("-c")) {
                    commands = Arrays.asList(arg[1].replace("\"", "").split(";"));
                } else if (arg[0].equals("-f")) {
                    commands = Files.readAllLines(Paths.get(arg[1]));
                } else {
                    System.out.println("Valid arguments are:");
                    System.out.println("  -c=\"<semi-colon separated list of commands>\"");
                    System.out.println("  -f=<filename>");
                    return;
                }

                for (String command : commands) {
                    if (command.trim().length() > 0) {
                        app.processCommand(command.trim());
                    }
                }
                if (commands.size() > 0 && commands.get(commands.size() - 1).equals("Q")) {
                    return;
                }

            }
        } catch (IOException ioe){
            ioe.printStackTrace();
        }
        app.run();
    }
}
