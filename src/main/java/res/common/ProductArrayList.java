package res.common;

import java.util.ArrayList;

public class ProductArrayList implements java.io.Serializable {
    private ArrayList<Product> list;

    public ProductArrayList() {
        list = new ArrayList<>();
    }

    public void setList(ArrayList<Product> list) {
        this.list = list;
    }

    public ArrayList<Product> getList() {
        return list;
    }
}
