package com.secureauth.productservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MediaResponse {
    private Long id;
    private String fileName;
    private String url;
    private String mimeType;
    private String fileType; // document/image/video
}
