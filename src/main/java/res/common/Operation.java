package res.common;

public class Operation implements java.io.Serializable {
    private String login;
    private String name;
    private String operationType;
    private String targetType;
    private String target;
    private String info;

    public void setLogin(String login) {
        this.login = login;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getLogin() {
        return login;
    }

    public String getName() {
        return name;
    }

    public String getOperationType() {
        return operationType;
    }

    public String getTargetType() {
        return targetType;
    }

    public String getTarget() {
        return target;
    }

    public String getInfo() {
        return info;
    }
}
