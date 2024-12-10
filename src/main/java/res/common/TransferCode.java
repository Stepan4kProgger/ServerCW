package res.common;

public class TransferCode implements java.io.Serializable {
    private final String code;

    public TransferCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
