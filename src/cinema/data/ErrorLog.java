package cinema.data;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;

import cinema.util.Validator;

// Saves unexpected program errors (bugs) to data/error.log with the time and the full Java details.
// The program keeps running, and the team can open the log to see exactly what went wrong and where.
public class ErrorLog {
    public static final String FILE_NAME = "error.log";

    public static void write(Exception error) {
        File file = new File(FileManager.FOLDER, FILE_NAME);
        try {
            file.getParentFile().mkdirs();
            try (PrintWriter out = new PrintWriter(new FileWriter(file, true))) {   // true = add to the end
                out.println("==== " + LocalDateTime.now().format(Validator.DATE_TIME_FORMAT));
                error.printStackTrace(out);
            }
        } catch (IOException e) {
            System.out.println("  [ !! ] Could not write " + file.getPath() + ": " + e.getMessage());
        }
    }
}
