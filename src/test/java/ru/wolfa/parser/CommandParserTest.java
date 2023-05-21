package ru.wolfa.parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ru.wolfa.cam.driver.CameraDriver;
import ru.wolfa.cam.signals.receiver.SettingsService;
import ru.wolfa.telegram.TelegramSenderServiceImpl;
import static org.mockito.Mockito.*;

public class CommandParserTest {

    private CommandParser parser;
    private CameraDriver cameraDriver;
    private TelegramSenderServiceImpl sender;
    private SettingsService settings;

    @BeforeEach
    public void setupTest() {
        cameraDriver = mock(CameraDriver.class);
        sender = mock(TelegramSenderServiceImpl.class);
        settings = mock(SettingsService.class);
        when(sender.getBotName()).thenReturn("TestBot");
        parser = new CommandParser(cameraDriver, sender, settings);
    }

    @Test
    public void notCommand() {
        parser.process("simple message", 1234);
        parser.process(null, 1234);
        verify(sender, times(1)).getBotName();
        verifyNoMoreInteractions(sender);
        verifyNoMoreInteractions(cameraDriver);
    }

    @Test
    public void testHelp1() {
        parser.process("/start", 1234);
        verifyNoMoreInteractions(cameraDriver);
        verify(sender, times(1)).sendMessage(eq(1234), any());
        verifyNoMoreInteractions(cameraDriver);
    }

    @Test
    public void testHelp2() {
        parser.process("/help", 1233);
        verifyNoMoreInteractions(cameraDriver);
        verify(sender, times(1)).sendMessage(eq(1233), any());
        verifyNoMoreInteractions(cameraDriver);
    }

    @Test
    public void testNotExistCommand() {
        parser.process("/ANYCOMMAND", 1234);
        verify(sender, times(1)).getBotName();
        verify(sender, times(1)).sendMessage(eq(1234), eq("Команда не обработана.\nПомощь: /help"));
        verifyNoMoreInteractions(sender);
        verifyNoMoreInteractions(cameraDriver);
    }

    @Test
    public void testCam1Down() {
        parser.process("/d1", 1003);
        verify(cameraDriver, times(1)).executeCmd(eq(1003), eq(1), eq("down"));
        verify(sender, times(1)).getBotName();
        verifyNoMoreInteractions(sender);
        verifyNoMoreInteractions(cameraDriver);
    }

    @Test
    public void testCam1Up() {
        parser.process("/u1", 1453);
        verify(cameraDriver, times(1)).executeCmd(eq(1453), eq(1), eq("up"));
        verify(sender, times(1)).getBotName();
        verifyNoMoreInteractions(sender);
        verifyNoMoreInteractions(cameraDriver);
    }

    @Test
    public void testCam1Right() {
        parser.process("/r1", 1434);
        verify(cameraDriver, times(1)).executeCmd(eq(1434), eq(1), eq("right"));
        verify(sender, times(1)).getBotName();
        verifyNoMoreInteractions(sender);
        verifyNoMoreInteractions(cameraDriver);
    }

    @Test
    public void testCam1Left() {
        parser.process("/l1", 1679);
        verify(cameraDriver, times(1)).executeCmd(eq(1679), eq(1), eq("left"));
        verify(sender, times(1)).getBotName();
        verifyNoMoreInteractions(sender);
        verifyNoMoreInteractions(cameraDriver);
    }

    @Test
    public void testCam1Shot() {
        parser.process("/s1", 1229);
        verify(cameraDriver, times(1)).executeShot(eq(1229), eq(1));
        verify(sender, times(1)).getBotName();
        verifyNoMoreInteractions(sender);
        verifyNoMoreInteractions(cameraDriver);
    }

    @Test
    public void testCam1ShotWithBotName() {
        parser.process("/s1@TestBot", 1229);
        verify(cameraDriver, times(1)).executeShot(eq(1229), eq(1));
        verify(sender, times(1)).getBotName();
        verifyNoMoreInteractions(sender);
        verifyNoMoreInteractions(cameraDriver);
    }

    @Test
    public void testCam1Position() {
        parser.process("/p876 441", 1654);
        verify(cameraDriver, times(1)).callPosition(eq(1654), eq(876), eq(441));
        verify(sender, times(1)).getBotName();
        verifyNoMoreInteractions(sender);
        verifyNoMoreInteractions(cameraDriver);
    }

    @Test
    public void testCam1Enable() {
        parser.process("/enable 3", 1652);
        verify(settings).enableNotifications(eq(3));
        verify(sender, times(1)).getBotName();
        verify(sender, times(1)).sendMessage(eq(1652), eq("Enabled notifications from camera 3"));
        verifyNoMoreInteractions(sender);
        verifyNoMoreInteractions(cameraDriver);
    }

    @Test
    public void testCam1Disable() {
        parser.process("/disable 2", 1651);
        verify(settings).disableNotifications(eq(2));
        verify(sender, times(1)).getBotName();
        verify(sender, times(1)).sendMessage(eq(1651), eq("Disabled notifications from camera 2"));
        verifyNoMoreInteractions(sender);
        verifyNoMoreInteractions(cameraDriver);
    }

    @Test
    public void testRestore() {
        parser.process("/reset", 1650);
        verify(settings).restoreSettings();
        verify(sender, times(1)).getBotName();
        verify(sender, times(1)).sendMessage(eq(1650), eq("Enabled all notifications"));
        verifyNoMoreInteractions(sender);
        verifyNoMoreInteractions(cameraDriver);
    }

    @Test
    public void testBadCam1() {
        parser.process("/p1", 12334);
        verify(sender, times(1)).sendMessage(eq(12334), any());
        parser.process("/p1 NaN", 1234);
        verify(sender, times(1)).sendMessage(eq(12334), any());
        parser.process("/p1 a", 1234);
        verify(sender, times(1)).sendMessage(eq(12334), any());
        verify(sender, times(1)).getBotName();
        verifyNoMoreInteractions(cameraDriver);
        verifyNoMoreInteractions(sender);
    }

}
