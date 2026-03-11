package net.raphimc.immediatelyfast;

import net.raphimc.immediatelyfast.event.EventManager;
import net.raphimc.immediatelyfast.gui.ClickGui;
import net.raphimc.immediatelyfast.managers.FriendManager;
import net.raphimc.immediatelyfast.module.ModuleManager;
import net.raphimc.immediatelyfast.managers.ProfileManager;
import net.raphimc.immediatelyfast.utils.rotation.RotatorManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;

import java.io.File;
import java.io.IOException;
import java.net.*;

@SuppressWarnings("all")
public final class Argon {
    public RotatorManager rotatorManager;
    public ProfileManager profileManager;
    public ModuleManager moduleManager;
    public EventManager eventManager;
    public FriendManager friendManager;
    public static MinecraftClient mc;
    public String version = " b1.3";
    public static boolean BETA; //this was for beta kids but ablue never
    public static Argon INSTANCE;
    public boolean guiInitialized;
    public ClickGui clickGui;
    public Screen previousScreen = null;
    public long lastModified;
    public File argonJar;

    public Argon() throws InterruptedException, IOException {
        INSTANCE = this;
        this.eventManager = new EventManager();
        this.moduleManager = new ModuleManager();
        this.clickGui = new ClickGui();
        this.rotatorManager = new RotatorManager();
        this.profileManager = new ProfileManager();
        this.friendManager = new FriendManager();

        this.getProfileManager().loadProfile();
        this.setLastModified();

        this.guiInitialized = false;
        mc = MinecraftClient.getInstance();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            this.moduleManager.save();
        }));
    }

    public ProfileManager getProfileManager() {
        return profileManager;
    }

    public ModuleManager getModuleManager() {
        return moduleManager;
    }

    public FriendManager getFriendManager() {
        return friendManager;
    }

    public EventManager getEventManager() {
        return eventManager;
    }

    public ClickGui getClickGui() {
        return clickGui;
    }

    public void resetModifiedDate() {
        this.argonJar.setLastModified(lastModified);
    }

    public String getVersion() {
        return version;
    }

    public void setLastModified() {
        try {
            this.argonJar = new File(Argon.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            // Comment out when debugging
            this.lastModified = argonJar.lastModified();
        } catch (URISyntaxException ignored) {}
    }
}
