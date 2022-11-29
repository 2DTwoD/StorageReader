package DataContainers;

import SwingComponents.Dialogs;
import com.sourceforge.snap7.moka7.S7;
import com.sourceforge.snap7.moka7.S7Client;

import javax.swing.*;
import java.util.*;
import java.util.stream.IntStream;

public class CellContainer {
    String storage;
    String PLCip;
    int firstDBnum;
    int allDBbytes;
    final int byteSizeOfOneCell = 30;
    int numOfSerials;
    int numOfFloors;
    int numOfDepths;
    int[] skipX;
    int[] skipY;
    int[] skipXMRD2 = {2, 5};
    int[] skipXMRD34 = {21};
    int[] skipYMRD34 = {8};

    Comparator<String> comparator = (text1, text2) -> {
        int value1 = -1;
        int value2 = -1;
        if(!text1.equals("")){
            value1 = Integer.parseInt(text1);
        }
        if(!text2.equals("")){
            value2 = Integer.parseInt(text2);
        }
        return value1 - value2;
    };

    public ArrayList<CellInfo> cellInfoList = new ArrayList<>();
    public TreeSet<String> storageSet = new TreeSet<>();
    public TreeSet<String> xPosSet = new TreeSet<>(comparator);
    public TreeSet<String> yPosSet = new TreeSet<>(comparator);
    public TreeSet<String> zPosSet = new TreeSet<>(comparator);
    public TreeSet<String> sideSet = new TreeSet<>();
    public TreeSet<String> positionSet = new TreeSet<>(comparator);
    public TreeSet<String> depthSet = new TreeSet<>(comparator);
    public TreeSet<String> numberSet = new TreeSet<>();
    public TreeSet<String> heightSet = new TreeSet<>(comparator);
    public TreeSet<String> massSet = new TreeSet<>(comparator);

    JLabel statusLabel;
    Dialogs dialog = new Dialogs();

    public CellContainer(JLabel statusLabel){
        this.statusLabel = statusLabel;
        clearAllCollections();
    }
    public void getAllPLCData(){
        clearAllCollections();
        storage = "Склад рулонов, МРД2";
        PLCip = "192.168.32.22";//"192.168.137.2" - тестовый адрес
        firstDBnum = 801;
        allDBbytes = 1200;
        numOfSerials = 22;
        numOfFloors = 4;
        numOfDepths = 5;
        skipX = skipXMRD2;
        skipY = new int[]{};
        getDataFromPLC();
        storage = "Склад паллет, МРД3";
        PLCip = "192.168.32.23";
        firstDBnum = 801;
        allDBbytes = 960;
        numOfSerials = 21;
        numOfFloors = 8;
        numOfDepths = 2;
        skipX = skipXMRD34;
        skipY = skipYMRD34;
        getDataFromPLC();
        storage = "Склад паллет, МРД4";
        PLCip = "192.168.32.24";
        firstDBnum = 801;
        allDBbytes = 960;
        numOfSerials = 21;
        numOfFloors = 8;
        numOfDepths = 2;
        skipX = skipXMRD34;
        skipY = skipYMRD34;
        getDataFromPLC();
        statusLabel.setText("Готово! Всего строк: " + cellInfoList.size());
    }

    private void getDataFromPLC(){
        statusLabel.setText("Поключение к ПЛК: " + storage);
        S7Client client = new S7Client();
        client.SetConnectionType (S7.S7_BASIC);
        try {
            int result = client.ConnectTo(PLCip, 0, 2);
            if(result != 0) {
                dialog.errorDialog(String.format("Проблема: %s, %s", storage, S7Client.ErrorText(result)));
                return;
            }
            byte[] data = new byte[allDBbytes];
            int numOfContainersInDB = allDBbytes / byteSizeOfOneCell;
            storageSet.add(storage);
            CellInfo cell;
            String side;
            int xShift = 0;
            int floor = 0;
            for (int n : IntStream.range(0, numOfSerials).boxed().toList()) {
                if (arrayContains(skipX, n + 1)){
                    xShift ++;
                    continue;
                }
                client.ReadArea(S7.S7AreaDB, firstDBnum + n, 0, data.length, data);
                for (int i : IntStream.range(0, numOfContainersInDB).boxed().toList()) {
                    floor = (i / (numOfContainersInDB / 2 / numOfFloors)) % numOfFloors + 1;
                    if (arrayContains(skipY, floor)){
                        continue;
                    }
                    side = i < numOfContainersInDB / 2 ? "Левая сторона" : "Правая сторона";
                    cell = new CellInfo(storage, n + 1 - xShift, floor, i % numOfDepths + 1,
                            side, Arrays.copyOfRange(data, i * byteSizeOfOneCell, (i + 1) * byteSizeOfOneCell));
                    //cell.print();
                    xPosSet.add(String.valueOf(cell.xPos));
                    yPosSet.add(String.valueOf(cell.yPos));
                    zPosSet.add(String.valueOf(cell.zPos));
                    sideSet.add(cell.side);
                    positionSet.add(String.valueOf(cell.position));
                    depthSet.add(String.valueOf(cell.depth));
                    numberSet.add(cell.number);
                    heightSet.add(String.valueOf(cell.height));
                    massSet.add(String.valueOf(cell.mass));
                    cellInfoList.add(cell);
                }
                statusLabel.setText(String.format("Чтение с ПЛК: %s, Загружено: %d %%",
                        storage, 100 * (n + 1) / numOfSerials));
            }
        }
        catch (Exception e){
            dialog.errorDialog(String.format("Проблема: %s, %s", storage, e.getMessage()));
        }
        finally {
            client.Disconnect();
        }
    }

    boolean arrayContains(int[] array, int value){
        boolean result = false;
        for (int item: array){
            if (item == value){
                result = true;
                break;
            }
        }
        return  result;
    }


    void clearAllCollections(){
        storageSet.clear();
        xPosSet.clear();
        yPosSet.clear();
        zPosSet.clear();
        sideSet.clear();
        positionSet.clear();
        depthSet.clear();
        numberSet.clear();
        heightSet.clear();
        massSet.clear();
        cellInfoList.clear();
        storageSet.add("");
        xPosSet.add("");
        yPosSet.add("");
        zPosSet.add("");
        sideSet.add("");
        positionSet.add("");
        depthSet.add("");
        numberSet.add("");
        heightSet.add("");
        massSet.add("");
    }
}
