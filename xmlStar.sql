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