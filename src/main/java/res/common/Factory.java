package res.common;

public class Factory {
    public static Client makeClient() {
        return new Client();
    }

    public static Worker makeWorker() {
        return new Worker();
    }

    public static Operation makeOperation() {
        return new Operation();
    }

    public static Product makeProduct() {
        return new Product();
    }
}
