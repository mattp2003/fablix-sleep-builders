import com.mysql.cj.protocol.Resultset;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.sql.*;
import java.util.*;

public class HashParser {
    int lastMovie = 0;
    Map<String, String> MovieId = new HashMap<>();
    int lastStar = 0;
    Map<String, String> StarsId = new HashMap<>();
    int lastGenre = 0;
    Map<String, String> GenreId = new HashMap<String, String>();

    Map<String, String> genreMap = new HashMap<>();

    Document mainsDom;
    Document actorsDom;
    Document castsDom;
    int starsAdded = 0;
    int moviesAdded = 0;
    int genresAdded = 0;
    int stars_in_moviesAdded = 0;
    int genres_in_moviesAdded = 0;

    int duplicateStars = 0;
    int duplicateMovies = 0;
    int inconsistentStars = 0;
    int inconsistentMovies = 0;

    public void run() throws SQLException, ParserConfigurationException, SAXException, IOException {
        //set genre table
        genreMap.put("susp", "Thriller");
        genreMap.put("cnr", "Cops and Robbers");
        genreMap.put("dram", "Drama");
        genreMap.put("west", "Western");
        genreMap.put("myst", "Mystery");
        genreMap.put("s.f.", "Sci-Fi");
        genreMap.put("scfi", "Sci-Fi");
        genreMap.put("advt", "Adventure");
        genreMap.put("horr", "Horror");
        genreMap.put("romt", "Romance");
        genreMap.put("comd", "Comedy");
        genreMap.put("musc", "Musical");
        genreMap.put("docu", "Documentary");
        genreMap.put("porn", "Adult");
        genreMap.put("noir", "Noir");
        genreMap.put("biop", "Biography");
        genreMap.put("actn", "Action");


        String loginUser = "mytestuser";
        String loginPasswd = "My6$Password";
        String loginUrl = "jdbc:mysql://localhost:3306/moviedb";
        //Class.forName("com.mysql.jdbc.Driver").newInstance();
        Connection connection = DriverManager.getConnection(loginUrl, loginUser, loginPasswd);

        try(Statement statement = connection.createStatement()) {
            //System.out.println("Adding procedures");
            //addProcedures(statement);
            QueryMovies(statement);
            QueryStars(statement);
            QueryGenres(statement);

            System.out.println("Parsing files");
            parseXmlFile();
            System.out.println("Parsing stars");
            parseStars(statement);
            System.out.println("Parsing movies");
            parseMovies(statement);
            System.out.println("Parsing casts");
            parseCasts(statement);
            System.out.println("Executing Batch");
            statement.executeBatch();

            System.out.println(starsAdded + " stars added");
            System.out.println(moviesAdded + " movies added");
            System.out.println(genresAdded + " genres added");
            System.out.println(stars_in_moviesAdded + " stars in movies");
            System.out.println(genres_in_moviesAdded + " genres in movies");

            System.out.println(duplicateStars + " duplicate stars");
            System.out.println(duplicateMovies + " duplicate movies");
            System.out.println(inconsistentStars + " inconsistent stars");
            System.out.println(inconsistentMovies + " inconsistent movies");
        }
        catch (Exception e){
            //System.out.println(e);
            e.printStackTrace();
        }
    }
    private void QueryMovies(Statement statement) throws SQLException {
        String query = "SELECT id, title FROM movies;";
        ResultSet rs = statement.executeQuery(query);
        while (rs.next()){
            String title = rs.getString("title");
            String id = rs.getString("id");

            id = id.replaceAll("[^\\d.]", "");
            int idVal = Integer.parseInt(id);
            lastMovie = Math.max(lastMovie, idVal);

            MovieId.put(title, id);
        }
    }

    private void QueryStars(Statement statement) throws SQLException {
        String query = "SELECT id, name FROM stars;";
        ResultSet rs = statement.executeQuery(query);
        while (rs.next()){
            String name = rs.getString("name");
            String id = rs.getString("id");

            id = id.replaceAll("[^\\d.]", "");
            int idVal = Integer.parseInt(id);
            lastStar = Math.max(lastStar, idVal);

            StarsId.put(name, id);
        }
    }

    private void QueryGenres(Statement statement) throws SQLException {
        String query = "SELECT id, name FROM genres;";
        ResultSet rs = statement.executeQuery(query);
        while (rs.next()){
            String name = rs.getString("name");
            String id = rs.getString("id");

            id = id.replaceAll("[^\\d.]", "");
            int idVal = Integer.parseInt(id);
            lastGenre = Math.max(lastGenre, idVal);

            GenreId.put(name, id);
        }
    }

    private void parseXmlFile() {
        // get the factory
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

        try {

            // using factory get an instance of document builder
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

            // parse using builder to get DOM representation of the XML file
            mainsDom = documentBuilder.parse("mains243.xml");
            actorsDom = documentBuilder.parse("actors63.xml");
            castsDom = documentBuilder.parse("casts124.xml");

        } catch (ParserConfigurationException | SAXException | IOException error) {
            error.printStackTrace();
        }
    }
    private int getTotalStars(Statement statement) throws SQLException {
        String starQuery = "select count(id) as c from stars;";
        ResultSet rs = statement.executeQuery(starQuery);
        rs.next();
        return(Integer.parseInt(rs.getString("c")));

    }
    private int getTotalStarsInMovies(Statement statement) throws SQLException {
        String starQuery = "select count(*) as c from stars_in_movies;";
        ResultSet rs = statement.executeQuery(starQuery);
        rs.next();
        return(Integer.parseInt(rs.getString("c")));
    }
    private int getTotalGenres(Statement statement) throws SQLException {
        String starQuery = "select count(*) as c from genres;";
        ResultSet rs = statement.executeQuery(starQuery);
        rs.next();
        return(Integer.parseInt(rs.getString("c")));
    }
    private int getTotalGenresInMovies(Statement statement) throws SQLException {
        String starQuery = "select count(*) as c from genres_in_movies;";
        ResultSet rs = statement.executeQuery(starQuery);
        rs.next();
        return(Integer.parseInt(rs.getString("c")));
    }
    private void parseStars(Statement statement) throws SQLException{
        // get the document root Element
        Element documentElement = actorsDom.getDocumentElement();

        NodeList nodeList = documentElement.getElementsByTagName("actor");

        for (int i = 0; i < nodeList.getLength(); i++) {

            Element star = (Element) nodeList.item(i);

            String name = null;
            name = getTextValue(star, "stagename").replaceAll("[^A-Za-z0-9 ]", "");

            if (StarsId.containsKey(name)){
                duplicateStars++;
                continue;
            }

            String year = null;
            year = getTextValue(star, "dob");
            //System.out.println(year);
            lastStar++;
            String newId = "nm" + String.format("%07d",lastStar);
            System.out.println(newId);
            StarsId.put(name, newId);

            String query = "INSERT INTO stars VALUES (" + stringValue(newId) + ", "+ stringValue(name) + ", "+ year + ")";
            statement.addBatch(query);
            starsAdded++;
        }
    }

    private void parseMovies(Statement statement) throws SQLException {
        // get the document root Element
        Element documentElement = mainsDom.getDocumentElement();

        NodeList nodeList = documentElement.getElementsByTagName("directorfilms");
        for (int i = 0; i < nodeList.getLength(); i++) {

            // get the employee element
            Element director = (Element) nodeList.item(i);

            String directorName;
            directorName = getTextValue(director, "dirname");
            if (directorName == null) {
                directorName = getTextValue(director, "dirn");
            }

            NodeList filmList = ((Element)director.getElementsByTagName("films").item(0)).getElementsByTagName("film");

            for (int j = 0; j < filmList.getLength(); j++) {
                Element film = (Element) filmList.item(j);
                String filmName = null;
                filmName = getTextValue(film, "t");
                String year = null;
                year = getTextValue(film, "year");
                String id = null;
                id = getTextValue(film, "fid");


                if (MovieId.containsKey(filmName)){
                    duplicateMovies++;
                    System.out.println("Movie " + filmName + " is a duplicate");
                }
                else{
                    moviesAdded++;
                    lastMovie++;

                    String newId = "tt" + String.format("%07d",lastMovie);
                    System.out.println(newId);
                    MovieId.put(filmName, newId);
                    String query = "INSERT INTO movies VALUES (" + stringValue(newId) + ", "+ stringValue(filmName) + ", "+ year + ", "+ stringValue(directorName) + ")";
                    System.out.println(query);
                    statement.addBatch(query);

                    NodeList genres = film.getElementsByTagName("cats");
                    ArrayList<String> genreNames = new ArrayList<>();
                    for (int k = 0; k < genres.getLength(); k++) {
                        Element genre = (Element) genres.item(k);
                        String xmlGenre = getTextValue(genre, "cat");
                        //System.out.println(genreName);
                        if (xmlGenre != null){
                            if (genreMap.containsKey(xmlGenre.toLowerCase())) {
                                xmlGenre = genreMap.get(xmlGenre.toLowerCase());
                            }

                            if (GenreId.containsKey(xmlGenre)){
                                String genreId = GenreId.get(xmlGenre);
                                String genreInsert = "INSERT INTO genres_in_movies VALUES(" + genreId + ", "+ stringValue(newId) + ")";
                                statement.addBatch(genreInsert);
                            }
                            else{
                                lastGenre++;
                                GenreId.put(xmlGenre, "" + lastGenre);
                                String newGenre = "INSERT INTO genres VALUES(" + lastGenre + ", "+ stringValue(xmlGenre) + ")";
                                statement.addBatch(newGenre);
                                String genreInsert = "INSERT INTO genres_in_movies VALUES(" + lastGenre + ", "+ stringValue(newId) + ")";
                                statement.addBatch(genreInsert);
                                genresAdded++;
                            }
                            genres_in_moviesAdded++;
                        }
                    }
                }
            }
        }
    }
    private void parseCasts(Statement statement) throws SQLException {
        Element documentElement = castsDom.getDocumentElement();

        NodeList nodeList = documentElement.getElementsByTagName("m");
        for (int i = 0; i < nodeList.getLength(); i++) {
            Element e = (Element) nodeList.item(i);

            String actor = null;
            actor = getTextValue(e, "a");
            if (actor != null){
                actor = actor.replaceAll("[^A-Za-z0-9 ]", "");
            }
            String actorId = StarsId.get(actor);
            if (actorId == null){
                System.out.println("Actor " + actor + " is missing");
                inconsistentStars++;
                continue;
            }

            String movieTitle = null;
            movieTitle = getTextValue(e, "t");
            String movieId = MovieId.get(movieTitle);
            if (movieId == null){
                System.out.println("Movie " + movieTitle + " is missing");
                inconsistentMovies++;
                continue;
            }

            String query = "INSERT INTO stars_in_movies VALUES(" + stringValue(actorId) +", " + stringValue(movieId) + ");";
            //System.out.println(query);
            statement.addBatch(query);
            stars_in_moviesAdded++;
        }
    }

    private String stringValue(String s){
        if (s == null){
            return null;
        }
        return "\"" + s + "\"";
    }

    private String getTextValue(Element element, String tagName) {
        String textVal = null;
        NodeList nodeList = element.getElementsByTagName(tagName);
        if (nodeList.getLength() > 0 && nodeList.item(0).getFirstChild() != null) {
            textVal = nodeList.item(0).getFirstChild().getNodeValue();
            if (tagName.equals("year")){
                textVal = textVal.replaceAll("[a-zA-Z]", "0");
            }
            else if (tagName.equals("dob")){
                if (!textVal.matches("^[0-9]+$")){
                    return null;
                }
            }
        }
        return textVal;
    }

    /**
     * Calls getTextValue and returns a int value
     */
    private int getIntValue(Element ele, String tagName) {
        // in production application you would catch the exception
        if (tagName.equals("year")){
            String year = getTextValue(ele, "year").replaceAll("[a-z]", "0").trim();
            return Integer.parseInt(year);
        }
        return Integer.parseInt(getTextValue(ele, tagName));
    }

    public static void main(String[] args) throws Exception{
        // create an instance
        HashParser parser = new HashParser();

        // call run example
        parser.run();
    }
}
