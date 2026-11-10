CREATE TABLE students (
                          id UUID PRIMARY KEY,
                          first_name VARCHAR(100) NOT NULL,
                          last_name VARCHAR(100) NOT NULL,
                          identification_number VARCHAR(20) NOT NULL UNIQUE,
                          birth_date DATE NOT NULL,
                          phone VARCHAR(30) NOT NULL,
                          emergency_contact_name VARCHAR(100) NOT NULL,
                          emergency_contact_phone VARCHAR(30) NOT NULL,
                          medical_notes TEXT,
                          status VARCHAR(20) NOT NULL,
                          current_rank_type VARCHAR(10) NOT NULL,
                          current_rank_level INTEGER NOT NULL,
                          current_rank_color VARCHAR(20) NOT NULL,
                          current_rank_order INTEGER NOT NULL,
                          created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE promotion_records (
                                   id UUID PRIMARY KEY,
                                   student_id UUID NOT NULL,
                                   from_rank_type VARCHAR(10) NOT NULL,
                                   from_rank_level INTEGER NOT NULL,
                                   from_rank_color VARCHAR(20) NOT NULL,
                                   from_rank_order INTEGER NOT NULL,
                                   to_rank_type VARCHAR(10) NOT NULL,
                                   to_rank_level INTEGER NOT NULL,
                                   to_rank_color VARCHAR(20) NOT NULL,
                                   to_rank_order INTEGER NOT NULL,
                                   promotion_date DATE NOT NULL,
                                   examiner VARCHAR(100) NOT NULL,
                                   observations TEXT,
                                   created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
                                   CONSTRAINT fk_promotions_student FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE
);

CREATE INDEX idx_students_dni ON students(identification_number);
CREATE INDEX idx_promotions_student_id ON promotion_records(student_id);