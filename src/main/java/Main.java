
package com.example.economy;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.configuration.file.FileConfiguration;
import java.util.HashMap;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

public class Main extends JavaPlugin {
    private HashMap<UUID, Double> cashBalances = new HashMap<>();
    private HashMap<UUID, Double> bankBalances = new HashMap<>();
    private FileConfiguration config;
    private static final double DEFAULT_CASH = 100.0;
    private static final double DEFAULT_BANK = 0.0;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        config = getConfig();
        getLogger().info("Economy plugin enabled!");
        loadBalances();
        setupScoreboard();
    }

    private void setupScoreboard() {
        getServer().getScheduler().runTaskTimer(this, () -> {
            for (Player player : getServer().getOnlinePlayers()) {
                org.bukkit.scoreboard.Scoreboard board = getServer().getScoreboardManager().getNewScoreboard();
                org.bukkit.scoreboard.Objective obj = board.registerNewObjective("money", "dummy", "§6§lPORTAFOGLIO");
                obj.setDisplaySlot(org.bukkit.scoreboard.DisplaySlot.SIDEBAR);
                
                UUID playerId = player.getUniqueId();
                double cashBalance = cashBalances.getOrDefault(playerId, DEFAULT_CASH);
                double bankBalance = bankBalances.getOrDefault(playerId, DEFAULT_BANK);
                obj.getScore("§f⚜ §aDenaro: §6$§f" + String.format("%,.2f", cashBalance)).setScore(1);
                obj.getScore("§f⚜ §aBanca: §6$§f" + String.format("%,.2f", bankBalance)).setScore(0);
                
                player.setScoreboard(board);
            }
        }, 0L, 20L);
    }

    @Override
    public void onDisable() {
        saveBalances();
        getLogger().info("Economy plugin disabled!");
    }

    private void loadBalances() {
        if (config.contains("cash")) {
            for (String key : config.getConfigurationSection("cash").getKeys(false)) {
                cashBalances.put(UUID.fromString(key), config.getDouble("cash." + key));
            }
        }
        if (config.contains("bank")) {
            for (String key : config.getConfigurationSection("bank").getKeys(false)) {
                bankBalances.put(UUID.fromString(key), config.getDouble("bank." + key));
            }
        }
    }

    private void saveBalances() {
        for (UUID id : cashBalances.keySet()) {
            config.set("cash." + id.toString(), cashBalances.get(id));
        }
        for (UUID id : bankBalances.keySet()) {
            config.set("bank." + id.toString(), bankBalances.get(id));
        }
        saveConfig();
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        switch (command.getName().toLowerCase()) {
            case "eco":
                if (args.length == 1) {
                    completions.addAll(Arrays.asList("give", "take", "set"));
                } else if (args.length == 3) {
                    completions.addAll(Arrays.asList("100", "1000", "5000", "10000"));
                }
                break;
            case "deposit":
            case "withdraw":
                if (args.length == 1) {
                    completions.addAll(Arrays.asList("10", "100", "1000", "5000", "10000"));
                }
                break;
            case "pay":
                if (args.length == 2) {
                    completions.addAll(Arrays.asList("10", "100", "1000", "5000", "10000"));
                }
                break;
        }
        
        return completions;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cSolo i giocatori possono usare questo comando!");
            return true;
        }

        Player player = (Player) sender;

        switch (command.getName().toLowerCase()) {
            case "balance":
            case "bal":
                showBalance(player);
                return true;
            case "pay":
                if (args.length != 2) {
                    player.sendMessage("§cUso: /pay <giocatore> <importo>");
                    return true;
                }
                handlePayCommand(player, args[0], args[1]);
                return true;
            case "setbalance":
                if (!player.hasPermission("economy.admin")) {
                    player.sendMessage("§cNon hai il permesso!");
                    return true;
                }
                if (args.length != 2) {
                    player.sendMessage("§cUso: /setbalance <giocatore> <importo>");
                    return true;
                }
                handleSetBalance(player, args[0], args[1]);
                return true;
            case "addmoney":
                if (!player.hasPermission("economy.admin")) {
                    player.sendMessage("§cNon hai il permesso!");
                    return true;
                }
                if (args.length != 2) {
                    player.sendMessage("§cUso: /addmoney <giocatore> <importo>");
                    return true;
                }
                handleAddMoney(player, args[0], args[1]);
                return true;
            case "removemoney":
                if (!player.hasPermission("economy.admin")) {
                    player.sendMessage("§cNon hai il permesso!");
                    return true;
                }
                if (args.length != 2) {
                    player.sendMessage("§cUso: /removemoney <giocatore> <importo>");
                    return true;
                }
                handleRemoveMoney(player, args[0], args[1]);
                return true;
            case "eco":
                if (!player.hasPermission("economy.admin")) {
                    player.sendMessage("§cNon hai il permesso!");
                    return true;
                }
                if (args.length != 3) {
                    player.sendMessage("§cUso: /eco [give|take|set] <giocatore> <importo>");
                    return true;
                }
                switch (args[0].toLowerCase()) {
                    case "give":
                        handleAddMoney(player, args[1], args[2]);
                        return true;
                    case "take":
                        handleRemoveMoney(player, args[1], args[2]);
                        return true;
                    case "set":
                        handleSetBalance(player, args[1], args[2]);
                        return true;
                    default:
                        player.sendMessage("§cComando non valido! Usa give, take o set");
                        return true;
                }
            case "withdraw":
                if (args.length != 1) {
                    player.sendMessage("§cUso: /withdraw <importo>");
                    return true;
                }
                handleWithdraw(player, args[0]);
                return true;
            case "deposit":
                if (args.length != 1) {
                    player.sendMessage("§cUso: /deposit <importo>");
                    return true;
                }
                handleDeposit(player, args[0]);
                return true;
            case "money":
                showBalance(player);
                return true;
            case "economy":
                if (!player.hasPermission("economy.admin")) {
                    player.sendMessage("§cNon hai il permesso!");
                    return true;
                }
                showEconomyInfo(player);
                return true;
        }

        return false;
    }

    private void showBalance(Player player) {
        UUID playerId = player.getUniqueId();
        double cashBalance = cashBalances.getOrDefault(playerId, DEFAULT_CASH);
        double bankBalance = bankBalances.getOrDefault(playerId, DEFAULT_BANK);
        player.sendMessage("§8§l§m-------------§r §6§lECONOMIA §8§l§m-------------");
        player.sendMessage("");
        player.sendMessage("  §7Giocatore: §e" + player.getName());
        player.sendMessage("  §7Contanti: §a$§f" + String.format("%,.2f", cashBalance));
        player.sendMessage("  §7Banca: §a$§f" + String.format("%,.2f", bankBalance));
        player.sendMessage("");
        player.sendMessage("§8§l§m----------------------------------");
    }

    private void handlePayCommand(Player sender, String targetName, String amountStr) {
        try {
            Player target = getServer().getPlayer(targetName);
            if (target == null) {
                sender.sendMessage("§cGiocatore non trovato!");
                return;
            }

            double amount = Double.parseDouble(amountStr);
            if (amount <= 0) {
                sender.sendMessage("§cL'importo deve essere positivo!");
                return;
            }

            UUID senderId = sender.getUniqueId();
            double senderCash = cashBalances.getOrDefault(senderId, DEFAULT_CASH);

            if (senderCash < amount) {
                sender.sendMessage("§cContanti insufficienti!");
                return;
            }

            UUID targetId = target.getUniqueId();
            double targetCash = cashBalances.getOrDefault(targetId, DEFAULT_CASH);

            cashBalances.put(senderId, senderCash - amount);
            cashBalances.put(targetId, targetCash + amount);
            saveBalances();

            sender.sendMessage("§aHai inviato §6$" + String.format("%.2f", amount) + " §aa §6" + target.getName());
            target.sendMessage("§aHai ricevuto §6$" + String.format("%.2f", amount) + " §ada §6" + sender.getName());

        } catch (NumberFormatException e) {
            sender.sendMessage("§cImporto non valido!");
        }
    }

    private void handleSetBalance(Player admin, String targetName, String amountStr) {
        try {
            Player target = getServer().getPlayer(targetName);
            if (target == null) {
                admin.sendMessage("§cGiocatore non trovato!");
                return;
            }

            double amount = Double.parseDouble(amountStr);
            if (amount < 0) {
                admin.sendMessage("§cL'importo non può essere negativo!");
                return;
            }

            cashBalances.put(target.getUniqueId(), amount);
            bankBalances.put(target.getUniqueId(), 0.0);
            saveBalances();

            admin.sendMessage("§8⚡ §aHai impostato il saldo di §6" + target.getName() + " §aa §6$" + String.format("%,.2f", amount));
            target.sendMessage("§8⚡ §aIl tuo saldo è stato impostato a §6$" + String.format("%,.2f", amount));
        } catch (NumberFormatException e) {
            admin.sendMessage("§cImporto non valido!");
        }
    }

    private void handleAddMoney(Player admin, String targetName, String amountStr) {
        try {
            Player target = getServer().getPlayer(targetName);
            if (target == null) {
                admin.sendMessage("§cGiocatore non trovato!");
                return;
            }

            double amount = Double.parseDouble(amountStr);
            if (amount <= 0) {
                admin.sendMessage("§cL'importo deve essere positivo!");
                return;
            }

            UUID targetId = target.getUniqueId();
            double currentCash = cashBalances.getOrDefault(targetId, DEFAULT_CASH);
            cashBalances.put(targetId, currentCash + amount);
            saveBalances();

            admin.sendMessage("§aHai aggiunto §6$" + String.format("%.2f", amount) + " §aal saldo di §6" + target.getName());
            target.sendMessage("§aSono stati aggiunti §6$" + String.format("%.2f", amount) + " §aal tuo saldo");
        } catch (NumberFormatException e) {
            admin.sendMessage("§cImporto non valido!");
        }
    }

    private void handleRemoveMoney(Player admin, String targetName, String amountStr) {
        try {
            Player target = getServer().getPlayer(targetName);
            if (target == null) {
                admin.sendMessage("§cGiocatore non trovato!");
                return;
            }

            double amount = Double.parseDouble(amountStr);
            if (amount <= 0) {
                admin.sendMessage("§cL'importo deve essere positivo!");
                return;
            }

            UUID targetId = target.getUniqueId();
            double currentCash = cashBalances.getOrDefault(targetId, DEFAULT_CASH);
            
            if (currentCash < amount) {
                admin.sendMessage("§cIl giocatore non ha abbastanza contanti!");
                return;
            }

            cashBalances.put(targetId, currentCash - amount);
            saveBalances();

            admin.sendMessage("§aHai rimosso §6$" + String.format("%.2f", amount) + " §adal saldo di §6" + target.getName());
            target.sendMessage("§aSono stati rimossi §6$" + String.format("%.2f", amount) + " §adal tuo saldo");
        } catch (NumberFormatException e) {
            admin.sendMessage("§cImporto non valido!");
        }
    }

    private void handleWithdraw(Player player, String amountStr) {
        try {
            double amount = Double.parseDouble(amountStr);
            if (amount <= 0) {
                player.sendMessage("§cL'importo deve essere positivo!");
                return;
            }

            UUID playerId = player.getUniqueId();
            double bankBalance = bankBalances.getOrDefault(playerId, DEFAULT_BANK);
            
            if (bankBalance < amount) {
                player.sendMessage("§cNon hai abbastanza soldi in banca!");
                return;
            }

            bankBalances.put(playerId, bankBalance - amount);
            double cashBalance = cashBalances.getOrDefault(playerId, DEFAULT_CASH);
            cashBalances.put(playerId, cashBalance + amount);
            saveBalances();
            sendTransactionMessage(player, "Prelievo", amount);
        } catch (NumberFormatException e) {
            player.sendMessage("§cImporto non valido!");
        }
    }

    private void handleDeposit(Player player, String amountStr) {
        try {
            double amount = Double.parseDouble(amountStr);
            if (amount <= 0) {
                player.sendMessage("§cL'importo deve essere positivo!");
                return;
            }

            UUID playerId = player.getUniqueId();
            double cashBalance = cashBalances.getOrDefault(playerId, DEFAULT_CASH);
            
            if (cashBalance < amount) {
                player.sendMessage("§cNon hai abbastanza contanti!");
                return;
            }

            cashBalances.put(playerId, cashBalance - amount);
            double bankBalance = bankBalances.getOrDefault(playerId, DEFAULT_BANK);
            bankBalances.put(playerId, bankBalance + amount);
            saveBalances();
            sendTransactionMessage(player, "Deposito", amount);
        } catch (NumberFormatException e) {
            player.sendMessage("§cImporto non valido!");
        }
    }

    private void showEconomyInfo(Player admin) {
        double totalCash = cashBalances.values().stream().mapToDouble(Double::doubleValue).sum();
        double totalBank = bankBalances.values().stream().mapToDouble(Double::doubleValue).sum();
        double totalMoney = totalCash + totalBank;
        int totalPlayers = cashBalances.size();
        double avgMoney = totalPlayers > 0 ? totalMoney / totalPlayers : 0;
        
        admin.sendMessage("§8§l§m-------------§r §6§lSTATISTICHE ECONOMIA §8§l§m-------------");
        admin.sendMessage("");
        admin.sendMessage("  §7Denaro totale: §a$§f" + String.format("%,.2f", totalMoney));
        admin.sendMessage("  §7Giocatori: §e" + totalPlayers);
        admin.sendMessage("  §7Media per giocatore: §a$§f" + String.format("%,.2f", avgMoney));
        admin.sendMessage("");
        admin.sendMessage("§8§l§m------------------------------------------------");
    }

    private void sendTransactionMessage(Player player, String type, double amount) {
        player.sendMessage("§8⚡ §6§l§nTRANSAZIONE§r §8⚡");
        player.sendMessage("");
        player.sendMessage("  §8» §7Tipo: §e" + type);
        player.sendMessage("  §8» §7Importo: §a$§f" + String.format("%,.2f", amount));
        player.sendMessage("  §8» §7Denaro: §a$§f" + String.format("%,.2f", cashBalances.get(player.getUniqueId())));
        player.sendMessage("  §8» §7Banca: §a$§f" + String.format("%,.2f", bankBalances.get(player.getUniqueId())));
        player.sendMessage("");
        player.sendMessage("§8✦ §7Transazione completata con successo §8✦");
        
        // Create receipt
        org.bukkit.inventory.ItemStack receipt = new org.bukkit.inventory.ItemStack(org.bukkit.Material.PAPER);
        org.bukkit.inventory.meta.ItemMeta meta = receipt.getItemMeta();
        
        meta.setDisplayName("§6§l✉ Ricevuta Transazione §6§l✉");
        
        java.util.List<String> lore = new java.util.ArrayList<>();
        lore.add("§8§m--------------------------------");
        lore.add("§f");
        lore.add("§e     BANCA CENTRALE DI YOUNESS");
        lore.add("§f");
        lore.add("§7Data: §f" + new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(new java.util.Date()));
        lore.add("§7Tipo: §f" + type);
        lore.add("§7Importo: §a$§f" + String.format("%,.2f", amount));
        lore.add("§f");
        lore.add("§7Saldo Contanti: §a$§f" + String.format("%,.2f", cashBalances.get(player.getUniqueId())));
        lore.add("§7Saldo Banca: §a$§f" + String.format("%,.2f", bankBalances.get(player.getUniqueId())));
        lore.add("§f");
        lore.add("§8§m--------------------------------");
        lore.add("§8✦ Transazione Autorizzata ✦");
        
        meta.setLore(lore);
        receipt.setItemMeta(meta);
        
        // Give receipt to player
        player.getInventory().addItem(receipt);
    }
}
