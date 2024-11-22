package com.example.demo.events;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class Sound {
    public static void playSound() {
        try {
            File audioFile = new File("Q:\\info\\java\\lab3\\demo\\src\\main\\resources\\sound\\notification.wav");
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            clip.start();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }
}
