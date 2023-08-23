
Draw! is a command line program that accepts draw instruction from terminal
and outputs the resulting canvas.

Build:
----------------------------------------------------
1) Build the test c library under native

2) Build the project using gradle

Run:
----------------------------------------------------

1) Run with the provided jar files in the build/libs folder folder

2) You'll be greeted with a command prompt:

    Welcome!
    Please enter command:

3) Use the following commands to start drawing:

    C w h           Create a new canvas of width w and height h.

    L x1 y1 x2 y2   Create a new line from (x1,y1) to (x2,y2).

    R x1 y1 x2 y2   Create a new rectangle, whose upper left corner is (x1,y1) and
                    lower right corner is (x2,y2).

    B x y c         Fills the entire area connected to (x,y) with "colour" c.

    Q               Quit the program.

4) Enjoy!

