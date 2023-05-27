package models;

public class UserComments {
    private final User user;
    private final Comments comments;

    public UserComments(User user, Comments comments) {
        this.user = user;
        this.comments = comments;
    }

    @Override
    public String toString() {
        return "UserComments {" +
                "user=" + user +
                ", comments=" + comments +
                "}";
    }
}
