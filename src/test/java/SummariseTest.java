import junit.framework.TestCase;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.File;

public class SummariseTest extends TestCase {

    public void testSummariseTransactions() {
        File[] files = new File[3];
        files[0] = new File("C:\\Users\\GGPC\\Desktop\\data\\customer7.json");
        files[1] = new File("C:\\Users\\GGPC\\Desktop\\data\\customer8.json");
        files[2] = new File("C:\\Users\\GGPC\\Desktop\\data\\customer9.json");

        Model model = new Model();

        Controller.model = model;

        String customers = Controller.eliminateUnnecessaryData(files);
        JSONObject tidyJson = Controller.constructJSON(customers);
        JSONObject summarised = Controller.summariseTransactions(tidyJson);

        JSONArray afterCustomers = (JSONArray) summarised.get("customers");

        for (int i= 0;i<afterCustomers.size();i++)
        {
            JSONObject currentCustomer = (JSONObject) afterCustomers.get(i);

            assertNotNull(currentCustomer.get("totalSpent"));
            assertNotNull(currentCustomer.get("averageMonthlySpent"));

            Float total = Float.parseFloat(currentCustomer.get("totalSpent").toString());
            Float average = Float.parseFloat(currentCustomer.get("averageMonthlySpent").toString());

            assert(total!=average);
        }
    }
}