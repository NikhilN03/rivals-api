package com.rivals.model;

import java.util.List;

public record PageComment(
    List<CommentDto> items,
    String cursor  // nullable
) {}
