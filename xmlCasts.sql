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
