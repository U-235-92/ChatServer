package aq.koptev.server.models;

import aq.koptev.server.handlers.Handler;
import aq.koptev.server.sevicies.registration.RegistrationService;

import java.io.DataInputStream;
import java.io.IOException;

public class Registrator {

    private Handler handler;
    private RegistrationService registrationService;

    private boolean isProcessSuccess = false;

    public Registrator(Handler handler, RegistrationService registrationService) {
        this.handler = handler;
        this.registrationService = registrationService;
    }

    public synchronized void closeRegistrationProcess() {
        isProcessSuccess = true;
    }

    public void registration(DataInputStream inputStream) {
        Thread registrationThread = new Thread(() -> {
            while(!isProcessSuccess) {
                String message = null;
                try {
                    message = inputStream.readUTF();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                if(!message.startsWith("#")) {
                    System.out.println("Bad command");
                    continue;
                }
                String command = message.split("\\s+", 2)[0];
                if(command.equals(Command.REGISTRATION_COMMAND.getCommand())) {
                    String dataRegistration = message.split("\\s+", 2)[1];
                    try {
                        processRegistration(dataRegistration);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
        registrationThread.start();
    }

    private void processRegistration(String dataRegistration) throws IOException {
        String login = dataRegistration.split("\\s+", 2)[0];
        String password = dataRegistration.split("\\s+", 2)[1];
        if(login.matches("\\s+")) {
            handler.sendMessage(Command.ERROR_REGISTRATION_COMMAND.getCommand(), "Логин не может содержать символ пробела");
        } else if(login.length() > 30 || password.length() > 30) {
            handler.sendMessage(Command.ERROR_REGISTRATION_COMMAND.getCommand(), "Поле логин и пароль не могут быть длинее 30 символов");
        } else {
            User user = new User(login, password);
            if(registrationService.registerUser(user)) {
                handler.sendMessage(Command.OK_REGISTRATION_COMMAND.getCommand(), "");
            } else {
                handler.sendMessage(Command.ERROR_REGISTRATION_COMMAND.getCommand(), String.format("Логин %s уже занят", login));
            }
        }
    }
}
