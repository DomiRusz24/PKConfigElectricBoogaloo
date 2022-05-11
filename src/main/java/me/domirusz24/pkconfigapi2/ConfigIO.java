package me.domirusz24.pkconfigapi2;

import org.bukkit.configuration.file.FileConfiguration;

public interface ConfigIO<T> {
    T get(FileConfiguration config, String path);
    void set(FileConfiguration config, String path, Object value);

    Class<T> typeClass();
}
