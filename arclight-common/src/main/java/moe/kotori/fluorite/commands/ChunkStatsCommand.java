package moe.kotori.fluorite.commands;

import com.google.common.collect.Lists;
import io.izzel.arclight.common.bridge.core.world.WorldBridge;
import io.izzel.arclight.common.bridge.core.world.server.ServerWorldBridge;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChunkStatsCommand extends Command {
    private static Map<LevelChunk, Long> chunks = new HashMap<>();
    private static boolean enable = false;
    private static long lastNanoTime = 0;
    private static int totalTick = 0;

    public ChunkStatsCommand(String name) {
        super(name);
        this.description = "Chunk Stats Command";
        this.usageMessage = "/chunkstats start/stop";
        setPermission("fluorite.command.chunkstats");
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (!testPermission(sender)) return true;
        if (args.length != 1) {
            sender.sendMessage(ChatColor.RED + "Usage: " + usageMessage);
            return false;
        }
        if (args[0].equals("start")) {
            if (!enable) {
                chunks = new HashMap<>();
                lastNanoTime = 0;
                totalTick = 0;
                enable = true;

                sender.sendMessage("Chunk stats started.");
            } else {
                sender.sendMessage("Already running!");
            }

            return true;
        } else if (args[0].equals("stop")) {
            if (enable) {
                enable = false;
                sender.sendMessage("Please wait for analyzing..");

                List<ChunkTime> chunkList = new ArrayList<>();
                for (int i = 0; i < 10 ; i++) {
                    LevelChunk hight = null;
                    long t = 0;
                    for (LevelChunk chunkPos : chunks.keySet()) {
                        if (hight == null) {
                            hight = chunkPos;
                            t = chunks.get(chunkPos);
                            continue;
                        }
                        long tt = chunks.get(chunkPos);
                        if (tt > t) {
                            hight = chunkPos;
                            t = tt;
                        }
                    }
                    chunkList.add(new ChunkTime(hight, t));
                    chunks.remove(hight);
                }
                chunks.clear();

                sender.sendMessage("Chunks Time:");
                for (ChunkTime chunkTime : chunkList) {
                    int chunkX = chunkTime.chunk.getPos().x;
                    int chunkZ = chunkTime.chunk.getPos().z;
                    int posX = chunkX << 4;
                    int posZ = chunkZ << 4;
                    int time = (int) (chunkTime.time / 1000 / 1000);
                    int avg = totalTick > 0 ? time / totalTick : 0;

                    TextComponent component = new TextComponent(String.format("[%s: %d,%d at chunk %d,%d] has running time: %d ms (Arg %d ms/tick)", ((WorldBridge) chunkTime.chunk.level).bridge$getWorld().getName(), posX, posZ, chunkX, chunkZ, time, avg));
                    component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, String.format("/minecraft:tp %d 128 %d", posX, posZ)));
                    component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent[]{new TextComponent(String.format("Execute command: /minecraft:tp %d 128 %d", posX, posZ))}));
                    sender.spigot().sendMessage(component);
                }
            } else {
                sender.sendMessage("Not start!");
            }

            return true;
        }

        sender.sendMessage(ChatColor.RED + "Usage: " + usageMessage);
        return false;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) throws IllegalArgumentException {
        List<String> params = Lists.newArrayList(Arrays.asList("start", "stop"));

        List<String> tabs = Lists.newArrayList();

        if (args.length == 1) {
            String arg = args[0].toLowerCase();
            for (String s : params) {
                if (s.toLowerCase().startsWith(arg)) {
                    tabs.add(s);
                }
            }
        }

        return tabs;
    }

    public static void onServerTick() {
        if (!enable) return;
        totalTick++;
    }

    public static void onTickStart() {
        if (!enable) return;
        lastNanoTime = System.nanoTime();
    }

    public static void onTickEnd(Level world, BlockPos pos) {
        if (!enable) return;

        LevelChunk chunk = ((ServerWorldBridge) world).bridge$getChunkIfLoaded(pos.getX() >> 4, pos.getZ() >> 4);

        if (chunk != null && lastNanoTime > 0) {
            Long oldTime = chunks.getOrDefault(chunk, 0L);
            oldTime += System.nanoTime() - lastNanoTime;
            chunks.put(chunk, oldTime);
        }
    }

    record ChunkTime(LevelChunk chunk, long time) {
    }
}
