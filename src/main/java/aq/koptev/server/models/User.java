package aq.koptev.server.models;

public class User implements Comparable<User> {

    private String login;
    private String password;

    public User(String name, String password) {
        this.login = name;
        this.password = password;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public int compareTo(User o) {
        return o.login.compareTo(login);
    }

    public void setLogin(String login) {
        this.login = login;
    }
}
