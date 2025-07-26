package me.termalquake.commands;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Statistic;

import java.util.*;

public class SignPlayedCommand implements CommandExecutor, TabCompleter {

    private final JavaPlugin plugin;

    public SignPlayedCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 7) {
            sender.sendMessage("§cИспользование: /signplayed <x1 y1 z1> <x2 y2 z2> <+x/+z/-x/-z>");
            return true;
        }

        World world;
        if (sender instanceof Player player) {
            world = player.getWorld();
        } else if (sender instanceof BlockCommandSender blockSender) {
            world = blockSender.getBlock().getWorld();
        } else {
            sender.sendMessage("§cЭту команду можно использовать только в игре или через командный блок.");
            return true;
        }

        try {
            Location loc1 = new Location(world,
                    Integer.parseInt(args[0]),
                    Integer.parseInt(args[1]),
                    Integer.parseInt(args[2]));

            Location loc2 = new Location(world,
                    Integer.parseInt(args[3]),
                    Integer.parseInt(args[4]),
                    Integer.parseInt(args[5]));

            String direction = args[6];

            if (!isValidDirection(direction)) {
                sender.sendMessage("§cНеверное направление. Используйте +x, -x, +z или -z.");
                return true;
            }

            List<OfflinePlayer> topPlayers = getTopPlayers(6);
            placeSigns(world, loc1, loc2, direction, topPlayers);

            sender.sendMessage("§aТаблички с временем установлены!");
        } catch (NumberFormatException e) {
            sender.sendMessage("§cКоординаты должны быть числами.");
        }

        return true;
    }

    private boolean isValidDirection(String dir) {
        return dir.equalsIgnoreCase("+x") || dir.equalsIgnoreCase("-x")
                || dir.equalsIgnoreCase("+z") || dir.equalsIgnoreCase("-z");
    }

    private List<OfflinePlayer> getTopPlayers(int count) {
        return Arrays.stream(Bukkit.getOfflinePlayers())
                .sorted((p1, p2) -> Long.compare(
                        p2.getStatistic(Statistic.PLAY_ONE_MINUTE),
                        p1.getStatistic(Statistic.PLAY_ONE_MINUTE)))
                .limit(count)
                .toList();
    }

    private void placeSigns(World world, Location l1, Location l2, String direction, List<OfflinePlayer> players) {
        int minX = Math.min(l1.getBlockX(), l2.getBlockX());
        int maxX = Math.max(l1.getBlockX(), l2.getBlockX());
        int minY = Math.min(l1.getBlockY(), l2.getBlockY());
        int maxY = Math.max(l1.getBlockY(), l2.getBlockY());
        int minZ = Math.min(l1.getBlockZ(), l2.getBlockZ());
        int maxZ = Math.max(l1.getBlockZ(), l2.getBlockZ());

        int index = 0;

        // по Y (сверху вниз), потом по Z (строки), потом по X (ячейки в строке)
        for (int y = maxY; y >= minY; y--) {
            for (int z = minZ; z <= maxZ; z++) {
                for (int x = minX; x <= maxX; x++) {
                    if (index >= players.size()) return;

                    Location signLoc = new Location(world, x, y, z);
                    Block block = signLoc.getBlock();
                    block.setType(Material.OAK_WALL_SIGN);

                    BlockFace facing = switch (direction.toLowerCase()) {
                        case "+x" -> BlockFace.EAST;
                        case "-x" -> BlockFace.WEST;
                        case "+z" -> BlockFace.SOUTH;
                        case "-z" -> BlockFace.NORTH;
                        default -> BlockFace.SOUTH;
                    };

                    org.bukkit.block.data.type.WallSign wallSignData =
                            (org.bukkit.block.data.type.WallSign) Bukkit.createBlockData(Material.OAK_WALL_SIGN);
                    wallSignData.setFacing(facing);
                    block.setBlockData(wallSignData);

                    Sign sign = (Sign) block.getState();

                    OfflinePlayer p = players.get(index++);
                    long ticks = p.getStatistic(Statistic.PLAY_ONE_MINUTE);
                    long seconds = ticks / 20;
                    long minutes = seconds / 60;
                    long hours = minutes / 60;
                    minutes = minutes % 60;

                    sign.setLine(0, "Play Time:");
                    sign.setLine(1, p.getName());
                    sign.setLine(2, String.format(ChatColor.GRAY + "%02d:%02d:%02d", hours, minutes,seconds));
                    sign.update();
                }
            }
        }
    }


    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player player)) return Collections.emptyList();

        // Получаем блок, на который смотрит игрок
        Block targetBlock = player.getTargetBlockExact(20); // радиус 20 блоков
        if (targetBlock == null) return Collections.emptyList();

        int x = targetBlock.getX();
        int y = targetBlock.getY();
        int z = targetBlock.getZ();

        return switch (args.length) {
            case 1 -> List.of(String.valueOf(x));
            case 2 -> List.of(String.valueOf(y));
            case 3 -> List.of(String.valueOf(z));
            case 4 -> List.of(String.valueOf(x));
            case 5 -> List.of(String.valueOf(y));
            case 6 -> List.of(String.valueOf(z));
            case 7 -> {
                // Получаем направление взгляда игрока
                BlockFace facing = player.getFacing();

                // Подсказка — противоположное направление (назад от взгляда)
                String direction = switch (facing) {
                    case NORTH -> "+z"; // смотрит на север — табличка сзади, значит +z
                    case SOUTH -> "-z";
                    case EAST  -> "-x";
                    case WEST  -> "+x";
                    default    -> "+z";
                };
                yield List.of(direction);
            }
            default -> Collections.emptyList();
        };
    }
}
