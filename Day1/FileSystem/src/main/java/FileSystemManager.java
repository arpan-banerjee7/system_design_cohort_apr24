// Singleton design pattern
public class FileSystemManager {
    // static instance so that it is initialized only once
    private static FileSystemManager instance;
    private Directory root;

    // constructor private
    private FileSystemManager() {
        this.root = new Directory("root");
    }

    // singleton
    public static synchronized FileSystemManager getInstance() {
        if (instance == null) {
            instance = new FileSystemManager();
        }
        return instance;
    }

    public Directory getRoot() {
        return root;
    }

    public void displayFileSystem() {
        root.ls();
    }
}

