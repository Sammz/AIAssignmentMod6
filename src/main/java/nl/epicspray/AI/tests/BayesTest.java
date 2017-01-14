package nl.epicspray.AI.tests;


import nl.epicspray.AI.Bayes;
import nl.epicspray.AI.util.SystemController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Sam on 14-1-2017.
 */
public class BayesTest {
    public static void main(String[] args) {
        SystemController.getLogger().debug("Test");
        Map<Map<String,Integer>, String> training = new HashMap<Map<String, Integer>, String>();
        Map<String, Integer> file1 = new HashMap<String, Integer>();
        file1.put("Uganda", 2);
        file1.put("Bank", 1);
        Map<String, Integer> file2 = new HashMap<String, Integer>();
        file2.put("Uganda", 2);
        file2.put("Solar", 1);
        Map<String, Integer> file3 = new HashMap<String, Integer>();
        file3.put("Uganda", 1);
        file3.put("Mexico", 1);
        Map<String, Integer> file4 = new HashMap<String, Integer>();
        file4.put("Uganda", 1);
        file4.put("Japan", 1);
        file4.put("Tokyo", 1);
        Map<String, Integer> file5 = new HashMap<String, Integer>();
        file5.put("Uganda", 3);
        file5.put("Tokyo", 1);
        file5.put("Japan", 1);
        training.put(file1, "Spam");
        training.put(file2, "Spam");
        training.put(file3, "Spam");
        training.put(file4, "Ham");
        List<String> classes = new ArrayList<String>();
        classes.add("Spam");
        classes.add("Ham");
        Bayes bayes = new Bayes();
        bayes.train(classes, training);
        SystemController.getLogger().debug(bayes.classify(file5));
    }
}