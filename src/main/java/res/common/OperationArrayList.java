package res.common;

import java.util.ArrayList;

public class OperationArrayList implements java.io.Serializable {
    private ArrayList<Operation> list;

    public OperationArrayList() {
        list = new ArrayList<>();
    }

    public void setList(ArrayList<Operation> list) {
        this.list = list;
    }

    public ArrayList<Operation> getList() {
        return list;
    }
}
