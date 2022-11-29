package DataContainers;

import com.sourceforge.snap7.moka7.S7;

public class CellInfo {
    public String storage;
    public int xPos;
    public int yPos;
    public int zPos;
    public String side;
    public int position;
    public int depth;
    public String number;
    public int height;
    public int mass;
    public CellInfo(String storage, int xPos, int yPos, int zPos, String side, byte[] data){
        try {
            setCellInfo(storage, xPos, yPos, zPos, side, data);
        } catch (Exception e){
            System.out.println(e.getMessage());
        }

    }
    public void setCellInfo(String storage, int xPos, int yPos, int zPos, String side, byte[] data) throws Exception {
        if (data.length != 30){
            throw new Exception("Неправильный размер массива байтов");
        }
        this.storage = storage;
        this.xPos = xPos;
        this.yPos = yPos;
        this.zPos = zPos;
        this.side = side;
        position = S7.GetWordAt(data, 0);
        depth = S7.GetWordAt(data, 2);
        number = S7.GetStringAt(data, 4, 20);
        height = S7.GetWordAt(data, 24);
        mass = S7.GetDIntAt(data, 26);
    }
    public void print(){
        System.out.printf("xPos: %d, yPos: %d, zPos: %d, side: %s, position: %d, depth: %d" +
                ", number: %s, height: %d, mass: %d\n", xPos, yPos, zPos, side, position, depth, number, height, mass);
    }
}
