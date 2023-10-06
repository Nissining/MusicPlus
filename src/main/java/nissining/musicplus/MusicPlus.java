package nissining.musicplus;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.StrUtil;
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
import nissining.musicplus.entity.SongStatus;
import nissining.musicplus.music.MusicApi;
import nissining.musicplus.music.utils.Song;
import nissining.musicplus.player.MusicPlayer;
import nissining.musicplus.player.MusicPlayerMenu;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

/**
 * @author Nissining
 **/
public class MusicPlus extends PluginBase implements Listener {

    private Config config;
    public MusicApi musicApi = new MusicApi();
    public ArrayList<String> musicWorlds;

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
        config = getConfig();
        musicWorlds = new ArrayList<>(config.getStringList("music_worlds"));
        creSongStatus();
        var timer = DateUtil.timer();
        debug("正在加载track...");
        getServer().getScheduler().scheduleAsyncTask(new AsyncTask() {
            @Override
            public void onRun() {
                var i = musicApi.loadAllSong(new File(getDataFolder(), "music"));
                musicApi.init(MusicApi.PlayMode.values()[config.getInt("play_mode")]);
                while (!isDisabled()) {
                    ThreadUtil.sleep(musicApi.getNowSong().getDelay());
                    musicApi.tryPlay(MusicPlayer.getMps());
                }
                debug(StrUtil.format("已加载{}首Track！用时：{}ms", i, timer.interval()));
            }
        });
        getServer().getPluginManager().registerEvents(this, this);
    }

    public boolean isInMusicWorld(Player player) {
        if (musicWorlds.isEmpty()) {
            return true;
        }
        return musicWorlds.contains(player.getLevelName());
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

    public static final StringJoiner HELP_LIST = new StringJoiner("\n- ",
            "--- MusicPlus HelpList ---", "Plugin by Nissining")
            .add("")
            .add("/mplus <args> - 主要命令")
            .add("")
            .add("-------- args --------")
            .add("spawn - 在脚下创建SongStatus（高度默认为：3）")
            .add("next - 下一首")
            .add("last - 上一首")
            .add("play <id> - 指定播放")
            .add("mode <1|2|3> - 切换模式(1=列表循环播放 2=列表顺序播放 3=随机播放)")
            .add("my - 打开个人设置")
            .add("add - 快进15s")
            .add("")
            .add("reload - 热重载配置")
            .add("reloadSong - 重载音乐列表")
            .add("----------------------");

    @Override
    public boolean onCommand(CommandSender se, Command command, String s, String[] args) {
        String t = "";
        if (args.length >= 1) {
            if (isOpLabel(args[0]) && !se.isOp()) {
                t = "需要OP权限";
            } else {
                switch (args[0]) {
                    case "help" -> t = HELP_LIST.toString();
                    case "spawn" -> {
                        if (se instanceof Player p) {
                            List<Double> pos = new ArrayList<>() {{
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
                            Song song = musicApi.setNowSongById(Integer.parseInt(args[1]));
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
                            var input = 0;
                            try {
                                input = Integer.parseInt(args[1]);
                            } catch (NumberFormatException e) {
                                t = "填写模式需要是数字！";
                                e.printStackTrace();
                            }
                            musicApi.setPlayMode(MusicApi.PlayMode.values()[input]);
                        }
                    }
                    case "reload" -> {
                        this.config = new Config(getDataFolder() + "/config.yml", 2);
                        t = "config reloaded!";
                    }
                    case "reloadSong" -> {
                        musicApi.loadAllSong(new File(getDataFolder(), "music"));
                        musicApi.init(MusicApi.PlayMode.values()[config.getInt("play_mode")]);
                        t = "已重载音乐列表！";
                    }
                    case "my" -> MusicPlayerMenu.openMenu((Player) se);
                    case "add" -> {
                        musicApi.addMusicTick((short) 15);
                        t = "快进15s";
                    }
                    case "addI" -> musicApi.addMusicTick(Short.parseShort(args[1]));
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

    public static void debug(String s, Object... objects) {
        getInstance().getLogger().warning(StrUtil.format(s, objects));
    }

}
