package ru.wolfa.cam.signals.receiver;

import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Service;

/**
 * Simple settings implementation based on set.
 * 
 * @author sasha
 *
 */
@Service
public class SettingsServiceImpl implements SettingsService{

    /**
     * Empty by default.<br/>
     * On disable add
     */
    Set<Integer> notificationStatus = new HashSet<>();

    public void enableNotifications(int camId) {
        notificationStatus.remove(Integer.valueOf(camId));
    }

    public void disableNotifications(int camId) {
        notificationStatus.add(Integer.valueOf(camId));
    }

    public void restoreSettings() {
        notificationStatus.clear();
    }

    public boolean isNotificationsEnabled(int camId) {
        return !notificationStatus.contains(Integer.valueOf(camId));
    }

}
