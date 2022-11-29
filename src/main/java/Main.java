import SwingComponents.MainPanel;

import javax.swing.*;
import java.awt.*;

public class Main extends JFrame {
    public static void main(String[] args) {
        new Main();
    }
    public Main() {
        super("Чтец складов Hörmann");
        UIManager.getLookAndFeelDefaults().put("ComboBox.noActionOnKeyNavigation", true);

        JScrollPane scroller = new JScrollPane(new MainPanel());
        scroller.getVerticalScrollBar().setUnitIncrement(20);
        add(scroller);
        setPreferredSize(new Dimension(1024, 768));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setVisible(true);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        //Размеры экрана
        //System.out.println(Toolkit.getDefaultToolkit().getScreenSize());
    }
}
