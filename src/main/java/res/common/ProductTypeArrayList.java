package res.common;

import java.util.ArrayList;

public class ProductTypeArrayList implements java.io.Serializable {
    private ArrayList<ProductType> list;

    public ProductTypeArrayList() {
        list = new ArrayList<>();
    }

    public void setList(ArrayList<ProductType> list) {
        this.list = list;
    }

    public ArrayList<ProductType> getList() {
        return list;
    }
}
