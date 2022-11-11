package nissining.musicplus.music;

import nissining.musicplus.MusicPlus;

public class MusicTask extends Thread {

    private final MusicPlus m;

    public MusicTask(MusicPlus m) {
        this.m = m;
    }

    @Override
    public void run() {
        while (!m.musicAPI.musicList.isEmpty()) {
            try {
                m.musicAPI.tryPlay(m.getSongPlayers());
                sleep(15L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
