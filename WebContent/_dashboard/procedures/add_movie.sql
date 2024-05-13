USE moviedb;

DELIMITER $$

CREATE PROCEDURE add_movie(
    IN new_movie_id VARCHAR(10),
    IN movie_title VARCHAR(100),
    IN movie_year INTEGER,
    IN movie_director VARCHAR(100),
    IN new_star_id VARCHAR(10),
    IN star_name VARCHAR(100),
    IN new_genre_id INTEGER,
    IN genre_name VARCHAR(32),
    OUT out_movie_id VARCHAR(10),
    OUT out_star_id VARCHAR(10),
    OUT out_genre_id INTEGER
)
BEGIN
    DECLARE existing_star_id VARCHAR(10);
    DECLARE existing_genre_id INTEGER;
    DECLARE existing_movie_id VARCHAR(10);
    DECLARE new_genre_id INTEGER;
    DECLARE star_id VARCHAR(10);
    DECLARE genre_id INTEGER;
    DECLARE movie_id VARCHAR(10);

    -- Check if the movie already exists
    SELECT id INTO existing_movie_id FROM movies WHERE title = movie_title AND year = movie_year AND director = movie_director LIMIT 1;

    -- If the movie does not exist, insert it
    IF existing_movie_id IS NULL THEN
        INSERT INTO movies (id, title, year, director) VALUES (new_movie_id, movie_title, movie_year, movie_director);
    END IF;

    -- Insert the star if they do not exist
    SELECT id INTO existing_star_id FROM stars WHERE name = star_name LIMIT 1;
    IF existing_star_id IS NULL THEN
        INSERT INTO stars (id, name) VALUES (new_star_id, star_name);
    END IF;

    -- Insert the genre if it does not exist
    SELECT id INTO existing_genre_id FROM genres WHERE name = genre_name LIMIT 1;
    IF existing_genre_id IS NULL THEN
        INSERT INTO genres (name) VALUES (genre_name);
    END IF;
	
    IF existing_star_id is NULL THEN
		SELECT new_star_id INTO star_id;
	ELSE
		SELECT existing_star_id INTO star_id;
	END IF;
    
    IF existing_genre_id is NULL THEN
		SELECT new_genre_id INTO genre_id;
	ELSE 
		SELECT existing_genre_id INTO genre_id;
	END IF;
    
    IF existing_movie_id is NULL THEN
		SELECT new_movie_id INTO movie_id;
	ELSE 
		SELECT existing_movie_id INTO movie_id;
    END IF;
    
	INSERT INTO stars_in_movies (starId, movieId) VALUES (star_id, movie_id);
	INSERT INTO genres_in_movies (genreId, movieId) VALUES (genre_id, movie_id);

    SELECT id INTO out_movie_id FROM movies WHERE title = movie_title AND year = movie_year AND director = movie_director LIMIT 1;
    SELECT id INTO out_star_id FROM stars WHERE name = star_name LIMIT 1;
    SELECT id INTO out_genre_id FROM genres WHERE name = genre_name LIMIT 1;

END$$

DELIMITER ;