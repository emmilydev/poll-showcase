package dev.emmily.polls.config;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class Configuration extends YamlConfiguration {
  private final String fileName;
  private final Plugin plugin;
  private final File file;

  public Configuration(Plugin plugin, String filename, String fileExtension, File folder) {
    this.plugin = plugin;
    this.fileName = filename + (filename.endsWith(fileExtension) ? "" : fileExtension);
    this.file = new File(folder, this.fileName);
    this.createFile();
  }

  public Configuration(Plugin plugin, String fileName) {
    this(plugin, fileName, ".yml");
  }

  public Configuration(Plugin plugin, String fileName, String fileExtension) {
    this(plugin, fileName, fileExtension, plugin.getDataFolder());
  }

  public void reload() {
    try {
      this.load(file);
    } catch (InvalidConfigurationException | IOException e) {
      e.printStackTrace();
    }
  }

  private void createFile() {
    try {
      if (!file.exists()) {
        if (this.plugin.getResource(this.fileName) != null) {
          this.plugin.saveResource(this.fileName, false);
        } else {
          this.save(file);
        }
        this.load(file);
        return;
      }
      this.load(file);
      this.save(file);
    } catch (InvalidConfigurationException | IOException e) {
      e.printStackTrace();
    }
  }

  public void save() {
    try {
      this.save(file);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  @NotNull // fuck linter
  public String getString(String path, String def) {
    return Objects.requireNonNull(super.getString(path, def), path);
  }

  @Override
  @NotNull
  public String getString(String path) {
    return Objects.requireNonNull(super.getString(path), path);
  }
}
