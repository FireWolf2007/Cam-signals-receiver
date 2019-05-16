package ru.wolfa.cam.signals.receiver;

public interface SettingsService {

    void enableNotifications(int camId);

    void disableNotifications(int camId);

    void restoreSettings();

    boolean isNotificationsEnabled(int camId);

}
