package aq.koptev.server.sevicies.auth;

import aq.koptev.server.models.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SimpleAuthenticationService implements AuthenticationService {

    private List<User> users;
    private User authUser;

    public SimpleAuthenticationService() {
        users = new ArrayList<>();
        setUpUsers();
    }

    private void setUpUsers() {
        users.add(new User("tom", "111"));
        users.add(new User("knight", "111"));
        users.add(new User("e", ""));
        users.add(new User("w", ""));
        users.add(new User("ivan", "111"));
        users.add(new User("admin", "admin"));
        users.add(new User("test", "222"));
    }

    @Override
    public User getUser(String login, String password) {
        Collections.sort(users);
        int userIndex = Collections.binarySearch(users, new User(login, password));
        if(userIndex >= 0) {
            authUser = users.get(userIndex);
            if(authUser.getPassword().equals(password)) {
                return authUser;
            }
        }
        return null;
    }

    @Override
    public boolean isExistUser(String login) {
        Collections.sort(users);
        int userIndex = Collections.binarySearch(users, new User(login, ""));
        return userIndex >= 0;
    }

    @Override
    public String getErrorAuthenticationMessage(String login, String password) {
        if(authUser == null) {
            return WRONG_LOGIN;
        } else {
            if(!authUser.getPassword().equals(password)) {
                return WRONG_PASSWORD;
            } else {
                return EMPTY_MESSAGE;
            }
        }
    }

    @Override
    public void registerUser(User user) {
        users.add(user);
    }
}
