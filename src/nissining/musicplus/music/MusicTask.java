package nissining.musicplus.music;

import nissining.musicplus.MusicPlus;
import nissining.musicplus.player.MusicPlayer;

/**
 * @author Nissining
 **/
public class MusicTask implements Runnable {

    private final MusicPlus m;

    public MusicTask(MusicPlus m) {
        this.m = m;
    }

    @Override
    public void run() {
        if (!m.musicApi.musicList.isEmpty()) {
            m.musicApi.tryPlay(MusicPlayer.getMps());
        }
    }

}
