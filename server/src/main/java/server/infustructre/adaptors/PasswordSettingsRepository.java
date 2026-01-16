package server.infustructre.adaptors;

import server.domain.PasswordSettings;

public interface PasswordSettingsRepository {
    void save(PasswordSettings settings);

    PasswordSettings load();
}
