public class File implements FileSystem {
    private String name;
    private String content;

    public File(String name, String content) {
        this.name = name;
        this.content = content;
    }

    @Override
    public void ls() {
        System.out.println("file name is: " + name + " Contents: " + content);
    }
}