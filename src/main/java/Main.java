public class Main {
    public static void main(String[] args) {
        Model model = new Model();

        FileTypeSelection fileTypeSelection = new FileTypeSelection();

        Controller control = new Controller(fileTypeSelection, model);
    }
}
