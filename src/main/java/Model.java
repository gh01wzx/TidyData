import java.io.File;
import java.util.Arrays;
import java.util.List;

public class Model {
    //store uer file type selection result
    String fileType;
    //desired category
    final List<String> category = Arrays.asList("Loans", "SACC Loans", "Non SACC Loans", "Insurance", "Groceries",
            "Gambling", "Subscription TV", "Telecommunications", "Utilities");
    // selected file from file chooser
    File[] files;

    public List<String> getCategory() {
        return category;
    }
}
