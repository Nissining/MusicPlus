package nissining.musicplus.entity;

import cn.nukkit.entity.EntityLiving;
import cn.nukkit.entity.item.EntityMinecartEmpty;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;

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
        return EntityMinecartEmpty.NETWORK_ID;
    }

    @Override
    protected void initEntity() {
        super.initEntity();
        setScale(0.00001f);
        setNameTagVisible();
        setNameTagAlwaysVisible();
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
