package server.application.adaptors;

import server.domain.PasswordSettings;

public interface PasswordSettingsService {
    PasswordSettings getPasswordSettings();

    void updatePasswordSettings(boolean passwordlength8, boolean oneUpperletter, boolean oneNumber);

    void updatePasswordlength8(boolean passwordlength8);

    void updateOneUpperletter(boolean oneUpperletter);

    void updateOneNumber(boolean oneNumber);
}
