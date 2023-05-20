import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DatReader {

    private String fileContent;

    public DatReader(String filename) {
        Path filePath = Path.of(filename);
        try {
            fileContent = Files.readString(filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getStringVar(String varName) {
        Pattern p = Pattern.compile("[\\t ]*" + varName + "[\\t ]*=\\s*\"([^\"]+)\"");
        Matcher m = p.matcher(fileContent);
        m.find();
        return m.group(1);
    }

    public Double getDoubleVar(String varName) {
        Pattern p = Pattern.compile("[\\t ]*" + varName + "[\\t ]*=\\s*([0-9]+(?:\\.[0-9]*)?)");
        Matcher m = p.matcher(fileContent);
        m.find();
        return Double.parseDouble(m.group(1));
    }

    public int getIntVar(String varName) {
        Pattern p = Pattern.compile("[\\t ]*" + varName + "[\\t ]*=\\s*([0-9]+)");
        Matcher m = p.matcher(fileContent);
        m.find();
        return Integer.parseInt(m.group(1));
    }

    public Point getPointVar(String varName) {
        Pattern p = Pattern.compile("[\\t ]*" + varName + "[\\t ]*=\\s*\\(\\s*(\\d+)\\s*,\\s*(\\d+)\\s*\\)");

        Matcher m = p.matcher(fileContent);
        m.find();
        int x = Integer.parseInt(m.group(1));
        int y = Integer.parseInt(m.group(2));
        return new Point(x, y);
    }
}
