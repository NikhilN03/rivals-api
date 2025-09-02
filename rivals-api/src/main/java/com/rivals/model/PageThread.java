package com.rivals.model;

import java.util.List;

public record PageThread(
    List<ThreadDto> items,
    String cursor  // nullable
) {}
