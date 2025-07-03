package puyodead1.mlp.utils;

import meteordevelopment.meteorclient.systems.System;
import meteordevelopment.meteorclient.systems.Systems;
import net.minecraft.nbt.NbtCompound;


public class MLPSystem extends System<MLPSystem> {
    public String accessToken = "";
    public String username = "";
    public int permLevel = -1;

    public MLPSystem() {
        super("mlp-config");
    }

    public static MLPSystem get() {
        return Systems.get(MLPSystem.class);
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPermLevel(int permLevel) {
        this.permLevel = permLevel;
    }

    @Override
    public NbtCompound toTag() {
        NbtCompound tag = new NbtCompound();
        tag.putString("accessToken", accessToken);
        tag.putString("username", username);
        tag.putInt("permLevel", permLevel);

        return tag;
    }

    @Override
    public MLPSystem fromTag(NbtCompound tag) {
        accessToken = tag.getString("accessToken").get();
        username = tag.getString("username").get();
        permLevel = tag.getInt("permLevel").get();
        return this;
    }
}
