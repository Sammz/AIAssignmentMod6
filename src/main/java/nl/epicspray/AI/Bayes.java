package nl.epicspray.AI;

import nl.epicspray.AI.util.SystemController;
import sun.plugin2.gluegen.runtime.CPU;

import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by Gebruiker on 14-1-2017.
 */
public class Bayes {

    private Set<String> V = new HashSet<String>();
    private Map<String, Double> CPrior;
    private List<String> C = new CopyOnWriteArrayList<String>();
    private Map<Map<String, Integer>, String> D = new HashMap<Map<String, Integer>, String>();
    private Map<String, Map<String, Double>> chanceOnWordGivenClass;
    private Map<String, Map<String, Map<Boolean, Integer>>> chiSquareMap;
    private List<ChiObject> chiSquares;
    private Map<String, Integer> totalDocsPerClass = new HashMap<String, Integer>();
    private Map<String, Map<String, Integer>> countWordsPerClass = new HashMap<String, Map<String, Integer>>();
    private Map<String, Integer> totalWordsPerClass = new HashMap<String, Integer>();
    private int K = 3;
    private int totalDocs;

    public void train(List<String> classes, Map<Map<String, Integer>, String> docs){
        C = classes;
        D.putAll(docs);
        for(Map<String, Integer> map : D.keySet()){
            V.addAll(map.keySet());
        }
        totalDocsPerClass = new HashMap<String, Integer>();
        countWordsPerClass = new HashMap<String, Map<String, Integer>>();
        chanceOnWordGivenClass = new HashMap<String, Map<String, Double>>();
        totalWordsPerClass = new HashMap<String, Integer>();
        //Total classes
        for(String c : C){
            totalDocsPerClass.put(c, 0);
            countWordsPerClass.put(c, new HashMap<String, Integer>());
            chanceOnWordGivenClass.put(c, new HashMap<String, Double>());
            totalWordsPerClass.put(c, 0);
            SystemController.getLogger().debug("Found class: " + c);
        }

        //Map every word to a class instead of only a document
        SystemController.getLogger().debug("Counting documents per class and words per class");
        totalDocs = 0;
        for(String s : C){
            totalDocsPerClass.put(s, (int) D.keySet().parallelStream()
                    .filter(t -> D.get(t).equals(s))
                    .count());

            for(String v : V){
                countWordsPerClass.get(s).put(v, 0);
            }
            D.keySet().parallelStream()
                    .filter(map -> s.equals(D.get(map)))
                    .forEach(map -> map.keySet().forEach(c -> countWordsPerClass.get(s).put(c, countWordsPerClass.get(s).get(c) + map.get(c))));
        }

        for (String s : totalDocsPerClass.keySet()){
            totalDocs += totalDocsPerClass.get(s);
        }

        calculatePriorChances();
        calculateTotalWords();
        calculateChiSquare();
        calculateChancesPerWordPerClass();

        SystemController.getLogger().debug("Finished training");
    }

    public void addToTrainingData(Map<String, Integer> doc, String className){
        for(String c : C){
            if(c.equals(className)){
                totalDocs++;
                totalDocsPerClass.put(c, totalDocsPerClass.remove(c) + 1);
                for (String s : doc.keySet()) {
                    if (countWordsPerClass.get(s).containsKey(c)) {
                        countWordsPerClass.get(s).put(c, countWordsPerClass.get(s).remove(c) + doc.get(c));
                    } else {
                        countWordsPerClass.get(s).put(c, doc.get(c));
                    }
                }
            }
        }

        calculatePriorChances();
        calculateTotalWords();
        calculateChiSquare();
        calculateChancesPerWordPerClass();
    }

    public void calculatePriorChances(){
        SystemController.getLogger().debug("Calculating Prior chances");
        CPrior = new HashMap<String, Double>();
        for(String c : C){
            CPrior.put(c, ((double) totalDocsPerClass.get(c) / totalDocs));
            //SystemController.getLogger().debug("Calculated prior chance for class: " + c + " = " + CPrior.get(c));
        }
    }

    public void calculateChancesPerWordPerClass(){
        SystemController.getLogger().debug("Calculating changes per word per class");
        for(String c : C){
            for(String s : countWordsPerClass.get(c).keySet()){
                double val = ((double)countWordsPerClass.get(c).get(s) + K) / (totalWordsPerClass.get(c) + (K * V.size()));
                //SystemController.getLogger().debug("Chance on word: " + s + " for class: " + c + " = " + val +
                //        " ( (" + countWordsPerClass.get(c).get(s) + " + " + K + ") / (" +  (totalWordsPerClass.get(c)) + " + " +(K * V.size()) + ")");
                chanceOnWordGivenClass.get(c).put(s, val);
            }
        }
    }

    public void calculateTotalWords(){
        SystemController.getLogger().debug("Calculating total words per class");
        for(String c : C){
            countWordsPerClass.get(c).keySet().parallelStream().forEach(s -> totalWordsPerClass.put(c, totalWordsPerClass.get(c) + countWordsPerClass.get(c).get(s)));
        }
        for(String s : V){
            for(String c : C){
                if(!countWordsPerClass.get(c).containsKey(s)){
                    countWordsPerClass.get(c).put(s, 0);
                }
            }
        }
    }

    public void calculateChiSquare(){
        SystemController.getLogger().debug("Calculating Chi Square for every word in the vocabulary");
        chiSquareMap = new ConcurrentHashMap<>();
        for(String c : C){
            chiSquareMap.put(c, new ConcurrentHashMap<>());
            V.stream().forEach(v -> {
                        Map<Boolean, Integer> h = new ConcurrentHashMap<>();
                        h.put(true, 0);
                        chiSquareMap.get(c).put(v, h);
                    }
                );

//            for(Map<String, Integer> map : D.keySet()){
//                if(c.equals(D.get(map))){
//                    for(String v : map.keySet()){
//                        chiSquareMap.get(c).get(v).put(true, chiSquareMap.get(c).get(v).get(true) + 1);
//                    }
//                }
//            }

            D.keySet().parallelStream()
                    .filter(map -> c.equals(D.get(map)))
                    .forEach(map -> map.keySet().forEach(v -> chiSquareMap.get(c).get(v).put(true, chiSquareMap.get(c).get(v).get(true) + 1)));

            V.parallelStream().forEach(v -> chiSquareMap.get(c).get(v).put(false, totalDocs - chiSquareMap.get(c).get(v).get(true)));
        }

        SystemController.getLogger().debug("Composing best chisquare list");
        chiSquares = new ArrayList<>();
        for(String v : V){
            chiSquares.add(new ChiObject(computeChiSquare(v), v));
        }
        Collections.sort(chiSquares);
        Collections.reverse(chiSquares);
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

    public String classifyWithLearning(Map<Map<String, Integer>, String> docs){
        for(Map<String, Integer> doc : docs.keySet()){

        }
        return null;
    }

    public Double getAccuracy (Map<Map<String, Integer>, String> docs){
        int correct = 0;
        int incorrect = 0;
        Map<String, Map<String, Integer>> matrix = getConfusionMatrix(docs);
        for(String c : matrix.keySet()){
            for(String c1 : matrix.get(c).keySet()){
                if(c1.equals(c)){
                    correct += matrix.get(c).get(c1);
                } else {
                    incorrect += matrix.get(c).get(c1);
                }
            }
        }
        return (double) correct / (double) (incorrect+correct);
    }

    public List<String> getBestChiSquare(int top){
        List<String> res = new ArrayList<String>();

        if(top > V.size() -1){
            top = V.size() -1;
        }
        //SystemController.getLogger().warning("Best chi Squares: ");
        for(int i = 0; i < top; i ++){
            res.add(chiSquares.get(i).getWord());
            //SystemController.getLogger().debug(i +" : [" + chis.get(i).getWord() + ", " + chis.get(i).getVal() + "]");
        }

        return res;
    }

    public String classifyWithBestChiSquare(Map<String, Integer> doc, int topChi){
        List<String> best = getBestChiSquare(topChi);
        doc = new ConcurrentHashMap<String, Integer>(doc);
        for(String s : doc.keySet()){
            if(!best.contains(s)){
                doc.remove(s);
            }
        }
        return  classify(doc);
    }

    public Map<String, Double> getPrecision(Map<Map<String, Integer>, String> docs){
        Map<String, Map<String, Integer>> matrix = getConfusionMatrix(docs);
        Map<String, Double> res = new HashMap<String, Double>();
        for(String c : C){
            int correct = 0;
            int yal = 0;
            int incorrect = 0;
            for(String c1 : C){
                if(c.equals(c1)){
                    correct += matrix.get(c1).get(c);
                } else {
                    incorrect += matrix.get(c1).get(c);
                }
            }
            res.put(c, (double) correct / (correct + incorrect));
            SystemController.getLogger().debug("Precision for class: " + c + " : " + (double) correct / (correct + incorrect));
        }
        return res;
    }

    public Map<String, Double> getRecall(Map<Map<String, Integer>, String> docs){
        Map<String, Map<String, Integer>> matrix = getConfusionMatrix(docs);
        Map<String, Double> res = new HashMap<String, Double>();
        for(String c : C){
            int correct = 0;
            int incorrect = 0;
            for(String c1 : C){
                if(c.equals(c1)){
                    correct += matrix.get(c).get(c1);
                } else {
                    incorrect += matrix.get(c).get(c1);
                }
            }
            res.put(c, (double) correct / (correct + incorrect));
            SystemController.getLogger().debug("Recall for class: " + c + " : " + (double) correct / (correct + incorrect));
        }
        return res;
    }

    public Map<String, Map<String, Integer>> getConfusionMatrix(Map<Map<String, Integer>, String> docs){
        Map<String, Map<String, Integer>> matrix = new HashMap<String, Map<String, Integer>>();
        for(String c : C){
            Map<String, Integer> inner = new HashMap<String, Integer>();
            for(String c1 : C){
                inner.put(c1, 0);
            }
            matrix.put(c, inner);
        }

        for(Map<String, Integer> doc : docs.keySet()){
            String actual = docs.get(doc);
            String pred = classify(doc);
            Map<String, Integer> inner = matrix.remove(actual);
            inner.put(pred, inner.remove(pred) + 1);
            matrix.put(actual, inner);
        }
        System.out.print("  | ");
        for(String c : C){
            System.out.print(c + " | ");
        }
        System.out.println(" ");
        for(String c : C){
            System.out.print(c + " | ");
            for(String c1 : C){
                System.out.print(matrix.get(c).get(c1) + " | ");
            }
            System.out.println(" ");
        }
        return matrix;
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
