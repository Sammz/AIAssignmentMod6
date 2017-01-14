package nl.epicspray.AI.tokenizer;

import nl.epicspray.AI.Protocol;

import java.io.File;
import java.util.List;

/**
 * Created by Sam on 14-1-2017.
 */
public class TokenizerTest {

    public static void main(String[] args){
        File f = new File("C:\\Users\\Sam\\Downloads\\AI-blogs\\F");
        List<String> classes = Protocol.genderClass;

        Tokenizer t = new Tokenizer(f, classes);
        t.tokenizeFolder();


    }
}
