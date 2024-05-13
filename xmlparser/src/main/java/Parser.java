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

public class Parser {
    Map<String, String> xmlMovieId = new HashMap<>();
    Map<String, String> genreMap = new HashMap<String, String>();
    Set<String> missingActors = new HashSet<>();
    Set<String> missingMovies = new HashSet<>();
    Document mainsDom;
    Document actorsDom;
    Document castsDom;
    int maxStar = 0;
    int maxMovie = 0;
    int maxGenre = 0;

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
        Class.forName("com.mysql.jdbc.Driver").newInstance();
        Connection connection = DriverManager.getConnection(loginUrl, loginUser, loginPasswd);

        try(Statement statement = connection.createStatement()) {

            String starQuery = "select max(id) as m from stars;";
            ResultSet rs = statement.executeQuery(starQuery);
            if (rs.next() && rs.getString("m") != null){
                System.out.println(rs.getString("m"));
                maxStar = Integer.parseInt(rs.getString("m").substring(2));
            }

            String movieQuery = "select max(id) as m from movies;";
            rs = statement.executeQuery(movieQuery);
            if (rs.next() && rs.getString("m") != null){
                System.out.println(rs.getString("m"));
                maxMovie = Integer.parseInt(rs.getString("m").substring(2));
            }

            String genreQuery = "select max(id) as m from genres;";
            rs = statement.executeQuery(genreQuery);
            if (rs.next() && rs.getString("m") != null){
                System.out.println(rs.getString("m"));
                maxGenre = Integer.parseInt(rs.getString("m"));
            }
            System.out.println(maxStar + " " + maxMovie + " " + maxGenre);

            System.out.println("Parsing files");
            parseXmlFile();
            System.out.println("Parsing stars");
            parseStars(statement);
            System.out.println("Parsing movies");
            parseMovies(statement);
            System.out.println("Parsing casts");
            parseCasts(statement);

            int starsAdded = 0;
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

    private void parseStars(Statement statement) throws SQLException{
        // get the document root Element
        Element documentElement = actorsDom.getDocumentElement();

        NodeList nodeList = documentElement.getElementsByTagName("actor");
        for (int i = 0; i < nodeList.getLength(); i++) {

            Element star = (Element) nodeList.item(i);

            String name = null;
            name = getTextValue(star, "stagename").replaceAll("[^A-Za-z0-9 ]", "");
            if (name == null){
                inconsistentStars++;
                continue;
            }

            String year = null;
            year = getTextValue(star, "dob");
            //System.out.println(year);


            //check if in database
            String checkQuery = "SELECT * from stars WHERE name = " + stringValue(name) + " and birthYear = " + year + ";";
            ResultSet rs = statement.executeQuery(checkQuery);

            boolean exists = rs.next();

            if (exists){
                duplicateStars++;
                System.out.println(name + " is a duplicate star");
            }
            else{
                maxStar++;
                String updateQuery = "INSERT INTO stars VALUES(" + stringValue("nm" + String.format("%07d",maxStar)) + ", " + stringValue(name) + ", " + year + ");";
                //System.out.println(updateQuery);
                statement.executeUpdate(updateQuery);
                starsAdded++;
            }

        }
    }

    private void parseMovies(Statement statement) throws SQLException {
        // get the document root Element
        Element documentElement = mainsDom.getDocumentElement();

        // get a nodelist of employee Elements, parse each into Employee object
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

                if (filmName != null && id != null){
                    xmlMovieId.put(filmName, id);
                }
                //System.out.println(filmName + " " + year + " " + id);

                //check if in database
                String checkQuery = "SELECT * from movies WHERE title = " + stringValue(filmName) + ";";
                //System.out.println(checkQuery);
                ResultSet rs = statement.executeQuery(checkQuery);

                boolean exists = rs.next();

                if (exists){
                    duplicateMovies++;
                    System.out.println(filmName + " is a duplicate");
                }
                else{
                    maxMovie++;
                    String updateQuery = "INSERT INTO movies VALUES(" + stringValue("tt" + String.format("%07d",maxMovie)) + ", " + stringValue(filmName) + ", " + year + ", " + stringValue(directorName) + ");";
                    //System.out.println(updateQuery);
                    statement.executeUpdate(updateQuery);
                    moviesAdded++;

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

                            //check if genre exists
                            String checkGenre = "SELECT * from genres WHERE name = " + stringValue(xmlGenre) + ";";
                            //System.out.println(checkGenre);
                            rs = statement.executeQuery(checkGenre);

                            boolean genreExists = rs.next();

                            if (genreExists){
                                int genreId = rs.getInt(1);
                                String addGenre = "INSERT INTO genres_in_movies VALUES(" + genreId + ", " + stringValue("tt" + String.format("%07d",maxMovie)) +");";
                                //System.out.println(addGenre);
                                statement.executeUpdate(addGenre);
                            }
                            else{
                                maxGenre++;
                                String newGenre = "INSERT INTO genres VALUES(" + maxGenre + ", " + stringValue(xmlGenre) +");";
                                //System.out.println(newGenre);
                                statement.executeUpdate(newGenre);
                                genresAdded++;
                                String addGenre = "INSERT INTO genres_in_movies VALUES(" + maxGenre + ", " + stringValue("tt" + String.format("%07d",maxMovie)) +");";
                                //System.out.println(addGenre);
                                statement.executeUpdate(addGenre);
                            }
                            //System.out.println(maxGenre);
                            genres_in_moviesAdded++;
                        }
                    }
                }
            }
        }
    }
    private void parseCasts(Statement statement) throws SQLException {
        Element documentElement = castsDom.getDocumentElement();

        // get a nodelist of employee Elements, parse each into Employee object
        NodeList nodeList = documentElement.getElementsByTagName("m");
        //System.out.println(nodeList.getLength());
        for (int i = 0; i < nodeList.getLength(); i++) {
            Element e = (Element) nodeList.item(i);

            String actor = null;
            actor = getTextValue(e, "a");

            if (missingActors.contains(actor)){
                //System.out.println("Actor " + actor + " is missing");
                //inconsistentStars++;
                continue;
            }
            if (actor==null){
                System.out.println("Actor " + actor + " is missing");
                missingActors.add(actor);
                inconsistentStars++;
                continue;
            }
            String actorQuery = "SELECT * from stars WHERE name = " + stringValue(actor.replaceAll("[^A-Za-z0-9 ]", "")) + ";";
            ResultSet rs = statement.executeQuery(actorQuery);
            boolean actorExists = rs.next();
            if (!actorExists){
                System.out.println("Actor " + actor + " is missing");
                missingActors.add(actor);
                inconsistentStars++;
                continue;
            }
            String actorId = rs.getString("id");


            String movieTitle = null;
            movieTitle = getTextValue(e, "t");
            if (missingMovies.contains(movieTitle)){
                //System.out.println("Movie " + movieTitle + " is missing");
                //inconsistentMovies++;
                continue;
            }
            //String title = xmlMovieId.get(movieTitle);
            if (movieTitle == null){
                System.out.println("Movie " + movieTitle + " is missing");
                missingMovies.add(movieTitle);
                inconsistentMovies++;
                continue;
            }

            String movieQuery = "SELECT * from movies WHERE title = " + stringValue(movieTitle) + ";";
            rs = statement.executeQuery(movieQuery);
            boolean movieExists = rs.next();
            if (!movieExists){
                missingMovies.add(movieTitle);
                System.out.println("Movie " + movieTitle + " is missing");
                inconsistentMovies++;
                continue;
            }
            String newMovieId = rs.getString("id");

            String castQuery = "INSERT INTO stars_in_movies VALUES(" + stringValue(actorId) + ", " + stringValue(newMovieId) + ");";
            //System.out.println(castQuery);
            statement.executeUpdate(castQuery);
            stars_in_moviesAdded++;
        }
        //System.out.println(xmlCasts.toString());

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
            if (textVal != null){
                textVal.replaceAll("[^A-Za-z0-9' ]", "");
            }
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
        Parser parser = new Parser();

        // call run example
        parser.run();
    }
}
