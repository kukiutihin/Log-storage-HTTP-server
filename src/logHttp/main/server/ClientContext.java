package server;

public record ClientContext(long id) {
    public String to36String() {
        return Long.toString(id, 36);
    }
}