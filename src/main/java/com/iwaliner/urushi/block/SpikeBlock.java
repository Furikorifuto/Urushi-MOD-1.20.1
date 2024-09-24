package com.iwaliner.urushi.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SpikeBlock extends Block {
    public static final IntegerProperty LEVEL = BlockStateProperties.LEVEL;
    public static final BooleanProperty INVERTED = BlockStateProperties.INVERTED;
    public SpikeBlock(Properties p_49795_) {
        super(p_49795_);
        this.registerDefaultState(this.stateDefinition.any().setValue(LEVEL, 0).setValue(INVERTED,false));
    }

    @Override
    public boolean isPathfindable(BlockState p_60475_, BlockGetter p_60476_, BlockPos p_60477_, PathComputationType p_60478_) {
        return true;
    }
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LEVEL,INVERTED);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity livingEntity, ItemStack stack) {
        if(state.getBlock() instanceof SpikeBlock&&(state.getValue(LEVEL)!=0||state.getValue(INVERTED))){
            level.setBlockAndUpdate(pos,this.defaultBlockState());
        }
        level.scheduleTick(new BlockPos(pos), this, 1);
    }
    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int spikeProcess=state.getValue(LEVEL);
        if(!state.getValue(INVERTED)) {
            if (spikeProcess < 15) {
                level.scheduleTick(pos, this, spikeProcess == 14 ? 20 : 1);
                level.setBlockAndUpdate(pos, state.setValue(LEVEL, 1 + spikeProcess));
            } else {
                level.scheduleTick(pos, this,  1);
                level.setBlockAndUpdate(pos, state.setValue(LEVEL, 14).setValue(INVERTED,true));
            }
        }else{
            if(spikeProcess>0){
                level.scheduleTick(pos, this,  1);
                level.setBlockAndUpdate(pos, state.setValue(LEVEL, spikeProcess-1).setValue(INVERTED,true));
            }else{
                level.setBlockAndUpdate(pos,Blocks.AIR.defaultBlockState());
            }
        }
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        context.getLevel().scheduleTick(context.getClickedPos(), this, 1);
        return this.defaultBlockState();
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState state2, boolean b) {
        level.scheduleTick(pos, this, 1);
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {

        if(entity instanceof LivingEntity){
            AABB axisalignedbb =entity.getBoundingBox() .inflate(25.0D, 25.0D, 25.0D);
            List<Player> list = level.getEntitiesOfClass(Player.class, axisalignedbb);
            Player player=null;
            if(!list.isEmpty()) {
                for (Player p : list) {
                    player=p;
                    break;
                }
            }
            LivingEntity living= (LivingEntity) entity;
            if(player!=null) {
                living.hurt(living.damageSources().playerAttack(player), 3F);
            }else{
                living.hurt(living.damageSources().generic(), 3F);
            }

        }
    }

}