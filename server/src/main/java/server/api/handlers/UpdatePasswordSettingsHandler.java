package server.api.handlers;

import shareddto.EventType;
import shareddto.SocketMessage;
import shareddto.admin.PasswordSettingsDto;
import shareddto.admin.PasswordSettingsUpdateRequest;
import server.application.adaptors.PasswordSettingsService;
import server.domain.PasswordSettings;

import java.net.Socket;

public class UpdatePasswordSettingsHandler extends AbstractSocketHandler {
    private final PasswordSettingsService passwordSettingsService;

    public UpdatePasswordSettingsHandler(PasswordSettingsService passwordSettingsService) {
        this.passwordSettingsService = passwordSettingsService;
    }

    @Override
    public void handle(Object data, Socket clientSocket) throws Exception {
        try {
            PasswordSettingsUpdateRequest request = gson.fromJson(gson.toJsonTree(data),
                    PasswordSettingsUpdateRequest.class);
            if (request == null) {
                throw new IllegalArgumentException("request is required");
            }
            passwordSettingsService.updatePasswordSettings(
                    request.isPasswordlength8(),
                    request.isOneUpperletter(),
                    request.isOneNumber());
            PasswordSettings updated = passwordSettingsService.getPasswordSettings();
            PasswordSettingsDto dto = new PasswordSettingsDto(
                    updated.isPasswordlength8(),
                    updated.isOneUpperletter(),
                    updated.isOneNumber());
            sendMessage(clientSocket, new SocketMessage(EventType.UPDATE_PASSWORD_SETTINGS, dto));
        } catch (Exception e) {
            sendMessage(clientSocket, new SocketMessage(EventType.UPDATE_PASSWORD_SETTINGS, e.getMessage()));
        }
    }
}
