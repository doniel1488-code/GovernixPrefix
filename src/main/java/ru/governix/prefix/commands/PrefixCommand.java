package ru.governix.prefix.commands;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.NodeType;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.governix.prefix.GovernixPrefix;
import ru.governix.prefix.utils.ColorUtil;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PrefixCommand implements CommandExecutor, TabCompleter {

    private final GovernixPrefix plugin;
    private final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();

    public PrefixCommand(GovernixPrefix plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        if (args[0].equalsIgnoreCase("reset")) {
            if (args.length == 1) {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(ColorUtil.color("&cТолько для игроков."));
                    return true;
                }
                if (!player.hasPermission("governixprefix.use")) {
                    msg(sender, "no-permission");
                    return true;
                }
                setPrefix(player, null, sender);
                return true;
            }
            if (!sender.hasPermission("governixprefix.admin")) {
                msg(sender, "no-permission");
                return true;
            }
            OfflinePlayer target = getOffline(args[1]);
            if (target == null) {
                msg(sender, "player-not-found");
                return true;
            }
            setPrefix(target, null, sender);
            return true;
        }

        if (args.length >= 2 && sender.hasPermission("governixprefix.admin")) {
            OfflinePlayer target = getOffline(args[0]);
            if (target != null) {
                String prefix = String.join(" ", Arrays.copyOfRange(args, 1, args.length)).trim();
                setPrefix(target, prefix, sender);
                return true;
            }
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(ColorUtil.color("&cТолько для игроков."));
            return true;
        }

        if (!player.hasPermission("governixprefix.use")) {
            msg(sender, "no-permission");
            return true;
        }

        if (!player.hasPermission("governixprefix.cooldown.bypass")) {
            long now = System.currentTimeMillis();
            Long cd = cooldowns.get(player.getUniqueId());
            int cdSec = plugin.getConfig().getInt("cooldown-seconds", 120);
            if (cd != null && now < cd) {
                long left = (cd - now) / 1000 + 1;
                player.sendMessage(ColorUtil.color(plugin.msg("prefix") + plugin.msg("cooldown")
                        .replace("{time}", String.valueOf(left))));
                return true;
            }
        }

        String prefix = String.join(" ", args).trim();

        if (!validate(player, prefix)) return true;

        if (setPrefix(player, prefix, sender)) {
            cooldowns.put(player.getUniqueId(), System.currentTimeMillis()
                    + plugin.getConfig().getInt("cooldown-seconds", 120) * 1000L);
        }
        return true;
    }

    private boolean validate(Player player, String prefix) {
        if (prefix.isEmpty()) {
            msg(player, "empty-text");
            return false;
        }

        String stripped = ColorUtil.stripAll(prefix);
        int min = plugin.getConfig().getInt("min-length", 2);
        int max = plugin.getConfig().getInt("max-length", 24);

        boolean hasOnlyColors = stripped.isEmpty();
        if (hasOnlyColors && !plugin.getConfig().getBoolean("restrictions.allow-empty-text", false)) {
            msg(player, "empty-text");
            return false;
        }

        if (!hasOnlyColors) {
            if (stripped.length() < min) {
                player.sendMessage(ColorUtil.color(plugin.msg("prefix")
                        + plugin.msg("too-short").replace("{min}", String.valueOf(min))));
                return false;
            }
            if (stripped.length() > max) {
                player.sendMessage(ColorUtil.color(plugin.msg("prefix")
                        + plugin.msg("too-long").replace("{max}", String.valueOf(max))));
                return false;
            }
        }

        String lower = stripped.toLowerCase();
        for (String word : plugin.getConfig().getStringList("blacklist")) {
            if (lower.contains(word.toLowerCase())) {
                player.sendMessage(ColorUtil.color(plugin.msg("prefix")
                        + plugin.msg("contains-blacklist").replace("{word}", word)));
                return false;
            }
        }

        for (String ch : plugin.getConfig().getStringList("forbidden-chars")) {
            if (prefix.contains(ch)) {
                msg(player, "contains-forbidden-char");
                return false;
            }
        }

        if (ColorUtil.hasGradient(prefix) && !player.hasPermission("governixprefix.gradient")) {
            msg(player, "no-gradient-perm");
            return false;
        }
        if (ColorUtil.hasHexColors(prefix) && !player.hasPermission("governixprefix.hex")) {
            msg(player, "no-hex-perm");
            return false;
        }
        if (ColorUtil.hasLegacyColors(prefix) && !player.hasPermission("governixprefix.color")) {
            msg(player, "no-color-perm");
            return false;
        }
        if (ColorUtil.hasBold(prefix) && !player.hasPermission("governixprefix.bold")) {
            msg(player, "no-bold-perm");
            return false;
        }

        return true;
    }

    /**
     * Устанавливает (или сбрасывает) префикс через LuckPerms.
     *
     * ВАЖНО: префикс оборачивается в &r (reset) с двух сторон,
     * чтобы формат (градиент, жирный, курсив) НЕ перетекал на ник.
     */
    private boolean setPrefix(OfflinePlayer target, String prefix, CommandSender executor) {
        try {
            LuckPerms lp = LuckPermsProvider.get();
            User user = lp.getUserManager().getUser(target.getUniqueId());
            if (user == null) {
                msg(executor, "player-offline");
                return false;
            }

            user.data().clear(NodeType.PREFIX::matches);

            if (prefix != null && !prefix.isEmpty()) {
                String trimmed = prefix.trim();
                String finalPrefix = "&r" + trimmed + "&r ";

                int priority = plugin.getConfig().getInt("prefix-priority", 200);
                Node node = Node.builder("prefix." + priority + "." + finalPrefix).build();
                user.data().add(node);
            }

            lp.getUserManager().saveUser(user);

            String displayPrefix = prefix == null ? "" : prefix.trim();
            if (executor.equals(target)) {
                if (prefix == null) {
                    msg(executor, "reset-success");
                } else {
                    executor.sendMessage(ColorUtil.color(plugin.msg("prefix")
                            + plugin.msg("set-success").replace("{prefix}", displayPrefix)));
                }
            } else {
                if (prefix == null) {
                    executor.sendMessage(ColorUtil.color(plugin.msg("prefix")
                            + plugin.msg("admin-reset").replace("{player}", target.getName())));
                } else {
                    executor.sendMessage(ColorUtil.color(plugin.msg("prefix")
                            + plugin.msg("admin-set")
                            .replace("{player}", target.getName())
                            .replace("{prefix}", displayPrefix)));
                }
                if (target.isOnline() && target.getPlayer() != null) {
                    if (prefix == null) {
                        target.getPlayer().sendMessage(ColorUtil.color(plugin.msg("prefix")
                                + "&7Твой префикс сброшен администратором."));
                    } else {
                        target.getPlayer().sendMessage(ColorUtil.color(plugin.msg("prefix")
                                + plugin.msg("admin-target-notify").replace("{prefix}", displayPrefix)));
                    }
                }
            }
            return true;

        } catch (Throwable t) {
            plugin.getLogger().warning("LuckPerms error: " + t.getMessage());
            msg(executor, "luckperms-error");
            return false;
        }
    }

    private OfflinePlayer getOffline(String name) {
        Player online = Bukkit.getPlayerExact(name);
        if (online != null) return online;
        OfflinePlayer cached = Bukkit.getOfflinePlayerIfCached(name);
        if (cached != null) return cached;
        @SuppressWarnings("deprecation")
        OfflinePlayer off = Bukkit.getOfflinePlayer(name);
        if (off.hasPlayedBefore()) return off;
        return null;
    }

    private void msg(CommandSender sender, String key) {
        sender.sendMessage(ColorUtil.color(plugin.msg("prefix") + plugin.msg(key)));
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ColorUtil.color("&8&m━━━━ &bПрефикс &8&m━━━━"));
        sender.sendMessage(ColorUtil.color(plugin.msg("usage")));
        sender.sendMessage(ColorUtil.color(plugin.msg("usage-reset")));
        if (sender.hasPermission("governixprefix.admin")) {
            sender.sendMessage(ColorUtil.color(plugin.msg("usage-admin")));
            sender.sendMessage(ColorUtil.color(plugin.msg("usage-admin-reset")));
        }
        sender.sendMessage(ColorUtil.color("&8&m━━━━━━━━━━━━━━━━━"));
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (args.length == 1) {
            List<String> list = new ArrayList<>();
            list.add("reset");
            if (sender.hasPermission("governixprefix.admin")) {
                for (Player p : Bukkit.getOnlinePlayers()) list.add(p.getName());
            }
            return list.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .toList();
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("reset")
                && sender.hasPermission("governixprefix.admin")) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                    .toList();
        }
        return List.of();
    }
}
