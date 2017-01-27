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
        stopWordsList = cleanString(StopWordsList.stopWordsList).split("\\n");
    }

    // Makes a map of the class of the file linked to a map of a word and the number of times that word occurs in the file from all files in the given folder.
    public Map<Map<String, Integer>, String> tokenizeFolder(String option, File folder, List<String> classes) throws IllegalFileNameException, CouldNotStartTokenizingException {

        Map<Map<String, Integer>, String> docs = new HashMap<Map<String, Integer>, String>();
            if (folder.exists() && folder.isDirectory()) {

                if(classes.size() == folder.listFiles().length) {

                    for (File fileFolder : folder.listFiles()) {
                        if (fileFolder.exists() && fileFolder.isDirectory()) {

                            String fileClass;
                            if (classes.contains(fileFolder.getName())) {
                                fileClass = fileFolder.getName();

                            } else {
                                throw new IllegalFileNameException("Internal error.");
                            }
                            for (File file : fileFolder.listFiles()) {
                                docs.put(tokenizeFile(file), fileClass);
                            }
                        }
                    }
                    return docs;
                } else {
                    if(classes.size() > folder.listFiles().length) {
                        throw new CouldNotStartTokenizingException("Internal error.");
                    } else {
                        throw new CouldNotStartTokenizingException("Internal error.");
                    }
                }
            } else {
                throw new CouldNotStartTokenizingException("Internal error.");
            }
    }

    // Tokenize a file
    private Map<String, Integer> tokenizeFile(File file) {
        Map<String, Integer> fileMap = new HashMap<String, Integer>();

        try {
            Scanner in = new Scanner(file);
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
