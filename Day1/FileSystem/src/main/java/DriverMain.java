// https://github.com/sakshamece/LowLevelDesign/blob/main/InMemoryFileSystem/src/Directory.java
// other methods like delete remove, move copy
public class DriverMain {
    public static void main(String[] args) {
        // Get the single instance of FileSystemManager
        FileSystemManager fsManager = FileSystemManager.getInstance();
        Directory root = fsManager.getRoot();

        Directory movieDir = new Directory("movie");
        root.add(movieDir);

        File file = new File("Border", "classic movie");
        movieDir.add(file);

        // movie/"border"
        movieDir.ls();

        System.out.println("**********************");
        Directory comedyMovieDir = new Directory("thriller");
        File comFile = new File("Rambo", "Rambo part II");
        comedyMovieDir.add(comFile);

        movieDir.add(comedyMovieDir);

        // movie/"Border"
        // comedy/BHK
        movieDir.ls();


    }

}

