import java.io.File;
import java.util.Arrays;
import java.util.List;

public class Model {
    String fileType;
    final List<String> category = Arrays.asList("Loans", "SACC Loans", "Non SACC Loans", "Insurance", "Groceries",
            "Gambling", "Subscription TV", "Telecommunications", "Utilities");
    File[] files;

    public List<String> getCategory() {
        return category;
    }
}
