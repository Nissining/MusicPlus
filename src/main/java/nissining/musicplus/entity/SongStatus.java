package nissining.musicplus.entity;

import cn.nukkit.entity.EntityLiving;
import cn.nukkit.entity.projectile.EntityEgg;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.scheduler.Task;
import nissining.musicplus.MusicPlus;

/**
 * @author Nissining
 **/
public class SongStatus extends EntityLiving {
    public boolean canClose = false;

    public SongStatus(FullChunk fullChunk, CompoundTag compoundTag) {
        super(fullChunk, compoundTag);
    }

    @Override
    public int getNetworkId() {
        return EntityEgg.NETWORK_ID;
    }

    @Override
    protected void initEntity() {
        super.initEntity();
        setScale(0.00001f);
        setNameTagVisible();
        setNameTagAlwaysVisible();
        getServer().getScheduler().scheduleRepeatingTask(new Task() {
            @Override
            public void onRun(int i) {
                if (isClosed()) {
                    this.cancel();
                } else {
                    setNameTag(MusicPlus.ins.musicApi.songStat());
                }
            }
        },20);
    }

    @Override
    public void saveNBT() {

    }

    @Override
    public void close() {
        if (canClose) {
            super.close();
        }
    }
}
