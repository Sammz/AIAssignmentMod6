package nl.epicspray.AI.tests;


import nl.epicspray.AI.Bayes;
import nl.epicspray.AI.Protocol;
import nl.epicspray.AI.Tokenizer;
import nl.epicspray.AI.exceptions.CouldNotStartTokenizingException;
import nl.epicspray.AI.exceptions.IllegalFileNameException;
import nl.epicspray.AI.util.SystemController;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Sam on 14-1-2017.
 */
public class BayesTest {
    public static void main(String[] args) {
        SystemController.getLogger().debug("Log: " + Math.log(2.600009360033696E-6));
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
        SystemController.getLogger().warning("-----------starting test------------");
        String word = "hello";
        Map<String, Map<String, Map<Boolean, Integer>>> chiSquareMap = new HashMap<String, Map<String, Map<Boolean, Integer>>>();
        List<String> C  = new ArrayList<String>();
        C.add("A");
        C.add("B");
        chiSquareMap.put("A", new HashMap<String, Map<Boolean, Integer>>());
        chiSquareMap.put("B", new HashMap<String, Map<Boolean, Integer>>());
        chiSquareMap.get("A").put("hello", new HashMap<Boolean, Integer>());
        chiSquareMap.get("B").put("hello", new HashMap<Boolean, Integer>());
        chiSquareMap.get("A").get("hello").put(true, 10);
        chiSquareMap.get("A").get("hello").put(false, 50);
        chiSquareMap.get("B").get("hello").put(false, 59);
        chiSquareMap.get("B").get("hello").put(true, 1);
//        double val = 0;
//        int totalMisses = 0;
//        int totalContains = 0;
//        for(String c : C){
//            totalContains += chiSquareMap.get(c).get(word).get(true);
//            totalMisses += chiSquareMap.get(c).get(word).get(false);
//        }
//        for(String c : C){
//
//            int cc = chiSquareMap.get(c).get(word).get(true) + chiSquareMap.get(c).get(word).get(false);
//            double Em = (totalMisses * cc) / (double) (totalContains + totalMisses);
//            double Ec = (totalContains * cc) / (double) (totalContains + totalMisses);
//            val += (Math.pow(chiSquareMap.get(c).get(word).get(true) - Ec, 2 ) / Ec);
//            val += (Math.pow(chiSquareMap.get(c).get(word).get(false) - Em, 2 ) / Em);
//            SystemController.getLogger().debug("CC: " + cc + ", Em: " + Em + ", Ec: " + Ec + ", Valc: " +
//                    (Math.pow(chiSquareMap.get(c).get(word).get(true) - Ec, 2 ) / Ec) + ", Valm: " + (Math.pow(chiSquareMap.get(c).get(word).get(false) - Em, 2 ) / Em));
//        }
//        SystemController.getLogger().debug("Val: " + val);


        new BayesTest().testWithData();
    }

    public void testWithData(){
        File f = new File("C:\\Users\\Gebruiker\\Documents\\Corpus\\blogs\\F"); // The path to the folder
        File m = new File("C:\\Users\\Gebruiker\\Documents\\Corpus\\blogs\\M"); // The path to the folder
        File train = new File("C:\\Users\\Gebruiker\\Documents\\Corpus\\blogs\\Test"); // The path to the folder
        List<String> classes = Protocol.genderClass;
        Map<Map<String, Integer>, String> tokenizedF = null;
        Map<Map<String, Integer>, String> tokenizedM = null;
        Map<Map<String, Integer>, String> tokenizedT = new HashMap<Map<String, Integer>, String>();
        Map<Map<String, Integer>, String> tokenizedTrain = null;
        Tokenizer t= new Tokenizer();
        try {
            tokenizedF = t.tokenizeFolder(f, classes);
            tokenizedM = t.tokenizeFolder(m, classes);
            tokenizedTrain = t.tokenizeFolder(train, classes);
            tokenizedT.putAll(tokenizedF);
            tokenizedT.putAll(tokenizedM);
        } catch (CouldNotStartTokenizingException e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        } catch (IllegalFileNameException e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }

        Bayes bayes = new Bayes();
        bayes.train(classes, tokenizedT);
        for(Map<String, Integer> doc : tokenizedTrain.keySet()){
            SystemController.getLogger().debug("Classified as: " + bayes.classify(doc) + " correct: " + tokenizedTrain.get(doc));
        }

        String best = bayes.getHighestChiSquare();
        SystemController.getLogger().debug("Best ChiSquare: " + best + ", " + bayes.computeChiSquare(best));
    }
}