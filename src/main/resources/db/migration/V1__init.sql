-- Tạo database nếu chưa có
CREATE DATABASE MedConnectDB;
GO

USE MedConnectDB;
GO

-- Table for Users (handles registration, login for Patients, Doctors, Admins)
CREATE TABLE users (
                       user_id INT IDENTITY(1,1) PRIMARY KEY,
                       email NVARCHAR(255) UNIQUE NOT NULL,
                       password_hash NVARCHAR(255) NOT NULL,  -- Store hashed passwords
                       role NVARCHAR(50) NOT NULL,  -- From actors
                       verified BIT DEFAULT 0 NOT NULL,  -- From UC1 verification
                       blocked BIT DEFAULT 0 NOT NULL,  -- From UC2 login checks
                       created_at DATETIME DEFAULT GETDATE() NOT NULL,
                       last_login DATETIME NULL,
                       failed_login_attempts INT DEFAULT 0 NOT NULL,  -- Error E5 khóa tài khoản nhập sai 5 lần
                       lockout_end_time DATETIME NULL
);
GO

CREATE TABLE tokens (
                        token_id INT IDENTITY(1,1) PRIMARY KEY,
                        user_id INT NOT NULL,
                        token NVARCHAR(255) NOT NULL UNIQUE,
                        token_type NVARCHAR(50) NOT NULL, -- Phân biệt loại token
                        expiry_date_time DATETIME NOT NULL,
                        used BIT DEFAULT 0 NOT NULL,
                        created_at DATETIME DEFAULT GETDATE() NOT NULL,
                        CONSTRAINT fk_tokens_users FOREIGN KEY (user_id) REFERENCES users(user_id)
);
GO

-- Table for Specializations (managed by Admin in UC6)
CREATE TABLE specializations (
                                 specialization_id INT IDENTITY(1,1) PRIMARY KEY,
                                 name NVARCHAR(100) UNIQUE NOT NULL,  -- e.g., Cardiology, Dermatology
                                 description NVARCHAR(255) NULL,
                                 created_by INT NULL,  -- FK to users (Admin)
                                 created_at DATETIME DEFAULT GETDATE() NOT NULL,
                                 updated_at DATETIME NULL,
                                 CONSTRAINT fk_specializations_users FOREIGN KEY (created_by) REFERENCES users(user_id)
);
GO

-- Table for Doctors (profile completion in UC3, approval in UC5)
-- Sửa: doctor_id là IDENTITY PK riêng, thêm user_id FK. specialization_id NULLable cho register cơ bản.
CREATE TABLE doctors (
                         doctor_id INT IDENTITY(1,1) PRIMARY KEY,  -- ID tự sinh riêng
                         user_id INT NOT NULL,  -- FK mới đến users
                         specialization_id INT NULL,  -- NULLable, cập nhật sau
                         experience_years INT NULL,  -- NULLable, cập nhật sau
                         license_number NVARCHAR(100) NULL,  -- NULLable, cập nhật sau
                         photo_path NVARCHAR(255) NULL,  -- Path to uploaded photo
                         status NVARCHAR(50) DEFAULT 'Pending' NOT NULL,
                         rejection_reason NVARCHAR(500) NULL,
                         created_at DATETIME DEFAULT GETDATE() NOT NULL,
                         updated_at DATETIME NULL,
                         CONSTRAINT fk_doctors_users FOREIGN KEY (user_id) REFERENCES users(user_id),
                         CONSTRAINT fk_doctors_specializations FOREIGN KEY (specialization_id) REFERENCES specializations(specialization_id)
);
GO

-- Table for Doctor Documents (credentials uploads in UC3)
CREATE TABLE doctor_documents (
                                  document_id INT IDENTITY(1,1) PRIMARY KEY,
                                  doctor_id INT NOT NULL,
                                  file_path NVARCHAR(255) NOT NULL,  -- Path to scanned credentials
                                  document_type NVARCHAR(100) NOT NULL,  -- e.g., License, Certificate
                                  uploaded_at DATETIME DEFAULT GETDATE() NOT NULL,
                                  CONSTRAINT fk_doctor_documents_doctors FOREIGN KEY (doctor_id) REFERENCES doctors(doctor_id)
);
GO

-- Table for Patients (profile management in UC4)
-- Sửa: patient_id là IDENTITY PK riêng, thêm user_id FK.
CREATE TABLE patients (
                          patient_id INT IDENTITY(1,1) PRIMARY KEY,  -- ID tự sinh riêng
                          user_id INT NOT NULL,  -- FK mới đến users
                          full_name NVARCHAR(255) NOT NULL,
                          phone_number NVARCHAR(20) NULL,
                          address NVARCHAR(500) NULL,
                          date_of_birth DATE NULL,
                          medical_history NVARCHAR(MAX) NULL,  -- Allergies, etc., from UC4
                          created_at DATETIME DEFAULT GETDATE() NOT NULL,
                          updated_at DATETIME NULL,
                          CONSTRAINT fk_patients_users FOREIGN KEY (user_id) REFERENCES users(user_id)
);
GO

-- Các bảng còn lại giữ nguyên
-- Table for Schedules (weekly schedules managed in UC7)
CREATE TABLE schedules (
                           schedule_id INT IDENTITY(1,1) PRIMARY KEY,
                           doctor_id INT NOT NULL,
                           day_of_week INT NOT NULL CHECK (day_of_week BETWEEN 1 AND 7),  -- 1=Sunday, 7=Saturday
                           start_time TIME NOT NULL,
                           end_time TIME NOT NULL,
                           consultation_type NVARCHAR(50) NOT NULL,  -- From UC7
                           active BIT DEFAULT 1 NOT NULL,
                           created_at DATETIME DEFAULT GETDATE() NOT NULL,
                           updated_at DATETIME NULL,
                           CONSTRAINT fk_schedules_doctors FOREIGN KEY (doctor_id) REFERENCES doctors(doctor_id),
                           CONSTRAINT chk_end_time_after_start CHECK (end_time > start_time)
);
GO

-- Table for Appointments (booking in UC10, payment in UC11, video in UC13)
CREATE TABLE appointments (
                              appointment_id INT IDENTITY(1,1) PRIMARY KEY,
                              patient_id INT NOT NULL,
                              doctor_id INT NOT NULL,
                              schedule_id INT NULL,  -- Link to weekly schedule template if applicable
                              appointment_date_time DATETIME NOT NULL,  -- Specific date and time
                              consultation_type NVARCHAR(50) NOT NULL,
                              status NVARCHAR(50) NOT NULL,  -- From flows
                              payment_status NVARCHAR(50) NOT NULL,
                              video_call_link NVARCHAR(500) NULL,  -- Generated by ZEGOCLOUD
                              duration_minutes INT NULL,  -- Logged after completion
                              created_at DATETIME DEFAULT GETDATE() NOT NULL,
                              updated_at DATETIME NULL,
                              CONSTRAINT fk_appointments_patients FOREIGN KEY (patient_id) REFERENCES patients(patient_id),
                              CONSTRAINT fk_appointments_doctors FOREIGN KEY (doctor_id) REFERENCES doctors(doctor_id),
                              CONSTRAINT fk_appointments_schedules FOREIGN KEY (schedule_id) REFERENCES schedules(schedule_id)
);
GO

-- Table for Payments (handled in UC11 with VNPAY)
CREATE TABLE payments (
                          payment_id INT IDENTITY(1,1) PRIMARY KEY,
                          appointment_id INT NOT NULL,
                          amount DECIMAL(18,2) NOT NULL,
                          payment_method NVARCHAR(50) NOT NULL,
                          transaction_id NVARCHAR(100) NULL,  -- From gateway
                          status NVARCHAR(50) NOT NULL,
                          paid_at DATETIME NULL,
                          created_at DATETIME DEFAULT GETDATE() NOT NULL,
                          CONSTRAINT fk_payments_appointments FOREIGN KEY (appointment_id) REFERENCES appointments(appointment_id)
);
GO

-- Table for Consultation Documents (summaries/prescriptions in UC14)
CREATE TABLE consultation_documents (
                                        document_id INT IDENTITY(1,1) PRIMARY KEY,
                                        appointment_id INT NOT NULL,
                                        document_type NVARCHAR(50) NOT NULL,
                                        content NVARCHAR(MAX) NOT NULL,  -- Text content or path if file
                                        created_at DATETIME DEFAULT GETDATE() NOT NULL,
                                        CONSTRAINT fk_consultation_documents_appointments FOREIGN KEY (appointment_id) REFERENCES appointments(appointment_id)
);
GO

-- Table for Reviews (ratings/reviews in UC15)
CREATE TABLE reviews (
                         review_id INT IDENTITY(1,1) PRIMARY KEY,
                         appointment_id INT NOT NULL,
                         rating INT NOT NULL CHECK (rating BETWEEN 1 AND 5),
                         comment NVARCHAR(1000) NULL,
                         anonymous BIT DEFAULT 0 NOT NULL,
                         created_at DATETIME DEFAULT GETDATE() NOT NULL,
                         CONSTRAINT fk_reviews_appointments FOREIGN KEY (appointment_id) REFERENCES appointments(appointment_id)
);
GO

-- Table for Notifications (received in UC12)
CREATE TABLE notifications (
                               notification_id INT IDENTITY(1,1) PRIMARY KEY,
                               user_id INT NOT NULL,
                               message NVARCHAR(500) NOT NULL,
                               notification_type NVARCHAR(50) NOT NULL,  -- e.g., AppointmentConfirmation, Reminder
    [read] BIT DEFAULT 0 NOT NULL,
                               sent_at DATETIME DEFAULT GETDATE() NOT NULL,
                               CONSTRAINT fk_notifications_users FOREIGN KEY (user_id) REFERENCES users(user_id)
);
GO

-- Table for Activity Logs (mentioned in various UC for logging)
CREATE TABLE activity_logs (
                               log_id INT IDENTITY(1,1) PRIMARY KEY,
                               user_id INT NULL,
                               action NVARCHAR(255) NOT NULL,  -- e.g., 'Login', 'AppointmentBooked'
                               details NVARCHAR(500) NULL,
                               timestamp DATETIME DEFAULT GETDATE() NOT NULL,
                               CONSTRAINT fk_activity_logs_users FOREIGN KEY (user_id) REFERENCES users(user_id)
);
GO

-- Indexes for performance (based on common queries like search in UC8)
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_doctors_user_id ON doctors(user_id);  -- Index mới cho FK
CREATE INDEX idx_doctors_specialization_id ON doctors(specialization_id);
CREATE INDEX idx_doctors_status ON doctors(status);
CREATE INDEX idx_schedules_doctor_id ON schedules(doctor_id);
CREATE INDEX idx_appointments_patient_id ON appointments(patient_id);
CREATE INDEX idx_appointments_doctor_id ON appointments(doctor_id);
CREATE INDEX idx_appointments_appointment_date_time ON appointments(appointment_date_time);
CREATE INDEX idx_reviews_appointment_id ON reviews(appointment_id);
CREATE INDEX idx_notifications_user_id ON notifications(user_id);
GO

-- Index để query nhanh theo token và token_type
CREATE INDEX idx_tokens_token_token_type ON tokens(token, token_type);
GO
-- Index để query theo user_id
CREATE INDEX idx_tokens_user_id ON tokens(user_id);
GO

-- Insert một Specialization default cho Doctor (nếu cần test)
INSERT INTO specializations (name, created_at) VALUES ('General', GETDATE());
GO

-- Insert Admin user
INSERT INTO users (email, password_hash, role, verified, blocked, created_at)
VALUES (
    'admin@medconnect.vn',  -- Unique email
    'RWVgOrhLznxk2yyG8Ner8Q==',  -- Hashed password (meets BR-01: ≥ 8 characters, letters, numbers)
    'Admin',  -- Role as per Actor.docx
    1,  -- Verified = true (auto-verified for admin)
    0,  -- Blocked = false
    GETDATE()  -- created_at
);
GO

-- Log the creation of Admin account in activity_logs
INSERT INTO activity_logs (user_id, action, details, timestamp)
SELECT user_id, 'AdminAccountCreated', 'Created new admin account with email admin@medconnect.vn', GETDATE()
FROM users
WHERE email = 'admin@medconnect.vn';
GO