package cinema.ui;

import cinema.util.Format;

// Prints a table whose columns always line up. Each column has a fixed width; a value that is too long
// (a long name, email or title) is cut and ends with "...". Example:
//     Table table = new Table(5, 16, 20);
//     table.printHeader("ID", "NAME", "EMAIL");
//     table.printRow("U-2", "Juan Dela Cruz", "juan@mail.com");
public class Table {
    private static final String GAP = "  ";   // space between two columns

    private int[] widths;
    private boolean[] rightAligned;

    public Table(int... widths) {
        this.widths = widths;
        this.rightAligned = new boolean[widths.length];
    }

    // Numbers and money columns look better lined up on the right (columns are counted from 0)
    public void alignRight(int... columns) {
        for (int column : columns) {
            rightAligned[column] = true;
        }
    }

    // The column titles, then a line under them
    public void printHeader(String... titles) {
        printRow(titles);
        Screen.line();
    }

    // One row. Missing cells are left blank.
    public void printRow(String... cells) {
        String line = "  ";
        for (int i = 0; i < widths.length; i++) {
            String cell = "";
            if (i < cells.length && cells[i] != null) {
                cell = cells[i];
            }
            if (i > 0) {
                line += GAP;
            }
            if (rightAligned[i]) {
                line += Format.fitRight(cell, widths[i]);
            } else {
                line += Format.fit(cell, widths[i]);
            }
        }
        // no spaces at the end of the line
        int end = line.length();
        while (end > 0 && line.charAt(end - 1) == ' ') {
            end--;
        }
        System.out.println(line.substring(0, end));
    }
}
