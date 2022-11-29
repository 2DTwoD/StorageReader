package SwingComponents;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Set;

import javax.swing.*;

public class FilterComboBox extends JComboBox<String> {
    final private Set<String> array;
    final private JTextField textField;
    public FilterComboBox(Set<String> array) {
        super(array.toArray(String[]::new));
        this.array = array;
        FilterComboBox thisForKeyAdapter = this;
        this.setEditable(true);
        textField = (JTextField) this.getEditor().getEditorComponent();
        textField.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent ke) {
                SwingUtilities.invokeLater(() -> comboFilter(textField.getText(), textField.getCaretPosition()));
            }
            public void keyReleased(KeyEvent ke) {
                SwingUtilities.invokeLater(() -> {
                    if (ke.getKeyCode() == KeyEvent.VK_ENTER || ke.getKeyCode() == KeyEvent.VK_ESCAPE)
                        thisForKeyAdapter.hidePopup();
                });
            }
        });
    }

    public void comboFilter(String enteredText, int caretPosition) {
        ArrayList<String> filterArray= new ArrayList<>();
        String enteredTextTrim = enteredText.trim();
        for (String s : array) {
            if (s == null) {
                break;
            }
            if (s.toLowerCase().contains(enteredTextTrim.toLowerCase())) {
                filterArray.add(s);
            }
        }
        if (filterArray.size() > 0) {
            this.setModel(new DefaultComboBoxModel<>(filterArray.toArray(String[]::new)));
            this.setSelectedItem(enteredText);
            textField.setCaretPosition(caretPosition);
            this.showPopup();
        } else {
            this.hidePopup();
        }
    }
}
