package SwingComponents;

import DataContainers.CellContainer;
import DataContainers.CellInfo;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.print.PrinterException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainPanel extends JPanel {
    CellContainer cellContainer;
    ArrayList<CellInfo> filteredCellList;
    JPanel line1 = new JPanel(new FlowLayout());
    JPanel line2 = new JPanel(new FlowLayout());
    JPanel line3 = new JPanel(new FlowLayout());
    JPanel line4 = new JPanel(new FlowLayout());
    JPanel line5 = new JPanel(new FlowLayout());
    JPanel line6 = new JPanel(new FlowLayout());
    JPanel statusBar = new JPanel(new GridLayout());
    JLabel statusLabel = new JLabel("Привет, давай поработаем!", JLabel.LEFT);
    JTable mainTable;
    JScrollPane scroller;

    JButton readPLCButton = new JButton("Считать расположение грузов из ПЛК");
    JButton saveButton = new JButton("Сохранить в файл");
    JButton printButton = new JButton("Распечатать");
    JButton applyFilters = new JButton("Применить фильтры");
    JButton clearFilters = new JButton("Очистить фильтры");
    final String[] tableHeader = {
            "Наименование склада",
            "Номер груза",
            "Пролёт (коорд. X)",
            "Этаж (коорд. Y)",
            "Сторона (коорд. S)",
            "Глубина (коорд. Z)",
            "Позиция",
            "Глубина",
            "Высота",
            "Масса"
    };
    final int[] columnWidthArray = {155, 165, 100, 100, 100, 100, 70, 70, 70, 70};

    FilterBlock storageFilter;
    FilterBlock numberFilter;
    FilterBlock xFilter;
    FilterBlock yFilter;
    FilterBlock sideFilter;
    FilterBlock zFilter;
    FilterBlock positionFilter;
    FilterBlock depthFilter;
    FilterBlock heightFilter;
    FilterBlock massFilter;

    ExecutorService thread = Executors.newSingleThreadExecutor();
    Dialogs dialog = new Dialogs();

    public MainPanel(){
        cellContainer = new CellContainer(statusLabel);
        filteredCellList = new ArrayList<>(cellContainer.cellInfoList);
        setLayout(new VerticalLayout(this, 10, 0, VerticalLayout.CENTER));

        storageFilter = new FilterBlock(cellContainer.storageSet, columnWidthArray[0], false, false);
        numberFilter = new FilterBlock(cellContainer.numberSet, columnWidthArray[1], true, true);
        xFilter = new FilterBlock(cellContainer.xPosSet, columnWidthArray[2], false, false);
        yFilter = new FilterBlock(cellContainer.yPosSet, columnWidthArray[3], false, false);
        sideFilter = new FilterBlock(cellContainer.sideSet, columnWidthArray[4], false, false);
        zFilter = new FilterBlock(cellContainer.zPosSet, columnWidthArray[5], false, false);
        positionFilter = new FilterBlock(cellContainer.positionSet, columnWidthArray[6], false, true);
        depthFilter = new FilterBlock(cellContainer.depthSet, columnWidthArray[7], false, true);
        heightFilter = new FilterBlock(cellContainer.heightSet, columnWidthArray[8], false, true);
        massFilter = new FilterBlock(cellContainer.massSet, columnWidthArray[9], false, true);

        createPanel();
        thread.shutdown();

        readPLCButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (!thread.isTerminated()) {
                    dialog.messageDialog("Программа выполняет действия, подождите");
                    return;
                }
                if (dialog.confirmDialog("Считать данные с ПЛК складов?") != 0){
                    return;
                }
                thread = Executors.newSingleThreadExecutor();
                thread.submit(() -> {
                    cellContainer.getAllPLCData();
                    filteredCellList = new ArrayList<>(cellContainer.cellInfoList);
                    storageFilter.clearCombo();
                    numberFilter.clearCombo();
                    xFilter.clearCombo();
                    yFilter.clearCombo();
                    sideFilter.clearCombo();
                    zFilter.clearCombo();
                    positionFilter.clearCombo();
                    depthFilter.clearCombo();
                    heightFilter.clearCombo();
                    massFilter.clearCombo();
                    updateTable();
                    updateSets();
                    dialog.messageDialog("Считывание данных закончено!");
                });
                thread.shutdown();
            }
        });

        saveButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (!thread.isTerminated()) {
                    dialog.messageDialog("Программа выполняет действия, подождите");
                    return;
                }
                if(filteredCellList.size() == 0){
                    dialog.messageDialog("Таблица пуста");
                    return;
                }
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("Сохранить файл");
                if (fileChooser.showSaveDialog(line1) == JFileChooser.APPROVE_OPTION){
                    File file = fileChooser.getSelectedFile();
                    if (file == null){
                        dialog.errorDialog("Выбран некорректный путь при сохранении файла");
                        return;
                    }
                    if (!file.getName().toLowerCase().endsWith(".xls")){
                        file = new File(file.getParentFile(), file.getName() + ".xls");
                    }
                    //Создание xls файла
                    HSSFWorkbook workbook = new HSSFWorkbook();
                    HSSFSheet sheet = workbook.createSheet("Лист1");
                    int rowNum = 0;
                    Row row = sheet.createRow(rowNum);
                    sheet.createFreezePane(0, 1);
                    for (int i =0; i < tableHeader.length; i++){
                        row.createCell(i).setCellValue(tableHeader[i]);
                        sheet.autoSizeColumn(i);
                    }
                    for (CellInfo cell : filteredCellList) {
                        row = sheet.createRow(++rowNum);
                        row.createCell(0).setCellValue(cell.storage);
                        row.createCell(1).setCellValue(cell.number);
                        row.createCell(2).setCellValue(cell.xPos);
                        row.createCell(3).setCellValue(cell.yPos);
                        row.createCell(4).setCellValue(cell.side);
                        row.createCell(5).setCellValue(cell.zPos);
                        row.createCell(6).setCellValue(cell.position);
                        row.createCell(7).setCellValue(cell.depth);
                        row.createCell(8).setCellValue(cell.height);
                        row.createCell(9).setCellValue(cell.mass);
                    }
                    try (FileOutputStream out = new FileOutputStream(file)) {
                        workbook.write(out);
                        dialog.messageDialog("Файл сохранён");
                    } catch (IOException ex) {
                        dialog.errorDialog("Возникла ошибка в процессе записи файла: "+ ex.getMessage());
                    }
                }
            }
        });

        printButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (!thread.isTerminated()) {
                    dialog.messageDialog("Программа выполняет действия, подождите");
                    return;
                }
                if(filteredCellList.size() == 0){
                    dialog.messageDialog("Таблица пуста");
                    return;
                }
                try {
                    if (mainTable.print(JTable.PrintMode.FIT_WIDTH)){
                        dialog.messageDialog("Печать закончена");
                    }
                } catch (PrinterException ex) {
                    dialog.errorDialog("В процессе печати произошла ошибка: " + ex.getMessage());
                }
            }
        });

        applyFilters.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (!thread.isTerminated()) {
                    dialog.messageDialog("Программа выполняет действия, подождите");
                    return;
                }
                if (dialog.confirmDialog("Применить фильтры?") != 0){
                    return;
                }
                thread = Executors.newSingleThreadExecutor();
                thread.submit(() -> {
                    filteredCellList = new ArrayList<>(cellContainer.cellInfoList);

                    String storageRegEx = storageFilter.getFilter();
                    String numberRegEx = numberFilter.getFilter();
                    String xRegEx = xFilter.getFilter();
                    String yRegEx = yFilter.getFilter();
                    String sideRegEx = sideFilter.getFilter();
                    String zRegEx = zFilter.getFilter();
                    String positionRegEx = positionFilter.getFilter();
                    String depthRegEx = depthFilter.getFilter();
                    String heightRegEx = heightFilter.getFilter();
                    String massRegEx = massFilter.getFilter();

                    int i = 0;
                    boolean result;
                    int progressCount = 0;
                    int startListSize = filteredCellList.size();
                    while(i < filteredCellList.size()) {
                        CellInfo cellInfo = filteredCellList.get(i);
                        result = storageRegEx == null || cellInfo.storage.toLowerCase().matches(storageRegEx);
                        result &= numberRegEx == null || cellInfo.number.toLowerCase().matches(numberRegEx);
                        result &= xRegEx == null || String.valueOf(cellInfo.xPos).matches(xRegEx);
                        result &= yRegEx == null || String.valueOf(cellInfo.yPos).matches(yRegEx);
                        result &= sideRegEx == null || cellInfo.side.toLowerCase().matches(sideRegEx);
                        result &= zRegEx == null || String.valueOf(cellInfo.zPos).matches(zRegEx);
                        result &= positionRegEx == null || String.valueOf(cellInfo.position).matches(positionRegEx);
                        result &= depthRegEx == null || String.valueOf(cellInfo.depth).matches(depthRegEx);
                        result &= heightRegEx == null || String.valueOf(cellInfo.height).matches(heightRegEx);
                        result &= massRegEx == null || String.valueOf(cellInfo.mass).matches(massRegEx);
                        if (result) {
                            i++;
                        } else {
                            filteredCellList.remove(i);
                        }
                        progressCount++;
                        statusLabel.setText(String.format("Применение фильтров: %d %%", 100 * progressCount / startListSize));
                    }
                    updateTable();
                    statusLabel.setText("Готово! Всего строк: " + filteredCellList.size());
                    dialog.messageDialog("Фильтры применены!");
                });
                thread.shutdown();
            }
        });
        clearFilters.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (!thread.isTerminated()) {
                    dialog.messageDialog("Программа выполняет действия, подождите");
                    return;
                }
                if (dialog.confirmDialog("Очистить фильтры?") != 0){
                    return;
                }
                storageFilter.clearCombo();
                numberFilter.clearCombo();
                xFilter.clearCombo();
                yFilter.clearCombo();
                sideFilter.clearCombo();
                zFilter.clearCombo();
                positionFilter.clearCombo();
                depthFilter.clearCombo();
                heightFilter.clearCombo();
                massFilter.clearCombo();
            }
        });
    }
    void updateTable(){
        newTable();
        scroller.setViewportView(mainTable);
        scroller.repaint();
        scroller.revalidate();
    }
    void updateSets(){
        storageFilter.updateSet();
        numberFilter.updateSet();
        xFilter.updateSet();
        yFilter.updateSet();
        sideFilter.updateSet();
        zFilter.updateSet();
        positionFilter.updateSet();
        depthFilter.updateSet();
        heightFilter.updateSet();
        massFilter.updateSet();
    }
    void createPanel(){
        line1.add(getTitle("Чтец складов Hörmann"));
        line2.add(readPLCButton);
        line2.add(saveButton);
        line2.add(printButton);
        line3.add(getTitle("Фильтры:"));
        line4.add(applyFilters);
        line4.add(clearFilters);
        line5.add(storageFilter);
        line5.add(numberFilter);
        line5.add(xFilter);
        line5.add(yFilter);
        line5.add(sideFilter);
        line5.add(zFilter);
        line5.add(positionFilter);
        line5.add(depthFilter);
        line5.add(heightFilter);
        line5.add(massFilter);
        statusBar.add(statusLabel);
        newTable();
        scroller = new JScrollPane(mainTable);
        scroller.setPreferredSize(new Dimension(
                Arrays.stream(columnWidthArray).sum() + 17 * columnWidthArray.length,
                Toolkit.getDefaultToolkit().getScreenSize().height - 285));
        statusBar.setPreferredSize(new Dimension(
                Arrays.stream(columnWidthArray).sum() + 17 * columnWidthArray.length, 30));
        line6.add(scroller);
        add(line1);
        add(line2);
        add(line3);
        add(line4);
        add(line5);
        add(line6);
        add(statusBar);
    }
    void newTable(){
        String[][] data = new String[filteredCellList.size()][10];
        for (int i = 0; i < filteredCellList.size(); i++){
            data[i][0] = filteredCellList.get(i).storage;
            data[i][1] = filteredCellList.get(i).number;
            data[i][2] = String.valueOf(filteredCellList.get(i).xPos);
            data[i][3] = String.valueOf(filteredCellList.get(i).yPos);
            data[i][4] = filteredCellList.get(i).side;
            data[i][5] = String.valueOf(filteredCellList.get(i).zPos);
            data[i][6] = String.valueOf(filteredCellList.get(i).position);
            data[i][7] = String.valueOf(filteredCellList.get(i).depth);
            data[i][8] = String.valueOf(filteredCellList.get(i).height);
            data[i][9] = String.valueOf(filteredCellList.get(i).mass);
        }
        mainTable = new JTable(data, tableHeader);
        mainTable.setEnabled(false);
        mainTable.setAutoCreateRowSorter(true);
        mainTable.setFont(new Font(mainTable.getFont().getFontName(), Font.PLAIN, 15));
        mainTable.setRowHeight(22);
        mainTable.getTableHeader().setFont(new Font(mainTable.getFont().getFontName(), Font.BOLD, 12));

        TableColumn column;
        for (int i = 0; i < columnWidthArray.length; i++) {
            column = mainTable.getColumnModel().getColumn(i);
            column.setPreferredWidth(columnWidthArray[i]);
        }
    }
    JLabel getTitle(String text){
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(new Font(label.getFont().getFontName(), Font.ITALIC, 16));
        return label;
    }
}

class FilterBlock extends JPanel{
    JComboBox<String> combo;
    Set<String> set;
    boolean edit;
    FilterBlock(Set<String> set, int width, boolean filter, boolean edit){
        this.set = set;
        this.edit = edit;
        setLayout(new FlowLayout(FlowLayout.CENTER));
        if(filter) {
            combo = new FilterComboBox(set);
        } else {
            combo = new JComboBox<>(set.toArray(String[]::new));
            combo.setEditable(edit);
        }
        combo.setPreferredSize(new Dimension(width, 20));
        combo.setBackground(new Color(255, 255, 255));
        if(edit) {
            ((JTextField) combo.getEditor().getEditorComponent()).addCaretListener(
                    e -> onChange((JTextField) e.getSource()));
        } else {
            combo.addActionListener(e -> onChange(combo));
        }
        add(combo);
    }
    String getFilter(){
        String comboText = edit? ((JTextField) combo.getEditor().getEditorComponent()).getText().trim().toLowerCase():
                (String) combo.getSelectedItem();
        if (comboText == null || comboText.equals("")){
            return null;
        }
        if(edit) {
            return ".*" + comboText.toLowerCase() + ".*";
        } else {
            return comboText.toLowerCase();
        }
    }
    void updateSet(){
        combo.setModel(new DefaultComboBoxModel<>(set.toArray(String[]::new)));
    }
    void clearCombo(){
        if (edit) {
            JTextField field = (JTextField) combo.getEditor().getEditorComponent();
            field.setBackground(new Color(255, 255, 255));
            field.setText("");
        } else {
            combo.setSelectedIndex(0);
            combo.setBackground(new Color(255, 255, 255));
        }
    }

    void onChange(JComponent component){
        JTextField field = (JTextField) combo.getEditor().getEditorComponent();
        String comboText = field.getText();
        if (comboText == null) {
            return;
        }
        if (comboText.trim().equals("")) {
            component.setBackground(new Color(255, 255, 255));
        } else {
            component.setBackground(new Color(255, 255, 0));
        }
    }
}