-- Drop tables if they exist
DROP TABLE IF EXISTS defense_workflows CASCADE;
DROP TABLE IF EXISTS thesis_workflows CASCADE;
DROP TABLE IF EXISTS people CASCADE;

-- Create people table
CREATE TABLE people (
    id BIGSERIAL PRIMARY KEY,  
    name VARCHAR(255) NOT NULL,
    ist_id VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    type VARCHAR(50) NOT NULL CHECK (type IN ('STUDENT', 'TEACHER', 'STAFF', 'COORDINATOR', 'SC'))
);

-- Create thesis workflows table (stores jury members as an array)
CREATE TABLE thesis_workflows (
    id BIGSERIAL PRIMARY KEY,  
    student_id BIGINT NOT NULL REFERENCES people(id) ON DELETE CASCADE,  
    title VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'Proposta de Júri Submetida',
    submission_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    document_path VARCHAR(255),
    jury_president_id BIGINT REFERENCES people(id) ON DELETE SET NULL,  -- ✅ President is unique for a thesis
    jury_member_ids TEXT[],  -- ✅ Store jury member IDs directly as an array
    CONSTRAINT valid_thesis_status CHECK (status IN (
        'Proposta de Júri Submetida', 
        'Aprovado pelo SC', 
        'Presidente do Júri Atribuído', 
        'Documento Assinado', 
        'Submetido ao Fenix'
    ))
);

-- Create defense workflows table with status not nullable
CREATE TABLE defense_workflows (
    id BIGSERIAL PRIMARY KEY,  
    thesis_id BIGINT NOT NULL REFERENCES thesis_workflows(id) ON DELETE CASCADE,  
    student_id BIGINT NOT NULL REFERENCES people(id) ON DELETE CASCADE,  
    status VARCHAR(50) NOT NULL DEFAULT 'Por Agendar',  -- Set NOT NULL with default value
    defense_date TIMESTAMP NULL,      -- Allow NULL defense date
    grade NUMERIC(4,2) CHECK (grade >= 0 AND grade <= 20),
    CONSTRAINT valid_defense_status CHECK (status IN (
        'Defesa Agendada', 
        'Em Revisão', 
        'Submetido ao Fenix',
        'Por Agendar'
    ))
);

-- Create indexes for better performance
CREATE INDEX idx_thesis_status ON thesis_workflows(status);
CREATE INDEX idx_thesis_student_id ON thesis_workflows(student_id);
CREATE INDEX idx_defense_status ON defense_workflows(status);
CREATE INDEX idx_defense_student_id ON defense_workflows(student_id);