package kz.atu.lab11.service;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import kz.atu.lab11.model.Smartphone;

import java.util.List;

/** Сервис: все бизнес-правила каталога (валидация, дубликаты, статистика). */
public class SmartphoneService {

    public static final double MAX_PRICE = 10_000_000;

    private final ObservableList<Smartphone> phones =
            FXCollections.observableArrayList();

    public ObservableList<Smartphone> getPhones() {
        return phones;
    }

    public void addPhone(String n, String b, double p) {
        validate(n, b, p);
        checkDuplicate(null, n, b);
        phones.add(new Smartphone(n.trim(), b.trim(), p));
    }

    public void updatePhone(Smartphone s, String n, String b, double p) {
        if (s == null) throw new IllegalArgumentException("Смартфон не выбран");
        validate(n, b, p);
        checkDuplicate(s, n, b);
        s.setName(n.trim());
        s.setBrand(b.trim());
        s.setPrice(p);
    }

    public void deletePhone(Smartphone s) {
        if (s == null) throw new IllegalArgumentException("Смартфон не выбран");
        phones.remove(s);
    }

    /** Средняя цена всех смартфонов (0, если каталог пуст). */
    public double averagePrice() {
        return phones.stream()
                .mapToDouble(Smartphone::getPrice)
                .average()
                .orElse(0);
    }

    /** Уникальные бренды из данных, отсортированные по алфавиту. */
    public List<String> getBrands() {
        return phones.stream()
                .map(Smartphone::getBrand)
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }

    private void checkDuplicate(Smartphone self, String n, String b) {
        boolean exists = phones.stream().anyMatch(x ->
                x != self
                        && x.getName().equalsIgnoreCase(n.trim())
                        && x.getBrand().equalsIgnoreCase(b.trim()));
        if (exists) {
            throw new IllegalArgumentException("Такой смартфон уже есть в каталоге");
        }
    }

    private void validate(String n, String b, double p) {
        if (n == null || n.isBlank())
            throw new IllegalArgumentException("Введите название модели");
        if (b == null || b.isBlank())
            throw new IllegalArgumentException("Введите бренд");
        if (!Double.isFinite(p) || p <= 0 || p > MAX_PRICE)
            throw new IllegalArgumentException(
                    "Цена должна быть больше 0 и не более " + (long) MAX_PRICE);
    }
}
