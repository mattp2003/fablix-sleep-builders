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