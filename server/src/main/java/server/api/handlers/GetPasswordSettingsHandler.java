package server.api.handlers;

import shareddto.EventType;
import shareddto.SocketMessage;
import shareddto.admin.PasswordSettingsDto;
import server.application.adaptors.PasswordSettingsService;
import server.domain.PasswordSettings;

import java.net.Socket;

public class GetPasswordSettingsHandler extends AbstractSocketHandler {
    private final PasswordSettingsService passwordSettingsService;

    public GetPasswordSettingsHandler(PasswordSettingsService passwordSettingsService) {
        this.passwordSettingsService = passwordSettingsService;
    }

    @Override
    public void handle(Object data, Socket clientSocket) throws Exception {
        try {
            PasswordSettings settings = passwordSettingsService.getPasswordSettings();
            PasswordSettingsDto dto = new PasswordSettingsDto(
                    settings.isPasswordlength8(),
                    settings.isOneUpperletter(),
                    settings.isOneNumber());
            sendMessage(clientSocket, new SocketMessage(EventType.GET_PASSWORD_SETTINGS, dto));
        } catch (Exception e) {
            sendMessage(clientSocket, new SocketMessage(EventType.GET_PASSWORD_SETTINGS, e.getMessage()));
        }
    }
}
