package kz.atu.lab11.service;

import kz.atu.lab11.model.Smartphone;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SmartphoneServiceTest {

    @Test
    void shouldAddPhone() {
        SmartphoneService s = new SmartphoneService();
        s.addPhone("Galaxy S24", "Samsung", 449000);
        assertEquals(1, s.getPhones().size());
    }

    @Test
    void shouldRejectBlankName() {
        SmartphoneService s = new SmartphoneService();
        assertThrows(IllegalArgumentException.class,
                () -> s.addPhone("  ", "Samsung", 100000));
    }

    @Test
    void shouldRejectInvalidPrice() {
        SmartphoneService s = new SmartphoneService();
        assertThrows(IllegalArgumentException.class,
                () -> s.addPhone("Galaxy", "Samsung", -5));
        assertThrows(IllegalArgumentException.class,
                () -> s.addPhone("Galaxy", "Samsung", 0));
        assertThrows(IllegalArgumentException.class,
                () -> s.addPhone("Galaxy", "Samsung", Double.NaN));
    }

    @Test
    void shouldRejectDuplicate() {
        SmartphoneService s = new SmartphoneService();
        s.addPhone("iPhone 15", "Apple", 520000);
        assertThrows(IllegalArgumentException.class,
                () -> s.addPhone("iphone 15", "APPLE", 500000));
    }

    @Test
    void shouldUpdatePhone() {
        SmartphoneService s = new SmartphoneService();
        s.addPhone("Redmi 12", "Xiaomi", 90000);
        Smartphone p = s.getPhones().get(0);
        s.updatePhone(p, "Redmi 13", "Xiaomi", 99000);
        assertEquals("Redmi 13", p.getName());
        assertEquals(99000, p.getPrice());
    }

    @Test
    void shouldAllowUpdateWithoutChangingName() {
        SmartphoneService s = new SmartphoneService();
        s.addPhone("Redmi 12", "Xiaomi", 90000);
        Smartphone p = s.getPhones().get(0);
        assertDoesNotThrow(() -> s.updatePhone(p, "Redmi 12", "Xiaomi", 95000));
    }

    @Test
    void shouldRejectUpdateWithoutSelection() {
        SmartphoneService s = new SmartphoneService();
        assertThrows(IllegalArgumentException.class,
                () -> s.updatePhone(null, "A", "B", 1));
    }

    @Test
    void shouldDeletePhone() {
        SmartphoneService s = new SmartphoneService();
        s.addPhone("Pixel 8", "Google", 380000);
        s.deletePhone(s.getPhones().get(0));
        assertTrue(s.getPhones().isEmpty());
    }

    @Test
    void shouldRejectDeleteWithoutSelection() {
        SmartphoneService s = new SmartphoneService();
        assertThrows(IllegalArgumentException.class, () -> s.deletePhone(null));
    }

    @Test
    void shouldCalculateAveragePrice() {
        SmartphoneService s = new SmartphoneService();
        assertEquals(0, s.averagePrice());
        s.addPhone("A", "X", 100000);
        s.addPhone("B", "Y", 300000);
        assertEquals(200000, s.averagePrice(), 0.001);
    }

    @Test
    void shouldReturnDistinctSortedBrands() {
        SmartphoneService s = new SmartphoneService();
        s.addPhone("A1", "Xiaomi", 1);
        s.addPhone("A2", "Apple", 1);
        s.addPhone("A3", "Xiaomi", 1);
        assertEquals(java.util.List.of("Apple", "Xiaomi"), s.getBrands());
    }
}
