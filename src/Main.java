import javax.swing.SwingUtilities;
import ui.Login;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(Login::new);
    }
}