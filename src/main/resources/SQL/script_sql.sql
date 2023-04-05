create database api;

CREATE TABLE user (
  id BIGINT NOT NULL AUTO_INCREMENT,
  email VARCHAR(250) UNIQUE,
  password VARCHAR(250),
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id) 
);

use api;
select * from user;

INSERT INTO user (email, password) VALUES ("fernando@gmail.com","fe");