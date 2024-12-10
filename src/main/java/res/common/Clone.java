package res.common;

public interface Clone<SELF extends Clone<SELF>> {
    SELF makeClone();
}
