/*
 *  This file is a part of project QuickShop, the name is SubCommand_Find.java
 *  Copyright (C) Ghost_chu and contributors
 *
 *  This program is free software: you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as published by the
 *  Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *  for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.ghostchu.quickshop.command.subcommand;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.command.CommandHandler;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.permission.BuiltInShopPermission;
import com.ghostchu.quickshop.util.MsgUtil;
import com.ghostchu.quickshop.util.Util;
import io.papermc.lib.PaperLib;
import lombok.AllArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@AllArgsConstructor
public class SubCommand_Find implements CommandHandler<Player> {

    private final QuickShop plugin;

    @Override
    public void onCommand(@NotNull Player sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        if (cmdArg.length == 0) {
            plugin.text().of(sender, "command.no-type-given").send();
            return;
        }

        final Location loc = sender.getLocation().clone();
        final Vector playerVector = loc.toVector();

        //Combing command args
        final StringBuilder sb = new StringBuilder(cmdArg[0]);
        for (int i = 1; i < cmdArg.length; i++) {
            sb.append("_").append(cmdArg[i]);
        }


        final String lookFor = sb.toString().toLowerCase();
        final double maxDistance = plugin.getConfig().getInt("shop.finding.distance");
        final boolean usingOldLogic = plugin.getConfig().getBoolean("shop.finding.oldLogic");
        final int shopLimit = usingOldLogic ? 1 : plugin.getConfig().getInt("shop.finding.limit");
        final boolean allShops = plugin.getConfig().getBoolean("shop.finding.all");
        final boolean excludeOutOfStock = plugin.getConfig().getBoolean("shop.finding.exclude-out-of-stock");

        //Rewrite by Ghost_chu - Use vector to replace old chunks finding.

        Map<Shop, Double> aroundShops = new HashMap<>();

        //Choose finding source
        Collection<Shop> scanPool;
        if (allShops) {
            scanPool = plugin.getShopManager().getAllShops();
        } else {
            scanPool = plugin.getShopManager().getLoadedShops();
        }
        //Calc distance between player and shop
        for (Shop shop : scanPool) {
            if (!Objects.equals(shop.getLocation().getWorld(), loc.getWorld())) {
                continue;
            }
            if (aroundShops.size() == shopLimit) {
                break;
            }
            if (!shop.playerAuthorize(sender.getUniqueId(), BuiltInShopPermission.SEARCH)
                    || QuickShop.getPermissionManager().hasPermission(sender, "quickshop.other.search")) {
                continue;
            }
            Vector shopVector = shop.getLocation().toVector();
            double distance = shopVector.distance(playerVector);
            //Check distance
            if (distance <= maxDistance) {
                //Collect valid shop that trading items we want
                if (!ChatColor.stripColor(LegacyComponentSerializer.legacySection().serialize(Util.getItemStackName(shop.getItem()))).toLowerCase().contains(lookFor)) {
                    if (!shop.getItem().getType().name().toLowerCase().contains(lookFor)) {
                        continue;
                    }
                }
                if (excludeOutOfStock) {
                    if ((shop.isSelling() && shop.getRemainingStock() == 0) || (shop.isBuying() && shop.getRemainingSpace() == 0)) {
                        continue;
                    }
                }
                aroundShops.put(shop, distance);
            }
        }
        //Check if no shops found
        if (aroundShops.isEmpty()) {
            plugin.text().of(sender, "no-nearby-shop", lookFor).send();
            return;
        }

        //Okay now all shops is our wanted shop in Map

        List<Map.Entry<Shop, Double>> sortedShops = aroundShops.entrySet().stream().sorted(Map.Entry.<Shop, Double>comparingByValue(Double::compare).reversed()).toList();

        //Function
        if (usingOldLogic) {
            Map.Entry<Shop, Double> closest = sortedShops.get(0);
            Location lookAt = closest.getKey().getLocation().clone().add(0.5, 0.5, 0.5);
            PaperLib.teleportAsync(sender, Util.lookAt(sender.getEyeLocation(), lookAt).add(0, -1.62, 0),
                    PlayerTeleportEvent.TeleportCause.UNKNOWN);
            plugin.text().of(sender, "nearby-shop-this-way", closest.getValue().intValue()).send();
        } else {
            Component stringBuilder = plugin.text().of(sender, "nearby-shop-header", lookFor).forLocale()
                    .append(Component.newline());
            for (Map.Entry<Shop, Double> shopDoubleEntry : sortedShops) {
                Shop shop = shopDoubleEntry.getKey();
                Location location = shop.getLocation();
                //  "nearby-shop-entry": "&a- Info:{0} &aPrice:&b{1} &ax:&b{2} &ay:&b{3} &az:&b{4} &adistance: &b{5} &ablock(s)"
                stringBuilder = stringBuilder.append(plugin.text().of(sender, "nearby-shop-entry",
                        shop.getSignText(sender.getLocale()).get(1),
                        shop.getSignText(sender.getLocale()).get(3),
                        location.getBlockX(),
                        location.getBlockY(),
                        location.getBlockZ(),
                        shopDoubleEntry.getValue().intValue()
                ).forLocale()).append(Component.newline());
            }
            MsgUtil.sendDirectMessage(sender, stringBuilder.compact());
        }
    }
}
