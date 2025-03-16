-- Populate the database with test data

-- Clear existing data
TRUNCATE TABLE defense_workflows CASCADE;
TRUNCATE TABLE thesis_workflows CASCADE;
TRUNCATE TABLE people CASCADE;

-- Reset sequences
ALTER SEQUENCE people_id_seq RESTART WITH 1;
ALTER SEQUENCE thesis_workflows_id_seq RESTART WITH 1;
ALTER SEQUENCE defense_workflows_id_seq RESTART WITH 1;

-- Insert people (users)
-- Administrators
INSERT INTO people (name, ist_id, email, type) VALUES
('Admin User', 'ist1000001', 'admin@tecnico.ulisboa.pt', 'STAFF');

-- Coordinators
INSERT INTO people (name, ist_id, email, type) VALUES
('Prof. Luís Costa', 'ist1300001', 'luis.costa@tecnico.ulisboa.pt', 'COORDINATOR');

-- Scientific Committee
INSERT INTO people (name, ist_id, email, type) VALUES
('Dr. Carlos Mendes', 'ist1200001', 'carlos.mendes@tecnico.ulisboa.pt', 'SC'),
('Dr. Ana Soares', 'ist1200002', 'ana.soares@tecnico.ulisboa.pt', 'SC');

-- Professors
INSERT INTO people (name, ist_id, email, type) VALUES
('Prof. João Santos', 'ist1100001', 'joao.santos@tecnico.ulisboa.pt', 'TEACHER'),
('Prof. Maria Oliveira', 'ist1100002', 'maria.oliveira@tecnico.ulisboa.pt', 'TEACHER'),
('Prof. Carlos Pereira', 'ist1100003', 'carlos.pereira@tecnico.ulisboa.pt', 'TEACHER'),
('Prof. Teresa Almeida', 'ist1100004', 'teresa.almeida@tecnico.ulisboa.pt', 'TEACHER'),
('Prof. Ricardo Silva', 'ist1100005', 'ricardo.silva@tecnico.ulisboa.pt', 'TEACHER');

-- Staff
INSERT INTO people (name, ist_id, email, type) VALUES
('Sofia Rodrigues', 'ist1400001', 'sofia.rodrigues@tecnico.ulisboa.pt', 'STAFF'),
('Miguel Lopes', 'ist1400002', 'miguel.lopes@tecnico.ulisboa.pt', 'STAFF');

-- Students - NOW WE ADD 7 STUDENTS
INSERT INTO people (name, ist_id, email, type) VALUES
('Ana Silva', 'ist1123456', 'ana.silva@tecnico.ulisboa.pt', 'STUDENT'),
('Bruno Costa', 'ist1789012', 'bruno.costa@tecnico.ulisboa.pt', 'STUDENT'),
('Carla Martins', 'ist1345678', 'carla.martins@tecnico.ulisboa.pt', 'STUDENT'),
('Daniel Ferreira', 'ist1234567', 'daniel.ferreira@tecnico.ulisboa.pt', 'STUDENT'),
('Eva Lopes', 'ist1567890', 'eva.lopes@tecnico.ulisboa.pt', 'STUDENT'),
('Fernando Dias', 'ist1901234', 'fernando.dias@tecnico.ulisboa.pt', 'STUDENT'),
('Gabriela Santos', 'ist1123789', 'gabriela.santos@tecnico.ulisboa.pt', 'STUDENT');

