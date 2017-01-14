package nl.epicspray.AI.tests;

import nl.epicspray.AI.Protocol;
import nl.epicspray.AI.exceptions.CouldNotStartTokenizingException;
import nl.epicspray.AI.exceptions.IllegalFileNameException;
import nl.epicspray.AI.Tokenizer;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Created by Sam on 14-1-2017.
 */
public class TokenizerTest {

    public static void main(String[] args) {
        File f = new File("C:\\Users\\Sam\\Downloads\\AI-blogs\\F"); // The path to the folder
        List<String> classes = Protocol.genderClass;
        Map<Map<String, Integer>, String> tokenized = null;
        Tokenizer t= new Tokenizer();;
        try {
            tokenized = t.tokenizeFolder(f, classes);
            for (Map<String, Integer> map : tokenized.keySet()) {
                System.out.println("--------------------------" + tokenized.get(map) + "---------------------------");
                for (String name : map.keySet()) {

                    int value = map.get(name);
                    System.out.println(name + ": " + value);
                }
            }
        } catch (CouldNotStartTokenizingException e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        } catch (IllegalFileNameException e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }


    }
}
