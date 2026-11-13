package kz.atu.lab11.model;

import javafx.beans.property.*;

/** Модель данных: смартфон (название, бренд, цена). */
public class Smartphone {

    private final StringProperty name = new SimpleStringProperty();
    private final StringProperty brand = new SimpleStringProperty();
    private final DoubleProperty price = new SimpleDoubleProperty();

    public Smartphone(String n, String b, double p) {
        setName(n);
        setBrand(b);
        setPrice(p);
    }

    public String getName() { return name.get(); }
    public void setName(String v) { name.set(v); }
    public StringProperty nameProperty() { return name; }

    public String getBrand() { return brand.get(); }
    public void setBrand(String v) { brand.set(v); }
    public StringProperty brandProperty() { return brand; }

    public double getPrice() { return price.get(); }
    public void setPrice(double v) { price.set(v); }
    public DoubleProperty priceProperty() { return price; }
}
