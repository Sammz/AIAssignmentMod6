package nl.epicspray.AI.tokenizer;

import nl.epicspray.AI.Protocol;

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

    public File folder;
    public List<String> classes;

    // Initialize tokenizer
    public Tokenizer(File folder, List<String> classes) {
        this.folder = folder;
        this.classes = classes;
    }

    // Tokenize the folder
    public Map<Map<String, Integer>, String> tokenizeFolder() {
        Map<Map<String, Integer>, String> docs = new HashMap<Map<String, Integer>, String>();
        String fileClass;

        for (File file : folder.listFiles()) {
            String fileName = file.getName();
            if (classes.equals(Protocol.genderClass)) {
                if (fileName.contains("M")) {
                    fileClass = classes.get(1);
                } else fileClass = classes.get(2);

            } else {
                if (fileName.contains("p")) {
                    fileClass = classes.get(1);
                } else
                    fileClass = classes.get(2);
            }
            
            try {
                docs.put(tokenizeFile(file), fileClass);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        return docs;
    }

    // Tokenize a file
    private Map<String, Integer> tokenizeFile(File file) throws FileNotFoundException {
        Map<String, Integer> fileMap = new HashMap<String, Integer>();

        Scanner in = new Scanner(file);
        while (in.hasNext()) {
            String s = in.next();

        }

        return fileMap;
    }
}
