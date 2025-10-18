package com.medconnect.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Entity
@Table(name = "doctor_documents")
@Getter
@Setter
@NoArgsConstructor
public class DoctorDocument {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "document_id")
    private Integer documentId;

    @ManyToOne
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @Column(name = "file_path", nullable = false)
    private String filePath;

    @Column(name = "document_type", nullable = false)
    private String documentType;

    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt = LocalDateTime.now();

    @Transient // Đánh dấu để JPA bỏ qua, không tạo cột này trong DB
    public String getFileName() {
        if (filePath == null || filePath.isEmpty()) {
            return null;
        }
        return StringUtils.getFilename(this.filePath);
    }
}