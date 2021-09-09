import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.swing.*;
import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {

        //open a JFileChooser, load multiple json files
        File[] files = loadTheFiles();
        /**
         * this method take in a file list(JSON file we loaded before),then extract only necessary data from original
         * file, then concatenate them and parse it into a JSON Object
         *
         * Data structure after the concatenation will be
         * {
         *      "customers":[
         *           {
         *                "lastName":"xxx",
         *                "firstName": "xxx",
         *                "address": "xxx"
         *                "transactions":[
         *                     "date": "xxx",
         *                     "amount": xxx,
         *                     "balance": "xxx",
         *                     "text": "xxx",
         *                     "type": "xxx",
         *                     "tags": [
         *                          {
         *                               "xxx": "xxx"
         *                          },
         *                          {
         *                               "xxx": "xxx"
         *                          }
         *                          ]
         *                 ]
         *           },
         *           {...},
         *           {...}
         * }
         * */
        String wholeFile = eliminateUnnecessaryData(files);
        //String wholeFile = concatenateFiles(files);
        //PrintWriter pw = new PrintWriter("output.json");
        //System.out.println(wholeFile);

        // jason parser
//        JSONParser jsonParser = new JSONParser();
//        JSONObject tidyJSON = new JSONObject();
//
//        try (FileReader reader = new FileReader("tidyCustomer1.json")) {
//
//            //Read customers JSON file
//            JSONObject customers = (JSONObject) jsonParser.parse(reader);
//
//            String lastName = (String) customers.get("lastName");
//            String firstName = (String) customers.get("firstName");
//
//            JSONObject userAddress = (JSONObject) customers.get("userAddress");
//            String address = (String) userAddress.get("text");
//
//            JSONArray transactions = (JSONArray) customers.get("transactions");
//
//            tidyJSON = constructJSON(transactions, lastName, firstName, findDistrict(address));
//
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }
//
//        try (FileWriter fw = new FileWriter("TidyJSON.json")) {
//            fw.write(tidyJSON.toJSONString());
//            fw.flush();
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

    }

    /**
     * eliminate unnecessary data and concatenate files
     * @return String
     * */
    private static String eliminateUnnecessaryData(File[] files) {
        //for concatenated all customers
        String customers = "{ \"customers\": [";

        for (int i = 0; i < files.length; i++) {
            JSONParser jsonParser = new JSONParser();
            try (FileReader reader = new FileReader(files[i])) {

                //Read customers JSON file
                JSONObject file = (JSONObject) jsonParser.parse(reader);

                JSONObject statement = (JSONObject) file.get("statement");
                JSONObject bankData = (JSONObject) statement.get("bankData");


                JSONArray bankAccount = (JSONArray) bankData.get("bankAccounts");
                String toObject = "{ \"transactions\" : [ ";
                for (int j = 0; j < bankAccount.size(); j++) {
                    JSONObject current = (JSONObject) bankAccount.get(i);
                    String temp = current.get("transactions").toString();
                    temp = temp.substring(1, temp.length() - 1);
                    if (bankAccount.size() > 1) {
                        temp = temp + ",";
                    }
                    toObject += temp;
                }
                toObject = toObject.substring(0, toObject.length() - 1);
                toObject += "] }";
                JSONParser jp = new JSONParser();
                JSONObject customer = (JSONObject) jp.parse(toObject);
                String lastName = (String) file.get("lastName");
                String firstName = (String) file.get("firstName");
                JSONObject userAddress = (JSONObject) bankData.get("userAddress");
                String address = (String) userAddress.get("text");
                customer.put("lastName", lastName);
                customer.put("firstName", firstName);
                customer.put("address", address);

                customers += customer.toString();

                if (files.length > 1 && i != files.length - 1) {
                    customers += ",";
                }

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        customers += "] }";

        return customers;
    }

    private static String concatenateFiles(File[] files) {
        String wholeFile = "{\"customers\":[";
        for (int i = 0; i < files.length; i++) {
            try {
                BufferedReader br = new BufferedReader(new FileReader(files[i]));
                String line = br.readLine();
                while (line != null) {
                    wholeFile += line;
                    line = br.readLine();
                }

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            // if only one file are selected, then it won't add comma
            if (files.length > 1) {
                wholeFile += ",";
            }
        }
        wholeFile = wholeFile + "]}";

        return wholeFile;
    }

    private static File[] loadTheFiles() {
        JFrame frame = new JFrame();

        JFileChooser fileChooser = new JFileChooser();

        fileChooser.setMultiSelectionEnabled(true);
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));

        int result = fileChooser.showOpenDialog(frame);

        File[] files = fileChooser.getSelectedFiles();
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.dispose();

        return files;
    }

    private static JSONObject constructJSON(JSONArray transactions, String lastName, String firstName, String address) {
        JSONArray transactionList = new JSONArray();

        for (int i = 0; i < transactions.size(); i++) {
            //get current transaction
            JSONObject currentTrans = (JSONObject) transactions.get(i);
            //get current transaction tags
            JSONArray tags = (JSONArray) currentTrans.get("tags");
            //check if the category of this transaction is in the desired category list
            HashMap result = checkInCategory(tags);

            if (result.get("category") != null && result.get("thirdParty") != null) {
                //get transaction amount
                String amount = currentTrans.get("amount").toString();

                JSONObject transaction = new JSONObject();
                transaction.put("category", result.get("category"));
                transaction.put("thirdParty", result.get("thirdParty"));
                transaction.put("amount", amount);

                transactionList.add(transaction);
            }

        }
        JSONObject tidyObject = new JSONObject();
        tidyObject.put("transactions", transactionList);
        tidyObject.put("firstName", firstName);
        tidyObject.put("lastName", lastName);
        tidyObject.put("district", address);


        return tidyObject;
    }

    private static String findDistrict(String address) {
        String district = "";

        for (int i = address.length() - 6; address.charAt(i) != ' '; i--) {
            district += address.charAt(i);
        }

        return new StringBuffer(district).reverse().toString();
    }

    private static HashMap<String, String> checkInCategory(JSONArray tags) {
        int result = 0;

        //desired categories
        List<String> category = Arrays.asList("Loans", "SACC Loans", "Non SACC Loans", "Insurance", "Groceries",
                "Gambling", "Subscription TV", "Telecommunications", "Utilities");

        //List<String> tagList = new ArrayList<String>();
        HashMap<String, String> tagList = new HashMap<String, String>();

        for (int i = 0; i < tags.size(); i++) {
            JSONObject tag = (JSONObject) tags.get(i);

            if (tag.get("category") != null && category.contains(tag.get("category"))) {
                tagList.put("category", (String) tag.get("category"));
            }
            if (tag.get("thirdParty") != null) {
                tagList.put("thirdParty", (String) tag.get("thirdParty"));
            }
        }

        return tagList;
    }

}
