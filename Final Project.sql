CREATE DATABASE FreelancerManagementDB;
GO

USE FreelancerManagementDB;
GO

CREATE TABLE Freelancers (
    freelancer_id INT PRIMARY KEY,
    name VARCHAR(100),
    phone VARCHAR(20),
    skill VARCHAR(100)
);

CREATE TABLE Clients (
    client_id INT PRIMARY KEY,
    name VARCHAR(100),
    email VARCHAR(100)
);

CREATE TABLE Projects (
    project_id INT PRIMARY KEY,
    title VARCHAR(200),
    description VARCHAR(500),
    budget DECIMAL(10,2),
    freelancer_id INT,
    client_id INT,

    FOREIGN KEY (freelancer_id) REFERENCES Freelancers(freelancer_id),
    FOREIGN KEY (client_id) REFERENCES Clients(client_id)
);

CREATE TABLE Payments (
    payment_id INT PRIMARY KEY,
    payment_date DATE,
    status VARCHAR(50),
    amount DECIMAL(10,2),
    freelancer_id INT,
    client_id INT,
    project_id INT,

    FOREIGN KEY (freelancer_id) REFERENCES Freelancers(freelancer_id),
    FOREIGN KEY (client_id) REFERENCES Clients(client_id),
    FOREIGN KEY (project_id) REFERENCES Projects(project_id)
);

--------------------------------------------------
-- ALTER TABLE QUERIES
--------------------------------------------------

-- Add new column in Freelancers table
ALTER TABLE Freelancers
ADD experience_years INT;

-- Add new column in Clients table
ALTER TABLE Clients
ADD company_address VARCHAR(200);

-- Modify email column size
ALTER TABLE Clients
ALTER COLUMN email VARCHAR(150);

-- Add CHECK constraint in Payments table
ALTER TABLE Payments
ADD CONSTRAINT chk_amount CHECK (amount > 0);

--------------------------------------------------
-- INSERT DATA
--------------------------------------------------

INSERT INTO Freelancers VALUES
(1, 'Ali Khan', '03001234567', 'Web Development', 3),
(2, 'Sara Ahmed', '03111234567', 'Graphic Design', 2),
(3, 'Usman Tariq', '03221234567', 'Mobile App Development', 4),
(4, 'Hassan Ali', '03331234567', 'AI Developer', 5),
(5, 'Ayesha Malik', '03441234567', 'UI/UX Designer', 2);

INSERT INTO Clients VALUES
(101, 'TechSoft Ltd', 'contact@techsoft.com', 'Lahore'),
(102, 'Creative Studio', 'info@creativestudio.com', 'Karachi'),
(103, 'Digital Hub', 'hello@digitalhub.com', 'Islamabad'),
(104, 'NextGen Solutions', 'support@nextgen.com', 'Faisalabad'),
(105, 'Bright Media', 'info@brightmedia.com', 'Multan');

INSERT INTO Projects VALUES
(201, 'E-commerce Website', 'Online store development', 1500, 1, 101),
(202, 'Logo Design', 'Brand identity design', 300, 2, 102),
(203, 'Mobile App', 'Food delivery app', 2500, 3, 103),
(204, 'AI Chatbot', 'Customer support bot', 3000, 4, 104),
(205, 'UI Redesign', 'Website UI improvement', 1200, 5, 105);

INSERT INTO Payments VALUES
(301, '2026-05-01', 'Completed', 1500, 1, 101, 201),
(302, '2026-05-02', 'Completed', 300, 2, 102, 202),
(303, '2026-05-03', 'Pending', 2500, 3, 103, 203),
(304, '2026-05-04', 'Completed', 3000, 4, 104, 204),
(305, '2026-05-05', 'Pending', 1200, 5, 105, 205);

--------------------------------------------------
-- UPDATE QUERIES
--------------------------------------------------

UPDATE Freelancers
SET skill = 'Full Stack Development'
WHERE freelancer_id = 1;

UPDATE Clients
SET email = 'support@techsoft.com'
WHERE client_id = 101;

UPDATE Projects
SET budget = 2000
WHERE project_id = 201;

UPDATE Payments
SET status = 'Completed'
WHERE payment_id = 303;

UPDATE Freelancers
SET phone = '03000000000'
WHERE freelancer_id = 2;

--------------------------------------------------
-- DELETE QUERIES
--------------------------------------------------

DELETE FROM Payments
WHERE payment_id = 305;

DELETE FROM Projects
WHERE project_id = 205;

DELETE FROM Clients
WHERE client_id = 105;

DELETE FROM Freelancers
WHERE freelancer_id = 5;

--------------------------------------------------
-- SELECT QUERIES
--------------------------------------------------

SELECT * FROM Freelancers;
SELECT * FROM Clients;
SELECT * FROM Projects;
SELECT * FROM Payments;

SELECT 
P.title,
P.budget,
F.name AS Freelancer,
C.name AS Client
FROM Projects P
INNER JOIN Freelancers F 
ON P.freelancer_id = F.freelancer_id
INNER JOIN Clients C 
ON P.client_id = C.client_id;

SELECT 
P.title,
F.name AS Freelancer,
C.name AS Client
FROM Projects P
LEFT JOIN Freelancers F 
ON P.freelancer_id = F.freelancer_id
LEFT JOIN Clients C 
ON P.client_id = C.client_id;

SELECT 
F.name AS Freelancer,
P.title AS Project
FROM Projects P
RIGHT JOIN Freelancers F 
ON P.freelancer_id = F.freelancer_id;

SELECT 
F.name AS Freelancer,
P.title AS Project
FROM Freelancers F
FULL OUTER JOIN Projects P 
ON F.freelancer_id = P.freelancer_id;

SELECT 
F.name AS Freelancer,
C.name AS Client
FROM Freelancers F
CROSS JOIN Clients C;

SELECT 
Pay.payment_id,
C.name AS Client,
F.name AS Freelancer,
Pay.amount,
Pay.status
FROM Payments Pay
INNER JOIN Clients C 
ON Pay.client_id = C.client_id
INNER JOIN Freelancers F 
ON Pay.freelancer_id = F.freelancer_id;

--------------------------------------------------
-- TCL QUERIES
--------------------------------------------------

BEGIN TRANSACTION;

INSERT INTO Freelancers 
VALUES (6, 'Test User', '03001112222', 'Tester', 1);

SAVE TRANSACTION SavePoint1;

INSERT INTO Clients 
VALUES (106, 'Temp Client', 'temp@gmail.com', 'Gujranwala');

ROLLBACK TRANSACTION SavePoint1;

COMMIT TRANSACTION;