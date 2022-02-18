/*
 *  This file is a part of project QuickShop, the name is Text.java
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

package com.ghostchu.quickshop.api.localization.text;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public interface Text {
    /**
     * Getting the text that use specify locale
     *
     * @param locale The minecraft locale code (like en_us)
     * @return The text
     */
    @NotNull Component forLocale(@NotNull String locale);

    /**
     * Getting the text for player locale
     *
     * @return Getting the text for player locale
     */
    @NotNull Component forLocale();

    /**
     * Send text to the player
     */
    void send();
}