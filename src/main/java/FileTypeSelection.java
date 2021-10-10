import javax.swing.*;

public class FileTypeSelection extends JFrame {
    JFrame frame;
    JPanel panel;

    /**
     * initialisation of the file type selection
     * <p>
     * FileTypeSelection is just a window with two buttons in it, and it allows user to choose file type: single or multiple
     */
    FileTypeSelection() {
        frame = new JFrame();
        panel = new JPanel();

        panel.setName("panel");

        frame.setSize(500, 100);
        frame.setTitle("File type selection");
        frame.add(panel);
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        JLabel message = new JLabel("Single file or Multiple Files? Single file must be formatted by VSCode before" +
                " loading.");
        panel.add(message);

        JButton single = new JButton("Single File");

        single.setName("single");

        JButton multiple = new JButton("Multiple File");

        multiple.setName("multiple");

        panel.add(single);
        panel.add(multiple);
    }
}
