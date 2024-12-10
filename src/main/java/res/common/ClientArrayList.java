package res.common;

import java.util.ArrayList;

public class ClientArrayList implements java.io.Serializable {
    private ArrayList<Client> list;

    public ClientArrayList() {
        list = new ArrayList<>();
    }

    public void setList(ArrayList<Client> list) {
        this.list = list;
    }

    public ArrayList<Client> getList() {
        return list;
    }
}
