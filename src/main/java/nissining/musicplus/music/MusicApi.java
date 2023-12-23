package nissining.musicplus.music;

import cn.hutool.core.util.StrUtil;
import cn.nukkit.level.Sound;
import cn.nukkit.network.protocol.PlaySoundPacket;

import cn.nukkit.utils.TextFormat;
import lombok.*;
import nissining.musicplus.MusicPlus;
import nissining.musicplus.music.utils.Layer;
import nissining.musicplus.music.utils.NBSDecoder;
import nissining.musicplus.music.utils.Song;
import nissining.musicplus.player.MusicPlayer;
import nissining.musicplus.utils.MyUtils;
import nissining.musicplus.utils.PageBean;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Nissining
 **/
@NoArgsConstructor
@Data
public class MusicApi {

    public Song nowSong = null;
    public short musicTick = -1;
    public long lastPlayed = 0;
    public int musicId = 0;

    @AllArgsConstructor
    @Getter
    public enum PlayMode {
        LIST_PLAY("列表顺序"),
        LIST_LOOP("列表循环"),
        SINGLE_LOOP("单曲循环"),
        RANDOM("随机"),
        ;
        private final String modeName;
    }

    public PlayMode playMode = PlayMode.LIST_LOOP;
    /**
     * 已加载的tracks
     */
    public ArrayList<Song> musicList = new ArrayList<>();

    public String getNowSongName() {
        return nowSong.getSongName();
    }

    public int loadAllSong(File musicPath) {
        if (!musicPath.exists()) {
            return 0;
        }
        var files = musicPath.listFiles(file ->
                !file.isDirectory() && StrUtil.endWith(file.getName(), ".nbs"));
        if (files == null || files.length == 0) {
            return 0;
        }
        musicList = Arrays.stream(files)
                .map(NBSDecoder::parse)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(ArrayList::new));
        return musicList.size();
    }

    /**
     * 初始化Music
     *
     * @param mode 0:列表顺序 1:列表循环 2:单曲循环 3:随机
     */
    public void init(PlayMode mode) {
        setPlayMode(mode);
        if (this.nowSong == null) {
            this.nextSongByMode();
        }
    }

    /**
     * 下一首
     */
    public boolean nextSong() {
        var index = musicList.indexOf(nowSong);
        var nextId = 0;
        if (index >= musicList.size() - 1) {
            // 列表顺序播放
            if (getPlayMode().equals(PlayMode.LIST_PLAY)) {
                return false;
            }
        } else {
            nextId = index + 1;
        }
        setNowSongById(nextId);
        return true;
    }

    /**
     * 上一首
     */
    public boolean lastSong() {
        var nextId = musicList.size() - 1;
        if (musicId <= 0) {
            if (getPlayMode().equals(PlayMode.LIST_PLAY)) {
                return false;
            }
        } else {
            nextId = musicId - 1;
        }
        setNowSongById(nextId);
        return true;
    }

    public void randomSong() {
        musicId = MyUtils.rand(0, musicList.size() - 1);
        setNowSongById(musicId);
    }

    /**
     * 设置音乐
     *
     * @param id 列表id
     * @return 音乐
     */
    public Song setNowSongById(int id) {
        if (musicList.isEmpty()) {
            MusicPlus.debug("播放列表为空！");
            return null;
        }
        if (id > musicList.size() - 1) {
            MusicPlus.debug("超出范围！ID: {}", id);
            return null;
        }
        resetSong();
        setMusicId(id);
        setNowSong(musicList.get(id));
        MusicPlus.debug("现在播放的是: {} BPM: {}",
                nowSong.getSongName(),
                nowSong.getSpeed());
        return getNowSong();
    }

    public void resetSong() {
        nowSong = null;
        musicTick = -1;
        lastPlayed = System.currentTimeMillis() + 3000;
    }

    /**
     * 根据播放模式进行下一首音乐
     */
    public void nextSongByMode() {
        if (musicList.isEmpty()) {
            MusicPlus.debug("播放列表为空！添加曲目再进行播放！");
            return;
        }
        switch (getPlayMode()) {
            case SINGLE_LOOP -> setNowSongById(getMusicId());
            case RANDOM -> randomSong();
            default -> nextSong();
        }
    }

    public void addMusicTick(short i) {
        setMusicTick((short) Math.min(this.musicTick + i, nowSong.getLength()));
    }

    public void tryPlay() {
        if (nowSong == null) {
            return;
        }
        if (System.currentTimeMillis() - lastPlayed < 50 * nowSong.getDelay()) {
            return;
        }
        var isFinish = musicTick > nowSong.getLength();
        // 如果是顺序播放并已播放完列表所有track
        // 停止播放
        if (isFinish && musicId >= musicList.size() && getPlayMode().equals(PlayMode.LIST_PLAY)) {
            resetSong();
            MusicPlus.debug("播放失败！原因： 模式是顺序播放！已播放完毕");
            return;
        }
        musicTick++;
        if (isFinish) {
            nextSongByMode();
            return;
        }
        playTick(musicTick);
        lastPlayed = System.currentTimeMillis();
    }

    public static final HashMap<Integer, Sound> SOUNDS = new HashMap<>() {{
        put(0, Sound.NOTE_HARP);
        put(1, Sound.NOTE_BASS);
        put(2, Sound.NOTE_BD);
        put(3, Sound.NOTE_SNARE);
        put(4, Sound.NOTE_HAT);
        put(5, Sound.NOTE_GUITAR);
        put(6, Sound.NOTE_FLUTE);
        put(7, Sound.NOTE_BELL);
        put(8, Sound.NOTE_CHIME);
        put(9, Sound.NOTE_XYLOPHONE);
    }};

    private static final HashMap<Integer, Float> KEYS = new HashMap<>() {{
        put(0, 0.5f);
        put(1, 0.529732f);
        put(2, 0.561231f);
        put(3, 0.594604f);
        put(4, 0.629961f);
        put(5, 0.667420f);
        put(6, 0.707107f);
        put(7, 0.749154f);
        put(8, 0.793701f);
        put(9, 0.840896f);
        put(10, 0.890899f);
        put(11, 0.943874f);
        put(12, 1.0f);
        put(13, 1.059463f);
        put(14, 1.122462f);
        put(15, 1.189207f);
        put(16, 1.259921f);
        put(17, 1.334840f);
        put(18, 1.414214f);
        put(19, 1.498307f);
        put(20, 1.587401f);
        put(21, 1.681793f);
        put(22, 1.781797f);
        put(23, 1.887749f);
        put(24, 2.0f);
    }};

    public void playTick(int tick) {
        for (Layer l : nowSong.getLayerHashMap().values()) {
            var note = l.getNote(tick);
            if (note == null) {
                continue;
            }
            var sound = SOUNDS.getOrDefault((int) note.getInstrument(), null);
            var fl = KEYS.getOrDefault(note.getKey() - 33, 0F);
            MusicPlayer.players.forEach((k, v) -> {
                if (v.isStopMusic()) {
                    return;
                }
                val p = v.getPlayer();
                if (sound != null) {
                    // 播放声音
                    var soundPk = new PlaySoundPacket();
                    soundPk.name = sound.getSound();
                    soundPk.volume = l.getVolume();
                    soundPk.pitch = fl;
                    soundPk.x = p.getFloorX();
                    soundPk.y = p.getFloorY();
                    soundPk.z = p.getFloorZ();
                    p.dataPacket(soundPk);
                }
            });
        }
    }

    public String songStat() {
        if (musicList.isEmpty()) {
            return TextFormat.RED + "你的播放列表是空的！";
        }
        if (nowSong == null) {
            return TextFormat.RED + "没有正在播放的音乐!";
        }

        var pageBean = new PageBean<Song>();
        var maxShow = MusicPlus.getInstance().getConfig().getInt("song_status_maxShow");
        var index = (musicList.indexOf(nowSong) / maxShow) + 1;
        var queryPager = pageBean.queryPager(index, maxShow, musicList);
        if (queryPager.isEmpty()) {
            return TextFormat.RED + "不存在的页数！";
        }

        var title = MusicPlus.ins.getConfig().getString("song_status_title");
        var sj = new StringJoiner("\n", title + "\n", "");
        sj.add(" ");

        for (int i = 0; i < maxShow; i++) {
            var songName = "&7" + (i + 1) + ".none";
            if (i < queryPager.size()) {
                songName = getNowSong().getFormatSongName(i, queryPager.get(i));
            }
            sj.add(TextFormat.colorize(songName));
        }

        // show par
        var playedTime = MyUtils.getFt((int) (musicTick / getNowSong().getSpeed()));
        var playedMaxTime = MyUtils.getFt((int) (nowSong.getLength() / getNowSong().getSpeed()));
        var playedPar = MyUtils.addPar(musicTick, nowSong.getLength(), true);

        sj.add(" ");
        sj.add(StrUtil.format("当前模式: {} | 页数: {}/{}",
                getPlayMode().getModeName(),
                index,
                pageBean.getTotalPages()));
        sj.add(playedTime + "/" + playedMaxTime + " " + playedPar);
        return sj.toString();
    }

}
