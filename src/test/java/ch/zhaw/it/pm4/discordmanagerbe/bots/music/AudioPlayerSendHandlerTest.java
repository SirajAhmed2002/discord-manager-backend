package ch.zhaw.it.pm4.discordmanagerbe.bots.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AudioPlayerSendHandlerTest {

    @Mock
    private AudioPlayer audioPlayer;

    @Mock
    private AudioFrame audioFrame;

    private AudioPlayerSendHandler sendHandler;

    @BeforeEach
    void setUp() {
        sendHandler = new AudioPlayerSendHandler(audioPlayer);
    }

    @Test
    void canProvide_shouldReturnTrue_whenAudioFrameIsAvailable() {
        when(audioPlayer.provide()).thenReturn(audioFrame);

        boolean result = sendHandler.canProvide();

        assertTrue(result);
        verify(audioPlayer).provide();
    }

    @Test
    void canProvide_shouldReturnFalse_whenNoAudioFrameIsAvailable() {
        when(audioPlayer.provide()).thenReturn(null);

        boolean result = sendHandler.canProvide();

        assertFalse(result);
        verify(audioPlayer).provide();
    }

    @Test
    void provide20MsAudio_shouldReturnByteBuffer_withAudioFrameData() {
        byte[] testData = new byte[]{1, 2, 3, 4};
        when(audioFrame.getData()).thenReturn(testData);

        when(audioPlayer.provide()).thenReturn(audioFrame);
        sendHandler.canProvide();

        ByteBuffer buffer = sendHandler.provide20MsAudio();

        assertNotNull(buffer);
        assertEquals(ByteBuffer.wrap(testData), buffer);
        verify(audioFrame).getData();
    }

    @Test
    void isOpus_shouldReturnTrue() {
        assertTrue(sendHandler.isOpus());
    }
}