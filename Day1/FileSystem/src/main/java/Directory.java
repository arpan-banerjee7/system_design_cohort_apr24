
import java.util.ArrayList;
import java.util.List;

public class Directory implements FileSystem {
    String name;
    List<FileSystem> fileSystemList;

    public Directory(String name) {
        this.name = name;
        this.fileSystemList = new ArrayList<>();
    }

    public void add(FileSystem obj) {
        this.fileSystemList.add(obj);
    }

    @Override
    public void ls() {
        System.out.println(" _Directory name: " + name);
        for (FileSystem fileSystem : fileSystemList) {
            fileSystem.ls();
        }
    }

}