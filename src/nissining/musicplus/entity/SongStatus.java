package nissining.musicplus.entity;

import cn.nukkit.entity.EntityLiving;
import cn.nukkit.entity.projectile.EntitySnowball;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;
import nissining.musicplus.MusicPlus;

/**
 * @author Nissining
 **/
public class SongStatus extends EntityLiving {

    private int tick = 20;

    public SongStatus(FullChunk fullChunk, CompoundTag compoundTag) {
        super(fullChunk, compoundTag);
    }

    @Override
    public int getNetworkId() {
        return EntitySnowball.NETWORK_ID;
    }

    @Override
    protected void initEntity() {
        super.initEntity();
        setScale(0.001f);
        setNameTagVisible();
        setNameTagAlwaysVisible();
    }

    @Override
    public void saveNBT() {

    }

    @Override
    public boolean onUpdate(int i) {
        boolean onUpdate = super.onUpdate(i);
        // update song status
        if (tick > 0) {
            tick--;
            if (tick < 1) {
                tick = 20;
                setNameTag(MusicPlus.ins.musicApi.songStat());
            }
        }
        return onUpdate;
    }

}
