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
    public static void main(String[] args) {
        //open a JFileChooser, load multiple json files
        File[] files = loadTheFiles();
        //eliminate unnecessary data and concatenate files
        String wholeFile = eliminateUnnecessaryData(files);
        //neat object
        JSONObject tidyJSON = constructJSON(wholeFile);
        //write to local
        writeLocalFile(tidyJSON);
    }

    /**
     * open a JFile chooser to load the files
     *
     * @return files
     */
    private static File[] loadTheFiles() {
        //add a frame contain the file chooser
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


    /**
     * this method take in a file list(JSON file we loaded before),then extract only necessary data from original
     * file, then concatenate them and parse it into a JSON Object
     * <p>
     * Data structure after the concatenation will be
     * {
     * "customers":[
     * {
     * "lastName":"xxx",
     * "firstName": "xxx",
     * "address": "xxx"
     * "transactions":[
     * "date": "xxx",
     * "amount": xxx,
     * "balance": "xxx",
     * "text": "xxx",
     * "type": "xxx",
     * "tags": [
     * {
     * "xxx": "xxx"
     * },
     * {
     * "xxx": "xxx"
     * }
     * ]
     * },
     * {...},
     * {...}
     * ]
     * }
     *
     * @return wholeFile
     */
    private static String eliminateUnnecessaryData(File[] files) {
        //for concatenated all customers
        String customers = "{ \"customers\": [";

        //for each file(customer)
        for (int i = 0; i < files.length; i++) {
            JSONParser jsonParser = new JSONParser();

            //read current file
            try (FileReader reader = new FileReader(files[i])) {

                //parse customers JSON file
                JSONObject file = (JSONObject) jsonParser.parse(reader);
                //access the nested json object to extract necessary data
                JSONObject statement = (JSONObject) file.get("statement");
                JSONObject bankData = (JSONObject) statement.get("bankData");
                // a customer have many accounts,each account have many transactions
                JSONArray bankAccount = (JSONArray) bankData.get("bankAccounts");

                String toObject = concatenateTransactions(bankAccount);

                JSONParser jp = new JSONParser();
                //make concatenated file to a json object
                JSONObject customer = (JSONObject) jp.parse(toObject);

                //add other customer data such as last name...
                String lastName = (String) file.get("lastName");
                String firstName = (String) file.get("firstName");
                //get user address, because address are nested object, access it require access the outer object
                JSONObject userAddress = (JSONObject) bankData.get("userAddress");
                String address = (String) userAddress.get("text");

                customer.put("lastName", lastName);
                customer.put("firstName", firstName);
                customer.put("address", address);

                customers += customer.toString();

                //add "," only if more than 1 customer files are selected, and don't add "," at the end of file
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
        //closure
        customers += "] }";

        return customers;
    }

    /**
     * this method is used to concatenate all transactions from different bank account of a person
     *
     * @return toObject
     */
    private static String concatenateTransactions(JSONArray bankAccount) {
        //for concatenated transactions
        String toObject = "{ \"transactions\" : [ ";
        // for each bank account
        for (int j = 0; j < bankAccount.size(); j++) {
            // was i now j
            JSONObject current = (JSONObject) bankAccount.get(j);
            String temp = current.get("transactions").toString();

            /**
             * because transactions are json object list, like "transactions":[...]
             * to concatenate the transactions without format error, we need to delete "[" and "]"
             * */
            temp = temp.substring(1, temp.length() - 1);

            // if customer have account number greater than 1, then add "," in between 2 transactions
            if (bankAccount.size() > 1) {
                temp = temp + ",";
            }

            toObject += temp;
        }
        //get rid of last "," , because now it is the end of the transaction, no more transactions to add
        toObject = toObject.substring(0, toObject.length() - 1);
        //add close symbol
        toObject += "] }";

        return toObject;
    }


    /**
     * this method take in a whole file,and then reconstruct transactions structure, the transactions after the
     * reconstruction only have transaction in the desired category list with {amount,category,thirdParty} as property
     *
     * @return wholeJSON
     */
    private static JSONObject constructJSON(String file) {
        JSONParser jp = new JSONParser();
        //for contain reconstructed transactions
        JSONArray transactionList = new JSONArray();
        String wholeFile = "{ \"customers\":[";
        try {
            JSONObject jsObject = (JSONObject) jp.parse(file);
            JSONArray customers = (JSONArray) jsObject.get("customers");
            JSONObject tidyObject = new JSONObject();

            for (int i = 0; i < customers.size(); i++) {
                JSONObject currentCustomer = (JSONObject) customers.get(i);
                JSONArray transactions = (JSONArray) currentCustomer.get("transactions");

                for (int j = 0; j < transactions.size(); j++) {
                    JSONObject currentTransaction = (JSONObject) transactions.get(j);
                    JSONArray tags = (JSONArray) currentTransaction.get("tags");

                    HashMap result = checkInCategory(tags);

                    if (result.get("category") != null && result.get("thirdParty") != null) {
                        JSONObject tidyTransaction = new JSONObject();
                        //get transaction amount
                        String amount = currentTransaction.get("amount").toString();
                        tidyTransaction.put("category", result.get("category"));
                        tidyTransaction.put("thirdParty", result.get("thirdParty"));
                        tidyTransaction.put("amount", amount);

                        //add to tidy transaction
                        transactionList.add(tidyTransaction);
                    }
                }
                tidyObject.put("firstName", currentCustomer.get("firstName"));
                tidyObject.put("lastName", currentCustomer.get("lastName"));
                tidyObject.put("district", findDistrict(currentCustomer.get("address").toString()));
                tidyObject.put("transactions", transactionList);
                wholeFile += tidyObject.toString();

                if (customers.size() > 1 && i != customers.size() - 1) {
                    wholeFile += ",";
                }

            }
        } catch (ParseException e) {
            System.out.println(e);
        }
        //closure
        wholeFile += "] }";

        JSONObject tidyJson = null;
        try {
            tidyJson = (JSONObject) jp.parse(wholeFile);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return tidyJson;
    }

    //find district from a customer address
    private static String findDistrict(String address) {
        String district = "";

        if (address.length() > 6) {
            for (int i = address.length() - 6; address.charAt(i) != ' '; i--) {
                district += address.charAt(i);
            }
        }

        return new StringBuffer(district).reverse().toString();
    }

    /**
     * check whether the transaction is what we wanted (in category list)
     *
     * @return tagList
     */
    private static HashMap<String, String> checkInCategory(JSONArray tags) {
        int result = 0;

        //desired categories
        List<String> category = Arrays.asList("Loans", "SACC Loans", "Non SACC Loans", "Insurance", "Groceries",
                "Gambling", "Subscription TV", "Telecommunications", "Utilities");

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

    /**
     * write the concatenated and neat
     */
    private static void writeLocalFile(JSONObject tidyJSON) {
        try (FileWriter fw = new FileWriter("TidyJSON.json")) {
            fw.write(tidyJSON.toJSONString());
            fw.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
