/*
    This file is part of Boyfriend
    Copyright (C) 2021  l1ttleO

    Boyfriend is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Boyfriend is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Boyfriend.  If not, see <https://www.gnu.org/licenses/>.
*/

package ru.l1ttleO.boyfriend.exceptions;

import org.jetbrains.annotations.NotNull;

import static ru.l1ttleO.boyfriend.I18n.tl;

public class NoPermissionException extends Exception {

    public NoPermissionException(final @NotNull String message) {
        super(message);
    }

    public NoPermissionException(final boolean selfInteract, final boolean userInteract) {
        super(getMessage(selfInteract, userInteract));
    }

    public static String getMessage(final boolean selfInteract, final boolean userInteract) {
        if (!selfInteract && !userInteract)
            return tl("command.no_permissions");
        return tl("interact.cant", selfInteract && userInteract ? tl("interact.us") :
                selfInteract ? tl("interact.i") : tl("interact.you"));
    }
}
