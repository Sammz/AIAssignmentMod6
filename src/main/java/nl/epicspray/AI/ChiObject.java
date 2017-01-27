package nl.epicspray.AI;

/**
 * Created by Pelle de Greeuw on 27-Jan-17.
 */
public class ChiObject implements Comparable {

    private double val;
    private String word;

    public ChiObject(double val, String word) {
        this.val = val;
        this.word = word;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public double getVal() {
        return val;
    }

    public void setVal(double val) {
        this.val = val;
    }

    @Override
    public int compareTo(Object o) {
        if(o instanceof ChiObject){
            if(((ChiObject) o).val == this.val){
                return 0;
            } else if (val > ((ChiObject) o).val){
                return 1;
            } else {
                return -1;
            }
        }
        return 0;
    }
}
