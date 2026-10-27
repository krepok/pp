package washing;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        Menu menu = new Menu(new ListStorage(), new MapStorage());
        menu.run();
    }
}

class Menu {
    private static final String PASSWORD_HASH = "21232f297a57a5a743894a0e4a801fc3";
    private static final String KEY = "secret";

    private final Storage list;
    private final Storage map;
    private Storage storage;
    private final Scanner in = new Scanner(System.in);
    private final Map<String, FileHandler> handlers = new LinkedHashMap<>();

    public Menu(Storage list, Storage map) 
    {
        this.list = list;
        this.map = map;
        this.storage = list;
        handlers.put("txt", new TxtFile());
        handlers.put("xml", new XmlFile());
        handlers.put("json", new JsonFile());
        handlers.put("enc", new EncFile(KEY));
    }

    public void run() throws Exception {
        if (!login()) {
            return;
        }
        load("txt");
        while (true) {
            System.out.println("1 Показать | 2 Добавить | 3 Изменить | 4 Удалить | 5 Сортировать | 6 Загрузить | 7 Сохранить | 8 Архив | 9 Сменить хранилище | 0 Выход");
            try {
                switch (ask("Пункт")) {
                    case "1" -> print(storage.getAll());
                    case "2" -> add();
                    case "3" -> edit();
                    case "4" -> delete();
                    case "5" -> sort();
                    case "6" -> load(ask("Формат (txt/xml/json/enc)"));
                    case "7" -> save(ask("Формат (txt/xml/json/enc)"));
                    case "8" -> {
                        Zipper.zip("archive.zip", "data.txt", "data.xml", "data.json", "data.enc", "errors.log");
                        System.out.println("Создан archive.zip");
                    }
                    case "9" -> switchStorage();
                    case "0" -> {
                        return;
                    }
                    default -> System.out.println("Неверный пункт");
                }
            } catch (Exception e) {
                System.out.println("Ошибка: " + e.getMessage());
            }
        }
    }

    private boolean login() throws Exception {
        for (int i = 0; i < 3; i++) {
            if (Crypto.md5(ask("Пароль")).equals(PASSWORD_HASH)) {
                return true;
            }
            System.out.println("Неверный пароль");
        }
        return false;
    }

    private String ask(String question) {
        System.out.print(question + ": ");
        return in.nextLine().trim();
    }

    private WashingMachine input(String id) {
        String line = String.join(";", id, ask("Тип"), ask("Модель"), ask("Загрузка, кг"), ask("Скорость отжима"), ask("Дата выпуска (гггг-мм-дд)"), ask("Цена"));
        try {
            return WashingMachine.parse(line);
        } 
        catch (IllegalArgumentException e) {
            Log.error(e.getMessage(), line);
            return null;
        }
    }

    private void add() {
        WashingMachine m = input(ask("ID"));
        if (m == null) {
            return;
        }
        if (storage.add(m)) {
            autosave();
        }
        else {
            System.out.println("ID уже существует");
        }
    }

    private void edit() {
        WashingMachine m = input(ask("ID"));
        if (m == null) {
            return;
        }
        if (storage.update(m)) {
            autosave();
        }
        else {
            System.out.println("Не найдено");
        }
    }

    private void delete() {
        if (storage.delete(Integer.parseInt(ask("ID")))) {
            autosave();
        }
        else {
            System.out.println("Не найдено");
        }
    }

    private void sort() {
        Comparator<WashingMachine> comparator;
        switch (ask("Поле: 1 модель, 2 цена, 3 дата, 4 загрузка")) {
            case "1" -> comparator = (a, b) -> a.getModel().compareTo(b.getModel());
            case "2" -> comparator = (a, b) -> Double.compare(a.getPrice(), b.getPrice());
            case "3" -> comparator = (a, b) -> a.getReleaseDate().compareTo(b.getReleaseDate());
            case "4" -> comparator = (a, b) -> Double.compare(a.getCapacity(), b.getCapacity());
            default -> {
                System.out.println("Неверное поле");
                return;
            }
        }
        print(storage.sorted(comparator));
    }

    private void print(Collection<WashingMachine> items) {
        Iterator<WashingMachine> iterator = items.iterator();
        while (iterator.hasNext()) {
            System.out.println(iterator.next());
        }
    }

    private void load(String ext) {
        FileHandler handler = handlers.get(ext);
        if (handler == null) {
            System.out.println("Неизвестный формат");
            return;
        }
        try {
            List<WashingMachine> items = handler.read("data." + ext);
            storage.clear();
            for (WashingMachine m : items) {
                if (!storage.add(m)) {
                    Log.error("Повторный ID", m.toLine());
                }
            }
        }
        catch (Exception e) {
            System.out.println("Ошибка чтения: " + e.getMessage());
        }
    }

    private void save(String ext) {
        FileHandler handler = handlers.get(ext);
        if (handler == null) {
            System.out.println("Неизвестный формат");
            return;
        }
        try {
            handler.write("data." + ext, storage.getAll());
        }
        catch (Exception e) {
            System.out.println("Ошибка записи: " + e.getMessage());
        }
    }

    private void autosave() {
        save("txt");
    }

    private void switchStorage() {
        Storage other = storage == list ? map : list;
        other.clear();
        for (WashingMachine m : storage.getAll()) {
            other.add(m);
        }
        storage = other;
        System.out.println("Хранилище: " + storage.getClass().getSimpleName());
    }
}