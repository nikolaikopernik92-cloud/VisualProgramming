package kz.atu.lab09;

import java.util.LinkedHashMap;
import java.util.Set;

public final class RegistrationValidator {

    private static final String EMAIL_REGEX =
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    /** Формат идентификатора заказа: PR-1234 */
    private static final String ID_REGEX = "^PR-\\d{4}$";

    private RegistrationValidator() { }

    public static ValidationResult validate(
            String name, String email, String orderId,
            String pagesText, String copiesText,
            String format, boolean consent, Set<String> existingIds) {

        var errors = new LinkedHashMap<String, String>();

        if (name == null || name.trim().length() < 2 || name.trim().length() > 80)
            errors.put("name", "ФИО: от 2 до 80 символов");

        if (email == null || !email.trim().matches(EMAIL_REGEX))
            errors.put("email", "Введите корректный email");

        // --- индивидуальная проверка: уникальный идентификатор ---
        if (orderId == null || orderId.isBlank()) {
            errors.put("orderId", "Введите ID заказа");
        } else if (!orderId.trim().toUpperCase().matches(ID_REGEX)) {
            errors.put("orderId", "Формат ID: PR-1234");
        } else if (existingIds.contains(orderId.trim().toUpperCase())) {
            errors.put("orderId", "Заказ с таким ID уже существует");
        }

        checkInt(errors, "pages", pagesText, 1, 500,
                "Страниц: от 1 до 500", "Введите целое число страниц");
        checkInt(errors, "copies", copiesText, 1, 5,
                "Копий: от 1 до 5", "Введите целое число копий");

        if (format == null || format.isBlank())
            errors.put("format", "Выберите формат печати");

        if (!consent)
            errors.put("consent", "Подтвердите согласие");

        return new ValidationResult(errors);
    }

    private static void checkInt(LinkedHashMap<String, String> errors, String key,
                                 String text, int min, int max,
                                 String rangeMsg, String formatMsg) {
        try {
            int v = Integer.parseInt(text.trim());
            if (v < min || v > max) errors.put(key, rangeMsg);
        } catch (NumberFormatException | NullPointerException e) {
            errors.put(key, formatMsg);
        }
    }
}
