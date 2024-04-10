CREATE DATABASE moviedb;

CREATE TABLE moviedb.movies(
	id varchar(10) DEFAULT '',
    title varchar(100) DEFAULT '',
    year int NOT NULL,
    director varchar(100) DEFAULT '',
    PRIMARY KEY (id)
);

CREATE TABLE moviedb.stars(
	id varchar(10) DEFAULT '',
    name varchar(100) DEFAULT '',
    birthYear int,
    PRIMARY KEY (id)
);

CREATE TABLE moviedb.stars_in_movies(
	starId varchar(10) DEFAULT '',
    FOREIGN KEY (starId) REFERENCES moviedb.stars(id),
    
    movieId varchar(10) DEFAULT '',
    FOREIGN KEY (movieId) REFERENCES moviedb.movies(id)
);

CREATE TABLE moviedb.genres(
	id int NOT NULL AUTO_INCREMENT,
    name varchar(32) DEFAULT '',
    PRIMARY KEY (id)
);

CREATE TABLE moviedb.genres_in_movies(
	genreId int NOT NULL,
    FOREIGN KEY (genreId) REFERENCES moviedb.genres(id),
    
    movieId varchar(10) DEFAULT '',
    FOREIGN KEY (movieId) REFERENCES moviedb.movies(id)
);

CREATE TABLE moviedb.creditcards(
	id varchar(20) DEFAULT '',
    firstName varchar(50) DEFAULT '',
    lastName varchar(50) DEFAULT '',
    expiration date NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE moviedb.customers(
	id int NOT NULL AUTO_INCREMENT,
    firstName varchar(50) DEFAULT '',
    lastName varchar(50) DEFAULT '',
    ccId varchar(20) DEFAULT '',
    FOREIGN KEY (ccId) REFERENCES moviedb.creditcards(id),
    address varchar(200) DEFAULT '',
    email varchar(50) DEFAULT '',
    password varchar(50) DEFAULT '',
    PRIMARY KEY (id)
);

CREATE TABLE moviedb.sales(
	id int NOT NULL AUTO_INCREMENT,
    PRIMARY KEY (id),
    customerId int NOT NULL,
    FOREIGN KEY (customerId) REFERENCES moviedb.customers(id),
    movieId varchar(10) DEFAULT '',
    FOREIGN KEY (movieId) REFERENCES moviedb.movies(id),
    saleDate date NOT NULL
);

CREATE TABLE moviedb.ratings(
	movieId varchar(50) DEFAULT '',
    FOREIGN KEY (movieId) REFERENCES moviedb.movies(id),
    rating float NOT NULL,
    numVotes int NOT NULL
);