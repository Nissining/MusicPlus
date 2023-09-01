package nissining.musicplus;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.entity.Entity;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerJoinEvent;
import cn.nukkit.event.player.PlayerQuitEvent;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.scheduler.AsyncTask;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.ConfigSection;
import nissining.musicplus.entity.SongStatus;
import nissining.musicplus.music.MusicApi;
import nissining.musicplus.music.MusicTask;
import nissining.musicplus.player.MusicPlayer;
import nissining.musicplus.player.MusicPlayerMenu;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;

/**
 * @author Nissining
 **/
public class MusicPlus extends PluginBase implements Listener {

    private Config config;
    public static final ScheduledThreadPoolExecutor SCHEDULED = new ScheduledThreadPoolExecutor(10);
    public MusicApi musicApi = new MusicApi();
    public List<String> musicWorlds;

    public static MusicPlus ins;

    public static MusicPlus getInstance() {
        return ins;
    }

    @Override
    public void onLoad() {
        ins = this;
    }

    @Override
    public void onEnable() {
        if (getDataFolder().mkdirs()) {
            debug("folder created");
        }
        boolean music = new File(getDataFolder(), "music").mkdirs();
        if (music) {
            debug("music folder created");
        }

        saveResource("config.yml");
        config = new Config(getDataFolder() + "/config.yml", 2, new ConfigSection() {{
            put("music_worlds", new ArrayList<String>());
            put("song_status_pos", new ArrayList<Double>());
            put("song_status_level", "");
            put("song_status_title", "--- MusicPlus MusicList ---");
            put("song_status_maxShow", 10);
            put("play_mode", 3);
        }});
        musicWorlds = config.getStringList("music_worlds");

        creSongStatus();

        debug("正在加载track...");
        var timer = DateUtil.timer();
        getServer().getScheduler().scheduleAsyncTask(new AsyncTask() {
            @Override
            public void onRun() {
                var i = musicApi.loadAllSong(new File(getDataFolder(), "music"));
                debug(String.format("track加载完毕！共计%d首track！用时：%dms", i, timer.interval()));
                musicApi.init(config.getInt("play_mode"));
                startPlay();
            }
        });
        getServer().getPluginManager().registerEvents(this, this);
    }

    public boolean isInMusicWorld(Player player) {
        if (musicWorlds.isEmpty()) {
            return true;
        }
        return musicWorlds.stream().anyMatch(worldName ->
                player.level.getFolderName().equals(worldName));
    }

    private void startPlay() {
        MusicTask musicTask = new MusicTask(this);
        SCHEDULED.scheduleWithFixedDelay(musicTask, 0, 15, TimeUnit.MILLISECONDS);
        debug("MusicPlus已开始播放 :-)");
    }

    private void creSongStatus() {
        List<Double> pos = config.getDoubleList("song_status_pos");
        Level level = getServer().getLevelByName(config.getString("song_status_level"));
        if (pos.isEmpty() || level == null) {
            return;
        }
        Position position = new Position(pos.get(0), pos.get(1) + 3, pos.get(2), level);
        CompoundTag nbt = Entity.getDefaultNBT(position);
        SongStatus songStatus = new SongStatus(position.getChunk(), nbt);
        songStatus.spawnToAll();
    }

    private static final String[] OP_LABEL = new String[]{
            "spawn", "next", "last", "play", "mode", "add", "reload", "reloadSong"
    };

    private boolean isOpLabel(String label) {
        return List.of(OP_LABEL).contains(label);
    }

    public static final ArrayList<String> HELP_LIST = CollUtil.newArrayList(
            "--- MusicPlus 帮助列表 ---",
            "/mplus <args> - 主指令",
            " ",
            "args:",
            "  spawn - 创建播放状态栏",
            "  next - 切换下一首",
            "  last - 切换上一首",
            "  play <id> - 播放指定Track",
            "  play <id> - 播放指定Track",
            "  play <id> - 播放指定Track",
            "  mode <1|2|3> - 切换模式(1=列表循环播放 2=列表顺序播放 3=随机播放)",
            "  my - 打开个人设置",
            "  add - 快进15s",
            "  reload - 热重载配置",
            "  reloadSong - 重载音乐列表",
            "  clear - 清除当前世界所有播放状态栏",
            " ",
            "Plugin by Nissining"
    );

    @Override
    public boolean onCommand(CommandSender se, Command command, String s, String[] args) {
        String t = "";
        if (args.length >= 1) {

            if (isOpLabel(args[0]) && !se.isOp()) {
                t = "需要OP权限";

            } else {
                switch (args[0]) {
                    case "help" -> t = String.join("\n", HELP_LIST);
                    case "spawn" -> {
                        if (se instanceof Player p) {
                            var pos = new ArrayList<>() {{
                                add(p.getX());
                                add(p.getY());
                                add(p.getZ());
                            }};
                            config.set("song_status_pos", pos);
                            config.set("song_status_level", p.level.getFolderName());
                            config.save();
                            creSongStatus();
                            t = "Song status created and saved!";
                        }
                    }
                    // mplus play <page> <0-9>
                    case "play" -> {
                        if (args.length == 2) {
                            var song = musicApi.setNowSongById(Integer.parseInt(args[1]));
                            if (song == null) {
                                t = "播放失败！Song不存在！";
                            } else {
                                t = "已切换为Song: " + song.getSongName();
                            }
                        }
                    }
                    case "next" -> {
                        if (musicApi.nextSong()) {
                            t = "已切换到下一首！ " + musicApi.getNowSongName();
                        } else {
                            t = "下一首失败！可能是列表顺序播放导致！";
                        }
                    }
                    case "last" -> {
                        if (musicApi.lastSong()) {
                            t = "已切换到上一首！ " + musicApi.getNowSongName();
                        } else {
                            t = "上一首失败！可能是列表顺序播放导致！";
                        }
                    }
                    case "mode" -> {
                        if (args.length == 2) {
                            t = "播放模式切换为：" + musicApi.setPlayMode(Integer.parseInt(args[1]));
                        }
                    }
                    case "reload" -> {
                        this.config = new Config(getDataFolder() + "/config.yml", 2);
                        t = "config reloaded!";
                    }
                    case "reloadSong" -> {
                        musicApi.loadAllSong(new File(getDataFolder(), "music"));
                        musicApi.init(config.getInt("play_mode"));
                        t = "已重载音乐列表！";
                    }
                    case "my" -> MusicPlayerMenu.openMenu((Player) se);
                    case "add" -> {
                        musicApi.addMusicTick((short) 15);
                        t = "快进15s";
                    }
                    case "addI" -> musicApi.addMusicTick(Short.parseShort(args[1]));
                    case "clear" -> {
                        if (se instanceof Player p) {
                            clearSongStatus(p.getLevel());
                        }
                    }
                }
            }

        } else {
            t = "Usage: /mplus help";
        }

        if (!t.isEmpty()) {
            se.sendMessage(t);
        }

        return true;
    }

    private void clearSongStatus(Level level) {
        Objects.requireNonNull(level);
        for (Entity entity : level.getEntities()) {
            if (entity instanceof SongStatus songStatus) {
                songStatus.invalidClose = true;
                songStatus.close();
            }
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        getServer().getScheduler().scheduleAsyncTask(new AsyncTask() {
            @Override
            public void onRun() {
                if (isInMusicWorld(player)) {
                    MusicPlayer.addMusicPlayer(player);
                }
            }
        });
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        getServer().getScheduler().scheduleAsyncTask(new AsyncTask() {
            @Override
            public void onRun() {
                MusicPlayer.removeMusicPlayer(event.getPlayer());
            }
        });
    }

    public static void debug(String debug) {
        getInstance().getLogger().notice(debug);
    }
}
