import junit.framework.TestCase;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.File;

public class ConstructJSONTest extends TestCase {

    public void testConstructJSON() {
        File[] files = new File[3];
        files[0] = new File("C:\\Users\\GGPC\\Desktop\\data\\customer3.json");
        files[1] = new File("C:\\Users\\GGPC\\Desktop\\data\\customer4.json");
        files[2] = new File("C:\\Users\\GGPC\\Desktop\\data\\customer5.json");

        Model model = new Model();

        Controller.model = model;

        String customers = Controller.eliminateUnnecessaryData(files);

        JSONObject afterConstruct = Controller.constructJSON(customers);

        JSONArray afterCustomer = (JSONArray) afterConstruct.get("customers");

        assertEquals(afterCustomer.size(),3);

        JSONObject singleCustomer = (JSONObject) afterCustomer.get(0);
        JSONArray transactionList = (JSONArray) singleCustomer.get("transactions");

        for (int i =0;i<transactionList.size();i++)
        {
            JSONObject currentTransaction = (JSONObject) transactionList.get(i);

            if (!Controller.model.category.contains(currentTransaction.get("category")))
            {
                assertEquals(1,2);
            }
        }
    }
}