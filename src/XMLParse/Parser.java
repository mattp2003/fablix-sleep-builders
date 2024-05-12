package XMLParse;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class Parser {
    Map<String, String> genreMap = new HashMap<String, String>();
    Map<String, Integer> genres2id = new HashMap<>();
    Map<String, String> movies2id = new HashMap<>();
    int lastMovieId = -1;
    Map<String, String> stars2id = new HashMap<>();
    int lastStarId = -1;

    Document mainsDom;
    Document actorsDom;
    Document castsDom;

    Map<String, Movie> xmlMovies = new HashMap<>();
    Map<String, String> xmlStarsDob = new HashMap<>();
    Map<String, ArrayList<String>> xmlCasts = new HashMap<>();

    ArrayList<String> inconsistencies = new ArrayList<>();

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
        genreMap.put("actn", "Biography");


        String loginUser = "mytestuser";
        String loginPasswd = "My6$Password";
        String loginUrl = "jdbc:mysql://localhost:3306/moviedb";
        Connection connection = DriverManager.getConnection(loginUrl, loginUser, loginPasswd);

        //load genres
        queryGenres(connection);

        //load movies
        queryMovies(connection);

        //load stars
        queryStars(connection);

        // parse the xml file and get the dom object for all files
        parseXmlFile();

        parseMovies();
        parseStars();
        parseCasts();


        Map<String, String> newMovieKey = new HashMap<>();
        Map<String, String> newStarKey = new HashMap<>();

        try (Statement statement = connection.createStatement()) {

            //Add new movies to database
            for (Map.Entry<String, Movie> entry : xmlMovies.entrySet()) {
                Movie movie = entry.getValue();
                String xmlId = entry.getKey();
                String title = movie.getTitle();
                if (title == null || title.isEmpty() || title.equals("null")){
                    inconsistencies.add(movie.getDirector() + "'s movie with id " + xmlId + " was not added as there is no title");
                    continue;
                }
                if (movies2id.containsKey(title)){
                    newMovieKey.put(xmlId, movies2id.get(title));
                }
                else{
                    lastMovieId+=1;
                    String newId = "tt" + String.format("%07d",lastMovieId);
                    newMovieKey.put(xmlId, newId);

                    String year = "null";
                    if (movie.getYear() != null){
                        year = sqlValue(movie.getYear());
                    }
                    String director = "null";
                    if (movie.getDirector() != null){
                        director = sqlValue(movie.getDirector());
                    }
                    newId = sqlValue(newId);
                    title = sqlValue(title);
                    //System.out.println(newId + ", "  + title + ", " + year + ", " + director);
                    String query = "INSERT INTO movies VALUES(" + newId + ", "  + title + ", " + year + ", " + director + ");";
                    //System.out.println(query);

                    statement.addBatch(query);
                }
            }
            System.out.println("Adding new movies");
            statement.executeBatch();
            statement.clearBatch();
            System.out.println("Finished adding new movies");

            //add genres
            for (Map.Entry<String, Movie> entry : xmlMovies.entrySet()){
                //also need to add genres of the movie
                Movie movie = entry.getValue();
                String id = newMovieKey.get(entry.getKey());

                if (id == null || id.equals("null")){
                    inconsistencies.add(entry.getKey() + " has no ID in database and was not added");
                    continue;
                }

                id = sqlValue(id);

                for (String genre: movie.getGenres()){
                    String genreQuery = "INSERT INTO genres_in_movies VALUES(" + genres2id.get(genre) + ", " + id + ");";
                    //System.out.println(genreQuery);
                    statement.addBatch(genreQuery);
                    //System.out.println(genreQuery);
                }

            }
            System.out.println("Adding new movie genres");
            statement.executeBatch();
            statement.clearBatch();
            System.out.println("Finished adding new movie genres");


            //add new actors
            for (Map.Entry<String, String> entry : xmlStarsDob.entrySet()){
                String name = entry.getKey();
                String year = "null";
                if (entry.getValue() != null){
                    year = sqlValue(entry.getValue());
                }

                if (stars2id.containsKey(name)){
                    newStarKey.put(name, stars2id.get(name));
                }
                else{
                    lastStarId+=1;
                    String newId = "nm" + String.format("%07d",lastStarId);
                    newStarKey.put(name, newId);

                    String query = "INSERT INTO stars VALUES(" + sqlValue(newId) + ", "  + sqlValue(name) + ", " + year + ");";
                    //System.out.println(query);
                    statement.addBatch(query);
                }
            }

            System.out.println("Adding new stars");
            statement.executeBatch();
            statement.clearBatch();
            System.out.println("Finished adding new stars");

            //add the casts
            for (Map.Entry<String, ArrayList<String>> entry : xmlCasts.entrySet()){
                String xmlMovieId = entry.getKey();
                ArrayList<String> actors = entry.getValue();

                String newMovieId = newMovieKey.get(xmlMovieId);
                for (String actor : actors){
                    String newStarId = newStarKey.get(actor);
                    //System.out.println(newStarId);
                    //System.out.println(newMovieId);
                    if (newStarId == null || newStarId.equals("null")){
                        inconsistencies.add(xmlMovieId + "'s actor"  + actor + " has no ID in database and was not added");
                        continue;
                    }
                    if (newMovieId == null || newMovieId.equals("null")){
                        inconsistencies.add(actor + "'s casting in " + xmlMovieId + " was not added as the movie cannot be found in database");
                        continue;
                    }

                    String query = "INSERT INTO stars_in_movies VALUES(" + sqlValue(newStarId) + ", " + sqlValue(newMovieId) + ");";
                    //System.out.println(query);
                    statement.addBatch(query);
                }
            }
            System.out.println("Adding new stars_in_movies");
            statement.executeBatch();
            System.out.println("Finished adding stars_in_movies");
        }
        for (String i : inconsistencies){
            System.out.println(i);
        }
    }
    private String sqlValue(String s){
        return "\"" + s + "\"";
    }

    private void queryGenres(Connection conn) throws SQLException {
        System.out.println("Querying Genres");
        try (Statement statement = conn.createStatement()){
            String query = "SELECT id, name from genres;";

            ResultSet resultSet = statement.executeQuery(query);

            int newId = 0;
            while (resultSet.next()) {
                genres2id.put(resultSet.getString("name"), resultSet.getInt("id"));
                newId = Math.max(resultSet.getInt("id"), newId);
            }

            for (Map.Entry<String, String> entry : genreMap.entrySet()) {
                String k = entry.getKey();
                String v = entry.getValue();
                if (!genres2id.containsKey(v)){
                    newId+=1;
                    genres2id.put(v, newId);
                    String addGenre = "INSERT INTO genres VALUES (" + newId + ", '" + v + "');";
                    statement.addBatch(addGenre);
                }
            }
            //System.out.println(statement.toString());
            statement.executeBatch();
        }
        catch (Exception e){
            System.out.println(e.toString());
            System.out.println("Connection failed");
        }
    }

    private void queryMovies(Connection conn){
        System.out.println("Querying Movies");
        try (Statement statement = conn.createStatement()){
            String query = "SELECT title, id from movies;";

            ResultSet resultSet = statement.executeQuery(query);

            while (resultSet.next()) {
                movies2id.put(resultSet.getString("title"), resultSet.getString("id"));
                lastMovieId = Math.max(Integer.parseInt(resultSet.getString("id").strip().replaceAll("[a-z]", "")), lastMovieId);
            }
        }
        catch (Exception e){
            System.out.println(e.toString());
            System.out.println("Connection failed");
        }
    }
    private void queryStars(Connection conn){
        System.out.println("Querying stars");
        try (Statement statement = conn.createStatement()){
            String query = "SELECT name, id from stars;";

            ResultSet resultSet = statement.executeQuery(query);

            while (resultSet.next()) {
                stars2id.put(resultSet.getString("name"), resultSet.getString("id"));
                lastStarId = Math.max(Integer.parseInt(resultSet.getString("id").strip().replaceAll("[a-z]", "")), lastStarId);
            }
        }
        catch (Exception e){
            System.out.println(e.toString());
            System.out.println("Connection failed");
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

    private void parseMovies() {
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

                NodeList genres = film.getElementsByTagName("cats");
                ArrayList<String> genreNames = new ArrayList<>();
                for (int k = 0; k < genres.getLength(); k++) {
                    Element genre = (Element) genres.item(k);
                    String genreName = getTextValue(genre, "cat");
                    //System.out.println(genreName);
                    if (genreName != null && genreMap.containsKey(genreName.toLowerCase())) {

                        genreNames.add(genreMap.get(genreName));
                    }
                }
                //System.out.println(filmName + " " + year + " " + directorName);
                Movie movie = new Movie(filmName, year, directorName, genreNames);
                xmlMovies.put(id, movie);
            }
        }
    }

    private void parseStars(){
        // get the document root Element
        Element documentElement = actorsDom.getDocumentElement();

        // get a nodelist of employee Elements, parse each into Employee object
        NodeList nodeList = documentElement.getElementsByTagName("actor");
        for (int i = 0; i < nodeList.getLength(); i++) {

            // get the employee element
            Element star = (Element) nodeList.item(i);

            String name = null;
            name = getTextValue(star, "stagename").replaceAll("[^A-Za-z0-9 ]", "");
            String year = null;
            year = getTextValue(star, "dob");
           //System.out.println(name + " " + year);

            xmlStarsDob.put(name, year);
        }
    }

    private void parseCasts(){
        Element documentElement = castsDom.getDocumentElement();

        // get a nodelist of employee Elements, parse each into Employee object
        NodeList nodeList = documentElement.getElementsByTagName("m");
        //System.out.println(nodeList.getLength());
        for (int i = 0; i < nodeList.getLength(); i++) {
            Element e = (Element) nodeList.item(i);

            String actor = null;
            actor = getTextValue(e, "a");

            String movieId = null;
            movieId = getTextValue(e, "f");

            if (xmlCasts.containsKey(movieId)){
                xmlCasts.get(movieId).add(actor);
            }
            else{
                xmlCasts.put(movieId, new ArrayList<>());
                xmlCasts.get(movieId).add(actor);
            }
        }
        //System.out.println(xmlCasts.toString());

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
            String year = getTextValue(ele, "year").replaceAll("[a-z]", "0").strip();
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
