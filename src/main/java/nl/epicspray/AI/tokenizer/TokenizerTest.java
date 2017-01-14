package nl.epicspray.AI.tokenizer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sam on 14-1-2017.
 */
public class TokenizerTest {

    public static void main(String[] args){
        File f = new File("C:\\Users\\Sam\\Downloads\\AI-blogs\\F");
        List<String> l = new ArrayList<String>();
        l.add("F");
        l.add("M");
        System.out.println(l);
        Tokenizer t = new Tokenizer(f, l);
        t.tokenizeFolder();


    }
}
