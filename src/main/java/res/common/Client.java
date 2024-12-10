package res.common;

public class Client implements java.io.Serializable, StringConvertible, Clone<Client> {
    private String login;
    private String password;
    private String name;
    private int admin;

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public String getName() {
        return name;
    }

    public int getAdmin() {
        return admin;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAdmin(int admin) {
        this.admin = admin;
    }

    public String isAdminStr() {
        return admin == 1 ? "Да" : "Нет";
    }

    public Client() {
    }

    public Client(String login, String password) {
        setLogin(login);
        setPassword(password);
    }

    public Client(String login, String password, int isAdmin) {
        setLogin(login);
        setPassword(password);
        setAdmin(isAdmin);
    }

    public Client(Client client) {
        setLogin(client.getLogin());
        setPassword(client.getPassword());
        setName(client.getName());
        setAdmin(client.getAdmin());
    }

    @Override
    public String toQueryString() {
        return "'" + getLogin() + "', '" + getPassword() + "', '" + getName() + "', " + getAdmin();
    }

    @Override
    public Client makeClone() {
        return new Client(this);
    }
}
