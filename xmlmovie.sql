
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