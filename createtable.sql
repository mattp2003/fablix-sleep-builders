CREATE DATABASE moviedb;

USE moviedb;

CREATE TABLE movies (
    id VARCHAR(10) PRIMARY KEY DEFAULT '',
    title VARCHAR(100) DEFAULT '',
    year INTEGER NOT NULL,
    director VARCHAR(100) DEFAULT ''
);

CREATE TABLE stars (
    id VARCHAR(10) PRIMARY KEY DEFAULT '',
    name VARCHAR(100) DEFAULT '',
    birthYear INTEGER
);

CREATE TABLE stars_in_movies (
    starId VARCHAR(10) DEFAULT '' UNIQUE,
    movieId VARCHAR(10) DEFAULT '' UNIQUE,
    FOREIGN KEY (starId) REFERENCES stars(id),
    FOREIGN KEY (movieId) REFERENCES movies(id),
);

CREATE TABLE genres (
    id INTEGER AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(32) DEFAULT ''
);

CREATE TABLE genres_in_movies (
    genreId INTEGER NOT NULL,
    movieId VARCHAR(10) DEFAULT '',
    FOREIGN KEY (genreId) REFERENCES genres(id),
    FOREIGN KEY (movieId) REFERENCES movies(id)
);

CREATE TABLE creditcards (
    id VARCHAR(20) PRIMARY KEY DEFAULT '',
    firstName VARCHAR(50) DEFAULT '',
    lastName VARCHAR(50) DEFAULT '',
    expiration DATE NOT NULL
);

CREATE TABLE customers (
    id INTEGER AUTO_INCREMENT PRIMARY KEY,
    firstName VARCHAR(50) DEFAULT '',
    lastName VARCHAR(50) DEFAULT '',
    ccId VARCHAR(20) DEFAULT '',
    address VARCHAR(200) DEFAULT '',
    email VARCHAR(50) DEFAULT '',
    password VARCHAR(20) DEFAULT '',
    FOREIGN KEY (ccID) REFERENCES creditcards(id)
);

CREATE TABLE sales (
    id INTEGER AUTO_INCREMENT PRIMARY KEY,
    customerId INTEGER NOT NULL,
    movieId VARCHAR(10) DEFAULT '',
    saleDate DATE NOT NULL,
    FOREIGN KEY (customerId) REFERENCES customers(id),
    FOREIGN KEY (movieId) REFERENCES movies(id)
);

CREATE TABLE ratings (
    movieId VARCHAR(10) DEFAULT '',
    rating FLOAT NOT NULL,
    numVotes INTEGER NOT NULL,
    FOREIGN KEY (movieId) REFERENCES movies(id)
);

CREATE TABLE employees (
	email VARCHAR(50) PRIMARY KEY NOT NULL,
    password varchar(20) NOT NULL,
    fullname varchar(100) DEFAULT ''
);