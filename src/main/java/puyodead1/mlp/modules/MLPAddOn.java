package puyodead1.mlp.modules;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.commands.Commands;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudGroup;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.PostInit;
import meteordevelopment.meteorclient.utils.misc.MeteorStarscript;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Items;
import org.meteordev.starscript.value.Value;
import org.meteordev.starscript.value.ValueMap;
import puyodead1.mlp.MLPMod;
import puyodead1.mlp.modules.commands.*;
import puyodead1.mlp.modules.hud.SocialEngineeringHud;
import puyodead1.mlp.modules.hud.Watermark;
import puyodead1.mlp.utils.MLPSystem;
import puyodead1.mlp.utils.TicketIDGenerator;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class MLPAddOn extends MeteorAddon {
    public static final Category MLP_CATEGORY = new Category("MLP", Items.TNT.getDefaultStack());
    public static final HudGroup MainHud = new HudGroup("mlp");

    @PostInit
    public static void init() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            MLPSystem.get().save(MeteorClient.FOLDER);
        }));

        // StarScript
        ValueMap ss = MeteorStarscript.ss.getGlobals().get("server").get().getMap();
        ss.set("brand", () -> Value.string((mc.getNetworkHandler() != null && mc.getNetworkHandler().getBrand() != null) ? mc.getNetworkHandler().getBrand() : "Unknown"));
        ss.set("day", () -> mc.world != null ? Value.number(Math.round((float) mc.world.getTimeOfDay() / 24000L)) : Value.string("Unknown"));
        ss.set("real_day", () -> mc.world != null ? Value.number(Math.round((float) ((mc.world.getTimeOfDay() / 24000L) / 3) / 24)) : Value.string("Unknown"));
        ss.set("ticketID", () -> Value.string(getTicketID()));
    }

    public static String getTicketID(){
        if (mc == null || mc.getNetworkHandler() == null) return "";
        String address = MinecraftClient.getInstance().getNetworkHandler().getServerInfo().address;
        if (!TicketIDGenerator.isValidIPv4WithPort(address)) return "";

        return TicketIDGenerator.generateTicketID(address);
    }

    @Override
    public String getPackage() {
        return "puyodead1.mlp";
    }

    @Override
    public void onInitialize() {
        Modules modules = Modules.get();
        modules.add(new StreamerMode());
        modules.add(new GameModeNotifier());
        modules.add(new CapeModule());
        modules.add(new Detect());

        Commands.add(new CopyIPCMD());
        Commands.add(new MLPVanityTagCMD());
        Commands.add(new TicketIDCommand());

        Hud.get().register(SocialEngineeringHud.INFO);
        Hud.get().register(Watermark.INFO);
    }

    @Override
    public void onRegisterCategories() {
        Modules.registerCategory(MLP_CATEGORY);
    }
}
