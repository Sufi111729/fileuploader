package com.Fileupload.sufi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.Fileupload.sufi.model.UploadedFile;

public interface UploadedFileRepository extends JpaRepository<UploadedFile, Long> {
}
