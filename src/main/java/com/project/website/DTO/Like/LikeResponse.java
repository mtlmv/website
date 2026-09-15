package com.project.website.DTO.Like;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)

public class LikeResponse {
    boolean liked;
    String massage;
}
