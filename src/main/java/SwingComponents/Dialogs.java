package SwingComponents;

import javax.swing.*;

public class Dialogs extends JOptionPane {

    public void messageDialog(String text){
        showMessageDialog(null, text, "Внимание", INFORMATION_MESSAGE);
    }

    public void errorDialog(String text){
        showMessageDialog(null, text, "Ошибка", ERROR_MESSAGE);
    }

    public int confirmDialog(String text){
        return showConfirmDialog(null, text, "Подтверждение", OK_CANCEL_OPTION);
    }
}
