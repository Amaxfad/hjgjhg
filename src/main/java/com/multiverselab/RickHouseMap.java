package com.multiverselab;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.LevelResource;

/**
 * Измерение "Дом Рика": мир берётся из карты, которую игрок кладёт в
 * config/multiverselab/rick_house.zip. Карта в сам мод не встроена.
 * При запуске мира файлы region/entities/poi распаковываются в папку измерения.
 */
public final class RickHouseMap {
    private RickHouseMap() {}

    /** Куда телепортировать игрока (точка на полу карты). */
    public static final double SPAWN_X = -84.5, SPAWN_Y = -28.0, SPAWN_Z = -73.3;

    private static final Pattern ENTRY = Pattern.compile("(?:^|.*/)(region|entities|poi)/([^/]+\\.mca)$");
    private static final String MARKER = ".multiverselab_map_installed";

    public static Path zipPath() {
        return FabricLoader.getInstance().getConfigDir().resolve("multiverselab").resolve("rick_house.zip");
    }

    private static Path dimDir(MinecraftServer server) {
        return DimensionType.getStorageFolder(PortalTravel.keyOf(PortalTravel.RICK_HOUSE),
                server.getWorldPath(LevelResource.ROOT));
    }

    public static boolean isInstalled(MinecraftServer server) {
        return Files.exists(dimDir(server).resolve(MARKER));
    }

    /** Распаковывает карту, если она лежит в config и ещё не установлена в этом мире. */
    public static void install(MinecraftServer server) {
        Path zip = zipPath();
        if (!Files.isRegularFile(zip)) {
            return;
        }
        Path dir = dimDir(server);
        if (Files.exists(dir.resolve(MARKER))) {
            return;
        }
        int count = 0;
        try (ZipFile zf = new ZipFile(zip.toFile())) {
            Enumeration<? extends ZipEntry> en = zf.entries();
            while (en.hasMoreElements()) {
                ZipEntry e = en.nextElement();
                if (e.isDirectory()) {
                    continue;
                }
                String name = e.getName().replace('\\', '/');
                Matcher m = ENTRY.matcher(name);
                // берём только основной мир (не DIM-1 / DIM1)
                if (!m.matches() || name.contains("DIM")) {
                    continue;
                }
                Path out = dir.resolve(m.group(1)).resolve(m.group(2));
                Files.createDirectories(out.getParent());
                try (InputStream in = zf.getInputStream(e)) {
                    Files.copy(in, out, StandardCopyOption.REPLACE_EXISTING);
                }
                count++;
            }
            if (count > 0) {
                Files.createDirectories(dir);
                Files.writeString(dir.resolve(MARKER), "installed");
            }
        } catch (IOException ex) {
            System.err.println("[multiverselab] Не удалось распаковать карту: " + ex);
        }
        System.out.println("[multiverselab] Карта Дома Рика: распаковано файлов: " + count);
    }
}
