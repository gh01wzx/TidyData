import junit.framework.TestCase;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Test;

import java.io.File;

public class EliminationTest extends TestCase {
    @Test
    public void testEliminateUnnecessaryData() throws ParseException {
        // load the customer data
        File[] files = new File[2];
        files[0] = new File("C:\\Users\\GGPC\\Desktop\\data\\customer1.json");
        files[1] = new File("C:\\Users\\GGPC\\Desktop\\data\\customer2.json");
        String customers = Controller.eliminateUnnecessaryData(files);

        //for parser the string
        JSONParser jp = new JSONParser();

        JSONObject test = (JSONObject) jp.parse(customers);

        JSONArray customersTest = (JSONArray) test.get("customers");

        assertEquals(customersTest.size(),2);

        JSONObject accountTest = (JSONObject) customersTest.get(0);

        assertEquals(accountTest.get("bankAccounts"),null);

    }
}