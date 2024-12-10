package res.common;

import java.util.ArrayList;

public class WorkerArrayList implements java.io.Serializable {
    private ArrayList<Worker> list;

    public WorkerArrayList() {
        list = new ArrayList<>();
    }

    public void setList(ArrayList<Worker> list) {
        this.list = list;
    }

    public ArrayList<Worker> getList() {
        return list;
    }
}
