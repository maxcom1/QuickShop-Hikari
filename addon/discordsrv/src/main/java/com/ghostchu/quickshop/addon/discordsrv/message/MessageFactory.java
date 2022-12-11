package com.ghostchu.quickshop.addon.discordsrv.message;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.event.*;
import com.ghostchu.quickshop.api.localization.text.ProxiedLocale;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.util.MsgUtil;
import com.ghostchu.quickshop.util.Util;
import github.scarsz.discordsrv.dependencies.jda.api.EmbedBuilder;
import github.scarsz.discordsrv.dependencies.jda.api.entities.MessageEmbed;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.enginehub.squirrelid.Profile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class MessageFactory {
    private final QuickShop plugin;
    private final MessageManager messageManager;

    public MessageFactory(QuickShop plugin, MessageManager messageManager) {
        this.plugin = plugin;
        this.messageManager = messageManager;
    }


    @NotNull
    public MessageEmbed modShopCreated(@NotNull ShopCreateEvent event) {
        Shop shop = event.getShop();
        Map<String, String> placeHolders = applyPlaceHolders(shop, new HashMap<>());
        return messageManager.getEmbedMessage("mod-shop-created", shop.getOwner(), placeHolders);
    }

    @NotNull
    public MessageEmbed modShopRemoved(@NotNull ShopDeleteEvent event) {
        Shop shop = event.getShop();
        Map<String, String> placeHolders = applyPlaceHolders(shop, new HashMap<>());
        placeHolders.put("delete.reason", "N/A unsupported yet");
        return messageManager.getEmbedMessage("mod-remove-shop", shop.getOwner(), placeHolders);
    }

    @NotNull
    public MessageEmbed shopPurchasedSelf(@NotNull ShopSuccessPurchaseEvent event) {
        Shop shop = event.getShop();
        Map<String, String> placeHolders = applyPlaceHolders(shop, new HashMap<>());
        applyPlaceHoldersForPurchaseEvent(placeHolders, event.getPurchaser(), event);
        if (shop.isSelling()) {
            return messageManager.getEmbedMessage("bought-from-your-shop", shop.getOwner(), placeHolders);
        } else {
            return messageManager.getEmbedMessage("sold-to-your-shop", shop.getOwner(), placeHolders);
        }
    }

    @NotNull
    public MessageEmbed shopOutOfSpace(@NotNull ShopSuccessPurchaseEvent event) {
        Shop shop = event.getShop();
        Map<String, String> placeHolders = applyPlaceHolders(shop, new HashMap<>());
        applyPlaceHoldersForPurchaseEvent(placeHolders, event.getPurchaser(), event);
        return messageManager.getEmbedMessage("out-of-space", shop.getOwner(), placeHolders);
    }

    @NotNull
    public MessageEmbed shopOutOfStock(@NotNull ShopSuccessPurchaseEvent event) {
        Shop shop = event.getShop();
        Map<String, String> placeHolders = applyPlaceHolders(shop, new HashMap<>());
        applyPlaceHoldersForPurchaseEvent(placeHolders, event.getPurchaser(), event);
        return messageManager.getEmbedMessage("out-of-stock", shop.getOwner(), placeHolders);
    }

    @NotNull
    public MessageEmbed shopPurchasedMod(@NotNull ShopSuccessPurchaseEvent event) {
        Shop shop = event.getShop();
        Map<String, String> placeHolders = applyPlaceHolders(shop, new HashMap<>());
        applyPlaceHoldersForPurchaseEvent(placeHolders, event.getPurchaser(), event);
        return messageManager.getEmbedMessage("mod-shop-purchase", shop.getOwner(), placeHolders);
    }

    @NotNull
    public MessageEmbed shopTransferToYou(@NotNull ShopOwnershipTransferEvent event) {
        Shop shop = event.getShop();
        Map<String, String> placeHolders = applyPlaceHolders(shop, new HashMap<>());
        placeHolders.put("transfer.from", getPlayerName(event.getOldOwner()));
        placeHolders.put("transfer.to", getPlayerName(event.getNewOwner()));
        return messageManager.getEmbedMessage("shop-transfer-to-you", shop.getOwner(), placeHolders);
    }

    @NotNull
    public MessageEmbed modShopTransfer(@NotNull ShopOwnershipTransferEvent event) {
        Shop shop = event.getShop();
        Map<String, String> placeHolders = applyPlaceHolders(shop, new HashMap<>());
        placeHolders.put("transfer.from", getPlayerName(event.getOldOwner()));
        placeHolders.put("transfer.to", getPlayerName(event.getNewOwner()));
        return messageManager.getEmbedMessage("mod-shop-transfer", shop.getOwner(), placeHolders);
    }

    @NotNull
    public MessageEmbed priceChanged(@NotNull ShopPriceChangeEvent event) {
        Shop shop = event.getShop();
        Map<String, String> placeHolders = applyPlaceHolders(shop, new HashMap<>());
        placeHolders.put("change-price.from", String.valueOf(event.getOldPrice()));
        placeHolders.put("change-price.to", String.valueOf(event.getNewPrice()));
        return messageManager.getEmbedMessage("shop-price-changed", shop.getOwner(), placeHolders);
    }

    @NotNull
    public MessageEmbed modPriceChanged(@NotNull ShopPriceChangeEvent event) {
        Shop shop = event.getShop();
        Map<String, String> placeHolders = applyPlaceHolders(shop, new HashMap<>());
        placeHolders.put("change-price.from", String.valueOf(event.getOldPrice()));
        placeHolders.put("change-price.to", String.valueOf(event.getNewPrice()));
        return messageManager.getEmbedMessage("mod-shop-price-changed", shop.getOwner(), placeHolders);
    }

    @NotNull
    public MessageEmbed shopPermissionChanged(@NotNull ShopPlayerGroupSetEvent event) {
        Shop shop = event.getShop();
        Map<String, String> placeHolders = applyPlaceHolders(shop, new HashMap<>());
        placeHolders.put("change-permission.player", String.valueOf(event.getPlayer()));
        placeHolders.put("change-permission.from-group", event.getOldGroup());
        placeHolders.put("change-permission.to-group", event.getNewGroup());
        List<String> oldPermissions = plugin.getShopPermissionManager().getGroupPermissions(event.getOldGroup());
        List<String> newPermissions = plugin.getShopPermissionManager().getGroupPermissions(event.getNewGroup());
        newPermissions.removeAll(oldPermissions);
        StringJoiner builder = new StringJoiner("\n");
        newPermissions.forEach(builder::add);
        placeHolders.put("change-permission.perms-list", builder.toString());
        return messageManager.getEmbedMessage("shop-permission-changed", shop.getOwner(), placeHolders);
    }

    @NotNull
    private EmbedBuilder createPurchaseTemplateEmbedMessage(@NotNull Shop shop, @NotNull UUID langUser, @NotNull String playerName, @NotNull World world, int amount, double balance, double taxes) {
        EmbedBuilder builder = new EmbedBuilder();
        Map<String, String> placeHolders = new HashMap<>();
        applyPlaceHolders(shop, placeHolders, langUser);
        builder.addField(lang(langUser, "addon.discord.field.player", placeHolders), playerName, false);
        builder.addField(lang(langUser, "addon.discord.field.item", placeHolders), wrap(Util.getItemStackName(shop.getItem())), true);
        builder.addField(lang(langUser, "addon.discord.field.amount", placeHolders), String.valueOf(amount), true);
        builder.addField(lang(langUser, "addon.discord.field.balance", placeHolders), plugin.getEconomy().format(balance, world, shop.getCurrency()), true);
        if (plugin.getConfig().getBoolean("show-tax")) {
            builder.addField(lang(langUser, "addon.discord.field.taxes", placeHolders), plugin.getEconomy().format(taxes, world, shop.getCurrency()), true);
        } else {
            builder.addBlankField(true);
        }
        return builder;
    }

    @NotNull
    private Map<String, String> applyPlaceHoldersForPurchaseEvent(@NotNull Map<String, String> placeHolders, @Nullable UUID langUser, @NotNull ShopSuccessPurchaseEvent event) {
        Shop shop = event.getShop();
        placeHolders.put("purchase.uuid", event.getPurchaser().toString());
        placeHolders.put("purchase.name", getPlayerName(event.getPurchaser()));
        //noinspection DataFlowIssue
        placeHolders.put("purchase.world", shop.getLocation().getWorld().getName());
        placeHolders.put("purchase.amount", String.valueOf(event.getAmount()));
        placeHolders.put("purchase.balance", String.valueOf(event.getBalanceWithoutTax()));
        placeHolders.put("purchase.balance-formatted", plugin.getEconomy().format(event.getBalanceWithoutTax(), shop.getLocation().getWorld(), shop.getCurrency()));
        placeHolders.put("purchase.taxes", String.valueOf(event.getTax()));
        placeHolders.put("purchase.taxes-formatted", plugin.getEconomy().format(event.getTax(), shop.getLocation().getWorld(), shop.getCurrency()));
        return placeHolders;
    }

    private String wrap(@NotNull Component component) {
        return wrap(component, Collections.emptyMap());
    }

    private String wrap(@NotNull Component component, @NotNull Map<String, String> placeholders) {
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            component = component.replaceText(b -> b.matchLiteral("%%" + entry.getKey() + "%%").replacement(entry.getValue()));
        }
        return PlainTextComponentSerializer.plainText().serialize(component);
    }

    private String getPlayerName(UUID uuid) {
        String name = uuid.toString();
        Player bukkitPlayer = Bukkit.getPlayer(uuid);
        if (bukkitPlayer != null) {
            name = bukkitPlayer.getName();
        } else {
            Profile profile = plugin.getPlayerFinder().find(uuid);
            if (profile != null) {
                if (profile.getName() != null) {
                    name = profile.getName();
                }
            }
        }
        return name;
    }

    @NotNull
    private ProxiedLocale getPlayerLocale(UUID uuid) {
        Util.ensureThread(true);
        ProxiedLocale locale;
        if (uuid != null) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                locale = plugin.getTextManager().findRelativeLanguages(uuid);
            } else {
                try {
                    locale = new ProxiedLocale(plugin.getDatabaseHelper().getPlayerLocale(uuid).get(10, TimeUnit.SECONDS), MsgUtil.getDefaultGameLanguageCode());
                } catch (InterruptedException | ExecutionException | TimeoutException e) {
                    locale = MsgUtil.getDefaultGameLanguageLocale();
                }
            }
        } else {
            locale = MsgUtil.getDefaultGameLanguageLocale();
        }
        return locale;
    }

    private @NotNull Map<String, String> applyPlaceHolders(@NotNull Shop shop, @NotNull Map<String, String> map) {
        return applyPlaceHolders(shop, map, null);
    }

    private @NotNull Map<String, String> applyPlaceHolders(@NotNull Shop shop, @NotNull Map<String, String> map, @Nullable UUID langUser) {
        map.put("shop.name", ChatColor.stripColor(shop.getShopName()));
        map.put("shop.owner.name", wrap(shop.ownerName(getPlayerLocale(langUser))));
        map.put("shop.location.world", shop.getLocation().getWorld().getName());
        map.put("shop.location.x", String.valueOf(shop.getLocation().getBlockX()));
        map.put("shop.location.y", String.valueOf(shop.getLocation().getBlockY()));
        map.put("shop.location.z", String.valueOf(shop.getLocation().getBlockY()));
        map.put("shop.location.id", String.valueOf(shop.getShopId()));
        map.put("shop.item.name", wrap(Util.getItemStackName(shop.getItem())));
        map.put("shop.item.amount", String.valueOf(shop.getItem().getAmount()));
        map.put("shop.item.material", shop.getItem().getType().name());
        map.put("shop.owner.currency", shop.getCurrency());
        //map.put("shop.remaining-space", String.valueOf(shop.getRemainingSpace()));
        //map.put("shop.remaining-stock", String.valueOf(shop.getRemainingStock()));
        map.put("shop.stacking-amount", String.valueOf(shop.getShopStackingAmount()));
        map.put("shop.unlimited", String.valueOf(shop.isUnlimited()));
        if (shop.getShopName() != null) {
            map.put("shop.display-name", shop.getShopName());
        } else {
            // world, x, y, z
            map.put("shop.display-name", String.format("%s %s, %s, %s", shop.getLocation().getWorld().getName(), shop.getLocation().getBlockX(), shop.getLocation().getBlockY(), shop.getLocation().getBlockZ()));
        }
        map.put("shop.type", shop.getShopType().name());
        return map;
    }

    private Component locale(@Nullable UUID langUser, @NotNull String key) {
        return plugin.text().of(key).forLocale(getPlayerLocale(langUser).getLocale());
    }

    private String lang(@Nullable UUID langUser, @NotNull String key, @NotNull Map<String, String> placeHolders) {
        return wrap(locale(langUser, key), placeHolders);
    }
}