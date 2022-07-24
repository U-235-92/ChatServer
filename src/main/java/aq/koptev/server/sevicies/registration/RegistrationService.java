package aq.koptev.server.sevicies.registration;

import aq.koptev.server.models.User;

public interface RegistrationService {

    boolean registerUser(User user);
}
