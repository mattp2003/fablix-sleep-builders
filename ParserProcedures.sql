
DELIMITER $$
CREATE PROCEDURE xmlMovie(
	IN movie_title VARCHAR(100),
    IN movie_year INTEGER,
    IN movie_director VARCHAR(100)
)
BEGIN
	DECLARE existing_movie_id VARCHAR(10);
    DECLARE max_id VARCHAR(10);
    DECLARE max_value INTEGER;
    DECLARE new_id VARCHAR(10);
    
    SELECT id INTO existing_movie_id FROM movies WHERE title = movie_title LIMIT 1;
    
    IF movie_year IS NOT NULL AND existing_movie_id IS NULL THEN
		SELECT max(id) into max_id FROM movies;
		IF max_id IS NULL THEN
			SELECT 0 INTO max_value;
		ELSE
			SELECT CAST(SUBSTRING(max_id, 3, 8) AS SIGNED) + 1 INTO max_value;
        END IF;
        
        SELECT CONCAT("tt", LPAD(CAST(max_value AS CHAR), 7, '0')) INTO new_id;
        
        INSERT INTO movies VALUES(new_id, movie_title, movie_year, movie_director);
        
        SELECT new_id as result;
	ELSE
        SELECT "duplicate" as result;
	END IF;
    
END$$

DELIMITER ;

DELIMITER $$
CREATE PROCEDURE xmlStar(
	IN star_name VARCHAR(100),
    IN star_year INTEGER
)
BEGIN
	DECLARE existing_star_id VARCHAR(10);
    DECLARE max_id VARCHAR(10);
    DECLARE max_value INTEGER;
    DECLARE new_id VARCHAR(10);
    
    SELECT id INTO existing_star_id FROM stars WHERE name = star_name LIMIT 1;
    
    IF existing_star_id IS NULL THEN
		SELECT max(id) into max_id FROM stars;
		IF max_id IS NULL THEN
			SELECT 0 INTO max_value;
		ELSE
			SELECT CAST(SUBSTRING(max_id, 3, 8) AS SIGNED) + 1 INTO max_value;
        END IF;
        
        SELECT CONCAT("nm", LPAD(CAST(max_value AS CHAR), 7, '0')) INTO new_id;
        
        INSERT INTO stars VALUES(new_id, star_name, star_year);
        
        SELECT 0 as result;
	ELSE
        SELECT 1 as result;
	END IF;
    
END$$

DELIMITER ;

DELIMITER $$
CREATE PROCEDURE xmlGenre(
	IN genre_name VARCHAR(100),
	IN movie_title VARCHAR(100)
)
BEGIN
	DECLARE existing_movie_id VARCHAR(10);
    DECLARE existing_genre_id VARCHAR(10);
    
    SELECT id INTO existing_movie_id FROM movies WHERE title = movie_title LIMIT 1;
    SELECT id INTO existing_genre_id FROM genres WHERE name = genre_name LIMIT 1;
    
    IF existing_genre_id IS NULL THEN
		insert into genres (name) values (genre_name);
        SELECT id INTO existing_genre_id FROM genres WHERE name = genre_name LIMIT 1;
    END IF;
    
    IF existing_movie_id IS NOT NULL THEN
        
        INSERT INTO genres_in_movies VALUES(existing_genre_id, existing_movie_id);
        SELECT "added" as result;
	ELSE
        SELECT "inconsistent" as result;
	END IF;
    
END$$

DELIMITER ;

DELIMITER $$
CREATE PROCEDURE xmlCasts(
	IN star_name VARCHAR(100),
	IN movie_id VARCHAR(10)
)
BEGIN
	DECLARE existing_movie_id VARCHAR(10);
    DECLARE existing_star_id VARCHAR(10);
    
    SELECT id INTO existing_star_id FROM stars WHERE name = star_name LIMIT 1;
    
    IF existing_star_id IS NOT NULL AND movie_id IS NOT NULL THEN
        
        INSERT INTO stars_in_movies VALUES(existing_star_id, movie_id);
	END IF;
    
END$$

DELIMITER ;

