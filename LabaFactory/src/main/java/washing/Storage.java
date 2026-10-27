package washing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public abstract class Storage {
    public abstract boolean add(WashingMachine m);

    public abstract boolean update(WashingMachine m);

    public abstract boolean delete(int id);

    public abstract WashingMachine get(int id);

    public abstract Collection<WashingMachine> getAll();

    public abstract void clear();

    public List<WashingMachine> sorted(Comparator<WashingMachine> comparator) {
        List<WashingMachine> result = new ArrayList<>(getAll());
        Collections.sort(result, comparator);
        return result;
    }
}

class ListStorage extends Storage {
    private final List<WashingMachine> items = new ArrayList<>();

    @Override
    public boolean add(WashingMachine m) {
        if (get(m.getId()) != null) {
            return false;
        }
        return items.add(m);
    }

    @Override
    public boolean update(WashingMachine m) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getId() == m.getId()) {
                items.set(i, m);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean delete(int id) {
        return items.removeIf(m -> m.getId() == id);
    }

    @Override
    public WashingMachine get(int id) {
        for (WashingMachine m : items) {
            if (m.getId() == id) {
                return m;
            }
        }
        return null;
    }

    @Override
    public Collection<WashingMachine> getAll() {
        return items;
    }

    @Override
    public void clear() {
        items.clear();
    }
}

class MapStorage extends Storage {
    private final Map<Integer, WashingMachine> items = new TreeMap<>();

    @Override
    public boolean add(WashingMachine m) {
        return items.putIfAbsent(m.getId(), m) == null;
    }

    @Override
    public boolean update(WashingMachine m) {
        return items.replace(m.getId(), m) != null;
    }

    @Override
    public boolean delete(int id) {
        return items.remove(id) != null;
    }

    @Override
    public WashingMachine get(int id) {
        return items.get(id);
    }

    @Override
    public Collection<WashingMachine> getAll() {
        return items.values();
    }

    @Override
    public void clear() {
        items.clear();
    }
}
