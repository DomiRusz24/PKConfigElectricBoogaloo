package me.domirusz24.pkconfigapi2;

import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import org.bukkit.configuration.file.FileConfiguration;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public final class PKConfigAPI {
    private static final ConfigIO<Object> DEFAULT_IO = new ConfigIO<Object>() {
        @Override
        public Object get(FileConfiguration config, String path) {
            return config.get(path);
        }

        @Override
        public void set(FileConfiguration config, String path, Object value) {
            config.set(path, value);
        }

        @Override
        public Class<Object> typeClass() {
            return Object.class;
        }
    };

    private static void forEachField(CoreAbility ability, FieldAction action) {
        for (Field field : ability.getClass().getDeclaredFields()) {
            if (Modifier.isFinal(field.getModifiers()) || Modifier.isStatic(field.getModifiers())) return;
            if (field.getDeclaredAnnotation(ConfigValue.class) == null) continue;
            field.setAccessible(true);

            String pathName = field.getDeclaredAnnotation(ConfigValue.class).value();
            if (pathName.equals("")) pathName = field.getName();

            pathName = "ProjectKorra.Ability." + pathName;

            action.run(pathName, field);
        }
    }

    private static ConfigIO<?> getConfigIOFor(Class<?> clazz, ConfigIO<?>... configIOList) {
        for (ConfigIO<?> configIO : configIOList) {
            if (configIO.typeClass().equals(clazz)) {
                return configIO;
            }
        }

        return DEFAULT_IO;
    }

    public static void addDefaults(CoreAbility ability, ConfigIO<?>... configIOList) {
        if (ability.getPlayer() != null) return;

        FileConfiguration config = ConfigManager.getConfig();

        forEachField(ability, (path, field) -> {
            try {
                getConfigIOFor(field.getType(), configIOList).set(config, path, field.get(ability));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        });
    }

    public static void loadValues(CoreAbility ability, ConfigIO<?>... configIOList) {
        if (ability.getPlayer() == null) return;

        FileConfiguration config = ConfigManager.getConfig();

        forEachField(ability, (path, field) -> {
            try {
                field.set(ability, getConfigIOFor(field.getType(), configIOList).get(config, path));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        });

    }

    private interface FieldAction {
        void run(String path, Field field);
    }
}
