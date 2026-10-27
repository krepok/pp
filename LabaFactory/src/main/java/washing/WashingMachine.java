package washing;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

abstract class Entity {
    private int id;

    public Entity(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}

public class WashingMachine extends Entity {
    private String type;
    private String model;
    private double capacity;
    private int spinSpeed;
    private Date releaseDate;
    private double price;

    public WashingMachine() {
        super(0);
    }

    public WashingMachine(int id, String type, String model, double capacity, int spinSpeed, Date releaseDate, double price) {
        super(id);
        this.type = type;
        this.model = model;
        this.capacity = capacity;
        this.spinSpeed = spinSpeed;
        this.releaseDate = releaseDate;
        this.price = price;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public double getCapacity() {
        return capacity;
    }

    public void setCapacity(double capacity) {
        this.capacity = capacity;
    }

    public int getSpinSpeed() {
        return spinSpeed;
    }

    public void setSpinSpeed(int spinSpeed) {
        this.spinSpeed = spinSpeed;
    }

    public Date getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(Date releaseDate) {
        this.releaseDate = releaseDate;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public static WashingMachine parse(String line) {
        String[] p = line.split(";", -1);
        if (p.length != 7) {
            throw new IllegalArgumentException("Неверное количество полей");
        }

        try {
            SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
            format.setLenient(false);

            int id = Integer.parseInt(p[0].trim());
            String type = p[1].trim();
            String model = p[2].trim();
            double capacity = Double.parseDouble(p[3].trim());
            int spinSpeed = Integer.parseInt(p[4].trim());
            Date date = format.parse(p[5].trim());
            double price = Double.parseDouble(p[6].trim());

            if (id <= 0 || type.isEmpty() || model.isEmpty() || capacity <= 0 || spinSpeed <= 0 || price < 0) {
                throw new IllegalArgumentException("Недопустимое значение поля");
            }

            return new WashingMachine(id, type, model, capacity, spinSpeed, date, price);
        }
        catch (NumberFormatException | ParseException e) {
            throw new IllegalArgumentException("Неверный формат числа или даты");
        }
    }

    public String toLine() {
        String date = (releaseDate == null) ? "" : new SimpleDateFormat("dd.MM.yyyy").format(releaseDate);
        return String.join(";", String.valueOf(getId()), Objects.toString(type, ""), Objects.toString(model, ""), String.valueOf(capacity), String.valueOf(spinSpeed), date, String.valueOf(price));
    }

    @Override
    public String toString() {
        return "{id=" + getId() + ", type=" + type + ", model=" + model + ", capacity=" + capacity + ", spinSpeed=" + spinSpeed + ", releaseDate=" + (releaseDate == null ? "" : new SimpleDateFormat("dd.MM.yyyy").format(releaseDate)) + ", price=" + price + "}";
    }
}
