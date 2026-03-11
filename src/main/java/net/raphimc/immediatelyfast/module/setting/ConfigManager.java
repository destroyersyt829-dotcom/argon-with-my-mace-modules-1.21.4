package net.raphimc.immediatelyfast.module;

import com.google.gson.*;
import net.raphimc.immediatelyfast.module.setting.BooleanSetting;
import net.raphimc.immediatelyfast.module.setting.NumberSetting;
import net.raphimc.immediatelyfast.module.setting.Setting;

import java.io.*;
import java.nio.file.*;
import java.util.List;

public class ConfigManager {

    private static final Path CONFIG_FILE = Paths.get("config", "settings.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void save(List<Module> modules) {
        try {
            Files.createDirectories(CONFIG_FILE.getParent());

            JsonObject root = new JsonObject();

            for (Module module : modules) {
                JsonObject moduleObj = new JsonObject();
                moduleObj.addProperty("enabled", module.isEnabled());

                JsonObject settingsObj = new JsonObject();
                for (Setting<?> setting : module.getSettings()) {
                    String name = setting.getName().toString();
                    if (setting instanceof BooleanSetting bs) {
                        settingsObj.addProperty(name, bs.getValue());
                    } else if (setting instanceof NumberSetting ns) {
                        settingsObj.addProperty(name, ns.getValue());
                    }
                }
                moduleObj.add("settings", settingsObj);
                root.add(module.getName().toString(), moduleObj);
            }

            Files.writeString(CONFIG_FILE, GSON.toJson(root));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void load(List<Module> modules) {
        if (!Files.exists(CONFIG_FILE)) return;

        try {
            String content = Files.readString(CONFIG_FILE);
            JsonObject root = JsonParser.parseString(content).getAsJsonObject();

            for (Module module : modules) {
                String moduleName = module.getName().toString();
                if (!root.has(moduleName)) continue;

                JsonObject moduleObj = root.getAsJsonObject(moduleName);

                // Restore enabled state
                if (moduleObj.has("enabled")) {
                    boolean shouldBeEnabled = moduleObj.get("enabled").getAsBoolean();
                    if (shouldBeEnabled != module.isEnabled()) {
                        module.toggle();
                    }
                }

                // Restore settings
                if (moduleObj.has("settings")) {
                    JsonObject settingsObj = moduleObj.getAsJsonObject("settings");
                    for (Setting<?> setting : module.getSettings()) {
                        String name = setting.getName().toString();
                        if (!settingsObj.has(name)) continue;

                        if (setting instanceof BooleanSetting bs) {
                            bs.setValue(settingsObj.get(name).getAsBoolean());
                        } else if (setting instanceof NumberSetting ns) {
                            ns.setValue(settingsObj.get(name).getAsDouble());
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
  }
