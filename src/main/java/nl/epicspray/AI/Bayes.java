package nl.epicspray.AI;

import nl.epicspray.AI.util.SystemController;
import sun.plugin2.gluegen.runtime.CPU;

import java.util.*;

/**
 * Created by Gebruiker on 14-1-2017.
 */
public class Bayes {

    private Set<String> V = new HashSet<String>();
    private Map<String, Double> CPrior;
    private List<String> C;
    private Map<Map<String, Integer>, String> D;
    private Map<String, Map<String, Double>> chanceOnWordGivenClass;
    private int K = 1;

    public void train(List<String> classes, Map<Map<String, Integer>, String> docs){
        C = classes;
        D = docs;
        for(Map<String, Integer> map : D.keySet()){
            V.addAll(map.keySet());
        }
        Map<String, Integer> totalDocsPerClass = new HashMap<String, Integer>();
        Map<String, Map<String, Integer>> countWordsPerClass = new HashMap<String, Map<String, Integer>>();
        chanceOnWordGivenClass = new HashMap<String, Map<String, Double>>();
        Map<String, Integer> totalWordsPerClass = new HashMap<String, Integer>();
        //Total classes
        for(String c : C){
            totalDocsPerClass.put(c, 0);
            countWordsPerClass.put(c, new HashMap<String, Integer>());
            chanceOnWordGivenClass.put(c, new HashMap<String, Double>());
            totalWordsPerClass.put(c, 0);
            SystemController.getLogger().debug("Found class: " + c);
        }

        //Map every word to a class instead of only a document
        int totalDocs = 0;
        for(String s : C){
            for(Map<String, Integer> map : D.keySet()){
                if(s.equals(D.get(map))){
                    totalDocs++;
                    totalDocsPerClass.put(s, totalDocsPerClass.remove(s) + 1);
                    SystemController.getLogger().debug("Found Doc with class: " + s + ", total : " + totalDocsPerClass.get(s));
                    for(String c : map.keySet()){
                        if(countWordsPerClass.get(s).containsKey(c)){
                            countWordsPerClass.get(s).put(c, countWordsPerClass.get(s).remove(c) + map.get(c));
                        } else {
                            countWordsPerClass.get(s).put(c, map.get(c));
                        }
                        SystemController.getLogger().debug("Current words: " + c + " in class: " + s + " = " + countWordsPerClass.get(s).get(c));
                    }
                }
            }
        }
        CPrior = new HashMap<String, Double>();
        for(String c : C){
            CPrior.put(c, ((double) totalDocsPerClass.get(c) / totalDocs));
            SystemController.getLogger().debug("Calculated prior chance for class: " + c + " = " + CPrior.get(c));
        }

        //Calculate total amount of words
        for(String c : C){
            for(String s : countWordsPerClass.get(c).keySet()){
                SystemController.getLogger().debug("Final words: " + s + " in class: " + c + " = " + countWordsPerClass.get(c).get(s));
                totalWordsPerClass.put(c, totalWordsPerClass.get(c) + countWordsPerClass.get(c).get(s));
            }
        }
        for(String s : V){
            for(String c : C){
                if(!countWordsPerClass.get(c).containsKey(s)){
                    countWordsPerClass.get(c).put(s, 0);
                }
            }
        }

        //calculate actual prior chances
        for(String c : C){
            for(String s : countWordsPerClass.get(c).keySet()){
                double val = ((double)countWordsPerClass.get(c).get(s) + K) / (totalWordsPerClass.get(c) + (K * V.size()));
                SystemController.getLogger().debug("Chance on word: " + s + " for class: " + c + " = " + val +
                        " ( (" + countWordsPerClass.get(c).get(s) + " + " + K + ") / (" +  (totalWordsPerClass.get(c)) + " + " +(K * V.size()) + ")");
                chanceOnWordGivenClass.get(c).put(s, val);
            }
        }
    }

    public String classify(Map<String, Integer> doc){
        double max = 0;
        String bestClass = null;

        for(String c : C){
            SystemController.getLogger().debug("Class: " + c);
            double val = CPrior.get(c);
            for(String s : doc.keySet()){
                if(chanceOnWordGivenClass.get(c).containsKey(s)){
                    val = val * (Math.pow(chanceOnWordGivenClass.get(c).get(s), doc.get(s)));
                }
            }
            SystemController.getLogger().debug("Total chance for class: " + c + " = " + val);
            if(max < val){
                bestClass = c;
                max = val;
            }
        }

        return bestClass;
    }
}
