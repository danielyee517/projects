import java.io.Serializable;
import java.util.HashSet;

public class MarkedFiles implements Serializable {
    private HashSet<String> filenames = new HashSet<String>();

    public boolean contains(String filename) {
        return filenames.contains(filename);
    }

    public boolean add(String filename) {
        return filenames.add(filename);
    }

    public boolean remove(String filename) {
        return filenames.remove(filename);
    }

    public HashSet<String> getFilenames() {
        return filenames;
    }

    public int size() {
        return filenames.size();
    }

    public void clear() {
        filenames.clear();
    }
}
