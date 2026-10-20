/**
 * Read and parse speech data, extract key metadata (president name and date), and perform linguistic calculations across different historical eras.
 *
 * Aarav and Rohan
 * 10/20/26
 */

// imports
import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class Speech {

    // calling all the files
    public static void main(String[] args) throws IOException {
        
        // beginning era
        analyze("speeches/George_Washington_1796-09-19T13_03_58-04_56_September_19,_1796__Farewell_Address.json");
        analyze("speeches/George_Washington_1789-04-30T13_03_58-04_56_April_30,_1789__First_Inaugural_Address.json");
        // middle era
        analyze("speeches/Abraham_Lincoln_1854-10-16T13_03_58-04_56_October_16,_1854__At_Peoria,_Illinois.json");
        analyze("speeches/William_Taft_1910-12-06T13_00_00-05_00_December_6,_1910__Second_Annual_Message.json");
        // end era
        analyze("speeches/Donald_Trump_2020-04-13T17_30_00-04_00_April_13,_2020__Coronavirus_Task_Force_Briefing.json");
        analyze("speeches/Donald_Trump_2025-09-30T10_00_00-04_00_September_30,_2025__Remarks_to_Military_Leaders.json");
    }

    // scanning and cleaning the files
    public static void analyze(String filename) throws IOException {
        
        File file = new File(filename);

        if (!file.exists()) {
            System.out.println("File not found: " + filename);
            System.out.println();
            return;
        }

        Scanner scanner = new Scanner(file);
        String rawJson = "";
        while (scanner.hasNextLine()) {
            rawJson += scanner.nextLine() + " ";
        }
        scanner.close();

        String president = extractJsonValue(rawJson, "president");
        String date = extractJsonValue(rawJson, "date");
        String text = extractJsonValue(rawJson, "text");

        // split words
        String[] words = text.split("\\s+");

        int totalWords = 0;
        int totalChars = 0;
        for (int i = 0; i < words.length; i++) {
            String clean = words[i].replaceAll("[^a-zA-Z]", "");
            if (clean.length() > 0) {
                totalWords++;
                totalChars += clean.length();
            }
        }
        
        double averageLength = 0.0;
        if (totalWords > 0) {
            averageLength = (double) totalChars / totalWords;
        }

        // Count key target words
        int people = countWord(words, "people");
        int government = countWord(words, "government");
        int united = countWord(words, "united");
        int our = countWord(words, "our");

        System.out.println("File: " + filename);
        System.out.println("President: " + president);
        System.out.println("Date: " + date);
        System.out.println("Total words: " + totalWords);
        System.out.println("Average word length: " + averageLength);

        // Check totalWords > 0 to prevent NaN in percentage calculations
        if (totalWords > 0) {
            System.out.println("people: " + people + " (" + (100.0 * people / totalWords) + "%)");
            System.out.println("government: " + government + " (" + (100.0 * government / totalWords) + "%)");
            System.out.println("united: " + united + " (" + (100.0 * united / totalWords) + "%)");
            System.out.println("our: " + our + " (" + (100.0 * our / totalWords) + "%)");
        } else {
            System.out.println("Warning: Could not extract speech text from " + filename);
        }
        System.out.println();
    }

    // replacing all non-letter characters and checking equality
    public static int countWord(String[] words, String target) {
        int count = 0;
        for (int i = 0; i < words.length; i++) {
            String clean = words[i].replaceAll("[^a-zA-Z]", "");
            if (clean.equalsIgnoreCase(target)) {
                count++;
            }
        }
        return count;
    }

    // Extracts string or array values for a given JSON key using AP CSA String methods
    public static String extractJsonValue(String json, String key) {
        String keyTarget = "\"" + key + "\"";
        int keyIndex = json.indexOf(keyTarget);
        if (keyIndex == -1) {
            //check if key is named transcript
            if (key.equals("text")) {
                return extractJsonValue(json, "transcript");
            }
            return "";
        }

        int colonIndex = json.indexOf(":", keyIndex + keyTarget.length());
        if (colonIndex == -1) {
            return "";
        }

        // Check for array values 
        int bracketIndex = json.indexOf("[", colonIndex + 1);
        int firstQuoteIndex = json.indexOf("\"", colonIndex + 1);

        if (bracketIndex != -1 && (firstQuoteIndex == -1 || bracketIndex < firstQuoteIndex)) {
            int closeBracketIndex = json.indexOf("]", bracketIndex + 1);
            if (closeBracketIndex != -1) {
                String arrayContent = json.substring(bracketIndex + 1, closeBracketIndex);
                return arrayContent.replace("\",\"", " ")
                                   .replace("\"", "")
                                   .replace("\\\"", "\"");
            }
        }

        int startQuote = firstQuoteIndex;
        if (startQuote == -1) {
            return "";
        }

        int endQuote = startQuote + 1;
        while (endQuote < json.length()) {
            if (json.charAt(endQuote) == '"') {
                int backslashCount = 0;
                int p = endQuote - 1;
                while (p >= 0 && json.charAt(p) == '\\') {
                    backslashCount++;
                    p--;
                }
                if (backslashCount % 2 == 0) {
                    break;
                }
            }
            endQuote++;
        }

        if (endQuote >= json.length()) {
            return "";
        }

        return json.substring(startQuote + 1, endQuote);
    }
}