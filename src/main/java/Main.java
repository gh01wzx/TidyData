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
//        int fileType = fileTypeSelection();
//        //open a JFileChooser, load multiple json files
//        File[] files = FileChooser.loadTheFiles();
//        //eliminate unnecessary data and concatenate files
//        String wholeFile = eliminateUnnecessaryData(files);
//        //neat object
//        JSONObject tidyJSON = constructJSON(wholeFile);
//        //summaries the transactions(construct relationship property)
//        JSONObject summarised = summarisedTransactions(tidyJSON);
//        //write to local
//        writeLocalFile(tidyJSON);
        Model model = new Model();
        FileTypeSelection fileTypeSelection = new FileTypeSelection();
        Controller control = new Controller(fileTypeSelection,model);
    }


}
