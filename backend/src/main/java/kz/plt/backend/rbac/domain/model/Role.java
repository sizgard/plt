package kz.plt.backend.rbac.domain.model;

public record Role(
        Long id,
        String name,
        int priority,
        boolean isSystem
) {
}