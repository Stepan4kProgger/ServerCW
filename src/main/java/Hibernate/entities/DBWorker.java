package Hibernate.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "worker")
public class DBWorker {
    @Id
    @Column(name = "name", length = 64, nullable = false)
    private String name;

    @Column(name = "age", nullable = false)
    private int age;

    @Column(name = "login", length = 32)
    private String login;

    @Column(name = "post", length = 32, nullable = false)
    private String post;

    // Getters и setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPost() {
        return post;
    }

    public void setPost(String post) {
        this.post = post;
    }
}

