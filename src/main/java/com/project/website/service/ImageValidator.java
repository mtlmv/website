package com.project.website.service;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

/**
 * Общие правила для картинок постов и комментариев: держим их в одном месте,
 * чтобы ограничения не разъехались между двумя местами загрузки.
 */
@Component
public class ImageValidator {

    /** Что реально умеет показать браузер. SVG исключён намеренно: внутри может быть скрипт. */
    private static final Set<String> ALLOWED_TYPES =
            Set.of("image/jpeg", "image/png", "image/webp", "image/gif");

    private static final long MAX_BYTES = 12L * 1024 * 1024;

    public void validate(MultipartFile image) {
        if (image.getSize() > MAX_BYTES) {
            throw new RuntimeException("Файл больше 12 МБ");
        }
        String contentType = image.getContentType();
        // Тип берём из заголовка запроса, а не из имени файла: расширение подделывается тривиально
        if (contentType == null || !ALLOWED_TYPES.contains(contentType.toLowerCase())) {
            throw new RuntimeException("Можно прикрепить только изображение: JPEG, PNG, WebP или GIF");
        }
    }
}
