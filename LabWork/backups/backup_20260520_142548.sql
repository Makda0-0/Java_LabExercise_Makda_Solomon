-- Database Backup: Wed May 20 14:25:48 EAT 2026

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS students;
CREATE TABLE students (id INT PRIMARY KEY, name VARCHAR(100), department VARCHAR(100), section VARCHAR(50), year INT);
INSERT INTO students VALUES (101, 'John', 'Computer Science', 'A', 2024);
INSERT INTO students VALUES (102, 'Jack', 'Electrical Engineering', 'B', 2023);
INSERT INTO students VALUES (103, 'Leah', 'Mechanical Engineering', 'A', 2025);
INSERT INTO students VALUES (104, 'Sarah', 'Computer Science', 'C', 2024);
INSERT INTO students VALUES (123, 'Aman', 'SWE', 'A', 2023);
INSERT INTO students VALUES (301, 'Khan', 'Electrical', 'D', 2024);
INSERT INTO students VALUES (201, 'Kengi', 'SWE', 'D', 2023);

DROP TABLE IF EXISTS teachers;
CREATE TABLE teachers (id INT PRIMARY KEY, name VARCHAR(100), department VARCHAR(100));
INSERT INTO teachers VALUES (201, 'Dr. Smith', 'Computer Science');
INSERT INTO teachers VALUES (202, 'Prof. Johnson', 'Electrical Engineering');

SET FOREIGN_KEY_CHECKS = 1;
