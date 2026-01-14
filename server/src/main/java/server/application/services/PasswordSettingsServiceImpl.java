package server.application.services;

import server.application.adaptors.PasswordSettingsService;
import server.domain.PasswordSettings;
import server.infustructre.adaptors.PasswordSettingsRepository;

public class PasswordSettingsServiceImpl implements PasswordSettingsService {
    private final PasswordSettingsRepository passwordSettingsRepository;

    public PasswordSettingsServiceImpl(PasswordSettingsRepository passwordSettingsRepository) {
        if (passwordSettingsRepository == null) {
            throw new IllegalArgumentException("passwordSettingsRepository must not be null");
        }
        this.passwordSettingsRepository = passwordSettingsRepository;
    }

    @Override
    public PasswordSettings getPasswordSettings() {
        return passwordSettingsRepository.load();
    }

    @Override
    public void updatePasswordSettings(boolean passwordlength8, boolean oneUpperletter, boolean oneNumber) {
        PasswordSettings settings = new PasswordSettings(passwordlength8, oneUpperletter, oneNumber);
        passwordSettingsRepository.save(settings);
    }

    @Override
    public void updatePasswordlength8(boolean passwordlength8) {
        PasswordSettings currentSettings = passwordSettingsRepository.load();
        PasswordSettings updatedSettings = new PasswordSettings(
                passwordlength8,
                currentSettings.isOneUpperletter(),
                currentSettings.isOneNumber());
        passwordSettingsRepository.save(updatedSettings);
    }

    @Override
    public void updateOneUpperletter(boolean oneUpperletter) {
        PasswordSettings currentSettings = passwordSettingsRepository.load();
        PasswordSettings updatedSettings = new PasswordSettings(
                currentSettings.isPasswordlength8(),
                oneUpperletter,
                currentSettings.isOneNumber());
        passwordSettingsRepository.save(updatedSettings);
    }

    @Override
    public void updateOneNumber(boolean oneNumber) {
        PasswordSettings currentSettings = passwordSettingsRepository.load();
        PasswordSettings updatedSettings = new PasswordSettings(
                currentSettings.isPasswordlength8(),
                currentSettings.isOneUpperletter(),
                oneNumber);
        passwordSettingsRepository.save(updatedSettings);
    }
}
