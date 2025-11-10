USE MedConnectDB;
GO

-- 1️⃣ Tắt kiểm tra khóa ngoại tạm thời
EXEC sp_msforeachtable 'ALTER TABLE ? NOCHECK CONSTRAINT ALL';

-- 2️⃣ Xóa dữ liệu theo thứ tự phụ thuộc (bảng con trước, cha sau)
DELETE FROM notifications;
DELETE FROM reviews;
DELETE FROM consultation_documents;
DELETE FROM payments;
DELETE FROM appointments;
DELETE FROM schedules;
DELETE FROM patients;
DELETE FROM doctor_documents;
DELETE FROM doctors;
DELETE FROM specializations;
DELETE FROM tokens;
DELETE FROM users;

-- 3️⃣ Bật lại kiểm tra khóa ngoại
EXEC sp_msforeachtable 'ALTER TABLE ? CHECK CONSTRAINT ALL';

-- 4️⃣ Reset lại IDENTITY về 1 cho tất cả các bảng
DBCC CHECKIDENT ('notifications', RESEED, 0);
DBCC CHECKIDENT ('reviews', RESEED, 0);
DBCC CHECKIDENT ('consultation_documents', RESEED, 0);
DBCC CHECKIDENT ('payments', RESEED, 0);
DBCC CHECKIDENT ('appointments', RESEED, 0);
DBCC CHECKIDENT ('schedules', RESEED, 0);
DBCC CHECKIDENT ('patients', RESEED, 0);
DBCC CHECKIDENT ('doctor_documents', RESEED, 0);
DBCC CHECKIDENT ('doctors', RESEED, 0);
DBCC CHECKIDENT ('specializations', RESEED, 0);
DBCC CHECKIDENT ('tokens', RESEED, 0);
DBCC CHECKIDENT ('users', RESEED, 0);

PRINT '✅ Đã xóa toàn bộ dữ liệu và khôi phục trạng thái mặc định.';
GO
