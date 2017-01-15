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
    private Map<String, Map<String, Map<Boolean, Integer>>> chiSquareMap;
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
                    //SystemController.getLogger().debug("Found Doc with class: " + s + ", total : " + totalDocsPerClass.get(s));
                    for(String c : map.keySet()){
                        if(countWordsPerClass.get(s).containsKey(c)){
                            countWordsPerClass.get(s).put(c, countWordsPerClass.get(s).remove(c) + map.get(c));
                        } else {
                            countWordsPerClass.get(s).put(c, map.get(c));
                        }
                        //SystemController.getLogger().debug("Current words: " + c + " in class: " + s + " = " + countWordsPerClass.get(s).get(c));
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
                //SystemController.getLogger().debug("Final words: " + s + " in class: " + c + " = " + countWordsPerClass.get(c).get(s));
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
        chiSquareMap = new HashMap<String, Map<String, Map<Boolean, Integer>>>();
        for(String c : C){
            chiSquareMap.put(c, new HashMap<String, Map<Boolean, Integer>>());
            for(Map<String, Integer> map : D.keySet()){
                if(c.equals(D.get(map))){
                    for(String v : V){
                        if (map.containsKey(v)) {
                            if(chiSquareMap.get(c).containsKey(v)){
                                chiSquareMap.get(c).get(v).put(true, chiSquareMap.get(c).get(v).get(true) + 1);
                            } else {
                                chiSquareMap.get(c).put(v, new HashMap<Boolean, Integer>());
                                chiSquareMap.get(c).get(v).put(true, 1);
                                chiSquareMap.get(c).get(v).put(false, 0);
                            }
                        } else {
                            if(chiSquareMap.get(c).containsKey(v)){
                                chiSquareMap.get(c).get(v).put(false, chiSquareMap.get(c).get(v).get(false) + 1);
                            } else {
                                chiSquareMap.get(c).put(v, new HashMap<Boolean, Integer>());
                                chiSquareMap.get(c).get(v).put(true, 0);
                                chiSquareMap.get(c).get(v).put(false, 1);
                            }
                        }
                    }
                }
            }
        }

        //calculate actual prior chances
        for(String c : C){
            for(String s : countWordsPerClass.get(c).keySet()){
                double val = ((double)countWordsPerClass.get(c).get(s) + K) / (totalWordsPerClass.get(c) + (K * V.size()));
                //SystemController.getLogger().debug("Chance on word: " + s + " for class: " + c + " = " + val +
                //        " ( (" + countWordsPerClass.get(c).get(s) + " + " + K + ") / (" +  (totalWordsPerClass.get(c)) + " + " +(K * V.size()) + ")");
                chanceOnWordGivenClass.get(c).put(s, val);
            }
        }
    }

    public String classify(Map<String, Integer> doc){
        double max = Double.NEGATIVE_INFINITY;
        String bestClass = null;

        for(String c : C) {
            //SystemController.getLogger().debug("Class: " + c);
            double val = Math.log(CPrior.get(c));
            for (String s : doc.keySet()) {
                if (chanceOnWordGivenClass.get(c).containsKey(s)) {
                    double temp = Math.log((Math.pow(chanceOnWordGivenClass.get(c).get(s), doc.get(s))));
                    if (temp == Double.NEGATIVE_INFINITY) {
                        temp = 0;
                    }
                    val = val + temp;
                }
            }
            //SystemController.getLogger().debug("Total chance for class: " + c + " = " + val);
            if (max < val) {
                bestClass = c;
                max = val;
            }
        }
        return bestClass;
    }

    public double computeChiSquare(String word){
        double val = 0;
        int totalMisses = 0;
        int totalContains = 0;
        for(String c : C){
            totalContains += chiSquareMap.get(c).get(word).get(true);
            totalMisses += chiSquareMap.get(c).get(word).get(false);
        }
        for(String c : C){

            int cc = chiSquareMap.get(c).get(word).get(true) + chiSquareMap.get(c).get(word).get(false);
            double Em = (totalMisses * cc) / (double) (totalContains + totalMisses);
            double Ec = (totalContains * cc) / (double) (totalContains + totalMisses);
            val += (Math.pow(chiSquareMap.get(c).get(word).get(true) - Ec, 2 ) / Ec);
            val += (Math.pow(chiSquareMap.get(c).get(word).get(false) - Em, 2 ) / Em);
            //SystemController.getLogger().debug("CC: " + cc + ", Em: " + Em + ", Ec: " + Ec + ", Valc: " +
            //        (Math.pow(chiSquareMap.get(c).get(word).get(true) - Ec, 2 ) / Ec) + ", Valm: " + (Math.pow(chiSquareMap.get(c).get(word).get(false) - Em, 2 ) / Em));
        }
        //SystemController.getLogger().debug("Val: " + val);
        return val;
    }

    public String getHighestChiSquare(){
        String max = null;
        double val = 0;
        for(String s : V){
            double t = computeChiSquare(s);
            //SystemController.getLogger().debug("Computed Chi for: " + s + ", " + t);
            if(t > val){
                val = t;
                max = s;
            }
        }
        return max;
    }


}
