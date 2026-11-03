package washing;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public abstract class FileHandler {
    public abstract List<WashingMachine> read(String path) throws Exception;

    public abstract void write(String path, Collection<WashingMachine> items) throws Exception;

    protected List<WashingMachine> parseLines(List<String> lines) {
        List<WashingMachine> result = new ArrayList<>();
        for (String line : lines) {
            if (line.isBlank()) {
                continue;
            }
            try {
                result.add(WashingMachine.parse(line));
            }
            catch (IllegalArgumentException e) {
                Log.error(e.getMessage(), line);
            }
        }
        return result;
    }

    protected List<String> toLines(Collection<WashingMachine> items) {
        List<String> lines = new ArrayList<>();
        for (WashingMachine m : items) {
            lines.add(m.toLine());
        }
        return lines;
    }
}

class TxtFile extends FileHandler {
    @Override
    public List<WashingMachine> read(String path) throws Exception {
        return parseLines(Files.readAllLines(Path.of(path)));
    }

    @Override
    public void write(String path, Collection<WashingMachine> items) throws Exception {
        Files.write(Path.of(path), toLines(items));
    }
}

class XmlFile extends FileHandler {
    private static final String[] NAMES = {"id", "type", "model", "capacity", "spinSpeed", "releaseDate", "price"};

    @Override
    public List<WashingMachine> read(String path) throws Exception {
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File(path));
        NodeList nodes = doc.getElementsByTagName("machine");
        List<String> lines = new ArrayList<>();

        for (int i = 0; i < nodes.getLength(); i++) {
            Element element = (Element) nodes.item(i);
            List<String> values = new ArrayList<>();
            
            for (String name : NAMES) {
                NodeList children = element.getElementsByTagName(name);
                values.add(children.getLength() == 0 ? "" : children.item(0).getTextContent());
            }
            lines.add(String.join(";", values));
        }
        return parseLines(lines);
    }

    @Override
    public void write(String path, Collection<WashingMachine> items) throws Exception {
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element root = doc.createElement("machines");

        doc.appendChild(root);
        for (String line : toLines(items)) {
            String[] values = line.split(";", -1);
            Element machine = doc.createElement("machine");
            root.appendChild(machine);

            for (int i = 0; i < NAMES.length; i++) {
                Element field = doc.createElement(NAMES[i]);
                field.setTextContent(values[i]);
                machine.appendChild(field);
            }
        }
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.transform(new DOMSource(doc), new StreamResult(new File(path)));
    }
}

class JsonFile extends FileHandler {
    private final Gson gson = new GsonBuilder().setDateFormat("dd.MM.yyyy").setPrettyPrinting().create();

    @Override
    public List<WashingMachine> read(String path) throws Exception {
        WashingMachine[] array = gson.fromJson(Files.readString(Path.of(path)), WashingMachine[].class);
        List<String> lines = new ArrayList<>();
        
        for (WashingMachine m : array) {
            lines.add(m.toLine());
        }
        return parseLines(lines);
    }

    @Override
    public void write(String path, Collection<WashingMachine> items) throws Exception {
        Files.writeString(Path.of(path), gson.toJson(new ArrayList<>(items)));
    }
}

class EncFile extends FileHandler {
    private final String key;

    public EncFile(String key) {
        this.key = key;
    }

    @Override
    public List<WashingMachine> read(String path) throws Exception {
        String text = Crypto.decrypt(Files.readString(Path.of(path)), key);
        return parseLines(Arrays.asList(text.split("\n")));
    }

    @Override
    public void write(String path, Collection<WashingMachine> items) throws Exception {
        Files.writeString(Path.of(path), Crypto.encrypt(String.join("\n", toLines(items)), key));
    }
}