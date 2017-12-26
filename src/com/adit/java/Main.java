package com.adit.java;

import com.adit.java.model.Actor;
import com.adit.java.model.Movie;
import com.adit.java.model.Person;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class Main {

    public static void main(String[] args) throws IOException {
        Scrabble();
    }

    /**
     * Use Custom Spliterator to Load Person Objects from Text File
     */
    public static List<Person> customSpliterator() {
        Path path = Paths.get("./people.txt");
        try (Stream<String> lines = Files.lines(path);) {
            Spliterator<String> lineSpliterator = lines.spliterator();
            Spliterator<Person> peopleSpliterator = new PersonSpliterator(lineSpliterator);
            Stream<Person> people = StreamSupport.stream(peopleSpliterator, false);
            return people.collect(Collectors.toList());

        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    /**
     * Cocatenate Two Stream of Lines into One Stream of Words using FlatMap
     * Dataset -> Tom Sawyer cut into 2 Txt files
     */
    public static void StreamConcat() {

        try (Stream<String> lines1 = Files.lines(Paths.get("./tom1.txt"));
             Stream<String> lines2 = Files.lines(Paths.get("./tom2.txt"))) {

            Stream<Stream<String>> streamofStreams = Stream.of(lines1, lines2);
            Stream<String> streamOfLines = streamofStreams.flatMap(Function.identity());
            System.out.println(streamOfLines.count());
            Function<String, Stream<String>> lineSplitter =
                    line -> Pattern.compile(" ").splitAsStream(line);
            Stream<String> streamOfWords = streamOfLines.flatMap(lineSplitter);

            System.out.println(streamOfWords.map(word -> word.toLowerCase())
                    .distinct().count());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * IntStream
     */
    public static double AverageAge() {
        List<Person> people = customSpliterator();
        return people.stream()
                .mapToInt(Person::getAge)
                .filter(age -> age > 20)
                .average().orElse(0);
    }

    /**
     * Prints Highest Scoring Shakespeare word in Scrabble
     * @throws IOException
     */
    public static void Scrabble() throws IOException {
        Set<String> shakespeare = Files.lines(Paths.get("./words.shakespeare.txt"))
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        Set<String> scrabble = Files.lines(Paths.get("./ospd.txt"))
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        final int [] scrabbleScore = {
                1,3,3,2,1,4,2,4,1,8,5,1,3,1,1,3,10,1,1,1,1,4,4,8,4,10
        };
        Function<String, Integer> getScore =
                word -> word.chars()
                        .map(letter -> scrabbleScore[letter - 'a'])
                        .sum();
        long start = System.currentTimeMillis();
        String result = shakespeare.stream()
                 .parallel()
                 .filter(scrabble::contains)
                 .max(Comparator.comparing(getScore))
                 .orElse("");
        long end = System.currentTimeMillis();
        System.out.println("Parallel seconds : " + (end-start));
        start =  System.currentTimeMillis();
        result = shakespeare.stream()
                .filter(scrabble::contains)
                .max(Comparator.comparing(getScore))
                .orElse("");
        end  = System.currentTimeMillis();
        System.out.println("Sequential seconds :" + (end-start));
    }

    /**
     *
     * @param n
     * Find N Highest Score from Shakespeare that are valid in Scrabble
     * Dataset 70k+ words in Shakespeare
     * @throws IOException
     */
    public static void ScrabbleHighScore(int n) throws IOException {
        Set<String> shakespeare = Files.lines(Paths.get("./words.shakespeare.txt"))
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        Set<String> scrabbleWords = Files.lines(Paths.get("./ospd.txt"))
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        // Scrabble Letter Score Array
        final int [] scrabbleScore = {
                1,3,3,2,1,4,2,4,1,8,5,1,3,1,1,3,10,1,1,1,1,4,4,8,4,10
        };

        // Scrabble Letter Distribution
        int [] scrabbleLetterDistribution = {
                9,2,2,1,12,2,3,2,9,1,1,4,2,6,8,2,1,6,4,6,4,2,2,1,2,1
        };

        Function<String, Integer> getScore =
                word -> word.chars()
                        .map(letter -> scrabbleScore[letter - 'a'])
                        .sum();

        Function<String, Map<Integer, Long>> letterFreq =
                word -> word.chars().boxed()
                        .collect(Collectors.groupingBy(letter -> letter, Collectors.counting()));

        Function<String, Long> nBlanks =
                word -> letterFreq.apply(word)
                        .entrySet()
                        .stream()
                        .mapToLong(
                                entry -> Long.max(entry.getValue() -
                                        scrabbleLetterDistribution[entry.getKey() - 'a'],0))
                        .sum();

        Function<String, Integer> scoreWithLetterDistribution =
                word -> letterFreq.apply(word).entrySet()
                        .stream()
                        .mapToInt(entry -> scrabbleScore[entry.getKey() - 'a'] *
                                    Integer.min(entry.getValue().intValue() -
                                            scrabbleLetterDistribution[entry.getKey() -'a'], 0))
                        .sum();

        Map<Integer, List<String>> wordsToScore =
                shakespeare.stream()
                .filter(scrabbleWords::contains)   // Check if word is in Scrabble
                .filter(word -> nBlanks.apply(word) <= 2) // Check number Of Blanks < =2
                .collect(Collectors.groupingBy(scoreWithLetterDistribution));

        wordsToScore.entrySet()
                .stream() //Sort in Decreasing Order of Score
                .sorted(Comparator.comparing(entry -> -entry.getKey()))
                .limit(n)
                .forEach(entry -> System.out.println(entry.getKey() + " " + entry.getValue()));

    }


    /**
     *  CUSTOM COLLECTOR EXAMPLE
     *  DataSet -> 14,000 Movies and Actors from Imdb
     */

    public static void CustomCollector() {
        Set<Movie>  movies = new HashSet<>();
        Stream<String> lines = null;
        try {
            // Parse movie Text file;
            lines = Files.lines(Paths.get("./movies-mpaa.txt"));
            lines.forEach(
                    (String line) -> {
                        String [] elements = line.split("/");
                        String title = elements[0].substring(0, elements[0].indexOf("(")).trim();
                        String releaseYear =
                                elements[0].substring(elements[0].lastIndexOf("(") + 1 ,
                                        elements[0].lastIndexOf(")"));
                        if (releaseYear.contains(",")) {
                            return;
                        }
                        Movie movie = new Movie(title, Integer.valueOf(releaseYear));
                        for (int i = 1; i < elements.length ; i++) {
                            String[] name = elements[i].split(", ");
                            String lastName = name[0].trim();
                            String firstName = "";
                            if (name.length > 1) {
                                firstName = name[1].trim();
                            }
                            Actor actor = new Actor(firstName, lastName);
                            movie.getActors().add(actor);
                        }
                        movies.add(movie);
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Num of Actors
        long numberOfActors = movies.stream().distinct().count();
        System.out.println("Num of Distinct Actors : " + numberOfActors);
        movies.stream().flatMap(movie -> movie.getActors().stream());

        // Actors that played in the greatest number of movies;

    }


}
