package kz.plt.backend.shared.security;

public enum Action {
    READ(1L << 0),
    CREATE(1L << 1),
    UPDATE(1L << 2),
    DELETE(1L << 3),
    ALL((1L << 0) | (1L << 1) | (1L << 2) | (1L << 3));

    private final long mask;

    Action(long mask){
        this.mask = mask;
    }
    public long mask(){
        return mask;
    }
}