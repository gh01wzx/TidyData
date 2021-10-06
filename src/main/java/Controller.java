import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.text.DecimalFormat;
import java.util.*;

public class Controller {
    public static Model model;
    FileTypeSelection fileType;

    public Controller(FileTypeSelection file_Type, Model model1) {
        this.fileType = file_Type;
        model = model1;

        addActionListener();
        fileType.frame.setVisible(true);
    }

    /**
     * this method is used to summarise the transactions
     */
    private static JSONObject summariseTransactions(JSONObject tidyJSON) {
        // get a list of customers data
        JSONArray customers = (JSONArray) tidyJSON.get("customers");
        System.out.println(customers);

        JSONObject summarisedCustomers = new JSONObject();
        JSONArray customersArray = new JSONArray();

        for (int i = 0; i < customers.size(); i++) {
            //get current current customer
            JSONObject currentCustomer = (JSONObject) customers.get(i);
            // get current customer array
            JSONArray transactions = (JSONArray) currentCustomer.get("transactions");
            // cluster customer transactions into different category
            JSONObject clustered = clusterTransactions(transactions);
            // summarise each cluster
            JSONObject customer = summariseCluster(clustered);
            customer.put("lastName", currentCustomer.get("lastName"));
            customer.put("firstName", currentCustomer.get("firstName"));
            customer.put("district", currentCustomer.get("district"));

            customersArray.add(customer);
        }

        summarisedCustomers.put("customers", customersArray);

        writeLocalFile(summarisedCustomers);

        return tidyJSON;
    }

    private static JSONObject summariseCluster(JSONObject clustered) {
        //to remove empty object
        clustered = removeEmpty(clustered);
        //calculate total spending
        JSONObject customerTemp = totalSpending(clustered);
        JSONObject transactions = (JSONObject) customerTemp.get("transactions");
        Set<String> categories = transactions.keySet();
        JSONObject customer = new JSONObject();

        for (String category : categories) {
            JSONObject currentCate = summariseCategory((JSONArray) transactions.get(category));

            customer.put(category, currentCate);
        }

        customer.put("averageMonthlySpent", customerTemp.get("averageMonthlySpent").toString());
        customer.put("totalSpent", customerTemp.get("totalSpent").toString());

        return customer;
    }

    private static JSONObject summariseCategory(JSONArray currentCategoryTransactions) {
        float totalSpending = 0;
        float monthlySpending = 0;
        int count = currentCategoryTransactions.size();
        //get unique third party
        HashSet<String> thirdParties = new HashSet<>();
        DecimalFormat df = new DecimalFormat("#.##");

        for (int i = 0; i < currentCategoryTransactions.size(); i++) {
            JSONObject transaction = (JSONObject) currentCategoryTransactions.get(i);

            totalSpending += Float.parseFloat(transaction.get("amount").toString());
            thirdParties.add(transaction.get("thirdParty").toString());
        }

        totalSpending = Math.abs(totalSpending);
        monthlySpending = totalSpending / 12;

        List<String> thirdPty = new ArrayList<>(thirdParties);
        JSONObject summarised = new JSONObject();

        // if current category transaction have more than 1 third-parties
        if (thirdParties.size() > 1) {
            JSONObject clusteredThirdPTY = new JSONObject();

            for (int i = 0; i < thirdPty.size(); i++) {
                JSONArray temp = new JSONArray();

                for (int j = 0; j < currentCategoryTransactions.size(); j++) {
                    JSONObject currentTransaction = (JSONObject) currentCategoryTransactions.get(j);

                    String currentThirdPty = currentTransaction.get("thirdParty").toString();

                    if (currentThirdPty.compareTo(thirdPty.get(i)) == 0) {
                        temp.add(currentTransaction);
                    }
                }
                clusteredThirdPTY.put(thirdPty.get(i), temp);
            }
            JSONArray thirdPartyArray = new JSONArray();

            //summarise different third party
            for (int i = 0; i < thirdPty.size(); i++) {
                JSONArray currentTPTY = (JSONArray) clusteredThirdPTY.get(thirdPty.get(i));

                float totalCurrent = 0;
                float monthlyCurrent = 0;
                int currentCount = currentTPTY.size();


                for (int j = 0; j < currentTPTY.size(); j++) {
                    JSONObject currentTrans = (JSONObject) currentTPTY.get(j);

                    totalCurrent += Float.parseFloat(currentTrans.get("amount").toString());
                }

                totalCurrent = Math.abs(totalCurrent);
                monthlyCurrent = totalCurrent / 12;

                float spendPercentCurr = totalCurrent / totalSpending;
                float frequencyPercentageCurr = (float) currentCount / (float) count;

                JSONObject summarisedTPTY = new JSONObject();


                summarisedTPTY.put("thirdParty", thirdPty.get(i));
                summarisedTPTY.put("monthlySpending", df.format(monthlyCurrent));
                summarisedTPTY.put("totalSpending", df.format(totalCurrent));
                summarisedTPTY.put("count", currentCount);
                summarisedTPTY.put("spendingPercentage", df.format(spendPercentCurr));
                summarisedTPTY.put("frequencyPercentage", df.format(frequencyPercentageCurr));


                thirdPartyArray.add(summarisedTPTY);

                //summarised.put(thirdPty.get(i),summarisedTPTY);
            }
            String mostFreq = findTheMost(thirdPartyArray, "frequencyPercentage");
            String mostSpend = findTheMost(thirdPartyArray, "spendingPercentage");

            summarised.put("total", df.format(totalSpending));
            summarised.put("average", df.format(monthlySpending));
            summarised.put("count", count);
            summarised.put("primary", mostSpend);
            summarised.put("mostFrenquentlyVisit", mostFreq);
            summarised.put("mostSpending", mostSpend);
            summarised.put("thirdParty", thirdPartyArray);
        } else {
            summarised.put("total", df.format(totalSpending));
            summarised.put("average", df.format(monthlySpending));
            summarised.put("count", count);
            summarised.put("primary", thirdPty.get(0));
            summarised.put("mostFrenquentlyVisit", thirdPty.get(0));
            summarised.put("mostSpending", thirdPty.get(0));

            JSONObject currentTPTY = new JSONObject();

            currentTPTY.put("thirdParty", thirdPty.get(0));
            currentTPTY.put("monthlySpending", df.format(monthlySpending));
            currentTPTY.put("totalSpending", df.format(totalSpending));
            currentTPTY.put("count", count);
            currentTPTY.put("spendingPercentage", 1.00);
            currentTPTY.put("frequencyPercentage", 1.00);

            JSONArray thirdPTYarray = new JSONArray();
            thirdPTYarray.add(currentTPTY);

            summarised.put("thirdParty", thirdPTYarray);
        }

        return summarised;
    }

    private static String findTheMost(JSONArray thirdPartyArray, String item) {
        float highest = 0;
        String mostFreqItem = "";

        for (int i = 0; i < thirdPartyArray.size(); i++) {
            JSONObject temp = (JSONObject) thirdPartyArray.get(i);

            if (Float.parseFloat(temp.get(item).toString()) > highest) {
                highest = Float.parseFloat(temp.get(item).toString());
                mostFreqItem = temp.get("thirdParty").toString();
            }
        }

        return mostFreqItem;
    }

    private static JSONObject totalSpending(JSONObject clustered) {
        Set<String> categories = clustered.keySet();
        float totalSpent = 0;
        float monthly = 0;

        for (String category : categories) {
            JSONArray current = (JSONArray) clustered.get(category);

            for (int i = 0; i < current.size(); i++) {
                JSONObject transaction = (JSONObject) current.get(i);

                totalSpent = totalSpent + Float.parseFloat(transaction.get("amount").toString());
            }
        }

        totalSpent = Math.abs(totalSpent);
        monthly = totalSpent / 12;

        JSONObject customer = new JSONObject();

        customer.put("totalSpent", totalSpent);
        customer.put("averageMonthlySpent", monthly);
        customer.put("transactions", clustered);

        return customer;
    }

    private static JSONObject removeEmpty(JSONObject clustered) {
        Set<String> categories = clustered.keySet();
        ArrayList<String> toRemove = new ArrayList<>();

        for (String category : categories) {
            JSONArray current = (JSONArray) clustered.get(category);

            if (current.isEmpty() == true) {
                toRemove.add(category);
            }
        }

        for (String remove : toRemove) {
            clustered.remove(remove);
        }

        return clustered;
    }

    private static JSONObject clusterTransactions(JSONArray transactions) {
        JSONObject clustered = new JSONObject();

        JSONArray loans = new JSONArray();
        JSONArray sacc = new JSONArray();
        JSONArray non_SACC = new JSONArray();
        JSONArray insurance = new JSONArray();
        JSONArray groceries = new JSONArray();
        JSONArray gambling = new JSONArray();
        JSONArray subscription = new JSONArray();
        JSONArray telecommunications = new JSONArray();
        JSONArray utilities = new JSONArray();

        for (int i = 0; i < transactions.size(); i++) {
            JSONObject currentTransaction = (JSONObject) transactions.get(i);

            switch (currentTransaction.get("category").toString()) {
                case "Loans":
                    loans.add(currentTransaction);
                    break;
                case "SACC Loans":
                    sacc.add(currentTransaction);
                    break;
                case "Non SACC Loans":
                    non_SACC.add(currentTransaction);
                    break;
                case "Insurance":
                    insurance.add(currentTransaction);
                    break;
                case "Groceries":
                    groceries.add(currentTransaction);
                    break;
                case "Gambling":
                    gambling.add(currentTransaction);
                    break;
                case "Subscription TV":
                    subscription.add(currentTransaction);
                    break;
                case "Telecommunications":
                    telecommunications.add(currentTransaction);
                    break;
                case "Utilities":
                    utilities.add(currentTransaction);
                    break;
            }
        }
        clustered.put("loans", loans);
        clustered.put("sacc", sacc);
        clustered.put("non_sacc", non_SACC);
        clustered.put("insurance", insurance);
        clustered.put("groceries", groceries);
        clustered.put("gambling", gambling);
        clustered.put("subscription", subscription);
        clustered.put("telecommunications", telecommunications);
        clustered.put("utilities", utilities);

        return clustered;
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

        constructJSON(customers);

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
        if (bankAccount.size() > 1) {
            //get rid of last "," , because now it is the end of the transaction, no more transactions to add
            toObject = toObject.substring(0, toObject.length() - 1);
        }
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

        String wholeFile = "{ \"customers\":[";
        try {
            JSONObject parsedObject = (JSONObject) jp.parse(file);
            JSONArray customers = (JSONArray) parsedObject.get("customers");
            JSONObject tidyObject = new JSONObject();

            for (int i = 0; i < customers.size(); i++) {
                JSONObject currentCustomer = (JSONObject) customers.get(i);
                JSONArray transactions = (JSONArray) currentCustomer.get("transactions");
                //for contain reconstructed transactions
                JSONArray transactionList = new JSONArray();

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

        summariseTransactions(tidyJson);

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


        HashMap<String, String> tagList = new HashMap<String, String>();

        for (int i = 0; i < tags.size(); i++) {
            JSONObject tag = (JSONObject) tags.get(i);

            if (tag.get("category") != null && model.getCategory().contains(tag.get("category"))) {
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

    private void addActionListener() {
        for (int i = 0; i < this.fileType.panel.getComponents().length; i++) {
            if (this.fileType.panel.getComponent(i).getName() == "single") {
                JButton single = (JButton) this.fileType.panel.getComponent(i);
                single.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        JFileChooser fileChooser = new JFileChooser();

                        fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));

                        int status = fileChooser.showOpenDialog(null);

                        if (status == JFileChooser.APPROVE_OPTION) {
                            model.files = fileChooser.getSelectedFiles();
                        }
                        fileType.frame.dispose();
                    }
                });
            } else if (this.fileType.panel.getComponent(i).getName() == "multiple") {
                JButton multiple = (JButton) this.fileType.panel.getComponent(i);
                multiple.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        JFileChooser fileChooser = new JFileChooser();

                        fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
                        fileChooser.setMultiSelectionEnabled(true);

                        int status = fileChooser.showOpenDialog(null);

                        if (status == JFileChooser.APPROVE_OPTION) {
                            model.files = fileChooser.getSelectedFiles();
                        }
                        fileType.frame.dispose();
                        eliminateUnnecessaryData(model.files);
                    }
                });
            }
        }
    }
}
