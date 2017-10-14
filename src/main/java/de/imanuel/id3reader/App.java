package de.imanuel.id3reader;

import com.mpatric.mp3agic.ID3v1;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.lang.System.out;

/**
 * The main application
 */
public class App {

    /**
     * The start point of the application
     *
     * @param args The parameters from the command line
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            out.println("Please provide a folder name as parameter");
        } else {
            try {
                List<Mp3File> files = getFiles(args[0]);
                String genreFilter = "";
                if (args.length == 2) {
                    genreFilter = args[1];
                }

                final String genreFilterForLambda = genreFilter;
                files.parallelStream().filter(file -> {
                    ID3v1 id3v1Tag = file.getId3v1Tag();
                    return null != id3v1Tag &&
                            (
                                    Objects.equals(id3v1Tag.getGenreDescription(), genreFilterForLambda)
                                            || Objects.equals(genreFilterForLambda, "")
                            );
                }).forEach(file -> {
                    ID3v1 id3v1Tag = file.getId3v1Tag();
                    out.println(String.format("%-20s %-30s - %-30s (%s)", id3v1Tag.getGenreDescription(), id3v1Tag.getArtist(), id3v1Tag.getTitle(), file.getFilename()));
                });
            } catch (Exception e) {
                out.println("There was an error listing the mp3 files");
                e.printStackTrace();
            }
        }
    }

    /**
     * Iterates over a directory and lists all mp3 files recursively
     *
     * @param directory The directory to iterate over
     * @return A List with all mp3 files
     * @throws InvalidDataException    Thrown when the mp3 data are invalid
     * @throws IOException             Thrown when an error opening the file occurs
     * @throws UnsupportedTagException Thrown when the mp3 tags are invalid
     */
    private static List<Mp3File> getFiles(String directory) throws InvalidDataException, IOException, UnsupportedTagException {
        List<Mp3File> result = new ArrayList<>();
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(directory))) {
            for (Path path : directoryStream) {
                if (Files.isDirectory(path)) {
                    result.addAll(getFiles(path.toString()));
                } else if (path.toString().endsWith(".mp3")) {
                    result.add(new Mp3File(path.toFile()));
                }
            }
        } catch (InvalidDataException | IOException | UnsupportedTagException e) {
            throw e;
        }

        return result;
    }
}
