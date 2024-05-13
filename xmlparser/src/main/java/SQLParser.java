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

public class SQLParser {
    Map<String, String> xmlMovieId = new HashMap<>();
    Map<String, String> MovieId = new HashMap<>();
    Map<String, String> genreMap = new HashMap<String, String>();
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
            System.out.println("Adding procedures");
            addProcedures(statement);

            System.out.println("Parsing files");
            parseXmlFile();
            System.out.println("Parsing stars");
            parseStars(statement);
            System.out.println("Parsing movies");
            parseMovies(statement);
            System.out.println("Parsing casts");
            parseCasts(statement);

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

        int start = getTotalStars(statement);

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

            String query = "CALL xmlStar(" + stringValue(name) +", " + stringValue(year)+ "); ";
            statement.addBatch(query);
        }
        int[] res  = statement.executeBatch();
        statement.clearBatch();
        starsAdded = getTotalStars(statement) - start;
        duplicateStars = res.length - starsAdded;
    }

    private void parseMovies(Statement statement) throws SQLException {
        // get the document root Element
        Element documentElement = mainsDom.getDocumentElement();

        int genreCount = getTotalGenres(statement);
        int genreMCount = getTotalGenresInMovies(statement);
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

                String query = "CALL xmlMovie(" + stringValue(filmName) +", " + year + ", " + stringValue(directorName) + ");";
                ResultSet rs = statement.executeQuery(query);
                rs.next();
                String result = rs.getString("result");

                if (result.equals("duplicate")){
                    duplicateMovies++;
                    System.out.println("Movie " + filmName + " is a duplicate");
                }
                else{
                    moviesAdded++;
                    //String newId = result;
                    MovieId.put(filmName, result);

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

                            String genreQuery = "CALL xmlGenre(" + stringValue(xmlGenre) +", "  + stringValue(filmName) + ");";
                            statement.addBatch(genreQuery);
                        }
                    }
                }
            }
        }
        System.out.println("Batch executing genres");
        statement.executeBatch();
        statement.clearBatch();
        genresAdded = getTotalGenres(statement) - genreCount;
        genres_in_moviesAdded = getTotalGenresInMovies(statement) - genreMCount;
    }
    private void parseCasts(Statement statement) throws SQLException {
        Element documentElement = castsDom.getDocumentElement();

        int c = getTotalStarsInMovies(statement);
        // get a nodelist of employee Elements, parse each into Employee object
        NodeList nodeList = documentElement.getElementsByTagName("m");
        //System.out.println(nodeList.getLength())
        for (int i = 0; i < nodeList.getLength(); i++) {
            Element e = (Element) nodeList.item(i);

            String actor = null;
            actor = getTextValue(e, "a");
            if (actor != null){
                actor = actor.replaceAll("[^A-Za-z0-9 ]", "");
            }

            String movieTitle = null;
            movieTitle = getTextValue(e, "t");
            String movieId = MovieId.get(movieTitle);
            if (movieId == null){
                System.out.println("Movie " + movieTitle + " is missing");
                continue;
            }
            else{
                movieId = movieId.replaceAll("[^A-Za-z0-9 ]", "");
            }

            String query = "CALL xmlCasts(" + stringValue(actor) +", " + stringValue(movieId) + ");";
            statement.addBatch(query);
        }
        int[] res  = statement.executeBatch();
        stars_in_moviesAdded = getTotalStarsInMovies(statement) - c;
        inconsistentStars = res.length-stars_in_moviesAdded;
        statement.clearBatch();
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
    private void addProcedures(Statement statement){
        String xmlMovies = "\n" +
                "DELIMITER $$\n" +
                "CREATE PROCEDURE xmlMovie(\n" +
                "\tIN movie_title VARCHAR(100), IN movie_year INTEGER,\n" +
                "    IN movie_director VARCHAR(100)\n" +
                ")\n" +
                "BEGIN\n" +
                "\tDECLARE existing_movie_id VARCHAR(10);\n" +
                "    DECLARE max_id VARCHAR(10);\n" +
                "    DECLARE max_value INTEGER;\n" +
                "    DECLARE new_id VARCHAR(10);\n" +
                "    \n" +
                "    SELECT id INTO existing_movie_id FROM movies WHERE title = movie_title LIMIT 1;\n" +
                "    \n" +
                "    IF movie_year IS NOT NULL AND existing_movie_id IS NULL THEN\n" +
                "\t\tSELECT max(id) into max_id FROM movies;\n" +
                "\t\tIF max_id IS NULL THEN\n" +
                "\t\t\tSELECT 0 INTO max_value;\n" +
                "\t\tELSE\n" +
                "\t\t\tSELECT CAST(SUBSTRING(max_id, 3, 8) AS SIGNED) + 1 INTO max_value;\n" +
                "        END IF;\n" +
                "        \n" +
                "        SELECT CONCAT(\"tt\", LPAD(CAST(max_value AS CHAR), 7, '0')) INTO new_id;\n" +
                "        \n" +
                "        INSERT INTO movies VALUES(new_id, movie_title, movie_year, movie_director);\n" +
                "        \n" +
                "        SELECT new_id as result;\n" +
                "\tELSE\n" +
                "        SELECT \"duplicate\" as result;\n" +
                "\tEND IF;\n" +
                "    \n" +
                "END$$\n" +
                "\n" +
                "DELIMITER ;";
        String xmlStar = "DELIMITER $$ CREATE PROCEDURE xmlStar( 	IN star_name VARCHAR(100), IN star_year INTEGER ) BEGIN 	DECLARE existing_star_id VARCHAR(10); DECLARE max_id VARCHAR(10); DECLARE max_value INTEGER; DECLARE new_id VARCHAR(10); SELECT id INTO existing_star_id FROM stars WHERE name = star_name LIMIT 1; IF existing_star_id IS NULL THEN 		SELECT max(id) into max_id FROM stars; 		IF max_id IS NULL THEN 			SELECT 0 INTO max_value; 		ELSE 			SELECT CAST(SUBSTRING(max_id, 3, 8) AS SIGNED) + 1 INTO max_value; END IF; SELECT CONCAT(\"nm\", LPAD(CAST(max_value AS CHAR), 7, '0')) INTO new_id; INSERT INTO stars VALUES(new_id, star_name, star_year); SELECT 0 as result; 	ELSE SELECT 1 as result; 	END IF; END$$ DELIMITER ;";
        String xmlGenre = "DELIMITER $$ CREATE PROCEDURE xmlGenre( \tIN genre_name VARCHAR(100), \tIN movie_title VARCHAR(100) ) BEGIN \tDECLARE existing_movie_id VARCHAR(10); DECLARE existing_genre_id VARCHAR(10); SELECT id INTO existing_movie_id FROM movies WHERE title = movie_title LIMIT 1; SELECT id INTO existing_genre_id FROM genres WHERE name = genre_name LIMIT 1; IF existing_genre_id IS NULL THEN \t\tinsert into genres (name) values (genre_name); SELECT id INTO existing_genre_id FROM genres WHERE name = genre_name LIMIT 1; END IF; IF existing_movie_id IS NOT NULL THEN INSERT INTO genres_in_movies VALUES(existing_genre_id, existing_movie_id); SELECT \"added\" as result; \tELSE SELECT \"inconsistent\" as result; END IF; END $$ DELIMITER ;";
        String xmlCasts = "DELIMITER $$ CREATE PROCEDURE xmlCasts( \tIN star_name VARCHAR(100), \tIN movie_id VARCHAR(10) ) BEGIN \tDECLARE existing_movie_id VARCHAR(10); DECLARE existing_star_id VARCHAR(10); SELECT id INTO existing_star_id FROM stars WHERE name = star_name LIMIT 1; IF existing_star_id IS NOT NULL AND movie_id IS NOT NULL THEN INSERT INTO stars_in_movies VALUES(existing_star_id, movie_id); \tEND IF; END $$ DELIMITER ;";

        System.out.println(xmlMovies);
        try {
            statement.execute(xmlMovies);
        }
        catch (SQLException e) {
            e.printStackTrace();
        }



    }

    public static void main(String[] args) throws Exception{
        // create an instance
        SQLParser parser = new SQLParser();

        // call run example
        parser.run();
    }
}
