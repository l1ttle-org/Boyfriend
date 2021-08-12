package ru.l1ttleO.boyfriend.exceptions;

import org.jetbrains.annotations.NotNull;

public class NoPermissionException extends Exception {
    
    public NoPermissionException(final @NotNull String message) {
        super(message);
    }
    
    public NoPermissionException(final boolean selfInteract, final boolean userInteract) {
        super(getMessage(selfInteract, userInteract));
    }
    
    public static String getMessage(final boolean selfInteract, final boolean userInteract) {
        if (!selfInteract && !userInteract)
            return "У тебя недостаточно прав для использования этой команды!";
        return "У %s недостаточно прав для взаимодействия с данным пользователем!".formatted(
            selfInteract && userInteract ? "нас" :
            selfInteract ? "меня" : "тебя");
    }
}
