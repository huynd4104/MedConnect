USE MedConnectDB;
GO

-- ========== 1. BẢNG users ==========
-- Mật khẩu được hash từ "password123" bằng BCrypt
INSERT INTO users (email, password_hash, role, verified, blocked, created_at) VALUES
('patient1@example.com', '$2a$10$flEuDUA/oKhVQljmki2q/OmBwZlvAMmVzowL13NgWKStAb1XeP7jS', 'Patient', 1, 0, GETDATE()),
('patient2@example.com', '$2a$10$flEuDUA/oKhVQljmki2q/OmBwZlvAMmVzowL13NgWKStAb1XeP7jS', 'Patient', 1, 0, GETDATE()),
('patient3@example.com', '$2a$10$flEuDUA/oKhVQljmki2q/OmBwZlvAMmVzowL13NgWKStAb1XeP7jS', 'Patient', 1, 0, GETDATE()),
('patient4@example.com', '$2a$10$flEuDUA/oKhVQljmki2q/OmBwZlvAMmVzowL13NgWKStAb1XeP7jS', 'Patient', 0, 0, GETDATE()), -- Bệnh nhân chưa xác thực
('patient5@example.com', '$2a$10$flEuDUA/oKhVQljmki2q/OmBwZlvAMmVzowL13NgWKStAb1XeP7jS', 'Patient', 1, 0, GETDATE()),
('patient6@example.com', '$2a$10$flEuDUA/oKhVQljmki2q/OmBwZlvAMmVzowL13NgWKStAb1XeP7jS', 'Patient', 1, 0, GETDATE()),
('doctor1@example.com', '$2a$10$flEuDUA/oKhVQljmki2q/OmBwZlvAMmVzowL13NgWKStAb1XeP7jS', 'Doctor', 1, 0, GETDATE()),
('doctor2@example.com', '$2a$10$flEuDUA/oKhVQljmki2q/OmBwZlvAMmVzowL13NgWKStAb1XeP7jS', 'Doctor', 1, 0, GETDATE()),
('doctor3@example.com', '$2a$10$flEuDUA/oKhVQljmki2q/OmBwZlvAMmVzowL13NgWKStAb1XeP7jS', 'Doctor', 1, 0, GETDATE()),
('doctor4@example.com', '$2a$10$flEuDUA/oKhVQljmki2q/OmBwZlvAMmVzowL13NgWKStAb1XeP7jS', 'Doctor', 0, 0, GETDATE()), -- Bác sĩ chưa xác thực
('doctor5@example.com', '$2a$10$flEuDUA/oKhVQljmki2q/OmBwZlvAMmVzowL13NgWKStAb1XeP7jS', 'Doctor', 1, 0, GETDATE()),
('doctor6@example.com', '$2a$10$flEuDUA/oKhVQljmki2q/OmBwZlvAMmVzowL13NgWKStAb1XeP7jS', 'Doctor', 1, 0, GETDATE()),
('admin@medconnect.vn', '$2a$10$flEuDUA/oKhVQljmki2q/OmBwZlvAMmVzowL13NgWKStAb1XeP7jS', 'Admin', 1, 0, GETDATE());
GO

-- ========== 2. BẢNG tokens ==========
-- Khai báo ID user
DECLARE @patient1_id INT = (SELECT TOP 1 user_id FROM users WHERE email = 'patient1@example.com');
DECLARE @patient2_id INT = (SELECT TOP 1 user_id FROM users WHERE email = 'patient2@example.com');
DECLARE @patient4_id INT = (SELECT TOP 1 user_id FROM users WHERE email = 'patient4@example.com');
DECLARE @doctor1_user_id INT = (SELECT TOP 1 user_id FROM users WHERE email = 'doctor1@example.com');
DECLARE @doctor2_user_id INT = (SELECT TOP 1 user_id FROM users WHERE email = 'doctor2@example.com');
DECLARE @doctor4_user_id INT = (SELECT TOP 1 user_id FROM users WHERE email = 'doctor4@example.com');

-- Chèn tokens
INSERT INTO tokens (user_id, token, token_type, expiry_date_time, used, created_at) VALUES
                                                                                        (@patient1_id, 'reset_token_p1_xyz', 'PasswordReset', DATEADD(hour, 1, GETDATE()), 0, GETDATE()),
                                                                                        (@patient2_id, 'verify_token_p2_456', 'Verification', DATEADD(hour, 24, GETDATE()), 0, GETDATE()),
                                                                                        (@patient4_id, 'verify_token_p4_101', 'Verification', DATEADD(hour, 24, GETDATE()), 0, GETDATE()), -- Token cho user chưa verify
                                                                                        (@doctor1_user_id, 'verify_token_d1_abc', 'Verification', DATEADD(hour, 24, GETDATE()), 0, GETDATE()),
                                                                                        (@doctor2_user_id, 'reset_token_d2_uvw', 'PasswordReset', DATEADD(hour, 1, GETDATE()), 0, GETDATE()),
                                                                                        (@doctor4_user_id, 'verify_token_d4_jkl', 'Verification', DATEADD(hour, 24, GETDATE()), 0, GETDATE()); -- Token cho user chưa verify
GO

-- ========== 3. BẢNG specializations (Sử dụng Tiếng Việt) ==========
DECLARE @admin_id INT = (SELECT TOP 1 user_id FROM users WHERE email = 'admin@medconnect.vn');
INSERT INTO specializations (name, description, created_by, created_at) VALUES
                                                                            (N'Tim mạch', N'Chuyên khoa điều trị các bệnh lý về tim và mạch máu.', @admin_id, GETDATE()),
                                                                            (N'Da liễu', N'Chuyên khoa điều trị các bệnh về da, tóc, móng.', @admin_id, GETDATE()),
                                                                            (N'Nhi khoa', N'Chuyên khoa chăm sóc sức khỏe cho trẻ em và thanh thiếu niên.', @admin_id, GETDATE()),
                                                                            (N'Đa khoa', N'Khám và điều trị các bệnh lý thông thường.', @admin_id, GETDATE()),
                                                                            (N'Thần kinh', N'Chuyên khoa điều trị các rối loạn của hệ thần kinh.', @admin_id, GETDATE()),
                                                                            (N'Mắt (Nhãn khoa)', N'Chuyên khoa khám và điều trị các bệnh về mắt.', @admin_id, GETDATE()),
                                                                            (N'Cơ xương khớp', N'Chuyên khoa điều trị các bệnh lý về cơ, xương, và khớp.', @admin_id, GETDATE()),
                                                                            (N'Tai Mũi Họng', N'Chuyên khoa điều trị các bệnh lý liên quan đến tai, mũi và họng.', @admin_id, GETDATE());
GO

-- ========== 4. BẢNG doctors (ĐÃ SỬA LỖI KHAI BÁO BIẾN) ==========
-- Khai báo ID user (Bác sĩ) - (PHẢI KHAI BÁO LẠI SAU GO)
DECLARE @doctor1_user_id INT = (SELECT TOP 1 user_id FROM users WHERE email = 'doctor1@example.com');
DECLARE @doctor2_user_id INT = (SELECT TOP 1 user_id FROM users WHERE email = 'doctor2@example.com');
DECLARE @doctor3_user_id INT = (SELECT TOP 1 user_id FROM users WHERE email = 'doctor3@example.com');
DECLARE @doctor4_user_id INT = (SELECT TOP 1 user_id FROM users WHERE email = 'doctor4@example.com');
DECLARE @doctor5_user_id INT = (SELECT TOP 1 user_id FROM users WHERE email = 'doctor5@example.com');
DECLARE @doctor6_user_id INT = (SELECT TOP 1 user_id FROM users WHERE email = 'doctor6@example.com');
-- Khai báo ID Chuyên khoa (Tiếng Việt)
DECLARE @cardiology_id INT = (SELECT TOP 1 specialization_id FROM specializations WHERE name = N'Tim mạch');
DECLARE @dermatology_id INT = (SELECT TOP 1 specialization_id FROM specializations WHERE name = N'Da liễu');
DECLARE @pediatrics_id INT = (SELECT TOP 1 specialization_id FROM specializations WHERE name = N'Nhi khoa');
DECLARE @neurology_id INT = (SELECT TOP 1 specialization_id FROM specializations WHERE name = N'Thần kinh');
DECLARE @ent_id INT = (SELECT TOP 1 specialization_id FROM specializations WHERE name = N'Tai Mũi Họng');

-- Chèn dữ liệu doctors
INSERT INTO doctors (user_id, specialization_id, full_name, phone_number, clinic_address, experience_years, license_number, photo_path, introduction, status, created_at) VALUES
                                                                                                                                                                              (
                                                                                                                                                                                  @doctor1_user_id, @cardiology_id, N'Bác sĩ Nguyễn Văn A', '0911111111', N'11 Đường A, Quận 1, TP.HCM', 10, 'LIC11111', 'https://benhvienphusanmekong.com/wp-content/uploads/2022/09/bs-tho.jpg',
                                                                                                                                                                                  N'Giảng viên cao cấp – Đại học Y Hà Nội. ' + CHAR(13) + CHAR(10) +
                                                                                                                                                                                  N'Nguyên Trưởng Bộ môn Tim mạch – Đại học Y Hà Nội. ' + CHAR(13) + CHAR(10) +
                                                                                                                                                                                  N'Nguyên Giám đốc Trung tâm Tim mạch - Bệnh viện Đại học Y Hà Nội. ' + CHAR(13) + CHAR(10) +
                                                                                                                                                                                  N'Nguyên Viện trưởng Viện Tim mạch Việt Nam. ' + CHAR(13) + CHAR(10) +
                                                                                                                                                                                  N'Nguyên Hiệu trưởng Trường Đại học Y Hà Nội. ' + CHAR(13) + CHAR(10) +
                                                                                                                                                                                  N'Hơn 10 năm kinh nghiệm khám và điều trị bệnh Tim mạch.',
                                                                                                                                                                                  'Approved', GETDATE()
                                                                                                                                                                              ),
                                                                                                                                                                              (
                                                                                                                                                                                  @doctor2_user_id, @dermatology_id, N'Bác sĩ Trần Thị B', '0922222222', N'22 Đường B, Quận HB, Hà Nội', 5, 'LIC22222', 'https://benhvienphusanmekong.com/wp-content/uploads/2022/10/DTC_1340-768x1150.jpg',
                                                                                                                                                                                  N'Bác sĩ Chuyên khoa II Da liễu – Bệnh viện Da liễu Trung ương. ' + CHAR(13) + CHAR(10) +
                                                                                                                                                                                  N'Giảng viên Bộ môn Da liễu – Đại học Y Dược TP.HCM. ' + CHAR(13) + CHAR(10) +
                                                                                                                                                                                  N'Hơn 5 năm kinh nghiệm trong điều trị các bệnh lý da liễu, da thẩm mỹ và ứng dụng laser trong y học. ' + CHAR(13) + CHAR(10) +
                                                                                                                                                                                  N'Chứng chỉ đào tạo chuyên sâu về Da liễu Thẩm mỹ tại Hàn Quốc. ' + CHAR(13) + CHAR(10) +
                                                                                                                                                                                  N'Thành viên Hội Da liễu Việt Nam.',
                                                                                                                                                                                  'Approved', GETDATE()
                                                                                                                                                                              ),
                                                                                                                                                                              (
                                                                                                                                                                                  @doctor3_user_id, @pediatrics_id, N'Bác sĩ Lê Văn C', '0933333333', N'33 Đường C, Quận 3, TP.HCM', 8, 'LIC33333', 'https://benhvienphusanmekong.com/wp-content/uploads/2022/10/DTC_1101-768x1150.jpg',
                                                                                                                                                                                  N'Phó Trưởng khoa Nhi tổng hợp – Bệnh viện Nhi Trung ương. ' + CHAR(13) + CHAR(10) +
                                                                                                                                                                                  N'Chuyên gia về các bệnh lý hô hấp và tiêu hóa ở trẻ em. ' + CHAR(13) + CHAR(10) +
                                                                                                                                                                                  N'Hơn 8 năm kinh nghiệm làm việc tại các bệnh viện nhi hàng đầu. ' + CHAR(13) + CHAR(10) +
                                                                                                                                                                                  N'Tác giả nhiều bài báo khoa học về dinh dưỡng và phát triển trẻ em. ' + CHAR(13) + CHAR(10) +
                                                                                                                                                                                  N'Tận tâm, yêu trẻ và luôn cập nhật các phác đồ điều trị mới nhất.',
                                                                                                                                                                                  'Approved', GETDATE()
                                                                                                                                                                              ),
                                                                                                                                                                              (
                                                                                                                                                                                  @doctor4_user_id, @neurology_id, N'Bác sĩ Phạm Thị D', '0944444444', N'44 Đường D, Quận ĐĐ, Hà Nội', 12, 'LIC44444', 'https://benhvienphusanmekong.com/wp-content/uploads/2022/10/DTC_1513-768x1150.jpg',
                                                                                                                                                                                  N'Trưởng khoa Thần kinh – Bệnh viện Bạch Mai. ' + CHAR(13) + CHAR(10) +
                                                                                                                                                                                  N'Chuyên gia điều trị đột quỵ và các bệnh lý sa sút trí tuệ (Alzheimer, Parkinson). ' + CHAR(13) + CHAR(10) +
                                                                                                                                                                                  N'Hơn 12 năm kinh nghiệm trong lĩnh vực Thần kinh học. ' + CHAR(13) + CHAR(10) +
                                                                                                                                                                                  N'Tu nghiệp chuyên sâu về Thần kinh mạch máu tại Pháp. ' + CHAR(13) + CHAR(10) +
                                                                                                                                                                                  N'Giảng viên kiêm nhiệm – Đại học Y Hà Nội.',
                                                                                                                                                                                  'Pending', GETDATE() -- Chờ duyệt
                                                                                                                                                                              ),
                                                                                                                                                                              (
                                                                                                                                                                                  @doctor5_user_id, @cardiology_id, N'Bác sĩ Hoàng Văn E', '0955555555', N'55 Đường E, Quận 1, TP.HCM', 15, 'LIC55555', 'https://benhvienphusanmekong.com/wp-content/uploads/2022/09/DTC_1161-768x1151.jpg',
                                                                                                                                                                                  N'Chuyên gia Can thiệp Tim mạch – Viện Tim TP.HCM. ' + CHAR(13) + CHAR(10) +
                                                                                                                                                                                  N'Hơn 15 năm kinh nghiệm trong lĩnh vực nội tim mạch và can thiệp động mạch vành. ' + CHAR(13) + CHAR(10) +
                                                                                                                                                                                  N'Nguyên Phó khoa Tim mạch can thiệp – Bệnh viện Chợ Rẫy. ' + CHAR(13) + CHAR(10) +
                                                                                                                                                                                  N'Thành viên Hiệp hội Tim mạch học Việt Nam và Hiệp hội Tim mạch can thiệp Hoa Kỳ (SCAI). ' + CHAR(13) + CHAR(10) +
                                                                                                                                                                                  N'Thực hiện thành công hàng ngàn ca can thiệp mạch vành phức tạp.',
                                                                                                                                                                                  'Approved', GETDATE()
                                                                                                                                                                              ),
                                                                                                                                                                              (
                                                                                                                                                                                  @doctor6_user_id, @ent_id, N'Bác sĩ Vũ Thị F', '0966666666', N'66 Đường F, Quận BT, Hà Nội', 7, 'LIC66666', 'https://benhvienphusanmekong.com/wp-content/uploads/2022/10/DTC_1663-768x1151.jpg',
                                                                                                                                                                                  N'Bác sĩ Chuyên khoa Tai Mũi Họng – Bệnh viện Tai Mũi Họng Trung ương. ' + CHAR(13) + CHAR(10) +
                                                                                                                                                                                  N'Chuyên điều trị các bệnh lý về viêm xoang, viêm amidan, và các vấn đề về thính lực. ' + CHAR(13) + CHAR(10) +
                                                                                                                                                                                  N'Hơn 7 năm kinh nghiệm. ' + CHAR(13) + CHAR(10) +
                                                                                                                                                                                  N'Chứng chỉ Phẫu thuật nội soi mũi xoang nâng cao. ' + CHAR(13) + CHAR(10) +
                                                                                                                                                                                  N'Tận tình tư vấn và điều trị dứt điểm các bệnh lý tai mũi họng mãn tính.',
                                                                                                                                                                                  'Rejected', GETDATE()
                                                                                                                                                                              );
GO

-- ========== 5. BẢNG doctor_documents ==========
-- Khai báo ID doctor (Từ bảng doctors) - (PHẢI KHAI BÁO LẠI SAU GO)
DECLARE @doctor1_id INT = (SELECT TOP 1 doctor_id FROM doctors WHERE user_id = (SELECT user_id FROM users WHERE email = 'doctor1@example.com'));
DECLARE @doctor2_id INT = (SELECT TOP 1 doctor_id FROM doctors WHERE user_id = (SELECT user_id FROM users WHERE email = 'doctor2@example.com'));
DECLARE @doctor3_id INT = (SELECT TOP 1 doctor_id FROM doctors WHERE user_id = (SELECT user_id FROM users WHERE email = 'doctor3@example.com'));
DECLARE @doctor4_id INT = (SELECT TOP 1 doctor_id FROM doctors WHERE user_id = (SELECT user_id FROM users WHERE email = 'doctor4@example.com'));
DECLARE @doctor5_id INT = (SELECT TOP 1 doctor_id FROM doctors WHERE user_id = (SELECT user_id FROM users WHERE email = 'doctor5@example.com'));

-- Chèn tài liệu
INSERT INTO doctor_documents (doctor_id, file_path, document_type, uploaded_at) VALUES
                                                                                    (@doctor1_id, '/uploads/doc1_dr_a.pdf', 'License', GETDATE()),
                                                                                    (@doctor1_id, '/uploads/cert1_dr_a.pdf', 'Certificate', GETDATE()),
                                                                                    (@doctor1_id, '/uploads/cv_dr_a.docx', 'CV', GETDATE()),
                                                                                    (@doctor2_id, '/uploads/doc1_dr_b.jpg', 'License', GETDATE()),
                                                                                    (@doctor2_id, '/uploads/cert1_dr_b.png', 'Certificate', GETDATE()),
                                                                                    (@doctor3_id, '/uploads/doc1_dr_c.pdf', 'License', GETDATE()),
                                                                                    (@doctor3_id, '/uploads/cert1_dr_c.jpg', 'Certificate', GETDATE()),
                                                                                    (@doctor4_id, '/uploads/doc1_dr_d.pdf', 'License', GETDATE()), -- Tài liệu cho bác sĩ đang 'Pending'
                                                                                    (@doctor5_id, '/uploads/doc1_dr_e.pdf', 'License', GETDATE()),
                                                                                    (@doctor5_id, '/uploads/cert1_dr_e.pdf', 'Certificate', GETDATE());
GO

-- ========== 6. BẢNG patients ==========
-- Đảm bảo collation cho tiếng Việt (nếu cần, thường database đã set)
ALTER TABLE patients
ALTER COLUMN address NVARCHAR(500) COLLATE Vietnamese_CI_AS NULL;
GO

-- Khai báo ID user (Bệnh nhân)
DECLARE @patient1_user_id INT = (SELECT TOP 1 user_id FROM users WHERE email = 'patient1@example.com');
DECLARE @patient2_user_id INT = (SELECT TOP 1 user_id FROM users WHERE email = 'patient2@example.com');
DECLARE @patient3_user_id INT = (SELECT TOP 1 user_id FROM users WHERE email = 'patient3@example.com');
DECLARE @patient4_user_id INT = (SELECT TOP 1 user_id FROM users WHERE email = 'patient4@example.com');
DECLARE @patient5_user_id INT = (SELECT TOP 1 user_id FROM users WHERE email = 'patient5@example.com');
DECLARE @patient6_user_id INT = (SELECT TOP 1 user_id FROM users WHERE email = 'patient6@example.com');

-- Chèn dữ liệu patients
INSERT INTO patients (user_id, full_name, phone_number, address, date_of_birth, medical_history, created_at) VALUES
                                                                                                                 (@patient1_user_id, N'Trần Văn C', '0331112222', N'789 Đường KLM, Quận 3, TP.HCM', '1990-05-15', N'Tiền sử dị ứng phấn hoa.', GETDATE()),
                                                                                                                 (@patient2_user_id, N'Lê Thị D', '0445556666', N'101 Đường DEF, Quận Đống Đa, Hà Nội', '1985-11-20', N'Tiền sử bệnh hen suyễn.', GETDATE()),
                                                                                                                 (@patient3_user_id, N'Phạm Văn E', '0557778888', N'202 Đường GHI, Quận 5, TP.HCM', '2001-01-30', N'Khỏe mạnh.', GETDATE()),
                                                                                                                 (@patient4_user_id, N'Hoàng Thị F', '0669990000', N'303 Đường JKL, Quận Ba Đình, Hà Nội', '1995-07-07', N'Tiền sử đau dạ dày.', GETDATE()),
                                                                                                                 (@patient5_user_id, N'Vũ Văn G', '0771112223', N'404 Đường MNO, Quận 10, TP.HCM', '1988-03-25', NULL, GETDATE()),
                                                                                                                 (@patient6_user_id, N'Đặng Thị H', '0882223334', N'505 Đường PQR, Quận Cầu Giấy, Hà Nội', '1992-09-12', N'Tiền sử dị ứng hải sản.', GETDATE());
GO

-- ========== 7. BẢNG schedules (ĐÃ SỬA LỖI KHAI BÁO BIẾN) ==========
-- Khai báo ID doctor (Chỉ các bác sĩ 'Approved') - (PHẢI KHAI BÁO LẠI SAU GO)
DECLARE @doctor1_id INT = (SELECT TOP 1 doctor_id FROM doctors WHERE user_id = (SELECT user_id FROM users WHERE email = 'doctor1@example.com') AND status = 'Approved');
DECLARE @doctor2_id INT = (SELECT TOP 1 doctor_id FROM doctors WHERE user_id = (SELECT user_id FROM users WHERE email = 'doctor2@example.com') AND status = 'Approved');
DECLARE @doctor3_id INT = (SELECT TOP 1 doctor_id FROM doctors WHERE user_id = (SELECT user_id FROM users WHERE email = 'doctor3@example.com') AND status = 'Approved');
DECLARE @doctor5_id INT = (SELECT TOP 1 doctor_id FROM doctors WHERE user_id = (SELECT user_id FROM users WHERE email = 'doctor5@example.com') AND status = 'Approved');
-- (Bỏ qua doctor4 (Pending) và doctor6 (Rejected))

-- Chèn schedules (day_of_week: 1=CN, 2=T2, ..., 7=T7)
INSERT INTO schedules (doctor_id, day_of_week, start_time, end_time, consultation_type, active, created_at) VALUES
-- Doctor 1 (Tim mạch - 4 lịch)
(@doctor1_id, 2, '08:00:00', '11:00:00', 'InPerson', 1, GETDATE()), -- T2 Sáng (Tại phòng khám)
(@doctor1_id, 2, '13:00:00', '16:00:00', 'Online', 1, GETDATE()),   -- T2 Chiều (Online)
(@doctor1_id, 4, '09:00:00', '12:00:00', 'Online', 1, GETDATE()),   -- T4 Sáng (Online)
(@doctor1_id, 5, '14:00:00', '17:00:00', 'InPerson', 1, GETDATE()), -- T6 Chiều (Tại phòng khám)
-- Doctor 2 (Da liễu - 4 lịch)
(@doctor2_id, 3, '14:00:00', '17:00:00', 'InPerson', 1, GETDATE()), -- T3 Chiều (Tại phòng khám)
(@doctor2_id, 5, '08:30:00', '11:30:00', 'InPerson', 1, GETDATE()), -- T5 Sáng (Tại phòng khám)
(@doctor2_id, 6, '09:00:00', '12:00:00', 'Online', 1, GETDATE()),   -- T7 Sáng (Online)
(@doctor2_id, 6, '13:00:00', '15:00:00', 'Online', 1, GETDATE()),   -- T7 Chiều (Online)
-- Doctor 3 (Nhi khoa - 5 lịch)
(@doctor3_id, 2, '08:30:00', '11:30:00', 'InPerson', 1, GETDATE()), -- T2 Sáng (Tại phòng khám)
(@doctor3_id, 3, '08:30:00', '11:30:00', 'InPerson', 1, GETDATE()), -- T3 Sáng (Tại phòng khám)
(@doctor3_id, 4, '13:30:00', '16:30:00', 'Online', 1, GETDATE()),   -- T4 Chiều (Online)
(@doctor3_id, 5, '13:30:00', '16:30:00', 'Online', 1, GETDATE()),   -- T5 Chiều (Online)
(@doctor3_id, 7, '09:00:00', '11:00:00', 'InPerson', 1, GETDATE()), -- T7 Sáng (Tại phòng khám)
-- Doctor 5 (Tim mạch - 3 lịch)
(@doctor5_id, 3, '08:00:00', '12:00:00', 'InPerson', 1, GETDATE()), -- T3 Sáng (Tại phòng khám)
(@doctor5_id, 5, '08:00:00', '12:00:00', 'InPerson', 1, GETDATE()), -- T5 Sáng (Tại phòng khám)
(@doctor5_id, 6, '13:00:00', '17:00:00', 'Online', 1, GETDATE());   -- T7 Chiều (Online)
GO

PRINT 'CHÈN DỮ LIỆU MẪU CHO 7 BẢNG HOÀN TẤT.';
GO