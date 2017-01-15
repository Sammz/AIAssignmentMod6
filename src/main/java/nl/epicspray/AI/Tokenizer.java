package nl.epicspray.AI;

import nl.epicspray.AI.exceptions.CouldNotStartTokenizingException;
import nl.epicspray.AI.exceptions.IllegalFileNameException;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Created by Sam on 14-1-2017.
 */
public class Tokenizer {

    private String[] stopWordsList;

    // Make tokenizer, instantiates the stopWordsList.
    public Tokenizer() {
        stopWordsList = cleanString(Protocol.stopWordsList).split("\\n");
    }

    // Makes a map of the class of the file linked to a map of a word and the number of times that word occurs in the file from all files in the given folder.
    public Map<Map<String, Integer>, String> tokenizeFolder(File folder, List<String> classes) throws IllegalFileNameException, CouldNotStartTokenizingException {
        if (folder.exists() && folder.isDirectory() && (classes.equals(Protocol.genderClass) || classes.equals(Protocol.mailClass))) {
            Map<Map<String, Integer>, String> docs = new HashMap<Map<String, Integer>, String>();
            String fileClass;
            for (File file : folder.listFiles()) {
                String fileName = file.getName();
                if (classes.equals(Protocol.genderClass)) {
                    if (fileName.contains("M")) {
                        fileClass = classes.get(0);
                    } else if (fileName.contains("F")) {
                        fileClass = classes.get(1);
                    } else throw new IllegalFileNameException("File: " + fileName + "    has no M or F identifier.");
                } else {
                    if (fileName.contains("p")) {
                        fileClass = classes.get(0);
                    } else
                        fileClass = classes.get(1);
                }
                docs.put(tokenizeFile(file), fileClass);
            }
            return docs;
        } else
            throw new CouldNotStartTokenizingException("Given file doesn't exist or is no folder, or list of classes is not conform protocol.");
    }

    // Tokenize a file
    private Map<String, Integer> tokenizeFile(File file) {
        Map<String, Integer> fileMap = new HashMap<String, Integer>();
        Scanner in = null;
        try {
            in = new Scanner(file);
            while (in.hasNext()) {
                String s = cleanString(in.next());
                if (!isStopWord(s) && !s.equals("")) {
                    if (!fileMap.containsKey(s)) {
                        fileMap.put(s, 1);
                    } else {
                        int i = fileMap.get(s);
                        fileMap.replace(s, i, i + 1);
                    }
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return fileMap;
    }

    // Check if word is a stopword
    private boolean isStopWord(String word) {
        boolean isStopWord = false;
        for (String stopWord : stopWordsList) {
            if (word.equals(stopWord)) {
                isStopWord = true;
                break;
            }
        }
        return isStopWord;
    }

    // Removes all punctuation and integers from the given string and sets all letters to lowercase
    private String cleanString(String word) {
        word = word.replaceAll("\\p{Punct}", "");
        word = word.replaceAll("[0-9]", "");
        word = word.toLowerCase();
        return word;
    }
}
