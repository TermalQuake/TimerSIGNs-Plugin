package me.termalquake;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TimerPlugin extends JavaPlugin implements TabCompleter {

    @Override
    public void onEnable() {
        getLogger().info("TimerPlugin has been enabled");
        // Регистрируем TabCompleter для команды timer
        this.getCommand("timer").setTabCompleter(this);
    }

    @Override
    public void onDisable() {
        getLogger().info("TimerPlugin has been disabled");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Проверяем, что команда "timer" была введена
        if (command.getName().equalsIgnoreCase("timer")) {
            // Проверяем, что введено ровно 6 аргументов
            if (args.length != 6) {
                sender.sendMessage("Usage: /timer <x1> <y1> <z1> <x2> <y2> <z2>");
                return false;
            }

            try {
                // Парсим аргументы как целые числа
                int x1 = Integer.parseInt(args[0]);
                int y1 = Integer.parseInt(args[1]);
                int z1 = Integer.parseInt(args[2]);
                int x2 = Integer.parseInt(args[3]);
                int y2 = Integer.parseInt(args[4]);
                int z2 = Integer.parseInt(args[5]);

                if (sender instanceof Player) {
                    // Проверяем, что отправитель команды является игроком
                    Player player = (Player) sender;
                    // Создаем локации из полученных координат
                    Location loc1 = new Location(player.getWorld(), x1, y1, z1);
                    Location loc2 = new Location(player.getWorld(), x2, y2, z2);
                    // Вызываем метод для размещения табличек
                    placeSigns(player, loc1, loc2);
                }
            } catch (NumberFormatException e) {
                // Сообщение об ошибке, если координаты не являются целыми числами
                sender.sendMessage("Coordinates must be integers.");
                return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (command.getName().equalsIgnoreCase("timer")) {
            // Возвращаем пустой список, если аргументов больше 6
            if (args.length > 6) {
                return Collections.emptyList();
            }
            // Возвращаем список из одного пустого элемента, чтобы включить текущее значение табуляции
            return Collections.singletonList("");
        }
        return null;
    }

    // Метод для форматирования времени игры в формате чч:мм:сс
    private String formatPlayTime(long playTime) {
        long hours = playTime / 3600;
        long minutes = (playTime % 3600) / 60;
        long seconds = playTime % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    // Метод для размещения табличек с информацией о игроках и их времени игры
    private void placeSigns(Player player, Location loc1, Location loc2) {
        // Определяем минимальные и максимальные координаты в заданном диапазоне
        int minX = Math.min(loc1.getBlockX(), loc2.getBlockX());
        int minY = Math.min(loc1.getBlockY(), loc2.getBlockY());
        int minZ = Math.min(loc1.getBlockZ(), loc2.getBlockZ());
        int maxX = Math.max(loc1.getBlockX(), loc2.getBlockX());
        int maxY = Math.max(loc1.getBlockY(), loc2.getBlockY());
        int maxZ = Math.max(loc1.getBlockZ(), loc2.getBlockZ());

        // Получаем список всех игроков на сервере
        List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());

        int playerIndex = 0; // Индекс текущего игрока
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    if (playerIndex >= players.size()) {
                        return; // Выходим, если больше нет игроков для обработки
                    }

                    Player currentPlayer = players.get(playerIndex);
                    String playerName = currentPlayer.getName();
                    long playTime = currentPlayer.getStatistic(Statistic.PLAY_ONE_MINUTE) / 20;
                    String playTimeString = formatPlayTime(playTime);

                    // Получаем блок по текущим координатам и устанавливаем его тип как табличку
                    Block block = player.getWorld().getBlockAt(x, y, z);
                    block.setType(Material.OAK_WALL_SIGN);

                    // Устанавливаем текст на табличке
                    Sign sign = (Sign) block.getState();
                    sign.setLine(0, playerName);
                    sign.setLine(1, "Play Time:");
                    sign.setLine(2, playTimeString);
                    sign.update();

                    playerIndex++; // Переходим к следующему игроку
                }
            }
        }
    }
}
