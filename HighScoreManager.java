package mygame;

import java.io.*;

public class HighScoreManager {
    // No leading slash => relative to where you run the program
    private static final String SCORE_FILE = "src/assets/score.csv";

    /**
     * Reads the best score from 'assets/score.csv'.
     * Returns 0 if the file doesn't exist or can't be parsed.
     */
    public static int readBestScore() {
        File file = new File(SCORE_FILE);
        
        // Debug: print out the absolute path for troubleshooting
        System.out.println("[DEBUG] Reading score from: " + file.getAbsolutePath());

        if (!file.exists()) {
            // If no file yet, we assume best score is 0
            return 0;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line = br.readLine();
            if (line == null || line.isEmpty()) {
                return 0;
            }

            if (line.contains(",")) {
                // e.g., "BestScore,125"
                String[] parts = line.split(",", 2);
                if (parts.length == 2) {
                    try {
                        return Integer.parseInt(parts[1].trim());
                    } catch (NumberFormatException e) {
                        return 0;
                    }
                } else {
                    return 0;
                }
            } else {
                // e.g., just "125"
                return Integer.parseInt(line.trim());
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * Writes the best score to 'assets/score.csv', overwriting any existing file.
     * Also ensures the parent "assets" folder is created if needed.
     */
    public static void writeBestScore(int bestScore) {
        File file = new File(SCORE_FILE);

        // Debug: print out the absolute path for troubleshooting
        System.out.println("[DEBUG] Writing score to: " + file.getAbsolutePath());

        // Ensure parent folder exists
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            boolean madeDirs = parentDir.mkdirs();
            if (madeDirs) {
                System.out.println("[DEBUG] Created directories: " + parentDir.getAbsolutePath());
            }
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            bw.write("BestScore," + bestScore);
            System.out.println("[DEBUG] Wrote BestScore," + bestScore);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
