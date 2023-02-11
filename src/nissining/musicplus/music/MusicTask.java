package nissining.musicplus.music;

import nissining.musicplus.MusicPlus;
import nissining.musicplus.player.MusicPlayer;

/**
 * @author Nissining
 **/
public class MusicTask extends Thread {

    private final MusicPlus m;

    public MusicTask(MusicPlus m) {
        this.m = m;
    }

    @Override
    public void run() {
        if (!m.musicApi.musicList.isEmpty()) {
            try {
                m.musicApi.tryPlay(MusicPlayer.getMps());
                sleep(15L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
