package res.common;

public class Worker implements java.io.Serializable, StringConvertible, Clone<Worker> {
    private String name;
    private int age;
    private String login;
    private String post;

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public int getAge() {
        return age;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getLogin() {
        return login;
    }

    public void setPost(String post) {
        this.post = post;
    }

    public String getPost() {
        return post;
    }

    public Worker() {
    }

    public Worker(Worker worker) {
        this.name = worker.name;
        this.age = worker.age;
        this.login = worker.login;
        this.post = worker.post;
    }

    @Override
    public String toQueryString() {
        return "'" + getName() + "', " + getAge() + ", '" + getPost() + "'";
    }

    @Override
    public Worker makeClone() {
        return new Worker(this);
    }
}
